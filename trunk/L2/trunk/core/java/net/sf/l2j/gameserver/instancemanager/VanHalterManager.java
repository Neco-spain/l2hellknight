package net.sf.l2j.gameserver.instancemanager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
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
import net.sf.l2j.gameserver.datatables.DoorTable;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2GrandBossInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.status.GrandBossStatus;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.templates.StatsSet;
import net.sf.l2j.util.Rnd;

public class VanHalterManager
{
    protected static Logger _log = Logger.getLogger(VanHalterManager.class.getName());
    private static VanHalterManager _instance = new VanHalterManager();

    // list of intruders.
    protected List<L2PcInstance> _PlayersInLair = new FastList<L2PcInstance>();
    protected Map<Integer,List<L2PcInstance>> _BleedingPlayers = new FastMap<Integer,List<L2PcInstance>>();

    // spawn data of monsters.
    protected Map<Integer,L2Spawn> _MonsterSpawn = new FastMap<Integer,L2Spawn>();
    protected List<L2Spawn> _RoyalGuardSpawn = new FastList<L2Spawn>();
    protected List<L2Spawn> _RoyalGuardCaptainSpawn = new FastList<L2Spawn>();
    protected List<L2Spawn> _RoyalGuardHelperSpawn = new FastList<L2Spawn>();
    protected List<L2Spawn> _ToriolRevelationSpawn = new FastList<L2Spawn>();
    protected List<L2Spawn> _ToriolRevelationAlive = new FastList<L2Spawn>();
    protected List<L2Spawn> _GuardOfAltarSpawn = new FastList<L2Spawn>();
    protected Map<Integer,L2Spawn> _CameraMarkerSpawn = new FastMap<Integer,L2Spawn>();
    protected L2Spawn _RitualOfferingSpawn = null;
    protected L2Spawn _RitualSacrificeSpawn = null;
    protected L2Spawn _vanHalterSpawn = null;

    // instance of monsters.
    protected List<L2NpcInstance> _Monsters = new FastList<L2NpcInstance>();
    protected List<L2NpcInstance> _RoyalGuard = new FastList<L2NpcInstance>();
    protected List<L2NpcInstance> _RoyalGuardCaptain = new FastList<L2NpcInstance>();
    protected List<L2NpcInstance> _RoyalGuardHepler = new FastList<L2NpcInstance>();
    protected List<L2NpcInstance> _ToriolRevelation = new FastList<L2NpcInstance>();
    protected List<L2NpcInstance> _GuardOfAltar = new FastList<L2NpcInstance>();
    protected Map<Integer,L2NpcInstance> _CameraMarker = new FastMap<Integer,L2NpcInstance>();
    protected List<L2DoorInstance> _DoorOfAltar = new FastList<L2DoorInstance>();
    protected List<L2DoorInstance> _DoorOfSacrifice = new FastList<L2DoorInstance>();
    protected L2NpcInstance _RitualOffering = null;
    protected L2NpcInstance _RitualSacrifice = null;
    protected L2GrandBossInstance _vanHalter = null;

    // Task
    protected ScheduledFuture<?> _MovieTask = null;
    protected ScheduledFuture<?> _CloseDoorOfAltarTask = null;
    protected ScheduledFuture<?> _OpenDoorOfAltarTask = null;
    protected ScheduledFuture<?> _LockUpDoorOfAltarTask = null;
    protected ScheduledFuture<?> _CallRoyalGuardHelperTask = null;
    protected ScheduledFuture<?> _TimeUpTask = null;
    protected ScheduledFuture<?> _IntervalTask = null;
    protected ScheduledFuture<?> _HalterEscapeTask = null;
    protected ScheduledFuture<?> _SetBleedTask = null;

    // state of High Priestess van Halter
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

    public VanHalterManager()
    {

    }

    public static VanHalterManager getInstance()
    {
        if (_instance == null) _instance = new VanHalterManager();

        return _instance;
    }

