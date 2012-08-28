package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Item;

public final class RequestCrystallizeItem extends L2GameClientPacket
{
  private static Logger _log = Logger.getLogger(RequestCrystallizeItem.class.getName());
  private int _objectId;
  private int _count;

  protected void readImpl()
  {
    _objectId = readD();
    _count = readD();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();

    if (player == null) {
      return;
    }
    if (System.currentTimeMillis() - player.gCPW() < 100L)
    {
      player.sendActionFailed();
      return;
    }

    player.sCPW();

    if ((_count <= 0) || (player.isParalyzed()))
    {
      return;
    }

    if ((player.getPrivateStoreType() != 0) || (player.isInCrystallize()))
    {
      player.sendPacket(Static.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE);
      return;
    }

    int skillLevel = player.getSkillLevel(248);
    if (skillLevel <= 0)
    {
      player.sendPacket(Static.CRYSTALLIZE_LEVEL_TOO_LOW);
      player.sendActionFailed();
      return;
    }

    PcInventory inventory = player.getInventory();
    if (inventory != null)
    {
      L2ItemInstance item = inventory.getItemByObjectId(_objectId);
      if ((item == null) || (item.isWear()))
      {
        player.sendActionFailed();
        return;
      }

      int itemId = item.getItemId();
      if (((itemId >= 6611) && (itemId <= 6621)) || (itemId == 6842)) {
        return;
      }
      if (_count > item.getCount())
      {
        _count = player.getInventory().getItemByObjectId(_objectId).getCount();
      }

    }

    L2ItemInstance itemToRemove = player.getInventory().getItemByObjectId(_objectId);
    if ((itemToRemove == null) || (itemToRemove.isWear()))
    {
      return;
    }
    if ((!itemToRemove.getItem().isCrystallizable()) || (itemToRemove.getItem().getCrystalCount() <= 0) || (itemToRemove.getItem().getCrystalType() == 0))
    {
      _log.warning("" + player.getObjectId() + " tried to crystallize " + itemToRemove.getItem().getItemId());
      return;
    }

    if ((itemToRemove.getItem().getCrystalType() == 2) && (skillLevel <= 1))
    {
      player.sendPacket(Static.CRYSTALLIZE_LEVEL_TOO_LOW);
      player.sendActionFailed();
      return;
    }

    if ((itemToRemove.getItem().getCrystalType() == 3) && (skillLevel <= 2))
    {
      player.sendPacket(Static.CRYSTALLIZE_LEVEL_TOO_LOW);
      player.sendActionFailed();
      return;
    }

    if ((itemToRemove.getItem().getCrystalType() == 4) && (skillLevel <= 3))
    {
      player.sendPacket(Static.CRYSTALLIZE_LEVEL_TOO_LOW);
      player.sendActionFailed();
      return;
    }

    if ((itemToRemove.getItem().getCrystalType() == 5) && (skillLevel <= 4))
    {
      player.sendPacket(Static.CRYSTALLIZE_LEVEL_TOO_LOW);
      player.sendActionFailed();
      return;
    }

    player.setInCrystallize(true);

    if (itemToRemove.isEquipped())
    {
      L2ItemInstance[] unequiped = player.getInventory().unEquipItemInSlotAndRecord(itemToRemove.getEquipSlot());
      InventoryUpdate iu = new InventoryUpdate();
      for (int i = 0; i < unequiped.length; i++)
      {
        iu.addModifiedItem(unequiped[i]);
      }
      player.sendPacket(iu);
    }

    L2ItemInstance removedItem = player.getInventory().destroyItem("Crystalize", _objectId, _count, player, null);

    int crystalId = itemToRemove.getItem().getCrystalItemId();
    int crystalAmount = itemToRemove.getCrystalCount();
    L2ItemInstance createditem = player.getInventory().addItem("Crystalize", crystalId, crystalAmount, player, itemToRemove);

    player.sendPacket(SystemMessage.id(SystemMessageId.EARNED_S2_S1_S).addItemName(crystalId).addNumber(crystalAmount));

    if (!Config.FORCE_INVENTORY_UPDATE)
    {
      InventoryUpdate iu = new InventoryUpdate();
      if (removedItem.getCount() == 0)
        iu.addRemovedItem(removedItem);
      else {
        iu.addModifiedItem(removedItem);
      }
      if (createditem.getCount() != crystalAmount)
        iu.addModifiedItem(createditem);
      else {
        iu.addNewItem(createditem);
      }
      player.sendPacket(iu);
    }
    else {
      player.sendItems(false);
    }

    player.sendChanges();
    player.broadcastUserInfo();

    L2World world = L2World.getInstance();
    world.removeObject(removedItem);

    player.setInCrystallize(false);
  }
}