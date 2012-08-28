package l2p.gameserver.serverpackets;

import l2p.gameserver.model.Player;

public class ExVoteSystemInfo extends L2GameServerPacket
{
  private int _receivedRec;
  private int _givingRec;
  private int _time;
  private int _bonusPercent;
  private boolean _showTimer;

  public ExVoteSystemInfo(Player player)
  {
    _receivedRec = player.getRecomLeft();
    _givingRec = player.getRecomHave();
    _time = player.getRecomBonusTime();
    _bonusPercent = player.getRecomBonus();
    _showTimer = ((!player.isRecomTimerActive()) || (player.isHourglassEffected()));
  }

  protected void writeImpl()
  {
    writeEx(201);
    writeD(_receivedRec);
    writeD(_givingRec);
    writeD(_time);
    writeD(_bonusPercent);
    writeD(_showTimer ? 1 : 0);
  }
}