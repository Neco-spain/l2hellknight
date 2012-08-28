package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2ItemInstance;

public class DropItem extends L2GameServerPacket
{
  private static final String _S__16_DROPITEM = "[S] 0c DropItem";
  private L2ItemInstance _item;
  private int _charObjId;

  public DropItem(L2ItemInstance item, int playerObjId)
  {
    _item = item;
    _charObjId = playerObjId;
  }

  protected final void writeImpl()
  {
    writeC(12);
    writeD(_charObjId);
    writeD(_item.getObjectId());
    writeD(_item.getItemId());

    writeD(_item.getX());
    writeD(_item.getY());
    writeD(_item.getZ());

    if (_item.isStackable())
    {
      writeD(1);
    }
    else
    {
      writeD(0);
    }
    writeD(_item.getCount());

    writeD(1);
  }

  public String getType()
  {
    return "[S] 0c DropItem";
  }
}