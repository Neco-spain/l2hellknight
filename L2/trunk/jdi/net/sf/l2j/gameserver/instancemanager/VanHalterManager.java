package net.sf.l2j.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.datatables.DoorTable;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2GrandBossInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.MonsterKnownList;
import net.sf.l2j.gameserver.model.actor.knownlist.NpcKnownList;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.templates.StatsSet;
import net.sf.l2j.util.Rnd;

public class VanHalterManager
{
  protected static Logger _log = Logger.getLogger(VanHalterManager.class.getName());
  private static VanHalterManager _instance = new VanHalterManager();

  protected List<L2PcInstance> _PlayersInLair = new FastList();
  protected Map<Integer, List<L2PcInstance>> _BleedingPlayers = new FastMap();

  protected Map<Integer, L2Spawn> _MonsterSpawn = new FastMap();
  protected List<L2Spawn> _RoyalGuardSpawn = new FastList();
  protected List<L2Spawn> _RoyalGuardCaptainSpawn = new FastList();
  protected List<L2Spawn> _RoyalGuardHelperSpawn = new FastList();
  protected List<L2Spawn> _ToriolRevelationSpawn = new FastList();
  protected List<L2Spawn> _ToriolRevelationAlive = new FastList();
  protected List<L2Spawn> _GuardOfAltarSpawn = new FastList();
  protected Map<Integer, L2Spawn> _CameraMarkerSpawn = new FastMap();
  protected L2Spawn _RitualOfferingSpawn = null;
  protected L2Spawn _RitualSacrificeSpawn = null;
  protected L2Spawn _vanHalterSpawn = null;

  protected List<L2NpcInstance> _Monsters = new FastList();
  protected List<L2NpcInstance> _RoyalGuard = new FastList();
  protected List<L2NpcInstance> _RoyalGuardCaptain = new FastList();
  protected List<L2NpcInstance> _RoyalGuardHepler = new FastList();
  protected List<L2NpcInstance> _ToriolRevelation = new FastList();
  protected List<L2NpcInstance> _GuardOfAltar = new FastList();
  protected Map<Integer, L2NpcInstance> _CameraMarker = new FastMap();
  protected List<L2DoorInstance> _DoorOfAltar = new FastList();
  protected List<L2DoorInstance> _DoorOfSacrifice = new FastList();
  protected L2NpcInstance _RitualOffering = null;
  protected L2NpcInstance _RitualSacrifice = null;
  protected L2GrandBossInstance _vanHalter = null;

  protected ScheduledFuture<?> _MovieTask = null;
  protected ScheduledFuture<?> _CloseDoorOfAltarTask = null;
  protected ScheduledFuture<?> _OpenDoorOfAltarTask = null;
  protected ScheduledFuture<?> _LockUpDoorOfAltarTask = null;
  protected ScheduledFuture<?> _CallRoyalGuardHelperTask = null;
  protected ScheduledFuture<?> _TimeUpTask = null;
  protected ScheduledFuture<?> _IntervalTask = null;
  protected ScheduledFuture<?> _HalterEscapeTask = null;
  protected ScheduledFuture<?> _SetBleedTask = null;

  boolean _isLocked = false;
  boolean _isHalterSpawned = false;
  boolean _isSacrificeSpawned = false;
  boolean _isCaptainSpawned = false;
  boolean _isHelperCalled = false;
  protected String _ZoneType;
  protected String _QuestName;
  protected StatsSet _StateSet;
  protected int _Alive;
  protected int _BossId = 29062;
  protected int _DummyId = 29059;

  public static VanHalterManager getInstance()
  {
    if (_instance == null) _instance = new VanHalterManager();

    return _instance;
  }

  public void init()
  {
    _PlayersInLair.clear();
    _ZoneType = "AltarofSacrifice";
    _QuestName = "vanhalter";
    _StateSet = GrandBossManager.getInstance().getStatsSet(_BossId);
    _Alive = GrandBossManager.getInstance().getBossStatus(_BossId);

    _isLocked = false;
    _isCaptainSpawned = false;
    _isHelperCalled = false;
    _isHalterSpawned = false;

    _DoorOfAltar.add(DoorTable.getInstance().getDoor(Integer.valueOf(19160014)));
    _DoorOfAltar.add(DoorTable.getInstance().getDoor(Integer.valueOf(19160015)));
    openDoorOfAltar(true);
    _DoorOfSacrifice.add(DoorTable.getInstance().getDoor(Integer.valueOf(19160016)));
    _DoorOfSacrifice.add(DoorTable.getInstance().getDoor(Integer.valueOf(19160017)));
    closeDoorOfSacrifice();

    loadRoyalGuard();
    loadToriolRevelation();
    loadRoyalGuardCaptain();
    loadRoyalGuardHelper();
    loadGuardOfAltar();
    loadVanHalter();
    loadRitualOffering();
    loadRitualSacrifice();

    _CameraMarkerSpawn.clear();
    try
    {
      L2NpcTemplate template1 = NpcTable.getInstance().getTemplate(_DummyId);
      L2Spawn tempSpawn = new L2Spawn(template1);
      tempSpawn.setLocx(-16397);
      tempSpawn.setLocy(-55200);
      tempSpawn.setLocz(-10449);
      tempSpawn.setHeading(16384);
      tempSpawn.setAmount(1);
      tempSpawn.setRespawnDelay(60000);
      SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
      _CameraMarkerSpawn.put(Integer.valueOf(1), tempSpawn);

      template1 = NpcTable.getInstance().getTemplate(_DummyId);
      tempSpawn = new L2Spawn(template1);
      tempSpawn.setLocx(-16397);
      tempSpawn.setLocy(-55200);
      tempSpawn.setLocz(-10051);
      tempSpawn.setHeading(16384);
      tempSpawn.setAmount(1);
      tempSpawn.setRespawnDelay(60000);
      SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
      _CameraMarkerSpawn.put(Integer.valueOf(2), tempSpawn);

      template1 = NpcTable.getInstance().getTemplate(_DummyId);
      tempSpawn = new L2Spawn(template1);
      tempSpawn.setLocx(-16397);
      tempSpawn.setLocy(-55200);
      tempSpawn.setLocz(-9741);
      tempSpawn.setHeading(16384);
      tempSpawn.setAmount(1);
      tempSpawn.setRespawnDelay(60000);
      SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
      _CameraMarkerSpawn.put(Integer.valueOf(3), tempSpawn);

      template1 = NpcTable.getInstance().getTemplate(_DummyId);
      tempSpawn = new L2Spawn(template1);
      tempSpawn.setLocx(-16397);
      tempSpawn.setLocy(-55200);
      tempSpawn.setLocz(-9394);
      tempSpawn.setHeading(16384);
      tempSpawn.setAmount(1);
      tempSpawn.setRespawnDelay(60000);
      SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
      _CameraMarkerSpawn.put(Integer.valueOf(4), tempSpawn);

      template1 = NpcTable.getInstance().getTemplate(_DummyId);
      tempSpawn = new L2Spawn(template1);
      tempSpawn.setLocx(-16397);
      tempSpawn.setLocy(-55197);
      tempSpawn.setLocz(-8739);
      tempSpawn.setHeading(16384);
      tempSpawn.setAmount(1);
      tempSpawn.setRespawnDelay(60000);
      SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
      _CameraMarkerSpawn.put(Integer.valueOf(5), tempSpawn);
    }
    catch (Exception e)
    {
      _log.warning("VanHalterManager : " + e.getMessage());
    }

    if (_SetBleedTask != null) _SetBleedTask.cancel(true);
    _SetBleedTask = ThreadPoolManager.getInstance().scheduleGeneral(new Bleeding(), 2000L);

    _log.info("VanHalterManager : State of High Priestess van Halter is " + _Alive + ".");
    if (_Alive == 3) {
      enterInterval();
    }
    else
    {
      setupAlter();
    }

    Date dt = new Date(_StateSet.getLong("respawn_time"));
    _log.info("VanHalterManager : Next spawn date of High Priestess van Halter is " + dt + ".");
    _log.info("VanHalterManager : Init VanHalterManager.");
  }

