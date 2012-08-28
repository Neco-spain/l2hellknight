package l2m.gameserver.serverpackets;

import l2m.gameserver.model.Player;
import l2m.gameserver.utils.Location;

public class Ride extends L2GameServerPacket
{
  private int _mountType;
  private int _id;
  private int _rideClassID;
  private Location _loc;

  public Ride(Player cha)
  {
    _id = cha.getObjectId();
    _mountType = cha.getMountType();
    _rideClassID = (cha.getMountNpcId() + 1000000);
    _loc = cha.getLoc();
  }

  protected final void writeImpl()
  {
    writeC(140);
    writeD(_id);
    writeD(_mountType == 0 ? 0 : 1);
    writeD(_mountType);
    writeD(_rideClassID);
    writeD(_loc.x);
    writeD(_loc.y);
    writeD(_loc.z);
  }
}