package l2p.gameserver.model.items;

import java.util.List;
import l2p.commons.dao.JdbcEntityState;
import l2p.gameserver.dao.ItemsDAO;
import l2p.gameserver.model.Player;

public class PcRefund extends ItemContainer
{
  public PcRefund(Player player)
  {
  }

  protected void onAddItem(ItemInstance item)
  {
    item.setLocation(ItemInstance.ItemLocation.VOID);
    if (item.getJdbcState().isPersisted())
    {
      item.setJdbcState(JdbcEntityState.UPDATED);
      item.update();
    }

    if (_items.size() > 12)
    {
      destroyItem((ItemInstance)_items.remove(0));
    }
  }

  protected void onModifyItem(ItemInstance item)
  {
  }

  protected void onRemoveItem(ItemInstance item)
  {
  }

  protected void onDestroyItem(ItemInstance item)
  {
    item.setCount(0L);
    item.delete();
  }

  public void clear()
  {
    writeLock();
    try
    {
      _itemsDAO.delete(_items);
      _items.clear();
    }
    finally
    {
      writeUnlock();
    }
  }
}