package l2p.gameserver.handler.admincommands.impl;

import l2p.gameserver.handler.admincommands.IAdminCommandHandler;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.base.PlayerAccess;

public class AdminRide
  implements IAdminCommandHandler
{
  public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
  {
    Commands command = (Commands)comm;

    if (!activeChar.getPlayerAccess().Rider) {
      return false;
    }
    switch (1.$SwitchMap$l2p$gameserver$handler$admincommands$impl$AdminRide$Commands[command.ordinal()])
    {
    case 1:
      if ((activeChar.isMounted()) || (activeChar.getPet() != null))
      {
        activeChar.sendMessage("Already Have a Pet or Mounted.");
        return false;
      }
      if (wordList.length != 2)
      {
        activeChar.sendMessage("Incorrect id.");
        return false;
      }
      activeChar.setMount(Integer.parseInt(wordList[1]), 0, 85);
      break;
    case 2:
    case 3:
      if ((activeChar.isMounted()) || (activeChar.getPet() != null))
      {
        activeChar.sendMessage("Already Have a Pet or Mounted.");
        return false;
      }
      activeChar.setMount(12621, 0, 85);
      break;
    case 4:
    case 5:
      if ((activeChar.isMounted()) || (activeChar.getPet() != null))
      {
        activeChar.sendMessage("Already Have a Pet or Mounted.");
        return false;
      }
      activeChar.setMount(12526, 0, 85);
      break;
    case 6:
    case 7:
      activeChar.setMount(0, 0, 0);
    }

    return true;
  }

  public Enum[] getAdminCommandEnum()
  {
    return Commands.values();
  }

  private static enum Commands
  {
    admin_ride, 
    admin_ride_wyvern, 
    admin_ride_strider, 
    admin_unride, 
    admin_wr, 
    admin_sr, 
    admin_ur;
  }
}