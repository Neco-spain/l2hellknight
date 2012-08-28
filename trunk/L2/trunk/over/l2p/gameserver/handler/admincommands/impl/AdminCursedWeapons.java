package l2p.gameserver.handler.admincommands.impl;

import l2p.gameserver.handler.admincommands.IAdminCommandHandler;
import l2p.gameserver.instancemanager.CursedWeaponsManager;
import l2p.gameserver.model.CursedWeapon;
import l2p.gameserver.model.GameObject;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.base.PlayerAccess;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.utils.ItemFunctions;

public class AdminCursedWeapons
  implements IAdminCommandHandler
{
  public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
  {
    Commands command = (Commands)comm;

    if (!activeChar.getPlayerAccess().Menu) {
      return false;
    }
    CursedWeaponsManager cwm = CursedWeaponsManager.getInstance();

    CursedWeapon cw = null;
    switch (1.$SwitchMap$l2p$gameserver$handler$admincommands$impl$AdminCursedWeapons$Commands[command.ordinal()])
    {
    case 1:
    case 2:
    case 3:
    case 4:
      if (wordList.length < 2)
      {
        activeChar.sendMessage("\u0412\u044B \u043D\u0435 \u0443\u043A\u0430\u0437\u0430\u043B\u0438 id");
        return false;
      }
      for (CursedWeapon cwp : CursedWeaponsManager.getInstance().getCursedWeapons())
        if (cwp.getName().toLowerCase().contains(wordList[1].toLowerCase()))
          cw = cwp;
      if (cw != null)
        break;
      activeChar.sendMessage("\u041D\u0435\u0438\u0437\u0432\u0435\u0441\u0442\u043D\u044B\u0439 id");
      return false;
    }

    switch (1.$SwitchMap$l2p$gameserver$handler$admincommands$impl$AdminCursedWeapons$Commands[command.ordinal()])
    {
    case 5:
      activeChar.sendMessage("======= Cursed Weapons: =======");
      for (CursedWeapon c : cwm.getCursedWeapons())
      {
        activeChar.sendMessage("> " + c.getName() + " (" + c.getItemId() + ")");
        if (c.isActivated())
        {
          Player pl = c.getPlayer();
          activeChar.sendMessage("  Player holding: " + pl.getName());
          activeChar.sendMessage("  Player karma: " + c.getPlayerKarma());
          activeChar.sendMessage("  Time Remaining: " + c.getTimeLeft() / 60000L + " min.");
          activeChar.sendMessage("  Kills : " + c.getNbKills());
        }
        else if (c.isDropped())
        {
          activeChar.sendMessage("  Lying on the ground.");
          activeChar.sendMessage("  Time Remaining: " + c.getTimeLeft() / 60000L + " min.");
          activeChar.sendMessage("  Kills : " + c.getNbKills());
        }
        else {
          activeChar.sendMessage("  Don't exist in the world.");
        }
      }
      break;
    case 6:
      activeChar.sendMessage("Cursed weapons can't be reloaded.");
      break;
    case 1:
      if (cw == null)
        return false;
      CursedWeaponsManager.getInstance().endOfLife(cw);
      break;
    case 2:
      if (cw == null)
        return false;
      activeChar.teleToLocation(cw.getLoc());
      break;
    case 3:
      if (cw == null)
        return false;
      if (cw.isActive()) {
        activeChar.sendMessage("This cursed weapon is already active.");
      }
      else {
        GameObject target = activeChar.getTarget();
        if ((target != null) && (target.isPlayer()) && (!((Player)target).isInOlympiadMode()))
        {
          Player player = (Player)target;
          ItemInstance item = ItemFunctions.createItem(cw.getItemId());
          cwm.activate(player, player.getInventory().addItem(item));
          cwm.showUsageTime(player, cw);
        }
      }
      break;
    case 4:
      if (cw == null)
        return false;
      if (cw.isActive()) {
        activeChar.sendMessage("This cursed weapon is already active.");
      }
      else {
        GameObject target = activeChar.getTarget();
        if ((target == null) || (!target.isPlayer()) || (((Player)target).isInOlympiadMode()))
          break;
        Player player = (Player)target;
        cw.create(null, player);
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
    admin_cw_info, 
    admin_cw_remove, 
    admin_cw_goto, 
    admin_cw_reload, 
    admin_cw_add, 
    admin_cw_drop;
  }
}