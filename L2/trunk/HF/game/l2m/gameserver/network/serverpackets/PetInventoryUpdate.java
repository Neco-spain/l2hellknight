package l2m.gameserver.serverpackets;

import java.util.ArrayList;
import java.util.List;
import l2m.gameserver.model.items.ItemInfo;
import l2m.gameserver.model.items.ItemInstance;

public class PetInventoryUpdate extends L2GameServerPacket
{
  public static final int UNCHANGED = 0;
  public static final int ADDED = 1;
  public static final int MODIFIED = 2;
  public static final int REMOVED = 3;
  private final List<ItemInfo> _items = new ArrayList(1);

  public PetInventoryUpdate addNewItem(ItemInstance item)
  {
    addItem(item).setLastChange(1);
    return this;
  }

  public PetInventoryUpdate addModifiedItem(ItemInstance item)
  {
    addItem(item).setLastChange(2);
    return this;
  }

  public PetInventoryUpdate addRemovedItem(ItemInstance item)
  {
    addItem(item).setLastChange(3);
    return this;
  }

  private ItemInfo addItem(ItemInstance item)
  {
    ItemInfo info;
    _items.add(info = new ItemInfo(item));
    return info;
  }

  protected final void writeImpl()
  {
    writeC(180);
    writeH(_items.size());
    for (ItemInfo temp : _items)
    {
      writeH(temp.getLastChange());
      writeItemInfo(temp);
    }
  }
}