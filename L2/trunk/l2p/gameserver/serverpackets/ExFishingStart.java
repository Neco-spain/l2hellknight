package l2p.gameserver.serverpackets;

import l2p.gameserver.model.Creature;
import l2p.gameserver.utils.Location;

public class ExFishingStart extends L2GameServerPacket
{
  private int _charObjId;
  private Location _loc;
  private int _fishType;
  private boolean _isNightLure;

  public ExFishingStart(Creature character, int fishType, Location loc, boolean isNightLure)
  {
    _charObjId = character.getObjectId();
    _fishType = fishType;
    _loc = loc;
    _isNightLure = isNightLure;
  }

  protected final void writeImpl()
  {
    writeEx(30);
    writeD(_charObjId);
    writeD(_fishType);
    writeD(_loc.x);
    writeD(_loc.y);
    writeD(_loc.z);
    writeC(_isNightLure ? 1 : 0);
    writeC(1);
  }
}