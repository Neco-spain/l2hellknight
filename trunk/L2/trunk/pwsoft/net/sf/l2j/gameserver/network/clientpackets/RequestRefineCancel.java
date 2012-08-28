package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExVariationCancelResult;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Item;

public final class RequestRefineCancel extends L2GameClientPacket
{
  private int _targetItemObjId;

  protected void readImpl()
  {
    _targetItemObjId = readD();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    L2ItemInstance targetItem = (L2ItemInstance)L2World.getInstance().findObject(_targetItemObjId);

    if (player == null) return;
    if (targetItem == null)
    {
      player.sendPacket(new ExVariationCancelResult(0));
      return;
    }

    if (!targetItem.canBeEnchanted())
    {
      player.sendPacket(new ExVariationCancelResult(0));
      return;
    }

    if (!targetItem.isAugmented())
    {
      player.sendPacket(Static.AUGMENTATION_REMOVAL_CAN_ONLY_BE_DONE_ON_AN_AUGMENTED_ITEM);
      player.sendPacket(new ExVariationCancelResult(0));
      return;
    }

    int price = 0;
    switch (targetItem.getItem().getItemGrade())
    {
    case 2:
      if (targetItem.getCrystalCount() < 1720)
        price = 95000;
      else if (targetItem.getCrystalCount() < 2452)
        price = 150000;
      else
        price = 210000;
      break;
    case 3:
      if (targetItem.getCrystalCount() < 1746)
        price = 240000;
      else
        price = 270000;
      break;
    case 4:
      if (targetItem.getCrystalCount() < 2160)
        price = 330000;
      else if (targetItem.getCrystalCount() < 2824)
        price = 390000;
      else
        price = 420000;
      break;
    case 5:
      price = 480000;
      break;
    default:
      player.sendPacket(new ExVariationCancelResult(0));
      return;
    }

    if (!player.reduceAdena("RequestRefineCancel", price, null, true)) return;

    if (targetItem.isEquipped()) player.disarmWeapons();

    targetItem.removeAugmentation();

    player.sendPacket(new ExVariationCancelResult(1));

    InventoryUpdate iu = new InventoryUpdate();
    iu.addModifiedItem(targetItem);
    player.sendPacket(iu);

    player.sendPacket(SystemMessage.id(SystemMessageId.AUGMENTATION_HAS_BEEN_SUCCESSFULLY_REMOVED_FROM_YOUR_S1).addString(targetItem.getItemName()));
  }
}