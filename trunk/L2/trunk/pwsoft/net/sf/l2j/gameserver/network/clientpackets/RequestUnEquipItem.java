package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.templates.L2Item;

public class RequestUnEquipItem extends L2GameClientPacket
{
  private int _slot;

  protected void readImpl()
  {
    _slot = readD();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();

    if (player == null) {
      return;
    }

    if (System.currentTimeMillis() - player.gCPL() < 100L) {
      return;
    }

    player.sCPL();

    L2ItemInstance item = player.getInventory().getPaperdollItemByL2ItemId(_slot);
    if (item == null) {
      return;
    }

    if ((player.isStunned()) || (player.isSleeping()) || (player.isParalyzed()) || (player.isAlikeDead())) {
      return;
    }
    if (item.isWear()) {
      return;
    }

    switch (item.getItem().getBodyPart())
    {
    case 128:
    case 256:
    case 16384:
      if ((player.isCursedWeaponEquiped()) || (item.getItemId() == 6408))
      {
        player.sendActionFailed();
        return;
      }
      player.equipWeapon(item);
      break;
    default:
      player.useEquippableItem(item, true);
    }
  }
}