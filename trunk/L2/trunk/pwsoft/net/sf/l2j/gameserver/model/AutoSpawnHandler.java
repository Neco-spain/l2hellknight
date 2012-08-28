package net.sf.l2j.gameserver.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.MapRegionTable;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.mysql.Close;
import net.sf.l2j.mysql.Connect;
import net.sf.l2j.util.Location;
import net.sf.l2j.util.Rnd;
import net.sf.l2j.util.log.AbstractLogger;

public class AutoSpawnHandler
{
  protected static final Logger _log = AbstractLogger.getLogger(AutoSpawnHandler.class.getName());
  private static AutoSpawnHandler _instance;
  private static final int DEFAULT_INITIAL_SPAWN = 30000;
  private static final int DEFAULT_RESPAWN = 3600000;
  private static final int DEFAULT_DESPAWN = 3600000;
  protected Map<Integer, AutoSpawnInstance> _registeredSpawns;
  protected Map<Integer, ScheduledFuture> _runningSpawns;
  protected boolean _activeState = true;

  private AutoSpawnHandler()
  {
    _registeredSpawns = new FastMap();
    _runningSpawns = new FastMap();

    restoreSpawnData();
  }

  public static AutoSpawnHandler getInstance()
  {
    return _instance;
  }

  public static void init()
  {
    _instance = new AutoSpawnHandler();
  }

  public final int size()
  {
    return _registeredSpawns.size();
  }

  private void restoreSpawnData()
  {
    int numLoaded = 0;

    Connect con = null;
    PreparedStatement statement = null;
    PreparedStatement statement2 = null;
    ResultSet rs = null;
    ResultSet rs2 = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      con.setTransactionIsolation(1);

      statement = con.prepareStatement("SELECT * FROM random_spawn ORDER BY groupId ASC");
      rs = statement.executeQuery();

      while (rs.next())
      {
        AutoSpawnInstance spawnInst = registerSpawn(rs.getInt("npcId"), rs.getInt("initialDelay"), rs.getInt("respawnDelay"), rs.getInt("despawnDelay"));

        spawnInst.setSpawnCount(rs.getInt("count"));
        spawnInst.setBroadcast(rs.getBoolean("broadcastSpawn"));
        spawnInst.setRandomSpawn(rs.getBoolean("randomSpawn"));
        numLoaded++;

        statement2 = con.prepareStatement("SELECT * FROM random_spawn_loc WHERE groupId=?");
        statement2.setInt(1, rs.getInt("groupId"));
        rs2 = statement2.executeQuery();

        while (rs2.next())
        {
          spawnInst.addSpawnLocation(rs2.getInt("x"), rs2.getInt("y"), rs2.getInt("z"), rs2.getInt("heading"));
        }

        Close.S(statement2);
      }
    }
    catch (Exception e)
    {
      _log.warning("AutoSpawnHandler: Could not restore spawn data: " + e);
    }
    finally
    {
      Close.SR(statement2, rs2);
      Close.CSR(con, statement, rs);
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
    if (spawnInst == null) {
      return false;
    }
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
      _log.warning("AutoSpawnHandler: Could not auto spawn for NPC ID " + spawnInst._npcId + " (Object ID = " + spawnInst._objectId + "): " + e);

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
    if (spawnInst == null) {
      return;
    }
    int objectId = spawnInst._objectId;

    if (isSpawnRegistered(objectId))
    {
      ScheduledFuture spawnTask = null;

      if (isActive)
      {
        AutoSpawner rs = new AutoSpawner(objectId);

        if (spawnInst._desDelay > 0) {
          spawnTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(rs, spawnInst._initDelay, spawnInst._resDelay);
        }
        else {
          spawnTask = ThreadPoolManager.getInstance().scheduleEffect(rs, spawnInst._initDelay);
        }
        _runningSpawns.put(Integer.valueOf(objectId), spawnTask);
      }
      else {
        AutoDespawner rd = new AutoDespawner(objectId);
        spawnTask = (ScheduledFuture)_runningSpawns.remove(Integer.valueOf(objectId));

        if (spawnTask != null) {
          spawnTask.cancel(false);
        }
        ThreadPoolManager.getInstance().scheduleEffect(rd, 0L);
      }

      spawnInst.setSpawnActive(isActive);
    }
  }

