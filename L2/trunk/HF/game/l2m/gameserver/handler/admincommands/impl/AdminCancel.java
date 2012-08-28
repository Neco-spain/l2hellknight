package l2m.gameserver.handler.admincommands.impl;

import l2m.gameserver.cache.Msg;
import l2m.gameserver.handler.admincommands.IAdminCommandHandler;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.EffectList;
import l2m.gameserver.model.GameObject;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.World;
import l2m.gameserver.model.base.PlayerAccess;

public class AdminCancel
  implements IAdminCommandHandler
{
  public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
  {
    Commands command = (Commands)comm;

    if (!activeChar.getPlayerAccess().CanEditChar) {
      return false;
    }
    switch (1.$SwitchMap$l2p$gameserver$handler$admincommands$impl$AdminCancel$Commands[command.ordinal()])
    {
    case 1:
      handleCancel(activeChar, wordList.length > 1 ? wordList[1] : null);
    }

    return true;
  }

  public Enum[] getAdminCommandEnum()
  {
    return Commands.values();
  }

  private void handleCancel(Player activeChar, String targetName)
  {
    GameObject obj = activeChar.getTarget();
    if (targetName != null)
    {
      Player plyr = World.getPlayer(targetName);
      if (plyr != null)
        obj = plyr;
      else {
        try
        {
          int radius = Math.max(Integer.parseInt(targetName), 100);
          for (Creature character : activeChar.getAroundCharacters(radius, 200))
            character.getEffectList().stopAllEffects();
          activeChar.sendMessage("Apply Cancel within " + radius + " unit radius.");
          return;
        }
        catch (NumberFormatException e)
        {
          activeChar.sendMessage("Enter valid player name or radius");
          return;
        }
      }
    }
    if (obj == null)
      obj = activeChar;
    if (obj.isCreature())
      ((Creature)obj).getEffectList().stopAllEffects();
    else
      activeChar.sendPacket(Msg.INVALID_TARGET);
  }

  private static enum Commands
  {
    admin_cancel;
  }
}