package l2rt.gameserver.model.instances;

import l2rt.common.ThreadPoolManager;
import l2rt.database.*;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.*;
import l2rt.gameserver.model.entity.olympiad.Olympiad;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.SetSummonRemainTime;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.skills.EffectType;
import l2rt.gameserver.skills.Env;
import l2rt.gameserver.skills.effects.EffectTemplate;
import l2rt.gameserver.tables.SkillTable;
import l2rt.gameserver.taskmanager.DecayTaskManager;
import l2rt.gameserver.templates.L2NpcTemplate;
import l2rt.util.SqlBatch;

import java.sql.ResultSet;
import java.util.concurrent.Future;
import java.util.logging.Level;

public class L2SummonInstance extends L2Summon
{
	public final int CYCLE = 5000; // in millis
	private float _expPenalty = 0;
	private int _itemConsumeIdInTime;
	private int _itemConsumeCountInTime;
	private int _itemConsumeDelay;

	private Future<?> _disappearTask;

	private int _consumeCountdown;
	private int _lifetimeCountdown;
	private int _maxLifetime;
	private int _ownerObjectId;

	public L2SummonInstance(int objectId, L2NpcTemplate template, L2Player owner, int lifetime, int consumeid, int consumecount, int consumedelay)
	{
		super(objectId, template, owner);
		setName(template.name);
		_lifetimeCountdown = _maxLifetime = lifetime;
		_itemConsumeIdInTime = consumeid;
		_itemConsumeCountInTime = consumecount;
		_consumeCountdown = _itemConsumeDelay = consumedelay;
		_disappearTask = ThreadPoolManager.getInstance().scheduleGeneral(new Lifetime(this), CYCLE);
		
		// сохраним, а то при выходе игрока не сможем получить эти данные
		_ownerObjectId = owner.getObjectId();

		//TODO добавить проверки на ивенты
		// на олимпе не восстанавливаем и на ивентах
		if(owner.getOlympiadGameId() == -1 && !owner.isInOlympiadMode() && !Olympiad.isRegisteredInComp(owner) && owner.getTeam() == 0)
			restoreEffects(); // восстанавливаем эффекты суммона
	}

	@Override
	public final byte getLevel()
	{
		return getTemplate() != null ? getTemplate().level : 0;
	}

	@Override
	public int getSummonType()
	{
		return 1;
	}

	@Override
	public int getCurrentFed()
	{
		return _lifetimeCountdown;
	}

	@Override
	public int getMaxFed()
	{
		return _maxLifetime;
	}

	public void setExpPenalty(float expPenalty)
	{
		_expPenalty = expPenalty;
	}

	@Override
	public float getExpPenalty()
	{
		return _expPenalty;
	}

	@Override
	public void reduceCurrentHp(double damage, L2Character attacker, L2Skill skill, boolean awake, boolean standUp, boolean directHp, boolean canReflect)
	{
		if(attacker.isPlayable() && isInZoneBattle() != attacker.isInZoneBattle())
		{
			attacker.getPlayer().sendPacket(Msg.INVALID_TARGET);
			return;
		}

		super.reduceCurrentHp(damage, attacker, skill, awake, standUp, directHp, canReflect);

		L2Player owner = getPlayer();
		if(owner == null)
			return;

		owner.sendPacket(new SystemMessage(SystemMessage.C1_HAS_RECEIVED_DAMAGE_OF_S3_FROM_C2).addName(this).addName(attacker).addNumber((long) damage));
	}

	class Lifetime implements Runnable
	{
		private L2SummonInstance _summon;

		Lifetime(L2SummonInstance summon)
		{
			_summon = summon;
		}

