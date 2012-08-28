package l2p.gameserver.serverpackets;

public class ExCubeGameChangePoints extends L2GameServerPacket
{
  int _timeLeft;
  int _bluePoints;
  int _redPoints;

  public ExCubeGameChangePoints(int timeLeft, int bluePoints, int redPoints)
  {
    _timeLeft = timeLeft;
    _bluePoints = bluePoints;
    _redPoints = redPoints;
  }

  protected void writeImpl()
  {
    writeEx(152);
    writeD(2);

    writeD(_timeLeft);
    writeD(_bluePoints);
    writeD(_redPoints);
  }
}