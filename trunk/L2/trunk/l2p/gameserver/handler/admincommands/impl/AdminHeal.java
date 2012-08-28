package l2p.gameserver.handler.admincommands.impl;

import l2p.gameserver.cache.Msg;
import l2p.gameserver.handler.admincommands.IAdminCommandHandler;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.GameObject;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.World;
import l2p.gameserver.model.base.PlayerAccess;

public class AdminHeal
  implements IAdminCommandHandler
{
  public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
  {
    Commands command = (Commands)comm;

    if (!activeChar.getPlayerAccess().Heal) {
      return false;
    }
    switch (1.$SwitchMap$l2p$gameserver$handler$admincommands$impl$AdminHeal$Commands[command.ordinal()])
    {
    case 1:
      if (wordList.length == 1)
        handleRes(activeChar);
      else {
        handleRes(activeChar, wordList[1]);
      }
    }

    return true;
  }

  public Enum[] getAdminCommandEnum()
  {
    return Commands.values();
  }

  private void handleRes(Player activeChar)
  {
    handleRes(activeChar, null);
  }

  private void handleRes(Player activeChar, String player)
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
        {
          character.setCurrentHpMp(character.getMaxHp(), character.getMaxMp());
          if (character.isPlayer())
            character.setCurrentCp(character.getMaxCp());
        }
        activeChar.sendMessage("Healed within " + radius + " unit radius.");
        return;
      }
    }

    if (obj == null) {
      obj = activeChar;
    }
    if ((obj instanceof Creature))
    {
      Creature target = (Creature)obj;
      target.setCurrentHpMp(target.getMaxHp(), target.getMaxMp());
      if (target.isPlayer())
        target.setCurrentCp(target.getMaxCp());
    }
    else {
      activeChar.sendPacket(Msg.INVALID_TARGET);
    }
  }

  private static enum Commands
  {
    admin_heal;
  }
}