package l2m.gameserver.network.serverpackets;

public class ExRegenMax extends L2GameServerPacket
{
  private double _max;
  private int _count;
  private int _time;
  public static final int POTION_HEALING_GREATER = 16457;
  public static final int POTION_HEALING_MEDIUM = 16440;
  public static final int POTION_HEALING_LESSER = 16416;

  public ExRegenMax(double max, int count, int time)
  {
    _max = (max * 0.66D);
    _count = count;
    _time = time;
  }

  protected void writeImpl()
  {
    writeEx(1);
    writeD(1);
    writeD(_count);
    writeD(_time);
    writeF(_max);
  }
}