package net.sf.l2j.gameserver.network.serverpackets;

public class FlyToLocation extends L2GameServerPacket
{
  private int _charObjId;
  private int _x;
  private int _y;
  private int _z;
  private int _xDst;
  private int _yDst;
  private int _zDst;
  private int _type;

  public FlyToLocation(int _charObjId, int _x, int _y, int _z, int _xDst, int _yDst, int _zDst, int _type)
  {
    this._charObjId = _charObjId;
    this._x = _x;
    this._y = _y;
    this._z = _z;
    this._xDst = _xDst;
    this._yDst = _yDst;
    this._zDst = _zDst;
    this._type = _type;
  }

  protected final void writeImpl()
  {
    writeC(197);

    writeD(_charObjId);

    writeD(_xDst);
    writeD(_yDst);
    writeD(_zDst);

    writeD(_x);
    writeD(_y);
    writeD(_z);

    writeD(_type);
  }
}