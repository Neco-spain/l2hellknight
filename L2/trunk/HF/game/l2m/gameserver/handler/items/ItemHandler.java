package l2m.gameserver.handler.items;

import l2p.commons.data.xml.AbstractHolder;
import l2m.gameserver.data.xml.holder.ItemHolder;
import l2m.gameserver.templates.item.ItemTemplate;

public class ItemHandler extends AbstractHolder
{
  private static final ItemHandler _instance = new ItemHandler();

  public static ItemHandler getInstance()
  {
    return _instance;
  }

  public void registerItemHandler(IItemHandler handler)
  {
    int[] ids = handler.getItemIds();
    for (int itemId : ids)
    {
      ItemTemplate template = ItemHolder.getInstance().getTemplate(itemId);
      if ((template != null) && (template.getHandler() == IItemHandler.NULL))
        template.setHandler(handler);
    }
  }

  public int size()
  {
    return 0;
  }

  public void clear()
  {
  }
}