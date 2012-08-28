package net.sf.l2j.gameserver.network.clientpackets;

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
  private static final String _C__D0_2E_REQUESTREFINECANCEL = "[C] D0:2E RequestRefineCancel";
  private int _targetItemObjId;

  protected void readImpl()
  {
    _targetItemObjId = readD();
  }

  protected void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    L2ItemInstance targetItem = (L2ItemInstance)L2World.getInstance().findObject(_targetItemObjId);

    if (activeChar == null) return;
    if (targetItem == null)
    {
      activeChar.sendPacket(new ExVariationCancelResult(0));
      return;
    }

    if (!targetItem.isAugmented())
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.AUGMENTATION_REMOVAL_CAN_ONLY_BE_DONE_ON_AN_AUGMENTED_ITEM));
      activeChar.sendPacket(new ExVariationCancelResult(0));
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
      activeChar.sendPacket(new ExVariationCancelResult(0));
      return;
    }

    if (!activeChar.reduceAdena("RequestRefineCancel", price, null, true)) return;

    if (targetItem.isEquipped()) activeChar.disarmWeapons();

    targetItem.removeAugmentation();

    activeChar.sendPacket(new ExVariationCancelResult(1));

    InventoryUpdate iu = new InventoryUpdate();
    iu.addModifiedItem(targetItem);
    activeChar.sendPacket(iu);

    SystemMessage sm = new SystemMessage(SystemMessageId.AUGMENTATION_HAS_BEEN_SUCCESSFULLY_REMOVED_FROM_YOUR_S1);
    sm.addString(targetItem.getItemName());
    activeChar.sendPacket(sm);
  }

  public String getType()
  {
    return "[C] D0:2E RequestRefineCancel";
  }
}