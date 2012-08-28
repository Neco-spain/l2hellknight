package l2p.gameserver.handler.admincommands.impl;

import l2p.gameserver.cache.Msg;
import l2p.gameserver.handler.admincommands.IAdminCommandHandler;
import l2p.gameserver.model.GameObject;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.World;
import l2p.gameserver.model.base.PlayerAccess;
import l2p.gameserver.model.instances.DoorInstance;

public class AdminDoorControl
  implements IAdminCommandHandler
{
  public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
  {
    Commands command = (Commands)comm;

    if (!activeChar.getPlayerAccess().Door)
      return false;
    GameObject target;
    switch (1.$SwitchMap$l2p$gameserver$handler$admincommands$impl$AdminDoorControl$Commands[command.ordinal()])
    {
    case 1:
      GameObject target;
      if (wordList.length > 1)
        target = World.getAroundObjectById(activeChar, Integer.parseInt(wordList[1]));
      else {
        target = activeChar.getTarget();
      }
      if ((target != null) && (target.isDoor()))
        ((DoorInstance)target).openMe();
      else {
        activeChar.sendPacket(Msg.INVALID_TARGET);
      }
      break;
    case 2:
      GameObject target;
      if (wordList.length > 1)
        target = World.getAroundObjectById(activeChar, Integer.parseInt(wordList[1]));
      else
        target = activeChar.getTarget();
      if ((target != null) && (target.isDoor()))
        ((DoorInstance)target).closeMe();
      else {
        activeChar.sendPacket(Msg.INVALID_TARGET);
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
    admin_open, 
    admin_close;
  }
}