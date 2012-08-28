package l2p.gameserver.serverpackets;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.model.items.TradeItem;

public class PrivateStoreManageListSell extends L2GameServerPacket
{
  private int _sellerId;
  private long _adena;
  private boolean _package;
  private List<TradeItem> _sellList;
  private List<TradeItem> _sellList0;

  public PrivateStoreManageListSell(Player seller, boolean pkg)
  {
    _sellerId = seller.getObjectId();
    _adena = seller.getAdena();
    _package = pkg;
    _sellList0 = seller.getSellList(_package);
    _sellList = new ArrayList();

    for (TradeItem si : _sellList0)
    {
      if (si.getCount() <= 0L)
      {
        _sellList0.remove(si);
        continue;
      }

      ItemInstance item = seller.getInventory().getItemByObjectId(si.getObjectId());
      if (item == null)
      {
        item = seller.getInventory().getItemByItemId(si.getItemId());
      }
      if ((item == null) || (!item.canBeTraded(seller)) || (item.getItemId() == 57))
      {
        _sellList0.remove(si);
        continue;
      }

      si.setCount(Math.min(item.getCount(), si.getCount()));
    }

    ItemInstance[] items = seller.getInventory().getItems();

    for (ItemInstance item : items) {
      if ((!item.canBeTraded(seller)) || (item.getItemId() == 57))
        continue;
      Iterator i$ = _sellList0.iterator();
      while (true) if (i$.hasNext()) { TradeItem si = (TradeItem)i$.next();
          if (si.getObjectId() == item.getObjectId())
          {
            if (si.getCount() == item.getCount()) {
              break;
            }
            TradeItem ti = new TradeItem(item);
            ti.setCount(item.getCount() - si.getCount());
            _sellList.add(ti); } else {
            continue;
          } } else {
          _sellList.add(new TradeItem(item));
        }
    }
  }

  protected final void writeImpl()
  {
    writeC(160);

    writeD(_sellerId);
    writeD(_package ? 1 : 0);
    writeQ(_adena);

    writeD(_sellList.size());
    for (TradeItem si : _sellList)
    {
      writeItemInfo(si);
      writeQ(si.getStorePrice());
    }

    writeD(_sellList0.size());
    for (TradeItem si : _sellList0)
    {
      writeItemInfo(si);
      writeQ(si.getOwnersPrice());
      writeQ(si.getStorePrice());
    }
  }
}