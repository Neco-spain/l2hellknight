package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.poly.ObjectPoly;

public final class SpawnItem extends L2GameServerPacket
{
  private static final String _S__15_SPAWNITEM = "[S] 15 SpawnItem";
  private int _objectId;
  private int _itemId;
  private int _x;
  private int _y;
  private int _z;
  private int _stackable;
  private int _count;

  public SpawnItem(L2Object obj)
  {
    _objectId = obj.getObjectId();
    _x = obj.getX();
    _y = obj.getY();
    _z = obj.getZ();

    if ((obj instanceof L2ItemInstance))
    {
      L2ItemInstance item = (L2ItemInstance)obj;
      _itemId = item.getItemId();
      _stackable = (item.isStackable() ? 1 : 0);
      _count = item.getCount();
    }
    else
    {
      _itemId = obj.getPoly().getPolyId();
      _stackable = 0;
      _count = 1;
    }
  }

  protected final void writeImpl()
  {
    writeC(11);
    writeD(_objectId);
    writeD(_itemId);

    writeD(_x);
    writeD(_y);
    writeD(_z);

    writeD(_stackable);
    writeD(_count);
    writeD(0);
  }

  public String getType()
  {
    return "[S] 15 SpawnItem";
  }
}