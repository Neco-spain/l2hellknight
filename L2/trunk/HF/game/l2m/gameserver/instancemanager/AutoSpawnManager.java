package l2m.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import l2p.commons.dbutils.DbUtils;
import l2p.commons.threading.RunnableImpl;
import l2p.commons.util.Rnd;
import l2m.gameserver.Announcements;
import l2m.gameserver.Config;
import l2m.gameserver.ThreadPoolManager;
import l2m.gameserver.data.xml.holder.NpcHolder;
import l2m.gameserver.database.DatabaseFactory;
import l2m.gameserver.idfactory.IdFactory;
import l2m.gameserver.model.SimpleSpawner;
import l2m.gameserver.model.Spawner;
import l2m.gameserver.model.base.Race;
import l2m.gameserver.model.instances.NpcInstance;
import l2m.gameserver.templates.mapregion.RestartArea;
import l2m.gameserver.templates.mapregion.RestartPoint;
import l2m.gameserver.templates.npc.NpcTemplate;
import l2m.gameserver.utils.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoSpawnManager
{
  private static final Logger _log = LoggerFactory.getLogger(AutoSpawnManager.class);
  private static AutoSpawnManager _instance;
  private static final int DEFAULT_INITIAL_SPAWN = 30000;
  private static final int DEFAULT_RESPAWN = 3600000;
  private static final int DEFAULT_DESPAWN = 3600000;
  protected Map<Integer, AutoSpawnInstance> _registeredSpawns;
  protected Map<Integer, ScheduledFuture<?>> _runningSpawns;

  public AutoSpawnManager()
  {
    _registeredSpawns = new ConcurrentHashMap();
    _runningSpawns = new ConcurrentHashMap();

    restoreSpawnData();

    _log.info("AutoSpawnHandler: Loaded " + size() + " handlers in total.");
  }

  public static AutoSpawnManager getInstance()
  {
    if (_instance == null) {
      _instance = new AutoSpawnManager();
    }
    return _instance;
  }

  public final int size()
  {
    return _registeredSpawns.size();
  }

  private void restoreSpawnData()
  {
    int numLoaded = 0;
    Connection con = null;
    PreparedStatement statement = null;
    PreparedStatement statement2 = null;
    ResultSet rset = null; ResultSet rset2 = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();

      statement = con.prepareStatement("SELECT * FROM random_spawn ORDER BY groupId ASC");
      statement2 = con.prepareStatement("SELECT * FROM random_spawn_loc WHERE groupId=?");

      rset = statement.executeQuery();
      while (rset.next())
      {
        AutoSpawnInstance spawnInst = registerSpawn(rset.getInt("npcId"), rset.getInt("initialDelay"), rset.getInt("respawnDelay"), rset.getInt("despawnDelay"));
        spawnInst.setSpawnCount(rset.getInt("count"));
        spawnInst.setBroadcast(rset.getBoolean("broadcastSpawn"));
        spawnInst.setRandomSpawn(rset.getBoolean("randomSpawn"));
        numLoaded++;

        statement2.setInt(1, rset.getInt("groupId"));
        rset2 = statement2.executeQuery();
        while (rset2.next())
        {
          spawnInst.addSpawnLocation(rset2.getInt("x"), rset2.getInt("y"), rset2.getInt("z"), rset2.getInt("heading"));
        }DbUtils.close(rset2);
      }
    }
    catch (Exception e)
    {
      _log.warn("AutoSpawnHandler: Could not restore spawn data: " + e);
    }
    finally
    {
      DbUtils.closeQuietly(statement2, rset2);
      DbUtils.closeQuietly(con, statement, rset);
    }
  }

  public AutoSpawnInstance registerSpawn(int npcId, int[][] spawnPoints, int initialDelay, int respawnDelay, int despawnDelay)
  {
    if (initialDelay < 0) {
      initialDelay = 30000;
    }
    if (respawnDelay < 0) {
      respawnDelay = 3600000;
    }
    if (despawnDelay < 0) {
      despawnDelay = 3600000;
    }
    AutoSpawnInstance newSpawn = new AutoSpawnInstance(npcId, initialDelay, respawnDelay, despawnDelay);

    if (spawnPoints != null) {
      for (int[] spawnPoint : spawnPoints)
        newSpawn.addSpawnLocation(spawnPoint);
    }
    int newId = IdFactory.getInstance().getNextId();
    newSpawn._objectId = newId;
    _registeredSpawns.put(Integer.valueOf(newId), newSpawn);

    setSpawnActive(newSpawn, true);

    return newSpawn;
  }

  public AutoSpawnInstance registerSpawn(int npcId, int initialDelay, int respawnDelay, int despawnDelay)
  {
    return registerSpawn(npcId, (int[][])null, initialDelay, respawnDelay, despawnDelay);
  }

  public boolean removeSpawn(AutoSpawnInstance spawnInst)
  {
    if (!isSpawnRegistered(spawnInst)) {
      return false;
    }

    try
    {
      _registeredSpawns.remove(Integer.valueOf(spawnInst.getNpcId()));

      ScheduledFuture respawnTask = (ScheduledFuture)_runningSpawns.remove(Integer.valueOf(spawnInst._objectId));
      respawnTask.cancel(false);
    }
    catch (Exception e)
    {
      _log.warn("AutoSpawnHandler: Could not auto spawn for NPC ID " + spawnInst._npcId + " (Object ID = " + spawnInst._objectId + "): " + e);
      return false;
    }

    return true;
  }

  public void removeSpawn(int objectId)
  {
    removeSpawn((AutoSpawnInstance)_registeredSpawns.get(Integer.valueOf(objectId)));
  }

  public void setSpawnActive(AutoSpawnInstance spawnInst, boolean isActive)
  {
    int objectId = spawnInst._objectId;

    if (isSpawnRegistered(objectId))
    {
      ScheduledFuture spawnTask = null;

      if (isActive)
      {
        AutoSpawner rset = new AutoSpawner(objectId);
        if (spawnInst._desDelay > 0)
          spawnTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(rset, spawnInst._initDelay, spawnInst._resDelay);
        else {
          spawnTask = ThreadPoolManager.getInstance().schedule(rset, spawnInst._initDelay);
        }
        _runningSpawns.put(Integer.valueOf(objectId), spawnTask);
      }
      else
      {
        spawnTask = (ScheduledFuture)_runningSpawns.remove(Integer.valueOf(objectId));

        if (spawnTask != null) {
          spawnTask.cancel(false);
        }
      }
      spawnInst.setSpawnActive(isActive);
    }
  }

  public final long getTimeToNextSpawn(AutoSpawnInstance spawnInst)
  {
    int objectId = spawnInst._objectId;

    if (!isSpawnRegistered(objectId)) {
      return -1L;
    }
    return ((ScheduledFuture)_runningSpawns.get(Integer.valueOf(objectId))).getDelay(TimeUnit.MILLISECONDS);
  }

  public final AutoSpawnInstance getAutoSpawnInstance(int id, boolean isObjectId)
  {
    if (isObjectId)
    {
      if (isSpawnRegistered(id))
        return (AutoSpawnInstance)_registeredSpawns.get(Integer.valueOf(id));
    }
    else {
      for (AutoSpawnInstance spawnInst : _registeredSpawns.values())
        if (spawnInst._npcId == id)
          return spawnInst;
    }
    return null;
  }

  public Map<Integer, AutoSpawnInstance> getAllAutoSpawnInstance(int id)
  {
    Map spawnInstList = new ConcurrentHashMap();

    for (AutoSpawnInstance spawnInst : _registeredSpawns.values()) {
      if (spawnInst._npcId == id)
        spawnInstList.put(Integer.valueOf(spawnInst._objectId), spawnInst);
    }
    return spawnInstList;
  }

  public final boolean isSpawnRegistered(int objectId)
  {
    return _registeredSpawns.containsKey(Integer.valueOf(objectId));
  }

  public final boolean isSpawnRegistered(AutoSpawnInstance spawnInst)
  {
    return _registeredSpawns.containsValue(spawnInst);
  }

  public class AutoSpawnInstance
  {
    protected int _objectId;
    protected int _spawnIndex;
    protected int _npcId;
    protected int _initDelay;
    protected int _resDelay;
    protected int _desDelay;
    protected int _spawnCount = 1;
    protected int _lastLocIndex = -1;

    private List<NpcInstance> _npcList = new ArrayList();
    private List<Location> _locList = new ArrayList();
    private boolean _spawnActive;
    private boolean _randomSpawn = false;
    private boolean _broadcastAnnouncement = false;

    protected AutoSpawnInstance(int npcId, int initDelay, int respawnDelay, int despawnDelay)
    {
      _npcId = npcId;
      _initDelay = initDelay;
      _resDelay = respawnDelay;
      _desDelay = despawnDelay;
    }

    void setSpawnActive(boolean activeValue)
    {
      _spawnActive = activeValue;
    }

    boolean addAttackable(NpcInstance npcInst)
    {
      return _npcList.add(npcInst);
    }

    boolean removeAttackable(NpcInstance npcInst)
    {
      return _npcList.remove(npcInst);
    }

    public int getObjectId()
    {
      return _objectId;
    }

    public int getInitialDelay()
    {
      return _initDelay;
    }

    public int getRespawnDelay()
    {
      return _resDelay;
    }

    public int getDespawnDelay()
    {
      return _desDelay;
    }

    public int getNpcId()
    {
      return _npcId;
    }

    public int getSpawnCount()
    {
      return _spawnCount;
    }

    public Location[] getLocationList()
    {
      return (Location[])_locList.toArray(new Location[_locList.size()]);
    }

    public NpcInstance[] getAttackableList()
    {
      return (NpcInstance[])_npcList.toArray(new NpcInstance[_npcList.size()]);
    }

    public Spawner[] getSpawns()
    {
      List npcSpawns = new ArrayList();

      for (NpcInstance npcInst : _npcList) {
        npcSpawns.add(npcInst.getSpawn());
      }
      return (Spawner[])npcSpawns.toArray(new Spawner[npcSpawns.size()]);
    }

    public void setSpawnCount(int spawnCount)
    {
      _spawnCount = spawnCount;
    }

    public void setRandomSpawn(boolean randValue)
    {
      _randomSpawn = randValue;
    }

    public void setBroadcast(boolean broadcastValue)
    {
      _broadcastAnnouncement = broadcastValue;
    }

    public boolean isSpawnActive()
    {
      return _spawnActive;
    }

    public boolean isRandomSpawn()
    {
      return _randomSpawn;
    }

    public boolean isBroadcasting()
    {
      return _broadcastAnnouncement;
    }

    public boolean addSpawnLocation(int x, int y, int z, int heading)
    {
      return _locList.add(new Location(x, y, z, heading));
    }

    public boolean addSpawnLocation(int[] spawnLoc)
    {
      if (spawnLoc.length != 3) {
        return false;
      }
      return addSpawnLocation(spawnLoc[0], spawnLoc[1], spawnLoc[2], -1);
    }

    public Location removeSpawnLocation(int locIndex)
    {
      try
      {
        return (Location)_locList.remove(locIndex);
      }
      catch (IndexOutOfBoundsException e) {
      }
      return null;
    }
  }

  private class AutoDespawner extends RunnableImpl
  {
    private int _objectId;

    AutoDespawner(int objectId)
    {
      _objectId = objectId;
    }

    public void runImpl()
      throws Exception
    {
      try
      {
        AutoSpawnManager.AutoSpawnInstance spawnInst = (AutoSpawnManager.AutoSpawnInstance)_registeredSpawns.get(Integer.valueOf(_objectId));

        for (NpcInstance npcInst : spawnInst.getAttackableList())
        {
          npcInst.deleteMe();
          spawnInst.removeAttackable(npcInst);
        }
      }
      catch (Exception e)
      {
        _log.warn("AutoSpawnHandler: An error occurred while despawning spawn (Object ID = " + _objectId + "): " + e);
      }
    }
  }

  private class AutoSpawner extends RunnableImpl
  {
    private int _objectId;

    AutoSpawner(int objectId)
    {
      _objectId = objectId;
    }

    public void runImpl()
      throws Exception
    {
      try
      {
        AutoSpawnManager.AutoSpawnInstance spawnInst = (AutoSpawnManager.AutoSpawnInstance)_registeredSpawns.get(Integer.valueOf(_objectId));

        if ((!spawnInst.isSpawnActive()) || (Config.DONTLOADSPAWN)) {
          return;
        }
        Location[] locationList = spawnInst.getLocationList();

        if (locationList.length == 0)
        {
          _log.info("AutoSpawnHandler: No location co-ords specified for spawn instance (Object ID = " + _objectId + ").");
          return;
        }

        int locationCount = locationList.length;
        int locationIndex = Rnd.get(locationCount);

        if (!spawnInst.isRandomSpawn())
        {
          locationIndex = spawnInst._lastLocIndex;
          locationIndex++;

          if (locationIndex == locationCount) {
            locationIndex = 0;
          }
          spawnInst._lastLocIndex = locationIndex;
        }

        int x = locationList[locationIndex].x;
        int y = locationList[locationIndex].y;
        int z = locationList[locationIndex].z;
        int heading = locationList[locationIndex].h;

        NpcTemplate npcTemp = NpcHolder.getInstance().getTemplate(spawnInst.getNpcId());
        SimpleSpawner newSpawn = new SimpleSpawner(npcTemp);

        newSpawn.setLocx(x);
        newSpawn.setLocy(y);
        newSpawn.setLocz(z);
        if (heading != -1)
          newSpawn.setHeading(heading);
        newSpawn.setAmount(spawnInst.getSpawnCount());
        if (spawnInst._desDelay == 0) {
          newSpawn.setRespawnDelay(spawnInst._resDelay);
        }

        NpcInstance npcInst = null;

        for (int i = 0; i < spawnInst._spawnCount; i++)
        {
          npcInst = newSpawn.doSpawn(true);

          npcInst.setXYZ(npcInst.getX() + Rnd.get(50), npcInst.getY() + Rnd.get(50), npcInst.getZ());

          spawnInst.addAttackable(npcInst);
        }

        RestartPoint loc = (RestartPoint)((RestartArea)MapRegionManager.getInstance().getRegionData(RestartArea.class, npcInst.getLoc())).getRestartPoint().get(Race.human);
        if ((spawnInst.isBroadcasting()) && (npcInst != null))
        {
          Announcements.getInstance().announceByCustomMessage("l2p.gameserver.model.AutoSpawnHandler.spawnNPC", new String[] { npcInst.getName(), loc.getName() });
        }

        if (spawnInst.getDespawnDelay() > 0)
        {
          AutoSpawnManager.AutoDespawner rd = new AutoSpawnManager.AutoDespawner(AutoSpawnManager.this, _objectId);
          ThreadPoolManager.getInstance().schedule(rd, spawnInst.getDespawnDelay() - 1000);
        }
      }
      catch (Exception e)
      {
        _log.warn("AutoSpawnHandler: An error occurred while initializing spawn instance (Object ID = " + _objectId + "): " + e);
        _log.error("", e);
      }
    }
  }
}