  protected void loadRoyalGuard()
  {
    _RoyalGuardSpawn.clear();

    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid between ? and ? ORDER BY id");
      statement.setInt(1, 22175);
      statement.setInt(2, 22176);
      ResultSet rset = statement.executeQuery();

      while (rset.next())
      {
        L2NpcTemplate template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
        if (template1 != null)
        {
          L2Spawn spawnDat = new L2Spawn(template1);
          spawnDat.setAmount(rset.getInt("count"));
          spawnDat.setLocx(rset.getInt("locx"));
          spawnDat.setLocy(rset.getInt("locy"));
          spawnDat.setLocz(rset.getInt("locz"));
          spawnDat.setHeading(rset.getInt("heading"));
          spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
          SpawnTable.getInstance().addNewSpawn(spawnDat, false);
          _RoyalGuardSpawn.add(spawnDat); continue;
        }

        _log.warning("VanHalterManager.loadRoyalGuard: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
      }

      rset.close();
      statement.close();
      _log.info("VanHalterManager: Loaded " + _RoyalGuardSpawn.size() + " Royal Guard spawn locations.");
    }
    catch (Exception e)
    {
      _log.warning("VanHalterManager.loadRoyalGuard: Spawn could not be initialized: " + e);
    }
    finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
  }

  protected void spawnRoyalGuard() {
    if (!_RoyalGuard.isEmpty()) deleteRoyalGuard();

    for (L2Spawn rgs : _RoyalGuardSpawn)
    {
      rgs.startRespawn();
      _RoyalGuard.add(rgs.doSpawn());
    }
  }

  protected void deleteRoyalGuard()
  {
    for (L2NpcInstance rg : _RoyalGuard)
    {
      rg.getSpawn().stopRespawn();
      rg.deleteMe();
    }

    _RoyalGuard.clear();
  }

  protected void loadToriolRevelation()
  {
    _ToriolRevelationSpawn.clear();

    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid between ? and ? ORDER BY id");
      statement.setInt(1, 32058);
      statement.setInt(2, 32068);
      ResultSet rset = statement.executeQuery();

      while (rset.next())
      {
        L2NpcTemplate template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
        if (template1 != null)
        {
          L2Spawn spawnDat = new L2Spawn(template1);
          spawnDat.setAmount(rset.getInt("count"));
          spawnDat.setLocx(rset.getInt("locx"));
          spawnDat.setLocy(rset.getInt("locy"));
          spawnDat.setLocz(rset.getInt("locz"));
          spawnDat.setHeading(rset.getInt("heading"));
          spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
          SpawnTable.getInstance().addNewSpawn(spawnDat, false);
          _ToriolRevelationSpawn.add(spawnDat); continue;
        }

        _log.warning("VanHalterManager.loadToriolRevelation: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
      }

      rset.close();
      statement.close();
      _log.info("VanHalterManager: Loaded " + _ToriolRevelationSpawn.size() + " Triol's Revelation spawn locations.");
    }
    catch (Exception e)
    {
      _log.warning("VanHalterManager.loadToriolRevelation: Spawn could not be initialized: " + e);
    }
    finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
  }

  protected void spawnToriolRevelation() {
    if (!_ToriolRevelation.isEmpty()) deleteToriolRevelation();

    for (L2Spawn trs : _ToriolRevelationSpawn)
    {
      trs.startRespawn();
      _ToriolRevelation.add(trs.doSpawn());

      if ((trs.getNpcid() != 32067) && (trs.getNpcid() != 32068))
        _ToriolRevelationAlive.add(trs);
    }
  }

  protected void deleteToriolRevelation()
  {
    for (L2NpcInstance tr : _ToriolRevelation)
    {
      tr.getSpawn().stopRespawn();
      tr.deleteMe();
    }

    _ToriolRevelation.clear();
    _BleedingPlayers.clear();
  }

