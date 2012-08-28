package l2m.gameserver.model.items;

import l2m.gameserver.model.pledge.Clan;

public final class ClanWarehouse extends Warehouse
{
  public ClanWarehouse(Clan clan)
  {
    super(clan.getClanId());
  }

  public ItemInstance.ItemLocation getItemLocation()
  {
    return ItemInstance.ItemLocation.CLANWH;
  }
}