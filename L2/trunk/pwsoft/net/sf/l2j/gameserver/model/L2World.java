package net.sf.l2j.gameserver.model;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastList.Node;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GmListTable;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.ObjectKnownList;
import net.sf.l2j.util.Point3D;
import net.sf.l2j.util.log.AbstractLogger;

public final class L2World
{
  private static final Logger _log = AbstractLogger.getLogger(L2World.class.getName());
  public static final int SHIFT_BY = 12;
  public static final int MAP_MIN_X = Config.MAP_MIN_X;
  public static final int MAP_MAX_X = Config.MAP_MAX_X;
  public static final int MAP_MIN_Y = Config.MAP_MIN_Y;
  public static final int MAP_MAX_Y = Config.MAP_MAX_Y;

  public static final int OFFSET_X = Math.abs(MAP_MIN_X >> 12);
  public static final int OFFSET_Y = Math.abs(MAP_MIN_Y >> 12);

  private static final int REGIONS_X = (MAP_MAX_X >> 12) + OFFSET_X;
  private static final int REGIONS_Y = (MAP_MAX_Y >> 12) + OFFSET_Y;
  private static Map<Integer, L2PcInstance> _allPlayers = new ConcurrentHashMap();
  private static Map<Integer, L2Object> _allObjects = new ConcurrentHashMap();
  private static Map<Integer, L2PetInstance> _petsInstance = new ConcurrentHashMap();
  private static final L2World _instance = new L2World();
  private static L2WorldRegion[][] _worldRegions;
  private long _timestamp_online = 0L;
  private int _online = 0;

  private long _timestamp_offline = 0L;
  private int _offline = 0;

  private long _timestamp_online2 = 0L;
  private int _online2 = 0;

  private L2World()
  {
    initRegions();
  }

  public static L2World getInstance()
  {
    return _instance;
  }

  public void storeObject(L2Object object)
  {
    if (object == null) {
      return;
    }

    if (_allObjects.containsKey(Integer.valueOf(object.getObjectId()))) {
      return;
    }

    _allObjects.put(Integer.valueOf(object.getObjectId()), object);
  }

  public void removeObject(L2Object object)
  {
    if (object == null) {
      return;
    }
    _allObjects.remove(Integer.valueOf(object.getObjectId()));
  }

  public void removeObjects(List<L2Object> list) {
    if (list == null) {
      return;
    }

    for (L2Object o : list) {
      if (o == null) {
        continue;
      }
      _allObjects.remove(Integer.valueOf(o.getObjectId()));
    }
  }

  public void removeObjects(L2Object[] objects) {
    if (objects == null) {
      return;
    }

    for (L2Object o : objects) {
      if (o == null) {
        continue;
      }
      _allObjects.remove(Integer.valueOf(o.getObjectId()));
    }
  }

  public L2Object findObject(int i)
  {
    return (L2Object)_allObjects.get(Integer.valueOf(i));
  }

  @Deprecated
  public final Map<Integer, L2Object> getAllVisibleObjects()
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

  public L2PcInstance getPlayer(String name)
  {
    return findPlayer(null, name);
  }

  private L2PcInstance findPlayer(L2PcInstance player, String name) {
    for (Map.Entry entry : _allPlayers.entrySet()) {
      player = (L2PcInstance)entry.getValue();
      if ((player == null) || (player.isOnline() == 0))
      {
        continue;
      }
      if (player.getName().equalsIgnoreCase(name)) {
        return player;
      }
    }
    return null;
  }

  public L2PcInstance getPlayer(int id) {
    return (L2PcInstance)_allPlayers.get(Integer.valueOf(id));
  }

  public Collection<L2PetInstance> getAllPets()
  {
    return _petsInstance.values();
  }

  public L2PetInstance getPet(int ownerId)
  {
    return (L2PetInstance)_petsInstance.get(Integer.valueOf(ownerId));
  }

  public L2PetInstance addPet(int ownerId, L2PetInstance pet)
  {
    return (L2PetInstance)_petsInstance.put(Integer.valueOf(ownerId), pet);
  }

