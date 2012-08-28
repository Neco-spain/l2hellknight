package l2m.gameserver.network.serverpackets;

public class ExPutCommissionResultForVariationMake extends L2GameServerPacket
{
  private int _gemstoneObjId;
  private int _unk1;
  private int _unk3;
  private long _gemstoneCount;
  private long _unk2;

  public ExPutCommissionResultForVariationMake(int gemstoneObjId, long count)
  {
    _gemstoneObjId = gemstoneObjId;
    _unk1 = 1;
    _gemstoneCount = count;
    _unk2 = 1L;
    _unk3 = 1;
  }

  protected void writeImpl()
  {
    writeEx(85);
    writeD(_gemstoneObjId);
    writeD(_unk1);
    writeQ(_gemstoneCount);
    writeQ(_unk2);
    writeD(_unk3);
  }
}