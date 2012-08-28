package l2p.gameserver.serverpackets;

import l2p.gameserver.model.instances.DoorInstance;

public class DoorInfo extends L2GameServerPacket
{
  private int obj_id;
  private int door_id;
  private int view_hp;

  @Deprecated
  public DoorInfo(DoorInstance door)
  {
    obj_id = door.getObjectId();
    door_id = door.getDoorId();
    view_hp = (door.isHPVisible() ? 1 : 0);
  }

  protected final void writeImpl()
  {
    writeC(76);
    writeD(obj_id);
    writeD(door_id);
    writeD(view_hp);
  }
}