package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

public class AdminHelpPage
  implements IAdminCommandHandler
{
  private static final String[] ADMIN_COMMANDS = { "admin_help" };
  private static final int REQUIRED_LEVEL = Config.GM_MIN;

  public boolean useAdminCommand(String command, L2PcInstance activeChar) {
    if ((!Config.ALT_PRIVILEGES_ADMIN) && 
      (!checkLevel(activeChar.getAccessLevel()))) return false;

    if (command.startsWith("admin_help"))
    {
      try
      {
        String val = command.substring(11);
        showHelpPage(activeChar, val);
      }
      catch (StringIndexOutOfBoundsException e)
      {
      }

    }

    return true;
  }

  public String[] getAdminCommandList() {
    return ADMIN_COMMANDS;
  }

  private boolean checkLevel(int level) {
    return level >= REQUIRED_LEVEL;
  }

  public static void showHelpPage(L2PcInstance targetChar, String filename)
  {
    String content = HtmCache.getInstance().getHtmForce("data/html/admin/" + filename);
    NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
    adminReply.setHtml(content);
    targetChar.sendPacket(adminReply);
  }
}