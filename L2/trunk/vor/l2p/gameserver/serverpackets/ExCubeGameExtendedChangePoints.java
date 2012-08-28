package l2p.gameserver.serverpackets;

import l2p.gameserver.model.Player;

public class ExCubeGameExtendedChangePoints extends L2GameServerPacket
{
  private int _timeLeft;
  private int _bluePoints;
  private int _redPoints;
  private boolean _isRedTeam;
  private int _objectId;
  private int _playerPoints;

  public ExCubeGameExtendedChangePoints(int timeLeft, int bluePoints, int redPoints, boolean isRedTeam, Player player, int playerPoints)
  {
    _timeLeft = timeLeft;
    _bluePoints = bluePoints;
    _redPoints = redPoints;
    _isRedTeam = isRedTeam;
    _objectId = player.getObjectId();
    _playerPoints = playerPoints;
  }

  protected void writeImpl()
  {
    writeEx(152);
    writeD(0);

    writeD(_timeLeft);
    writeD(_bluePoints);
    writeD(_redPoints);

    writeD(_isRedTeam ? 1 : 0);
    writeD(_objectId);
    writeD(_playerPoints);
  }
}