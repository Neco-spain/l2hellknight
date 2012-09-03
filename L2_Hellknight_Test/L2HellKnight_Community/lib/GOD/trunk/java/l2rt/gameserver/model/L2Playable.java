package l2rt.gameserver.model;

import l2rt.Config;
import l2rt.extensions.multilang.CustomMessage;
import l2rt.gameserver.ai.CtrlEvent;
import l2rt.gameserver.ai.CtrlIntention;
import l2rt.gameserver.ai.DefaultAI;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.geodata.GeoEngine;
import l2rt.gameserver.instancemanager.SiegeManager;
import l2rt.gameserver.model.L2Skill.SkillTargetType;
import l2rt.gameserver.model.L2Skill.SkillType;
import l2rt.gameserver.model.L2Zone.ZoneType;
import l2rt.gameserver.model.entity.Duel;
import l2rt.gameserver.model.entity.Duel.DuelState;
import l2rt.gameserver.model.instances.L2DoorInstance;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.instances.L2TerritoryFlagInstance;
import l2rt.gameserver.model.items.Inventory;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.model.quest.QuestState;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.skills.Stats;
import l2rt.gameserver.tables.SkillTable;
import l2rt.gameserver.templates.L2CharTemplate;
import l2rt.gameserver.templates.L2EtcItem;
import l2rt.gameserver.templates.L2Weapon;
import l2rt.gameserver.templates.L2Weapon.WeaponType;
import l2rt.util.GArray;
import l2rt.util.GCSArray;
import l2rt.util.Rnd;

import java.util.Map.Entry;

import static l2rt.gameserver.model.L2Zone.ZoneType.Siege;
import static l2rt.gameserver.model.L2Zone.ZoneType.peace_zone;

public abstract class L2Playable extends L2Character
{
	private byte _isSilentMoving;

	private long _checkAggroTimestamp = 0;

	public L2Playable(int objectId, L2CharTemplate template)
	{
		super(objectId, template);
	}

	public abstract Inventory getInventory();

	/**
	 * Проверяет, выставлять ли PvP флаг для игрока.<BR><BR>
	 */
	@Override
	public boolean checkPvP(final L2Character target, L2Skill skill)
	{
		L2Player player = getPlayer();

		if(isDead() || target == null || player == null || target == this || target == player || target == player.getPet() || player.getKarma() < 0)
			return false;

		if(skill != null)
		{
			if(skill.altUse())
				return false;
			if(skill.getSkillType().equals(SkillType.BEAST_FEED))
				return false;
			if(skill.getTargetType() == SkillTargetType.TARGET_UNLOCKABLE)
				return false;
			if(skill.getTargetType() == SkillTargetType.TARGET_CHEST)
				return false;
		}

		// Проверка на дуэли... Мэмбэры одной дуэли не флагаются
		if(getDuel() != null && getDuel() == target.getDuel())
			return false;

		if(isInZone(peace_zone) || target.isInZone(peace_zone) || isInZoneBattle() || target.isInZoneBattle())
			return false;
		if(isInZone(Siege) && target.isInZone(Siege))
			return false;
		if(skill == null || skill.isOffensive())
		{
			if(target.getKarma() < 0)
				return false;
			else if(target.isPlayable())
				return true;
		}
		else if(target.getPvpFlag() > 0 || target.getKarma() < 0 || target.isMonster())
			return true;

		return false;
	}

