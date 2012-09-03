package l2rt.gameserver.skills.skillclasses;

import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.geodata.GeoEngine;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.templates.StatsSet;
import l2rt.util.GArray;

import static l2rt.gameserver.model.L2Zone.ZoneType.*;

public class Call extends L2Skill
{
	final boolean _party;

	public Call(StatsSet set)
	{
		super(set);
		_party = set.getBool("party", false);
	}

	@Override
	public boolean checkCondition(L2Character activeChar, L2Character target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(activeChar.isPlayer())
		{
			if(_party && ((L2Player) activeChar).getParty() == null)
				return false;

			SystemMessage msg = canSummonHere(activeChar);
			if(msg != null)
			{
				activeChar.sendPacket(msg);
				return false;
			}

			// Эта проверка только для одиночной цели
			if(!_party)
			{
				if(activeChar == target)
					return false;

				msg = canBeSummoned(target);
				if(msg != null)
				{
					activeChar.sendPacket(msg);
					return false;
				}
			}
		}

		return super.checkCondition(activeChar, target, forceUse, dontMove, first);
	}

	@Override
	public void useSkill(L2Character activeChar, GArray<L2Character> targets)
	{
		SystemMessage msg = canSummonHere(activeChar);
		if(msg != null)
		{
			activeChar.sendPacket(msg);
			return;
		}

		if(_party)
		{
			if(((L2Player) activeChar).getParty() != null)
				for(L2Player target : ((L2Player) activeChar).getParty().getPartyMembers())
					if(!target.equals(activeChar) && canBeSummoned(target) == null && !target.isTerritoryFlagEquipped())
					{
						target.stopMove();
						target.teleToLocation(GeoEngine.findPointToStay(activeChar.getX(), activeChar.getY(), activeChar.getZ(), 100, 150, activeChar.getReflection().getGeoIndex()));
						getEffects(activeChar, target, getActivateRate() > 0, false);
					}

			if(isSSPossible())
				activeChar.unChargeShots(isMagic());
			return;
		}

		for(L2Character target : targets)
			if(target != null)
			{
				if(canBeSummoned(target) != null)
					continue;

				((L2Player) target).summonCharacterRequest(activeChar.getName(), GeoEngine.findPointToStay(activeChar.getX(), activeChar.getY(), activeChar.getZ(), 100, 150, activeChar.getReflection().getGeoIndex()), getId() == 1403 || getId() == 1404 ? 1 : 0);

				getEffects(activeChar, target, getActivateRate() > 0, false);
			}

		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}

	/**
	 * Может ли призывающий в данный момент использовать призыв
	 */
	public static SystemMessage canSummonHere(L2Character activeChar)
	{
		// "Нельзя вызывать персонажей в/из зоны свободного PvP"
		// "в зоны осад"
		// "на Олимпийский стадион"
		// "в зоны определенных рейд-боссов и эпик-боссов"
		if(activeChar.isInZoneBattle() || activeChar.isInZone(Siege) || activeChar.isInZone(no_restart) || activeChar.isInZone(no_summon) || activeChar.isInZone(OlympiadStadia) || activeChar.isFlying() || activeChar.isInVehicle() || activeChar.getReflection().getId() != 0)
			return Msg.YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION;

		if(activeChar.isInCombat())
			return Msg.YOU_CANNOT_SUMMON_DURING_COMBAT;

		if(((L2Player) activeChar).getPrivateStoreType() != L2Player.STORE_PRIVATE_NONE || ((L2Player) activeChar).isInTransaction())
			return Msg.YOU_CANNOT_SUMMON_DURING_A_TRADE_OR_WHILE_USING_THE_PRIVATE_SHOPS;

		return null;
	}

	/**
	 * Может ли цель ответить на призыв
	 */
	public static SystemMessage canBeSummoned(L2Character target)
	{
		if(target == null || !target.isPlayer() || target.getPlayer().isTerritoryFlagEquipped())
			return Msg.INVALID_TARGET;

		if(target.isInZoneOlympiad())
			return Msg.YOU_CANNOT_SUMMON_PLAYERS_WHO_ARE_CURRENTLY_PARTICIPATING_IN_THE_GRAND_OLYMPIAD;

		if(target.isInZoneBattle() || target.isInZone(Siege) || target.isInZone(no_restart) || target.isInZone(no_summon) || target.getReflection().getId() != 0 || target.isFlying() || target.isInVehicle())
			return Msg.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING;

		// Нельзя призывать мертвых персонажей
		if(target.isDead())
			return new SystemMessage(SystemMessage.S1_IS_DEAD_AT_THE_MOMENT_AND_CANNOT_BE_SUMMONED).addString(target.getName());

		// Нельзя призывать персонажей, которые находятся в режиме PvP или Combat Mode
		if(target.getPvpFlag() != 0 || target.isInCombat())
			return new SystemMessage(SystemMessage.S1_IS_ENGAGED_IN_COMBAT_AND_CANNOT_BE_SUMMONED).addString(target.getName());

		L2Player pTarget = (L2Player) target;

		// Нельзя призывать торгующих персонажей
		if(pTarget.getPrivateStoreType() != L2Player.STORE_PRIVATE_NONE || pTarget.isInTransaction())
			return new SystemMessage(SystemMessage.S1_IS_CURRENTLY_TRADING_OR_OPERATING_A_PRIVATE_STORE_AND_CANNOT_BE_SUMMONED).addString(target.getName());

		return null;
	}
}