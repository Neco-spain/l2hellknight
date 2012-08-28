package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.ExConfirmCancelItem;
import net.sf.l2j.gameserver.templates.L2Item;

public final class RequestConfirmCancelItem extends L2GameClientPacket
{
  private int _itemId;

  protected void readImpl()
  {
    _itemId = readD();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    L2ItemInstance item = (L2ItemInstance)L2World.getInstance().findObject(_itemId);

    if ((player == null) || (item == null)) return;
    if (!item.isAugmented())
    {
      player.sendPacket(Static.AUGMENTATION_REMOVAL_CAN_ONLY_BE_DONE_ON_AN_AUGMENTED_ITEM);
      return;
    }

    int price = 0;
    switch (item.getItem().getItemGrade())
    {
    case 2:
      if (item.getCrystalCount() < 1720)
        price = 95000;
      else if (item.getCrystalCount() < 2452)
        price = 150000;
      else
        price = 210000;
      break;
    case 3:
      if (item.getCrystalCount() < 1746)
        price = 240000;
      else
        price = 270000;
      break;
    case 4:
      if (item.getCrystalCount() < 2160)
        price = 330000;
      else if (item.getCrystalCount() < 2824)
        price = 390000;
      else
        price = 420000;
      break;
    case 5:
      price = 480000;
      break;
    default:
      return;
    }

    player.sendPacket(new ExConfirmCancelItem(_itemId, price));
  }
}