package l2p.gameserver.handler.admincommands.impl;

import l2p.gameserver.handler.admincommands.IAdminCommandHandler;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.base.InvisibleType;
import l2p.gameserver.model.base.PlayerAccess;
import l2p.gameserver.serverpackets.CameraMode;
import l2p.gameserver.serverpackets.SpecialCamera;

public class AdminCamera
  implements IAdminCommandHandler
{
  public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
  {
    Commands command = (Commands)comm;

    if (!activeChar.getPlayerAccess().Menu) {
      return false;
    }
    switch (1.$SwitchMap$l2p$gameserver$handler$admincommands$impl$AdminCamera$Commands[command.ordinal()])
    {
    case 1:
      if (fullString.length() > 15) {
        fullString = fullString.substring(15);
      }
      else {
        activeChar.sendMessage("Usage: //freelook 1 or //freelook 0");
        return false;
      }

      int mode = Integer.parseInt(fullString);
      if (mode == 1)
      {
        activeChar.setInvisibleType(InvisibleType.NORMAL);
        activeChar.setIsInvul(true);
        activeChar.setNoChannel(-1L);
        activeChar.setFlying(true);
      }
      else
      {
        activeChar.setInvisibleType(InvisibleType.NONE);
        activeChar.setIsInvul(false);
        activeChar.setNoChannel(0L);
        activeChar.setFlying(false);
      }
      activeChar.sendPacket(new CameraMode(mode));

      break;
    case 2:
      int id = Integer.parseInt(wordList[1]);
      int dist = Integer.parseInt(wordList[2]);
      int yaw = Integer.parseInt(wordList[3]);
      int pitch = Integer.parseInt(wordList[4]);
      int time = Integer.parseInt(wordList[5]);
      int duration = Integer.parseInt(wordList[6]);
      activeChar.sendPacket(new SpecialCamera(id, dist, yaw, pitch, time, duration));
      break;
    }

    return true;
  }

  public Enum[] getAdminCommandEnum()
  {
    return Commands.values();
  }

  private static enum Commands
  {
    admin_freelook, 
    admin_cinematic;
  }
}