  protected void loadRoyalGuardCaptain()
  {
    _RoyalGuardCaptainSpawn.clear();

    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid = ? ORDER BY id");
      statement.setInt(1, 22188);
      ResultSet rset = statement.executeQuery();

      while (rset.next())
      {
        L2NpcTemplate template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
        if (template1 != null)
        {
          L2Spawn spawnDat = new L2Spawn(template1);
          spawnDat.setAmount(rset.getInt("count"));
          spawnDat.setLocx(rset.getInt("locx"));
          spawnDat.setLocy(rset.getInt("locy"));
          spawnDat.setLocz(rset.getInt("locz"));
          spawnDat.setHeading(rset.getInt("heading"));
          spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
          SpawnTable.getInstance().addNewSpawn(spawnDat, false);
          _RoyalGuardCaptainSpawn.add(spawnDat); continue;
        }

        _log.warning("VanHalterManager.loadRoyalGuardCaptain: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
      }

      rset.close();
      statement.close();
      _log.info("VanHalterManager: Loaded " + _RoyalGuardCaptainSpawn.size() + " Royal Guard Captain spawn locations.");
    }
    catch (Exception e)
    {
      _log.warning("VanHalterManager.loadRoyalGuardCaptain: Spawn could not be initialized: " + e);
    }
    finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
  }

  protected void spawnRoyalGuardCaptain() {
    if (!_RoyalGuardCaptain.isEmpty()) deleteRoyalGuardCaptain();

    for (L2Spawn trs : _RoyalGuardCaptainSpawn)
    {
      trs.startRespawn();
      _RoyalGuardCaptain.add(trs.doSpawn());
    }
    _isCaptainSpawned = true;
  }

  protected void deleteRoyalGuardCaptain()
  {
    for (L2NpcInstance tr : _RoyalGuardCaptain)
    {
      tr.getSpawn().stopRespawn();
      tr.deleteMe();
    }

    _RoyalGuardCaptain.clear();
  }

  protected void loadRoyalGuardHelper()
  {
    _RoyalGuardHelperSpawn.clear();

    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid = ? ORDER BY id");
      statement.setInt(1, 22191);
      ResultSet rset = statement.executeQuery();

      while (rset.next())
      {
        L2NpcTemplate template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
        if (template1 != null)
        {
          L2Spawn spawnDat = new L2Spawn(template1);
          spawnDat.setAmount(rset.getInt("count"));
          spawnDat.setLocx(rset.getInt("locx"));
          spawnDat.setLocy(rset.getInt("locy"));
          spawnDat.setLocz(rset.getInt("locz"));
          spawnDat.setHeading(rset.getInt("heading"));
          spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
          SpawnTable.getInstance().addNewSpawn(spawnDat, false);
          _RoyalGuardHelperSpawn.add(spawnDat); continue;
        }

        _log.warning("VanHalterManager.loadRoyalGuardHelper: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
      }

      rset.close();
      statement.close();
      _log.info("VanHalterManager: Loaded " + _RoyalGuardHelperSpawn.size() + " Royal Guard Helper spawn locations.");
    }
    catch (Exception e)
    {
      _log.warning("VanHalterManager.loadRoyalGuardHelper: Spawn could not be initialized: " + e);
    }
    finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
  }

  protected void spawnRoyalGuardHepler() {
    for (L2Spawn trs : _RoyalGuardHelperSpawn)
    {
      trs.startRespawn();
      _RoyalGuardHepler.add(trs.doSpawn());
    }
  }

  protected void deleteRoyalGuardHepler()
  {
    for (L2NpcInstance tr : _RoyalGuardHepler)
    {
      tr.getSpawn().stopRespawn();
      tr.deleteMe();
    }

    _RoyalGuardHepler.clear();
  }

  protected void loadGuardOfAltar()
  {
    _GuardOfAltarSpawn.clear();

    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid = ? ORDER BY id");
      statement.setInt(1, 32051);
      ResultSet rset = statement.executeQuery();

      while (rset.next())
      {
        L2NpcTemplate template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
        if (template1 != null)
        {
          L2Spawn spawnDat = new L2Spawn(template1);
          spawnDat.setAmount(rset.getInt("count"));
          spawnDat.setLocx(rset.getInt("locx"));
          spawnDat.setLocy(rset.getInt("locy"));
          spawnDat.setLocz(rset.getInt("locz"));
          spawnDat.setHeading(rset.getInt("heading"));
          spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
          SpawnTable.getInstance().addNewSpawn(spawnDat, false);
          _GuardOfAltarSpawn.add(spawnDat); continue;
        }

        _log.warning("VanHalterManager.loadGuardOfAltar: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
      }

      rset.close();
      statement.close();
      _log.info("VanHalterManager: Loaded " + _GuardOfAltarSpawn.size() + " Guard Of Altar spawn locations.");
    }
    catch (Exception e)
    {
      _log.warning("VanHalterManager.loadGuardOfAltar: Spawn could not be initialized: " + e);
    }
    finally {
      try {
        con.close();
      } catch (Exception e) {
      }
    }
  }

  protected void spawnGuardOfAltar() {
    if (!_GuardOfAltar.isEmpty()) deleteGuardOfAltar();

    for (L2Spawn trs : _GuardOfAltarSpawn)
    {
      trs.startRespawn();
      _GuardOfAltar.add(trs.doSpawn());
    }
  }

  protected void deleteGuardOfAltar()
  {
    for (L2NpcInstance tr : _GuardOfAltar)
    {
      tr.getSpawn().stopRespawn();
      tr.deleteMe();
    }

    _GuardOfAltar.clear();
  }

  protected void loadVanHalter()
  {
    _vanHalterSpawn = null;

    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid = ? ORDER BY id");
      statement.setInt(1, 29062);
      ResultSet rset = statement.executeQuery();

      while (rset.next())
      {
        L2NpcTemplate template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
        if (template1 != null)
        {
          L2Spawn spawnDat = new L2Spawn(template1);
          spawnDat.setAmount(rset.getInt("count"));
          spawnDat.setLocx(rset.getInt("locx"));
          spawnDat.setLocy(rset.getInt("locy"));
          spawnDat.setLocz(rset.getInt("locz"));
          spawnDat.setHeading(rset.getInt("heading"));
          spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
          SpawnTable.getInstance().addNewSpawn(spawnDat, false);
          _vanHalterSpawn = spawnDat; continue;
        }

        _log.warning("VanHalterManager.loadVanHalter: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
      }

      rset.close();
      statement.close();
      _log.info("VanHalterManager: Loaded High Priestess van Halter spawn locations.");
    }
    catch (Exception e)
    {
      _log.warning("VanHalterManager.loadVanHalter: Spawn could not be initialized: " + e);
    }
    finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
  }

