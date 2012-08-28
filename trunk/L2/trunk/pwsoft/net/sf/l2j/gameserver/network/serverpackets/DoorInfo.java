package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;

public class DoorInfo extends L2GameServerPacket
{
  private L2DoorInstance _door;

  public DoorInfo(L2DoorInstance door)
  {
    _door = door;
  }

  protected final void writeImpl()
  {
    writeC(76);
    writeD(_door.getObjectId());
    writeD(_door.getDoorId());
    writeD(1);
  }
}