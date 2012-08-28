package net.sf.l2j.gameserver.model;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GmListTable;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.ObjectKnownList;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.util.L2ObjectMap;
import net.sf.l2j.util.Point3D;

public final class L2World
{
  private static Logger _log = Logger.getLogger(L2World.class.getName());
  public static final int SHIFT_BY = 12;
  public static final int MAP_MIN_X = -131072;
  public static final int MAP_MAX_X = 228608;
  public static final int MAP_MIN_Y = -262144;
  public static final int MAP_MAX_Y = 262144;
  public static final int OFFSET_X = Math.abs(-32);
  public static final int OFFSET_Y = Math.abs(-64);

  private static final int REGIONS_X = 55 + OFFSET_X;
  private static final int REGIONS_Y = 64 + OFFSET_Y;
  private static Map<String, L2PcInstance> _allPlayers;
  private static ConcurrentHashMap<String, L2PcInstance> _allPlayersws = new ConcurrentHashMap();
  private L2ObjectMap<L2Object> _allObjects;
  private FastMap<Integer, L2PetInstance> _petsInstance;
  private static final L2World _instance = new L2World();
  private L2WorldRegion[][] _worldRegions;
  private long _timestamp_offline = 0L;

  private int _offline = 0;

  private L2World()
  {
    _allPlayers = new FastMap().setShared(true);
    _petsInstance = new FastMap().setShared(true);
    _allObjects = L2ObjectMap.createL2ObjectMap();

    initRegions();
  }

  public static L2World getInstance()
  {
    return _instance;
  }

  public void storeObject(L2Object object)
  {
    if (_allObjects.get(object.getObjectId()) != null)
    {
      if (Config.DEBUG)
        _log.warning("[L2World] objectId " + object.getObjectId() + " already exist in OID map!");
      return;
    }
    _allObjects.put(object);
  }

  public long timeStoreObject(L2Object object)
  {
    long time = System.currentTimeMillis();
    _allObjects.put(object);
    time -= System.currentTimeMillis();
    return time;
  }

  public void removeObject(L2Object object)
  {
    _allObjects.remove(object);
  }

  public void removeObjects(List<L2Object> list)
  {
    L2Object o;
    for (Iterator i$ = list.iterator(); i$.hasNext(); _allObjects.remove(o)) o = (L2Object)i$.next();
  }

  public void removeObjects(L2Object[] objects)
  {
    for (L2Object o : objects) _allObjects.remove(o);
  }

  public long timeRemoveObject(L2Object object)
  {
    long time = System.currentTimeMillis();
    _allObjects.remove(object);
    time -= System.currentTimeMillis();
    return time;
  }

  public L2Object findObject(int oID)
  {
    return _allObjects.get(oID);
  }

  public boolean findPlayer(String player)
  {
    return _allPlayers.containsKey(player.toLowerCase());
  }

  public long timeFindObject(int objectID)
  {
    long time = System.currentTimeMillis();
    _allObjects.get(objectID);
    time -= System.currentTimeMillis();
    return time;
  }

  @Deprecated
  public final L2ObjectMap<L2Object> getAllVisibleObjects()
  {
    return _allObjects;
  }

  public final int getAllVisibleObjectsCount()
  {
    return _allObjects.size();
  }

  public FastList<L2PcInstance> getAllGMs()
  {
    return GmListTable.getInstance().getAllGms(true);
  }

  public Collection<L2PcInstance> getAllPlayers()
  {
    return _allPlayers.values();
  }

  public static int getAllPlayersCount()
  {
    return _allPlayers.size();
  }

  public L2PcInstance getPlayer(String name)
  {
    return (L2PcInstance)_allPlayers.get(name.toLowerCase());
  }

  public L2PcInstance getPlayer(int playerObjId)
  {
    return (L2PcInstance)_allPlayers.get(Integer.valueOf(playerObjId));
  }

  public Collection<L2PetInstance> getAllPets()
  {
    return _petsInstance.values();
  }

  public L2PetInstance getPet(int ownerId)
  {
    return (L2PetInstance)_petsInstance.get(new Integer(ownerId));
  }

  public L2PetInstance addPet(int ownerId, L2PetInstance pet)
  {
    return (L2PetInstance)_petsInstance.put(new Integer(ownerId), pet);
  }

