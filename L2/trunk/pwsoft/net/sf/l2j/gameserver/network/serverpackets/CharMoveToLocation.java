package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Character;

public class CharMoveToLocation extends L2GameServerPacket
{
  private int _charObjId;
  private int _x;
  private int _y;
  private int _z;
  private int _xDst;
  private int _yDst;
  private int _zDst;

  public CharMoveToLocation(L2Character cha)
  {
    _charObjId = cha.getObjectId();
    _x = cha.getX();
    _y = cha.getY();
    _z = cha.getZ();
    _xDst = cha.getXdestination();
    _yDst = cha.getYdestination();
    _zDst = cha.getZdestination();
  }

  protected final void writeImpl()
  {
    writeC(1);

    writeD(_charObjId);

    writeD(_xDst);
    writeD(_yDst);
    writeD(_zDst);

    writeD(_x);
    writeD(_y);
    writeD(_z);
  }
}