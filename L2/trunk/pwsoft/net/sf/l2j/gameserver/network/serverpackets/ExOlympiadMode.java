package net.sf.l2j.gameserver.network.serverpackets;

public class ExOlympiadMode extends L2GameServerPacket
{
  private int _mode;

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
}