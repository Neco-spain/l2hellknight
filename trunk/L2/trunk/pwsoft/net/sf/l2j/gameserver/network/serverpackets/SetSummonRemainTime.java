package net.sf.l2j.gameserver.network.serverpackets;

public class SetSummonRemainTime extends L2GameServerPacket
{
  private int _maxTime;
  private int _remainingTime;

  public SetSummonRemainTime(int maxTime, int remainingTime)
  {
    _remainingTime = remainingTime;
    _maxTime = maxTime;
  }

  protected final void writeImpl()
  {
    writeC(209);
    writeD(_maxTime);
    writeD(_remainingTime);
  }

  public String getType()
  {
    return "S.SetSummonRemainTime";
  }
}