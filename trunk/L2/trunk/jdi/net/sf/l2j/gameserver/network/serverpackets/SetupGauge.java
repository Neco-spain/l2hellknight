package net.sf.l2j.gameserver.network.serverpackets;

public class SetupGauge extends L2GameServerPacket
{
  private static final String _S__85_SETUPGAUGE = "[S] 6d SetupGauge";
  public static final int BLUE = 0;
  public static final int RED = 1;
  public static final int CYAN = 2;
  private int _dat1;
  private int _time;

  public SetupGauge(int dat1, int time)
  {
    _dat1 = dat1;
    _time = time;
  }

  protected final void writeImpl()
  {
    writeC(109);
    writeD(_dat1);
    writeD(_time);

    writeD(_time);
  }

  public String getType()
  {
    return "[S] 6d SetupGauge";
  }
}