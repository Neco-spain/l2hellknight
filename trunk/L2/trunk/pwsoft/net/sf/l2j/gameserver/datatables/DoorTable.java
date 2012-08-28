package net.sf.l2j.gameserver.datatables;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import javolution.util.FastMap;
import javolution.util.FastTable;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.ObjectKnownList;
import net.sf.l2j.gameserver.model.entity.ClanHall;
import net.sf.l2j.gameserver.pathfinding.AbstractNodeLoc;
import net.sf.l2j.gameserver.templates.L2CharTemplate;
import net.sf.l2j.gameserver.templates.StatsSet;
import net.sf.l2j.util.log.AbstractLogger;

public class DoorTable
{
  private static Logger _log = AbstractLogger.getLogger(DoorTable.class.getName());
  private FastMap<Integer, L2DoorInstance> _staticItems;
  private static FastTable<String> _castleDoors = new FastTable();
  private static boolean _loaded = false;
  private static DoorTable _instance;
  private boolean _initialized = true;

  public static DoorTable getInstance()
  {
    if (_instance == null) {
      _instance = new DoorTable();
    }
    return _instance;
  }

  public DoorTable()
  {
    _staticItems = new FastMap().shared("DoorTable._staticItems");
  }

  public void reloadAll()
  {
    respawn();
  }

  public void respawn()
  {
    _staticItems = null;
    _instance = null;
    _instance = new DoorTable();
  }

  public void parseData()
  {
    LineNumberReader lnr = null;
    try
    {
      File doorData = new File(Config.DATAPACK_ROOT, "data/door.csv");
      lnr = new LineNumberReader(new BufferedReader(new FileReader(doorData)));

      String line = null;

      while ((line = lnr.readLine()) != null)
      {
        if ((line.trim().length() == 0) || (line.startsWith("#"))) {
          continue;
        }
        L2DoorInstance door = parseList(line);
        _staticItems.put(Integer.valueOf(door.getDoorId()), door);
        door.spawnMe(door.getX(), door.getY(), door.getZ());
        ClanHall clanhall = ClanHallManager.getInstance().getNearbyClanHall(door.getX(), door.getY(), 500);
        if (clanhall != null)
        {
          clanhall.getDoors().add(door);
          door.setClanHall(clanhall);
        }

      }

    }
    catch (FileNotFoundException e1)
    {
      _initialized = false;
      _log.warning("door.csv is missing in data folder");
    }
    catch (IOException e1)
    {
      _initialized = false;
      _log.warning("error while creating door table " + e);
    }
    finally {
      try {
        lnr.close(); } catch (Exception e1) {
      }
    }
    _log.config("Loading DoorTable... total " + _staticItems.size() + " Doors.");
  }

