package net.sf.l2j.gameserver.instancemanager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.datatables.DoorTable;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SepulcherMonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SepulcherNpcInstance;
import net.sf.l2j.gameserver.model.quest.QuestState;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.util.Util;
import net.sf.l2j.mysql.Close;
import net.sf.l2j.mysql.Connect;
import net.sf.l2j.util.Rnd;
import net.sf.l2j.util.log.AbstractLogger;
import scripts.zone.type.L2BossZone;

public class FourSepulchersManager
{
  protected static final Logger _log = AbstractLogger.getLogger(FourSepulchersManager.class.getName());
  private static final String QUEST_ID = "q620_FourGoblets";
  private static final int ENTRANCE_PASS = 7075;
  private static final int USED_PASS = 7261;
  private static final int CHAPEL_KEY = 7260;
  private static final int ANTIQUE_BROOCH = 7262;
  protected boolean _firstTimeRun;
  protected boolean _inEntryTime = false;
  protected boolean _inWarmUpTime = false;
  protected boolean _inAttackTime = false;
  protected boolean _inCoolDownTime = false;

  protected ScheduledFuture<?> _changeCoolDownTimeTask = null;
  protected ScheduledFuture<?> _changeEntryTimeTask = null;
  protected ScheduledFuture<?> _changeWarmUpTimeTask = null;
  protected ScheduledFuture<?> _changeAttackTimeTask = null;
  protected ScheduledFuture<?> _onPartyAnnihilatedTask = null;

  private int[][] _startHallSpawn = { { 181632, -85587, -7218 }, { 179963, -88978, -7218 }, { 173217, -86132, -7218 }, { 175608, -82296, -7218 } };

  private int[][][] _shadowSpawnLoc = { { { 25339, 191231, -85574, -7216, 33380 }, { 25349, 189534, -88969, -7216, 32768 }, { 25346, 173195, -76560, -7215, 49277 }, { 25342, 175591, -72744, -7215, 49317 } }, { { 25342, 191231, -85574, -7216, 33380 }, { 25339, 189534, -88969, -7216, 32768 }, { 25349, 173195, -76560, -7215, 49277 }, { 25346, 175591, -72744, -7215, 49317 } }, { { 25346, 191231, -85574, -7216, 33380 }, { 25342, 189534, -88969, -7216, 32768 }, { 25339, 173195, -76560, -7215, 49277 }, { 25349, 175591, -72744, -7215, 49317 } }, { { 25349, 191231, -85574, -7216, 33380 }, { 25346, 189534, -88969, -7216, 32768 }, { 25342, 173195, -76560, -7215, 49277 }, { 25339, 175591, -72744, -7215, 49317 } } };

  protected FastMap<Integer, Boolean> _archonSpawned = new FastMap();
  protected FastMap<Integer, Boolean> _hallInUse = new FastMap();
  protected FastMap<Integer, int[]> _startHallSpawns = new FastMap();
  protected FastMap<Integer, Integer> _hallGateKeepers = new FastMap();
  protected FastMap<Integer, Integer> _keyBoxNpc = new FastMap();
  protected FastMap<Integer, Integer> _victim = new FastMap();
  protected FastMap<Integer, L2PcInstance> _challengers = new FastMap();
  protected FastMap<Integer, L2Spawn> _executionerSpawns = new FastMap();
  protected FastMap<Integer, L2Spawn> _keyBoxSpawns = new FastMap();
  protected FastMap<Integer, L2Spawn> _mysteriousBoxSpawns = new FastMap();
  protected FastMap<Integer, L2Spawn> _shadowSpawns = new FastMap();
  protected FastMap<Integer, FastList<L2Spawn>> _dukeFinalMobs = new FastMap();
  protected FastMap<Integer, FastList<L2SepulcherMonsterInstance>> _dukeMobs = new FastMap();
  protected FastMap<Integer, FastList<L2Spawn>> _emperorsGraveNpcs = new FastMap();
  protected FastMap<Integer, FastList<L2Spawn>> _magicalMonsters = new FastMap();
  protected FastMap<Integer, FastList<L2Spawn>> _physicalMonsters = new FastMap();
  protected FastMap<Integer, FastList<L2SepulcherMonsterInstance>> _viscountMobs = new FastMap();
  protected FastList<L2Spawn> _physicalSpawns;
  protected FastList<L2Spawn> _magicalSpawns;
  protected FastList<L2Spawn> _managers;
  protected FastList<L2Spawn> _dukeFinalSpawns;
  protected FastList<L2Spawn> _emperorsGraveSpawns;
  protected FastList<L2NpcInstance> _allMobs = new FastList();

  protected long _attackTimeEnd = 0L;
  protected long _coolDownTimeEnd = 0L;
  protected long _entryTimeEnd = 0L;
  protected long _warmUpTimeEnd = 0L;

  protected byte _newCycleMin = 55;
  private static FourSepulchersManager _instance;

  public static final FourSepulchersManager getInstance()
  {
    return _instance;
  }

  public static void init()
  {
    _instance = new FourSepulchersManager();
    _instance.load();
  }

  public void load()
  {
    if (_changeCoolDownTimeTask != null)
      _changeCoolDownTimeTask.cancel(true);
    if (_changeEntryTimeTask != null)
      _changeEntryTimeTask.cancel(true);
    if (_changeWarmUpTimeTask != null)
      _changeWarmUpTimeTask.cancel(true);
    if (_changeAttackTimeTask != null) {
      _changeAttackTimeTask.cancel(true);
    }
    _changeCoolDownTimeTask = null;
    _changeEntryTimeTask = null;
    _changeWarmUpTimeTask = null;
    _changeAttackTimeTask = null;

    _inEntryTime = false;
    _inWarmUpTime = false;
    _inAttackTime = false;
    _inCoolDownTime = false;

    _firstTimeRun = true;
    initFixedInfo();
    loadMysteriousBox();
    initKeyBoxSpawns();
    loadPhysicalMonsters();
    loadMagicalMonsters();
    initLocationShadowSpawns();
    initExecutionerSpawns();
    loadDukeMonsters();
    loadEmperorsGraveMonsters();
    spawnManagers();
    timeSelector();
  }