	/**
	 * Проверяет, можно ли атаковать цель (для физ атак)
	 */
	public boolean checkAttack(L2Character target)
	{
		L2Player player = getPlayer();
		if(player == null)
			return false;

		if(target == null || target.isDead())
		{
			player.sendPacket(Msg.INVALID_TARGET);
			return false;
		}

		if(!isInRange(target, 2000))
		{
			player.sendPacket(Msg.YOUR_TARGET_IS_OUT_OF_RANGE);
			return false;
		}

		if(target.isDoor() && !((L2DoorInstance) target).isAttackable(this))
		{
			player.sendPacket(Msg.INVALID_TARGET);
			return false;
		}

		if(target.paralizeOnAttack(player))
		{
			if(Config.PARALIZE_ON_RAID_DIFF)
				paralizeMe(target);
			return false;
		}

		if(!GeoEngine.canSeeTarget(this, target, false) || getReflection() != target.getReflection())
		{
			player.sendPacket(Msg.CANNOT_SEE_TARGET);
			return false;
		}

		// Запрет на атаку мирных NPC в осадной зоне на TW. Иначе таким способом набивают очки.
		if(player.getTerritorySiege() > -1 && target.isNpc() && !(target instanceof L2TerritoryFlagInstance) && !(target.getAI() instanceof DefaultAI) && player.isInZone(ZoneType.Siege))
		{
			player.sendPacket(Msg.INVALID_TARGET);
			return false;
		}

		if(target.isPlayable())
		{
			// Нельзя атаковать того, кто находится на арене, если ты сам не на арене
			if(isInZoneBattle() != target.isInZoneBattle())
			{
				player.sendPacket(Msg.INVALID_TARGET);
				return false;
			}

			// Если цель либо атакующий находится в мирной зоне - атаковать нельзя
			if((isInZonePeace() || target.isInZonePeace()) && !player.getPlayerAccess().PeaceAttack)
			{
				player.sendPacket(Msg.YOU_MAY_NOT_ATTACK_THIS_TARGET_IN_A_PEACEFUL_ZONE);
				return false;
			}
			if(player.isInOlympiadMode() && !player.isOlympiadCompStart())
				return false;
		}

		return true;
	}

	@Override
	public void doAttack(L2Character target)
	{
		L2Player player = getPlayer();
		if(player == null)
			return;

		if(isAMuted() || isAttackingNow())
		{
			player.sendActionFailed();
			return;
		}

		if(player.inObserverMode())
		{
			player.sendMessage(new CustomMessage("l2rt.gameserver.model.L2Playable.OutOfControl.ObserverNoAttack", player));
			return;
		}

		if(!checkAttack(target))
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
			player.sendActionFailed();
			return;
		}

		// Прерывать дуэли если цель не дуэлянт
		if(getDuel() != null)
			if(target.getDuel() != getDuel())
				getDuel().setDuelState(player.getStoredId(), DuelState.Interrupted);
			else if(getDuel().getDuelState(player.getStoredId()) == DuelState.Interrupted)
			{
				player.sendPacket(Msg.INVALID_TARGET);
				return;
			}

		L2Weapon weaponItem = getActiveWeaponItem();

		if(weaponItem != null && (weaponItem.getItemType() == WeaponType.BOW || weaponItem.getItemType() == WeaponType.CROSSBOW))
		{
			double bowMpConsume = weaponItem.getMpConsume();
			if(bowMpConsume > 0)
			{
				// cheap shot SA
				double chance = calcStat(Stats.MP_USE_BOW_CHANCE, 0., target, null);
				if(chance > 0 && Rnd.chance(chance))
					bowMpConsume = calcStat(Stats.MP_USE_BOW, bowMpConsume, target, null);

				if(_currentMp < bowMpConsume)
				{
					getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
					player.sendPacket(Msg.NOT_ENOUGH_MP);
					player.sendActionFailed();
					return;
				}

				reduceCurrentMp(bowMpConsume, null);
			}

			if(!player.checkAndEquipArrows())
			{
				getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
				player.sendPacket(player.getActiveWeaponInstance().getItemType() == WeaponType.BOW ? Msg.YOU_HAVE_RUN_OUT_OF_ARROWS : Msg.NOT_ENOUGH_BOLTS);
				player.sendActionFailed();
				return;
			}
		}

