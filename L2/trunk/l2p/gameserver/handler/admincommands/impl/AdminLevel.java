package l2p.gameserver.handler.admincommands.impl;

import l2p.gameserver.cache.Msg;
import l2p.gameserver.handler.admincommands.IAdminCommandHandler;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.GameObject;
import l2p.gameserver.model.PetData;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.base.Experience;
import l2p.gameserver.model.base.PlayerAccess;
import l2p.gameserver.model.instances.PetInstance;
import l2p.gameserver.tables.PetDataTable;

public class AdminLevel
  implements IAdminCommandHandler
{
  private void setLevel(Player activeChar, GameObject target, int level)
  {
    if ((target == null) || ((!target.isPlayer()) && (!target.isPet())))
    {
      activeChar.sendPacket(Msg.INVALID_TARGET);
      return;
    }
    if ((level < 1) || (level > Experience.getMaxLevel()))
    {
      activeChar.sendMessage("You must specify level 1 - " + Experience.getMaxLevel());
      return;
    }
    if (target.isPlayer())
    {
      Long exp_add = Long.valueOf(Experience.LEVEL[level] - ((Player)target).getExp());
      ((Player)target).addExpAndSp(exp_add.longValue(), 0L);
      return;
    }
    if (target.isPet())
    {
      Long exp_add = Long.valueOf(PetDataTable.getInstance().getInfo(((PetInstance)target).getNpcId(), level).getExp() - ((PetInstance)target).getExp());
      ((PetInstance)target).addExpAndSp(exp_add.longValue(), 0L);
    }
  }

  public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
  {
    Commands command = (Commands)comm;

    if (!activeChar.getPlayerAccess().CanEditChar) {
      return false;
    }
    GameObject target = activeChar.getTarget();
    if ((target == null) || ((!target.isPlayer()) && (!target.isPet())))
    {
      activeChar.sendPacket(Msg.INVALID_TARGET);
      return false;
    }
    int level;
    switch (1.$SwitchMap$l2p$gameserver$handler$admincommands$impl$AdminLevel$Commands[command.ordinal()])
    {
    case 1:
    case 2:
      if (wordList.length < 2)
      {
        activeChar.sendMessage("USAGE: //addLevel level");
        return false;
      }
      try
      {
        level = Integer.parseInt(wordList[1]);
      }
      catch (NumberFormatException e)
      {
        activeChar.sendMessage("You must specify level");
        return false;
      }
      setLevel(activeChar, target, level + ((Creature)target).getLevel());
      break;
    case 3:
    case 4:
      if (wordList.length < 2)
      {
        activeChar.sendMessage("USAGE: //setLevel level");
        return false;
      }
      try
      {
        level = Integer.parseInt(wordList[1]);
      }
      catch (NumberFormatException e)
      {
        activeChar.sendMessage("You must specify level");
        return false;
      }
      setLevel(activeChar, target, level);
    }

    return true;
  }

  public Enum[] getAdminCommandEnum()
  {
    return Commands.values();
  }

  private static enum Commands
  {
    admin_add_level, 
    admin_addLevel, 
    admin_set_level, 
    admin_setLevel;
  }
}