  protected void timeSelector()
  {
    timeCalculator();
    long currentTime = Calendar.getInstance().getTimeInMillis();

    if ((currentTime >= _coolDownTimeEnd) && (currentTime < _entryTimeEnd))
    {
      clean();
      _changeEntryTimeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ChangeEntryTime(), 0L);
      _log.info("FourSepulchersManager: Beginning in Entry time");
    }
    else if ((currentTime >= _entryTimeEnd) && (currentTime < _warmUpTimeEnd))
    {
      clean();
      _changeWarmUpTimeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ChangeWarmUpTime(), 0L);
      _log.info("FourSepulchersManager: Beginning in WarmUp time");
    }
    else if ((currentTime >= _warmUpTimeEnd) && (currentTime < _attackTimeEnd))
    {
      clean();
      _changeAttackTimeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ChangeAttackTime(), 0L);
      _log.info("FourSepulchersManager: Beginning in Attack time");
    }
    else
    {
      _changeCoolDownTimeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ChangeCoolDownTime(), 0L);
      _log.info("FourSepulchersManager: Beginning in Cooldown time");
    }
  }

  protected void timeCalculator()
  {
    Calendar tmp = Calendar.getInstance();
    if (tmp.get(12) < _newCycleMin)
      tmp.set(10, Calendar.getInstance().get(10) - 1);
    tmp.set(12, _newCycleMin);
    _coolDownTimeEnd = tmp.getTimeInMillis();
    _entryTimeEnd = (_coolDownTimeEnd + Config.FS_TIME_ENTRY * 60000);
    _warmUpTimeEnd = (_entryTimeEnd + Config.FS_TIME_WARMUP * 60000);
    _attackTimeEnd = (_warmUpTimeEnd + Config.FS_TIME_ATTACK * 60000);
  }

  public void clean()
  {
    for (int i = 31921; i < 31925; i++)
    {
      int[] Location = (int[])_startHallSpawns.get(Integer.valueOf(i));
      GrandBossManager.getInstance().getZone(Location[0], Location[1], Location[2]).oustAllPlayers();
    }

    deleteAllMobs();

    closeAllDoors();

    _hallInUse.clear();
    _hallInUse.put(Integer.valueOf(31921), Boolean.valueOf(false));
    _hallInUse.put(Integer.valueOf(31922), Boolean.valueOf(false));
    _hallInUse.put(Integer.valueOf(31923), Boolean.valueOf(false));
    _hallInUse.put(Integer.valueOf(31924), Boolean.valueOf(false));
    Iterator i$;
    if (_archonSpawned.size() != 0)
    {
      Set npcIdSet = _archonSpawned.keySet();
      for (i$ = npcIdSet.iterator(); i$.hasNext(); ) { int npcId = ((Integer)i$.next()).intValue();

        _archonSpawned.put(Integer.valueOf(npcId), Boolean.valueOf(false));
      }
    }
  }

  protected void spawnManagers()
  {
    _managers = new FastList();

    int i = 31921;
    for (; i <= 31924; i++)
    {
      if ((i < 31921) || (i > 31924))
        continue;
      L2NpcTemplate template1 = NpcTable.getInstance().getTemplate(i);
      if (template1 == null)
        continue;
      try
      {
        L2Spawn spawnDat = new L2Spawn(template1);

        spawnDat.setAmount(1);
        spawnDat.setRespawnDelay(60);
        switch (i)
        {
        case 31921:
          spawnDat.setLocx(181061);
          spawnDat.setLocy(-85595);
          spawnDat.setLocz(-7200);
          spawnDat.setHeading(-32584);
          break;
        case 31922:
          spawnDat.setLocx(179292);
          spawnDat.setLocy(-88981);
          spawnDat.setLocz(-7200);
          spawnDat.setHeading(-33272);
          break;
        case 31923:
          spawnDat.setLocx(173202);
          spawnDat.setLocy(-87004);
          spawnDat.setLocz(-7200);
          spawnDat.setHeading(-16248);
          break;
        case 31924:
          spawnDat.setLocx(175606);
          spawnDat.setLocy(-82853);
          spawnDat.setLocz(-7200);
          spawnDat.setHeading(-16248);
        }

        _managers.add(spawnDat);
        SpawnTable.getInstance().addNewSpawn(spawnDat, false);
        spawnDat.doSpawn();
        spawnDat.startRespawn();
        _log.info("FourSepulchersManager: spawned " + spawnDat.getTemplate().name);
      }
      catch (SecurityException e)
      {
        e.printStackTrace();
      }
      catch (ClassNotFoundException e)
      {
        e.printStackTrace();
      }
      catch (NoSuchMethodException e)
      {
        e.printStackTrace();
      }
    }
  }

  protected void initFixedInfo()
  {
    _startHallSpawns.put(Integer.valueOf(31921), _startHallSpawn[0]);
    _startHallSpawns.put(Integer.valueOf(31922), _startHallSpawn[1]);
    _startHallSpawns.put(Integer.valueOf(31923), _startHallSpawn[2]);
    _startHallSpawns.put(Integer.valueOf(31924), _startHallSpawn[3]);

    _hallInUse.put(Integer.valueOf(31921), Boolean.valueOf(false));
    _hallInUse.put(Integer.valueOf(31922), Boolean.valueOf(false));
    _hallInUse.put(Integer.valueOf(31923), Boolean.valueOf(false));
    _hallInUse.put(Integer.valueOf(31924), Boolean.valueOf(false));

    _hallGateKeepers.put(Integer.valueOf(31925), Integer.valueOf(25150012));
    _hallGateKeepers.put(Integer.valueOf(31926), Integer.valueOf(25150013));
    _hallGateKeepers.put(Integer.valueOf(31927), Integer.valueOf(25150014));
    _hallGateKeepers.put(Integer.valueOf(31928), Integer.valueOf(25150015));
    _hallGateKeepers.put(Integer.valueOf(31929), Integer.valueOf(25150016));
    _hallGateKeepers.put(Integer.valueOf(31930), Integer.valueOf(25150002));
    _hallGateKeepers.put(Integer.valueOf(31931), Integer.valueOf(25150003));
    _hallGateKeepers.put(Integer.valueOf(31932), Integer.valueOf(25150004));
    _hallGateKeepers.put(Integer.valueOf(31933), Integer.valueOf(25150005));
    _hallGateKeepers.put(Integer.valueOf(31934), Integer.valueOf(25150006));
    _hallGateKeepers.put(Integer.valueOf(31935), Integer.valueOf(25150032));
    _hallGateKeepers.put(Integer.valueOf(31936), Integer.valueOf(25150033));
    _hallGateKeepers.put(Integer.valueOf(31937), Integer.valueOf(25150034));
    _hallGateKeepers.put(Integer.valueOf(31938), Integer.valueOf(25150035));
    _hallGateKeepers.put(Integer.valueOf(31939), Integer.valueOf(25150036));
    _hallGateKeepers.put(Integer.valueOf(31940), Integer.valueOf(25150022));
    _hallGateKeepers.put(Integer.valueOf(31941), Integer.valueOf(25150023));
    _hallGateKeepers.put(Integer.valueOf(31942), Integer.valueOf(25150024));
    _hallGateKeepers.put(Integer.valueOf(31943), Integer.valueOf(25150025));
    _hallGateKeepers.put(Integer.valueOf(31944), Integer.valueOf(25150026));

    _keyBoxNpc.put(Integer.valueOf(18120), Integer.valueOf(31455));
    _keyBoxNpc.put(Integer.valueOf(18121), Integer.valueOf(31455));
    _keyBoxNpc.put(Integer.valueOf(18122), Integer.valueOf(31455));
    _keyBoxNpc.put(Integer.valueOf(18123), Integer.valueOf(31455));
    _keyBoxNpc.put(Integer.valueOf(18124), Integer.valueOf(31456));
    _keyBoxNpc.put(Integer.valueOf(18125), Integer.valueOf(31456));
    _keyBoxNpc.put(Integer.valueOf(18126), Integer.valueOf(31456));
    _keyBoxNpc.put(Integer.valueOf(18127), Integer.valueOf(31456));
    _keyBoxNpc.put(Integer.valueOf(18128), Integer.valueOf(31457));
    _keyBoxNpc.put(Integer.valueOf(18129), Integer.valueOf(31457));
    _keyBoxNpc.put(Integer.valueOf(18130), Integer.valueOf(31457));
    _keyBoxNpc.put(Integer.valueOf(18131), Integer.valueOf(31457));
    _keyBoxNpc.put(Integer.valueOf(18149), Integer.valueOf(31458));
    _keyBoxNpc.put(Integer.valueOf(18150), Integer.valueOf(31459));
    _keyBoxNpc.put(Integer.valueOf(18151), Integer.valueOf(31459));
    _keyBoxNpc.put(Integer.valueOf(18152), Integer.valueOf(31459));
    _keyBoxNpc.put(Integer.valueOf(18153), Integer.valueOf(31459));
    _keyBoxNpc.put(Integer.valueOf(18154), Integer.valueOf(31460));
    _keyBoxNpc.put(Integer.valueOf(18155), Integer.valueOf(31460));
    _keyBoxNpc.put(Integer.valueOf(18156), Integer.valueOf(31460));
    _keyBoxNpc.put(Integer.valueOf(18157), Integer.valueOf(31460));
    _keyBoxNpc.put(Integer.valueOf(18158), Integer.valueOf(31461));
    _keyBoxNpc.put(Integer.valueOf(18159), Integer.valueOf(31461));
    _keyBoxNpc.put(Integer.valueOf(18160), Integer.valueOf(31461));
    _keyBoxNpc.put(Integer.valueOf(18161), Integer.valueOf(31461));
    _keyBoxNpc.put(Integer.valueOf(18162), Integer.valueOf(31462));
    _keyBoxNpc.put(Integer.valueOf(18163), Integer.valueOf(31462));
    _keyBoxNpc.put(Integer.valueOf(18164), Integer.valueOf(31462));
    _keyBoxNpc.put(Integer.valueOf(18165), Integer.valueOf(31462));
    _keyBoxNpc.put(Integer.valueOf(18183), Integer.valueOf(31463));
    _keyBoxNpc.put(Integer.valueOf(18184), Integer.valueOf(31464));
    _keyBoxNpc.put(Integer.valueOf(18212), Integer.valueOf(31465));
    _keyBoxNpc.put(Integer.valueOf(18213), Integer.valueOf(31465));
    _keyBoxNpc.put(Integer.valueOf(18214), Integer.valueOf(31465));
    _keyBoxNpc.put(Integer.valueOf(18215), Integer.valueOf(31465));
    _keyBoxNpc.put(Integer.valueOf(18216), Integer.valueOf(31466));
    _keyBoxNpc.put(Integer.valueOf(18217), Integer.valueOf(31466));
    _keyBoxNpc.put(Integer.valueOf(18218), Integer.valueOf(31466));
    _keyBoxNpc.put(Integer.valueOf(18219), Integer.valueOf(31466));

    _victim.put(Integer.valueOf(18150), Integer.valueOf(18158));
    _victim.put(Integer.valueOf(18151), Integer.valueOf(18159));
    _victim.put(Integer.valueOf(18152), Integer.valueOf(18160));
    _victim.put(Integer.valueOf(18153), Integer.valueOf(18161));
    _victim.put(Integer.valueOf(18154), Integer.valueOf(18162));
    _victim.put(Integer.valueOf(18155), Integer.valueOf(18163));
    _victim.put(Integer.valueOf(18156), Integer.valueOf(18164));
    _victim.put(Integer.valueOf(18157), Integer.valueOf(18165));
  }

  private void loadMysteriousBox()
  {
    _mysteriousBoxSpawns.clear();

    Connect con = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      st = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay, key_npc_id FROM four_sepulchers_spawnlist Where spawntype = ? ORDER BY id");
      st.setInt(1, 0);
      rs = st.executeQuery();

      while (rs.next())
      {
        L2NpcTemplate template1 = NpcTable.getInstance().getTemplate(rs.getInt("npc_templateid"));
        if (template1 != null)
        {
          L2Spawn spawnDat = new L2Spawn(template1);
          spawnDat.setAmount(rs.getInt("count"));
          spawnDat.setLocx(rs.getInt("locx"));
          spawnDat.setLocy(rs.getInt("locy"));
          spawnDat.setLocz(rs.getInt("locz"));
          spawnDat.setHeading(rs.getInt("heading"));
          spawnDat.setRespawnDelay(rs.getInt("respawn_delay"));
          SpawnTable.getInstance().addNewSpawn(spawnDat, false);
          int keyNpcId = rs.getInt("key_npc_id");
          _mysteriousBoxSpawns.put(Integer.valueOf(keyNpcId), spawnDat);
          continue;
        }
        _log.warning("FourSepulchersManager.LoadMysteriousBox: Data missing in NPC table for ID: " + rs.getInt("npc_templateid") + ".");
      }
      _log.info("FourSepulchersManager: loaded " + _mysteriousBoxSpawns.size() + " Mysterious-Box spawns.");
    }
    catch (Exception e)
    {
      _log.warning("FourSepulchersManager.LoadMysteriousBox: Spawn could not be initialized: " + e);
    }
    finally
    {
      Close.CSR(con, st, rs);
    }
  }

  private void initKeyBoxSpawns()
  {
    for (Iterator i$ = _keyBoxNpc.keySet().iterator(); i$.hasNext(); ) { int keyNpcId = ((Integer)i$.next()).intValue();
      try
      {
        L2NpcTemplate template = NpcTable.getInstance().getTemplate(((Integer)_keyBoxNpc.get(Integer.valueOf(keyNpcId))).intValue());
        if (template != null)
        {
          L2Spawn spawnDat = new L2Spawn(template);
          spawnDat.setAmount(1);
          spawnDat.setLocx(0);
          spawnDat.setLocy(0);
          spawnDat.setLocz(0);
          spawnDat.setHeading(0);
          spawnDat.setRespawnDelay(3600);
          SpawnTable.getInstance().addNewSpawn(spawnDat, false);
          _keyBoxSpawns.put(Integer.valueOf(keyNpcId), spawnDat);
        }
        else
        {
          _log.warning("FourSepulchersManager.InitKeyBoxSpawns: Data missing in NPC table for ID: " + _keyBoxNpc.get(Integer.valueOf(keyNpcId)) + ".");
        }

      }
      catch (Exception e)
      {
        _log.warning("FourSepulchersManager.InitKeyBoxSpawns: Spawn could not be initialized: " + e);
      }
    }
  }

  private void loadPhysicalMonsters()
  {
    _physicalMonsters.clear();

    int loaded = 0;
    Connect con = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    PreparedStatement st2 = null;
    ResultSet rs2 = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      st = con.prepareStatement("SELECT Distinct key_npc_id FROM four_sepulchers_spawnlist Where spawntype = ? ORDER BY key_npc_id");
      st.setInt(1, 1);
      rs = st.executeQuery();
      while (rs.next())
      {
        int keyNpcId = rs.getInt("key_npc_id");

        st2 = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay, key_npc_id FROM four_sepulchers_spawnlist Where key_npc_id = ? and spawntype = ? ORDER BY id");
        st2.setInt(1, keyNpcId);
        st2.setInt(2, 1);
        rs2 = st2.executeQuery();

        _physicalSpawns = new FastList();

        while (rs2.next())
        {
          L2NpcTemplate template1 = NpcTable.getInstance().getTemplate(rs2.getInt("npc_templateid"));
          if (template1 != null)
          {
            L2Spawn spawnDat = new L2Spawn(template1);
            spawnDat.setAmount(rs2.getInt("count"));
            spawnDat.setLocx(rs2.getInt("locx"));
            spawnDat.setLocy(rs2.getInt("locy"));
            spawnDat.setLocz(rs2.getInt("locz"));
            spawnDat.setHeading(rs2.getInt("heading"));
            spawnDat.setRespawnDelay(rs2.getInt("respawn_delay"));
            SpawnTable.getInstance().addNewSpawn(spawnDat, false);
            _physicalSpawns.add(spawnDat);
            loaded++; continue;
          }

          _log.warning("FourSepulchersManager.LoadPhysicalMonsters: Data missing in NPC table for ID: " + rs2.getInt("npc_templateid") + ".");
        }
        Close.SR(st2, rs2);
        _physicalMonsters.put(Integer.valueOf(keyNpcId), _physicalSpawns);
      }
      _log.info("FourSepulchersManager: loaded " + loaded + " Physical type monsters spawns.");
    }
    catch (Exception e)
    {
      _log.warning("FourSepulchersManager.LoadPhysicalMonsters: Spawn could not be initialized: " + e);
    }
    finally
    {
      Close.SR(st2, rs2);
      Close.CSR(con, st, rs);
    }
  }

  private void loadMagicalMonsters()
  {
    _magicalMonsters.clear();

    int loaded = 0;
    Connect con = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    PreparedStatement st2 = null;
    ResultSet rs2 = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      st = con.prepareStatement("SELECT Distinct key_npc_id FROM four_sepulchers_spawnlist Where spawntype = ? ORDER BY key_npc_id");
      st.setInt(1, 2);
      rs = st.executeQuery();
      while (rs.next())
      {
        int keyNpcId = rs.getInt("key_npc_id");

        st2 = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay, key_npc_id FROM four_sepulchers_spawnlist Where key_npc_id = ? and spawntype = ? ORDER BY id");
        st2.setInt(1, keyNpcId);
        st2.setInt(2, 2);
        rs2 = st2.executeQuery();

        _magicalSpawns = new FastList();

        while (rs2.next())
        {
          L2NpcTemplate template1 = NpcTable.getInstance().getTemplate(rs2.getInt("npc_templateid"));
          if (template1 != null)
          {
            L2Spawn spawnDat = new L2Spawn(template1);
            spawnDat.setAmount(rs2.getInt("count"));
            spawnDat.setLocx(rs2.getInt("locx"));
            spawnDat.setLocy(rs2.getInt("locy"));
            spawnDat.setLocz(rs2.getInt("locz"));
            spawnDat.setHeading(rs2.getInt("heading"));
            spawnDat.setRespawnDelay(rs2.getInt("respawn_delay"));
            SpawnTable.getInstance().addNewSpawn(spawnDat, false);
            _magicalSpawns.add(spawnDat);
            loaded++; continue;
          }

          _log.warning("FourSepulchersManager.LoadMagicalMonsters: Data missing in NPC table for ID: " + rs2.getInt("npc_templateid") + ".");
        }

        Close.SR(st2, rs2);
        _magicalMonsters.put(Integer.valueOf(keyNpcId), _magicalSpawns);
      }
      _log.info("FourSepulchersManager: loaded " + loaded + " Magical type monsters spawns.");
    }
    catch (Exception e)
    {
      _log.warning("FourSepulchersManager.LoadMagicalMonsters: Spawn could not be initialized: " + e);
    }
    finally
    {
      Close.SR(st2, rs2);
      Close.CSR(con, st, rs);
    }
  }

  private void loadDukeMonsters()
  {
    _dukeFinalMobs.clear();
    _archonSpawned.clear();

    int loaded = 0;
    Connect con = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    PreparedStatement st2 = null;
    ResultSet rs2 = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      st = con.prepareStatement("SELECT Distinct key_npc_id FROM four_sepulchers_spawnlist Where spawntype = ? ORDER BY key_npc_id");
      st.setInt(1, 5);
      rs = st.executeQuery();
      while (rs.next())
      {
        int keyNpcId = rs.getInt("key_npc_id");

        st2 = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay, key_npc_id FROM four_sepulchers_spawnlist Where key_npc_id = ? and spawntype = ? ORDER BY id");
        st2.setInt(1, keyNpcId);
        st2.setInt(2, 5);
        rs2 = st2.executeQuery();

        _dukeFinalSpawns = new FastList();

        while (rs2.next())
        {
          L2NpcTemplate template1 = NpcTable.getInstance().getTemplate(rs2.getInt("npc_templateid"));
          if (template1 != null)
          {
            L2Spawn spawnDat = new L2Spawn(template1);
            spawnDat.setAmount(rs2.getInt("count"));
            spawnDat.setLocx(rs2.getInt("locx"));
            spawnDat.setLocy(rs2.getInt("locy"));
            spawnDat.setLocz(rs2.getInt("locz"));
            spawnDat.setHeading(rs2.getInt("heading"));
            spawnDat.setRespawnDelay(rs2.getInt("respawn_delay"));
            SpawnTable.getInstance().addNewSpawn(spawnDat, false);
            _dukeFinalSpawns.add(spawnDat);
            loaded++; continue;
          }

          _log.warning("FourSepulchersManager.LoadDukeMonsters: Data missing in NPC table for ID: " + rs2.getInt("npc_templateid") + ".");
        }

        Close.SR(st2, rs2);
        _dukeFinalMobs.put(Integer.valueOf(keyNpcId), _dukeFinalSpawns);
        _archonSpawned.put(Integer.valueOf(keyNpcId), Boolean.valueOf(false));
      }
      _log.info("FourSepulchersManager: loaded " + loaded + " Church of duke monsters spawns.");
    }
    catch (Exception e)
    {
      _log.warning("FourSepulchersManager.LoadDukeMonsters: Spawn could not be initialized: " + e);
    }
    finally
    {
      Close.SR(st2, rs2);
      Close.CSR(con, st, rs);
    }
  }

  private void loadEmperorsGraveMonsters()
  {
    _emperorsGraveNpcs.clear();

    int loaded = 0;
    Connect con = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    PreparedStatement st2 = null;
    ResultSet rs2 = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      st = con.prepareStatement("SELECT Distinct key_npc_id FROM four_sepulchers_spawnlist Where spawntype = ? ORDER BY key_npc_id");
      st.setInt(1, 6);
      rs = st.executeQuery();
      while (rs.next())
      {
        int keyNpcId = rs.getInt("key_npc_id");

        st2 = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay, key_npc_id FROM four_sepulchers_spawnlist Where key_npc_id = ? and spawntype = ? ORDER BY id");
        st2.setInt(1, keyNpcId);
        st2.setInt(2, 6);
        rs2 = st2.executeQuery();

        _emperorsGraveSpawns = new FastList();

        while (rs2.next())
        {
          L2NpcTemplate template1 = NpcTable.getInstance().getTemplate(rs2.getInt("npc_templateid"));
          if (template1 != null)
          {
            L2Spawn spawnDat = new L2Spawn(template1);
            spawnDat.setAmount(rs2.getInt("count"));
            spawnDat.setLocx(rs2.getInt("locx"));
            spawnDat.setLocy(rs2.getInt("locy"));
            spawnDat.setLocz(rs2.getInt("locz"));
            spawnDat.setHeading(rs2.getInt("heading"));
            spawnDat.setRespawnDelay(rs2.getInt("respawn_delay"));
            SpawnTable.getInstance().addNewSpawn(spawnDat, false);
            _emperorsGraveSpawns.add(spawnDat);
            loaded++; continue;
          }

          _log.warning("FourSepulchersManager.LoadEmperorsGraveMonsters: Data missing in NPC table for ID: " + rs2.getInt("npc_templateid") + ".");
        }

        Close.SR(st2, rs2);
        _emperorsGraveNpcs.put(Integer.valueOf(keyNpcId), _emperorsGraveSpawns);
      }
      _log.info("FourSepulchersManager: loaded " + loaded + " Emperor's grave NPC spawns.");
    }
    catch (Exception e)
    {
      _log.warning("FourSepulchersManager.LoadEmperorsGraveMonsters: Spawn could not be initialized: " + e);
    }
    finally
    {
      Close.SR(st2, rs2);
      Close.CSR(con, st, rs);
    }
  }

  protected void initLocationShadowSpawns()
  {
    int locNo = Rnd.get(4);
    int[] gateKeeper = { 31929, 31934, 31939, 31944 };

    _shadowSpawns.clear();

    for (int i = 0; i <= 3; i++)
    {
      L2NpcTemplate template = NpcTable.getInstance().getTemplate(_shadowSpawnLoc[locNo][i][0]);
      if (template != null)
      {
        try
        {
          L2Spawn spawnDat = new L2Spawn(template);
          spawnDat.setAmount(1);
          spawnDat.setLocx(_shadowSpawnLoc[locNo][i][1]);
          spawnDat.setLocy(_shadowSpawnLoc[locNo][i][2]);
          spawnDat.setLocz(_shadowSpawnLoc[locNo][i][3]);
          spawnDat.setHeading(_shadowSpawnLoc[locNo][i][4]);
          SpawnTable.getInstance().addNewSpawn(spawnDat, false);
          int keyNpcId = gateKeeper[i];
          _shadowSpawns.put(Integer.valueOf(keyNpcId), spawnDat);
        }
        catch (Exception e)
        {
          _log.log(Level.SEVERE, "Error on InitLocationShadowSpawns", e);
        }
      }
      else
      {
        _log.warning("FourSepulchersManager.InitLocationShadowSpawns: Data missing in NPC table for ID: " + _shadowSpawnLoc[locNo][i][0] + ".");
      }
    }
  }

  protected void initExecutionerSpawns()
  {
    for (Iterator i$ = _victim.keySet().iterator(); i$.hasNext(); ) { int keyNpcId = ((Integer)i$.next()).intValue();
      try
      {
        L2NpcTemplate template = NpcTable.getInstance().getTemplate(((Integer)_victim.get(Integer.valueOf(keyNpcId))).intValue());
        if (template != null)
        {
          L2Spawn spawnDat = new L2Spawn(template);
          spawnDat.setAmount(1);
          spawnDat.setLocx(0);
          spawnDat.setLocy(0);
          spawnDat.setLocz(0);
          spawnDat.setHeading(0);
          spawnDat.setRespawnDelay(3600);
          SpawnTable.getInstance().addNewSpawn(spawnDat, false);
          _executionerSpawns.put(Integer.valueOf(keyNpcId), spawnDat);
        }
        else
        {
          _log.warning("FourSepulchersManager.InitExecutionerSpawns: Data missing in NPC table for ID: " + _victim.get(Integer.valueOf(keyNpcId)) + ".");
        }

      }
      catch (Exception e)
      {
        _log.warning("FourSepulchersManager.InitExecutionerSpawns: Spawn could not be initialized: " + e);
      }
    }
  }

  public boolean isEntryTime()
  {
    return _inEntryTime;
  }

  public boolean isAttackTime()
  {
    return _inAttackTime;
  }

  public synchronized void tryEntry(L2NpcInstance npc, L2PcInstance player)
  {
    int npcId = npc.getNpcId();
    switch (npcId)
    {
    case 31921:
    case 31922:
    case 31923:
    case 31924:
      break;
    default:
      if (!player.isGM())
      {
        _log.warning("Player " + player.getName() + "(" + player.getObjectId() + ") tried to cheat in four sepulchers.");
        Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " tried to enter four sepulchers with invalid npc id.", Config.DEFAULT_PUNISH);
      }

      return;
    }

    if (((Boolean)_hallInUse.get(Integer.valueOf(npcId))).booleanValue())
    {
      showHtmlFile(player, npcId + "-FULL.htm", npc, null);
      return;
    }

    if (Config.FS_PARTY_MEMBER_COUNT > 1)
    {
      if ((!player.isInParty()) || (player.getParty().getMemberCount() < Config.FS_PARTY_MEMBER_COUNT))
      {
        showHtmlFile(player, npcId + "-SP.htm", npc, null);
        return;
      }

      if (!player.getParty().isLeader(player))
      {
        showHtmlFile(player, npcId + "-NL.htm", npc, null);
        return;
      }

      for (L2PcInstance mem : player.getParty().getPartyMembers())
      {
        QuestState qs = mem.getQuestState("q620_FourGoblets");
        if ((qs == null) || ((!qs.isStarted()) && (!qs.isCompleted())))
        {
          showHtmlFile(player, npcId + "-NS.htm", npc, mem);
          return;
        }
        if (mem.getInventory().getItemByItemId(7075) == null)
        {
          showHtmlFile(player, npcId + "-SE.htm", npc, mem);
          return;
        }

        if (player.getWeightPenalty() >= 3)
        {
          mem.sendPacket(Static.INVENTORY_LESS_THAN_80_PERCENT);
          return;
        }
      }
    }
    else if ((Config.FS_PARTY_MEMBER_COUNT <= 1) && (player.isInParty()))
    {
      if (!player.getParty().isLeader(player))
      {
        showHtmlFile(player, npcId + "-NL.htm", npc, null);
        return;
      }
      for (L2PcInstance mem : player.getParty().getPartyMembers())
      {
        QuestState qs = mem.getQuestState("q620_FourGoblets");
        if ((qs == null) || ((!qs.isStarted()) && (!qs.isCompleted())))
        {
          showHtmlFile(player, npcId + "-NS.htm", npc, mem);
          return;
        }
        if (mem.getInventory().getItemByItemId(7075) == null)
        {
          showHtmlFile(player, npcId + "-SE.htm", npc, mem);
          return;
        }

        if (player.getWeightPenalty() >= 3)
        {
          mem.sendPacket(Static.INVENTORY_LESS_THAN_80_PERCENT);
          return;
        }
      }
    }
    else
    {
      QuestState qs = player.getQuestState("q620_FourGoblets");
      if ((qs == null) || ((!qs.isStarted()) && (!qs.isCompleted())))
      {
        showHtmlFile(player, npcId + "-NS.htm", npc, player);
        return;
      }
      if (player.getInventory().getItemByItemId(7075) == null)
      {
        showHtmlFile(player, npcId + "-SE.htm", npc, player);
        return;
      }

      if (player.getWeightPenalty() >= 3)
      {
        player.sendPacket(Static.INVENTORY_LESS_THAN_80_PERCENT);
        return;
      }
    }

    if (!isEntryTime())
    {
      showHtmlFile(player, npcId + "-NE.htm", npc, null);
      return;
    }

    showHtmlFile(player, npcId + "-OK.htm", npc, null);

    entry(npcId, player);
  }

  private void entry(int npcId, L2PcInstance player)
  {
    int[] Location = (int[])_startHallSpawns.get(Integer.valueOf(npcId));

    if (Config.FS_PARTY_MEMBER_COUNT > 1)
    {
      List members = new FastList();
      for (L2PcInstance mem : player.getParty().getPartyMembers())
      {
        if ((!mem.isDead()) && (Util.checkIfInRange(700, player, mem, true)))
        {
          members.add(mem);
        }
      }

      for (L2PcInstance mem : members)
      {
        GrandBossManager.getInstance().getZone(Location[0], Location[1], Location[2]).allowPlayerEntry(mem, 30);
        int driftx = Rnd.get(-80, 80);
        int drifty = Rnd.get(-80, 80);
        mem.teleToLocation(Location[0] + driftx, Location[1] + drifty, Location[2]);
        mem.destroyItemByItemId("Quest", 7075, 1, mem, true);
        if (mem.getInventory().getItemByItemId(7262) == null)
        {
          mem.addItem("Quest", 7261, 1, mem, true);
        }

        L2ItemInstance hallsKey = mem.getInventory().getItemByItemId(7260);
        if (hallsKey != null)
        {
          mem.destroyItemByItemId("Quest", 7260, hallsKey.getCount(), mem, true);
        }
      }

      _challengers.remove(Integer.valueOf(npcId));
      _challengers.put(Integer.valueOf(npcId), player);

      _hallInUse.remove(Integer.valueOf(npcId));
      _hallInUse.put(Integer.valueOf(npcId), Boolean.valueOf(true));
    }
    else if ((Config.FS_PARTY_MEMBER_COUNT <= 1) && (player.isInParty()))
    {
      List members = new FastList();
      for (L2PcInstance mem : player.getParty().getPartyMembers())
      {
        if ((!mem.isDead()) && (Util.checkIfInRange(700, player, mem, true)))
        {
          members.add(mem);
        }
      }

      for (L2PcInstance mem : members)
      {
        GrandBossManager.getInstance().getZone(Location[0], Location[1], Location[2]).allowPlayerEntry(mem, 30);
        int driftx = Rnd.get(-80, 80);
        int drifty = Rnd.get(-80, 80);
        mem.teleToLocation(Location[0] + driftx, Location[1] + drifty, Location[2]);
        mem.destroyItemByItemId("Quest", 7075, 1, mem, true);
        if (mem.getInventory().getItemByItemId(7262) == null)
        {
          mem.addItem("Quest", 7261, 1, mem, true);
        }

        L2ItemInstance hallsKey = mem.getInventory().getItemByItemId(7260);
        if (hallsKey != null)
        {
          mem.destroyItemByItemId("Quest", 7260, hallsKey.getCount(), mem, true);
        }
      }

      _challengers.remove(Integer.valueOf(npcId));
      _challengers.put(Integer.valueOf(npcId), player);

      _hallInUse.remove(Integer.valueOf(npcId));
      _hallInUse.put(Integer.valueOf(npcId), Boolean.valueOf(true));
    }
    else
    {
      GrandBossManager.getInstance().getZone(Location[0], Location[1], Location[2]).allowPlayerEntry(player, 30);
      int driftx = Rnd.get(-80, 80);
      int drifty = Rnd.get(-80, 80);
      player.teleToLocation(Location[0] + driftx, Location[1] + drifty, Location[2]);
      player.destroyItemByItemId("Quest", 7075, 1, player, true);
      if (player.getInventory().getItemByItemId(7262) == null)
      {
        player.addItem("Quest", 7261, 1, player, true);
      }

      L2ItemInstance hallsKey = player.getInventory().getItemByItemId(7260);
      if (hallsKey != null)
      {
        player.destroyItemByItemId("Quest", 7260, hallsKey.getCount(), player, true);
      }

      _challengers.remove(Integer.valueOf(npcId));
      _challengers.put(Integer.valueOf(npcId), player);

      _hallInUse.remove(Integer.valueOf(npcId));
      _hallInUse.put(Integer.valueOf(npcId), Boolean.valueOf(true));
    }
  }

  public void spawnMysteriousBox(int npcId)
  {
    if (!isAttackTime()) {
      return;
    }
    L2Spawn spawnDat = (L2Spawn)_mysteriousBoxSpawns.get(Integer.valueOf(npcId));
    if (spawnDat != null)
    {
      _allMobs.add(spawnDat.doSpawn());
      spawnDat.stopRespawn();
    }
  }

  public void spawnMonster(int npcId)
  {
    if (!isAttackTime()) {
      return;
    }

    FastList mobs = new FastList();
    FastList monsterList;
    FastList monsterList;
    if (Rnd.get(2) == 0)
    {
      monsterList = (FastList)_physicalMonsters.get(Integer.valueOf(npcId));
    }
    else
    {
      monsterList = (FastList)_magicalMonsters.get(Integer.valueOf(npcId));
    }

    if (monsterList != null)
    {
      boolean spawnKeyBoxMob = false;
      boolean spawnedKeyBoxMob = false;

      for (L2Spawn spawnDat : monsterList)
      {
        if (spawnedKeyBoxMob)
        {
          spawnKeyBoxMob = false;
        }
        else
        {
          switch (npcId)
          {
          case 31469:
          case 31474:
          case 31479:
          case 31484:
            if (Rnd.get(48) != 0)
              break;
            spawnKeyBoxMob = true; break;
          default:
            spawnKeyBoxMob = false;
          }
        }

        L2SepulcherMonsterInstance mob = null;

        if (spawnKeyBoxMob)
        {
          try
          {
            L2NpcTemplate template = NpcTable.getInstance().getTemplate(18149);
            if (template != null)
            {
              L2Spawn keyBoxMobSpawn = new L2Spawn(template);
              keyBoxMobSpawn.setAmount(1);
              keyBoxMobSpawn.setLocx(spawnDat.getLocx());
              keyBoxMobSpawn.setLocy(spawnDat.getLocy());
              keyBoxMobSpawn.setLocz(spawnDat.getLocz());
              keyBoxMobSpawn.setHeading(spawnDat.getHeading());
              keyBoxMobSpawn.setRespawnDelay(3600);
              SpawnTable.getInstance().addNewSpawn(keyBoxMobSpawn, false);
              mob = (L2SepulcherMonsterInstance)keyBoxMobSpawn.doSpawn();
              keyBoxMobSpawn.stopRespawn();
            }
            else
            {
              _log.warning("FourSepulchersManager.SpawnMonster: Data missing in NPC table for ID: 18149");
            }
          }
          catch (Exception e)
          {
            _log.warning("FourSepulchersManager.SpawnMonster: Spawn could not be initialized: " + e);
          }

          spawnedKeyBoxMob = true;
        }
        else
        {
          mob = (L2SepulcherMonsterInstance)spawnDat.doSpawn();
          spawnDat.stopRespawn();
        }

        if (mob != null)
        {
          mob.mysteriousBoxId = npcId;
          switch (npcId)
          {
          case 31469:
          case 31472:
          case 31474:
          case 31477:
          case 31479:
          case 31482:
          case 31484:
          case 31487:
            mobs.add(mob);
          case 31470:
          case 31471:
          case 31473:
          case 31475:
          case 31476:
          case 31478:
          case 31480:
          case 31481:
          case 31483:
          case 31485:
          case 31486: } _allMobs.add(mob);
        }
      }

      switch (npcId)
      {
      case 31469:
      case 31474:
      case 31479:
      case 31484:
        _viscountMobs.put(Integer.valueOf(npcId), mobs);
        break;
      case 31472:
      case 31477:
      case 31482:
      case 31487:
        _dukeMobs.put(Integer.valueOf(npcId), mobs);
      case 31470:
      case 31471:
      case 31473:
      case 31475:
      case 31476:
      case 31478:
      case 31480:
      case 31481:
      case 31483:
      case 31485:
      case 31486: }  }  } 
  public synchronized boolean isViscountMobsAnnihilated(int npcId) { FastList mobs = (FastList)_viscountMobs.get(Integer.valueOf(npcId));

    if (mobs == null) {
      return true;
    }
    for (L2SepulcherMonsterInstance mob : mobs)
    {
      if (!mob.isDead()) {
        return false;
      }
    }
    return true;
  }

  public synchronized boolean isDukeMobsAnnihilated(int npcId)
  {
    FastList mobs = (FastList)_dukeMobs.get(Integer.valueOf(npcId));

    if (mobs == null) {
      return true;
    }
    for (L2SepulcherMonsterInstance mob : mobs)
    {
      if (!mob.isDead()) {
        return false;
      }
    }
    return true;
  }

  public void spawnKeyBox(L2NpcInstance activeChar)
  {
    if (!isAttackTime()) {
      return;
    }
    L2Spawn spawnDat = (L2Spawn)_keyBoxSpawns.get(Integer.valueOf(activeChar.getNpcId()));

    if (spawnDat != null)
    {
      spawnDat.setAmount(1);
      spawnDat.setLocx(activeChar.getX());
      spawnDat.setLocy(activeChar.getY());
      spawnDat.setLocz(activeChar.getZ());
      spawnDat.setHeading(activeChar.getHeading());
      spawnDat.setRespawnDelay(3600);
      _allMobs.add(spawnDat.doSpawn());
      spawnDat.stopRespawn();
    }
  }

  public void spawnExecutionerOfHalisha(L2NpcInstance activeChar)
  {
    if (!isAttackTime()) {
      return;
    }
    L2Spawn spawnDat = (L2Spawn)_executionerSpawns.get(Integer.valueOf(activeChar.getNpcId()));

    if (spawnDat != null)
    {
      spawnDat.setAmount(1);
      spawnDat.setLocx(activeChar.getX());
      spawnDat.setLocy(activeChar.getY());
      spawnDat.setLocz(activeChar.getZ());
      spawnDat.setHeading(activeChar.getHeading());
      spawnDat.setRespawnDelay(3600);
      _allMobs.add(spawnDat.doSpawn());
      spawnDat.stopRespawn();
    }
  }

  public void spawnArchonOfHalisha(int npcId)
  {
    if (!isAttackTime()) {
      return;
    }
    if (((Boolean)_archonSpawned.get(Integer.valueOf(npcId))).booleanValue()) {
      return;
    }
    FastList monsterList = (FastList)_dukeFinalMobs.get(Integer.valueOf(npcId));

    if (monsterList != null)
    {
      for (L2Spawn spawnDat : monsterList)
      {
        L2SepulcherMonsterInstance mob = (L2SepulcherMonsterInstance)spawnDat.doSpawn();
        spawnDat.stopRespawn();

        if (mob != null)
        {
          mob.mysteriousBoxId = npcId;
          _allMobs.add(mob);
        }
      }
      _archonSpawned.put(Integer.valueOf(npcId), Boolean.valueOf(true));
    }
  }

  public void spawnEmperorsGraveNpc(int npcId)
  {
    if (!isAttackTime()) {
      return;
    }
    FastList monsterList = (FastList)_emperorsGraveNpcs.get(Integer.valueOf(npcId));

    if (monsterList != null)
    {
      for (L2Spawn spawnDat : monsterList)
      {
        _allMobs.add(spawnDat.doSpawn());
        spawnDat.stopRespawn();
      }
    }
  }

  protected void locationShadowSpawns()
  {
    int locNo = Rnd.get(4);

    int[] gateKeeper = { 31929, 31934, 31939, 31944 };

    for (int i = 0; i <= 3; i++)
    {
      int keyNpcId = gateKeeper[i];
      L2Spawn spawnDat = (L2Spawn)_shadowSpawns.get(Integer.valueOf(keyNpcId));
      spawnDat.setLocx(_shadowSpawnLoc[locNo][i][1]);
      spawnDat.setLocy(_shadowSpawnLoc[locNo][i][2]);
      spawnDat.setLocz(_shadowSpawnLoc[locNo][i][3]);
      spawnDat.setHeading(_shadowSpawnLoc[locNo][i][4]);
      _shadowSpawns.put(Integer.valueOf(keyNpcId), spawnDat);
    }
  }

  public void spawnShadow(int npcId)
  {
    if (!isAttackTime()) {
      return;
    }
    L2Spawn spawnDat = (L2Spawn)_shadowSpawns.get(Integer.valueOf(npcId));
    if (spawnDat != null)
    {
      L2SepulcherMonsterInstance mob = (L2SepulcherMonsterInstance)spawnDat.doSpawn();
      spawnDat.stopRespawn();

      if (mob != null)
      {
        mob.mysteriousBoxId = npcId;
        _allMobs.add(mob);
      }
    }
  }

  public void deleteAllMobs()
  {
    for (L2NpcInstance mob : _allMobs)
    {
      try
      {
        mob.getSpawn().stopRespawn();
        mob.deleteMe();
      }
      catch (Exception e)
      {
        _log.log(Level.SEVERE, "FourSepulchersManager: Failed deleting mob.", e);
      }
    }
    _allMobs.clear();
  }

  protected void closeAllDoors()
  {
    for (Iterator i$ = _hallGateKeepers.values().iterator(); i$.hasNext(); ) { int doorId = ((Integer)i$.next()).intValue();
      try
      {
        L2DoorInstance door = DoorTable.getInstance().getDoor(Integer.valueOf(doorId));
        if (door != null)
        {
          door.closeMe();
        }
        else
          _log.warning("FourSepulchersManager: Attempted to close undefined door. doorId: " + doorId);
      }
      catch (Exception e)
      {
        _log.log(Level.SEVERE, "FourSepulchersManager: Failed closing door", e);
      }
    }
  }

  protected byte minuteSelect(byte min)
  {
    if (min % 5.0D != 0.0D)
    {
      switch (min)
      {
      case 6:
      case 7:
        min = 5;
        break;
      case 8:
      case 9:
      case 11:
      case 12:
        min = 10;
        break;
      case 13:
      case 14:
      case 16:
      case 17:
        min = 15;
        break;
      case 18:
      case 19:
      case 21:
      case 22:
        min = 20;
        break;
      case 23:
      case 24:
      case 26:
      case 27:
        min = 25;
        break;
      case 28:
      case 29:
      case 31:
      case 32:
        min = 30;
        break;
      case 33:
      case 34:
      case 36:
      case 37:
        min = 35;
        break;
      case 38:
      case 39:
      case 41:
      case 42:
        min = 40;
        break;
      case 43:
      case 44:
      case 46:
      case 47:
        min = 45;
        break;
      case 48:
      case 49:
      case 51:
      case 52:
        min = 50;
        break;
      case 53:
      case 54:
      case 56:
      case 57:
        min = 55;
      case 10:
      case 15:
      case 20:
      case 25:
      case 30:
      case 35:
      case 40:
      case 45:
      case 50:
      case 55: }  } return min;
  }

  public void managerSay(byte min)
  {
    String msg;
    String msg1;
    String msg2;
    if (_inAttackTime)
    {
      if (min < 5) {
        return;
      }
      min = minuteSelect(min);

      msg = min + " minute(s) have passed.";

      if (min == 90) {
        msg = "Game over. The teleport will appear momentarily";
      }
      for (L2Spawn temp : _managers)
      {
        if (temp == null)
        {
          _log.warning("FourSepulchersManager: managerSay(): manager is null");
          continue;
        }
        if (!(temp.getLastSpawn() instanceof L2SepulcherNpcInstance))
        {
          _log.warning("FourSepulchersManager: managerSay(): manager is not Sepulcher instance");
          continue;
        }

        if (!((Boolean)_hallInUse.get(Integer.valueOf(temp.getNpcid()))).booleanValue()) {
          continue;
        }
        ((L2SepulcherNpcInstance)temp.getLastSpawn()).sayString(msg, 1);
      }

    }
    else if (_inEntryTime)
    {
      msg1 = "You may now enter the Sepulcher";
      msg2 = "If you place your hand on the stone statue in front of each sepulcher, you will be able to enter";
      for (L2Spawn temp : _managers)
      {
        if (temp == null)
        {
          _log.warning("FourSepulchersManager: Something goes wrong in managerSay()...");
          continue;
        }
        if (!(temp.getLastSpawn() instanceof L2SepulcherNpcInstance))
        {
          _log.warning("FourSepulchersManager: Something goes wrong in managerSay()...");
          continue;
        }
        ((L2SepulcherNpcInstance)temp.getLastSpawn()).sayString(msg1, 1);
        ((L2SepulcherNpcInstance)temp.getLastSpawn()).sayString(msg2, 1);
      }
    }
  }

  public Map<Integer, Integer> getHallGateKeepers()
  {
    return _hallGateKeepers;
  }

  public void showHtmlFile(L2PcInstance player, String file, L2NpcInstance npc, L2PcInstance member)
  {
    NpcHtmlMessage html = NpcHtmlMessage.id(npc.getObjectId());
    html.setFile("data/html/SepulcherNpc/" + file);
    if (member != null)
      html.replace("%member%", member.getName());
    player.sendPacket(html);
  }

  protected class ChangeCoolDownTime
    implements Runnable
  {
    protected ChangeCoolDownTime()
    {
    }

    public void run()
    {
      _inEntryTime = false;
      _inWarmUpTime = false;
      _inAttackTime = false;
      _inCoolDownTime = true;

      clean();

      Calendar time = Calendar.getInstance();

      if ((Calendar.getInstance().get(12) > _newCycleMin) && (!_firstTimeRun))
        time.set(10, Calendar.getInstance().get(10) + 1);
      time.set(12, _newCycleMin);
      FourSepulchersManager._log.info("FourSepulchersManager: Entry time: " + time.getTime());
      if (_firstTimeRun) {
        _firstTimeRun = false;
      }

      long interval = time.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
      _changeEntryTimeTask = ThreadPoolManager.getInstance().scheduleGeneral(new FourSepulchersManager.ChangeEntryTime(FourSepulchersManager.this), interval);

      if (_changeCoolDownTimeTask != null)
      {
        _changeCoolDownTimeTask.cancel(true);
        _changeCoolDownTimeTask = null;
      }
    }
  }

  protected class ChangeAttackTime
    implements Runnable
  {
    protected ChangeAttackTime()
    {
    }

    public void run()
    {
      _inEntryTime = false;
      _inWarmUpTime = false;
      _inAttackTime = true;
      _inCoolDownTime = false;

      locationShadowSpawns();

      spawnMysteriousBox(31921);
      spawnMysteriousBox(31922);
      spawnMysteriousBox(31923);
      spawnMysteriousBox(31924);

      if (!_firstTimeRun)
      {
        _warmUpTimeEnd = Calendar.getInstance().getTimeInMillis();
      }

      long interval = 0L;

      if (_firstTimeRun)
      {
        for (double min = Calendar.getInstance().get(12); min < _newCycleMin; min += 1.0D)
        {
          if (min % 5.0D != 0.0D)
            continue;
          FourSepulchersManager._log.info(Calendar.getInstance().getTime() + " Atk announce scheduled to " + min + " minute of this hour.");
          Calendar inter = Calendar.getInstance();
          inter.set(12, (int)min);
          ThreadPoolManager.getInstance().scheduleGeneral(new FourSepulchersManager.ManagerSay(FourSepulchersManager.this), inter.getTimeInMillis() - Calendar.getInstance().getTimeInMillis());

          break;
        }
      }
      else
      {
        ThreadPoolManager.getInstance().scheduleGeneral(new FourSepulchersManager.ManagerSay(FourSepulchersManager.this), 302000L);
      }

      if (_firstTimeRun)
        interval = _attackTimeEnd - Calendar.getInstance().getTimeInMillis();
      else
        interval = Config.FS_TIME_ATTACK * 60000;
      _changeCoolDownTimeTask = ThreadPoolManager.getInstance().scheduleGeneral(new FourSepulchersManager.ChangeCoolDownTime(FourSepulchersManager.this), interval);

      if (_changeAttackTimeTask != null)
      {
        _changeAttackTimeTask.cancel(true);
        _changeAttackTimeTask = null;
      }
    }
  }

  protected class ChangeWarmUpTime
    implements Runnable
  {
    protected ChangeWarmUpTime()
    {
    }

    public void run()
    {
      _inEntryTime = true;
      _inWarmUpTime = false;
      _inAttackTime = false;
      _inCoolDownTime = false;

      long interval = 0L;

      if (_firstTimeRun)
        interval = _warmUpTimeEnd - Calendar.getInstance().getTimeInMillis();
      else
        interval = Config.FS_TIME_WARMUP * 60000;
      _changeAttackTimeTask = ThreadPoolManager.getInstance().scheduleGeneral(new FourSepulchersManager.ChangeAttackTime(FourSepulchersManager.this), interval);

      if (_changeWarmUpTimeTask != null)
      {
        _changeWarmUpTimeTask.cancel(true);
        _changeWarmUpTimeTask = null;
      }
    }
  }

  protected class ChangeEntryTime
    implements Runnable
  {
    protected ChangeEntryTime()
    {
    }

    public void run()
    {
      _inEntryTime = true;
      _inWarmUpTime = false;
      _inAttackTime = false;
      _inCoolDownTime = false;

      long interval = 0L;

      if (_firstTimeRun)
        interval = _entryTimeEnd - Calendar.getInstance().getTimeInMillis();
      else {
        interval = Config.FS_TIME_ENTRY * 60000;
      }

      ThreadPoolManager.getInstance().scheduleGeneral(new FourSepulchersManager.ManagerSay(FourSepulchersManager.this), 0L);
      _changeWarmUpTimeTask = ThreadPoolManager.getInstance().scheduleEffect(new FourSepulchersManager.ChangeWarmUpTime(FourSepulchersManager.this), interval);
      if (_changeEntryTimeTask != null)
      {
        _changeEntryTimeTask.cancel(true);
        _changeEntryTimeTask = null;
      }
    }
  }

  protected class ManagerSay
    implements Runnable
  {
    protected ManagerSay()
    {
    }

    public void run()
    {
      if (_inAttackTime)
      {
        Calendar tmp = Calendar.getInstance();
        tmp.setTimeInMillis(Calendar.getInstance().getTimeInMillis() - _warmUpTimeEnd);
        if (tmp.get(12) + 5 < Config.FS_TIME_ATTACK)
        {
          managerSay((byte)tmp.get(12));

          ThreadPoolManager.getInstance().scheduleGeneral(new ManagerSay(FourSepulchersManager.this), 300000L);
        }
        else if (tmp.get(12) + 5 >= Config.FS_TIME_ATTACK)
        {
          managerSay(90);
        }
      }
      else if (_inEntryTime) {
        managerSay(0);
      }
    }
  }
}