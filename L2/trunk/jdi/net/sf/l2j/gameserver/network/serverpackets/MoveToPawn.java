package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Character;

public class MoveToPawn extends L2GameServerPacket
{
  private static final String _S__75_MOVETOPAWN = "[S] 60 MoveToPawn";
  private int _charObjId;
  private int _targetId;
  private int _distance;
  private int _x;
  private int _y;
  private int _z;

  public MoveToPawn(L2Character cha, L2Character target, int distance)
  {
    _charObjId = cha.getObjectId();
    _targetId = target.getObjectId();
    _distance = distance;
    _x = cha.getX();
    _y = cha.getY();
    _z = cha.getZ();
  }

  protected final void writeImpl()
  {
    writeC(96);

    writeD(_charObjId);
    writeD(_targetId);
    writeD(_distance);

    writeD(_x);
    writeD(_y);
    writeD(_z);
  }

  public String getType()
  {
    return "[S] 60 MoveToPawn";
  }
}