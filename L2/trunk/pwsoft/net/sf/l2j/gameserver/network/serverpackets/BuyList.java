package net.sf.l2j.gameserver.network.serverpackets;

import java.util.List;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2TradeList;
import net.sf.l2j.gameserver.templates.L2Item;

public final class BuyList extends L2GameServerPacket
{
  private int _listId;
  private L2ItemInstance[] _list;
  private int _money;
  private double _taxRate = 0.0D;

  public BuyList(L2TradeList list, int currentMoney)
  {
    _listId = list.getListId();
    List lst = list.getItems();
    _list = ((L2ItemInstance[])lst.toArray(new L2ItemInstance[lst.size()]));
    _money = currentMoney;
  }

  public BuyList(L2TradeList list, int currentMoney, double taxRate)
  {
    _listId = list.getListId();
    List lst = list.getItems();
    _list = ((L2ItemInstance[])lst.toArray(new L2ItemInstance[lst.size()]));
    _money = currentMoney;
    _taxRate = taxRate;
  }

  public BuyList(List<L2ItemInstance> lst, int listId, int currentMoney)
  {
    _listId = listId;
    _list = ((L2ItemInstance[])lst.toArray(new L2ItemInstance[lst.size()]));
    _money = currentMoney;
  }

  protected final void writeImpl()
  {
    writeC(17);
    writeD(_money);
    writeD(_listId);

    writeH(_list.length);

    for (L2ItemInstance item : _list)
    {
      if ((item.getCount() > 0) || (item.getCount() == -1)) {
        writeH(item.getItem().getType1());
        writeD(item.getObjectId());
        writeD(item.getItemId());
        if (item.getCount() < 0)
          writeD(0);
        else
          writeD(item.getCount());
        writeH(item.getItem().getType2());
        writeH(0);

        if (item.getItem().getType1() != 4)
        {
          writeD(item.getItem().getBodyPart());
          writeH(item.getEnchantLevel());
          writeH(0);
          writeH(0);
        }
        else
        {
          writeD(0);
          writeH(0);
          writeH(0);
          writeH(0);
        }

        if ((item.getItemId() >= 3960) && (item.getItemId() <= 4026))
          writeD((int)(item.getPriceToSell() * Config.RATE_SIEGE_GUARDS_PRICE * (1.0D + _taxRate)));
        else
          writeD((int)(item.getPriceToSell() * (1.0D + _taxRate)));
      }
    }
  }
}