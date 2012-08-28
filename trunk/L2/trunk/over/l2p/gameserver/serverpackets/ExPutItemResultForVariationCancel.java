package l2p.gameserver.serverpackets;

import l2p.gameserver.clientpackets.RequestRefineCancel;
import l2p.gameserver.model.items.ItemInstance;

public class ExPutItemResultForVariationCancel extends L2GameServerPacket
{
  private int _itemObjectId;
  private int _itemId;
  private int _aug1;
  private int _aug2;
  private long _price;

  public ExPutItemResultForVariationCancel(ItemInstance item)
  {
    _itemObjectId = item.getObjectId();
    _itemId = item.getItemId();
    _aug1 = (0xFFFF & item.getAugmentationId());
    _aug2 = (item.getAugmentationId() >> 16);
    _price = RequestRefineCancel.getRemovalPrice(item.getTemplate());
  }

  protected void writeImpl()
  {
    writeEx(87);
    writeD(_itemObjectId);
    writeD(_itemId);
    writeD(_aug1);
    writeD(_aug2);
    writeQ(_price);
    writeD(1);
  }
}