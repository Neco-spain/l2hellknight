package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Object;

public class DeleteObject extends L2GameServerPacket
{
  private int _objectId;

  public DeleteObject(L2Object obj)
  {
    _objectId = obj.getObjectId();
  }

  protected final void writeImpl()
  {
    writeC(18);
    writeD(_objectId);
    writeD(0);
  }
}