package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;

import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.MapRegionTable;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.model.L2SiegeClan;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.ClanHall;
import net.sf.l2j.gameserver.model.entity.TvTEvent;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.Revive;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.util.IllegalPlayerAction;
import net.sf.l2j.gameserver.util.Util;

public final class RequestRestartPoint extends L2GameClientPacket
{
	private static final String _C__6d_REQUESTRESTARTPOINT = "[C] 6d RequestRestartPoint";
	private static Logger _log = Logger.getLogger(RequestRestartPoint.class.getName());

	protected int     _requestedPointType;
	protected boolean _continuation;


	@Override
	protected void readImpl()
	{
		_requestedPointType = readD();
	}

	class DeathTask implements Runnable
	{
		L2PcInstance activeChar;
		DeathTask (L2PcInstance _activeChar)
		{
			activeChar = _activeChar;
		}

		public void run()
		{
			try
			{
				Location loc = null;
				Castle castle=null;

				if (activeChar.isInFunEvent())
				{
					activeChar.sendMessage("Please wait respawn time!");
					return;
				}

				if (activeChar.isInJail()) _requestedPointType = 27;
				else if (activeChar.isFestivalParticipant()) _requestedPointType = 4;

				switch (_requestedPointType)
				{
					case 1: // to clanhall
						if (activeChar.getClan().getHasHideout() == 0)
						{
							//cheater
							activeChar.sendMessage("You may not use this respawn point!");
							Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " used respawn cheat.", IllegalPlayerAction.PUNISH_KICK);
							return;
						}
						loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, MapRegionTable.TeleportWhereType.ClanHall);

						if (ClanHallManager.getInstance().getClanHallByOwner(activeChar.getClan())!= null &&
								ClanHallManager.getInstance().getClanHallByOwner(activeChar.getClan()).getFunction(ClanHall.FUNC_RESTORE_EXP) != null)
						{
							activeChar.restoreExp(ClanHallManager.getInstance().getClanHallByOwner(activeChar.getClan()).getFunction(ClanHall.FUNC_RESTORE_EXP).getLvl());
						}
						break;

					case 2: // to castle
						Boolean isInDefense = false;
						castle = CastleManager.getInstance().getCastle(activeChar);
						if (castle != null && castle.getSiege().getIsInProgress())
						{
							//siege in progress
							if (castle.getSiege().checkIsDefender(activeChar.getClan()))
								isInDefense = true;
						}
						if (activeChar.getClan().getHasCastle() == 0 && !isInDefense)
						{
							//cheater
							activeChar.sendMessage("You may not use this respawn point!");
							Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " used respawn cheat.", IllegalPlayerAction.PUNISH_KICK);
							return;
						}
						loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, MapRegionTable.TeleportWhereType.Castle);
						break;

					case 3: // to siege HQ
						L2SiegeClan siegeClan = null;
						castle = CastleManager.getInstance().getCastle(activeChar);

						if (castle != null && castle.getSiege().getIsInProgress())
							siegeClan = castle.getSiege().getAttackerClan(activeChar.getClan());

						if (siegeClan == null || siegeClan.getFlag().size() == 0)
						{
							//cheater
							activeChar.sendMessage("You may not use this respawn point!");
							Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " used respawn cheat.", IllegalPlayerAction.PUNISH_KICK);
							return;
						}
						loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, MapRegionTable.TeleportWhereType.SiegeFlag);
						break;

					case 4: // Fixed or Player is a festival participant
						if (!activeChar.isGM() && !activeChar.isFestivalParticipant())
						{
							//cheater
							activeChar.sendMessage("You may not use this respawn point!");
							Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " used respawn cheat.", IllegalPlayerAction.PUNISH_KICK);
							return;
						}
						loc = new Location(activeChar.getX(), activeChar.getY(), activeChar.getZ()); // spawn them where they died
						break;

					case 27: // to jail
						if (!activeChar.isInJail()) return;
						loc = new Location(-114356, -249645, -2984);
						break;
					default:
						loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, MapRegionTable.TeleportWhereType.Town);
						
				}
				this.activeChar.broadcastPacket(new Revive(this.activeChar));

				//Teleport and revive
				activeChar.setIsIn7sDungeon(false); //����
				activeChar.setIsPendingRevive(true);
				activeChar.teleToLocation(loc, true);
				
			} catch (Throwable e) {
				//_log.log(Level.SEVERE, "", e);
			}
		}
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();

		if (activeChar == null)
			return;
			
		if (TvTEvent.isStarted() && TvTEvent.isPlayerParticipant(activeChar.getObjectId()))
			return;

		//SystemMessage sm2 = new SystemMessage(SystemMessage.S1_S2);
		//sm2.addString("type:"+requestedPointType);
		//activeChar.sendPacket(sm2);

		if (activeChar.isFakeDeath())
		{
			activeChar.stopFakeDeath(null);
			activeChar.broadcastPacket(new Revive(activeChar));
			return;
		}
		else if(!activeChar.isAlikeDead())
		{
			_log.warning("Living player ["+activeChar.getName()+"] called RestartPointPacket! Ban this player!");
			return;
		}

		Castle castle = CastleManager.getInstance().getCastle(activeChar.getX(), activeChar.getY(), activeChar.getZ());
		if (castle != null && castle.getSiege().getIsInProgress())
		{
			//DeathFinalizer df = new DeathFinalizer(10000);
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
			if (activeChar.getClan() != null && castle.getSiege().checkIsAttacker(activeChar.getClan()))
			{
				// Schedule respawn delay for attacker
				ThreadPoolManager.getInstance().scheduleGeneral(new DeathTask(activeChar), castle.getSiege().getAttackerRespawnDelay());
				sm.addString("You will be re-spawned in " + castle.getSiege().getAttackerRespawnDelay()/1000 + " seconds");
				activeChar.sendPacket(sm);
			}
			else
			{
				// Schedule respawn delay for defender with penalty for CT lose
				ThreadPoolManager.getInstance().scheduleGeneral(new DeathTask(activeChar), castle.getSiege().getDefenderRespawnDelay());
				sm.addString("You will be re-spawned in " + castle.getSiege().getDefenderRespawnDelay()/1000 + " seconds");
				activeChar.sendPacket(sm);
			}
			sm = null;
			return;
		}

		ThreadPoolManager.getInstance().scheduleGeneral(new DeathTask(activeChar), 1);
	}



	/* (non-Javadoc)
     * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
     */
	@Override
	public String getType()
	{
		return _C__6d_REQUESTRESTARTPOINT;
	}
}
