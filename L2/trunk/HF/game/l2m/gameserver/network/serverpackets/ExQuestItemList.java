package l2m.gameserver.network.serverpackets;

import l2m.gameserver.model.items.ItemInstance;
import l2m.gameserver.model.items.LockType;
import l2m.gameserver.templates.item.ItemTemplate;

public class ExQuestItemList extends L2GameServerPacket
{
  private int _size;
  private ItemInstance[] _items;
  private LockType _lockType;
  private int[] _lockItems;

  public ExQuestItemList(int size, ItemInstance[] t, LockType lockType, int[] lockItems)
  {
    _size = size;
    _items = t;
    _lockType = lockType;
    _lockItems = lockItems;
  }

  protected void writeImpl()
  {
    writeEx(198);
    writeH(_size);

    for (ItemInstance temp : _items)
    {
      if (!temp.getTemplate().isQuest()) {
        continue;
      }
      writeItemInfo(temp);
    }

    writeH(_lockItems.length);
    if (_lockItems.length > 0)
    {
      writeC(_lockType.ordinal());
      for (int i : _lockItems)
        writeD(i);
    }
  }
}