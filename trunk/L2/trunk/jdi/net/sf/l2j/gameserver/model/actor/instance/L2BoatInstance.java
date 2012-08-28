package net.sf.l2j.gameserver.model.actor.instance;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.Collection;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Character.MoveData;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.knownlist.BoatKnownList;
import net.sf.l2j.gameserver.model.actor.knownlist.CharKnownList;
import net.sf.l2j.gameserver.model.actor.position.ObjectPosition;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.OnVehicleCheckLocation;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.network.serverpackets.VehicleDeparture;
import net.sf.l2j.gameserver.network.serverpackets.VehicleInfo;
import net.sf.l2j.gameserver.templates.L2CharTemplate;
import net.sf.l2j.gameserver.templates.L2Weapon;

public class L2BoatInstance extends L2Character
{
  protected static final Logger _logBoat = Logger.getLogger(L2BoatInstance.class.getName());
  private String _name;
  protected L2BoatTrajet _t1;
  protected L2BoatTrajet _t2;
  protected int _cycle = 0;
  protected VehicleDeparture _vd = null;
  private Map<Integer, L2PcInstance> _inboat;
  public int _runstate = 0;

  private int lastx = -1;
  private int lasty = -1;
  protected boolean needOnVehicleCheckLocation = false;

  public L2BoatInstance(int objectId, L2CharTemplate template, String name)
  {
    super(objectId, template);
    super.setKnownList(new BoatKnownList(this));

    _name = name;
  }

  public void moveToLocation(int x, int y, int z, float speed)
  {
    int curX = getX();
    int curY = getY();
    int dx = x - curX;
    int dy = y - curY;
    double distance = Math.sqrt(dx * dx + dy * dy);

    double sin = dy / distance;
    double cos = dx / distance;
    L2Character.MoveData m = new L2Character.MoveData();

    int ticksToMove = (int)(10.0D * distance / speed);
    int heading = (int)(Math.atan2(-sin, -cos) * 10430.378350470453D);
    heading += 32768;
    getPosition().setHeading(heading);

    m._xDestination = x;
    m._yDestination = y;
    m._zDestination = z;
    m._heading = 0;
    m.onGeodataPathIndex = -1;
    m._moveStartTime = GameTimeController.getGameTicks();
    _move = m;
    GameTimeController.getInstance().registerMovingObject(this);
  }

  public void evtArrived()
  {
    if (_runstate != 0)
    {
      Boatrun bc = new Boatrun(_runstate, this);
      ThreadPoolManager.getInstance().scheduleGeneral(bc, 10L);
      _runstate = 0;
    }
  }

  public void sendVehicleDeparture(L2PcInstance activeChar)
  {
    if (_vd != null)
    {
      activeChar.sendPacket(_vd);
    }
  }

  public VehicleDeparture getVehicleDeparture() {
    return _vd;
  }

  public void beginCycle() {
    say(10);
    BoatCaptain bc = new BoatCaptain(1, this);
    ThreadPoolManager.getInstance().scheduleGeneral(bc, 300000L);
  }

  public void updatePeopleInTheBoat(int x, int y, int z)
  {
    if (_inboat != null)
    {
      boolean check = false;
      if ((lastx == -1) || (lasty == -1))
      {
        check = true;
        lastx = x;
        lasty = y;
      }
      else if ((x - lastx) * (x - lastx) + (y - lasty) * (y - lasty) > 2250000)
      {
        check = true;
        lastx = x;
        lasty = y;
      }
      for (int i = 0; i < _inboat.size(); i++)
      {
        L2PcInstance player = (L2PcInstance)_inboat.get(Integer.valueOf(i));
        if ((player != null) && (player.isInBoat()))
        {
          if (player.getBoat() == this)
          {
            player.getPosition().setXYZ(x, y, z);
            player.revalidateZone(false);
          }
        }
        if (check != true)
          continue;
        if ((needOnVehicleCheckLocation != true) || (player == null))
          continue;
        OnVehicleCheckLocation vcl = new OnVehicleCheckLocation(this, x, y, z);
        player.sendPacket(vcl);
      }
    }
  }

