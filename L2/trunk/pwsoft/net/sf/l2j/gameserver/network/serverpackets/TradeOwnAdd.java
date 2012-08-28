package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.TradeList.TradeItem;
import net.sf.l2j.gameserver.templates.L2Item;

public class TradeOwnAdd extends L2GameServerPacket
{
  private TradeList.TradeItem _item;

  public TradeOwnAdd(TradeList.TradeItem item)
  {
    _item = item;
  }

  protected final void writeImpl()
  {
    writeC(32);

    writeH(1);

    writeH(_item.getItem().getType1());
    writeD(_item.getObjectId());
    writeD(_item.getItem().getItemId());
    writeD(_item.getCount());
    writeH(_item.getItem().getType2());
    writeH(0);

    writeD(_item.getItem().getBodyPart());
    writeH(_item.getEnchant());
    writeH(0);
    writeH(0);
  }
}