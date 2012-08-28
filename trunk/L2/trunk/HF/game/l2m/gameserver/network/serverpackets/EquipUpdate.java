package l2m.gameserver.network.serverpackets;

import l2m.gameserver.model.items.ItemInfo;
import l2m.gameserver.model.items.ItemInstance;

public class EquipUpdate extends L2GameServerPacket
{
  private ItemInfo _item;

  public EquipUpdate(ItemInstance item, int change)
  {
    _item = new ItemInfo(item);
    _item.setLastChange(change);
  }

  protected final void writeImpl()
  {
    writeC(75);
    writeD(_item.getLastChange());
    writeD(_item.getObjectId());
    writeD(_item.getEquipSlot());
  }
}