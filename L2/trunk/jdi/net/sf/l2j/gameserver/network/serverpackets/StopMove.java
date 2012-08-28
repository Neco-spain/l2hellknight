package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Character;

public class StopMove extends L2GameServerPacket
{
  private static final String _S__59_STOPMOVE = "[S] 47 StopMove";
  private int _objectId;
  private int _x;
  private int _y;
  private int _z;
  private int _heading;

  public StopMove(L2Character cha)
  {
    this(cha.getObjectId(), cha.getX(), cha.getY(), cha.getZ(), cha.getHeading());
  }

  public StopMove(int objectId, int x, int y, int z, int heading)
  {
    _objectId = objectId;
    _x = x;
    _y = y;
    _z = z;
    _heading = heading;
  }

  protected final void writeImpl()
  {
    writeC(71);
    writeD(_objectId);
    writeD(_x);
    writeD(_y);
    writeD(_z);
    writeD(_heading);
  }

  public String getType()
  {
    return "[S] 47 StopMove";
  }
}