package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2PetDataTable;
import net.sf.l2j.gameserver.model.PetInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.network.serverpackets.PetItemList;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Item;
import scripts.items.IItemHandler;
import scripts.items.ItemHandler;

public final class RequestPetUseItem extends L2GameClientPacket
{
  private static Logger _log = Logger.getLogger(RequestPetUseItem.class.getName());
  private int _objectId;

  protected void readImpl()
  {
    _objectId = readD();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();

    if (player == null) {
      return;
    }
    if (System.currentTimeMillis() - player.gCPX() < 200L) {
      return;
    }
    player.sCPX();

    L2PetInstance pet = (L2PetInstance)player.getPet();

    if (pet == null) {
      return;
    }
    L2ItemInstance item = pet.getInventory().getItemByObjectId(_objectId);

    if (item == null) {
      return;
    }
    if (item.isWear()) {
      return;
    }
    int itemId = item.getItemId();

    if ((player.isAlikeDead()) || (pet.isDead()))
    {
      player.sendPacket(SystemMessage.id(SystemMessageId.S1_CANNOT_BE_USED).addItemName(item.getItemId()));
      return;
    }

    if (item.isEquipable())
    {
      if ((L2PetDataTable.isWolf(pet.getNpcId())) && (item.getItem().isForWolf()))
      {
        useItem(pet, item, player);
        return;
      }
      if ((L2PetDataTable.isHatchling(pet.getNpcId())) && (item.getItem().isForHatchling()))
      {
        useItem(pet, item, player);
        return;
      }
      if ((L2PetDataTable.isStrider(pet.getNpcId())) && (item.getItem().isForStrider()))
      {
        useItem(pet, item, player);
        return;
      }
      if ((L2PetDataTable.isBaby(pet.getNpcId())) && (item.getItem().isForBabyPet()))
      {
        useItem(pet, item, player);
        return;
      }

      player.sendPacket(Static.ITEM_NOT_FOR_PETS);
      return;
    }

    if (L2PetDataTable.isPetFood(itemId))
    {
      if ((L2PetDataTable.isWolf(pet.getNpcId())) && (L2PetDataTable.isWolfFood(itemId)))
      {
        feed(player, pet, item);
        return;
      }
      if ((L2PetDataTable.isSinEater(pet.getNpcId())) && (L2PetDataTable.isSinEaterFood(itemId)))
      {
        feed(player, pet, item);
        return;
      }
      if ((L2PetDataTable.isHatchling(pet.getNpcId())) && (L2PetDataTable.isHatchlingFood(itemId)))
      {
        feed(player, pet, item);
        return;
      }
      if ((L2PetDataTable.isStrider(pet.getNpcId())) && (L2PetDataTable.isStriderFood(itemId)))
      {
        feed(player, pet, item);
        return;
      }
      if ((L2PetDataTable.isWyvern(pet.getNpcId())) && (L2PetDataTable.isWyvernFood(itemId)))
      {
        feed(player, pet, item);
        return;
      }
      if ((L2PetDataTable.isBaby(pet.getNpcId())) && (L2PetDataTable.isBabyFood(itemId)))
      {
        feed(player, pet, item);
      }
    }

    IItemHandler handler = ItemHandler.getInstance().getItemHandler(item.getItemId());
    if (handler != null)
    {
      useItem(pet, item, player);
    }
    else
      player.sendPacket(Static.ITEM_NOT_FOR_PETS);
  }

  private synchronized void useItem(L2PetInstance pet, L2ItemInstance item, L2PcInstance player)
  {
    if (item.isEquipable())
    {
      if (item.isEquipped())
      {
        pet.getInventory().unEquipItemInSlot(item.getLocationSlot());
        switch (item.getItem().getBodyPart())
        {
        case 128:
          pet.setWeapon(0);
          break;
        case 1024:
          pet.setArmor(0);
          break;
        case 8:
          pet.setJewel(0);
        }

      }
      else
      {
        pet.getInventory().equipItem(item);
        switch (item.getItem().getBodyPart())
        {
        case 128:
          pet.setWeapon(item.getItemId());
          break;
        case 1024:
          pet.setArmor(item.getItemId());
          break;
        case 8:
          pet.setJewel(item.getItemId());
        }

      }

      PetItemList pil = new PetItemList(pet);
      player.sendPacket(pil);

      pet.updateAndBroadcastStatus(1);
    }
    else
    {
      IItemHandler handler = ItemHandler.getInstance().getItemHandler(item.getItemId());

      if (handler == null) {
        _log.warning("no itemhandler registered for itemId:" + item.getItemId());
      }
      else {
        handler.useItem(pet, item);
        pet.updateAndBroadcastStatus(1);
      }
    }
  }

  private void feed(L2PcInstance player, L2PetInstance pet, L2ItemInstance item)
  {
    if (pet.destroyItem("Feed", item.getObjectId(), 1, pet, false)) {
      pet.setCurrentFed(pet.getCurrentFed() + 100);
    }
    pet.broadcastPacket(new MagicSkillUser(pet, pet, 2101, 1, 1, 0));
    pet.broadcastStatusUpdate();
  }

  public String getType()
  {
    return "[C] PetUseItem";
  }
}