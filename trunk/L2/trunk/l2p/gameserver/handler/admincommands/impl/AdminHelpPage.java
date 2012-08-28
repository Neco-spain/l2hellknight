package l2p.gameserver.handler.admincommands.impl;

import l2p.gameserver.handler.admincommands.IAdminCommandHandler;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.base.PlayerAccess;
import l2p.gameserver.serverpackets.NpcHtmlMessage;

public class AdminHelpPage
  implements IAdminCommandHandler
{
  public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
  {
    Commands command = (Commands)comm;

    if (!activeChar.getPlayerAccess().Menu) {
      return false;
    }
    switch (1.$SwitchMap$l2p$gameserver$handler$admincommands$impl$AdminHelpPage$Commands[command.ordinal()])
    {
    case 1:
      if (wordList.length != 2)
      {
        activeChar.sendMessage("Usage: //showhtml <file>");
        return false;
      }
      activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/" + wordList[1]));
    }

    return true;
  }

  public static void showHelpHtml(Player targetChar, String content)
  {
    NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
    adminReply.setHtml(content);
    targetChar.sendPacket(adminReply);
  }

  public Enum[] getAdminCommandEnum()
  {
    return Commands.values();
  }

  private static enum Commands
  {
    admin_showhtml;
  }
}