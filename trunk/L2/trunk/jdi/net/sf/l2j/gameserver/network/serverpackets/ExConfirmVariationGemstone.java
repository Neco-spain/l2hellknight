package net.sf.l2j.gameserver.network.serverpackets;

public class ExConfirmVariationGemstone extends L2GameServerPacket
{
  private static final String _S__FE_54_EXCONFIRMVARIATIONGEMSTONE = "[S] FE:54 ExConfirmVariationGemstone";
  private int _gemstoneObjId;
  private int _unk1;
  private int _gemstoneCount;
  private int _unk2;
  private int _unk3;

  public ExConfirmVariationGemstone(int gemstoneObjId, int count)
  {
    _gemstoneObjId = gemstoneObjId;
    _unk1 = 1;
    _gemstoneCount = count;
    _unk2 = 1;
    _unk3 = 1;
  }

  protected void writeImpl()
  {
    writeC(254);
    writeH(84);
    writeD(_gemstoneObjId);
    writeD(_unk1);
    writeD(_gemstoneCount);
    writeD(_unk2);
    writeD(_unk3);
  }

  public String getType()
  {
    return "[S] FE:54 ExConfirmVariationGemstone";
  }
}