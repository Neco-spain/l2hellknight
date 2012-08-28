package net.sf.l2j.gameserver.network.serverpackets;

public class Dice extends L2GameServerPacket
{
  private int _charObjId;
  private int _itemId;
  private int _number;
  private int _x;
  private int _y;
  private int _z;

  public Dice(int charObjId, int itemId, int number, int x, int y, int z)
  {
    _charObjId = charObjId;
    _itemId = itemId;
    _number = number;
    _x = x;
    _y = y;
    _z = z;
  }

  protected final void writeImpl()
  {
    writeC(212);
    writeD(_charObjId);
    writeD(_itemId);
    writeD(_number);
    writeD(_x);
    writeD(_y);
    writeD(_z);
  }
}