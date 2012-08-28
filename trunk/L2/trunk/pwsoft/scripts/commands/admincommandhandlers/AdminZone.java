package scripts.commands.admincommandhandlers;

import java.util.StringTokenizer;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GmListTable;
import net.sf.l2j.gameserver.datatables.MapRegionTable;
import net.sf.l2j.gameserver.datatables.MapRegionTable.TeleportWhereType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.util.Location;
import scripts.commands.IAdminCommandHandler;

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
        activeChar.sendAdmResultMessage("This is a PvP zone.");
      else {
        activeChar.sendAdmResultMessage("This is NOT a PvP zone.");
      }
      if (activeChar.isInsideZone(64))
        activeChar.sendAdmResultMessage("This is a no landing zone.");
      else {
        activeChar.sendAdmResultMessage("This is NOT a no landing zone.");
      }
      activeChar.sendAdmResultMessage("MapRegion: x:" + MapRegionTable.getInstance().getMapRegionX(activeChar.getX()) + " y:" + MapRegionTable.getInstance().getMapRegionX(activeChar.getY()));

      activeChar.sendAdmResultMessage("Closest Town: " + MapRegionTable.getInstance().getClosestTownName(activeChar));

      Location loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, MapRegionTable.TeleportWhereType.Castle);
      activeChar.sendAdmResultMessage("TeleToLocation (Castle): x:" + loc.getX() + " y:" + loc.getY() + " z:" + loc.getZ());

      loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, MapRegionTable.TeleportWhereType.ClanHall);
      activeChar.sendAdmResultMessage("TeleToLocation (ClanHall): x:" + loc.getX() + " y:" + loc.getY() + " z:" + loc.getZ());

      loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, MapRegionTable.TeleportWhereType.SiegeFlag);
      activeChar.sendAdmResultMessage("TeleToLocation (SiegeFlag): x:" + loc.getX() + " y:" + loc.getY() + " z:" + loc.getZ());

      loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, MapRegionTable.TeleportWhereType.Town);
      activeChar.sendAdmResultMessage("TeleToLocation (Town): x:" + loc.getX() + " y:" + loc.getY() + " z:" + loc.getZ());
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