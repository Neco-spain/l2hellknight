package l2m.gameserver.network.serverpackets;

public class ExCubeGameEnd extends L2GameServerPacket
{
  boolean _isRedTeamWin;

  public ExCubeGameEnd(boolean isRedTeamWin)
  {
    _isRedTeamWin = isRedTeamWin;
  }

  protected void writeImpl()
  {
    writeEx(152);
    writeD(1);

    writeD(_isRedTeamWin ? 1 : 0);
  }
}