  public void removePet(int ownerId)
  {
    _petsInstance.remove(Integer.valueOf(ownerId));
  }

  public void removePet(L2PetInstance pet)
  {
    _petsInstance.values().remove(pet);
  }

  public void addVisibleObject(L2Object object, L2WorldRegion newRegion, L2Character dropper)
  {
    if (object.isPlayer()) {
      L2PcInstance player = object.getPlayer();

      if (!player.isTeleporting()) {
        L2PcInstance tmp = (L2PcInstance)_allPlayers.get(Integer.valueOf(player.getObjectId()));
        if ((tmp != null) && (tmp != player)) {
          _log.warning("Duplicate character? Closing both characters: (" + player.getName() + ")");

          player.kick();
          tmp.kick();
          return;
        }
        _allPlayers.put(Integer.valueOf(player.getObjectId()), player);
      }

    }

    FastList visibles = getVisibleObjects(object, 2000);

    L2Object visible = null;
    FastList.Node n = visibles.head(); for (FastList.Node end = visibles.tail(); (n = n.getNext()) != end; ) {
      visible = (L2Object)n.getValue();
      if (visible == null)
      {
        continue;
      }

      visible.getKnownList().addKnownObject(object, dropper);

      object.getKnownList().addKnownObject(visible, dropper);
    }

    if (object.isL2Npc())
      object.setShowSpawnAnimation(0);
  }

  public void removeFromAllPlayers(L2PcInstance cha)
  {
    _allPlayers.remove(Integer.valueOf(cha.getObjectId()));
  }

  public void removeVisibleObject(L2Object object, L2WorldRegion oldRegion)
  {
    if (object == null) {
      return;
    }

    if (oldRegion != null)
    {
      oldRegion.removeVisibleObject(object);

      for (L2WorldRegion reg : oldRegion.getSurroundingRegions()) {
        for (L2Object obj : reg.getVisibleObjects())
        {
          if ((obj != null) && (obj.getKnownList() != null)) {
            obj.getKnownList().removeKnownObject(object);
          }

          if (object.getKnownList() != null) {
            object.getKnownList().removeKnownObject(obj);
          }

        }

      }

      object.getKnownList().removeAllKnownObjects();

      if ((object.isPlayer()) && 
        (!object.getPlayer().isTeleporting()))
        removeFromAllPlayers(object.getPlayer());
    }
  }

