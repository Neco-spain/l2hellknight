package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Object;

public class Revive extends L2GameServerPacket
{
  private static final String _S__0C_REVIVE = "[S] 07 Revive";
  private int _objectId;

  public Revive(L2Object obj)
  {
    _objectId = obj.getObjectId();
  }

  protected final void writeImpl()
  {
    writeC(7);
    writeD(_objectId);
  }

  public String getType()
  {
    return "[S] 07 Revive";
  }
}