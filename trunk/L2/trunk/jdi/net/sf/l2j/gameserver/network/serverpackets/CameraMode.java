package net.sf.l2j.gameserver.network.serverpackets;

public class CameraMode extends L2GameServerPacket
{
  private static final String _S__F1_CAMERAMODE = "[S] F1 CameraMode";
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

  public String getType()
  {
    return "[S] F1 CameraMode";
  }
}