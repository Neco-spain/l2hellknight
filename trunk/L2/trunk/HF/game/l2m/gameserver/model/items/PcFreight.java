package l2m.gameserver.model.items;

import l2m.gameserver.model.Player;

public class PcFreight extends Warehouse
{
  public PcFreight(Player player)
  {
    super(player.getObjectId());
  }

  public PcFreight(int objectId)
  {
    super(objectId);
  }

  public ItemInstance.ItemLocation getItemLocation()
  {
    return ItemInstance.ItemLocation.FREIGHT;
  }
}