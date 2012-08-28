package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.TradeList;
import net.sf.l2j.gameserver.model.TradeList.TradeItem;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.templates.L2Item;

public class TradeUpdate extends L2GameServerPacket
{
  private static final String _S__74_TRADEUPDATE = "[S] 74 TradeUpdate";
  private final L2ItemInstance[] _items;
  private final TradeList.TradeItem[] _trade_items;

  public TradeUpdate(TradeList trade, L2PcInstance activeChar)
  {
    _items = activeChar.getInventory().getItems();
    _trade_items = trade.getItems();
  }

  private int getItemCount(int objectId)
  {
    for (L2ItemInstance item : _items)
      if (item.getObjectId() == objectId)
        return item.getCount();
    return 0;
  }

  public String getType()
  {
    return "[S] 74 TradeUpdate";
  }

  protected final void writeImpl()
  {
    writeC(116);

    writeH(_trade_items.length);
    for (TradeList.TradeItem _item : _trade_items)
    {
      int _aveable_count = getItemCount(_item.getObjectId()) - _item.getCount();

      boolean _stackable = _item.getItem().isStackable();
      if (_aveable_count == 0)
      {
        _aveable_count = 1;
        _stackable = false;
      }
      writeH(_stackable ? 3 : 2);
      writeH(_item.getItem().getType1());
      writeD(_item.getObjectId());
      writeD(_item.getItem().getItemId());
      writeD(_aveable_count);
      writeH(_item.getItem().getType2());
      writeH(0);
      writeD(_item.getItem().getBodyPart());
      writeH(_item.getEnchant());
      writeH(0);
      writeH(0);
    }
  }
}