		super.doAttack(target);
	}

	private GCSArray<QuestState> _NotifyQuestOfDeathList;
	private GCSArray<QuestState> _NotifyQuestOfPlayerKillList;

	public void addNotifyQuestOfDeath(QuestState qs)
	{
		if(qs == null || _NotifyQuestOfDeathList != null && _NotifyQuestOfDeathList.contains(qs))
			return;
		if(_NotifyQuestOfDeathList == null)
			_NotifyQuestOfDeathList = new GCSArray<QuestState>();
		_NotifyQuestOfDeathList.add(qs);
	}

	public void addNotifyOfPlayerKill(QuestState qs)
	{
		if(qs == null || _NotifyQuestOfPlayerKillList != null && _NotifyQuestOfPlayerKillList.contains(qs))
			return;
		if(_NotifyQuestOfPlayerKillList == null)
			_NotifyQuestOfPlayerKillList = new GCSArray<QuestState>();
		_NotifyQuestOfPlayerKillList.add(qs);
	}

	public void removeNotifyOfPlayerKill(QuestState qs)
	{
		if(qs == null || _NotifyQuestOfPlayerKillList == null)
			return;
		_NotifyQuestOfPlayerKillList.remove(qs);
		if(_NotifyQuestOfPlayerKillList.isEmpty())
			_NotifyQuestOfPlayerKillList = null;
	}

	public GCSArray<QuestState> getNotifyOfPlayerKillList()
	{
		return _NotifyQuestOfPlayerKillList;
	}

	@Override
	public void doDie(L2Character killer)
	{
		super.doDie(killer);

		if(killer != null)
		{
			L2Player pk = killer.getPlayer();
			L2Player player = getPlayer();
			if(pk != null && player != null)
			{
				L2Party party = pk.getParty();
				if(party == null)
				{
					GCSArray<QuestState> killList = pk.getNotifyOfPlayerKillList();
					if(killList != null)
						for(QuestState qs : killList)
							qs.getQuest().notifyPlayerKill(player, qs);
				}
				else
					for(L2Player member : party.getPartyMembers())
						if(member != null && member.isInRange(pk, 2000))
						{
							GCSArray<QuestState> killList = member.getNotifyOfPlayerKillList();
							if(killList != null)
								for(QuestState qs : killList)
									qs.getQuest().notifyPlayerKill(player, qs);
						}
			}
		}

		if(_NotifyQuestOfDeathList != null)
		{
			for(QuestState qs : _NotifyQuestOfDeathList)
				qs.getQuest().notifyDeath(killer, this, qs);
			_NotifyQuestOfDeathList = null;
		}
	}

	@Override
	public int getPAtkSpd()
	{
		return Math.max((int) (calcStat(Stats.POWER_ATTACK_SPEED, calcStat(Stats.ATK_BASE, _template.basePAtkSpd, null, null), null, null) / getArmourExpertisePenalty()), 1);
	}

	@Override
	public int getPAtk(final L2Character target)
	{
		double init = getActiveWeaponInstance() == null ? _template.basePAtk : 0;
		return (int) calcStat(Stats.POWER_ATTACK, init, target, null);
	}

	@Override
	public int getMAtk(final L2Character target, final L2Skill skill)
	{
		if(skill != null && skill.getMatak() > 0)
			return skill.getMatak();
		final double init = getActiveWeaponInstance() == null ? _template.baseMAtk : 0;
		return (int) calcStat(Stats.MAGIC_ATTACK, init, target, skill);
	}

	@Override
	public boolean isAttackable(L2Character attacker)
	{
		return checkTarget(attacker, true);
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return checkTarget(attacker, false);
	}

	private boolean checkTarget(L2Character attacker, boolean force)
	{
		L2Player player = getPlayer();
		if(attacker == null || player == null || attacker == this || attacker == player || isDead() || attacker.isAlikeDead())
			return false;

		if(!GeoEngine.canSeeTarget(attacker, this, false) || getReflection() != attacker.getReflection() || isInvisible())
			return false;

		if(isInVehicle())
			return false;

		L2Player pcAttacker = attacker.getPlayer();
		L2Clan clan1 = player.getClan();
		if(pcAttacker != null)
		{
			if(pcAttacker.isInVehicle())
				return false;

			// Только враг и только если он еше не проиграл.
			Duel duel1 = player.getDuel();
			Duel duel2 = pcAttacker.getDuel();
			if(player != pcAttacker && duel1 != null && duel1 == duel2)
			{
				if(duel1.getTeamForPlayer(pcAttacker) == duel1.getTeamForPlayer(player))
					return false;
				if(duel1.getDuelState(player.getStoredId()) != Duel.DuelState.Fighting)
					return false;
				if(duel1.getDuelState(pcAttacker.getStoredId()) != Duel.DuelState.Fighting)
					return false;
				return true;
			}

			if(!force && duel1 != null && duel1 != duel2)
				return false;

			if(player.isInZone(ZoneType.epic) != pcAttacker.isInZone(ZoneType.epic))
				return false;

			if((player.isInOlympiadMode() || pcAttacker.isInOlympiadMode()) && player.getOlympiadGameId() != pcAttacker.getOlympiadGameId()) // На всякий случай
				return false;
			if(player.isInOlympiadMode() && !player.isOlympiadCompStart()) // Бой еще не начался
				return false;
			if(player.isInOlympiadMode() && player.isOlympiadCompStart() && player.getOlympiadSide() == pcAttacker.getOlympiadSide()) // Свою команду атаковать нельзя
				return false;

			if(pcAttacker.getTeam() > 0 && pcAttacker.isChecksForTeam() && player.getTeam() == 0) // Запрет на атаку/баф участником эвента незарегистрированного игрока
				return false;
			if(player.getTeam() > 0 && player.isChecksForTeam() && pcAttacker.getTeam() == 0) // Запрет на атаку/баф участника эвента незарегистрированным игроком
				return false;
			if(player.getTeam() > 0 && player.isChecksForTeam() && pcAttacker.getTeam() > 0 && pcAttacker.isChecksForTeam() && player.getTeam() == pcAttacker.getTeam()) // Свою команду атаковать нельзя
				return false;

			if(isInZoneBattle() != attacker.isInZoneBattle() && !player.getPlayerAccess().PeaceAttack)
				return false;
			if((isInZonePeace() || pcAttacker.isInZonePeace()) && !player.getPlayerAccess().PeaceAttack)
				return false;
			if(!force && player.getParty() != null && player.getParty() == pcAttacker.getParty())
				return false;

			if(isInZoneBattle())
				return true; // Остальные условия на аренах и на олимпиаде проверять не требуется

			if(!force && player.getClanId() != 0 && player.getClanId() == pcAttacker.getClanId())
				return false;

			if(isInZone(ZoneType.Siege) && attacker.isInZone(ZoneType.Siege))
			{
				if(player.getTerritorySiege() > -1 && player.getTerritorySiege() == pcAttacker.getTerritorySiege())
					return false;
				L2Clan clan2 = pcAttacker.getClan();
				if(clan1 == null || clan2 == null)
					return true;
				if(clan1.getSiege() == null || clan2.getSiege() == null)
					return true;
				if(clan1.getSiege() != clan2.getSiege())
					return true;
				if(clan1.isDefender() && clan2.isDefender())
					return false;
				if(clan1.getSiege().isMidVictory())
					return true;
				if(clan1.isAttacker() && clan2.isAttacker())
					return false;
				return true;
			}

			if(pcAttacker.atMutualWarWith(player))
				return true;
			// Защита от развода на флаг с копьем
			if(!force && pcAttacker.getPvpFlag() == 0 && getPvpFlag() != 0 && pcAttacker.getAI() != null && pcAttacker.getAI().getAttackTarget() != this)
				return false;
			if(player.getKarma() > 0 || player.getPvpFlag() != 0)
				return true;

			return force;
		}

		if(attacker.isSiegeGuard() && clan1 != null && clan1.isDefender() && SiegeManager.getSiege(this, true) == clan1.getSiege())
			return false;
		if(!force && isInZonePeace()) // Гварды с пикой, будут атаковать только одиночные цели в городе
			return false;

		return true;
	}

	@Override
	public int getKarma()
	{
		L2Player player = getPlayer();
		return player == null ? 0 : player.getKarma();
	}

	@Override
	public void callSkill(L2Skill skill, GArray<L2Character> targets, boolean useActionSkills)
	{
		L2Player player = getPlayer();
		if(player == null)
			return;

		GArray<L2Character> toRemove = new GArray<L2Character>();

		if(useActionSkills && !skill.altUse() && !skill.getSkillType().equals(SkillType.BEAST_FEED))
			for(L2Character target : targets)
			{
				if(target.isInvul() && skill.isOffensive() && (skill.getSkillType() != SkillType.STEAL_BUFF || (target.isPlayer() && ((L2Player) target).isGM())) && !target.isArtefact())
					toRemove.add(target);

				if(!skill.isOffensive())
				{
					if(target.isPlayable() && target != getPet() && !(this instanceof L2Summon && target == player))
					{
						int aggro = skill.getEffectPoint() != 0 ? skill.getEffectPoint() : Math.max(1, (int) skill.getPower());
						for(Entry<L2NpcInstance, HateInfo> entry : target.getHateList().entrySet())
							if(entry.getKey() != null && !entry.getKey().isDead() && entry.getValue().hate > 0 && entry.getKey().isInRange(this, 2000) && entry.getKey().getAI().getIntention() == CtrlIntention.AI_INTENTION_ATTACK)
							{
								if(!skill.isHandler() && entry.getKey().paralizeOnAttack(player))
								{
									if(Config.PARALIZE_ON_RAID_DIFF)
										paralizeMe(entry.getKey());
									return;
								}
								entry.getKey().getAI().notifyEvent(CtrlEvent.EVT_SEE_SPELL, skill, this);
								if(GeoEngine.canSeeTarget(entry.getKey(), target, false)) // Моб агрится только если видит цель, которую лечишь/бафаешь.
									entry.getKey().getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, this, aggro);
							}
					}
				}
				else if(target.isNpc())
				{
					// mobs will hate on debuff
					if(target.paralizeOnAttack(player))
					{
						if(Config.PARALIZE_ON_RAID_DIFF)
							paralizeMe(target);
						return;
					}
					target.getAI().notifyEvent(CtrlEvent.EVT_SEE_SPELL, skill, this);
					if(!skill.isAI())
					{
						int damage = skill.getEffectPoint() != 0 ? skill.getEffectPoint() : 1;
						target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, this, damage);
						target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, this, damage);
					}
				}
				// Check for PvP Flagging / Drawing Aggro
				if(checkPvP(target, skill))
					startPvPFlag(target);
			}

		for(L2Character cha : toRemove)
			targets.remove(cha);

		super.callSkill(skill, targets, useActionSkills);
	}

	@Override
	public void setXYZ(int x, int y, int z, boolean MoveTask)
	{
		super.setXYZ(x, y, z, MoveTask);
		L2Player player = getPlayer();

		if(!MoveTask || player == null || isAlikeDead() || isInvul() || !isVisible() || getCurrentRegion() == null)
			return;

		long now = System.currentTimeMillis();
		if(now - _checkAggroTimestamp < Config.AGGRO_CHECK_INTERVAL || player.getNonAggroTime() > now)
			return;

		_checkAggroTimestamp = now;
		if(getAI().getIntention() == CtrlIntention.AI_INTENTION_FOLLOW && (!isPlayer() || getFollowTarget() != null && getFollowTarget().getPlayer() != null && !getFollowTarget().getPlayer().isSilentMoving()))
			return;

		for(L2NpcInstance obj : L2World.getAroundNpc(this))
			if(obj != null)
				obj.getAI().checkAggression(this);
	}

	/**
	 * Оповещает других игроков о поднятии вещи
	 * @param item предмет который был поднят
	 */
	public void broadcastPickUpMsg(L2ItemInstance item)
	{
		L2Player player = getPlayer();

		if(item == null || player == null || player.isInvisible())
			return;

		if(item.isEquipable() && !(item.getItem() instanceof L2EtcItem))
		{
			SystemMessage msg = null;
			String player_name = player.getName();
			if(item.getEnchantLevel() > 0)
			{
				int msg_id = isPlayer() ? SystemMessage.ATTENTION_S1_PICKED_UP__S2_S3 : SystemMessage.ATTENTION_S1_PET_PICKED_UP__S2_S3;
				msg = new SystemMessage(msg_id).addString(player_name).addNumber(item.getEnchantLevel()).addItemName(item.getItemId());
			}
			else
			{
				int msg_id = isPlayer() ? SystemMessage.ATTENTION_S1_PICKED_UP_S2 : SystemMessage.ATTENTION_S1_PET_PICKED_UP__S2_S3;
				msg = new SystemMessage(msg_id).addString(player_name).addItemName(item.getItemId());
			}
			player.broadcastPacket(msg);
		}
	}

	public void paralizeMe(L2Character effector)
	{
		L2Skill revengeSkill = SkillTable.getInstance().getInfo(L2Skill.SKILL_RAID_CURSE, 1);
		L2Player player = getPlayer();
		if(player != this)
			revengeSkill.getEffects(effector, this, false, false);
		if(player != null)
			revengeSkill.getEffects(effector, player, false, false);
	}

	/**
	 * Set the Silent Moving mode Flag.<BR><BR>
	 */
	public void setSilentMoving(final boolean flag)
	{
		if(flag)
			_isSilentMoving++;
		else
			_isSilentMoving--;
	}

	/**
	 * @return True if the Silent Moving mode is active.<BR><BR>
	 */
	public boolean isSilentMoving()
	{
		return _isSilentMoving > 0;
	}
}