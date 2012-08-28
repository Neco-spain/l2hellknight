package l2m.gameserver.model.items;

import l2m.gameserver.model.Player;

public class PcWarehouse extends Warehouse
{
  public PcWarehouse(Player owner)
  {
    super(owner.getObjectId());
  }

  public PcWarehouse(int ownerId)
  {
    super(ownerId);
  }

  public ItemInstance.ItemLocation getItemLocation()
  {
    return ItemInstance.ItemLocation.WAREHOUSE;
  }
}