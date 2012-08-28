package l2p.gameserver.serverpackets;

import java.util.ArrayList;
import java.util.List;
import l2p.gameserver.data.xml.holder.BuyListHolder.NpcTradeList;
import l2p.gameserver.model.items.TradeItem;

public final class BuyListSeed extends L2GameServerPacket
{
  private int _manorId;
  private List<TradeItem> _list = new ArrayList();
  private long _money;

  public BuyListSeed(BuyListHolder.NpcTradeList list, int manorId, long currentMoney)
  {
    _money = currentMoney;
    _manorId = manorId;
    _list = list.getItems();
  }

  protected final void writeImpl()
  {
    writeC(233);

    writeQ(_money);
    writeD(_manorId);

    writeH(_list.size());

    for (TradeItem item : _list)
    {
      writeItemInfo(item);
      writeQ(item.getOwnersPrice());
    }
  }
}