package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Object;

public class TeleportToLocation extends L2GameServerPacket
{
  private static final String _S__38_TELEPORTTOLOCATION = "[S] 28 TeleportToLocation";
  private int _targetObjId;
  private int _x;
  private int _y;
  private int _z;

  public TeleportToLocation(L2Object obj, int x, int y, int z)
  {
    _targetObjId = obj.getObjectId();
    _x = x;
    _y = y;
    _z = z;
  }

  protected final void writeImpl()
  {
    writeC(40);
    writeD(_targetObjId);
    writeD(_x);
    writeD(_y);
    writeD(_z);
  }

  public String getType()
  {
    return "[S] 28 TeleportToLocation";
  }
}