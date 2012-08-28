package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GmListTable;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class AdminGm
  implements IAdminCommandHandler
{
  private static Logger _log = Logger.getLogger(AdminGm.class.getName());
  private static final String[] ADMIN_COMMANDS = { "admin_gm" };
  private static final int REQUIRED_LEVEL = Config.GM_ACCESSLEVEL;

  public boolean useAdminCommand(String command, L2PcInstance activeChar)
  {
    if (!Config.ALT_PRIVILEGES_ADMIN)
    {
      if (!checkLevel(activeChar.getAccessLevel())) {
        return false;
      }
    }
    if (command.equals("admin_gm")) {
      handleGm(activeChar);
    }
    return true;
  }

  public String[] getAdminCommandList() {
    return ADMIN_COMMANDS;
  }

  private boolean checkLevel(int level) {
    return level >= REQUIRED_LEVEL;
  }

  private void handleGm(L2PcInstance activeChar)
  {
    if (activeChar.isGM())
    {
      GmListTable.getInstance().deleteGm(activeChar);
      activeChar.setIsGM(false);

      activeChar.sendMessage("You no longer have GM status.");

      if (Config.DEBUG) _log.fine("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") turned his GM status off"); 
    }
    else
    {
      GmListTable.getInstance().addGm(activeChar, false);
      activeChar.setIsGM(true);

      activeChar.sendMessage("You now have GM status.");

      if (Config.DEBUG) _log.fine("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") turned his GM status on");
    }
  }
}