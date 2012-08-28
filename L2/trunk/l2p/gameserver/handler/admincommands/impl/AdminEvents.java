package l2p.gameserver.handler.admincommands.impl;

import l2p.gameserver.handler.admincommands.IAdminCommandHandler;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.base.PlayerAccess;
import l2p.gameserver.serverpackets.NpcHtmlMessage;

public class AdminEvents
  implements IAdminCommandHandler
{
  public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
  {
    Commands command = (Commands)comm;

    if (!activeChar.getPlayerAccess().IsEventGm) {
      return false;
    }
    switch (1.$SwitchMap$l2p$gameserver$handler$admincommands$impl$AdminEvents$Commands[command.ordinal()])
    {
    case 1:
      if (wordList.length == 1)
        activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/events/events.htm"));
      else {
        activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/events/" + wordList[1].trim()));
      }
    }

    return true;
  }

  public Enum[] getAdminCommandEnum()
  {
    return Commands.values();
  }

  private static enum Commands
  {
    admin_events;
  }
}