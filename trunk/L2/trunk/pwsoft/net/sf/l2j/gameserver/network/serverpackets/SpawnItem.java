package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2ItemInstance;

public class SpawnItem extends L2GameServerPacket
{
  private int _objectId;
  private int _itemId;
  private int _x;
  private int _y;
  private int _z;
  private int _stackable;
  private int _count;

  public SpawnItem(L2ItemInstance item)
  {
    _objectId = item.getObjectId();
    _itemId = item.getItemId();
    _x = item.getX();
    _y = item.getY();
    _z = item.getZ();
    _stackable = (item.isStackable() ? 1 : 0);
    _count = item.getCount();
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