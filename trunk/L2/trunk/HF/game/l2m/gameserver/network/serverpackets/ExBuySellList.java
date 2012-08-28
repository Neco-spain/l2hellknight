package l2m.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import l2m.gameserver.aConfig;
import l2m.gameserver.data.xml.holder.BuyListHolder.NpcTradeList;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.items.ItemContainer;
import l2m.gameserver.model.items.ItemInstance;
import l2m.gameserver.model.items.PcInventory;
import l2m.gameserver.model.items.TradeItem;

public abstract class ExBuySellList extends L2GameServerPacket
{
  private int _type;

  public ExBuySellList(int type)
  {
    _type = type;
  }

  protected void writeImpl()
  {
    writeEx(183);
    writeD(_type);
  }

  public static class SellRefundList extends ExBuySellList
  {
    private final List<TradeItem> _sellList;
    private final List<TradeItem> _refundList;
    private int _done;

    public SellRefundList(Player activeChar, boolean done)
    {
      super();
      _done = (done ? 1 : 0);
      if (done)
      {
        _refundList = Collections.emptyList();
        _sellList = Collections.emptyList();
      }
      else
      {
        ItemInstance[] items = activeChar.getRefund().getItems();
        _refundList = new ArrayList(items.length);
        for (ItemInstance item : items) {
          _refundList.add(new TradeItem(item));
        }
        items = activeChar.getInventory().getItems();
        _sellList = new ArrayList(items.length);
        for (ItemInstance item : items)
          if (item.canBeSold(activeChar))
            _sellList.add(new TradeItem(item));
      }
    }

    protected void writeImpl()
    {
      super.writeImpl();
      writeH(_sellList.size());
      for (TradeItem item : _sellList)
      {
        writeItemInfo(item);
        writeQ(aConfig.get("SellOneAdena", false) ? 1L : item.getReferencePrice() / 2L);
      }
      writeH(_refundList.size());
      for (TradeItem item : _refundList)
      {
        writeItemInfo(item);
        writeD(item.getObjectId());
        writeQ(item.getCount() * item.getReferencePrice() / 2L);
      }
      writeC(_done);
    }
  }

  public static class BuyList extends ExBuySellList
  {
    private final int _listId;
    private final List<TradeItem> _buyList;
    private final long _adena;
    private final double _taxRate;

    public BuyList(BuyListHolder.NpcTradeList tradeList, Player activeChar, double taxRate)
    {
      super();
      _adena = activeChar.getAdena();
      _taxRate = taxRate;

      if (tradeList != null)
      {
        _listId = tradeList.getListId();
        _buyList = tradeList.getItems();
        activeChar.setBuyListId(_listId);
      }
      else
      {
        _listId = 0;
        _buyList = Collections.emptyList();
        activeChar.setBuyListId(0);
      }
    }

    protected void writeImpl()
    {
      super.writeImpl();
      writeQ(_adena);
      writeD(_listId);
      writeH(_buyList.size());
      for (TradeItem item : _buyList)
      {
        writeItemInfo(item, item.getCurrentValue());
        writeQ(()(item.getOwnersPrice() * (1.0D + _taxRate)));
      }
    }
  }
}