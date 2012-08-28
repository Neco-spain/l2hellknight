package net.sf.l2j.gameserver.instancemanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.actor.instance.L2BoatInstance;
import net.sf.l2j.gameserver.model.actor.position.ObjectPosition;
import net.sf.l2j.gameserver.templates.L2CharTemplate;
import net.sf.l2j.gameserver.templates.StatsSet;

public class BoatManager
{
  private static final Logger _log = Logger.getLogger(BoatManager.class.getName());
  private static BoatManager _instance;
  private Map<Integer, L2BoatInstance> _staticItems = new FastMap();
  private boolean _initialized;

  public static final BoatManager getInstance()
  {
    if (_instance == null)
    {
      System.out.println("Initializing BoatManager");
      _instance = new BoatManager();
      _instance.load();
    }
    return _instance;
  }

  private final void load()
  {
    _initialized = true;
    if (!Config.ALLOW_BOAT)
    {
      _initialized = false;
      return;
    }
    LineNumberReader lnr = null;
    try
    {
      File doorData = new File(Config.DATAPACK_ROOT, "data/csv/boat.csv");
      lnr = new LineNumberReader(new BufferedReader(new FileReader(doorData)));

      String line = null;
      while ((line = lnr.readLine()) != null)
      {
        if ((line.trim().length() == 0) || (line.startsWith("#")))
          continue;
        L2BoatInstance boat = parseLine(line);
        boat.spawn();
        _staticItems.put(Integer.valueOf(boat.getObjectId()), boat);
      }
    }
    catch (FileNotFoundException e1)
    {
      _initialized = false;
      _log.warning("boat.csv is missing in data folder");
    }
    catch (Exception e1)
    {
      _initialized = false;
      _log.warning("error while creating boat table " + e);
      e.printStackTrace();
    }
    finally {
      try {
        lnr.close();
      } catch (Exception e1) {
      }
    }
  }

  private L2BoatInstance parseLine(String line) {
    StringTokenizer st = new StringTokenizer(line, ";");

    String name = st.nextToken();
    int id = Integer.parseInt(st.nextToken());
    int xspawn = Integer.parseInt(st.nextToken());
    int yspawn = Integer.parseInt(st.nextToken());
    int zspawn = Integer.parseInt(st.nextToken());
    int heading = Integer.parseInt(st.nextToken());

    StatsSet npcDat = new StatsSet();
    npcDat.set("npcId", id);
    npcDat.set("level", 0);
    npcDat.set("jClass", "boat");

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

    npcDat.set("collision_radius", 0);
    npcDat.set("collision_height", 0);
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
    npcDat.set("baseHpMax", 50000);
    npcDat.set("baseHpReg", 0.003000000026077032D);
    npcDat.set("baseMpReg", 0.003000000026077032D);
    npcDat.set("basePDef", 100);
    npcDat.set("baseMDef", 100);
    L2CharTemplate template = new L2CharTemplate(npcDat);
    L2BoatInstance boat = new L2BoatInstance(IdFactory.getInstance().getNextId(), template, name);
    boat.getPosition().setHeading(heading);
    boat.setXYZ(xspawn, yspawn, zspawn);

    int IdWaypoint1 = Integer.parseInt(st.nextToken());
    int IdWTicket1 = Integer.parseInt(st.nextToken());
    int ntx1 = Integer.parseInt(st.nextToken());
    int nty1 = Integer.parseInt(st.nextToken());
    int ntz1 = Integer.parseInt(st.nextToken());
    String npc1 = st.nextToken();
    String mess10_1 = st.nextToken();
    String mess5_1 = st.nextToken();
    String mess1_1 = st.nextToken();
    String mess0_1 = st.nextToken();
    String messb_1 = st.nextToken();
    boat.setTrajet1(IdWaypoint1, IdWTicket1, ntx1, nty1, ntz1, npc1, mess10_1, mess5_1, mess1_1, mess0_1, messb_1);
    IdWaypoint1 = Integer.parseInt(st.nextToken());
    IdWTicket1 = Integer.parseInt(st.nextToken());
    ntx1 = Integer.parseInt(st.nextToken());
    nty1 = Integer.parseInt(st.nextToken());
    ntz1 = Integer.parseInt(st.nextToken());
    npc1 = st.nextToken();
    mess10_1 = st.nextToken();
    mess5_1 = st.nextToken();
    mess1_1 = st.nextToken();
    mess0_1 = st.nextToken();
    messb_1 = st.nextToken();
    boat.setTrajet2(IdWaypoint1, IdWTicket1, ntx1, nty1, ntz1, npc1, mess10_1, mess5_1, mess1_1, mess0_1, messb_1);
    return boat;
  }

  public L2BoatInstance GetBoat(int boatId)
  {
    if (_staticItems == null) _staticItems = new FastMap();
    return (L2BoatInstance)_staticItems.get(Integer.valueOf(boatId));
  }
}