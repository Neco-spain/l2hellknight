package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GmListTable;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class AdminGmChat
  implements IAdminCommandHandler
{
  private static final String[] ADMIN_COMMANDS = { "admin_gmchat", "admin_snoop", "admin_gmchat_menu" };
  private static final int REQUIRED_LEVEL = Config.GM_MIN;

  public boolean useAdminCommand(String command, L2PcInstance activeChar) {
    if ((!Config.ALT_PRIVILEGES_ADMIN) && (
      (!checkLevel(activeChar.getAccessLevel())) || (!activeChar.isGM()))) return false;

    if (command.startsWith("admin_gmchat"))
      handleGmChat(command, activeChar);
    else if (command.startsWith("admin_snoop"))
      snoop(command, activeChar);
    if (command.startsWith("admin_gmchat_menu"))
      AdminHelpPage.showHelpPage(activeChar, "main_menu.htm");
    return true;
  }

  private void snoop(String command, L2PcInstance activeChar)
  {
    L2Object target = activeChar.getTarget();
    if (target == null)
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_MUST_SELECT_A_TARGET));
      return;
    }
    if (!(target instanceof L2PcInstance))
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
      return;
    }
    L2PcInstance player = (L2PcInstance)target;
    player.addSnooper(activeChar);
    activeChar.addSnooped(player);
  }

  public String[] getAdminCommandList() {
    return ADMIN_COMMANDS;
  }

  private boolean checkLevel(int level) {
    return level >= REQUIRED_LEVEL;
  }

  private void handleGmChat(String command, L2PcInstance activeChar)
  {
    try
    {
      int offset = 0;

      if (command.contains("menu"))
        offset = 17;
      else
        offset = 13;
      String text = command.substring(offset);
      CreatureSay cs = new CreatureSay(0, 9, activeChar.getName(), text);
      GmListTable.broadcastToGMs(cs);
    }
    catch (StringIndexOutOfBoundsException e)
    {
    }
  }
}