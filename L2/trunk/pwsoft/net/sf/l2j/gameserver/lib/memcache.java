package net.sf.l2j.gameserver.lib;

import java.util.HashMap;
import java.util.logging.Logger;
import net.sf.l2j.util.log.AbstractLogger;

@Deprecated
public class memcache
{
  private static Logger _log = AbstractLogger.getLogger(memcache.class.getName());
  private HashMap<Integer, String> _hms;
  private HashMap<Integer, Integer> _hmi;
  private HashMap<Integer, Long> _lastAccess;
  private static final memcache _instance = new memcache();

  public static memcache getInstance()
  {
    return _instance;
  }

  private memcache()
  {
    _hms = new HashMap();
    _hmi = new HashMap();
    _lastAccess = new HashMap();
  }

  private void checkExpired()
  {
    for (Integer k : _hmi.keySet())
    {
      if (((Long)_lastAccess.get(k)).longValue() + 3600000L >= System.currentTimeMillis());
    }

    for (Integer k : _hms.keySet())
      if (((Long)_lastAccess.get(k)).longValue() + 3600000L >= System.currentTimeMillis());
  }

  public void set(String type, String key, int value)
  {
    int hash = (type + "->" + key).hashCode();

    _hmi.put(Integer.valueOf(hash), Integer.valueOf(value));
    _lastAccess.put(Integer.valueOf(hash), Long.valueOf(System.currentTimeMillis()));
    checkExpired();
  }

  @Deprecated
  public boolean isSet(String type, String key) {
    int hash = (type + "->" + key).hashCode();
    boolean exists = (_hmi.containsKey(Integer.valueOf(hash))) || (_hms.containsKey(Integer.valueOf(hash)));
    if (exists) {
      _lastAccess.put(Integer.valueOf(hash), Long.valueOf(System.currentTimeMillis()));
    }
    checkExpired();
    _log.fine("Check exists memcache " + type + "(" + key + ")[" + hash + "] is " + exists);
    return exists;
  }

  @Deprecated
  public Integer getInt(String type, String key) {
    int hash = (type + "->" + key).hashCode();
    _lastAccess.put(Integer.valueOf(hash), Long.valueOf(System.currentTimeMillis()));
    checkExpired();
    _log.fine("Get memcache " + type + "(" + key + ")[" + hash + "] = " + _hmi.get(Integer.valueOf(hash)));
    return (Integer)_hmi.get(Integer.valueOf(hash));
  }
}