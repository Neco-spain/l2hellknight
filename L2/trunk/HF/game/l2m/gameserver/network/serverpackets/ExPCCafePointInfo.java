package l2m.gameserver.network.serverpackets;

import l2m.gameserver.model.Player;

public class ExPCCafePointInfo extends L2GameServerPacket
{
  private int _mAddPoint;
  private int _mPeriodType;
  private int _pointType;
  private int _pcBangPoints;
  private int _remainTime;

  public ExPCCafePointInfo(Player player, int mAddPoint, int mPeriodType, int pointType, int remainTime)
  {
    _pcBangPoints = player.getPcBangPoints();
    _mAddPoint = mAddPoint;
    _mPeriodType = mPeriodType;
    _pointType = pointType;
    _remainTime = remainTime;
  }

  protected final void writeImpl()
  {
    writeEx(50);
    writeD(_pcBangPoints);
    writeD(_mAddPoint);
    writeC(_mPeriodType);
    writeD(_remainTime);
    writeC(_pointType);
  }
}