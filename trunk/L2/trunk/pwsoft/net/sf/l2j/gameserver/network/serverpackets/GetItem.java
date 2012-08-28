package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2ItemInstance;

public class GetItem extends L2GameServerPacket
{
  private L2ItemInstance _item;
  private int _playerId;

  public GetItem(L2ItemInstance item, int playerId)
  {
    _item = item;
    _playerId = playerId;
  }

  protected final void writeImpl()
  {
    writeC(13);
    writeD(_playerId);
    writeD(_item.getObjectId());

    writeD(_item.getX());
    writeD(_item.getY());
    writeD(_item.getZ());
  }
}