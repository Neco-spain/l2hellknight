package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GmListTable;
import net.sf.l2j.gameserver.datatables.MapRegionTable;
import net.sf.l2j.gameserver.datatables.MapRegionTable.TeleportWhereType;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class AdminZone
  implements IAdminCommandHandler
{
  private static final int REQUIRED_LEVEL = Config.GM_TEST;
  private static final String[] ADMIN_COMMANDS = { "admin_zone_check", "admin_zone_reload" };

  public boolean useAdminCommand(String command, L2PcInstance activeChar)
  {
    if (activeChar == null) return false;

    if ((!Config.ALT_PRIVILEGES_ADMIN) && 
      (activeChar.getAccessLevel() < REQUIRED_LEVEL)) return false;

    StringTokenizer st = new StringTokenizer(command, " ");
    String actualCommand = st.nextToken();

    if (actualCommand.equalsIgnoreCase("admin_zone_check"))
    {
      if (activeChar.isInsideZone(1))
        activeChar.sendMessage("This is a PvP zone.");
      else {
        activeChar.sendMessage("This is NOT a PvP zone.");
      }
      if (activeChar.isInsideZone(64))
        activeChar.sendMessage("This is a no landing zone.");
      else {
        activeChar.sendMessage("This is NOT a no landing zone.");
      }
      activeChar.sendMessage("MapRegion: x:" + MapRegionTable.getInstance().getMapRegionX(activeChar.getX()) + " y:" + MapRegionTable.getInstance().getMapRegionX(activeChar.getY()));

      activeChar.sendMessage("Closest Town: " + MapRegionTable.getInstance().getClosestTownName(activeChar));

      Location loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, MapRegionTable.TeleportWhereType.Castle);
      activeChar.sendMessage("TeleToLocation (Castle): x:" + loc.getX() + " y:" + loc.getY() + " z:" + loc.getZ());

      loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, MapRegionTable.TeleportWhereType.ClanHall);
      activeChar.sendMessage("TeleToLocation (ClanHall): x:" + loc.getX() + " y:" + loc.getY() + " z:" + loc.getZ());

      loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, MapRegionTable.TeleportWhereType.SiegeFlag);
      activeChar.sendMessage("TeleToLocation (SiegeFlag): x:" + loc.getX() + " y:" + loc.getY() + " z:" + loc.getZ());

      loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, MapRegionTable.TeleportWhereType.Town);
      activeChar.sendMessage("TeleToLocation (Town): x:" + loc.getX() + " y:" + loc.getY() + " z:" + loc.getZ());
    } else if (actualCommand.equalsIgnoreCase("admin_zone_reload"))
    {
      GmListTable.broadcastMessageToGMs("Zones can not be reloaded in this version.");
    }
    return true;
  }

  public String[] getAdminCommandList()
  {
    return ADMIN_COMMANDS;
  }
}