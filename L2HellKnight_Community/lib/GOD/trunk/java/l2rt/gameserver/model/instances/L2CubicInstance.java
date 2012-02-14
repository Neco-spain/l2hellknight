package l2rt.gameserver.model.instances;

import l2rt.common.ThreadPoolManager;
import l2rt.gameserver.model.*;
import l2rt.gameserver.model.L2Skill.SkillType;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.tables.SkillTable;
import l2rt.util.GArray;
import l2rt.util.Rnd;

import java.util.concurrent.Future;
import java.util.logging.Logger;

public class L2CubicInstance
{
	protected static final Logger _log = Logger.getLogger(L2CubicInstance.class.getName());

	public static enum CubicType
	{
		STORM_CUBIC(1, 14, 80),
		VAMPIRIC_CUBIC(2, 14, 67),
		LIFE_CUBIC(3, -1, 55),
		VIPER_CUBIC(4, 14, 55),
		PHANTOM_CUBIC(5, 14, 55),
		BINDING_CUBIC(6, 14, 55),
		AQUA_CUBIC(7, 14, 67),
		SPARK_CUBIC(8, 14, 55),
		ATTRACTIVE_CUBIC(9, 11, 85),
		SMART_CUBIC_EVATEMPLAR(10, 5, 30),
		SMART_CUBIC_SHILLIENTEMPLAR(11, 5, 30),
		SMART_CUBIC_ARCANALORD(12, 5, 30),
		SMART_CUBIC_ELEMENTALMASTER(13, 5, 30),
		SMART_CUBIC_SPECTRALMASTER(14, 5, 30);

		public final int id;
		public final int delay;
		public final int chance;

		private CubicType(int id, int delay, int chance)
		{
			this.id = id;
			this.delay = delay;
			this.chance = chance;
		}

		public static CubicType getType(int id)
		{
			for(CubicType type : values())
				if(type.id == id)
					return type;
			return null;
		}
	}

	/** Оффсет для корректного сохранения кубиков в базе */
	public static final int CUBIC_STORE_OFFSET = 1000000;

	private long ownerStoreId, targetStoreId;

	private CubicType _type;
	private int _level = 1;

	private GArray<L2Skill> _offensiveSkills = new GArray<L2Skill>();
	private GArray<L2Skill> _healSkills = new GArray<L2Skill>();

	private Future<?> _disappearTask;
	private Future<?> _actionTask;
	private long _starttime;
	private long _lifetime;
	private boolean _givenByOther;

	public L2CubicInstance(final L2Player owner, final int id, final int level, final int lifetime, final boolean givenByOther)
	{
		_type = CubicType.getType(id);
		_level = level;
		_givenByOther = givenByOther;
		ownerStoreId = owner.getStoredId();

		switch(_type)
		{
			case STORM_CUBIC:
				_offensiveSkills.add(SkillTable.getInstance().getInfo(4049, level));
				break;
			case VAMPIRIC_CUBIC:
				_offensiveSkills.add(SkillTable.getInstance().getInfo(4050, level));
				break;
			case LIFE_CUBIC:
				_healSkills.add(SkillTable.getInstance().getInfo(4051, level));
				break;
			case VIPER_CUBIC:
				_offensiveSkills.add(SkillTable.getInstance().getInfo(4052, level));
				break;
			case PHANTOM_CUBIC:
				_offensiveSkills.add(SkillTable.getInstance().getInfo(4053, level));
				_offensiveSkills.add(SkillTable.getInstance().getInfo(4054, level));
				_offensiveSkills.add(SkillTable.getInstance().getInfo(4055, level));
				break;
			case BINDING_CUBIC:
				_offensiveSkills.add(SkillTable.getInstance().getInfo(4164, level));
				break;
			case AQUA_CUBIC:
				_offensiveSkills.add(SkillTable.getInstance().getInfo(4165, level));
				break;
			case SPARK_CUBIC:
				_offensiveSkills.add(SkillTable.getInstance().getInfo(4166, level));
				break;
			case ATTRACTIVE_CUBIC:
				_offensiveSkills.add(SkillTable.getInstance().getInfo(5115, level));
				_offensiveSkills.add(SkillTable.getInstance().getInfo(5116, level));
				break;
			case SMART_CUBIC_ARCANALORD:
				_healSkills.add(SkillTable.getInstance().getInfo(5579, 1)); // Cubic Cure
				_healSkills.add(SkillTable.getInstance().getInfo(4051, 7)); // Cubic Heal
				_offensiveSkills.add(SkillTable.getInstance().getInfo(4165, 9)); // Icy Air
				break;
			case SMART_CUBIC_ELEMENTALMASTER:
				_healSkills.add(SkillTable.getInstance().getInfo(5579, 1)); // Cubic Cure
				_offensiveSkills.add(SkillTable.getInstance().getInfo(4049, 8)); // Cubic Storm Strike
				_offensiveSkills.add(SkillTable.getInstance().getInfo(4166, 9)); // Shock
				break;
			case SMART_CUBIC_SPECTRALMASTER:
				_healSkills.add(SkillTable.getInstance().getInfo(5579, 1)); // Cubic Cure
				_offensiveSkills.add(SkillTable.getInstance().getInfo(4049, 8)); // Cubic Storm Strike
				_offensiveSkills.add(SkillTable.getInstance().getInfo(4052, 6)); // Poison
				break;
			case SMART_CUBIC_EVATEMPLAR:
				_healSkills.add(SkillTable.getInstance().getInfo(5579, 1)); // Cubic Cure
				_offensiveSkills.add(SkillTable.getInstance().getInfo(4053, 8)); // Decrease P.Atk
				_offensiveSkills.add(SkillTable.getInstance().getInfo(4165, 9)); // Icy Air
				break;
			case SMART_CUBIC_SHILLIENTEMPLAR:
				_healSkills.add(SkillTable.getInstance().getInfo(5579, 1)); // Cubic Cure
				_offensiveSkills.add(SkillTable.getInstance().getInfo(4049, 8)); // Cubic Storm Strike
				_offensiveSkills.add(SkillTable.getInstance().getInfo(5115, 4)); // Cubic Hate
				break;
		}

		for(L2Skill skill : _offensiveSkills)
			if(skill.getCastRange() != 1500)
				skill.setCastRange(1500); // Костыль, но так проще
		for(L2Skill skill : _healSkills)
			if(skill.getCastRange() != 1500)
				skill.setCastRange(1500); // Костыль, но так проще

		new ActionScheduler().run();

		if(_disappearTask == null)
			_disappearTask = ThreadPoolManager.getInstance().scheduleGeneral(new Disappear(), lifetime); // disappear in 20 mins
		_starttime = System.currentTimeMillis();
		_lifetime = lifetime;
	}

