package l2m.gameserver.serverpackets;

import l2m.gameserver.model.entity.boat.Boat;

public class VehicleStart extends L2GameServerPacket
{
  private int _objectId;
  private int _state;

  public VehicleStart(Boat boat)
  {
    _objectId = boat.getObjectId();
    _state = boat.getRunState();
  }

  protected void writeImpl()
  {
    writeC(192);
    writeD(_objectId);
    writeD(_state);
  }
}