package l2rt.gameserver.network.serverpackets;

import l2rt.Config;
import l2rt.gameserver.model.L2Player;

public class ExStorageMaxCount extends L2GameServerPacket
{
	private int _inventory;
	private int _warehouse;
	private int _clan;
	private int _privateSell;
	private int _privateBuy;
	private int _recipeDwarven;
	private int _recipeCommon;
	private int _inventoryExtraSlots;

	public ExStorageMaxCount(L2Player player)
	{
		_inventory = player.getInventoryLimit();
		_warehouse = player.getWarehouseLimit();
		_clan = Config.WAREHOUSE_SLOTS_CLAN;
		_privateBuy = _privateSell = player.getTradeLimit();
		_recipeDwarven = player.getDwarvenRecipeLimit();
		_recipeCommon = player.getCommonRecipeLimit();
		_inventoryExtraSlots = 0; //TODO
	}

	@Override
	protected final void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x2f);

		writeD(_inventory);
		writeD(_warehouse);
		writeD(_clan);
		writeD(_privateSell);
		writeD(_privateBuy);
		writeD(_recipeDwarven);
		writeD(_recipeCommon);
		writeD(_inventoryExtraSlots); // belt inventory slots increase count
        writeD(100);// Quest Inventory Limit
	}
}