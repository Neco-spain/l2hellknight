package l2m.gameserver.network.clientpackets;

import l2p.commons.dao.JdbcEntityState;
import l2m.gameserver.cache.Msg;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.actor.instances.player.ShortCut;
import l2m.gameserver.model.items.ItemInstance;
import l2m.gameserver.model.items.PcInventory;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.ExVariationCancelResult;
import l2m.gameserver.network.serverpackets.InventoryUpdate;
import l2m.gameserver.network.serverpackets.ShortCutRegister;
import l2m.gameserver.network.serverpackets.SystemMessage;
import l2m.gameserver.network.serverpackets.components.IStaticPacket;
import l2m.gameserver.templates.item.ItemTemplate;
import l2m.gameserver.templates.item.ItemTemplate.Grade;

public final class RequestRefineCancel extends L2GameClientPacket
{
  private int _targetItemObjId;

  protected void readImpl()
  {
    _targetItemObjId = readD();
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    if (activeChar.isActionsDisabled())
    {
      activeChar.sendPacket(new ExVariationCancelResult(0));
      return;
    }

    if (activeChar.isInStoreMode())
    {
      activeChar.sendPacket(new ExVariationCancelResult(0));
      return;
    }

    if (activeChar.isInTrade())
    {
      activeChar.sendPacket(new ExVariationCancelResult(0));
      return;
    }

    ItemInstance targetItem = activeChar.getInventory().getItemByObjectId(_targetItemObjId);

    if ((targetItem == null) || (!targetItem.isAugmented()))
    {
      activeChar.sendPacket(new IStaticPacket[] { new ExVariationCancelResult(0), Msg.AUGMENTATION_REMOVAL_CAN_ONLY_BE_DONE_ON_AN_AUGMENTED_ITEM });
      return;
    }

    int price = getRemovalPrice(targetItem.getTemplate());

    if (price < 0) {
      activeChar.sendPacket(new ExVariationCancelResult(0));
    }

    if (!activeChar.reduceAdena(price, true))
    {
      activeChar.sendPacket(new IStaticPacket[] { new ExVariationCancelResult(0), Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA });
      return;
    }

    boolean equipped = false;
    if ((equipped = targetItem.isEquipped())) {
      activeChar.getInventory().unEquipItem(targetItem);
    }

    targetItem.setAugmentationId(0);
    targetItem.setJdbcState(JdbcEntityState.UPDATED);
    targetItem.update();

    if (equipped) {
      activeChar.getInventory().equipItem(targetItem);
    }

    InventoryUpdate iu = new InventoryUpdate().addModifiedItem(targetItem);

    SystemMessage sm = new SystemMessage(1965);
    sm.addItemName(targetItem.getItemId());
    activeChar.sendPacket(new IStaticPacket[] { new ExVariationCancelResult(1), iu, sm });

    for (ShortCut sc : activeChar.getAllShortCuts())
      if ((sc.getId() == targetItem.getObjectId()) && (sc.getType() == 1))
        activeChar.sendPacket(new ShortCutRegister(activeChar, sc));
    activeChar.sendChanges();
  }

  public static int getRemovalPrice(ItemTemplate item)
  {
    switch (item.getItemGrade().cry)
    {
    case 1459:
      if (item.getCrystalCount() < 1720)
        return 95000;
      if (item.getCrystalCount() < 2452) {
        return 150000;
      }
      return 210000;
    case 1460:
      if (item.getCrystalCount() < 1746) {
        return 240000;
      }
      return 270000;
    case 1461:
      if (item.getCrystalCount() < 2160)
        return 330000;
      if (item.getCrystalCount() < 2824) {
        return 390000;
      }
      return 420000;
    case 1462:
      if (item.getCrystalCount() == 10394)
        return 920000;
      if (item.getCrystalCount() == 7050)
        return 720000;
      if (item.getName().contains("Vesper")) {
        return 920000;
      }
      return 480000;
    }

    return -1;
  }
}