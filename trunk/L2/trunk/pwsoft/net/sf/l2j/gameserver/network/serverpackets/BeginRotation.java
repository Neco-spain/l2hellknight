package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Character;

public class BeginRotation extends L2GameServerPacket
{
  private int _charId;
  private int _degree;
  private int _side;
  private int _speed;

  public BeginRotation(L2Character cha, int degree, int side, int speed)
  {
    _charId = cha.getObjectId();
    _degree = degree;
    _side = side;
    _speed = speed;
  }

  protected final void writeImpl()
  {
    writeC(98);
    writeD(_charId);
    writeD(_degree);
    writeD(_side);
    writeD(_speed);
  }
}