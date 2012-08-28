package l2m.gameserver.serverpackets;

import l2m.gameserver.model.GameObject;

public class Revive extends L2GameServerPacket
{
  private int _objectId;

  public Revive(GameObject obj)
  {
    _objectId = obj.getObjectId();
  }

  protected final void writeImpl()
  {
    writeC(1);
    writeD(_objectId);
  }
}