  public FastList<L2Object> getVisibleObjects(L2Object object)
  {
    L2WorldRegion reg = object.getWorldRegion();
    if (reg == null) {
      return null;
    }

    FastList result = new FastList();

    FastList _regions = reg.getSurroundingRegions();
    FastList.Node n = _regions.head(); for (FastList.Node end = _regions.tail(); (n = n.getNext()) != end; ) {
      L2WorldRegion value = (L2WorldRegion)n.getValue();
      if (value == null)
      {
        continue;
      }

      for (L2Object _object : value.getVisibleObjects()) {
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

  public FastList<L2Object> getVisibleObjects(L2Object object, int radius)
  {
    if ((object == null) || (!object.isVisible())) {
      return new FastList();
    }

    int x = object.getX();
    int y = object.getY();
    int sqRadius = radius * radius;

    FastList result = new FastList();

    FastList _regions = object.getWorldRegion().getSurroundingRegions();
    FastList.Node n = _regions.head(); for (FastList.Node end = _regions.tail(); (n = n.getNext()) != end; ) {
      L2WorldRegion value = (L2WorldRegion)n.getValue();
      if (value == null)
      {
        continue;
      }

      for (L2Object _object : value.getVisibleObjects()) {
        if ((_object == null) || 
          (_object.equals(object))) {
          continue;
        }
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

    FastList _regions = object.getWorldRegion().getSurroundingRegions();
    FastList.Node n = _regions.head(); for (FastList.Node end = _regions.tail(); (n = n.getNext()) != end; ) {
      L2WorldRegion value = (L2WorldRegion)n.getValue();
      if (value == null)
      {
        continue;
      }
      for (L2Object _object : value.getVisibleObjects()) {
        if ((_object == null) || 
          (_object.equals(object))) {
          continue;
        }
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

    FastList _regions = reg.getSurroundingRegions();
    FastList.Node n = _regions.head(); for (FastList.Node end = _regions.tail(); (n = n.getNext()) != end; ) {
      L2WorldRegion value = (L2WorldRegion)n.getValue();
      if (value == null)
      {
        continue;
      }

      Iterator _playables = value.iterateAllPlayers();

      while (_playables.hasNext()) {
        L2PlayableInstance _object = (L2PlayableInstance)_playables.next();

        if ((_object == null) || 
          (_object.equals(object)) || 
          (!_object.isVisible()))
        {
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

  public L2WorldRegion getRegion(int x, int y) {
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

    for (int i = 0; i <= REGIONS_X; i++) {
      for (int j = 0; j <= REGIONS_Y; j++) {
        _worldRegions[i][j] = new L2WorldRegion(i, j);
      }
    }

    for (int x = 0; x <= REGIONS_X; x++) {
      for (int y = 0; y <= REGIONS_Y; y++) {
        for (int a = -1; a <= 1; a++) {
          for (int b = -1; b <= 1; b++) {
            if (validRegion(x + a, y + b)) {
              _worldRegions[(x + a)][(y + b)].addSurroundingRegion(_worldRegions[x][y]);
            }
          }
        }
      }
    }

    _log.config("L2World: (" + REGIONS_X + " by " + REGIONS_Y + ") World Region Grid set up.");
  }

  public synchronized void deleteVisibleNpcSpawns()
  {
    deleteVisibleNpcSpawns(false);
  }

  public synchronized void deleteVisibleNpcSpawns(boolean respawn)
  {
    for (int i = 0; i <= REGIONS_X; i++) {
      for (int j = 0; j <= REGIONS_Y; j++) {
        _worldRegions[i][j].deleteVisibleNpcSpawns();
      }
    }

    if (respawn)
      SpawnTable.getInstance().reloadAll();
  }

  public synchronized void respawnVisibleNpcSpawns(int id)
  {
    for (int i = 0; i <= REGIONS_X; i++)
      for (int j = 0; j <= REGIONS_Y; j++)
        _worldRegions[i][j].respawnVisibleNpcSpawns(id);
  }

  public int getAllPlayersCount()
  {
    if (System.currentTimeMillis() - _timestamp_online < 10000L) {
      return _online;
    }

    _timestamp_online = System.currentTimeMillis();

    _online = _allPlayers.size();

    return _online;
  }

  public int getPlayersCount() {
    return _allPlayers.size();
  }

  public int getAllOfflineCount()
  {
    if (!Config.ALT_ALLOW_OFFLINE_TRADE) {
      return 0;
    }

    if (System.currentTimeMillis() - _timestamp_offline < 30000L) {
      return _offline;
    }

    _timestamp_offline = System.currentTimeMillis();

    int offline = 0;
    for (L2PcInstance player : getAllPlayers()) {
      if (player.isInOfflineMode()) {
        offline++;
      }
    }

    _offline = offline;
    return _offline;
  }

  public L2ItemInstance getItem(int itemObj)
  {
    L2Object obj = (L2Object)_allObjects.get(Integer.valueOf(itemObj));
    if ((obj != null) && (obj.isL2Item())) {
      return (L2ItemInstance)obj;
    }

    return null;
  }

  public void removePlayer(L2PcInstance player) {
    removeObject(player);
    removeFromAllPlayers(player);
  }

  public int getLivePlayersCount()
  {
    if (System.currentTimeMillis() - _timestamp_online2 < 10000L) {
      return _online2;
    }

    _online2 = 0;
    _timestamp_online2 = System.currentTimeMillis();
    L2PcInstance player = null;
    for (Map.Entry entry : _allPlayers.entrySet()) {
      player = (L2PcInstance)entry.getValue();
      if ((player == null) || (player.isFantome()))
      {
        continue;
      }
      _online2 += 1;
    }
    player = null;

    return _online2;
  }
}