  protected void spawnVanHalter() {
    _vanHalter = ((L2GrandBossInstance)_vanHalterSpawn.doSpawn());
    _vanHalter.setIsImobilised(true);
    _vanHalter.setIsInvul(true);
    _isHalterSpawned = true;
  }

  protected void deleteVanHalter()
  {
    if (_vanHalter != null)
    {
      _vanHalter.setIsImobilised(false);
      _vanHalter.setIsInvul(false);
      _vanHalter.getSpawn().stopRespawn();
      _vanHalter.deleteMe();
    }
  }

  protected void loadRitualOffering()
  {
    _RitualOfferingSpawn = null;

    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid = ? ORDER BY id");
      statement.setInt(1, 32038);
      ResultSet rset = statement.executeQuery();

      while (rset.next())
      {
        L2NpcTemplate template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
        if (template1 != null)
        {
          L2Spawn spawnDat = new L2Spawn(template1);
          spawnDat.setAmount(rset.getInt("count"));
          spawnDat.setLocx(rset.getInt("locx"));
          spawnDat.setLocy(rset.getInt("locy"));
          spawnDat.setLocz(rset.getInt("locz"));
          spawnDat.setHeading(rset.getInt("heading"));
          spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
          SpawnTable.getInstance().addNewSpawn(spawnDat, false);
          _RitualOfferingSpawn = spawnDat; continue;
        }

        _log.warning("VanHalterManager.loadRitualOffering: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
      }

      rset.close();
      statement.close();
      _log.info("VanHalterManager: Loaded Ritual Offering spawn locations.");
    }
    catch (Exception e)
    {
      _log.warning("VanHalterManager.loadRitualOffering: Spawn could not be initialized: " + e);
    }
    finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
  }

  protected void spawnRitualOffering() {
    _RitualOffering = _RitualOfferingSpawn.doSpawn();
    _RitualOffering.setIsImobilised(true);
    _RitualOffering.setIsInvul(true);
    _RitualOffering.setIsParalyzed(true);
  }

  protected void deleteRitualOffering()
  {
    if (_RitualOffering != null)
    {
      _RitualOffering.setIsImobilised(false);
      _RitualOffering.setIsInvul(false);
      _RitualOffering.setIsParalyzed(false);
      _RitualOffering.getSpawn().stopRespawn();
      _RitualOffering.deleteMe();
    }
  }

  protected void loadRitualSacrifice()
  {
    _RitualSacrificeSpawn = null;

    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid = ? ORDER BY id");
      statement.setInt(1, 22195);
      ResultSet rset = statement.executeQuery();

      while (rset.next())
      {
        L2NpcTemplate template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
        if (template1 != null)
        {
          L2Spawn spawnDat = new L2Spawn(template1);
          spawnDat.setAmount(rset.getInt("count"));
          spawnDat.setLocx(rset.getInt("locx"));
          spawnDat.setLocy(rset.getInt("locy"));
          spawnDat.setLocz(rset.getInt("locz"));
          spawnDat.setHeading(rset.getInt("heading"));
          spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
          SpawnTable.getInstance().addNewSpawn(spawnDat, false);
          _RitualSacrificeSpawn = spawnDat; continue;
        }

        _log.warning("VanHalterManager.loadRitualSacrifice: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
      }

      rset.close();
      statement.close();
      _log.info("VanHalterManager: Loaded Ritual Sacrifice spawn locations.");
    }
    catch (Exception e)
    {
      _log.warning("VanHalterManager.loadRitualSacrifice: Spawn could not be initialized: " + e);
    }
    finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
  }

  protected void spawnRitualSacrifice() {
    _RitualSacrifice = _RitualSacrificeSpawn.doSpawn();
    _RitualSacrifice.setIsImobilised(true);
    _RitualSacrifice.setIsInvul(true);
    _isSacrificeSpawned = true;
  }

  protected void deleteRitualSacrifice()
  {
    if (!_isSacrificeSpawned) return;

    if (_RitualSacrifice != null)
    {
      _RitualSacrifice.getSpawn().stopRespawn();
      _RitualSacrifice.deleteMe();
    }
    _isSacrificeSpawned = false;
  }

  protected void spawnCameraMarker()
  {
    _CameraMarker.clear();
    for (int i = 1; i <= _CameraMarkerSpawn.size(); i++)
    {
      _CameraMarker.put(Integer.valueOf(i), ((L2Spawn)_CameraMarkerSpawn.get(Integer.valueOf(i))).doSpawn());
      ((L2NpcInstance)_CameraMarker.get(Integer.valueOf(i))).setIsImobilised(true);
      ((L2NpcInstance)_CameraMarker.get(Integer.valueOf(i))).getSpawn().stopRespawn();
    }
  }

  protected void deleteCameraMarker()
  {
    if (_CameraMarker.isEmpty()) return;

    for (int i = 1; i <= _CameraMarker.size(); i++)
    {
      ((L2NpcInstance)_CameraMarker.get(Integer.valueOf(i))).deleteMe();
    }
    _CameraMarker.clear();
  }

  public void intruderDetection(L2PcInstance intruder)
  {
    if (!_PlayersInLair.contains(intruder)) _PlayersInLair.add(intruder);
    if ((_LockUpDoorOfAltarTask == null) && (!_isLocked) && (_isCaptainSpawned))
    {
      _LockUpDoorOfAltarTask = ThreadPoolManager.getInstance().scheduleGeneral(new LockUpDoorOfAltar(), Config.HPH_TIMEOFLOCKUPDOOROFALTAR * 1000);
    }
  }

