package net.sf.l2j.gameserver.network.serverpackets;

public class ExDuelEnd extends L2GameServerPacket
{
  private static final String _S__FE_4E_EXDUELEND = "[S] FE:4E ExDuelEnd";
  private int _unk1;

  public ExDuelEnd(int unk1)
  {
    _unk1 = unk1;
  }

  protected void writeImpl()
  {
    writeC(254);
    writeH(78);

    writeD(_unk1);
  }

  public String getType()
  {
    return "[S] FE:4E ExDuelEnd";
  }
}