  public void removePet(int ownerId)
  {
    _petsInstance.remove(new Integer(ownerId));
  }

  public void removePet(L2PetInstance pet)
  {
    _petsInstance.values().remove(pet);
  }

  public void addVisibleObject(L2Object object, L2WorldRegion newRegion, L2Character dropper)
  {
    if ((object instanceof L2PcInstance))
    {
      L2PcInstance player = (L2PcInstance)object;
      L2PcInstance tmp = (L2PcInstance)_allPlayers.get(player.getName().toLowerCase());

      if ((tmp != null) && (tmp != player))
      {
        if (((Config.OFFLINE_TRADE_ENABLE) || (Config.OFFLINE_CRAFT_ENABLE)) && (tmp.isOffline()))
        {
          if (tmp.getClient() != null)
          {
            tmp.getClient().setActiveChar(null);
          }

          tmp = null;
        }
        else
        {
          _log.warning("EnterWorld: Duplicate character!? Closing both characters (" + player.getName() + ")");
          L2GameClient client = player.getClient();

          player.store();
          player.deleteMe();
          client.setActiveChar(null);
          client = tmp.getClient();

          tmp.store();
          tmp.deleteMe();

          if (client != null)
          {
            client.setActiveChar(null);
          }

          tmp = null;

          return;
        }
      }

      if (!player.isTeleporting())
      {
        if (tmp != null)
        {
          _log.warning("Duplicate character!? Closing both characters (" + player.getName() + ")");
          player.closeNetConnection(true);
          tmp.closeNetConnection(true);
          return;
        }
        _allPlayers.put(player.getName().toLowerCase(), player);
      }
    }
    if (!newRegion.isActive().booleanValue()) {
      return;
    }

    FastList visibles = getVisibleObjects(object, 2000);
    if (Config.DEBUG) _log.finest("objects in range:" + visibles.size());

    for (L2Object visible : visibles)
    {
      visible.getKnownList().addKnownObject(object, dropper);

      object.getKnownList().addKnownObject(visible, dropper);
    }
  }

  public void removeFromAllPlayers(L2PcInstance cha)
  {
    _allPlayers.remove(cha.getName().toLowerCase());
  }

  public void addToAllPlayers(L2PcInstance cha)
  {
    _allPlayers.put(cha.getName().toLowerCase(), cha);
  }

  public void removeVisibleObject(L2Object object, L2WorldRegion oldRegion)
  {
    if (object == null) {
      return;
    }

    if (oldRegion != null)
    {
      oldRegion.removeVisibleObject(object);

      for (L2WorldRegion reg : oldRegion.getSurroundingRegions())
      {
        for (L2Object obj : reg.getVisibleObjects())
        {
          if (obj == null)
          {
            continue;
          }

          if (obj.getKnownList() != null) {
            obj.getKnownList().removeKnownObject(object);
          }

          if (object.getKnownList() != null) {
            object.getKnownList().removeKnownObject(obj);
          }

        }

      }

      object.getKnownList().removeAllKnownObjects();

      if ((object instanceof L2PcInstance))
      {
        if (!((L2PcInstance)object).isTeleporting())
          removeFromAllPlayers((L2PcInstance)object);
      }
    }
  }

  public FastList<L2Object> getVisibleObjects(L2Object object)
  {
    L2WorldRegion reg = object.getWorldRegion();

    if (reg == null) {
      return null;
    }

    FastList result = new FastList();

    for (L2WorldRegion regi : reg.getSurroundingRegions())
    {
      for (L2Object _object : regi.getVisibleObjects())
      {
        if ((_object.equals(object)) || 
          (!_object.isVisible())) {
          continue;
        }
        result.add(_object);
      }
    }

    return result;
  }

  public FastList<L2Object> getVisibleObjects(L2Object object, int radius)
  {
    if ((object == null) || (!object.isVisible())) {
      return new FastList();
    }
    int x = object.getX();
    int y = object.getY();
    int sqRadius = radius * radius;

    FastList result = new FastList();

    for (L2WorldRegion regi : object.getWorldRegion().getSurroundingRegions())
    {
      for (L2Object _object : regi.getVisibleObjects())
      {
        if (_object.equals(object))
          continue;
        int x1 = _object.getX();
        int y1 = _object.getY();

        double dx = x1 - x;

        double dy = y1 - y;

        if (dx * dx + dy * dy < sqRadius) {
          result.add(_object);
        }
      }
    }
    return result;
  }

