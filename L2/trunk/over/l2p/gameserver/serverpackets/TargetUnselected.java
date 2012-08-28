package l2p.gameserver.serverpackets;

import l2p.gameserver.model.GameObject;
import l2p.gameserver.utils.Location;

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