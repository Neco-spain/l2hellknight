package l2p.gameserver.model.items;

import l2p.gameserver.model.Player;

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