		public void run()
		{
			L2Player owner = getPlayer();
			if(owner == null)
			{
				_disappearTask = null;
				unSummon();
				return;
			}

			int usedtime = _summon.isInCombat() ? CYCLE : CYCLE / 4;
			_lifetimeCountdown -= usedtime;

			if(_lifetimeCountdown <= 0)
			{
				owner.sendPacket(Msg.SERVITOR_DISAPPEASR_BECAUSE_THE_SUMMONING_TIME_IS_OVER);
				_disappearTask = null;
				unSummon();
				return;
			}

			_consumeCountdown -= usedtime;
			if(_itemConsumeIdInTime > 0 && _itemConsumeCountInTime > 0 && _consumeCountdown <= 0)
			{
				L2ItemInstance item = owner.getInventory().getItemByItemId(_summon.getItemConsumeIdInTime());
				if(item != null && item.getCount() >= _summon.getItemConsumeCountInTime())
				{
					_consumeCountdown = _itemConsumeDelay;
					L2ItemInstance dest = owner.getInventory().destroyItemByItemId(_summon.getItemConsumeIdInTime(), _summon.getItemConsumeCountInTime(), true);
					owner.sendPacket(new SystemMessage(SystemMessage.A_SUMMONED_MONSTER_USES_S1).addItemName(dest.getItemId()));
				}
				else
				{
					owner.sendPacket(Msg.SINCE_YOU_DO_NOT_HAVE_ENOUGH_ITEMS_TO_MAINTAIN_THE_SERVITORS_STAY_THE_SERVITOR_WILL_DISAPPEAR);
					_summon.unSummon();
				}
			}

			owner.sendPacket(new SetSummonRemainTime(_summon));

			_disappearTask = ThreadPoolManager.getInstance().scheduleGeneral(new Lifetime(_summon), CYCLE);
		}
	}

	@Override
	public void doDie(L2Character killer)
	{
		super.doDie(killer);
		
		// сохраняем эффекты суммона
		storeEffects();
		if(isSalvation() && !getPlayer().isInOlympiadMode())
			getPlayer().reviveRequest(getPlayer(), 100, false);
		for(L2Effect e : getEffectList().getAllEffects())
			if(e.getEffectType() == EffectType.BlessNoblesse || e.getSkill().getId() == L2Skill.SKILL_FORTUNE_OF_NOBLESSE || e.getSkill().getId() == L2Skill.SKILL_RAID_BLESSING)
				e.exit();

		if(_disappearTask != null)
		{
			_disappearTask.cancel(false);
			_disappearTask = null;
		}

		DecayTaskManager.getInstance().addDecayTask(this);
	}

	public int getItemConsumeIdInTime()
	{
		return _itemConsumeIdInTime;
	}

	public int getItemConsumeCountInTime()
	{
		return _itemConsumeCountInTime;
	}

	public int getItemConsumeDelay()
	{
		return _itemConsumeDelay;
	}

	protected synchronized void stopDisappear()
	{
		if(_disappearTask != null)
		{
			_disappearTask.cancel(true);
			_disappearTask = null;
		}
	}

	@Override
	public void unSummon()
	{
		// сохраняем эффекты суммона
		storeEffects();
		stopDisappear();
		super.unSummon();
	}

	@Override
	public void displayHitMessage(L2Character target, int damage, boolean crit, boolean miss)
	{
		L2Player owner = getPlayer();
		if(owner == null)
			return;
		if(crit)
			owner.sendPacket(Msg.SUMMONED_MONSTERS_CRITICAL_HIT);
		if(miss)
			owner.sendPacket(new SystemMessage(SystemMessage.C1S_ATTACK_WENT_ASTRAY).addName(this));
		else if(!target.isInvul())
			owner.sendPacket(new SystemMessage(SystemMessage.C1_HAS_GIVEN_C2_DAMAGE_OF_S3).addName(this).addName(target).addNumber(damage));
	}
	
