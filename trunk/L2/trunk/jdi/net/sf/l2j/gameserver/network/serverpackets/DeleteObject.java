package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Object;

public class DeleteObject extends L2GameServerPacket
{
  private static final String _S__1E_DELETEOBJECT = "[S] 12 DeleteObject";
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

  public String getType()
  {
    return "[S] 12 DeleteObject";
  }
}