  public void begin()
  {
    if (_cycle == 1)
    {
      Collection knownPlayers = getKnownList().getKnownPlayers().values();
      int i;
      if ((knownPlayers != null) && (!knownPlayers.isEmpty()))
      {
        _inboat = new FastMap();
        i = 0;
        for (L2PcInstance player : knownPlayers)
        {
          if (player.isInBoat())
          {
            L2ItemInstance it = player.getInventory().getItemByItemId(_t1.idWTicket1);
            if ((it != null) && (it.getCount() >= 1))
            {
              player.getInventory().destroyItem("Boat", it.getObjectId(), 1, player, this);
              InventoryUpdate iu = new InventoryUpdate();
              iu.addModifiedItem(it);
              player.sendPacket(iu);
              _inboat.put(Integer.valueOf(i), player);
              i++;
            }
            else if ((it == null) && (_t1.idWTicket1 == 0))
            {
              _inboat.put(Integer.valueOf(i), player);
              i++;
            }
            else
            {
              player.teleToLocation(_t1.ntx1, _t1.nty1, _t1.ntz1, false);
            }
          }
        }
      }
      Boatrun bc = new Boatrun(0, this);
      ThreadPoolManager.getInstance().scheduleGeneral(bc, 0L);
    }
    else if (_cycle == 2)
    {
      Collection knownPlayers = getKnownList().getKnownPlayers().values();
      int i;
      if ((knownPlayers != null) && (!knownPlayers.isEmpty()))
      {
        _inboat = new FastMap();
        i = 0;
        for (L2PcInstance player : knownPlayers)
        {
          if (player.isInBoat())
          {
            L2ItemInstance it = player.getInventory().getItemByItemId(_t2.idWTicket1);
            if ((it != null) && (it.getCount() >= 1))
            {
              player.getInventory().destroyItem("Boat", it.getObjectId(), 1, player, this);
              InventoryUpdate iu = new InventoryUpdate();
              iu.addModifiedItem(it);
              player.sendPacket(iu);
              _inboat.put(Integer.valueOf(i), player);
              i++;
            }
            else if ((it == null) && (_t2.idWTicket1 == 0))
            {
              _inboat.put(Integer.valueOf(i), player);
              i++;
            }
            else
            {
              player.teleToLocation(_t2.ntx1, _t2.nty1, _t2.ntz1, false);
            }
          }
        }
      }

      Boatrun bc = new Boatrun(0, this);
      ThreadPoolManager.getInstance().scheduleGeneral(bc, 0L);
    }
  }

  public void say(int i)
  {
    Collection knownPlayers = getKnownList().getKnownPlayers().values();
    CreatureSay sm;
    PlaySound ps;
    CreatureSay sm;
    CreatureSay sm;
    CreatureSay sm;
    CreatureSay sm;
    switch (i)
    {
    case 10:
      CreatureSay sm;
      if (_cycle == 1)
      {
        sm = new CreatureSay(0, 1, _t1.npc1, _t1.sysmess10_1);
      }
      else
      {
        sm = new CreatureSay(0, 1, _t2.npc1, _t2.sysmess10_1);
      }
      ps = new PlaySound(0, "itemsound.ship_arrival_departure", 1, getObjectId(), getX(), getY(), getZ());
      if ((knownPlayers == null) || (knownPlayers.isEmpty()))
        return;
      for (L2PcInstance player : knownPlayers)
      {
        player.sendPacket(sm);
        player.sendPacket(ps);
      }
      break;
    case 5:
      if (_cycle == 1)
      {
        sm = new CreatureSay(0, 1, _t1.npc1, _t1.sysmess5_1);
      }
      else
      {
        sm = new CreatureSay(0, 1, _t2.npc1, _t2.sysmess5_1);
      }
      ps = new PlaySound(0, "itemsound.ship_5min", 1, getObjectId(), getX(), getY(), getZ());
      if ((knownPlayers == null) || (knownPlayers.isEmpty()))
        return;
      for (L2PcInstance player : knownPlayers)
      {
        player.sendPacket(sm);
        player.sendPacket(ps);
      }
      break;
    case 1:
      if (_cycle == 1)
      {
        sm = new CreatureSay(0, 1, _t1.npc1, _t1.sysmess1_1);
      }
      else
      {
        sm = new CreatureSay(0, 1, _t2.npc1, _t2.sysmess1_1);
      }
      ps = new PlaySound(0, "itemsound.ship_1min", 1, getObjectId(), getX(), getY(), getZ());
      if ((knownPlayers == null) || (knownPlayers.isEmpty()))
        return;
      for (L2PcInstance player : knownPlayers)
      {
        player.sendPacket(sm);
        player.sendPacket(ps);
      }
      break;
    case 0:
      if (_cycle == 1)
      {
        sm = new CreatureSay(0, 1, _t1.npc1, _t1.sysmess0_1);
      }
      else
      {
        sm = new CreatureSay(0, 1, _t2.npc1, _t2.sysmess0_1);
      }
      if ((knownPlayers == null) || (knownPlayers.isEmpty()))
        return;
      for (L2PcInstance player : knownPlayers)
      {
        player.sendPacket(sm);
      }
      break;
    case -1:
      if (_cycle == 1)
      {
        sm = new CreatureSay(0, 1, _t1.npc1, _t1.sysmessb_1);
      }
      else
      {
        sm = new CreatureSay(0, 1, _t2.npc1, _t2.sysmessb_1);
      }
      ps = new PlaySound(0, "itemsound.ship_arrival_departure", 1, getObjectId(), getX(), getY(), getZ());
      for (L2PcInstance player : knownPlayers)
      {
        player.sendPacket(sm);
        player.sendPacket(ps);
      }case 2:
    case 3:
    case 4:
    case 6:
    case 7:
    case 8:
    case 9:
    }
  }

