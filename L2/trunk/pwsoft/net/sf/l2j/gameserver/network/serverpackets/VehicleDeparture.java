package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2BoatInstance;

public class VehicleDeparture extends L2GameServerPacket
{
  private L2BoatInstance _boat;
  private int _speed1;
  private int _speed2;
  private int _x;
  private int _y;
  private int _z;

  public VehicleDeparture(L2BoatInstance boat, int speed1, int speed2, int x, int y, int z)
  {
    _boat = boat;
    _speed1 = speed1;
    _speed2 = speed2;
    _x = x;
    _y = y;
    _z = z;
  }

  protected void writeImpl()
  {
    writeC(90);
    writeD(_boat.getObjectId());
    writeD(_speed1);
    writeD(_speed2);
    writeD(_x);
    writeD(_y);
    writeD(_z);
  }

  public String getType()
  {
    return "S.VehicleDeparture";
  }
}