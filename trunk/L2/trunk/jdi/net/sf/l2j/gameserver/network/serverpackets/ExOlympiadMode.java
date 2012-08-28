package net.sf.l2j.gameserver.network.serverpackets;

public class ExOlympiadMode extends L2GameServerPacket
{
  private static final String _S__FE_2B_OLYMPIADMODE = "[S] FE:2B ExOlympiadMode";
  private static int _mode;

  public ExOlympiadMode(int mode)
  {
    _mode = mode;
  }

  protected final void writeImpl()
  {
    writeC(254);
    writeH(43);
    writeC(_mode);
  }

  public String getType()
  {
    return "[S] FE:2B ExOlympiadMode";
  }
}