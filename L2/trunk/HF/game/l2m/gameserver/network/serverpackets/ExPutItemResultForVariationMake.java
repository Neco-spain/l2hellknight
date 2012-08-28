package l2m.gameserver.network.serverpackets;

public class ExPutItemResultForVariationMake extends L2GameServerPacket
{
  private int _itemObjId;
  private int _unk1;
  private int _unk2;

  public ExPutItemResultForVariationMake(int itemObjId)
  {
    _itemObjId = itemObjId;
    _unk1 = 1;
    _unk2 = 1;
  }

  protected void writeImpl()
  {
    writeEx(83);
    writeD(_itemObjId);
    writeD(_unk1);
    writeD(_unk2);
  }
}