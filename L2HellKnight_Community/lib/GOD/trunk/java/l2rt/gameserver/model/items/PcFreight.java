package l2rt.gameserver.model.items;

import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.items.L2ItemInstance.ItemLocation;

public class PcFreight extends Warehouse
{
	private final long ownerStoreId;
	private final int ownerObjectId;

	public PcFreight(L2Player owner)
	{
		ownerStoreId = owner.getStoredId();
		ownerObjectId = owner.getObjectId();
	}

	public PcFreight(int id)
	{
		ownerStoreId = 0;
		ownerObjectId = id;
	}

	public L2Player getOwner()
	{
		return L2ObjectsStorage.getAsPlayer(ownerStoreId);
	}

	@Override
	public int getOwnerId()
	{
		return ownerObjectId;
	}

	@Override
	public ItemLocation getLocationType()
	{
		return ItemLocation.FREIGHT;
	}
}