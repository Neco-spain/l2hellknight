package l2p.gameserver.serverpackets;

import java.util.ArrayList;
import java.util.List;
import l2p.commons.lang.ArrayUtils;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.model.items.TradeItem;
import l2p.gameserver.model.items.Warehouse.ItemClassComparator;

public class PrivateStoreManageListBuy extends L2GameServerPacket
{
  private int _buyerId;
  private long _adena;
  private List<TradeItem> _buyList0;
  private List<TradeItem> _buyList;

  public PrivateStoreManageListBuy(Player buyer)
  {
    _buyerId = buyer.getObjectId();
    _adena = buyer.getAdena();
    _buyList0 = buyer.getBuyList();
    _buyList = new ArrayList();

    ItemInstance[] items = buyer.getInventory().getItems();
    ArrayUtils.eqSort(items, Warehouse.ItemClassComparator.getInstance());

    for (ItemInstance item : items) {
      if ((!item.canBeTraded(buyer)) || (item.getItemId() == 57))
        continue;
      TradeItem bi;
      _buyList.add(bi = new TradeItem(item));
      bi.setObjectId(0);
    }
  }

  protected final void writeImpl()
  {
    writeC(189);

    writeD(_buyerId);
    writeQ(_adena);

    writeD(_buyList.size());
    for (TradeItem bi : _buyList)
    {
      writeItemInfo(bi);
      writeQ(bi.getStorePrice());
    }

    writeD(_buyList0.size());
    for (TradeItem bi : _buyList0)
    {
      writeItemInfo(bi);
      writeQ(bi.getOwnersPrice());
      writeQ(bi.getStorePrice());
      writeQ(bi.getCount());
    }
  }
}