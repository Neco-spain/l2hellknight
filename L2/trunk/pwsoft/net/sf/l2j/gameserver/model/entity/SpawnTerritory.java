package net.sf.l2j.gameserver.model.entity;

import java.awt.Polygon;
import java.awt.geom.Rectangle2D;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastMap.Entry;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.util.Location;
import net.sf.l2j.util.Rnd;

public class SpawnTerritory
{
  private int _id = 0;
  private boolean _autospawn = true;
  private boolean _bossLair = false;

  private int _minX = 0;
  private int _maxX = 0;
  private int _minY = 0;
  private int _maxY = 0;
  private int _minZ = 0;
  private int _maxZ = 0;
  private int _minRespawn = 60000;

  private static int MOVE_DELAY = 10000;
  private Polygon _p = null;
  private Rectangle2D _r = null;
  private ScheduledFuture<?> _spawnTask = null;

  private FastMap<L2Spawn, Integer> _spawns = new FastMap().shared("SpawnTerritory._spawns");
  private FastList<Location> _waypoints = new FastList();

  public void checkSpawns()
  {
    boolean death = false;
    FastMap.Entry e = _spawns.head(); for (FastMap.Entry end = _spawns.tail(); (e = e.getNext()) != end; ) {
      L2Spawn spawn = (L2Spawn)e.getKey();
      int respawn = ((Integer)e.getValue()).intValue();
      if (spawn == null)
      {
        continue;
      }
      if ((spawn.getLastKill() > 0L) && (System.currentTimeMillis() - spawn.getLastKill() >= respawn)) {
        if (spawn.isFree()) {
          int[] xy = getRandomPoint();
          spawn.setLocx(xy[0]);
          spawn.setLocy(xy[1]);
          spawn.setLocz(Rnd.get(_minZ, _maxZ));
          spawn.setHeading(Rnd.get(65535));
          spawn.spawnOne();
        } else {
          spawn.spawnOne();
        }

        spawn.setLastKill(0L);
      } else {
        death = true;
      }
      try
      {
        Thread.sleep(1L);
      } catch (InterruptedException ex) {
      }
    }
    if (death)
      _spawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new RespawnTask(), _minRespawn);
    else
      _spawnTask = null;
  }

  public SpawnTerritory(int id)
  {
    _id = id;

    _p = new Polygon();
  }

  public void addPoint(int x, int y) {
    _p.addPoint(x, y);
  }

  public void setZ(int min, int max) {
    _minZ = min;
    _maxZ = max;
  }

  public int getMinZ() {
    return _minZ;
  }

  public int getMaxZ() {
    return _maxZ;
  }

  public int getId() {
    return _id;
  }

  public boolean isIdle()
  {
    return !_waypoints.isEmpty();
  }

  public void close()
  {
    _r = _p.getBounds2D();

    _minX = (int)_r.getMinX();
    _maxX = (int)_r.getMaxX();
    _minY = (int)_r.getMinY();
    _maxY = (int)_r.getMaxY();
  }

  public void addSpawn(L2Spawn npc, int respawn)
  {
    if ((respawn < _minRespawn) || (respawn > 120000)) {
      _minRespawn = respawn;
    }

    _spawns.put(npc, Integer.valueOf(respawn));
  }

  public void addWayPoint(int x, int y, int z)
  {
    _waypoints.add(new Location(x, y, z));
  }

  public void setWayPointDelay(int delay) {
    MOVE_DELAY = delay;
  }

  public void setManualSpawn() {
    _autospawn = false;
  }

  public void setBossSpawn() {
    _bossLair = true;
  }

  public boolean isAutoSpawn() {
    if (_bossLair) {
      return false;
    }

    return _autospawn;
  }

  public void spawn(int delay) {
    Lock shed = new ReentrantLock();
    shed.lock();
    try {
      if (_spawnTask != null) {
        return;
      }
      _spawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new RespawnTask(), delay);
    } finally {
      shed.unlock();
    }
  }

  public void notifyDeath() {
    Lock shed = new ReentrantLock();
    shed.lock();
    try
    {
      if ((_spawnTask != null) || (_bossLair))
      {
        return;
      }
      _spawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new RespawnTask(), _minRespawn);
    } finally {
      shed.unlock();
    }
  }

  public int[] getRandomPoint()
  {
    int x = 0;
    int y = 0;
    do
    {
      x = Rnd.get(_minX, _maxX);
      y = Rnd.get(_minY, _maxY);
    }

    while (!_p.contains(x, y));

    int[] rndPoint = new int[2];

    rndPoint[0] = x;
    rndPoint[1] = y;

    return rndPoint;
  }

  private class RespawnTask
    implements Runnable
  {
    public RespawnTask()
    {
    }

    public void run()
    {
      checkSpawns();
    }
  }
}