package l2m.gameserver.serverpackets;

import l2m.gameserver.model.Creature;

public class StartRotating extends L2GameServerPacket
{
  private int _charId;
  private int _degree;
  private int _side;
  private int _speed;

  public StartRotating(Creature cha, int degree, int side, int speed)
  {
    _charId = cha.getObjectId();
    _degree = degree;
    _side = side;
    _speed = speed;
  }

  protected final void writeImpl()
  {
    writeC(122);
    writeD(_charId);
    writeD(_degree);
    writeD(_side);
    writeD(_speed);
  }
}