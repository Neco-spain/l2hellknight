package net.sf.l2j.gameserver.network.serverpackets;

public class GMHide extends L2GameServerPacket
{
  private int _mode;

  public GMHide(int mode)
  {
    _mode = mode;
  }

  protected final void writeImpl()
  {
    writeC(141);
    writeD(_mode);
  }
}