  protected void openDoorOfAltar(boolean loop)
  {
    for (L2DoorInstance door : _DoorOfAltar)
    {
      door.openMe();
    }

    if (loop)
    {
      _isLocked = false;

      if (_CloseDoorOfAltarTask != null) _CloseDoorOfAltarTask.cancel(true);
      _CloseDoorOfAltarTask = null;
      _CloseDoorOfAltarTask = ThreadPoolManager.getInstance().scheduleGeneral(new CloseDoorOfAltar(), Config.HPH_INTERVALOFDOOROFALTER * 1000);
    }
    else
    {
      if (_CloseDoorOfAltarTask != null) _CloseDoorOfAltarTask.cancel(true);
      _CloseDoorOfAltarTask = null;
    }
  }

  protected void closeDoorOfAltar(boolean loop)
  {
    for (L2DoorInstance door : _DoorOfAltar)
    {
      door.closeMe();
    }

    if (loop)
    {
      if (_OpenDoorOfAltarTask != null) _OpenDoorOfAltarTask.cancel(true);
      _OpenDoorOfAltarTask = null;
      _OpenDoorOfAltarTask = ThreadPoolManager.getInstance().scheduleGeneral(new OpenDoorOfAltar(), Config.HPH_INTERVALOFDOOROFALTER * 1000);
    }
    else
    {
      if (_OpenDoorOfAltarTask != null) _OpenDoorOfAltarTask.cancel(true);
      _OpenDoorOfAltarTask = null;
    }
  }

  protected void openDoorOfSacrifice()
  {
    for (L2DoorInstance door : _DoorOfSacrifice)
    {
      door.openMe();
    }
  }

  protected void closeDoorOfSacrifice()
  {
    for (L2DoorInstance door : _DoorOfSacrifice)
    {
      door.closeMe();
    }
  }

  public void checkToriolRevelationDestroy()
  {
    if (_isCaptainSpawned) return;

    boolean isToriolRevelationDestroyed = true;
    for (L2Spawn tra : _ToriolRevelationAlive)
    {
      if (!tra.getLastSpawn().isDead()) isToriolRevelationDestroyed = false;
    }

    if (isToriolRevelationDestroyed)
    {
      spawnRoyalGuardCaptain();
    }
  }

