package l2m.gameserver.serverpackets;

import l2m.gameserver.model.GameObject;
import l2m.gameserver.utils.Location;

public class TargetUnselected extends L2GameServerPacket
{
  private int _targetId;
  private Location _loc;

  public TargetUnselected(GameObject obj)
  {
    _targetId = obj.getObjectId();
    _loc = obj.getLoc();
  }

  protected final void writeImpl()
  {
    writeC(36);
    writeD(_targetId);
    writeD(_loc.x);
    writeD(_loc.y);
    writeD(_loc.z);
    writeD(0);
  }
}