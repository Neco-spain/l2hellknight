package net.sf.l2j.gameserver.cache;

import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class WarehouseCacheManager
{
  private static WarehouseCacheManager _instance;
  protected final FastMap<L2PcInstance, Long> _cachedWh;
  protected final long _cacheTime;

  public static WarehouseCacheManager getInstance()
  {
    if (_instance == null)
      _instance = new WarehouseCacheManager();
    return _instance;
  }

  private WarehouseCacheManager()
  {
    _cacheTime = (Config.WAREHOUSE_CACHE_TIME * 60000L);
    _cachedWh = new FastMap().shared("WarehouseCacheManager._cachedWh");
    ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new CacheScheduler(), 120000L, 60000L);
  }

  public void addCacheTask(L2PcInstance pc)
  {
    _cachedWh.put(pc, Long.valueOf(System.currentTimeMillis()));
  }

  public void remCacheTask(L2PcInstance pc) {
    _cachedWh.remove(pc);
  }
  public class CacheScheduler implements Runnable {
    public CacheScheduler() {
    }

    public void run() {
      long cTime = System.currentTimeMillis();
      for (L2PcInstance pc : _cachedWh.keySet())
      {
        if (cTime - ((Long)_cachedWh.get(pc)).longValue() > _cacheTime)
        {
          pc.clearWarehouse();
          _cachedWh.remove(pc);
        }
      }
    }
  }
}