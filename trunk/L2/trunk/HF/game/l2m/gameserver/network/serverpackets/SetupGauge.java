package l2m.gameserver.serverpackets;

import l2m.gameserver.model.Creature;

public class SetupGauge extends L2GameServerPacket
{
  public static final int BLUE = 0;
  public static final int RED = 1;
  public static final int CYAN = 2;
  private int _charId;
  private int _dat1;
  private int _time;

  public SetupGauge(Creature character, int dat1, int time)
  {
    _charId = character.getObjectId();
    _dat1 = dat1;
    _time = time;
  }

  protected final void writeImpl()
  {
    writeC(107);
    writeD(_charId);
    writeD(_dat1);
    writeD(_time);

    writeD(_time);
  }
}