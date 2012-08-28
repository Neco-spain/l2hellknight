package net.sf.l2j.gameserver.network.serverpackets;

public class ExDuelStart extends L2GameServerPacket
{
  private static final String _S__FE_4D_EXDUELSTART = "[S] FE:4D ExDuelStart";
  private int _unk1;

  public ExDuelStart(int unk1)
  {
    _unk1 = unk1;
  }

  protected void writeImpl()
  {
    writeC(254);
    writeH(77);

    writeD(_unk1);
  }

  public String getType()
  {
    return "[S] FE:4D ExDuelStart";
  }
}