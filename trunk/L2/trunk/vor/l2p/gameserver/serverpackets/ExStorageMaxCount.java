package l2p.gameserver.serverpackets;

import l2p.gameserver.Config;
import l2p.gameserver.model.Player;

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
  private int _questItemsLimit;

  public ExStorageMaxCount(Player player)
  {
    _inventory = player.getInventoryLimit();
    _warehouse = player.getWarehouseLimit();
    _clan = Config.WAREHOUSE_SLOTS_CLAN;
    _privateBuy = (this._privateSell = player.getTradeLimit());
    _recipeDwarven = player.getDwarvenRecipeLimit();
    _recipeCommon = player.getCommonRecipeLimit();
    _inventoryExtraSlots = player.getBeltInventoryIncrease();
    _questItemsLimit = Config.QUEST_INVENTORY_MAXIMUM;
  }

  protected final void writeImpl()
  {
    writeEx(47);

    writeD(_inventory);
    writeD(_warehouse);
    writeD(_clan);
    writeD(_privateSell);
    writeD(_privateBuy);
    writeD(_recipeDwarven);
    writeD(_recipeCommon);
    writeD(_inventoryExtraSlots);
    writeD(_questItemsLimit);
  }
}