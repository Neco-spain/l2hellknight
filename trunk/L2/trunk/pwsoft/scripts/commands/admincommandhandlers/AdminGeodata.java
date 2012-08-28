package scripts.commands.admincommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import scripts.commands.IAdminCommandHandler;

public class AdminGeodata
  implements IAdminCommandHandler
{
  private static final String[] ADMIN_COMMANDS = { "admin_geo_z", "admin_geo_type", "admin_geo_nswe", "admin_geo_los", "admin_geo_position", "admin_geo_bug", "admin_geo_load", "admin_geo_unload" };

  private static final int REQUIRED_LEVEL = Config.GM_MIN;

  public boolean useAdminCommand(String command, L2PcInstance activeChar)
  {
    return true;
  }

  public String[] getAdminCommandList()
  {
    return ADMIN_COMMANDS;
  }

  private boolean checkLevel(int level)
  {
    return level >= REQUIRED_LEVEL;
  }
}