    // initialize
    public void init()
    {
    	// clear intruder.
    	_PlayersInLair.clear();
        _ZoneType = "AltarofSacrifice";
        _QuestName = "vanhalter";
        _StateSet = GrandBossManager.getInstance().getStatsSet(_BossId);
        _Alive = GrandBossManager.getInstance().getBossStatus(_BossId);

    	// clear flag.
        _isLocked = false;
        _isCaptainSpawned = false;
        _isHelperCalled = false;
        _isHalterSpawned = false;

    	// setting door state.
    	_DoorOfAltar.add(DoorTable.getInstance().getDoor(19160014));
    	_DoorOfAltar.add(DoorTable.getInstance().getDoor(19160015));
    	openDoorOfAltar(true);
    	_DoorOfSacrifice.add(DoorTable.getInstance().getDoor(19160016));
    	_DoorOfSacrifice.add(DoorTable.getInstance().getDoor(19160017));
    	closeDoorOfSacrifice();

        // load spawn data of monsters.
    	loadRoyalGuard();
    	loadToriolRevelation();
    	loadRoyalGuardCaptain();
    	loadRoyalGuardHelper();
    	loadGuardOfAltar();
    	loadVanHalter();
    	loadRitualOffering();
    	loadRitualSacrifice();

        // setting spawn data of Dummy camera marker.
    	_CameraMarkerSpawn.clear();
        try
        {
            L2NpcTemplate template1;
            L2Spawn tempSpawn;

            // Dummy camera marker.
            template1 = NpcTable.getInstance().getTemplate(_DummyId);
            tempSpawn = new L2Spawn(template1);
            tempSpawn.setLocx(-16397);
            tempSpawn.setLocy(-55200);
            tempSpawn.setLocz(-10449);
            tempSpawn.setHeading(16384);
            tempSpawn.setAmount(1);
            tempSpawn.setRespawnDelay(60000);
            SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
            _CameraMarkerSpawn.put(1, tempSpawn);

            template1 = NpcTable.getInstance().getTemplate(_DummyId);
            tempSpawn = new L2Spawn(template1);
            tempSpawn.setLocx(-16397);
            tempSpawn.setLocy(-55200);
            tempSpawn.setLocz(-10051);
            tempSpawn.setHeading(16384);
            tempSpawn.setAmount(1);
            tempSpawn.setRespawnDelay(60000);
            SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
            _CameraMarkerSpawn.put(2, tempSpawn);

            template1 = NpcTable.getInstance().getTemplate(_DummyId);
            tempSpawn = new L2Spawn(template1);
            tempSpawn.setLocx(-16397);
            tempSpawn.setLocy(-55200);
            tempSpawn.setLocz(-9741);
            tempSpawn.setHeading(16384);
            tempSpawn.setAmount(1);
            tempSpawn.setRespawnDelay(60000);
            SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
            _CameraMarkerSpawn.put(3, tempSpawn);

            template1 = NpcTable.getInstance().getTemplate(_DummyId);
            tempSpawn = new L2Spawn(template1);
            tempSpawn.setLocx(-16397);
            tempSpawn.setLocy(-55200);
            tempSpawn.setLocz(-9394);
            tempSpawn.setHeading(16384);
            tempSpawn.setAmount(1);
            tempSpawn.setRespawnDelay(60000);
            SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
            _CameraMarkerSpawn.put(4, tempSpawn);

            template1 = NpcTable.getInstance().getTemplate(_DummyId);
            tempSpawn = new L2Spawn(template1);
            tempSpawn.setLocx(-16397);
            tempSpawn.setLocy(-55197);
            tempSpawn.setLocz(-8739);
            tempSpawn.setHeading(16384);
            tempSpawn.setAmount(1);
            tempSpawn.setRespawnDelay(60000);
            SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
            _CameraMarkerSpawn.put(5, tempSpawn);

        }
        catch (Exception e)
        {
            _log.warning("VanHalterManager : " + e.getMessage());
        }

    	// set bleeding to palyers.
		if (_SetBleedTask != null) _SetBleedTask.cancel(true);
		_SetBleedTask = ThreadPoolManager.getInstance().scheduleGeneral(new Bleeding(),2000);

		// check state of High Priestess van Halter.
        _log.info("VanHalterManager : State of High Priestess van Halter is " + _Alive + ".");
        if (_Alive == GrandBossStatus.INTERVAL)
			enterInterval();
        else
        {
        	// spawn monsters.
            setupAlter();
        }

		Date dt = new Date(_StateSet.getLong("respawn_time"));
        _log.info("VanHalterManager : Next spawn date of High Priestess van Halter is " + dt + ".");
        _log.info("VanHalterManager : Init VanHalterManager.");
    }

