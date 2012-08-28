package l2m.gameserver.serverpackets;

import l2m.gameserver.utils.Location;

public class TargetSelected extends L2GameServerPacket
{
  private int _objectId;
  private int _targetId;
  private Location _loc;

  public TargetSelected(int objectId, int targetId, Location loc)
  {
    _objectId = objectId;
    _targetId = targetId;
    _loc = loc;
  }

  protected final void writeImpl()
  {
    writeC(35);
    writeD(_objectId);
    writeD(_targetId);
    writeD(_loc.x);
    writeD(_loc.y);
    writeD(_loc.z);
    writeD(0);
  }
}