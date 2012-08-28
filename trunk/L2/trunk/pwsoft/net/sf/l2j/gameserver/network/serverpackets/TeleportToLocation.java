package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Object;

public class TeleportToLocation extends L2GameServerPacket
{
  private int _targetObjId;
  private int _x;
  private int _y;
  private int _z;
  private boolean _fromServer = false;

  public TeleportToLocation(L2Object obj, int x, int y, int z, boolean flag)
  {
    _targetObjId = obj.getObjectId();
    _x = x;
    _y = y;
    _z = z;
    _fromServer = flag;
  }

  public TeleportToLocation(L2Object obj, int x, int y, int z)
  {
    _targetObjId = obj.getObjectId();
    _x = x;
    _y = y;
    _z = z;
  }

  public final void runImpl()
  {
  }

  protected final void writeImpl()
  {
    if (!_fromServer) {
      return;
    }
    writeC(40);
    writeD(_targetObjId);
    writeD(_x);
    writeD(_y);
    writeD(_z);
  }

  public String getType()
  {
    return "S.TeleportToLocation";
  }
}