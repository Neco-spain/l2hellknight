package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Character;

public class StopRotation extends L2GameServerPacket
{
  private int _charId;
  private int _degree;
  private int _speed;

  public StopRotation(L2Character cha, int degree, int speed)
  {
    _charId = cha.getObjectId();
    _degree = degree;
    _speed = speed;
  }

  protected final void writeImpl()
  {
    writeC(99);
    writeD(_charId);
    writeD(_degree);
    writeD(_speed);
    writeD(0);
  }
}