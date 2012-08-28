package l2m.gameserver.network.serverpackets;

import l2m.gameserver.model.items.ItemInstance;
import l2m.gameserver.utils.Location;

public class GetItem extends L2GameServerPacket
{
  private int _playerId;
  private int _itemObjId;
  private Location _loc;

  public GetItem(ItemInstance item, int playerId)
  {
    _itemObjId = item.getObjectId();
    _loc = item.getLoc();
    _playerId = playerId;
  }

  protected final void writeImpl()
  {
    writeC(23);
    writeD(_playerId);
    writeD(_itemObjId);
    writeD(_loc.x);
    writeD(_loc.y);
    writeD(_loc.z);
  }
}