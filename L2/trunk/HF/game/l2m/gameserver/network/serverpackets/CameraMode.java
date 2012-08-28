package l2m.gameserver.network.serverpackets;

public class CameraMode extends L2GameServerPacket
{
  int _mode;

  public CameraMode(int mode)
  {
    _mode = mode;
  }

  protected final void writeImpl()
  {
    writeC(247);
    writeD(_mode);
  }
}