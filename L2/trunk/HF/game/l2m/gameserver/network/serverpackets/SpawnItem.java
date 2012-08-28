package l2m.gameserver.serverpackets;

import l2m.gameserver.Config;
import l2m.gameserver.model.items.ItemInstance;

public class SpawnItem extends L2GameServerPacket
{
  private int _objectId;
  private int _itemId;
  private int _x;
  private int _y;
  private int _z;
  private int _stackable;
  private long _count;

  public SpawnItem(ItemInstance item)
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
    writeC(5);
    writeD(_objectId);
    writeD(_itemId);

    writeD(_x);
    writeD(_y);
    writeD(_z + Config.CLIENT_Z_SHIFT);
    writeD(_stackable);
    writeQ(_count);
    writeD(0);
  }
}