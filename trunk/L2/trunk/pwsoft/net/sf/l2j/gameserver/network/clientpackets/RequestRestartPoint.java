package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.MapRegionTable;
import net.sf.l2j.gameserver.datatables.MapRegionTable.TeleportWhereType;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.ClanHall;
import net.sf.l2j.gameserver.model.entity.ClanHall.ClanHallFunction;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.model.entity.TvTEvent;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.Revive;
import net.sf.l2j.util.Location;
import scripts.autoevents.basecapture.BaseCapture;

public final class RequestRestartPoint extends L2GameClientPacket
{
  protected int _requestedPointType;
  protected boolean _continuation;
  private static final int TO_CLANHALL = 1;
  private static final int TO_CASTLE = 2;
  private static final int FIXED_OR_FESTIVEL = 4;

  protected void readImpl()
  {
    _requestedPointType = readD();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();

    if (player == null) {
      return;
    }
    if (player.isFakeDeath())
    {
      player.stopFakeDeath(null);
      player.broadcastPacket(new Revive(player));
      return;
    }
    if (!player.isAlikeDead()) {
      return;
    }
    if (TvTEvent.isStarted())
    {
      if (TvTEvent.isPlayerParticipant(player.getName()))
      {
        player.sendCritMessage("\u0422\u0432\u0422 \u044D\u0432\u0435\u043D\u0442: \u0414\u043E\u0436\u0434\u0438\u0442\u0435\u0441\u044C \u0432\u043E\u0441\u043A\u0440\u0435\u0448\u0435\u043D\u0438\u044F.");
        return;
      }
    }

    if ((Config.EBC_ENABLE) && (BaseCapture.getEvent().isInBattle(player)))
    {
      player.sendCritMessage("-\u0417\u0430\u0445\u0432\u0430\u0442 \u0431\u0430\u0437\u044B-: \u0414\u043E\u0436\u0434\u0438\u0442\u0435\u0441\u044C \u0432\u043E\u0441\u043A\u0440\u0435\u0448\u0435\u043D\u0438\u044F.");
      return;
    }

    if (player.isFestivalParticipant()) {
      _requestedPointType = 4;
    }
    try
    {
      Location loc = null;
      Castle castle = null;
      switch (_requestedPointType)
      {
      case 1:
        if (player.getClan().getHasHideout() == 0)
          return;
        loc = MapRegionTable.getInstance().getTeleToLocation(player, MapRegionTable.TeleportWhereType.ClanHall);
        if ((ClanHallManager.getInstance().getClanHallByOwner(player.getClan()) == null) || (ClanHallManager.getInstance().getClanHallByOwner(player.getClan()).getFunction(5) == null)) break;
        player.restoreExp(ClanHallManager.getInstance().getClanHallByOwner(player.getClan()).getFunction(5).getLvl()); break;
      case 2:
        Boolean isInDefense = Boolean.valueOf(false);
        castle = CastleManager.getInstance().getCastle(player);
        if ((castle != null) && (castle.getSiege().getIsInProgress()))
        {
          if (castle.getSiege().checkIsDefender(player.getClan()))
            isInDefense = Boolean.valueOf(true);
        }
        if ((player.getClan().getHasCastle() == 0) && (!isInDefense.booleanValue()))
          return;
        loc = MapRegionTable.getInstance().getTeleToLocation(player, MapRegionTable.TeleportWhereType.Castle);
        break;
      case 4:
        if ((!player.isGM()) && (!player.isFestivalParticipant()))
          return;
        loc = new Location(player.getX(), player.getY(), player.getZ());
        break;
      case 3:
      default:
        loc = MapRegionTable.getInstance().getTeleToLocation(player, MapRegionTable.TeleportWhereType.Town);
      }

      player.setIsPendingRevive(true);
      player.teleToLocation(loc, true);
    }
    catch (Exception e)
    {
    }
  }
}