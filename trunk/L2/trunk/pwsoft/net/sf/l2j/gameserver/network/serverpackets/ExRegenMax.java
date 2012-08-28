package net.sf.l2j.gameserver.network.serverpackets;

public class ExRegenMax extends L2GameServerPacket
{
  private double _max;
  private int _count;
  private int _time;

  public ExRegenMax(double max, int count, int time)
  {
    _max = max;
    _count = count;
    _time = time;
  }

  protected void writeImpl()
  {
    writeC(254);
    writeH(1);
    writeD(1);
    writeD(_count);
    writeD(_time);
    writeF(_max);
  }
}