package l2m.gameserver.network.serverpackets;

import l2m.gameserver.model.Creature;
import l2m.gameserver.utils.Location;

public class FlyToLocation extends L2GameServerPacket
{
  private int _chaObjId;
  private final FlyType _type;
  private Location _loc;
  private Location _destLoc;

  public FlyToLocation(Creature cha, Location destLoc, FlyType type)
  {
    _destLoc = destLoc;
    _type = type;
    _chaObjId = cha.getObjectId();
    _loc = cha.getLoc();
  }

  protected void writeImpl()
  {
    writeC(212);
    writeD(_chaObjId);
    writeD(_destLoc.x);
    writeD(_destLoc.y);
    writeD(_destLoc.z);
    writeD(_loc.x);
    writeD(_loc.y);
    writeD(_loc.z);
    writeD(_type.ordinal());
  }

  public static enum FlyType
  {
    THROW_UP, 
    THROW_HORIZONTAL, 
    DUMMY, 
    CHARGE, 
    NONE;
  }
}