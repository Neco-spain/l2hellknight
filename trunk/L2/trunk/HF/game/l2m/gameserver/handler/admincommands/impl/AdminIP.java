package l2m.gameserver.handler.admincommands.impl;

import l2m.gameserver.handler.admincommands.IAdminCommandHandler;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.World;
import l2m.gameserver.model.base.PlayerAccess;

public class AdminIP
  implements IAdminCommandHandler
{
  public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
  {
    Commands command = (Commands)comm;

    if (!activeChar.getPlayerAccess().CanBan) {
      return false;
    }
    switch (1.$SwitchMap$l2p$gameserver$handler$admincommands$impl$AdminIP$Commands[command.ordinal()])
    {
    case 1:
      if (wordList.length != 2)
      {
        activeChar.sendMessage("Command syntax: //charip <char_name>");
        activeChar.sendMessage(" Gets character's IP.");
      }
      else
      {
        Player pl = World.getPlayer(wordList[1]);

        if (pl == null)
        {
          activeChar.sendMessage("Character " + wordList[1] + " not found.");
        }
        else
        {
          String ip_adr = pl.getIP();
          if (ip_adr.equalsIgnoreCase("<not connected>"))
          {
            activeChar.sendMessage("Character " + wordList[1] + " not found.");
          }
          else
          {
            activeChar.sendMessage("Character's IP: " + ip_adr);
          }
        }
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
    admin_charip;
  }
}