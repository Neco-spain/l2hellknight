package l2m.gameserver.handler.admincommands.impl;

import l2m.gameserver.handler.admincommands.IAdminCommandHandler;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.base.PlayerAccess;
import l2m.gameserver.network.serverpackets.Say2;
import l2m.gameserver.network.serverpackets.components.ChatType;
import l2m.gameserver.data.tables.GmListTable;

public class AdminGmChat
  implements IAdminCommandHandler
{
  public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
  {
    Commands command = (Commands)comm;

    if (!activeChar.getPlayerAccess().CanAnnounce) {
      return false;
    }
    switch (1.$SwitchMap$l2p$gameserver$handler$admincommands$impl$AdminGmChat$Commands[command.ordinal()])
    {
    case 1:
      try
      {
        String text = fullString.replaceFirst(Commands.admin_gmchat.name(), "");
        Say2 cs = new Say2(0, ChatType.ALLIANCE, activeChar.getName(), text);
        GmListTable.broadcastToGMs(cs);
      }
      catch (StringIndexOutOfBoundsException e)
      {
      }

    case 2:
    }

    return true;
  }

  public Enum[] getAdminCommandEnum()
  {
    return Commands.values();
  }

  private static enum Commands
  {
    admin_gmchat, 
    admin_snoop;
  }
}