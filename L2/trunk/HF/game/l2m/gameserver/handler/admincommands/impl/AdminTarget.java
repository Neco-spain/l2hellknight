package l2m.gameserver.handler.admincommands.impl;

import l2m.gameserver.handler.admincommands.IAdminCommandHandler;
import l2m.gameserver.model.GameObject;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.World;
import l2m.gameserver.model.base.PlayerAccess;

public class AdminTarget
  implements IAdminCommandHandler
{
  public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
  {
    Commands command = (Commands)comm;

    if (!activeChar.getPlayerAccess().CanViewChar) {
      return false;
    }
    try
    {
      String targetName = wordList[1];
      GameObject obj = World.getPlayer(targetName);
      if ((obj != null) && (obj.isPlayer()))
        obj.onAction(activeChar, false);
      else
        activeChar.sendMessage("Player " + targetName + " not found");
    }
    catch (IndexOutOfBoundsException e)
    {
      activeChar.sendMessage("Please specify correct name.");
    }

    return true;
  }

  public Enum[] getAdminCommandEnum()
  {
    return Commands.values();
  }

  private static enum Commands
  {
    admin_target;
  }
}