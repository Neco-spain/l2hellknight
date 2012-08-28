package l2p.gameserver.serverpackets;

import l2p.gameserver.model.Creature;

public class MoveToPawn extends L2GameServerPacket
{
  private int _chaId;
  private int _targetId;
  private int _distance;
  private int _x;
  private int _y;
  private int _z;
  private int _tx;
  private int _ty;
  private int _tz;

  public MoveToPawn(Creature cha, Creature target, int distance)
  {
    _chaId = cha.getObjectId();
    _targetId = target.getObjectId();
    _distance = distance;
    _x = cha.getX();
    _y = cha.getY();
    _z = cha.getZ();
    _tx = target.getX();
    _ty = target.getY();
    _tz = target.getZ();
  }

  protected final void writeImpl()
  {
    writeC(114);

    writeD(_chaId);
    writeD(_targetId);
    writeD(_distance);

    writeD(_x);
    writeD(_y);
    writeD(_z);

    writeD(_tx);
    writeD(_ty);
    writeD(_tz);
  }
}