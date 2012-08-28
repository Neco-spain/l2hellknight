package l2m.gameserver.serverpackets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import l2p.commons.lang.ArrayUtils;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.items.ClanWarehouse;
import l2m.gameserver.model.items.ItemInfo;
import l2m.gameserver.model.items.ItemInstance;
import l2m.gameserver.model.items.PcFreight;
import l2m.gameserver.model.items.Warehouse;
import l2m.gameserver.model.items.Warehouse.ItemClassComparator;
import l2m.gameserver.model.items.Warehouse.WarehouseType;
import l2m.gameserver.model.pledge.Clan;
import l2m.gameserver.templates.item.ItemTemplate.ItemClass;

public class WareHouseWithdrawList extends L2GameServerPacket
{
  private long _adena;
  private List<ItemInfo> _itemList = new ArrayList();
  private int _type;

  public WareHouseWithdrawList(Player player, Warehouse.WarehouseType type, ItemTemplate.ItemClass clss)
  {
    _adena = player.getAdena();
    _type = type.ordinal();
    ItemInstance[] items;
    switch (1.$SwitchMap$l2p$gameserver$model$items$Warehouse$WarehouseType[type.ordinal()])
    {
    case 1:
      items = player.getWarehouse().getItems(clss);
      break;
    case 2:
      items = player.getFreight().getItems(clss);
      break;
    case 3:
    case 4:
      items = player.getClan().getWarehouse().getItems(clss);
      break;
    default:
      _itemList = Collections.emptyList();
      return;
    }

    _itemList = new ArrayList(items.length);
    ArrayUtils.eqSort(items, Warehouse.ItemClassComparator.getInstance());
    for (ItemInstance item : items)
      _itemList.add(new ItemInfo(item));
  }

  protected final void writeImpl()
  {
    writeC(66);
    writeH(_type);
    writeQ(_adena);
    writeH(_itemList.size());
    for (ItemInfo item : _itemList)
    {
      writeItemInfo(item);
      writeD(item.getObjectId());
    }
  }
}