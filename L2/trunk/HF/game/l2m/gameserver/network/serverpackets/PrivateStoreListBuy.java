package l2m.gameserver.serverpackets;

import java.util.ArrayList;
import java.util.List;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.items.ItemInstance;
import l2m.gameserver.model.items.PcInventory;
import l2m.gameserver.model.items.TradeItem;

public class PrivateStoreListBuy extends L2GameServerPacket
{
  private int _buyerId;
  private long _adena;
  private List<TradeItem> _sellList;

  public PrivateStoreListBuy(Player seller, Player buyer)
  {
    _adena = seller.getAdena();
    _buyerId = buyer.getObjectId();
    _sellList = new ArrayList();

    List buyList = buyer.getBuyList();
    ItemInstance[] items = seller.getInventory().getItems();

    for (TradeItem bi : buyList)
    {
      TradeItem si = null;
      for (ItemInstance item : items) {
        if ((item.getItemId() != bi.getItemId()) || (!item.canBeTraded(seller)))
          continue;
        si = new TradeItem(item);
        _sellList.add(si);
        si.setOwnersPrice(bi.getOwnersPrice());
        si.setCount(bi.getCount());
        si.setCurrentValue(Math.min(bi.getCount(), item.getCount()));
      }
      if (si == null)
      {
        si = new TradeItem();
        si.setItemId(bi.getItemId());
        si.setOwnersPrice(bi.getOwnersPrice());
        si.setCount(bi.getCount());
        si.setCurrentValue(0L);
        _sellList.add(si);
      }
    }
  }

  protected final void writeImpl()
  {
    writeC(190);

    writeD(_buyerId);
    writeQ(_adena);
    writeD(_sellList.size());
    for (TradeItem si : _sellList)
    {
      writeItemInfo(si, si.getCurrentValue());
      writeD(si.getObjectId());
      writeQ(si.getOwnersPrice());
      writeQ(si.getStorePrice());
      writeQ(si.getCount());
    }
  }
}