  public void checkRoyalGuardCaptainDestroy()
  {
    if (!_isHalterSpawned) return;

    deleteRoyalGuard();
    deleteRoyalGuardCaptain();
    spawnGuardOfAltar();
    openDoorOfSacrifice();

    for (L2NpcInstance goa : _GuardOfAltar)
    {
      cs = new CreatureSay(goa.getObjectId(), 1, goa.getName(), "The door of the 3rd floor in the altar was opened.");
      for (L2PcInstance pc : _PlayersInLair)
      {
        pc.sendPacket(cs);
      }
    }
    CreatureSay cs;
    updateKnownList(_vanHalter);
    _vanHalter.setIsImobilised(true);
    _vanHalter.setIsInvul(true);
    spawnCameraMarker();

    if (_TimeUpTask != null) _TimeUpTask.cancel(true);
    _TimeUpTask = null;

    _MovieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(1), Config.HPH_APPTIMEOFHALTER * 1000);
  }

  protected void updateKnownList(L2NpcInstance boss)
  {
    boss.getKnownList().getKnownPlayers().clear();
    for (L2PcInstance pc : _PlayersInLair)
    {
      boss.getKnownList().getKnownPlayers().put(Integer.valueOf(pc.getObjectId()), pc);
    }
  }

  protected void combatBeginning()
  {
    if (_TimeUpTask != null) _TimeUpTask.cancel(true);
    _TimeUpTask = ThreadPoolManager.getInstance().scheduleGeneral(new TimeUp(), Config.HPH_FIGHTTIMEOFHALTER * 1000);

    Map _targets = new FastMap();
    int i = 0;

    for (L2PcInstance pc : _vanHalter.getKnownList().getKnownPlayers().values())
    {
      i++;
      _targets.put(Integer.valueOf(i), pc);
    }

    _vanHalter.reduceCurrentHp(1.0D, (L2Character)_targets.get(Integer.valueOf(Rnd.get(1, i))));
  }

  public void callRoyalGuardHelper()
  {
    if (!_isHelperCalled)
    {
      _isHelperCalled = true;
      _HalterEscapeTask = ThreadPoolManager.getInstance().scheduleGeneral(new HalterEscape(), 500L);
      _CallRoyalGuardHelperTask = ThreadPoolManager.getInstance().scheduleGeneral(new CallRoyalGuardHelper(), 1000L);
    }
  }

  protected void addBleeding()
  {
    L2Skill bleed = SkillTable.getInstance().getInfo(4615, 12);

    for (L2NpcInstance tr : _ToriolRevelation)
    {
      if ((tr.getKnownList().getKnownPlayersInRadius(tr.getAggroRange()).size() == 0) || (tr.isDead()))
        continue;
      List bpc = new FastList();

      for (L2PcInstance pc : tr.getKnownList().getKnownPlayersInRadius(tr.getAggroRange()))
      {
        if (pc.getFirstEffect(bleed) == null)
        {
          bleed.getEffects(tr, pc);
          tr.broadcastPacket(new MagicSkillUser(tr, pc, bleed.getId(), 12, 1, 1));
        }

        bpc.add(pc);
      }
      _BleedingPlayers.remove(Integer.valueOf(tr.getNpcId()));
      _BleedingPlayers.put(Integer.valueOf(tr.getNpcId()), bpc);
    }
  }

  public void removeBleeding(int npcId)
  {
    if (_BleedingPlayers.get(Integer.valueOf(npcId)) == null) return;
    for (L2PcInstance pc : (FastList)_BleedingPlayers.get(Integer.valueOf(npcId)))
    {
      if (pc.getFirstEffect(L2Effect.EffectType.DMG_OVER_TIME) != null) pc.stopEffects(L2Effect.EffectType.DMG_OVER_TIME);
    }
    _BleedingPlayers.remove(Integer.valueOf(npcId));
  }

  public void enterInterval()
  {
    if ((_vanHalter != null) && (_vanHalter.isDead()))
    {
      _vanHalter.getSpawn().stopRespawn();
    }
    else
    {
      deleteVanHalter();
    }
    deleteRoyalGuardHepler();
    deleteRoyalGuardCaptain();
    deleteRoyalGuard();
    deleteRitualOffering();
    deleteRitualSacrifice();
    deleteGuardOfAltar();

    if (_Alive != 3)
    {
      _StateSet.set("respawn_time", Calendar.getInstance().getTimeInMillis() + Rnd.get(Config.HPH_FIXINTERVALOFHALTER, Config.HPH_FIXINTERVALOFHALTER + Config.HPH_RANDOMINTERVALOFHALTER) * 1000);
      _Alive = 3;
      GrandBossManager.getInstance().setBossStatus(_BossId, _Alive);
      GrandBossManager.getInstance().setStatsSet(_BossId, _StateSet);
      GrandBossManager.getInstance().save();
    }

    if (_CallRoyalGuardHelperTask != null) _CallRoyalGuardHelperTask.cancel(true);
    _CallRoyalGuardHelperTask = null;

    if (_CloseDoorOfAltarTask != null) _CloseDoorOfAltarTask.cancel(true);
    _CloseDoorOfAltarTask = null;

    if (_HalterEscapeTask != null) _HalterEscapeTask.cancel(true);
    _HalterEscapeTask = null;

    if (_LockUpDoorOfAltarTask != null) _LockUpDoorOfAltarTask.cancel(true);
    _LockUpDoorOfAltarTask = null;

    if (_MovieTask != null) _MovieTask.cancel(true);
    _MovieTask = null;

    if (_OpenDoorOfAltarTask != null) _OpenDoorOfAltarTask.cancel(true);
    _OpenDoorOfAltarTask = null;

    if (_TimeUpTask != null) _TimeUpTask.cancel(true);
    _TimeUpTask = null;

    if (_IntervalTask != null) _IntervalTask.cancel(true);
    _IntervalTask = null;

    _IntervalTask = ThreadPoolManager.getInstance().scheduleGeneral(new Interval(), GrandBossManager.getInstance().getInterval(_BossId));
    _log.info("VanHalterManager : Interval START.");
  }

  public void setupAlter()
  {
    deleteVanHalter();
    deleteToriolRevelation();
    deleteRoyalGuardHepler();
    deleteRoyalGuardCaptain();
    deleteRoyalGuard();
    deleteRitualSacrifice();
    deleteRitualOffering();
    deleteGuardOfAltar();
    deleteCameraMarker();

    _isLocked = false;
    _isCaptainSpawned = false;
    _isHelperCalled = false;
    _isHalterSpawned = false;

    closeDoorOfSacrifice();
    openDoorOfAltar(true);

    spawnToriolRevelation();
    spawnRoyalGuard();
    spawnRitualOffering();
    spawnVanHalter();

    _Alive = 1;
    GrandBossManager.getInstance().setBossStatus(_BossId, _Alive);
    GrandBossManager.getInstance().save();

    if (_CallRoyalGuardHelperTask != null) _CallRoyalGuardHelperTask.cancel(true);
    _CallRoyalGuardHelperTask = null;

    if (_CloseDoorOfAltarTask != null) _CloseDoorOfAltarTask.cancel(true);
    _CloseDoorOfAltarTask = null;

    if (_HalterEscapeTask != null) _HalterEscapeTask.cancel(true);
    _HalterEscapeTask = null;

    if (_LockUpDoorOfAltarTask != null) _LockUpDoorOfAltarTask.cancel(true);
    _LockUpDoorOfAltarTask = null;

    if (_MovieTask != null) _MovieTask.cancel(true);
    _MovieTask = null;

    if (_OpenDoorOfAltarTask != null) _OpenDoorOfAltarTask.cancel(true);
    _OpenDoorOfAltarTask = null;

    if (_TimeUpTask != null) _TimeUpTask.cancel(true);
    _TimeUpTask = null;

    if (_IntervalTask != null) _IntervalTask.cancel(true);
    _IntervalTask = null;

    _TimeUpTask = ThreadPoolManager.getInstance().scheduleGeneral(new TimeUp(), Config.HPH_ACTIVITYTIMEOFHALTER * 1000);

    _log.info("VanHalterManager : Spawn Van Halter.");
  }

  private class Movie implements Runnable
  {
    int _distance = 6502500;
    int _taskId;

    public Movie(int taskId) {
      _taskId = taskId;
    }

    public void run()
    {
      _vanHalter.setHeading(16384);
      _vanHalter.setTarget(_RitualOffering);

      switch (_taskId)
      {
      case 1:
        _Alive = 1;
        GrandBossManager.getInstance().setBossStatus(_BossId, _Alive);
        GrandBossManager.getInstance().save();

        for (L2PcInstance pc : _PlayersInLair)
        {
          if (pc.getPlanDistanceSq(_vanHalter) <= _distance)
          {
            pc.enterMovieMode();
            pc.specialCamera(_vanHalter, 50, 90, 0, 0, 15000);
          }
          else {
            pc.leaveMovieMode();
          }

        }

        if (_MovieTask != null) _MovieTask.cancel(true);
        _MovieTask = null;
        _MovieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(VanHalterManager.this, 2), 16L);

        break;
      case 2:
        for (L2PcInstance pc : _PlayersInLair)
        {
          if (pc.getPlanDistanceSq((L2Object)_CameraMarker.get(Integer.valueOf(5))) <= _distance)
          {
            pc.enterMovieMode();
            pc.specialCamera((L2Object)_CameraMarker.get(Integer.valueOf(5)), 1842, 100, -3, 0, 15000);
          }
          else {
            pc.leaveMovieMode();
          }

        }

        if (_MovieTask != null) _MovieTask.cancel(true);
        _MovieTask = null;
        _MovieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(VanHalterManager.this, 3), 1L);

        break;
      case 3:
        for (L2PcInstance pc : _PlayersInLair)
        {
          if (pc.getPlanDistanceSq((L2Object)_CameraMarker.get(Integer.valueOf(5))) <= _distance)
          {
            pc.enterMovieMode();
            pc.specialCamera((L2Object)_CameraMarker.get(Integer.valueOf(5)), 1861, 97, -10, 1500, 15000);
          }
          else {
            pc.leaveMovieMode();
          }

        }

        if (_MovieTask != null) _MovieTask.cancel(true);
        _MovieTask = null;
        _MovieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(VanHalterManager.this, 4), 1500L);

        break;
      case 4:
        for (L2PcInstance pc : _PlayersInLair)
        {
          if (pc.getPlanDistanceSq((L2Object)_CameraMarker.get(Integer.valueOf(4))) <= _distance)
          {
            pc.enterMovieMode();
            pc.specialCamera((L2Object)_CameraMarker.get(Integer.valueOf(4)), 1876, 97, 12, 0, 15000);
          }
          else {
            pc.leaveMovieMode();
          }

        }

        if (_MovieTask != null) _MovieTask.cancel(true);
        _MovieTask = null;
        _MovieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(VanHalterManager.this, 5), 1L);

        break;
      case 5:
        for (L2PcInstance pc : _PlayersInLair)
        {
          if (pc.getPlanDistanceSq((L2Object)_CameraMarker.get(Integer.valueOf(4))) <= _distance)
          {
            pc.enterMovieMode();
            pc.specialCamera((L2Object)_CameraMarker.get(Integer.valueOf(4)), 1839, 94, 0, 1500, 15000);
          }
          else {
            pc.leaveMovieMode();
          }

        }

        if (_MovieTask != null) _MovieTask.cancel(true);
        _MovieTask = null;
        _MovieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(VanHalterManager.this, 6), 1500L);

        break;
      case 6:
        for (L2PcInstance pc : _PlayersInLair)
        {
          if (pc.getPlanDistanceSq((L2Object)_CameraMarker.get(Integer.valueOf(3))) <= _distance)
          {
            pc.enterMovieMode();
            pc.specialCamera((L2Object)_CameraMarker.get(Integer.valueOf(3)), 1872, 94, 15, 0, 15000);
          }
          else {
            pc.leaveMovieMode();
          }

        }

        if (_MovieTask != null) _MovieTask.cancel(true);
        _MovieTask = null;
        _MovieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(VanHalterManager.this, 7), 1L);

        break;
      case 7:
        for (L2PcInstance pc : _PlayersInLair)
        {
          if (pc.getPlanDistanceSq((L2Object)_CameraMarker.get(Integer.valueOf(3))) <= _distance)
          {
            pc.enterMovieMode();
            pc.specialCamera((L2Object)_CameraMarker.get(Integer.valueOf(3)), 1839, 92, 0, 1500, 15000);
          }
          else {
            pc.leaveMovieMode();
          }

        }

        if (_MovieTask != null) _MovieTask.cancel(true);
        _MovieTask = null;
        _MovieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(VanHalterManager.this, 8), 1500L);

        break;
      case 8:
        for (L2PcInstance pc : _PlayersInLair)
        {
          if (pc.getPlanDistanceSq((L2Object)_CameraMarker.get(Integer.valueOf(2))) <= _distance)
          {
            pc.enterMovieMode();
            pc.specialCamera((L2Object)_CameraMarker.get(Integer.valueOf(2)), 1872, 92, 15, 0, 15000);
          }
          else {
            pc.leaveMovieMode();
          }

        }

        if (_MovieTask != null) _MovieTask.cancel(true);
        _MovieTask = null;
        _MovieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(VanHalterManager.this, 9), 1L);

        break;
      case 9:
        for (L2PcInstance pc : _PlayersInLair)
        {
          if (pc.getPlanDistanceSq((L2Object)_CameraMarker.get(Integer.valueOf(2))) <= _distance)
          {
            pc.enterMovieMode();
            pc.specialCamera((L2Object)_CameraMarker.get(Integer.valueOf(2)), 1839, 90, 5, 1500, 15000);
          }
          else {
            pc.leaveMovieMode();
          }

        }

        if (_MovieTask != null) _MovieTask.cancel(true);
        _MovieTask = null;
        _MovieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(VanHalterManager.this, 10), 1500L);

        break;
      case 10:
        for (L2PcInstance pc : _PlayersInLair)
        {
          if (pc.getPlanDistanceSq((L2Object)_CameraMarker.get(Integer.valueOf(1))) <= _distance)
          {
            pc.enterMovieMode();
            pc.specialCamera((L2Object)_CameraMarker.get(Integer.valueOf(1)), 1872, 90, 5, 0, 15000);
          }
          else {
            pc.leaveMovieMode();
          }

        }

        if (_MovieTask != null) _MovieTask.cancel(true);
        _MovieTask = null;
        _MovieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(VanHalterManager.this, 11), 1L);

        break;
      case 11:
        for (L2PcInstance pc : _PlayersInLair)
        {
          if (pc.getPlanDistanceSq((L2Object)_CameraMarker.get(Integer.valueOf(1))) <= _distance)
          {
            pc.enterMovieMode();
            pc.specialCamera((L2Object)_CameraMarker.get(Integer.valueOf(1)), 2002, 90, 2, 1500, 15000);
          }
          else {
            pc.leaveMovieMode();
          }

        }

        if (_MovieTask != null) _MovieTask.cancel(true);
        _MovieTask = null;
        _MovieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(VanHalterManager.this, 12), 2000L);

        break;
      case 12:
        for (L2PcInstance pc : _PlayersInLair)
        {
          if (pc.getPlanDistanceSq(_vanHalter) <= _distance)
          {
            pc.enterMovieMode();
            pc.specialCamera(_vanHalter, 50, 90, 10, 0, 15000);
          }
          else {
            pc.leaveMovieMode();
          }

        }

        if (_MovieTask != null) _MovieTask.cancel(true);
        _MovieTask = null;
        _MovieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(VanHalterManager.this, 13), 1000L);

        break;
      case 13:
        L2Skill skill = SkillTable.getInstance().getInfo(1168, 7);
        _RitualOffering.setIsInvul(false);
        _vanHalter.setTarget(_RitualOffering);
        _vanHalter.setIsImobilised(false);
        _vanHalter.doCast(skill);
        _vanHalter.setIsImobilised(true);

        if (_MovieTask != null) _MovieTask.cancel(true);
        _MovieTask = null;
        _MovieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(VanHalterManager.this, 14), 4700L);

        break;
      case 14:
        _RitualOffering.setIsInvul(false);
        _RitualOffering.reduceCurrentHp(_RitualOffering.getMaxHp() * 2, _vanHalter);

        if (_MovieTask != null) _MovieTask.cancel(true);
        _MovieTask = null;
        _MovieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(VanHalterManager.this, 15), 4300L);

        break;
      case 15:
        spawnRitualSacrifice();
        deleteRitualOffering();

        for (L2PcInstance pc : _PlayersInLair)
        {
          if (pc.getPlanDistanceSq(_vanHalter) <= _distance)
          {
            pc.enterMovieMode();
            pc.specialCamera(_vanHalter, 100, 90, 15, 1500, 15000);
          }
          else {
            pc.leaveMovieMode();
          }

        }

        if (_MovieTask != null) _MovieTask.cancel(true);
        _MovieTask = null;
        _MovieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(VanHalterManager.this, 16), 2000L);

        break;
      case 16:
        for (L2PcInstance pc : _PlayersInLair)
        {
          if (pc.getPlanDistanceSq(_vanHalter) <= _distance)
          {
            pc.enterMovieMode();
            pc.specialCamera(_vanHalter, 5200, 90, -10, 9500, 6000);
          }
          else {
            pc.leaveMovieMode();
          }

        }

        if (_MovieTask != null) _MovieTask.cancel(true);
        _MovieTask = null;
        _MovieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(VanHalterManager.this, 17), 6000L);

        break;
      case 17:
        for (L2PcInstance pc : _PlayersInLair)
        {
          pc.leaveMovieMode();
        }
        deleteRitualSacrifice();
        deleteCameraMarker();
        _vanHalter.setIsImobilised(false);
        _vanHalter.setIsInvul(false);

        if (_MovieTask != null) _MovieTask.cancel(true);
        _MovieTask = null;
        _MovieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(VanHalterManager.this, 18), 1000L);

        break;
      case 18:
        combatBeginning();
        if (_MovieTask != null) _MovieTask.cancel(true);
        _MovieTask = null;
      }
    }
  }

  private class TimeUp
    implements Runnable
  {
    public TimeUp()
    {
    }

    public void run()
    {
      enterInterval();
    }
  }

  private class Interval
    implements Runnable
  {
    public Interval()
    {
    }

    public void run()
    {
      _PlayersInLair.clear();
      setupAlter();
    }
  }

  private class Bleeding
    implements Runnable
  {
    public Bleeding()
    {
    }

    public void run()
    {
      addBleeding();

      if (_SetBleedTask != null) _SetBleedTask.cancel(true);
      _SetBleedTask = ThreadPoolManager.getInstance().scheduleGeneral(new Bleeding(VanHalterManager.this), 2000L);
    }
  }

  private class HalterEscape
    implements Runnable
  {
    public HalterEscape()
    {
    }

    public void run()
    {
      if ((_RoyalGuardHepler.size() <= Config.HPH_CALLROYALGUARDHELPERCOUNT) && (!_vanHalter.isDead()))
      {
        if (_vanHalter.isAfraid())
        {
          _vanHalter.stopFear(null);
        }
        else
        {
          _vanHalter.startFear();
          if (_vanHalter.getZ() >= -10476)
          {
            L2CharPosition pos = new L2CharPosition(-16397, -53308, -10448, 0);
            if ((_vanHalter.getX() == pos.x) && (_vanHalter.getY() == pos.y))
            {
              _vanHalter.stopFear(null);
            }
            else
            {
              _vanHalter.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, pos);
            }
          }
          else if (_vanHalter.getX() >= -16397)
          {
            L2CharPosition pos = new L2CharPosition(-15548, -54830, -10475, 0);
            _vanHalter.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, pos);
          }
          else
          {
            L2CharPosition pos = new L2CharPosition(-17248, -54830, -10475, 0);
            _vanHalter.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, pos);
          }
        }
        if (_HalterEscapeTask != null) _HalterEscapeTask.cancel(true);
        _HalterEscapeTask = ThreadPoolManager.getInstance().scheduleGeneral(new HalterEscape(VanHalterManager.this), 5000L);
      }
      else
      {
        _vanHalter.stopFear(null);
        if (_HalterEscapeTask != null) _HalterEscapeTask.cancel(true);
        _HalterEscapeTask = null;
      }
    }
  }

  private class CallRoyalGuardHelper
    implements Runnable
  {
    public CallRoyalGuardHelper()
    {
    }

    public void run()
    {
      spawnRoyalGuardHepler();

      if ((_RoyalGuardHepler.size() <= Config.HPH_CALLROYALGUARDHELPERCOUNT) && (!_vanHalter.isDead()))
      {
        if (_CallRoyalGuardHelperTask != null) _CallRoyalGuardHelperTask.cancel(true);
        _CallRoyalGuardHelperTask = ThreadPoolManager.getInstance().scheduleGeneral(new CallRoyalGuardHelper(VanHalterManager.this), Config.HPH_CALLROYALGUARDHELPERINTERVAL * 1000);
      }
      else
      {
        if (_CallRoyalGuardHelperTask != null) _CallRoyalGuardHelperTask.cancel(true);
        _CallRoyalGuardHelperTask = null;
      }
    }
  }

  private class CloseDoorOfAltar
    implements Runnable
  {
    public CloseDoorOfAltar()
    {
    }

    public void run()
    {
      closeDoorOfAltar(true);
    }
  }

  private class OpenDoorOfAltar
    implements Runnable
  {
    public OpenDoorOfAltar()
    {
    }

    public void run()
    {
      openDoorOfAltar(true);
    }
  }

  private class LockUpDoorOfAltar
    implements Runnable
  {
    public LockUpDoorOfAltar()
    {
    }

    public void run()
    {
      closeDoorOfAltar(false);
      _isLocked = true;
      _LockUpDoorOfAltarTask = null;
    }
  }
}