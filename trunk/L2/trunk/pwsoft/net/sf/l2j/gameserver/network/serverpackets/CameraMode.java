package net.sf.l2j.gameserver.network.serverpackets;

public class CameraMode extends L2GameServerPacket
{
  private int _mode;

  public CameraMode(int mode)
  {
    _mode = mode;
  }

  public void writeImpl()
  {
    writeC(241);
    writeD(_mode);
  }
}