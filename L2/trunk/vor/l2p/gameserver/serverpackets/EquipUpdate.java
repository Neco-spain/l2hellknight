package l2p.gameserver.serverpackets;

import l2p.gameserver.model.items.ItemInfo;
import l2p.gameserver.model.items.ItemInstance;

@Deprecated
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