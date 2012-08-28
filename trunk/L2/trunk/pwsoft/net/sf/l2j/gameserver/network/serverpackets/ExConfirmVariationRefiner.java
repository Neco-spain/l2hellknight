package net.sf.l2j.gameserver.network.serverpackets;

public class ExConfirmVariationRefiner extends L2GameServerPacket
{
  private int _refinerItemObjId;
  private int _lifestoneItemId;
  private int _gemstoneItemId;
  private int _gemstoneCount;
  private int _unk2;

  public ExConfirmVariationRefiner(int refinerItemObjId, int lifeStoneId, int gemstoneItemId, int gemstoneCount)
  {
    _refinerItemObjId = refinerItemObjId;
    _lifestoneItemId = lifeStoneId;
    _gemstoneItemId = gemstoneItemId;
    _gemstoneCount = gemstoneCount;
    _unk2 = 1;
  }

  protected void writeImpl()
  {
    writeC(254);
    writeH(83);
    writeD(_refinerItemObjId);
    writeD(_lifestoneItemId);
    writeD(_gemstoneItemId);
    writeD(_gemstoneCount);
    writeD(_unk2);
  }
}