package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2BoatInstance;
import net.sf.l2j.gameserver.model.actor.position.ObjectPosition;

public class OnVehicleCheckLocation extends L2GameServerPacket
{
  private L2BoatInstance _boat;
  private int _x;
  private int _y;
  private int _z;

  public OnVehicleCheckLocation(L2BoatInstance instance, int x, int y, int z)
  {
    _boat = instance;
    _x = x;
    _y = y;
    _z = z;
  }

  protected void writeImpl()
  {
    writeC(91);
    writeD(_boat.getObjectId());
    writeD(_x);
    writeD(_y);
    writeD(_z);
    writeD(_boat.getPosition().getHeading());
  }
}