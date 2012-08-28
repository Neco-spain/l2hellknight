package l2m.gameserver.handler.admincommands.impl;

import l2m.gameserver.cache.Msg;
import l2m.gameserver.handler.admincommands.IAdminCommandHandler;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.GameObject;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.World;
import l2m.gameserver.model.base.PlayerAccess;
import org.apache.commons.lang3.math.NumberUtils;

public class AdminKill
  implements IAdminCommandHandler
{
  public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
  {
    Commands command = (Commands)comm;

    if (!activeChar.getPlayerAccess().CanEditNPC) {
      return false;
    }
    switch (1.$SwitchMap$l2p$gameserver$handler$admincommands$impl$AdminKill$Commands[command.ordinal()])
    {
    case 1:
      if (wordList.length == 1)
        handleKill(activeChar);
      else
        handleKill(activeChar, wordList[1]);
      break;
    case 2:
      handleDamage(activeChar, NumberUtils.toInt(wordList[1], 1));
    }

    return true;
  }

  public Enum[] getAdminCommandEnum()
  {
    return Commands.values();
  }

  private void handleKill(Player activeChar)
  {
    handleKill(activeChar, null);
  }

  private void handleKill(Player activeChar, String player)
  {
    GameObject obj = activeChar.getTarget();
    if (player != null)
    {
      Player plyr = World.getPlayer(player);
      if (plyr != null) {
        obj = plyr;
      }
      else {
        int radius = Math.max(Integer.parseInt(player), 100);
        for (Creature character : activeChar.getAroundCharacters(radius, 200))
          if (!character.isDoor())
            character.doDie(activeChar);
        activeChar.sendMessage("Killed within " + radius + " unit radius.");
        return;
      }
    }

    if ((obj != null) && (obj.isCreature()))
    {
      Creature target = (Creature)obj;
      target.doDie(activeChar);
    }
    else {
      activeChar.sendPacket(Msg.INVALID_TARGET);
    }
  }

  private void handleDamage(Player activeChar, int damage) {
    GameObject obj = activeChar.getTarget();

    if (obj == null)
    {
      activeChar.sendPacket(Msg.SELECT_TARGET);
      return;
    }

    if (!obj.isCreature())
    {
      activeChar.sendPacket(Msg.INVALID_TARGET);
      return;
    }

    Creature cha = (Creature)obj;
    cha.reduceCurrentHp(damage, activeChar, null, true, true, false, false, false, false, true);
    activeChar.sendMessage("You gave " + damage + " damage to " + cha.getName() + ".");
  }

  private static enum Commands
  {
    admin_kill, 
    admin_damage;
  }
}