package l2p.gameserver.serverpackets;

import java.util.ArrayList;
import java.util.List;
import l2p.commons.lang.ArrayUtils;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.items.ItemInfo;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.model.items.Warehouse.ItemClassComparator;
import l2p.gameserver.model.items.Warehouse.WarehouseType;

public class WareHouseDepositList extends L2GameServerPacket
{
  private int _whtype;
  private long _adena;
  private List<ItemInfo> _itemList;

  public WareHouseDepositList(Player cha, Warehouse.WarehouseType whtype)
  {
    _whtype = whtype.ordinal();
    _adena = cha.getAdena();

    ItemInstance[] items = cha.getInventory().getItems();
    ArrayUtils.eqSort(items, Warehouse.ItemClassComparator.getInstance());
    _itemList = new ArrayList(items.length);
    for (ItemInstance item : items)
      if (item.canBeStored(cha, _whtype == 1))
        _itemList.add(new ItemInfo(item));
  }

  protected final void writeImpl()
  {
    writeC(65);
    writeH(_whtype);
    writeQ(_adena);
    writeH(_itemList.size());
    for (ItemInfo item : _itemList)
    {
      writeItemInfo(item);
      writeD(item.getObjectId());
    }
  }
}