  public void spawn() {
    Collection knownPlayers = getKnownList().getKnownPlayers().values();
    _cycle = 1;
    beginCycle();
    if ((knownPlayers == null) || (knownPlayers.isEmpty()))
      return;
    VehicleInfo vi = new VehicleInfo(this);
    for (L2PcInstance player : knownPlayers)
      player.sendPacket(vi);
  }

  public void setTrajet1(int idWaypoint1, int idWTicket1, int ntx1, int nty1, int ntz1, String idnpc1, String sysmess10_1, String sysmess5_1, String sysmess1_1, String sysmess0_1, String sysmessb_1)
  {
    _t1 = new L2BoatTrajet(idWaypoint1, idWTicket1, ntx1, nty1, ntz1, idnpc1, sysmess10_1, sysmess5_1, sysmess1_1, sysmess0_1, sysmessb_1, _name);
  }

  public void setTrajet2(int idWaypoint1, int idWTicket1, int ntx1, int nty1, int ntz1, String idnpc1, String sysmess10_1, String sysmess5_1, String sysmess1_1, String sysmess0_1, String sysmessb_1) {
    _t2 = new L2BoatTrajet(idWaypoint1, idWTicket1, ntx1, nty1, ntz1, idnpc1, sysmess10_1, sysmess5_1, sysmess1_1, sysmess0_1, sysmessb_1, _name);
  }

  public void updateAbnormalEffect()
  {
  }

  public L2ItemInstance getActiveWeaponInstance()
  {
    return null;
  }

  public L2Weapon getActiveWeaponItem()
  {
    return null;
  }

  public L2ItemInstance getSecondaryWeaponInstance()
  {
    return null;
  }

  public L2Weapon getSecondaryWeaponItem()
  {
    return null;
  }

  public int getLevel()
  {
    return 0;
  }

  public boolean isAutoAttackable(L2Character attacker)
  {
    return false;
  }

  class Boatrun
    implements Runnable
  {
    private int _state;
    private L2BoatInstance _boat;

    public Boatrun(int i, L2BoatInstance instance)
    {
      _state = i;
      _boat = instance;
    }

    public void run()
    {
      _boat._vd = null;
      _boat.needOnVehicleCheckLocation = false;
      if (_boat._cycle == 1)
      {
        int time = _boat._t1.state(_state, _boat);
        if (time > 0)
        {
          _state += 1;
          Boatrun bc = new Boatrun(L2BoatInstance.this, _state, _boat);
          ThreadPoolManager.getInstance().scheduleGeneral(bc, time);
        }
        else if (time == 0)
        {
          _boat._cycle = 2;
          _boat.say(10);
          L2BoatInstance.BoatCaptain bc = new L2BoatInstance.BoatCaptain(L2BoatInstance.this, 1, _boat);
          ThreadPoolManager.getInstance().scheduleGeneral(bc, 300000L);
        }
        else
        {
          _boat.needOnVehicleCheckLocation = true;
          _state += 1;
          _boat._runstate = _state;
        }
      }
      else if (_boat._cycle == 2)
      {
        int time = _boat._t2.state(_state, _boat);
        if (time > 0)
        {
          _state += 1;
          Boatrun bc = new Boatrun(L2BoatInstance.this, _state, _boat);
          ThreadPoolManager.getInstance().scheduleGeneral(bc, time);
        }
        else if (time == 0)
        {
          _boat._cycle = 1;
          _boat.say(10);
          L2BoatInstance.BoatCaptain bc = new L2BoatInstance.BoatCaptain(L2BoatInstance.this, 1, _boat);
          ThreadPoolManager.getInstance().scheduleGeneral(bc, 300000L);
        }
        else
        {
          _boat.needOnVehicleCheckLocation = true;
          _state += 1;
          _boat._runstate = _state;
        }
      }
    }
  }

  class BoatCaptain
    implements Runnable
  {
    private int _state;
    private L2BoatInstance _boat;

    public BoatCaptain(int i, L2BoatInstance instance)
    {
      _state = i;
      _boat = instance;
    }

    public void run()
    {
      BoatCaptain bc;
      switch (_state)
      {
      case 1:
        _boat.say(5);
        bc = new BoatCaptain(L2BoatInstance.this, 2, _boat);
        ThreadPoolManager.getInstance().scheduleGeneral(bc, 240000L);
        break;
      case 2:
        _boat.say(1);
        bc = new BoatCaptain(L2BoatInstance.this, 3, _boat);
        ThreadPoolManager.getInstance().scheduleGeneral(bc, 40000L);
        break;
      case 3:
        _boat.say(0);
        bc = new BoatCaptain(L2BoatInstance.this, 4, _boat);
        ThreadPoolManager.getInstance().scheduleGeneral(bc, 20000L);
        break;
      case 4:
        _boat.say(-1);
        _boat.begin();
      }
    }
  }

