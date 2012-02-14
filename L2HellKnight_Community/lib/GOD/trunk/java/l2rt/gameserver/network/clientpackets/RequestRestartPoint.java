package l2rt.gameserver.network.clientpackets;

import l2rt.Config;
import l2rt.extensions.scripts.Functions;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.instancemanager.SiegeManager;
import l2rt.gameserver.instancemanager.ZoneManager;
import l2rt.gameserver.model.L2Clan;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Zone.ZoneType;
import l2rt.gameserver.model.entity.residence.Castle;
import l2rt.gameserver.model.entity.residence.ClanHall;
import l2rt.gameserver.model.entity.residence.Fortress;
import l2rt.gameserver.model.entity.residence.ResidenceFunction;
import l2rt.gameserver.model.entity.siege.Siege;
import l2rt.gameserver.model.entity.siege.SiegeClan;
import l2rt.gameserver.model.entity.siege.territory.TerritorySiege;
import l2rt.gameserver.network.serverpackets.Die;
import l2rt.gameserver.tables.MapRegion;
import l2rt.util.Location;
import l2rt.util.Rnd;

public class RequestRestartPoint extends L2GameClientPacket
{
	protected int _requestedPointType;
	protected boolean _continuation;

	private static final int TO_VILLAGE = 0;
	private static final int TO_CLANHALL = 1;
	private static final int TO_CASTLE = 2;
	private static final int TO_FORTRESS = 3;
	private static final int TO_SIEGEHQ = 4;
	private static final int FIXED = 5;
	private static final int AGATHION = 6;

	/**
	 * packet type id 0x7D
	 * format:    cd
	 */
	@Override
	public void readImpl()
	{
		_requestedPointType = readD();
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();

		if(activeChar == null)
			return;

		if(activeChar.isFakeDeath())
		{
			activeChar.breakFakeDeath();
			return;
		}

		if(!activeChar.isDead() && !activeChar.isGM())
		{
			activeChar.sendActionFailed();
			return;
		}

		// Запрещаем воскрешение во время эвента CtF
		try
		{
			if(activeChar.getTeam() > 0 && ZoneManager.getInstance().checkIfInZoneAndIndex(ZoneType.battle_zone, 4, activeChar) && (Boolean) Functions.callScripts("events.CtF.CtF", "isRunned", new Object[] {}))
			{
				activeChar.sendActionFailed();
				return;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		try
		{

			Location loc = null;
			long ref = 0;

			boolean isInDefense = false;
			L2Clan clan = activeChar.getClan();
			Siege siege = SiegeManager.getSiege(activeChar, true);

			switch(_requestedPointType)
			{
				case TO_CLANHALL:
					if(clan == null || clan.getHasHideout() == 0)
						loc = MapRegion.getTeleToClosestTown(activeChar);
					else
					{
						ClanHall clanHall = activeChar.getClanHall();
						loc = MapRegion.getTeleToClanHall(activeChar);
						if(clanHall.getFunction(ResidenceFunction.RESTORE_EXP) != null)
							activeChar.restoreExp(clanHall.getFunction(ResidenceFunction.RESTORE_EXP).getLevel());
					}
					break;
				case TO_CASTLE:
					isInDefense = false;
					if(siege != null && siege.checkIsDefender(clan))
						isInDefense = true;
					if((clan == null || clan.getHasCastle() == 0) && !isInDefense)
					{
						sendPacket(Msg.ActionFail, new Die(activeChar));
						return;
					}
					Castle castle = activeChar.getCastle();
					loc = MapRegion.getTeleToCastle(activeChar);
					if(castle.getFunction(ResidenceFunction.RESTORE_EXP) != null)
						activeChar.restoreExp(castle.getFunction(ResidenceFunction.RESTORE_EXP).getLevel());
					break;
				case TO_FORTRESS:
					isInDefense = false;
					if(siege != null && siege.checkIsDefender(clan))
						isInDefense = true;
					if((clan == null || clan.getHasFortress() == 0) && !isInDefense)
					{
						sendPacket(Msg.ActionFail, new Die(activeChar));
						return;
					}
					Fortress fort = activeChar.getFortress();
					loc = MapRegion.getTeleToFortress(activeChar);
					if(fort.getFunction(ResidenceFunction.RESTORE_EXP) != null)
						activeChar.restoreExp(fort.getFunction(ResidenceFunction.RESTORE_EXP).getLevel());
					break;
				case TO_SIEGEHQ:
					SiegeClan siegeClan = null;
					if(siege != null)
						siegeClan = siege.getAttackerClan(clan);
					else if(TerritorySiege.checkIfInZone(activeChar))
						siegeClan = TerritorySiege.getSiegeClan(clan);
					if(siegeClan == null || siegeClan.getHeadquarter() == null)
					{
						sendPacket(Msg.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE, new Die(activeChar));
						return;
					}
					loc = MapRegion.getTeleToHeadquarter(activeChar);
					break;
				case AGATHION:
					// TODO: agathion ress
					break;
				case FIXED:
					if(!activeChar.getPlayerAccess().ResurectFixed)
					{
						activeChar.sendActionFailed();
						return;
					}
					loc = activeChar.getLoc();
					ref = activeChar.getReflection().getId();
					break;
				case TO_VILLAGE:
				default:
					loc = MapRegion.getTeleToClosestTown(activeChar);
					break;
			}

			activeChar.setIsPendingRevive(true);
			if (Config.REVIVAL_POINT_IN_GIRAN)
			activeChar.teleToLocation(81749 + Rnd.get(-100, 100), 149171 + Rnd.get(-100, 100), -3464, ref);
			else
			activeChar.teleToLocation(loc, ref);
		}
		catch(Throwable e)
		{}
	}
}