package net.sf.l2j.gameserver.network.clientpackets;

import java.util.List;
import java.util.logging.Logger;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.MapRegionTable;
import net.sf.l2j.gameserver.datatables.MapRegionTable.TeleportWhereType;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2SiegeClan;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.ClanHall;
import net.sf.l2j.gameserver.model.entity.ClanHall.ClanHallFunction;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.model.entity.TvTEvent;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.Revive;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.util.Util;

public final class RequestRestartPoint extends L2GameClientPacket
{
  private static final String _C__6d_REQUESTRESTARTPOINT = "[C] 6d RequestRestartPoint";
  private static Logger _log = Logger.getLogger(RequestRestartPoint.class.getName());
  protected int _requestedPointType;
  protected boolean _continuation;

  protected void readImpl()
  {
    _requestedPointType = readD();
  }

  protected void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();

    if (activeChar == null) {
      return;
    }
    if ((TvTEvent.isStarted()) && (TvTEvent.isPlayerParticipant(activeChar.getObjectId()))) {
      return;
    }

    if (activeChar.isFakeDeath())
    {
      activeChar.stopFakeDeath(null);
      activeChar.broadcastPacket(new Revive(activeChar));
      return;
    }
    if (!activeChar.isAlikeDead())
    {
      _log.warning("Living player [" + activeChar.getName() + "] called RestartPointPacket! Ban this player!");
      return;
    }

    Castle castle = CastleManager.getInstance().getCastle(activeChar.getX(), activeChar.getY(), activeChar.getZ());
    if ((castle != null) && (castle.getSiege().getIsInProgress()))
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
      if ((activeChar.getClan() != null) && (castle.getSiege().checkIsAttacker(activeChar.getClan())))
      {
        ThreadPoolManager.getInstance().scheduleGeneral(new DeathTask(activeChar), castle.getSiege().getAttackerRespawnDelay());
        sm.addString("You will be re-spawned in " + castle.getSiege().getAttackerRespawnDelay() / 1000 + " seconds");
        activeChar.sendPacket(sm);
      }
      else
      {
        ThreadPoolManager.getInstance().scheduleGeneral(new DeathTask(activeChar), castle.getSiege().getDefenderRespawnDelay());
        sm.addString("You will be re-spawned in " + castle.getSiege().getDefenderRespawnDelay() / 1000 + " seconds");
        activeChar.sendPacket(sm);
      }
      sm = null;
      return;
    }

    ThreadPoolManager.getInstance().scheduleGeneral(new DeathTask(activeChar), 1L);
  }

  public String getType()
  {
    return "[C] 6d RequestRestartPoint";
  }

  class DeathTask
    implements Runnable
  {
    L2PcInstance activeChar;

    DeathTask(L2PcInstance _activeChar)
    {
      activeChar = _activeChar;
    }

    public void run()
    {
      try
      {
        Location loc = null;
        Castle castle = null;

        if (activeChar.isInFunEvent())
        {
          activeChar.sendMessage("Please wait respawn time!");
          return;
        }

        if (activeChar.isInJail()) _requestedPointType = 27;
        else if (activeChar.isFestivalParticipant()) _requestedPointType = 4;

        switch (_requestedPointType)
        {
        case 1:
          if (activeChar.getClan().getHasHideout() == 0)
          {
            activeChar.sendMessage("You may not use this respawn point!");
            Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " used respawn cheat.", 2);
            return;
          }
          loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, MapRegionTable.TeleportWhereType.ClanHall);

          if ((ClanHallManager.getInstance().getClanHallByOwner(activeChar.getClan()) == null) || (ClanHallManager.getInstance().getClanHallByOwner(activeChar.getClan()).getFunction(5) == null)) {
            break;
          }
          activeChar.restoreExp(ClanHallManager.getInstance().getClanHallByOwner(activeChar.getClan()).getFunction(5).getLvl()); break;
        case 2:
          Boolean isInDefense = Boolean.valueOf(false);
          castle = CastleManager.getInstance().getCastle(activeChar);
          if ((castle != null) && (castle.getSiege().getIsInProgress()))
          {
            if (castle.getSiege().checkIsDefender(activeChar.getClan()))
              isInDefense = Boolean.valueOf(true);
          }
          if ((activeChar.getClan().getHasCastle() == 0) && (!isInDefense.booleanValue()))
          {
            activeChar.sendMessage("You may not use this respawn point!");
            Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " used respawn cheat.", 2);
            return;
          }
          loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, MapRegionTable.TeleportWhereType.Castle);
          break;
        case 3:
          L2SiegeClan siegeClan = null;
          castle = CastleManager.getInstance().getCastle(activeChar);

          if ((castle != null) && (castle.getSiege().getIsInProgress())) {
            siegeClan = castle.getSiege().getAttackerClan(activeChar.getClan());
          }
          if ((siegeClan == null) || (siegeClan.getFlag().size() == 0))
          {
            activeChar.sendMessage("You may not use this respawn point!");
            Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " used respawn cheat.", 2);
            return;
          }
          loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, MapRegionTable.TeleportWhereType.SiegeFlag);
          break;
        case 4:
          if ((!activeChar.isGM()) && (!activeChar.isFestivalParticipant()))
          {
            activeChar.sendMessage("You may not use this respawn point!");
            Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " used respawn cheat.", 2);
            return;
          }
          loc = new Location(activeChar.getX(), activeChar.getY(), activeChar.getZ());
          break;
        case 27:
          if (!activeChar.isInJail()) return;
          loc = new Location(-114356, -249645, -2984);
          break;
        default:
          loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, MapRegionTable.TeleportWhereType.Town);
        }

        activeChar.broadcastPacket(new Revive(activeChar));

        activeChar.setIsIn7sDungeon(false);
        activeChar.setIsPendingRevive(true);
        activeChar.teleToLocation(loc, true);
      }
      catch (Throwable e)
      {
      }
    }
  }
}