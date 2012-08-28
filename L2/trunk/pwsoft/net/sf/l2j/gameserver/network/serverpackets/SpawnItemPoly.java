package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.poly.ObjectPoly;

public class SpawnItemPoly extends L2GameServerPacket
{
  private int _objectId;
  private int _itemId;
  private int _x;
  private int _y;
  private int _z;
  private int _stackable;
  private int _count;

  public SpawnItemPoly(L2Object object)
  {
    if (object.isL2Item())
    {
      L2ItemInstance item = (L2ItemInstance)object;
      _objectId = object.getObjectId();
      _itemId = object.getPoly().getPolyId();
      _x = item.getX();
      _y = item.getY();
      _z = item.getZ();
      _stackable = (item.isStackable() ? 1 : 0);
      _count = item.getCount();
    }
    else
    {
      _objectId = object.getObjectId();
      _itemId = object.getPoly().getPolyId();
      _x = object.getX();
      _y = object.getY();
      _z = object.getZ();
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
    return "S.SpawnItem";
  }
}