  public FastList<L2Object> getVisibleObjects3D(L2Object object, int radius)
  {
    if ((object == null) || (!object.isVisible())) {
      return new FastList();
    }
    int x = object.getX();
    int y = object.getY();
    int z = object.getZ();
    int sqRadius = radius * radius;

    FastList result = new FastList();

    for (L2WorldRegion regi : object.getWorldRegion().getSurroundingRegions())
    {
      for (L2Object _object : regi.getVisibleObjects())
      {
        if (_object.equals(object))
          continue;
        int x1 = _object.getX();
        int y1 = _object.getY();
        int z1 = _object.getZ();

        long dx = x1 - x;

        long dy = y1 - y;

        long dz = z1 - z;

        if (dx * dx + dy * dy + dz * dz < sqRadius) {
          result.add(_object);
        }
      }
    }
    return result;
  }

  public FastList<L2PlayableInstance> getVisiblePlayable(L2Object object)
  {
    L2WorldRegion reg = object.getWorldRegion();

    if (reg == null) {
      return null;
    }

    FastList result = new FastList();

    for (L2WorldRegion regi : reg.getSurroundingRegions())
    {
      Iterator _playables = regi.iterateAllPlayers();

      while (_playables.hasNext())
      {
        L2PlayableInstance _object = (L2PlayableInstance)_playables.next();

        if ((_object == null) || 
          (_object.equals(object)) || 
          (!_object.isVisible())) {
          continue;
        }
        result.add(_object);
      }
    }

    return result;
  }

  public L2WorldRegion getRegion(Point3D point)
  {
    return _worldRegions[((point.getX() >> 12) + OFFSET_X)][((point.getY() >> 12) + OFFSET_Y)];
  }

  public L2WorldRegion getRegion(int x, int y)
  {
    return _worldRegions[((x >> 12) + OFFSET_X)][((y >> 12) + OFFSET_Y)];
  }

  public L2WorldRegion[][] getAllWorldRegions()
  {
    return _worldRegions;
  }

  private boolean validRegion(int x, int y)
  {
    return (x >= 0) && (x <= REGIONS_X) && (y >= 0) && (y <= REGIONS_Y);
  }

  private void initRegions()
  {
    _log.config("L2World: Setting up World Regions");

    _worldRegions = new L2WorldRegion[REGIONS_X + 1][REGIONS_Y + 1];

    for (int i = 0; i <= REGIONS_X; i++)
    {
      for (int j = 0; j <= REGIONS_Y; j++)
      {
        _worldRegions[i][j] = new L2WorldRegion(i, j);
      }
    }

    for (int x = 0; x <= REGIONS_X; x++)
    {
      for (int y = 0; y <= REGIONS_Y; y++)
      {
        for (int a = -1; a <= 1; a++)
        {
          for (int b = -1; b <= 1; b++)
          {
            if (!validRegion(x + a, y + b))
              continue;
            _worldRegions[(x + a)][(y + b)].addSurroundingRegion(_worldRegions[x][y]);
          }
        }
      }

    }

    _log.config("L2World: (" + REGIONS_X + " by " + REGIONS_Y + ") World Region Grid set up.");
  }

  public synchronized void deleteVisibleNpcSpawns()
  {
    _log.info("Deleting all visible NPC's.");
    for (int i = 0; i <= REGIONS_X; i++)
    {
      for (int j = 0; j <= REGIONS_Y; j++)
      {
        _worldRegions[i][j].deleteVisibleNpcSpawns();
      }
    }
    _log.info("All visible NPC's deleted.");
  }

  public static ConcurrentHashMap<String, L2PcInstance> getAllPlayersHashmap()
  {
    return _allPlayersws;
  }

  public int getAllOfflineCount()
  {
    if ((!Config.OFFLINE_TRADE_ENABLE) && (!Config.OFFLINE_CRAFT_ENABLE)) {
      return 0;
    }
    if (System.currentTimeMillis() - _timestamp_offline < 10000L) {
      return _offline;
    }
    _timestamp_offline = System.currentTimeMillis();

    int offline = 0;
    for (L2PcInstance player : getAllPlayers()) {
      if (player.isOffline())
        offline++;
    }
    _offline = offline;
    return _offline;
  }
}