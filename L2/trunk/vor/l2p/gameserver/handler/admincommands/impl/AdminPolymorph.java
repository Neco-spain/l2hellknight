package l2p.gameserver.handler.admincommands.impl;

import l2p.gameserver.cache.Msg;
import l2p.gameserver.data.xml.holder.NpcHolder;
import l2p.gameserver.handler.admincommands.IAdminCommandHandler;
import l2p.gameserver.model.GameObject;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.base.PlayerAccess;

public class AdminPolymorph
  implements IAdminCommandHandler
{
  public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
  {
    Commands command = (Commands)comm;

    if (!activeChar.getPlayerAccess().CanPolymorph) {
      return false;
    }
    GameObject target = activeChar.getTarget();

    switch (1.$SwitchMap$l2p$gameserver$handler$admincommands$impl$AdminPolymorph$Commands[command.ordinal()])
    {
    case 1:
      target = activeChar;
    case 2:
    case 3:
      if ((target == null) || (!target.isPlayer()))
      {
        activeChar.sendPacket(Msg.INVALID_TARGET);
        return false;
      }
      try
      {
        int id = Integer.parseInt(wordList[1]);
        if (NpcHolder.getInstance().getTemplate(id) != null)
        {
          ((Player)target).setPolyId(id);
          ((Player)target).broadcastCharInfo();
        }
      }
      catch (Exception e)
      {
        activeChar.sendMessage("USAGE: //poly id [type:npc|item]");
        return false;
      }

    case 4:
      target = activeChar;
    case 5:
    case 6:
      if ((target == null) || (!target.isPlayer()))
      {
        activeChar.sendPacket(Msg.INVALID_TARGET);
        return false;
      }
      ((Player)target).setPolyId(0);
      ((Player)target).broadcastCharInfo();
    }

    return true;
  }

  public Enum[] getAdminCommandEnum()
  {
    return Commands.values();
  }

  private static enum Commands
  {
    admin_polyself, 
    admin_polymorph, 
    admin_poly, 
    admin_unpolyself, 
    admin_unpolymorph, 
    admin_unpoly;
  }
}