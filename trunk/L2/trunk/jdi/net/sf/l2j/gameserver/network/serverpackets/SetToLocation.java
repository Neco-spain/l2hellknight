package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Character;

public class SetToLocation extends L2GameServerPacket
{
  private static final String _S__76_SETTOLOCATION = "[S] 76 SetToLocation";
  private int _charObjId;
  private int _x;
  private int _y;
  private int _z;
  private int _heading;

  public SetToLocation(L2Character character)
  {
    _charObjId = character.getObjectId();
    _x = character.getX();
    _y = character.getY();
    _z = character.getZ();
    _heading = character.getHeading();
  }

  protected final void writeImpl()
  {
    writeC(118);

    writeD(_charObjId);
    writeD(_x);
    writeD(_y);
    writeD(_z);
    writeD(_heading);
  }

  public String getType()
  {
    return "[S] 76 SetToLocation";
  }
}