  public void setAllActive(boolean isActive)
  {
    if (_activeState == isActive) {
      return;
    }
    for (AutoSpawnInstance spawnInst : _registeredSpawns.values()) {
      setSpawnActive(spawnInst, isActive);
    }
    _activeState = isActive;
  }

  public final long getTimeToNextSpawn(AutoSpawnInstance spawnInst)
  {
    int objectId = spawnInst.getObjectId();

    if (!isSpawnRegistered(objectId)) {
      return -1L;
    }
    return ((ScheduledFuture)_runningSpawns.get(Integer.valueOf(objectId))).getDelay(TimeUnit.MILLISECONDS);
  }

  public final AutoSpawnInstance getAutoSpawnInstance(int id, boolean isObjectId)
  {
    if (isObjectId)
    {
      if (isSpawnRegistered(id)) {
        return (AutoSpawnInstance)_registeredSpawns.get(Integer.valueOf(id));
      }
    }
    else {
      for (AutoSpawnInstance spawnInst : _registeredSpawns.values())
        if (spawnInst.getNpcId() == id)
          return spawnInst;
    }
    return null;
  }

  public Map<Integer, AutoSpawnInstance> getAutoSpawnInstances(int npcId)
  {
    Map spawnInstList = new FastMap();

    for (AutoSpawnInstance spawnInst : _registeredSpawns.values()) {
      if (spawnInst.getNpcId() == npcId)
        spawnInstList.put(Integer.valueOf(spawnInst.getObjectId()), spawnInst);
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

  public static class AutoSpawnInstance
  {
    protected int _objectId;
    protected int _spawnIndex;
    protected int _npcId;
    protected int _initDelay;
    protected int _resDelay;
    protected int _desDelay;
    protected int _spawnCount = 1;

    protected int _lastLocIndex = -1;

    private List<L2NpcInstance> _npcList = new FastList();

    private List<Location> _locList = new FastList();
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

    protected void setSpawnActive(boolean activeValue)
    {
      _spawnActive = activeValue;
    }

    protected boolean addNpcInstance(L2NpcInstance npcInst)
    {
      return _npcList.add(npcInst);
    }

    protected boolean removeNpcInstance(L2NpcInstance npcInst)
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

    public L2NpcInstance[] getNPCInstanceList()
    {
      L2NpcInstance[] ret;
      synchronized (_npcList)
      {
        ret = new L2NpcInstance[_npcList.size()];
        _npcList.toArray(ret);
      }

      return ret;
    }

    public L2Spawn[] getSpawns()
    {
      List npcSpawns = new FastList();

      for (L2NpcInstance npcInst : _npcList) {
        npcSpawns.add(npcInst.getSpawn());
      }
      return (L2Spawn[])npcSpawns.toArray(new L2Spawn[npcSpawns.size()]);
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
      } catch (IndexOutOfBoundsException e) {
      }
      return null;
    }
  }

  private class AutoDespawner
    implements Runnable
  {
    private int _objectId;

    protected AutoDespawner(int objectId)
    {
      _objectId = objectId;
    }

    public void run()
    {
      try
      {
        AutoSpawnHandler.AutoSpawnInstance spawnInst = (AutoSpawnHandler.AutoSpawnInstance)_registeredSpawns.get(Integer.valueOf(_objectId));

        if (spawnInst == null)
        {
          AutoSpawnHandler._log.info("AutoSpawnHandler: No spawn registered for object ID = " + _objectId + ".");
          return;
        }

        for (L2NpcInstance npcInst : spawnInst.getNPCInstanceList())
        {
          if (npcInst == null) {
            continue;
          }
          npcInst.deleteMe();
          spawnInst.removeNpcInstance(npcInst);
        }

      }
      catch (Exception e)
      {
        AutoSpawnHandler._log.warning("AutoSpawnHandler: An error occurred while despawning spawn (Object ID = " + _objectId + "): " + e);
      }
    }
  }

  private class AutoSpawner
    implements Runnable
  {
    private int _objectId;

    protected AutoSpawner(int objectId)
    {
      _objectId = objectId;
    }

    public void run()
    {
      try
      {
        AutoSpawnHandler.AutoSpawnInstance spawnInst = (AutoSpawnHandler.AutoSpawnInstance)_registeredSpawns.get(Integer.valueOf(_objectId));

        if (!spawnInst.isSpawnActive()) {
          return;
        }
        Location[] locationList = spawnInst.getLocationList();

        if (locationList.length == 0)
        {
          return;
        }

        int locationCount = locationList.length;
        int locationIndex = Rnd.nextInt(locationCount);

        if (!spawnInst.isRandomSpawn())
        {
          locationIndex = spawnInst._lastLocIndex;
          locationIndex++;

          if (locationIndex == locationCount) {
            locationIndex = 0;
          }
          spawnInst._lastLocIndex = locationIndex;
        }

        int x = locationList[locationIndex].getX();
        int y = locationList[locationIndex].getY();
        int z = locationList[locationIndex].getZ();
        int heading = locationList[locationIndex].getHeading();

        L2NpcTemplate npcTemp = NpcTable.getInstance().getTemplate(spawnInst.getNpcId());
        if (npcTemp == null)
        {
          AutoSpawnHandler._log.warning("Couldnt find NPC id" + spawnInst.getNpcId() + " Try to update your DP");
          return;
        }
        L2Spawn newSpawn = new L2Spawn(npcTemp);

        newSpawn.setLocx(x);
        newSpawn.setLocy(y);
        newSpawn.setLocz(z);
        if (heading != -1)
          newSpawn.setHeading(heading);
        newSpawn.setAmount(spawnInst.getSpawnCount());
        if (spawnInst._desDelay == 0)
        {
          newSpawn.setRespawnDelay(spawnInst._resDelay);
        }

        SpawnTable.getInstance().addNewSpawn(newSpawn, false);
        L2NpcInstance npcInst = null;

        if (spawnInst._spawnCount == 1)
        {
          npcInst = newSpawn.doSpawn();
          npcInst.setXYZ(npcInst.getX(), npcInst.getY(), npcInst.getZ());
          spawnInst.addNpcInstance(npcInst);
        }
        else {
          for (int i = 0; i < spawnInst._spawnCount; i++)
          {
            npcInst = newSpawn.doSpawn();

            npcInst.setXYZ(npcInst.getX() + Rnd.nextInt(50), npcInst.getY() + Rnd.nextInt(50), npcInst.getZ());

            spawnInst.addNpcInstance(npcInst);
          }
        }

        String nearestTown = MapRegionTable.getInstance().getClosestTownName(npcInst);

        if (spawnInst.isBroadcasting()) {
          Announcements.getInstance().announceToAll("The " + npcInst.getName() + " has spawned near " + nearestTown + "!");
        }

        if (spawnInst.getDespawnDelay() > 0)
        {
          AutoSpawnHandler.AutoDespawner rd = new AutoSpawnHandler.AutoDespawner(AutoSpawnHandler.this, _objectId);
          ThreadPoolManager.getInstance().scheduleAi(rd, spawnInst.getDespawnDelay() - 1000, false);
        }
      }
      catch (Exception e) {
        AutoSpawnHandler._log.warning("AutoSpawnHandler: An error occurred while initializing spawn instance (Object ID = " + _objectId + "): " + e);

        e.printStackTrace();
      }
    }
  }
}