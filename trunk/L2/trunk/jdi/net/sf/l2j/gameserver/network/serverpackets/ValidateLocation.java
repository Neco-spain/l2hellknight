package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Character;

public class ValidateLocation extends L2GameServerPacket
{
  private static final String _S__76_SETTOLOCATION = "[S] 61 ValidateLocation";
  private int _charObjId;
  private int _x;
  private int _y;
  private int _z;
  private int _heading;

  public ValidateLocation(L2Character cha)
  {
    _charObjId = cha.getObjectId();
    _x = cha.getX();
    _y = cha.getY();
    _z = cha.getZ();
    _heading = cha.getHeading();
  }

  protected final void writeImpl()
  {
    writeC(97);

    writeD(_charObjId);
    writeD(_x);
    writeD(_y);
    writeD(_z);
    writeD(_heading);
  }

  public String getType()
  {
    return "[S] 61 ValidateLocation";
  }
}