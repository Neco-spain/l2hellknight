package l2rt.gameserver.skills.skillclasses;

import l2rt.extensions.multilang.CustomMessage;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.instancemanager.SiegeManager;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Clan;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.entity.residence.ResidenceType;
import l2rt.gameserver.model.entity.siege.Siege;
import l2rt.gameserver.model.entity.siege.SiegeClan;
import l2rt.gameserver.model.entity.siege.castle.CastleSiege;
import l2rt.gameserver.model.entity.siege.territory.TerritorySiege;
import l2rt.gameserver.model.instances.L2PetInstance;
import l2rt.gameserver.skills.Formulas;
import l2rt.gameserver.templates.StatsSet;
import l2rt.util.GArray;

public class Resurrect extends L2Skill
{
	private final boolean _canPet;

	public Resurrect(StatsSet set)
	{
		super(set);
		_canPet = set.getBool("canPet", false);
	}

	@Override
	public boolean checkCondition(final L2Character activeChar, final L2Character target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(!activeChar.isPlayer())
			return false;

		if(target == null || target != activeChar && !target.isDead())
		{
			activeChar.sendPacket(Msg.THAT_IS_THE_INCORRECT_TARGET);
			return false;
		}

		L2Player player = (L2Player) activeChar;
		L2Player pcTarget = target.getPlayer();

		if(pcTarget == null)
		{
			player.sendPacket(Msg.THAT_IS_THE_INCORRECT_TARGET);
			return false;
		}

		if(player.isInOlympiadMode() || pcTarget.isInOlympiadMode())
		{
			player.sendPacket(Msg.THAT_IS_THE_INCORRECT_TARGET);
			return false;
		}

		if(TerritorySiege.checkIfInZone(target))
		{
			if(pcTarget.getTerritorySiege() == -1) // Не зарегистрирован на осаду
			{
				activeChar.sendPacket(Msg.IT_IS_IMPOSSIBLE_TO_BE_RESSURECTED_IN_BATTLEFIELDS_WHERE_SIEGE_WARS_ARE_IN_PROCESS);
				return false;
			}

			L2Clan clan = pcTarget.getClan();
			SiegeClan siegeClan = TerritorySiege.getSiegeClan(clan);
			if(siegeClan == null || siegeClan.getHeadquarter() == null) // Возможно, стоит разрешить воскрешаться одиночкам
			{
				activeChar.sendPacket(Msg.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE);
				return false;
			}
		}

		Siege siege = SiegeManager.getSiege(target, true);
		if(siege != null)
		{
			L2Clan clan = pcTarget.getClan();
			if(clan == null || clan.getSiege() == null || clan.getSiege() != siege) // Не зарегистрирован на осаду
			{
				activeChar.sendPacket(Msg.IT_IS_IMPOSSIBLE_TO_BE_RESSURECTED_IN_BATTLEFIELDS_WHERE_SIEGE_WARS_ARE_IN_PROCESS);
				return false;
			}

			// Атакующая сторона, проверка на наличие флага
			SiegeClan attackClan = siege.getAttackerClan(clan);
			if(attackClan != null && attackClan.getHeadquarter() == null)
			{
				activeChar.sendPacket(Msg.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE);
				return false;
			}

			// Защищающая сторона, проверка на наличие кристалов в замке
			if(siege.checkIsDefender(clan) && siege.getSiegeUnit().getType() == ResidenceType.Castle && ((CastleSiege) siege).isAllTowersDead())
			{
				activeChar.sendPacket(Msg.THE_GUARDIAN_TOWER_HAS_BEEN_DESTROYED_AND_RESURRECTION_IS_NOT_POSSIBLE);
				return false;
			}
		}

		if(oneTarget())
			if(target.isPet())
			{
				if(pcTarget.isReviveRequested())
				{
					if(pcTarget.isRevivingPet())
						activeChar.sendPacket(Msg.BETTER_RESURRECTION_HAS_BEEN_ALREADY_PROPOSED);
					else
						activeChar.sendPacket(Msg.SINCE_THE_MASTER_WAS_IN_THE_PROCESS_OF_BEING_RESURRECTED_THE_ATTEMPT_TO_RESURRECT_THE_PET_HAS_BEEN_CANCELLED);
					return false;
				}
				if(!(_canPet || _targetType == SkillTargetType.TARGET_PET))
				{
					player.sendPacket(Msg.THAT_IS_THE_INCORRECT_TARGET);
					return false;
				}
			}
			else if(target.isPlayer())
			{
				if(pcTarget.isReviveRequested())
				{
					if(pcTarget.isRevivingPet())
						activeChar.sendPacket(Msg.WHILE_A_PET_IS_ATTEMPTING_TO_RESURRECT_IT_CANNOT_HELP_IN_RESURRECTING_ITS_MASTER); // While a pet is attempting to resurrect, it cannot help in resurrecting its master.
					else
						activeChar.sendPacket(Msg.BETTER_RESURRECTION_HAS_BEEN_ALREADY_PROPOSED); // Resurrection is already been proposed.
					return false;
				}
				if(_targetType == SkillTargetType.TARGET_PET)
				{
					player.sendPacket(Msg.THAT_IS_THE_INCORRECT_TARGET);
					return false;
				}
			}

		return super.checkCondition(activeChar, target, forceUse, dontMove, first);
	}

	@Override
	public void useSkill(L2Character activeChar, GArray<L2Character> targets)
	{
		double percent = _power;

		if(percent < 100 && !isHandler())
		{
			double wit_bonus = _power * (Formulas.WITbonus[activeChar.getWIT()] - 1);
			percent += wit_bonus > 20 ? 20 : wit_bonus;
			if(percent > 90)
				percent = 90;
		}

		for(L2Character target : targets)
			if(target != null)
			{
				if(target.getPlayer() == null)
					continue;

				if(target.isPet() && _canPet)
				{
					if(target.getPlayer() == activeChar)
						((L2PetInstance) target).doRevive(percent);
					else
						target.getPlayer().reviveRequest((L2Player) activeChar, percent, true);
				}
				else if(target.isPlayer())
				{
					if(_targetType == SkillTargetType.TARGET_PET)
						continue;

					L2Player targetPlayer = (L2Player) target;

					if(targetPlayer.isReviveRequested())
						continue;

					targetPlayer.reviveRequest((L2Player) activeChar, percent, false);
				}
				else
					continue;

				getEffects(activeChar, target, getActivateRate() > 0, false);
			}

		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}