    // load Royal Guard.
    protected void loadRoyalGuard()
    {
    	_RoyalGuardSpawn.clear();

        java.sql.Connection con = null;

        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid between ? and ? ORDER BY id");
            statement.setInt(1, 22175);
            statement.setInt(2, 22176);
            ResultSet rset = statement.executeQuery();

            L2Spawn spawnDat;
            L2NpcTemplate template1;

            while (rset.next())
            {
                template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
                if (template1 != null)
                {
                	spawnDat = new L2Spawn(template1);
                	spawnDat.setAmount(rset.getInt("count"));
                	spawnDat.setLocx(rset.getInt("locx"));
                	spawnDat.setLocy(rset.getInt("locy"));
                	spawnDat.setLocz(rset.getInt("locz"));
                	spawnDat.setHeading(rset.getInt("heading"));
                	spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
                	SpawnTable.getInstance().addNewSpawn(spawnDat, false);
                	_RoyalGuardSpawn.add(spawnDat);
                }
                else {
                    _log.warning("VanHalterManager.loadRoyalGuard: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
                }
            }

            rset.close();
            statement.close();
            _log.info("VanHalterManager: Loaded " + _RoyalGuardSpawn.size() + " Royal Guard spawn locations.");
        }
        catch (Exception e)
        {
            // problem with initializing spawn, go to next one
            _log.warning("VanHalterManager.loadRoyalGuard: Spawn could not be initialized: " + e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }

    protected void spawnRoyalGuard()
    {
    	if (!_RoyalGuard.isEmpty()) deleteRoyalGuard();

    	for(L2Spawn rgs : _RoyalGuardSpawn)
    	{
    		rgs.startRespawn();
    		_RoyalGuard.add(rgs.doSpawn());
    	}
    }

    protected void deleteRoyalGuard()
    {
    	for(L2NpcInstance rg : _RoyalGuard)
    	{
    		rg.getSpawn().stopRespawn();
    		rg.deleteMe();
    	}

    	_RoyalGuard.clear();
    }

    // load Triol's Revelation.
    protected void loadToriolRevelation()
    {
    	_ToriolRevelationSpawn.clear();

        java.sql.Connection con = null;

        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid between ? and ? ORDER BY id");
            statement.setInt(1, 32058);
            statement.setInt(2, 32068);
            ResultSet rset = statement.executeQuery();

            L2Spawn spawnDat;
            L2NpcTemplate template1;

            while (rset.next())
            {
                template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
                if (template1 != null)
                {
                	spawnDat = new L2Spawn(template1);
                	spawnDat.setAmount(rset.getInt("count"));
                	spawnDat.setLocx(rset.getInt("locx"));
                	spawnDat.setLocy(rset.getInt("locy"));
                	spawnDat.setLocz(rset.getInt("locz"));
                	spawnDat.setHeading(rset.getInt("heading"));
                	spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
                	SpawnTable.getInstance().addNewSpawn(spawnDat, false);
                	_ToriolRevelationSpawn.add(spawnDat);
                }
                else {
                    _log.warning("VanHalterManager.loadToriolRevelation: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
                }
            }

            rset.close();
            statement.close();
            _log.info("VanHalterManager: Loaded " + _ToriolRevelationSpawn.size() + " Triol's Revelation spawn locations.");
        }
        catch (Exception e)
        {
            // problem with initializing spawn, go to next one
            _log.warning("VanHalterManager.loadToriolRevelation: Spawn could not be initialized: " + e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }

    protected void spawnToriolRevelation()
    {
    	if (!_ToriolRevelation.isEmpty()) deleteToriolRevelation();

    	for(L2Spawn trs : _ToriolRevelationSpawn)
    	{
    		trs.startRespawn();
    		_ToriolRevelation.add(trs.doSpawn());

    		if (trs.getNpcid() != 32067 && trs.getNpcid() != 32068)
    			_ToriolRevelationAlive.add(trs);
    	}
    }

    protected void deleteToriolRevelation()
    {
    	for(L2NpcInstance tr : _ToriolRevelation)
    	{
    		tr.getSpawn().stopRespawn();
    		tr.deleteMe();
    	}

    	_ToriolRevelation.clear();
    	_BleedingPlayers.clear();

    }

    // load Royal Guard Captain.
    protected void loadRoyalGuardCaptain()
    {
    	_RoyalGuardCaptainSpawn.clear();

        java.sql.Connection con = null;

        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid = ? ORDER BY id");
            statement.setInt(1, 22188);
            ResultSet rset = statement.executeQuery();

            L2Spawn spawnDat;
            L2NpcTemplate template1;

            while (rset.next())
            {
                template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
                if (template1 != null)
                {
                	spawnDat = new L2Spawn(template1);
                	spawnDat.setAmount(rset.getInt("count"));
                	spawnDat.setLocx(rset.getInt("locx"));
                	spawnDat.setLocy(rset.getInt("locy"));
                	spawnDat.setLocz(rset.getInt("locz"));
                	spawnDat.setHeading(rset.getInt("heading"));
                	spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
                	SpawnTable.getInstance().addNewSpawn(spawnDat, false);
                	_RoyalGuardCaptainSpawn.add(spawnDat);
                }
                else {
                    _log.warning("VanHalterManager.loadRoyalGuardCaptain: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
                }
            }

            rset.close();
            statement.close();
            _log.info("VanHalterManager: Loaded " + _RoyalGuardCaptainSpawn.size() + " Royal Guard Captain spawn locations.");
        }
        catch (Exception e)
        {
            // problem with initializing spawn, go to next one
            _log.warning("VanHalterManager.loadRoyalGuardCaptain: Spawn could not be initialized: " + e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }

    protected void spawnRoyalGuardCaptain()
    {
    	if (!_RoyalGuardCaptain.isEmpty()) deleteRoyalGuardCaptain();

    	for(L2Spawn trs : _RoyalGuardCaptainSpawn)
    	{
    		trs.startRespawn();
    		_RoyalGuardCaptain.add(trs.doSpawn());
    	}
    	_isCaptainSpawned = true;
    }

    protected void deleteRoyalGuardCaptain()
    {
    	for(L2NpcInstance tr : _RoyalGuardCaptain)
    	{
    		tr.getSpawn().stopRespawn();
    		tr.deleteMe();
    	}

    	_RoyalGuardCaptain.clear();

    }

    // load Royal Guard Helper.
    protected void loadRoyalGuardHelper()
    {
    	_RoyalGuardHelperSpawn.clear();

        java.sql.Connection con = null;

        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid = ? ORDER BY id");
            statement.setInt(1, 22191);
            ResultSet rset = statement.executeQuery();

            L2Spawn spawnDat;
            L2NpcTemplate template1;

            while (rset.next())
            {
                template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
                if (template1 != null)
                {
                	spawnDat = new L2Spawn(template1);
                	spawnDat.setAmount(rset.getInt("count"));
                	spawnDat.setLocx(rset.getInt("locx"));
                	spawnDat.setLocy(rset.getInt("locy"));
                	spawnDat.setLocz(rset.getInt("locz"));
                	spawnDat.setHeading(rset.getInt("heading"));
                	spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
                	SpawnTable.getInstance().addNewSpawn(spawnDat, false);
                	_RoyalGuardHelperSpawn.add(spawnDat);
                }
                else {
                    _log.warning("VanHalterManager.loadRoyalGuardHelper: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
                }
            }

            rset.close();
            statement.close();
            _log.info("VanHalterManager: Loaded " + _RoyalGuardHelperSpawn.size() + " Royal Guard Helper spawn locations.");
        }
        catch (Exception e)
        {
            // problem with initializing spawn, go to next one
            _log.warning("VanHalterManager.loadRoyalGuardHelper: Spawn could not be initialized: " + e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }

    protected void spawnRoyalGuardHepler()
    {
    	for(L2Spawn trs : _RoyalGuardHelperSpawn)
    	{
    		trs.startRespawn();
    		_RoyalGuardHepler.add(trs.doSpawn());
    	}
    }

    protected void deleteRoyalGuardHepler()
    {
    	for(L2NpcInstance tr : _RoyalGuardHepler)
    	{
    		tr.getSpawn().stopRespawn();
    		tr.deleteMe();
    	}

    	_RoyalGuardHepler.clear();

    }

    // load Guard Of Altar
    protected void loadGuardOfAltar()
    {
    	_GuardOfAltarSpawn.clear();

        java.sql.Connection con = null;

        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid = ? ORDER BY id");
            statement.setInt(1, 32051);
            ResultSet rset = statement.executeQuery();

            L2Spawn spawnDat;
            L2NpcTemplate template1;

            while (rset.next())
            {
                template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
                if (template1 != null)
                {
                	spawnDat = new L2Spawn(template1);
                	spawnDat.setAmount(rset.getInt("count"));
                	spawnDat.setLocx(rset.getInt("locx"));
                	spawnDat.setLocy(rset.getInt("locy"));
                	spawnDat.setLocz(rset.getInt("locz"));
                	spawnDat.setHeading(rset.getInt("heading"));
                	spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
                	SpawnTable.getInstance().addNewSpawn(spawnDat, false);
                	_GuardOfAltarSpawn.add(spawnDat);
                }
                else {
                    _log.warning("VanHalterManager.loadGuardOfAltar: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
                }
            }

            rset.close();
            statement.close();
            _log.info("VanHalterManager: Loaded " + _GuardOfAltarSpawn.size() + " Guard Of Altar spawn locations.");
        }
        catch (Exception e)
        {
            // problem with initializing spawn, go to next one
            _log.warning("VanHalterManager.loadGuardOfAltar: Spawn could not be initialized: " + e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }

    }

    protected void spawnGuardOfAltar()
    {
    	if (!_GuardOfAltar.isEmpty()) deleteGuardOfAltar();

    	for(L2Spawn trs : _GuardOfAltarSpawn)
    	{
    		trs.startRespawn();
    		_GuardOfAltar.add(trs.doSpawn());
    	}
    }

    protected void deleteGuardOfAltar()
    {
    	for(L2NpcInstance tr : _GuardOfAltar)
    	{
    		tr.getSpawn().stopRespawn();
    		tr.deleteMe();
    	}

    	_GuardOfAltar.clear();

    }

    // load High Priestess van Halter.
    protected void loadVanHalter()
    {
    	_vanHalterSpawn = null;

        java.sql.Connection con = null;

        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid = ? ORDER BY id");
            statement.setInt(1, 29062);
            ResultSet rset = statement.executeQuery();

            L2Spawn spawnDat;
            L2NpcTemplate template1;

            while (rset.next())
            {
                template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
                if (template1 != null)
                {
                	spawnDat = new L2Spawn(template1);
                	spawnDat.setAmount(rset.getInt("count"));
                	spawnDat.setLocx(rset.getInt("locx"));
                	spawnDat.setLocy(rset.getInt("locy"));
                	spawnDat.setLocz(rset.getInt("locz"));
                	spawnDat.setHeading(rset.getInt("heading"));
                	spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
                	SpawnTable.getInstance().addNewSpawn(spawnDat, false);
                	_vanHalterSpawn = spawnDat;
                }
                else {
                    _log.warning("VanHalterManager.loadVanHalter: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
                }
            }

            rset.close();
            statement.close();
            _log.info("VanHalterManager: Loaded High Priestess van Halter spawn locations.");
        }
        catch (Exception e)
        {
            // problem with initializing spawn, go to next one
            _log.warning("VanHalterManager.loadVanHalter: Spawn could not be initialized: " + e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }

    protected void spawnVanHalter()
    {
    	_vanHalter = (L2GrandBossInstance)_vanHalterSpawn.doSpawn();
    	_vanHalter.setIsImobilised(true);
    	_vanHalter.setIsInvul(true);
    	_isHalterSpawned = true;
    }

    protected void deleteVanHalter()
    {
    	if(_vanHalter != null)
    	{
        	_vanHalter.setIsImobilised(false);
        	_vanHalter.setIsInvul(false);
        	_vanHalter.getSpawn().stopRespawn();
        	_vanHalter.deleteMe();
    	}
    }

    // load Ritual Offering.
    protected void loadRitualOffering()
    {
    	_RitualOfferingSpawn = null;

        java.sql.Connection con = null;

        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid = ? ORDER BY id");
            statement.setInt(1, 32038);
            ResultSet rset = statement.executeQuery();

            L2Spawn spawnDat;
            L2NpcTemplate template1;

            while (rset.next())
            {
                template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
                if (template1 != null)
                {
                	spawnDat = new L2Spawn(template1);
                	spawnDat.setAmount(rset.getInt("count"));
                	spawnDat.setLocx(rset.getInt("locx"));
                	spawnDat.setLocy(rset.getInt("locy"));
                	spawnDat.setLocz(rset.getInt("locz"));
                	spawnDat.setHeading(rset.getInt("heading"));
                	spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
                	SpawnTable.getInstance().addNewSpawn(spawnDat, false);
                	_RitualOfferingSpawn = spawnDat;
                }
                else {
                    _log.warning("VanHalterManager.loadRitualOffering: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
                }
            }

            rset.close();
            statement.close();
            _log.info("VanHalterManager: Loaded Ritual Offering spawn locations.");
        }
        catch (Exception e)
        {
            // problem with initializing spawn, go to next one
            _log.warning("VanHalterManager.loadRitualOffering: Spawn could not be initialized: " + e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }

    protected void spawnRitualOffering()
    {
    	_RitualOffering = _RitualOfferingSpawn.doSpawn();
    	_RitualOffering.setIsImobilised(true);
    	_RitualOffering.setIsInvul(true);
    	_RitualOffering.setIsParalyzed(true);
    }

    protected void deleteRitualOffering()
    {
    	if(_RitualOffering != null)
    	{
        	_RitualOffering.setIsImobilised(false);
        	_RitualOffering.setIsInvul(false);
        	_RitualOffering.setIsParalyzed(false);
        	_RitualOffering.getSpawn().stopRespawn();
        	_RitualOffering.deleteMe();
    	}
    }

    // Load Ritual Sacrifice.
    protected void loadRitualSacrifice()
    {
    	_RitualSacrificeSpawn = null;

        java.sql.Connection con = null;

        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid = ? ORDER BY id");
            statement.setInt(1, 22195);
            ResultSet rset = statement.executeQuery();

            L2Spawn spawnDat;
            L2NpcTemplate template1;

            while (rset.next())
            {
                template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
                if (template1 != null)
                {
                	spawnDat = new L2Spawn(template1);
                	spawnDat.setAmount(rset.getInt("count"));
                	spawnDat.setLocx(rset.getInt("locx"));
                	spawnDat.setLocy(rset.getInt("locy"));
                	spawnDat.setLocz(rset.getInt("locz"));
                	spawnDat.setHeading(rset.getInt("heading"));
                	spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
                	SpawnTable.getInstance().addNewSpawn(spawnDat, false);
                	_RitualSacrificeSpawn = spawnDat;
                }
                else {
                    _log.warning("VanHalterManager.loadRitualSacrifice: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
                }
            }

            rset.close();
            statement.close();
            _log.info("VanHalterManager: Loaded Ritual Sacrifice spawn locations.");
        }
        catch (Exception e)
        {
            // problem with initializing spawn, go to next one
            _log.warning("VanHalterManager.loadRitualSacrifice: Spawn could not be initialized: " + e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }

    protected void spawnRitualSacrifice()
    {
    	_RitualSacrifice = _RitualSacrificeSpawn.doSpawn();
    	_RitualSacrifice.setIsImobilised(true);
    	_RitualSacrifice.setIsInvul(true);
    	_isSacrificeSpawned = true;
    }

    protected void deleteRitualSacrifice()
    {
    	if (!_isSacrificeSpawned) return;

    	if(_RitualSacrifice != null)
    	{
        	_RitualSacrifice.getSpawn().stopRespawn();
        	_RitualSacrifice.deleteMe();
    	}
    	_isSacrificeSpawned = false;
    }

    protected void spawnCameraMarker()
    {
    	_CameraMarker.clear();
    	for(int i = 1; i <= _CameraMarkerSpawn.size();i++)
    	{
    		_CameraMarker.put(i, _CameraMarkerSpawn.get(i).doSpawn());
    		_CameraMarker.get(i).setIsImobilised(true);
    		_CameraMarker.get(i).getSpawn().stopRespawn();
    	}
    }

    protected void deleteCameraMarker()
    {
    	if (_CameraMarker.isEmpty()) return;

    	for(int i = 1; i <= _CameraMarker.size();i++)
    	{
    		_CameraMarker.get(i).deleteMe();
    	}
    	_CameraMarker.clear();
    }

    // door control.
    public void intruderDetection(L2PcInstance intruder)
    {
    	if (!_PlayersInLair.contains(intruder)) _PlayersInLair.add(intruder);
    	if (_LockUpDoorOfAltarTask == null && !_isLocked && _isCaptainSpawned)
    	{
    		_LockUpDoorOfAltarTask = ThreadPoolManager.getInstance().scheduleGeneral(new LockUpDoorOfAltar(),Config.HPH_TIMEOFLOCKUPDOOROFALTAR * 1000);
    	}
    }

    private class LockUpDoorOfAltar implements Runnable
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
    		_CloseDoorOfAltarTask = ThreadPoolManager.getInstance().scheduleGeneral(new CloseDoorOfAltar(),Config.HPH_INTERVALOFDOOROFALTER * 1000);
    	}
    	else
    	{
    		if (_CloseDoorOfAltarTask != null) _CloseDoorOfAltarTask.cancel(true);
    		_CloseDoorOfAltarTask = null;
    	}
    }

    private class OpenDoorOfAltar implements Runnable
    {
    	public OpenDoorOfAltar()
    	{
    	}

    	public void run()
    	{
    		openDoorOfAltar(true);
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
    		if(_OpenDoorOfAltarTask != null) _OpenDoorOfAltarTask.cancel(true);
    		_OpenDoorOfAltarTask = null;
    		_OpenDoorOfAltarTask = ThreadPoolManager.getInstance().scheduleGeneral(new OpenDoorOfAltar(),Config.HPH_INTERVALOFDOOROFALTER * 1000);
    	}
    	else
    	{
    		if(_OpenDoorOfAltarTask != null) _OpenDoorOfAltarTask.cancel(true);
    		_OpenDoorOfAltarTask = null;
    	}
    }

    private class CloseDoorOfAltar implements Runnable
    {
    	public CloseDoorOfAltar()
    	{
    	}

    	public void run()
    	{
    		closeDoorOfAltar(true);
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

    // event
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
        	CreatureSay cs = new CreatureSay(goa.getObjectId(),1,goa.getName(),"The door of the 3rd floor in the altar was opened.");
    		for (L2PcInstance pc : _PlayersInLair)
    		{
    			pc.sendPacket(cs);
    		}
    	}

    	updateKnownList(_vanHalter);
    	_vanHalter.setIsImobilised(true);
    	_vanHalter.setIsInvul(true);
    	spawnCameraMarker();

    	if (_TimeUpTask != null) _TimeUpTask.cancel(true);
    	_TimeUpTask = null;

    	_MovieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(1),Config.HPH_APPTIMEOFHALTER * 1000);
    }

    // update knownlist.
    protected void updateKnownList(L2NpcInstance boss)
    {
    	boss.getKnownList().getKnownPlayers().clear();
		for (L2PcInstance pc : _PlayersInLair)
		{
			boss.getKnownList().getKnownPlayers().put(pc.getObjectId(), pc);
		}
    }

    // start fight against High Priestess van Halter.
    protected void combatBeginning()
    {
    	if (_TimeUpTask != null) _TimeUpTask.cancel(true);
    	_TimeUpTask = ThreadPoolManager.getInstance().scheduleGeneral(new TimeUp(),Config.HPH_FIGHTTIMEOFHALTER * 1000);

    	Map<Integer, L2PcInstance> _targets = new FastMap<Integer, L2PcInstance>();
    	int i = 0;

    	for (L2PcInstance pc : _vanHalter.getKnownList().getKnownPlayers().values())
    	{
    		i++;
    		_targets.put(i, pc);
    	}

    	_vanHalter.reduceCurrentHp(1, _targets.get(Rnd.get(1, i)));

    }

    // call Royal Guard Helper and escape from player.
    public void callRoyalGuardHelper()
    {
    	if (!_isHelperCalled)
    	{
        	_isHelperCalled = true;
        	_HalterEscapeTask = ThreadPoolManager.getInstance().scheduleGeneral(new HalterEscape(),500);
        	_CallRoyalGuardHelperTask = ThreadPoolManager.getInstance().scheduleGeneral(new CallRoyalGuardHelper(),1000);
    	}
    }

    private class CallRoyalGuardHelper implements Runnable
    {
    	public CallRoyalGuardHelper()
    	{
    	}

    	public void run()
    	{
    		spawnRoyalGuardHepler();

    		if (_RoyalGuardHepler.size() <= Config.HPH_CALLROYALGUARDHELPERCOUNT && !_vanHalter.isDead())
    		{
	    		if (_CallRoyalGuardHelperTask != null) _CallRoyalGuardHelperTask.cancel(true);
    			_CallRoyalGuardHelperTask = ThreadPoolManager.getInstance().scheduleGeneral(new CallRoyalGuardHelper(),Config.HPH_CALLROYALGUARDHELPERINTERVAL * 1000);
    		}
    		else
    		{
	    		if (_CallRoyalGuardHelperTask != null) _CallRoyalGuardHelperTask.cancel(true);
        		_CallRoyalGuardHelperTask = null;
    		}
    	}
    }

    private class HalterEscape implements Runnable
    {
    	public HalterEscape()
    	{
    	}

    	public void run()
    	{
    		if (_RoyalGuardHepler.size() <= Config.HPH_CALLROYALGUARDHELPERCOUNT && !_vanHalter.isDead())
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
        	        	L2CharPosition pos = new L2CharPosition(-16397,-53308,-10448,0);
        	        	if (_vanHalter.getX() == pos.x && _vanHalter.getY() == pos.y)
        	        	{
                        	_vanHalter.stopFear(null);
        	        	}
        	        	else
        	        	{
            	        	_vanHalter.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,pos);
        	        	}
        	    	}
        	    	else if (_vanHalter.getX() >= -16397)
        	    	{
        	        	L2CharPosition pos = new L2CharPosition(-15548,-54830,-10475,0);
        	        	_vanHalter.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,pos);
        	    	}
        	    	else
        	    	{
        	        	L2CharPosition pos = new L2CharPosition(-17248,-54830,-10475,0);
        	        	_vanHalter.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,pos);
        	    	}
    			}
    	  		if (_HalterEscapeTask != null)	_HalterEscapeTask.cancel(true);
    	  		_HalterEscapeTask = ThreadPoolManager.getInstance().scheduleGeneral(new HalterEscape(),5000);
    		}
    		else
    		{
            	_vanHalter.stopFear(null);
    	   		if (_HalterEscapeTask != null)	_HalterEscapeTask.cancel(true);
    	   		_HalterEscapeTask = null;
    		}
    	}
    }

    // check bleeding player.
    protected void addBleeding()
    {
    	L2Skill bleed = SkillTable.getInstance().getInfo(4615,12);

		for (L2NpcInstance tr : _ToriolRevelation)
		{
			if (tr.getKnownList().getKnownPlayersInRadius(tr.getAggroRange()).size() == 0 || tr.isDead()) continue;

			List<L2PcInstance> bpc = new FastList<L2PcInstance>();

			for (L2PcInstance pc : tr.getKnownList().getKnownPlayersInRadius(tr.getAggroRange()))
			{
				if (pc.getFirstEffect(bleed) == null)
				{
					bleed.getEffects(tr, pc);
					tr.broadcastPacket(new MagicSkillUser(tr, pc, bleed.getId(), 12, 1, 1));
				}

				bpc.add(pc);
			}
			_BleedingPlayers.remove(tr.getNpcId());
			_BleedingPlayers.put(tr.getNpcId(), bpc);
		}
    }

    public void removeBleeding(int npcId)
    {
    	if (_BleedingPlayers.get(npcId) == null) return;
    	for (L2PcInstance pc : (FastList<L2PcInstance>)_BleedingPlayers.get(npcId))
    	{
			if (pc.getFirstEffect(EffectType.DMG_OVER_TIME) != null) pc.stopEffects(EffectType.DMG_OVER_TIME);
    	}
    	_BleedingPlayers.remove(npcId);
    }

    private class Bleeding implements Runnable
    {
    	public Bleeding()
    	{
    	}

    	public void run()
    	{
    		addBleeding();

    		if (_SetBleedTask != null) _SetBleedTask.cancel(true);
    		_SetBleedTask = ThreadPoolManager.getInstance().scheduleGeneral(new Bleeding(),2000);
    	}
    }

    // High Priestess van Halter dead or time up.
    public void enterInterval()
    {
    	// delete monsters
   		if (_vanHalter != null && _vanHalter.isDead())
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

    	// set interval end.
    	if (_Alive != GrandBossStatus.INTERVAL)
    	{
        	_StateSet.set("respawn_time", Calendar.getInstance().getTimeInMillis() + (Rnd.get(Config.HPH_FIXINTERVALOFHALTER,Config.HPH_FIXINTERVALOFHALTER + Config.HPH_RANDOMINTERVALOFHALTER) * 1000));
        	_Alive = GrandBossStatus.INTERVAL;
        	GrandBossManager.getInstance().setBossStatus(_BossId, _Alive);
        	GrandBossManager.getInstance().setStatsSet(_BossId, _StateSet);
        	GrandBossManager.getInstance().save();
    	}

    	// cancel all task
    	if (_CallRoyalGuardHelperTask != null)	_CallRoyalGuardHelperTask.cancel(true);
   		_CallRoyalGuardHelperTask = null;

   		if (_CloseDoorOfAltarTask != null)	_CloseDoorOfAltarTask.cancel(true);
   		_CloseDoorOfAltarTask = null;

   		if (_HalterEscapeTask != null)	_HalterEscapeTask.cancel(true);
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

    	_IntervalTask = ThreadPoolManager.getInstance().scheduleGeneral(new Interval(),GrandBossManager.getInstance().getInterval(_BossId));
        _log.info("VanHalterManager : Interval START.");
    }

    // interval.
    private class Interval implements Runnable
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

    // interval end.
    public void setupAlter()
    {
    	// delete all monsters
    	deleteVanHalter();
    	deleteToriolRevelation();
    	deleteRoyalGuardHepler();
    	deleteRoyalGuardCaptain();
    	deleteRoyalGuard();
    	deleteRitualSacrifice();
    	deleteRitualOffering();
    	deleteGuardOfAltar();
    	deleteCameraMarker();

    	// clear flag.
        _isLocked = false;
        _isCaptainSpawned = false;
        _isHelperCalled = false;
        _isHalterSpawned = false;

    	// set door state
    	closeDoorOfSacrifice();
    	openDoorOfAltar(true);

    	// respawn monsters.
    	spawnToriolRevelation();
    	spawnRoyalGuard();
    	spawnRitualOffering();
    	spawnVanHalter();

    	// Update Status
    	_Alive = GrandBossStatus.ALIVE;
    	GrandBossManager.getInstance().setBossStatus(_BossId, _Alive);
    	GrandBossManager.getInstance().save();

    	// cancel all task
    	if (_CallRoyalGuardHelperTask != null)	_CallRoyalGuardHelperTask.cancel(true);
   		_CallRoyalGuardHelperTask = null;

   		if (_CloseDoorOfAltarTask != null)	_CloseDoorOfAltarTask.cancel(true);
   		_CloseDoorOfAltarTask = null;

   		if (_HalterEscapeTask != null)	_HalterEscapeTask.cancel(true);
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

    	// set time up.
    	_TimeUpTask = ThreadPoolManager.getInstance().scheduleGeneral(new TimeUp(), Config.HPH_ACTIVITYTIMEOFHALTER * 1000);

    	_log.info("VanHalterManager : Spawn Van Halter.");
    }

    // time up.
    private class TimeUp implements Runnable
    {
    	public TimeUp()
    	{
    	}

    	public void run()
    	{
    		enterInterval();
    	}
    }

    // appearance movie.
    private class Movie implements Runnable
    {
    	int _distance = 6502500;
    	int _taskId;

        public Movie(int taskId)
        {
        	_taskId = taskId;
        }

        public void run()
        {

    		_vanHalter.setHeading(16384);
    		_vanHalter.setTarget(_RitualOffering);

    		switch(_taskId)
    		{
	    		case 1:
	            	_Alive = GrandBossStatus.ALIVE;
	            	GrandBossManager.getInstance().setBossStatus(_BossId, _Alive);
	            	GrandBossManager.getInstance().save();

	    	    	// set camera.
					for (L2PcInstance pc : _PlayersInLair)
					{
						if (pc.getPlanDistanceSq(_vanHalter) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_vanHalter, 50,90,0,0,15000);
						} else
						{
							pc.leaveMovieMode();
						}

					}

					// set next task.
		            if(_MovieTask != null) _MovieTask.cancel(true);
	            	_MovieTask = null;
		            _MovieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(2), 16);

					break;

	    		case 2:
					// set camera.
					for (L2PcInstance pc : _PlayersInLair)
					{
						if (pc.getPlanDistanceSq(_CameraMarker.get(5)) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_CameraMarker.get(5), 1842,100,-3,0,15000);
						} else
						{
							pc.leaveMovieMode();
						}
					}

					// set next task.
		            if(_MovieTask != null) _MovieTask.cancel(true);
	            	_MovieTask = null;
		            _MovieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(3), 1);

					break;

	    		case 3:
					// set camera.
					for (L2PcInstance pc : _PlayersInLair)
					{
						if (pc.getPlanDistanceSq(_CameraMarker.get(5)) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_CameraMarker.get(5), 1861,97,-10,1500,15000);
						} else
						{
							pc.leaveMovieMode();
						}
					}

					// set next task.
		            if(_MovieTask != null) _MovieTask.cancel(true);
		            _MovieTask = null;
		            _MovieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(4), 1500);

					break;

	    		case 4:
					// set camera.
					for (L2PcInstance pc : _PlayersInLair)
					{
						if (pc.getPlanDistanceSq(_CameraMarker.get(4)) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_CameraMarker.get(4), 1876,97,12,0,15000);
						} else
						{
							pc.leaveMovieMode();
						}
					}

					// set next task.
		            if(_MovieTask != null) _MovieTask.cancel(true);
	            	_MovieTask = null;
		            _MovieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(5), 1);

					break;

	    		case 5:
					// set camera.
					for (L2PcInstance pc : _PlayersInLair)
					{
						if (pc.getPlanDistanceSq(_CameraMarker.get(4)) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_CameraMarker.get(4), 1839,94,0,1500,15000);
						} else
						{
							pc.leaveMovieMode();
						}
					}

