package l2m.gameserver.serverpackets;

import java.util.ArrayList;
import java.util.List;
import l2p.commons.lang.ArrayUtils;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.items.ItemInfo;
import l2m.gameserver.model.items.ItemInstance;
import l2m.gameserver.model.items.PcInventory;
import l2m.gameserver.model.items.Warehouse.ItemClassComparator;
import l2m.gameserver.templates.item.ItemTemplate;

public class PackageSendableList extends L2GameServerPacket
{
  private int _targetObjectId;
  private long _adena;
  private List<ItemInfo> _itemList;

  public PackageSendableList(int objectId, Player cha)
  {
    _adena = cha.getAdena();
    _targetObjectId = objectId;

    ItemInstance[] items = cha.getInventory().getItems();
    ArrayUtils.eqSort(items, Warehouse.ItemClassComparator.getInstance());
    _itemList = new ArrayList(items.length);
    for (ItemInstance item : items)
      if (item.getTemplate().isFreightable())
        _itemList.add(new ItemInfo(item));
  }

  protected final void writeImpl()
  {
    writeC(210);
    writeD(_targetObjectId);
    writeQ(_adena);
    writeD(_itemList.size());
    for (ItemInfo item : _itemList)
    {
      writeItemInfo(item);
      writeD(item.getObjectId());
    }
  }
}