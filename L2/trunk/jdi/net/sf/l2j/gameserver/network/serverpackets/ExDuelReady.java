package net.sf.l2j.gameserver.network.serverpackets;

public class ExDuelReady extends L2GameServerPacket
{
  private static final String _S__FE_4C_EXDUELREADY = "[S] FE:4C ExDuelReady";
  private int _unk1;

  public ExDuelReady(int unk1)
  {
    _unk1 = unk1;
  }

  protected void writeImpl()
  {
    writeC(254);
    writeH(76);

    writeD(_unk1);
  }

  public String getType()
  {
    return "[S] FE:4C ExDuelReady";
  }
}