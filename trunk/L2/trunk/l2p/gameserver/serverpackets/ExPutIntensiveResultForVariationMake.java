package l2p.gameserver.serverpackets;

public class ExPutIntensiveResultForVariationMake extends L2GameServerPacket
{
  private int _refinerItemObjId;
  private int _lifestoneItemId;
  private int _gemstoneItemId;
  private int _unk;
  private long _gemstoneCount;

  public ExPutIntensiveResultForVariationMake(int refinerItemObjId, int lifeStoneId, int gemstoneItemId, long gemstoneCount)
  {
    _refinerItemObjId = refinerItemObjId;
    _lifestoneItemId = lifeStoneId;
    _gemstoneItemId = gemstoneItemId;
    _gemstoneCount = gemstoneCount;
    _unk = 1;
  }

  protected void writeImpl()
  {
    writeEx(84);
    writeD(_refinerItemObjId);
    writeD(_lifestoneItemId);
    writeD(_gemstoneItemId);
    writeQ(_gemstoneCount);
    writeD(_unk);
  }
}