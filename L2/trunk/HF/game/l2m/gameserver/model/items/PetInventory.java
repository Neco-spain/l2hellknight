package l2m.gameserver.model.items;

import java.util.Collection;
import java.util.List;
import l2m.gameserver.data.dao.ItemsDAO;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.instances.PetInstance;
import l2m.gameserver.network.serverpackets.PetInventoryUpdate;
import l2m.gameserver.templates.item.ItemTemplate;
import l2m.gameserver.utils.ItemFunctions;

public class PetInventory extends Inventory
{
  private final PetInstance _actor;

  public PetInventory(PetInstance actor)
  {
    super(actor.getPlayer().getObjectId());
    _actor = actor;
  }

  public PetInstance getActor()
  {
    return _actor;
  }

  public Player getOwner()
  {
    return _actor.getPlayer();
  }

  protected ItemInstance.ItemLocation getBaseLocation()
  {
    return ItemInstance.ItemLocation.PET_INVENTORY;
  }

  protected ItemInstance.ItemLocation getEquipLocation()
  {
    return ItemInstance.ItemLocation.PET_PAPERDOLL;
  }

  protected void onRefreshWeight()
  {
    getActor().sendPetInfo();
  }

  protected void sendAddItem(ItemInstance item)
  {
    getOwner().sendPacket(new PetInventoryUpdate().addNewItem(item));
  }

  protected void sendModifyItem(ItemInstance item)
  {
    getOwner().sendPacket(new PetInventoryUpdate().addModifiedItem(item));
  }

  protected void sendRemoveItem(ItemInstance item)
  {
    getOwner().sendPacket(new PetInventoryUpdate().addRemovedItem(item));
  }

  public void restore()
  {
    int ownerId = getOwnerId();

    writeLock();
    try
    {
      Collection items = _itemsDAO.getItemsByOwnerIdAndLoc(ownerId, getBaseLocation());

      for (ItemInstance item : items)
      {
        _items.add(item);
        onRestoreItem(item);
      }

      items = _itemsDAO.getItemsByOwnerIdAndLoc(ownerId, getEquipLocation());

      for (ItemInstance item : items)
      {
        _items.add(item);
        onRestoreItem(item);
        if (ItemFunctions.checkIfCanEquip(getActor(), item) == null)
          setPaperdollItem(item.getEquipSlot(), item);
      }
    }
    finally
    {
      writeUnlock();
    }

    refreshWeight();
  }

  public void store()
  {
    writeLock();
    try
    {
      _itemsDAO.update(_items);
    }
    finally
    {
      writeUnlock();
    }
  }

  public void validateItems()
  {
    for (ItemInstance item : _paperdoll)
      if ((item != null) && ((ItemFunctions.checkIfCanEquip(getActor(), item) != null) || (!item.getTemplate().testCondition(getActor(), item))))
        unEquipItem(item);
  }
}