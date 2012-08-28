package l2m.gameserver.network.serverpackets;

import l2m.gameserver.Config;
import l2m.gameserver.model.items.ItemInstance;
import l2m.gameserver.utils.Location;

public class DropItem extends L2GameServerPacket
{
  private Location _loc;
  private int _playerId;
  private int item_obj_id;
  private int item_id;
  private int _stackable;
  private long _count;

  public DropItem(ItemInstance item, int playerId)
  {
    _playerId = playerId;
    item_obj_id = item.getObjectId();
    item_id = item.getItemId();
    _loc = item.getLoc();
    _stackable = (item.isStackable() ? 1 : 0);
    _count = item.getCount();
  }

  protected final void writeImpl()
  {
    writeC(22);
    writeD(_playerId);
    writeD(item_obj_id);
    writeD(item_id);
    writeD(_loc.x);
    writeD(_loc.y);
    writeD(_loc.z + Config.CLIENT_Z_SHIFT);
    writeD(_stackable);
    writeQ(_count);
    writeD(1);
  }
}