					// set next task.
		            if(_MovieTask != null) _MovieTask.cancel(true);
	            	_MovieTask = null;
		            _MovieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(6), 1500);

					break;

	    		case 6:
					// set camera.
					for (L2PcInstance pc : _PlayersInLair)
					{
						if (pc.getPlanDistanceSq(_CameraMarker.get(3)) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_CameraMarker.get(3), 1872,94,15,0,15000);
						} else
						{
							pc.leaveMovieMode();
						}
					}

					// set next task.
		            if(_MovieTask != null) _MovieTask.cancel(true);
	            	_MovieTask = null;
		            _MovieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(7), 1);

					break;

	    		case 7:
					// set camera.
					for (L2PcInstance pc : _PlayersInLair)
					{
						if (pc.getPlanDistanceSq(_CameraMarker.get(3)) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_CameraMarker.get(3), 1839,92,0,1500,15000);
						} else
						{
							pc.leaveMovieMode();
						}
					}

					// set next task.
		            if(_MovieTask != null) _MovieTask.cancel(true);
	            	_MovieTask = null;
		            _MovieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(8), 1500);

					break;

	    		case 8:
					// set camera.
					for (L2PcInstance pc : _PlayersInLair)
					{
						if (pc.getPlanDistanceSq(_CameraMarker.get(2)) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_CameraMarker.get(2), 1872,92,15,0,15000);
						} else
						{
							pc.leaveMovieMode();
						}
					}

					// set next task.
		            if(_MovieTask != null) _MovieTask.cancel(true);
	            	_MovieTask = null;
		            _MovieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(9), 1);

					break;

	    		case 9:
					// set camera.
					for (L2PcInstance pc : _PlayersInLair)
					{
						if (pc.getPlanDistanceSq(_CameraMarker.get(2)) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_CameraMarker.get(2), 1839,90,5,1500,15000);
						} else
						{
							pc.leaveMovieMode();
						}
					}

					// set next task.
		            if(_MovieTask != null) _MovieTask.cancel(true);
	            	_MovieTask = null;
		            _MovieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(10), 1500);

					break;

	    		case 10:
					// set camera.
					for (L2PcInstance pc : _PlayersInLair)
					{
						if (pc.getPlanDistanceSq(_CameraMarker.get(1)) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_CameraMarker.get(1), 1872,90,5,0,15000);
						} else
						{
							pc.leaveMovieMode();
						}
					}

					// set next task.
		            if(_MovieTask != null) _MovieTask.cancel(true);
	            	_MovieTask = null;
		            _MovieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(11), 1);

					break;

	    		case 11:
					// set camera.
					for (L2PcInstance pc : _PlayersInLair)
					{
						if (pc.getPlanDistanceSq(_CameraMarker.get(1)) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_CameraMarker.get(1), 2002,90,2,1500,15000);
						} else
						{
							pc.leaveMovieMode();
						}
					}

					// set next task.
		            if(_MovieTask != null) _MovieTask.cancel(true);
	            	_MovieTask = null;
		            _MovieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(12), 2000);

					break;

	    		case 12:
					// set camera.
					for (L2PcInstance pc : _PlayersInLair)
					{
						if (pc.getPlanDistanceSq(_vanHalter) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_vanHalter, 50,90,10,0,15000);
						} else
						{
							pc.leaveMovieMode();
						}
					}

					// set next task.
		            if(_MovieTask != null) _MovieTask.cancel(true);
	            	_MovieTask = null;
		            _MovieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(13), 1000);

					break;

	    		case 13:
	    			// High Priestess van Halter uses the skill to kill Ritual Offering.
					L2Skill skill = SkillTable.getInstance().getInfo(1168, 7);
	    			_RitualOffering.setIsInvul(false);
					_vanHalter.setTarget(_RitualOffering);
	        		_vanHalter.setIsImobilised(false);
	    			_vanHalter.doCast(skill);
	        		_vanHalter.setIsImobilised(true);

					// set next task.
		            if(_MovieTask != null) _MovieTask.cancel(true);
	            	_MovieTask = null;
		            _MovieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(14), 4700);

					break;

	    		case 14:
	    			_RitualOffering.setIsInvul(false);
	    			_RitualOffering.reduceCurrentHp(_RitualOffering.getMaxHp() * 2, _vanHalter);

					// set next task.
		            if(_MovieTask != null) _MovieTask.cancel(true);
	            	_MovieTask = null;
		            _MovieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(15), 4300);

					break;

	    		case 15:
	    			spawnRitualSacrifice();
	    			deleteRitualOffering();

					// set camera.
					for (L2PcInstance pc : _PlayersInLair)
					{
						if (pc.getPlanDistanceSq(_vanHalter) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_vanHalter, 100,90,15,1500,15000);
						} else
						{
							pc.leaveMovieMode();
						}
					}

					// set next task.
		            if(_MovieTask != null) _MovieTask.cancel(true);
	            	_MovieTask = null;
		            _MovieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(16), 2000);

					break;

	    		case 16:
					// set camera.
					for (L2PcInstance pc : _PlayersInLair)
					{
						if (pc.getPlanDistanceSq(_vanHalter) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_vanHalter, 5200,90,-10,9500,6000);
						} else
						{
							pc.leaveMovieMode();
						}
					}

					// set next task.
		            if(_MovieTask != null) _MovieTask.cancel(true);
	            	_MovieTask = null;
		            _MovieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(17), 6000);

					break;

	    		case 17:
					// reset camera.
					for (L2PcInstance pc : _PlayersInLair)
					{
						pc.leaveMovieMode();
					}
					deleteRitualSacrifice();
					deleteCameraMarker();
			    	_vanHalter.setIsImobilised(false);
			    	_vanHalter.setIsInvul(false);

		            if(_MovieTask != null) _MovieTask.cancel(true);
	            	_MovieTask = null;
			    	_MovieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(18), 1000);

					break;

	    		case 18:
	    			combatBeginning();
		            if(_MovieTask != null) _MovieTask.cancel(true);
	            	_MovieTask = null;
    		}
        }
    }
}
