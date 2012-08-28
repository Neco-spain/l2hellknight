package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2StaticObjectInstance;

public class StaticObject extends L2GameServerPacket
{
  private static final String _S__99_StaticObjectPacket = "[S] 99 StaticObjectPacket";
  private L2StaticObjectInstance _staticObject;

  public StaticObject(L2StaticObjectInstance StaticObject)
  {
    _staticObject = StaticObject;
  }

  protected final void writeImpl()
  {
    writeC(153);
    writeD(_staticObject.getStaticObjectId());
    writeD(_staticObject.getObjectId());
  }

  public String getType()
  {
    return "[S] 99 StaticObjectPacket";
  }
}