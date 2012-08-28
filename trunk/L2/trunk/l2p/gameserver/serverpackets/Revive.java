package l2p.gameserver.serverpackets;

import l2p.gameserver.model.GameObject;

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