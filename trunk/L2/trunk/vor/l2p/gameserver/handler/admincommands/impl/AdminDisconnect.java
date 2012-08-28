package l2p.gameserver.handler.admincommands.impl;

import l2p.commons.threading.RunnableImpl;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.handler.admincommands.IAdminCommandHandler;
import l2p.gameserver.model.GameObject;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.World;
import l2p.gameserver.model.base.PlayerAccess;
import l2p.gameserver.serverpackets.components.CustomMessage;

public class AdminDisconnect
  implements IAdminCommandHandler
{
  public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
  {
    Commands command = (Commands)comm;

    if (!activeChar.getPlayerAccess().CanKick) {
      return false;
    }
    switch (2.$SwitchMap$l2p$gameserver$handler$admincommands$impl$AdminDisconnect$Commands[command.ordinal()])
    {
    case 1:
    case 2:
      Player player;
      Player player;
      if (wordList.length == 1)
      {
        GameObject target = activeChar.getTarget();
        if (target == null)
        {
          activeChar.sendMessage("Select character or specify player name.");
          break;
        }
        if (!target.isPlayer())
        {
          activeChar.sendPacket(Msg.INVALID_TARGET);
          break;
        }
        player = (Player)target;
      }
      else
      {
        player = World.getPlayer(wordList[1]);
        if (player == null)
        {
          activeChar.sendMessage("Character " + wordList[1] + " not found in game.");
          break;
        }
      }

      if (player.getObjectId() == activeChar.getObjectId())
      {
        activeChar.sendMessage("You can't logout your character.");
      }
      else
      {
        activeChar.sendMessage("Character " + player.getName() + " disconnected from server.");

        if (player.isInOfflineMode())
        {
          player.setOfflineMode(false);
          player.kick();
          return true;
        }

        player.sendMessage(new CustomMessage("admincommandhandlers.AdminDisconnect.YoureKickedByGM", player, new Object[0]));
        player.sendPacket(Msg.YOU_HAVE_BEEN_DISCONNECTED_FROM_THE_SERVER_PLEASE_LOGIN_AGAIN);
        ThreadPoolManager.getInstance().schedule(new RunnableImpl(player)
        {
          public void runImpl()
            throws Exception
          {
            val$player.kick();
          }
        }
        , 500L);
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
    admin_disconnect, 
    admin_kick;
  }
}