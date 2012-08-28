package net.sf.l2j.gameserver.network.serverpackets;

import java.util.List;
import javolution.util.FastList;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2TradeList;

public final class BuyListSeed extends L2GameServerPacket
{
  private static final String _S__E8_BUYLISTSEED = "[S] E8 BuyListSeed";
  private int _manorId;
  private List<L2ItemInstance> _list = new FastList();
  private int _money;

  public BuyListSeed(L2TradeList list, int manorId, int currentMoney)
  {
    _money = currentMoney;
    _manorId = manorId;
    _list = list.getItems();
  }

  protected final void writeImpl()
  {
    writeC(232);

    writeD(_money);
    writeD(_manorId);

    writeH(_list.size());

    for (L2ItemInstance item : _list)
    {
      writeH(4);
      writeD(0);
      writeD(item.getItemId());
      writeD(item.getCount());
      writeH(4);
      writeH(0);
      writeD(item.getPriceToSell());
    }
  }

  public String getType()
  {
    return "[S] E8 BuyListSeed";
  }
}