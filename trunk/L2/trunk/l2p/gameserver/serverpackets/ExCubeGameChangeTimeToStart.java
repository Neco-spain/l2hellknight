package l2p.gameserver.serverpackets;

public class ExCubeGameChangeTimeToStart extends L2GameServerPacket
{
  int _seconds;

  public ExCubeGameChangeTimeToStart(int seconds)
  {
    _seconds = seconds;
  }

  protected void writeImpl()
  {
    writeEx(151);
    writeD(3);

    writeD(_seconds);
  }
}