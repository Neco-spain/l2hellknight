package net.sf.l2j.gameserver.network.serverpackets;

public class ExVariationResult extends L2GameServerPacket
{
  private static final String _S__FE_55_EXVARIATIONRESULT = "[S] FE:55 ExVariationResult";
  private int _stat12;
  private int _stat34;
  private int _unk3;

  public ExVariationResult(int unk1, int unk2, int unk3)
  {
    _stat12 = unk1;
    _stat34 = unk2;
    _unk3 = unk3;
  }

  protected void writeImpl()
  {
    writeC(254);
    writeH(85);
    writeD(_stat12);
    writeD(_stat34);
    writeD(_unk3);
  }

  public String getType()
  {
    return "[S] FE:55 ExVariationResult";
  }
}