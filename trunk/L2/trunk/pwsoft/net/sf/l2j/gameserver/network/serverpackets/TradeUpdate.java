package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.templates.L2Item;

public class TradeUpdate extends L2GameServerPacket
{
  private L2ItemInstance temp;
  private int _amount;

  public TradeUpdate(L2ItemInstance x)
  {
    temp = x;
    _amount = x.getCount();
  }

  protected final void writeImpl()
  {
    writeC(116);

    writeH(1);
    boolean stackable = temp.isStackable();

    if (_amount == 0) {
      _amount = 1;
      stackable = false;
    }

    writeH(stackable ? 3 : 2);

    int type = temp.getItem().getType1();
    writeH(type);
    writeD(temp.getObjectId());
    writeD(temp.getItem().getItemId());
    writeD(_amount);
    writeH(temp.getItem().getType2());
    writeH(0);

    writeD(temp.getItem().getBodyPart());
    writeH(temp.getEnchantLevel());
    writeH(0);
    writeH(0);
  }
}