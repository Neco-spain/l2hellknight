package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.network.L2GameClient;

public class DoorStatusUpdate extends L2GameServerPacket
{
  private L2DoorInstance _door;

  public DoorStatusUpdate(L2DoorInstance door, boolean flag)
  {
    _door = door;
  }

  protected final void writeImpl()
  {
    writeC(77);
    writeD(_door.getObjectId());
    writeD(_door.getOpen() ? 0 : 1);
    writeD(_door.getDamage());
    writeD(_door.isEnemyOf(((L2GameClient)getClient()).getActiveChar()) ? 1 : 0);
    writeD(_door.getDoorId());
    writeD(_door.getMaxHp());
    writeD((int)_door.getCurrentHp());
  }
}