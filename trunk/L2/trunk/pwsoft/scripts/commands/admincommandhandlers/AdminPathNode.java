package scripts.commands.admincommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import scripts.commands.IAdminCommandHandler;

public class AdminPathNode
  implements IAdminCommandHandler
{
  private static final String[] ADMIN_COMMANDS = { "admin_pn_info", "admin_show_path", "admin_path_debug", "admin_show_pn", "admin_find_path" };

  private static final int REQUIRED_LEVEL = Config.GM_CREATE_NODES;

  public boolean useAdminCommand(String command, L2PcInstance activeChar)
  {
    return true;
  }

  public String[] getAdminCommandList() {
    return ADMIN_COMMANDS;
  }

  private boolean checkLevel(int level) {
    return level >= REQUIRED_LEVEL;
  }
}