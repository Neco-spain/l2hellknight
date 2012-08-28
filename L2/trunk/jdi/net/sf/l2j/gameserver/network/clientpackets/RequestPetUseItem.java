package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.handler.ItemHandler;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2PetDataTable;
import net.sf.l2j.gameserver.model.PetInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.PetInfo;
import net.sf.l2j.gameserver.network.serverpackets.PetItemList;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.util.FloodProtector;

public final class RequestPetUseItem extends L2GameClientPacket
{
  private static Logger _log = Logger.getLogger(RequestPetUseItem.class.getName());
  private static final String _C__8A_REQUESTPETUSEITEM = "[C] 8a RequestPetUseItem";
  private int _objectId;

  protected void readImpl()
  {
    _objectId = readD();
  }

  protected void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();

    if (activeChar == null) {
      return;
    }
    L2PetInstance pet = (L2PetInstance)activeChar.getPet();

    if (pet == null) {
      return;
    }
    L2ItemInstance item = pet.getInventory().getItemByObjectId(_objectId);

    if (!FloodProtector.getInstance().tryPerformAction(activeChar.getObjectId(), 0)) {
      return;
    }
    if (item == null) {
      return;
    }
    if (item.isWear()) {
      return;
    }
    int itemId = item.getItemId();

    if ((activeChar.isAlikeDead()) || (pet.isDead()))
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
      sm.addItemName(item.getItemId());
      activeChar.sendPacket(sm);
      sm = null;
      return;
    }

    if (Config.DEBUG) {
      _log.finest(activeChar.getObjectId() + ": pet use item " + _objectId);
    }

    if (item.isEquipable())
    {
      if ((L2PetDataTable.isWolf(pet.getNpcId())) && (item.getItem().isForWolf()))
      {
        useItem(pet, item, activeChar);
        return;
      }
      if ((L2PetDataTable.isHatchling(pet.getNpcId())) && (item.getItem().isForHatchling()))
      {
        useItem(pet, item, activeChar);
        return;
      }
      if ((L2PetDataTable.isStrider(pet.getNpcId())) && (item.getItem().isForStrider()))
      {
        useItem(pet, item, activeChar);
        return;
      }
      if ((L2PetDataTable.isBaby(pet.getNpcId())) && (item.getItem().isForBabyPet()))
      {
        useItem(pet, item, activeChar);
        return;
      }

      activeChar.sendPacket(new SystemMessage(SystemMessageId.ITEM_NOT_FOR_PETS));
      return;
    }

    if (L2PetDataTable.isPetFood(itemId))
    {
      if ((L2PetDataTable.isWolf(pet.getNpcId())) && (L2PetDataTable.isWolfFood(itemId)))
      {
        feed(activeChar, pet, item);
        return;
      }
      if ((L2PetDataTable.isSinEater(pet.getNpcId())) && (L2PetDataTable.isSinEaterFood(itemId)))
      {
        feed(activeChar, pet, item);
        return;
      }
      if ((L2PetDataTable.isHatchling(pet.getNpcId())) && (L2PetDataTable.isHatchlingFood(itemId)))
      {
        feed(activeChar, pet, item);
        return;
      }
      if ((L2PetDataTable.isStrider(pet.getNpcId())) && (L2PetDataTable.isStriderFood(itemId)))
      {
        feed(activeChar, pet, item);
        return;
      }
      if ((L2PetDataTable.isWyvern(pet.getNpcId())) && (L2PetDataTable.isWyvernFood(itemId)))
      {
        feed(activeChar, pet, item);
        return;
      }
      if ((L2PetDataTable.isBaby(pet.getNpcId())) && (L2PetDataTable.isBabyFood(itemId)))
      {
        feed(activeChar, pet, item);
      }
    }

    IItemHandler handler = ItemHandler.getInstance().getItemHandler(item.getItemId());

    if (handler != null)
    {
      useItem(pet, item, activeChar);
    }
    else
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.ITEM_NOT_FOR_PETS);
      activeChar.sendPacket(sm);
    }
  }

  private synchronized void useItem(L2PetInstance pet, L2ItemInstance item, L2PcInstance activeChar)
  {
    if (item.isEquipable())
    {
      if (item.isEquipped())
        pet.getInventory().unEquipItemInSlot(item.getEquipSlot());
      else {
        pet.getInventory().equipItem(item);
      }
      PetItemList pil = new PetItemList(pet);
      activeChar.sendPacket(pil);

      PetInfo pi = new PetInfo(pet);
      activeChar.sendPacket(pi);

      pet.updateEffectIcons(true);
    }
    else
    {
      IItemHandler handler = ItemHandler.getInstance().getItemHandler(item.getItemId());

      if (handler == null)
        _log.warning("no itemhandler registered for itemId:" + item.getItemId());
      else
        handler.useItem(pet, item);
    }
  }

  private void feed(L2PcInstance player, L2PetInstance pet, L2ItemInstance item)
  {
    if (pet.destroyItem("Feed", item.getObjectId(), 1, pet, false))
      pet.setCurrentFed(pet.getCurrentFed() + 100);
    pet.broadcastStatusUpdate();
  }

  public String getType()
  {
    return "[C] 8a RequestPetUseItem";
  }
}