	public void doAction(L2Character target)
	{
		L2Player owner = L2ObjectsStorage.getAsPlayer(ownerStoreId);
		if(owner == null)
			return;
		L2Character old_target = L2ObjectsStorage.getAsCharacter(targetStoreId);
		if(old_target != null && (old_target == target || owner == target || owner.getPet() == target))
			return;
		stopAttackAction();
		targetStoreId = target.getStoredId();
	}

	public CubicType getType()
	{
		return _type;
	}

	public int getId()
	{
		return _type.id;
	}

	public int getLevel()
	{
		return _level;
	}

	public boolean isGivenByOther()
	{
		return _givenByOther;
	}

	public long lifeLeft()
	{
		return _lifetime - (System.currentTimeMillis() - _starttime);
	}

	public void stopAttackAction()
	{
		targetStoreId = 0;
	}

	public void stopAllActions()
	{
		targetStoreId = 0;
		if(_actionTask != null)
		{
			_actionTask.cancel(false);
			_actionTask = null;
		}
	}

	public void deleteMe(boolean broadcast)
	{
		stopAllActions();

		if(_disappearTask != null)
		{
			_disappearTask.cancel(false);
			_disappearTask = null;
		}

		L2Player owner = L2ObjectsStorage.getAsPlayer(ownerStoreId);
		if(owner != null)
		{
			owner.delCubic(this);
			owner.broadcastUserInfo(true);
		}

		ownerStoreId = 0;
		_offensiveSkills = null;
		_healSkills = null;
	}

	private class ActionScheduler implements Runnable
	{
		public void run()
		{
			L2Player owner = L2ObjectsStorage.getAsPlayer(ownerStoreId);
			if(owner == null || owner.isDead())
			{
				deleteMe(true);
				return;
			}
			try
			{
				boolean use = false;
				for(L2Skill skill : _healSkills)
					if(skill.getId() == 5579) // Cure - Hardcoded
					{
						for(L2Effect e : owner.getEffectList().getAllEffects())
							if(e.getSkill().isOffensive() && e.getSkill().isCancelable() && !e._template._applyOnCaster)
							{
								owner.sendPacket(new SystemMessage(SystemMessage.THE_EFFECT_OF_S1_HAS_BEEN_REMOVED).addSkillName(e.getSkill().getDisplayId(), e.getSkill().getDisplayLevel()));
								e.exit();
								use = true;
							}
						if(use)
							// TODO: разобраться почему не работает анимация
							//	owner.broadcastPacket(new MagicSkillLaunched(owner.getObjectId(), 5579, 1, owner, false));
							return;
					}
					else if(skill.getSkillType() == SkillType.HEAL)
					{
						L2Character target = owner;
						if(owner.getParty() != null)
						{
							for(L2Playable member : owner.getParty().getPartyMembersWithPets())
								if(member != null && !member.isDead() && member.getCurrentHpRatio() < target.getCurrentHpRatio() && owner.isInRangeZ(member, skill.getCastRange()))
									target = member;
						}
						else if(owner.getPet() != null && !owner.getPet().isDead() && owner.getPet().getCurrentHpRatio() < owner.getCurrentHpRatio())
							target = owner.getPet();
						double hpp = target.getCurrentHpPercents();
						if(hpp < 95 && Rnd.chance(hpp > 60 ? 44 : hpp > 30 ? 66 : 100))
						{
							owner.altUseSkill(skill, target);
							return;
						}
					}

				if(targetStoreId != 0)
				{
					L2Character target = L2ObjectsStorage.getAsCharacter(targetStoreId);
					if(target == null || target.isDead() || !owner.isInRangeZ(target, 900))
					{
						stopAttackAction();
						return;
					}

					L2Skill skill = _offensiveSkills.get(Rnd.get(_offensiveSkills.size()));
					if(Rnd.chance(_type.chance) && skill.checkCondition(owner, target, false, false, true))
					{
						owner.altUseSkill(skill, target);
						return;
					}
				}
			}
			catch(final Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				_actionTask = ThreadPoolManager.getInstance().scheduleAi(this, Rnd.get(8, 12) * owner.calculateAttackDelay(), true);
			}
		}
	}

	private class Disappear implements Runnable
	{
		public void run()
		{
			deleteMe(true);
		}
	}
}