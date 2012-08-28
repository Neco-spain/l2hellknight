package l2p.gameserver.serverpackets;

import l2p.gameserver.model.items.ItemInfo;
import l2p.gameserver.templates.item.ItemTemplate;

public class TradeOtherAdd extends L2GameServerPacket
{
  private ItemInfo _temp;
  private long _amount;

  public TradeOtherAdd(ItemInfo item, long amount)
  {
    _temp = item;
    _amount = amount;
  }

  protected final void writeImpl()
  {
    writeC(27);
    writeH(1);
    writeH(_temp.getItem().getType1());
    writeD(_temp.getObjectId());
    writeD(_temp.getItemId());
    writeQ(_amount);
    writeH(_temp.getItem().getType2ForPackets());
    writeH(_temp.getCustomType1());
    writeD(_temp.getItem().getBodyPart());
    writeH(_temp.getEnchantLevel());
    writeH(0);
    writeH(_temp.getCustomType2());
    writeH(_temp.getAttackElement());
    writeH(_temp.getAttackElementValue());
    writeH(_temp.getDefenceFire());
    writeH(_temp.getDefenceWater());
    writeH(_temp.getDefenceWind());
    writeH(_temp.getDefenceEarth());
    writeH(_temp.getDefenceHoly());
    writeH(_temp.getDefenceUnholy());
    writeH(0);
    writeH(0);
    writeH(0);
  }
}