	/**
	 * сохраняет эффекты для суммона
	 */
	public void storeEffects()
	{
		ThreadConnection con = null;
		FiltredStatement statement = null;
		try
		{

			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();
			statement.executeUpdate("DELETE FROM summon_effects_save WHERE char_obj_id = " + _ownerObjectId + " AND npc_id=" + getNpcId());

			if(_effectList == null || _effectList.isEmpty())
				return;

			int order = 0;
			SqlBatch b = new SqlBatch("INSERT IGNORE INTO `summon_effects_save` (`char_obj_id`,`npc_id`,`skill_id`,`skill_level`,`effect_count`,`effect_cur_time`,`duration`,`order`) VALUES");
			synchronized (getEffectList())
			{
				StringBuilder sb;
				for(L2Effect effect : getEffectList().getAllEffects())
					if(effect != null && effect.isInUse() && !effect.getSkill().isToggle() && effect.getEffectType() != EffectType.HealOverTime && effect.getEffectType() != EffectType.CombatPointHealOverTime)
					{
						if(effect.isSaveable())
						{
							sb = new StringBuilder("(");
							sb.append(_ownerObjectId).append(",");
							sb.append(getNpcId()).append(",");
							sb.append(effect.getSkill().getId()).append(",");
							sb.append(effect.getSkill().getLevel()).append(",");
							sb.append(effect.getCount()).append(",");
							sb.append(effect.getTime()).append(",");
							sb.append(effect.getPeriod()).append(",");
							sb.append(order).append(")");
							b.write(sb.toString());
						}
						while((effect = effect.getNext()) != null && effect.isSaveable())
						{
							sb = new StringBuilder("(");
							sb.append(_ownerObjectId).append(",");
							sb.append(getNpcId()).append(",");
							sb.append(effect.getSkill().getId()).append(",");
							sb.append(effect.getSkill().getLevel()).append(",");
							sb.append(effect.getCount()).append(",");
							sb.append(effect.getTime()).append(",");
							sb.append(effect.getPeriod()).append(",");
							sb.append(order).append(")");
							b.write(sb.toString());
						}
						order++;
					}
			}
			if(!b.isEmpty())
				statement.executeUpdate(b.close());
		}
		catch(Exception e)
		{
			_log.log(Level.WARNING, "L2SummonInstance.storeEffects() error: ", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * восстанавливает эффекты для суммона
	 */
	public void restoreEffects()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		FiltredStatement statement1 = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT `skill_id`,`skill_level`,`effect_count`,`effect_cur_time`,`duration` FROM `summon_effects_save` WHERE `char_obj_id`=? AND `npc_id`=? ORDER BY `order` ASC");
			statement.setInt(1, _ownerObjectId);
			statement.setInt(2, getNpcId());
			rset = statement.executeQuery();
			while(rset.next())
			{
				int skillId = rset.getInt("skill_id");
				int skillLvl = rset.getInt("skill_level");
				int effectCount = rset.getInt("effect_count");
				long effectCurTime = rset.getLong("effect_cur_time");
				long duration = rset.getLong("duration");

				L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLvl);
				if(skill == null)
				{
					_log.warning("Can't restore Effect\tskill: " + skillId + ":" + skillLvl + " " + toString());
					Thread.dumpStack();
					continue;
				}

				if(skill.getEffectTemplates() == null)
				{
					_log.warning("Can't restore Effect, EffectTemplates is NULL\tskill: " + skillId + ":" + skillLvl + " " + toString());
					Thread.dumpStack();
					continue;
				}

				for(EffectTemplate et : skill.getEffectTemplates())
				{
					if(et == null)
						continue;
					Env env = new Env(this, this, skill);
					L2Effect effect = et.getEffect(env);
					if(effect == null)
						continue;
					if(effectCount == 1)
					{
						effect.setCount(effectCount);
						effect.setPeriod(duration - effectCurTime);
					}
					else
					{
						effect.setPeriod(duration);
						effect.setCount(effectCount);
					}
					getEffectList().addEffect(effect);
				}
			}
			statement1 = con.createStatement();
			statement1.executeUpdate("DELETE FROM summon_effects_save WHERE char_obj_id = " + _ownerObjectId + " AND npc_id=" + getNpcId());
		}
		catch(Exception e)
		{
			_log.log(Level.WARNING, "L2SummonInstance.restoreEffects() error: ", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}

		updateEffectIcons();
		broadcastPetInfo(); // обновляем иконки баффов
	}
}