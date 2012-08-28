package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.communitybbs.Manager.AdminBBSManager;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class AdminBBS
  implements IAdminCommandHandler
{
  private static final String[] ADMIN_COMMANDS = { "admin_bbs" };
  private static final int REQUIRED_LEVEL = Config.GM_MIN;

  public boolean useAdminCommand(String command, L2PcInstance activeChar)
  {
    if (!Config.ALT_PRIVILEGES_ADMIN)
    {
      if ((!checkLevel(activeChar.getAccessLevel())) || (!activeChar.isGM()))
      {
        return false;
      }
    }
    AdminBBSManager.getInstance().parsecmd(command, activeChar);
    return true;
  }

  public String[] getAdminCommandList()
  {
    return ADMIN_COMMANDS;
  }

  private boolean checkLevel(int level) {
    return level >= REQUIRED_LEVEL;
  }
}