  public static L2DoorInstance parseList(String line)
  {
    StringTokenizer st = new StringTokenizer(line, ";");

    String name = st.nextToken();
    int id = Integer.parseInt(st.nextToken());
    int x = Integer.parseInt(st.nextToken());
    int y = Integer.parseInt(st.nextToken());
    int z = Integer.parseInt(st.nextToken());
    int rangeXMin = Integer.parseInt(st.nextToken());
    int rangeYMin = Integer.parseInt(st.nextToken());
    int rangeZMin = Integer.parseInt(st.nextToken());
    int rangeXMax = Integer.parseInt(st.nextToken());
    int rangeYMax = Integer.parseInt(st.nextToken());
    int rangeZMax = Integer.parseInt(st.nextToken());
    int hp = Integer.parseInt(st.nextToken());
    int pdef = Integer.parseInt(st.nextToken());
    int mdef = Integer.parseInt(st.nextToken());
    boolean unlockable = false;

    if (st.hasMoreTokens())
      unlockable = Boolean.parseBoolean(st.nextToken());
    boolean autoOpen = false;
    if (st.hasMoreTokens()) {
      autoOpen = Boolean.parseBoolean(st.nextToken());
    }
    if (rangeXMin > rangeXMax) _log.severe("Error in door data, ID:" + id);
    if (rangeYMin > rangeYMax) _log.severe("Error in door data, ID:" + id);
    if (rangeZMin > rangeZMax) _log.severe("Error in door data, ID:" + id);
    int collisionRadius;
    int collisionRadius;
    if (rangeXMax - rangeXMin > rangeYMax - rangeYMin)
      collisionRadius = rangeYMax - rangeYMin;
    else {
      collisionRadius = rangeXMax - rangeXMin;
    }
    StatsSet npcDat = new StatsSet();
    npcDat.set("npcId", id);
    npcDat.set("level", 0);
    npcDat.set("jClass", "door");

    npcDat.set("baseSTR", 0);
    npcDat.set("baseCON", 0);
    npcDat.set("baseDEX", 0);
    npcDat.set("baseINT", 0);
    npcDat.set("baseWIT", 0);
    npcDat.set("baseMEN", 0);

    npcDat.set("baseShldDef", 0);
    npcDat.set("baseShldRate", 0);
    npcDat.set("baseAccCombat", 38);
    npcDat.set("baseEvasRate", 38);
    npcDat.set("baseCritRate", 38);

    npcDat.set("collision_radius", collisionRadius);
    npcDat.set("collision_height", rangeZMax - rangeZMin);
    npcDat.set("sex", "male");
    npcDat.set("type", "");
    npcDat.set("baseAtkRange", 0);
    npcDat.set("baseMpMax", 0);
    npcDat.set("baseCpMax", 0);
    npcDat.set("rewardExp", 0);
    npcDat.set("rewardSp", 0);
    npcDat.set("basePAtk", 0);
    npcDat.set("baseMAtk", 0);
    npcDat.set("basePAtkSpd", 0);
    npcDat.set("aggroRange", 0);
    npcDat.set("baseMAtkSpd", 0);
    npcDat.set("rhand", 0);
    npcDat.set("lhand", 0);
    npcDat.set("armor", 0);
    npcDat.set("baseWalkSpd", 0);
    npcDat.set("baseRunSpd", 0);
    npcDat.set("name", name);
    npcDat.set("baseHpMax", hp);
    npcDat.set("baseHpReg", 0.003000000026077032D);
    npcDat.set("baseMpReg", 0.003000000026077032D);
    npcDat.set("basePDef", pdef);
    npcDat.set("baseMDef", mdef);

    L2CharTemplate template = new L2CharTemplate(npcDat);
    L2DoorInstance door = new L2DoorInstance(IdFactory.getInstance().getNextId(), template, id, name, unlockable);
    door.setRange(rangeXMin, rangeYMin, rangeZMin, rangeXMax, rangeYMax, rangeZMax);
    try
    {
      door.setMapRegion(MapRegionTable.getInstance().getMapRegion(x, y));
    }
    catch (Exception e)
    {
      _log.severe("Error in door data, ID:" + id);
    }
    door.setCurrentHpMp(door.getMaxHp(), door.getMaxMp());
    door.setOpen(autoOpen);
    door.setXYZInvisible(x, y, z);

    return door;
  }

  public boolean isInitialized()
  {
    return _initialized;
  }

  public L2DoorInstance getDoor(Integer id)
  {
    return (L2DoorInstance)_staticItems.get(id);
  }

  public void putDoor(L2DoorInstance door)
  {
    _staticItems.put(Integer.valueOf(door.getDoorId()), door);
  }

  public L2DoorInstance[] getDoors()
  {
    L2DoorInstance[] _allTemplates = (L2DoorInstance[])_staticItems.values().toArray(new L2DoorInstance[_staticItems.size()]);
    return _allTemplates;
  }

  public void checkAutoOpen()
  {
    for (L2DoorInstance doorInst : getDoors())
    {
      if (doorInst.getDoorName().startsWith("goe")) {
        doorInst.setAutoActionDelay(420000);
      }
      else if (doorInst.getDoorName().startsWith("aden_tower"))
        doorInst.setAutoActionDelay(300000);
    }
  }

  public void checkDoorsBetween() {
    if (_loaded)
      return;
  }

  public boolean checkIfDoorsBetween(AbstractNodeLoc start, AbstractNodeLoc end)
  {
    return checkIfDoorsBetween(start.getX(), start.getY(), start.getZ(), end.getX(), end.getY(), end.getZ());
  }

  public boolean checkIfDoorsBetween(int x, int y, int z, int tx, int ty, int tz)
  {
    int region;
    try
    {
      region = MapRegionTable.getInstance().getMapRegion(x, y);
    }
    catch (Exception e)
    {
      return false;
    }

    if (intersectsBorders(x, y, z, tx, ty, tz)) {
      return true;
    }

    for (L2DoorInstance doorInst : getDoors())
    {
      if (doorInst == null) {
        continue;
      }
      if (doorInst.getMapRegion() != region) {
        continue;
      }
      if (doorInst.intersectsLine(x, y, z, tx, ty, tz)) {
        return true;
      }

    }

    return false;
  }

  public boolean checkIfDoorsBetween(L2Object obj, int x, int y, int z, int tx, int ty, int tz)
  {
    if (obj == null) {
      return false;
    }
    if (intersectsBorders(x, y, z, tx, ty, tz)) {
      return true;
    }

    for (L2DoorInstance door : obj.getKnownList().getKnownDoors())
    {
      if (door == null) {
        continue;
      }
      if (door.intersectsLine(x, y, z, tx, ty, tz))
        return true;
    }
    return false;
  }

  public boolean intersectsBorders(int x, int y, int z, int tx, int ty, int tz)
  {
    return CustomServerData.getInstance().intersectEventZone(x, y, z, tx, ty, tz);
  }
}