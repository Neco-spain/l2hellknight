package l2m.gameserver.cache;

import l2m.gameserver.model.Player;
import l2m.gameserver.model.World;
import l2m.gameserver.model.items.ItemInfo;
import l2m.gameserver.model.items.ItemInstance;
import l2m.gameserver.model.items.PcInventory;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

public class ItemInfoCache
{
  private static final ItemInfoCache _instance = new ItemInfoCache();
  private Cache cache;

  public static final ItemInfoCache getInstance()
  {
    return _instance;
  }

  private ItemInfoCache()
  {
    cache = CacheManager.getInstance().getCache(getClass().getName());
  }

  public void put(ItemInstance item)
  {
    cache.put(new Element(Integer.valueOf(item.getObjectId()), new ItemInfo(item)));
  }

  public ItemInfo get(int objectId)
  {
    Element element = cache.get(Integer.valueOf(objectId));

    ItemInfo info = null;
    if (element != null) {
      info = (ItemInfo)element.getObjectValue();
    }
    Player player = null;

    if (info != null)
    {
      player = World.getPlayer(info.getOwnerId());

      ItemInstance item = null;

      if (player != null) {
        item = player.getInventory().getItemByObjectId(objectId);
      }
      if ((item != null) && 
        (item.getItemId() == info.getItemId())) {
        cache.put(new Element(Integer.valueOf(item.getObjectId()), info = new ItemInfo(item)));
      }
    }
    return info;
  }
}