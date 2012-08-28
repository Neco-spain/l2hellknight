package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2BoatInstance;
import net.sf.l2j.gameserver.model.actor.position.ObjectPosition;

public class VehicleInfo extends L2GameServerPacket
{
  private L2BoatInstance _boat;

  public VehicleInfo(L2BoatInstance boat)
  {
    _boat = boat;
  }

  protected void writeImpl()
  {
    writeC(89);
    writeD(_boat.getObjectId());
    writeD(_boat.getX());
    writeD(_boat.getY());
    writeD(_boat.getZ());
    writeD(_boat.getPosition().getHeading());
  }

  public String getType()
  {
    return "[S] 59 VehicleInfo";
  }
}