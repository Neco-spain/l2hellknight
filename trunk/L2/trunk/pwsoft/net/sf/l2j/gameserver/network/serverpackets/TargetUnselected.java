package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Character;

public class TargetUnselected extends L2GameServerPacket
{
  private int _targetObjId;
  private int _x;
  private int _y;
  private int _z;

  public TargetUnselected(L2Character character)
  {
    _targetObjId = character.getObjectId();
    _x = character.getX();
    _y = character.getY();
    _z = character.getZ();
  }

  protected final void writeImpl()
  {
    writeC(42);
    writeD(_targetObjId);
    writeD(_x);
    writeD(_y);
    writeD(_z);
  }

  public String getType()
  {
    return "S.TargetUnselected";
  }
}