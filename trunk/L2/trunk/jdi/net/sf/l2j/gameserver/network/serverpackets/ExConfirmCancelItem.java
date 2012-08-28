package net.sf.l2j.gameserver.network.serverpackets;

public class ExConfirmCancelItem extends L2GameServerPacket
{
  private static final String _S__FE_56_EXCONFIRMCANCELITEM = "[S] FE:56 ExConfirmCancelItem";
  private int _itemObjId;
  private int _price;

  public ExConfirmCancelItem(int itemObjId, int price)
  {
    _itemObjId = itemObjId;
    _price = price;
  }

  protected void writeImpl()
  {
    writeC(254);
    writeH(86);
    writeD(1084847890);
    writeD(_itemObjId);
    writeD(39);
    writeD(8198);
    writeQ(_price);
    writeD(1);
  }

  public String getType()
  {
    return "[S] FE:56 ExConfirmCancelItem";
  }
}