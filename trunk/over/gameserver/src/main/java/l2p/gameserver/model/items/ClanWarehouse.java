package l2p.gameserver.model.items;

import l2p.gameserver.model.pledge.Clan;
import l2p.gameserver.model.items.ItemInstance.ItemLocation;

public final class ClanWarehouse extends Warehouse
{
	public ClanWarehouse(Clan clan)
	{
		super(clan.getClanId());
	}

	@Override
	public ItemLocation getItemLocation()
	{
		return ItemLocation.CLANWH;
	}
}