  private class L2BoatTrajet
  {
    private Map<Integer, L2BoatPoint> _path;
    public int idWaypoint1;
    public int idWTicket1;
    public int ntx1;
    public int nty1;
    public int ntz1;
    public int max;
    public String boatName;
    public String npc1;
    public String sysmess10_1;
    public String sysmess5_1;
    public String sysmess1_1;
    public String sysmessb_1;
    public String sysmess0_1;

    public L2BoatTrajet(int pIdWaypoint1, int pIdWTicket1, int pNtx1, int pNty1, int pNtz1, String pNpc1, String pSysmess10_1, String pSysmess5_1, String pSysmess1_1, String pSysmess0_1, String pSysmessb_1, String pBoatname)
    {
      idWaypoint1 = pIdWaypoint1;
      idWTicket1 = pIdWTicket1;
      ntx1 = pNtx1;
      nty1 = pNty1;
      ntz1 = pNtz1;
      npc1 = pNpc1;
      sysmess10_1 = pSysmess10_1;
      sysmess5_1 = pSysmess5_1;
      sysmess1_1 = pSysmess1_1;
      sysmessb_1 = pSysmessb_1;
      sysmess0_1 = pSysmess0_1;
      boatName = pBoatname;
      loadBoatPath();
    }

    public void parseLine(String line)
    {
      _path = new FastMap();
      StringTokenizer st = new StringTokenizer(line, ";");
      Integer.parseInt(st.nextToken());
      max = Integer.parseInt(st.nextToken());
      for (int i = 0; i < max; i++)
      {
        L2BoatPoint bp = new L2BoatPoint();
        bp.speed1 = Integer.parseInt(st.nextToken());
        bp.speed2 = Integer.parseInt(st.nextToken());
        bp.x = Integer.parseInt(st.nextToken());
        bp.y = Integer.parseInt(st.nextToken());
        bp.z = Integer.parseInt(st.nextToken());
        bp.time = Integer.parseInt(st.nextToken());
        _path.put(Integer.valueOf(i), bp);
      }
    }

    private void loadBoatPath()
    {
      LineNumberReader lnr = null;
      try
      {
        File doorData = new File(Config.DATAPACK_ROOT, "data/csv/boatpath.csv");
        lnr = new LineNumberReader(new BufferedReader(new FileReader(doorData)));

        String line = null;
        while ((line = lnr.readLine()) != null)
        {
          if ((line.trim().length() == 0) || (!line.startsWith(idWaypoint1 + ";"))) continue; parseLine(line);
          return;
        }
        L2BoatInstance._logBoat.warning("No path for boat " + boatName + " !!!");
      }
      catch (FileNotFoundException e1)
      {
        L2BoatInstance._logBoat.warning("boatpath.csv is missing in data folder");
      }
      catch (Exception e1)
      {
        L2BoatInstance._logBoat.warning("error while creating boat table " + e);
      }
      finally {
        try {
          lnr.close();
        }
        catch (Exception e1)
        {
        }
      }
    }

    public int state(int state, L2BoatInstance _boat)
    {
      if (state < max)
      {
        L2BoatPoint bp = (L2BoatPoint)_path.get(Integer.valueOf(state));
        double dx = bp.x - _boat.getX();
        double dy = bp.y - _boat.getX();
        double distance = Math.sqrt(dx * dx + dy * dy);

        double sin = dy / distance;
        double cos = dx / distance;

        int heading = (int)(Math.atan2(-sin, -cos) * 10430.378350470453D);
        heading += 32768;
        _boat.getPosition().setHeading(heading);

        _boat._vd = new VehicleDeparture(_boat, bp.speed1, bp.speed2, bp.x, bp.y, bp.z);

        _boat.moveToLocation(bp.x, bp.y, bp.z, bp.speed1);
        Collection knownPlayers = _boat.getKnownList().getKnownPlayers().values();
        if ((knownPlayers == null) || (knownPlayers.isEmpty()))
          return bp.time;
        for (L2PcInstance player : knownPlayers)
        {
          player.sendPacket(_boat._vd);
        }
        if (bp.time == 0)
        {
          bp.time = 1;
        }
        return bp.time;
      }

      return 0;
    }

    protected class L2BoatPoint
    {
      public int speed1;
      public int speed2;
      public int x;
      public int y;
      public int z;
      public int time;

      protected L2BoatPoint()
      {
      }
    }
  }
}