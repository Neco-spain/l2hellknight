package zone_scripts.AntharasLair.AntharasNest;

import gnu.trove.TIntObjectHashMap;
import javolution.util.FastList;
import l2.hellknight.Config;
import l2.hellknight.bflmpsvz.a.L2AttackableAIScript;
import l2.hellknight.gameserver.GeoData;
import l2.hellknight.gameserver.ThreadPoolManager;
import l2.hellknight.gameserver.ai.CtrlIntention;
import l2.hellknight.gameserver.datatables.NpcTable;
import l2.hellknight.gameserver.datatables.SpawnTable;
import l2.hellknight.gameserver.instancemanager.GrandBossManager;
import l2.hellknight.gameserver.model.L2CharPosition;
import l2.hellknight.gameserver.model.L2Spawn;
import l2.hellknight.gameserver.model.L2World;
import l2.hellknight.gameserver.model.actor.L2Character;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2GrandBossInstance;
import l2.hellknight.gameserver.model.actor.instance.L2MonsterInstance;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.zone.type.L2BossZone;
import l2.hellknight.gameserver.network.serverpackets.Earthquake;
import l2.hellknight.gameserver.network.serverpackets.L2GameServerPacket;
import l2.hellknight.gameserver.network.serverpackets.PlaySound;
import l2.hellknight.gameserver.network.serverpackets.SpecialCamera;
import l2.hellknight.gameserver.templates.StatsSet;
import l2.hellknight.gameserver.templates.L2NpcTemplate;
import l2.hellknight.util.Rnd;

import java.util.List;
import java.util.concurrent.ScheduledFuture;

public class LandDragon extends L2AttackableAIScript
{
	// config
	private static final int FWA_ACTIVITYTIMEOFANTHARAS = 120;
	private static final int FWA_INACTIVITYTIME = 900000;
	private static final boolean FWA_OLDANTHARAS = false;
	private static final boolean FWA_MOVEATRANDOM = true;
	private static final boolean FWA_DOSERVEREARTHQUAKE = true;
	private static final int FWA_LIMITOFWEAK = 45;
	private static final int FWA_LIMITOFNORMAL = 63;
	private static final int FWA_MAXMOBS = 10; // this includes Antharas itself
	private static final int FWA_INTERVALOFMOBSNORMAL = 150000;
	private static final int FWA_INTERVALOFMOBSSTRONG = 120000;
	private static final int FWA_PERCENTOFBEHEMOTH = 60;
	// Location of teleport cube.
	private final int _teleportCubeId = 31859;
	private final int _teleportCubeLocation[][] = { { 177615, 114941, -7709, 0 } };
	
	protected List<L2Spawn> _teleportCubeSpawn = new FastList<L2Spawn>();
	protected List<L2Npc> _teleportCube = new FastList<L2Npc>();
	
	// Spawn data of monsters.
	protected TIntObjectHashMap<L2Spawn> _monsterSpawn = new TIntObjectHashMap<L2Spawn>();
	
	// Instance of monsters.
	protected List<L2Npc> _monsters = new FastList<L2Npc>();
	protected L2GrandBossInstance _antharas = null;
	
	// monstersId
	private static final int ANTHARASOLDID = 29068;
	private static final int ANTHARASWEAKID = 29068;
	private static final int ANTHARASNORMALID = 29068;
	private static final int ANTHARASSTRONGID = 29068;
	
	// Tasks.
	protected ScheduledFuture<?> _cubeSpawnTask = null;
	protected ScheduledFuture<?> _monsterSpawnTask = null;
	protected ScheduledFuture<?> _activityCheckTask = null;
	protected ScheduledFuture<?> _socialTask = null;
	protected ScheduledFuture<?> _mobiliseTask = null;
	protected ScheduledFuture<?> _mobsSpawnTask = null;
	protected ScheduledFuture<?> _moveAtRandomTask = null;
	protected ScheduledFuture<?> _movieTask = null;
	
	//Antharas Status Tracking :
	private static final byte DORMANT = 0;		//Antharas is spawned and no one has entered yet. Entry is unlocked
	private static final byte WAITING = 1;		//Antharas is spawend and someone has entered, triggering a 30 minute window for additional people to enter
	//before he unleashes his attack. Entry is unlocked
	private static final byte FIGHTING = 2;		//Antharas is engaged in battle, annihilating his foes. Entry is locked
	private static final byte DEAD = 3;			//Antharas has been killed. Entry is locked
	
	protected static long _LastAction = 0;
	
	protected static L2BossZone _Zone;
	
	public static void main(String[] args)
	{
		// now call the constructor (starts up the ai)
		new LandDragon(-1,"LandDragon","ai");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Antharas Lair: Antharas Nest - Land Dragon");
	}
	
	// Boss: Antharas
	public LandDragon(int id,String name,String descr)
	{
		super(id,name,descr);
		int[] mob = {ANTHARASOLDID,ANTHARASWEAKID,ANTHARASNORMALID,ANTHARASSTRONGID,29069};
		this.registerMobs(mob);
		init();
	}
	
	// Initialize
	private void init()
	{
		// Setting spawn data of monsters.
		try
		{
			_Zone = GrandBossManager.getInstance().getZone(179700,113800,-7709);
			L2NpcTemplate template1;
			L2Spawn tempSpawn;
			
			// Old Antharas
			template1 = NpcTable.getInstance().getTemplate(ANTHARASOLDID);
			tempSpawn = new L2Spawn(template1);
			tempSpawn.setLocx(181323);
			tempSpawn.setLocy(114850);
			tempSpawn.setLocz(-7623);
			tempSpawn.setHeading(32542);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(FWA_ACTIVITYTIMEOFANTHARAS * 2);
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
			_monsterSpawn.put(29019, tempSpawn);
			
			// Weak Antharas
			template1 = NpcTable.getInstance().getTemplate(ANTHARASWEAKID);
			tempSpawn = new L2Spawn(template1);
			tempSpawn.setLocx(181323);
			tempSpawn.setLocy(114850);
			tempSpawn.setLocz(-7623);
			tempSpawn.setHeading(32542);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(FWA_ACTIVITYTIMEOFANTHARAS * 2);
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
			_monsterSpawn.put(29066, tempSpawn);
			
			// Normal Antharas
			template1 = NpcTable.getInstance().getTemplate(ANTHARASNORMALID);
			tempSpawn = new L2Spawn(template1);
			tempSpawn.setLocx(181323);
			tempSpawn.setLocy(114850);
			tempSpawn.setLocz(-7623);
			tempSpawn.setHeading(32542);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(FWA_ACTIVITYTIMEOFANTHARAS * 2);
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
			_monsterSpawn.put(29067, tempSpawn);
			
			// Strong Antharas
			template1 = NpcTable.getInstance().getTemplate(ANTHARASSTRONGID);
			tempSpawn = new L2Spawn(template1);
			tempSpawn.setLocx(181323);
			tempSpawn.setLocy(114850);
			tempSpawn.setLocz(-7623);
			tempSpawn.setHeading(32542);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(FWA_ACTIVITYTIMEOFANTHARAS * 2);
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
			_monsterSpawn.put(29068, tempSpawn);
		}
		catch (Exception e)
		{
			_log.warning(e.getMessage());
		}
		
		// Setting spawn data of teleport cube.
		try
		{
			L2NpcTemplate Cube = NpcTable.getInstance().getTemplate(_teleportCubeId);
			L2Spawn spawnDat;
			for (int[] element : _teleportCubeLocation)
			{
				spawnDat = new L2Spawn(Cube);
				spawnDat.setAmount(1);
				spawnDat.setLocx(element[0]);
				spawnDat.setLocy(element[1]);
				spawnDat.setLocz(element[2]);
				spawnDat.setHeading(element[3]);
				spawnDat.setRespawnDelay(60);
				spawnDat.setLocation(0);
				SpawnTable.getInstance().addNewSpawn(spawnDat, false);
				_teleportCubeSpawn.add(spawnDat);
			}
		}
		catch (Exception e)
		{
			_log.warning(e.getMessage());
		}
		int status = GrandBossManager.getInstance().getBossStatus(ANTHARASOLDID);
		if (FWA_OLDANTHARAS || status == WAITING)
		{
			StatsSet info = GrandBossManager.getInstance().getStatsSet(ANTHARASOLDID);
			Long respawnTime = info.getLong("respawn_time");
			if (status == DEAD && respawnTime <= System.currentTimeMillis())
			{
				// the time has already expired while the server was offline. Immediately spawn antharas in his cave.
				// also, the status needs to be changed to DORMANT
				GrandBossManager.getInstance().setBossStatus(ANTHARASOLDID,DORMANT);
				status = DORMANT;
			}
			else if (status == FIGHTING)
			{
				int loc_x = info.getInteger("loc_x");
				int loc_y = info.getInteger("loc_y");
				int loc_z = info.getInteger("loc_z");
				int heading = info.getInteger("heading");
				int hp = info.getInteger("currentHP");
				int mp = info.getInteger("currentMP");
				_antharas = (L2GrandBossInstance) addSpawn(ANTHARASOLDID,loc_x,loc_y,loc_z,heading,false,0);
				GrandBossManager.getInstance().addBoss(_antharas);
				_antharas.setCurrentHpMp(hp,mp);
				_LastAction = System.currentTimeMillis();
				// Start repeating timer to check for inactivity
				_activityCheckTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new CheckActivity(), 60000, 60000);
			}
			else if (status == DEAD)
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new UnlockAntharas(ANTHARASOLDID), respawnTime - System.currentTimeMillis());
			}
			else
			{
				setAntharasSpawnTask();
			}
		}
		else
		{
			int statusWeak = GrandBossManager.getInstance().getBossStatus(ANTHARASWEAKID);
			int statusNormal = GrandBossManager.getInstance().getBossStatus(ANTHARASNORMALID);
			int statusStrong = GrandBossManager.getInstance().getBossStatus(ANTHARASSTRONGID);
			int antharasId = 0;
			if (statusWeak == FIGHTING || statusWeak == DEAD)
			{
				antharasId = ANTHARASWEAKID;
				status = statusWeak;
			}
			else if (statusNormal == FIGHTING || statusNormal == DEAD)
			{
				antharasId = ANTHARASNORMALID;
				status = statusNormal;
			}
			else if (statusStrong == FIGHTING || statusStrong == DEAD)
			{
				antharasId = ANTHARASSTRONGID;
				status = statusStrong;
			}
			if (antharasId != 0 && status == FIGHTING)
			{
				StatsSet info = GrandBossManager.getInstance().getStatsSet(antharasId);
				int loc_x = info.getInteger("loc_x");
				int loc_y = info.getInteger("loc_y");
				int loc_z = info.getInteger("loc_z");
				int heading = info.getInteger("heading");
				int hp = info.getInteger("currentHP");
				int mp = info.getInteger("currentMP");
				_antharas = (L2GrandBossInstance) addSpawn(antharasId,loc_x,loc_y,loc_z,heading,false,0);
				GrandBossManager.getInstance().addBoss(_antharas);
				_antharas.setCurrentHpMp(hp,mp);
				_LastAction = System.currentTimeMillis();
				// Start repeating timer to check for inactivity
				_activityCheckTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new CheckActivity(), 60000, 60000);
			}
			else if (antharasId != 0 && status == DEAD)
			{
				StatsSet info = GrandBossManager.getInstance().getStatsSet(antharasId);
				Long respawnTime = info.getLong("respawn_time");
				if (respawnTime <= System.currentTimeMillis())
				{
					// the time has already expired while the server was offline. Immediately spawn antharas in his cave.
					// also, the status needs to be changed to DORMANT
					GrandBossManager.getInstance().setBossStatus(antharasId,DORMANT);
					status = DORMANT;
				}
				else
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new UnlockAntharas(antharasId), respawnTime - System.currentTimeMillis());
				}
			}
		}
	}
	
	// Do spawn teleport cube.
	public void spawnCube()
	{
		if (_mobsSpawnTask != null)
		{
			_mobsSpawnTask.cancel(true);
			_mobsSpawnTask = null;
		}
		if (_activityCheckTask != null)
		{
			_activityCheckTask.cancel(false);
			_activityCheckTask = null;
		}
		
		for (L2Spawn spawnDat : _teleportCubeSpawn)
		{
			_teleportCube.add(spawnDat.doSpawn());
		}
	}
	
	// Setting Antharas spawn task.
	public void setAntharasSpawnTask()
	{
		if (_monsterSpawnTask == null)
		{
			synchronized(this)
			{
				if (_monsterSpawnTask == null)
				{
					GrandBossManager.getInstance().setBossStatus(ANTHARASOLDID,WAITING);
					_monsterSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(1),Config.Antharas_Wait_Time);
				}
			}
		}
	}
	
	private void startMinionSpawns(int antharasId)
	{
		int intervalOfMobs;
		
		// Interval of minions is decided by the type of Antharas
		// that invaded the lair.
		switch (antharasId)
		{
			case ANTHARASNORMALID:
				intervalOfMobs = FWA_INTERVALOFMOBSNORMAL;
				break;
			default:
				intervalOfMobs = FWA_INTERVALOFMOBSSTRONG;
				break;
		}
		
		// Spawn mobs.
		_mobsSpawnTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new MobsSpawn(), intervalOfMobs, intervalOfMobs);
	}
	
	// Do spawn Antharas.
	private class AntharasSpawn implements Runnable
	{
		private int _taskId = 0;
		private final L2Character[] _players = _Zone.getCharactersInsideArray();
		
		AntharasSpawn(int taskId)
		{
			_taskId = taskId;
		}
		
		public void run()
		{
			int npcId;
			L2Spawn antharasSpawn = null;
			
			switch (_taskId)
			{
				case 1: // Spawn.
					// Strength of Antharas is decided by the number of players that
					// invaded the lair.
					_monsterSpawnTask.cancel(false);
					_monsterSpawnTask = null;
					if (FWA_OLDANTHARAS)
						npcId = 29019; // old
					else if (_players.length <= FWA_LIMITOFWEAK)
						npcId = 29066; // weak
					else if (_players.length > FWA_LIMITOFNORMAL)
						npcId = 29068; // strong
					else
						npcId = 29067; // normal
					
					// Do spawn.
					antharasSpawn = _monsterSpawn.get(npcId);
					_antharas = (L2GrandBossInstance) antharasSpawn.doSpawn();
					GrandBossManager.getInstance().addBoss(_antharas);
					
					_monsters.add(_antharas);
					_antharas.setIsImmobilized(true);
					
					GrandBossManager.getInstance().setBossStatus(ANTHARASOLDID,DORMANT);
					GrandBossManager.getInstance().setBossStatus(ANTHARASOLDID,FIGHTING);
					_LastAction = System.currentTimeMillis();
					// Start repeating timer to check for inactivity
					_activityCheckTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new CheckActivity(), 60000, 60000);
					
					// Setting 1st time of minions spawn task.
					if (!FWA_OLDANTHARAS)
					{
						startMinionSpawns(npcId);
					}
					
					// Set next task.
					if (_socialTask != null)
					{
						_socialTask.cancel(true);
						_socialTask = null;
					}
					_socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(2), 16);
					break;
				case 2:
					// Set camera.
					broadcastPacket(new SpecialCamera(_antharas.getObjectId(),700,13,-19,0,20000,0,0,1,0));
					
					// Set next task.
					if (_socialTask != null)
					{
						_socialTask.cancel(true);
						_socialTask = null;
					}
					_socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(3), 3000);
					break;
					
				case 3:
					// Do social.
					broadcastPacket(new SpecialCamera(_antharas.getObjectId(),700,13,0,6000,20000,0,0,1,0));
					// Set next task.
					if (_socialTask != null)
					{
						_socialTask.cancel(true);
						_socialTask = null;
					}
					_socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(4), 10000);
					break;
				case 4:
					broadcastPacket(new SpecialCamera(_antharas.getObjectId(),3700,0,-3,0,10000,0,0,1,0));
					// Set next task.
					if (_socialTask != null)
					{
						_socialTask.cancel(true);
						_socialTask = null;
					}
					_socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(5), 200);
					break;
					
				case 5:
					// Do social.
					broadcastPacket(new SpecialCamera(_antharas.getObjectId(),1100,0,-3,22000,30000,0,0,1,0));
					// Set next task.
					if (_socialTask != null)
					{
						_socialTask.cancel(true);
						_socialTask = null;
					}
					_socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(6), 10800);
					break;
					
				case 6:
					// Set camera.
					broadcastPacket(new SpecialCamera(_antharas.getObjectId(),1100,0,-3,300,7000,0,0,1,0));
					// Set next task.
					if (_socialTask != null)
					{
						_socialTask.cancel(true);
						_socialTask = null;
					}
					_socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(7), 1900);
					break;
					
				case 7:
					_antharas.abortCast();
					
					_mobiliseTask = ThreadPoolManager.getInstance().scheduleGeneral(new SetMobilised(_antharas), 16);
					
					// Move at random.
					if (FWA_MOVEATRANDOM)
					{
						L2CharPosition pos = new L2CharPosition(Rnd.get(175000,178500), Rnd.get(112400, 116000), -7707, 0);
						_moveAtRandomTask = ThreadPoolManager.getInstance().scheduleGeneral(new MoveAtRandom(_antharas, pos),500);
					}
					
					if (_socialTask != null)
					{
						_socialTask.cancel(true);
						_socialTask = null;
					}
					break;
			}
		}
	}
	
	private void broadcastPacket(L2GameServerPacket mov)
	{
		if (_Zone != null)
		{
			for (L2Character characters : _Zone.getCharactersInsideArray())
			{
				if (characters instanceof L2PcInstance)
					characters.sendPacket(mov);
			}
		}
	}
	
	// Do spawn Behemoth or Bomber.
	private class MobsSpawn implements Runnable
	{
		public MobsSpawn()
		{
		}
		
		public void run()
		{
			L2NpcTemplate template1;
			L2Spawn tempSpawn;
			boolean isBehemoth = Rnd.get(100) < FWA_PERCENTOFBEHEMOTH;
			try
			{
				int mobNumber = (isBehemoth ? 2 : 3);
				// Set spawn.
				for(int i = 0; i < mobNumber; i++)
				{
					if (_monsters.size() >= FWA_MAXMOBS)
						break;
					int npcId;
					if (isBehemoth)
						npcId = 29069;
					else
						npcId = Rnd.get(29069);
					template1 = NpcTable.getInstance().getTemplate(npcId);
					tempSpawn = new L2Spawn(template1);
					// allocates it at random in the lair of Antharas.
					int tried = 0;
					boolean notFound = true;
					int x = 175000;
					int y = 112400;
					int dt = (_antharas.getX() - x) * (_antharas.getX() - x) + (_antharas.getY() - y) * (_antharas.getY() - y);
					while (tried++ < 25 && notFound)
					{
						int rx = Rnd.get(175000, 179900);
						int ry = Rnd.get(112400, 116000);
						int rdt = (_antharas.getX() - rx) * (_antharas.getX() - rx) + (_antharas.getY() - ry) * (_antharas.getY() - ry);
						if (GeoData.getInstance().canSeeTarget(_antharas.getX(), _antharas.getY(), -7704, rx, ry, -7704))
							if (rdt < dt)
							{
								x = rx;
								y = ry;
								dt = rdt;
								if (rdt <= 900000)
									notFound = false;
							}
					}
					tempSpawn.setLocx(x);
					tempSpawn.setLocy(y);
					tempSpawn.setLocz(-7704);
					tempSpawn.setHeading(0);
					tempSpawn.setAmount(1);
					tempSpawn.setRespawnDelay(FWA_ACTIVITYTIMEOFANTHARAS * 2);
					SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
					// Do spawn.
					_monsters.add(tempSpawn.doSpawn());
				}
			}
			catch (Exception e)
			{
				_log.warning(e.getMessage());
			}
		}
	}
	// At end of activity time.
	private class CheckActivity implements Runnable
	{
		public void run()
		{
			Long temp = (System.currentTimeMillis() - _LastAction);
			if (temp > FWA_INACTIVITYTIME)
			{
				GrandBossManager.getInstance().setBossStatus(_antharas.getNpcId(),DORMANT);
				setUnspawn();
			}
		}
	}
	
	// Clean Antharas's lair.
	public void setUnspawn()
	{
		// Eliminate players.
		_Zone.oustAllPlayers();
		
		// Not executed tasks is canceled.
		if (_cubeSpawnTask != null)
		{
			_cubeSpawnTask.cancel(true);
			_cubeSpawnTask = null;
		}
		if (_monsterSpawnTask != null)
		{
			_monsterSpawnTask.cancel(true);
			_monsterSpawnTask = null;
		}
		if (_activityCheckTask != null)
		{
			_activityCheckTask.cancel(false);
			_activityCheckTask = null;
		}
		if (_socialTask != null)
		{
			_socialTask.cancel(true);
			_socialTask = null;
		}
		if (_mobiliseTask != null)
		{
			_mobiliseTask.cancel(true);
			_mobiliseTask = null;
		}
		if (_mobsSpawnTask != null)
		{
			_mobsSpawnTask.cancel(true);
			_mobsSpawnTask = null;
		}
		if (_moveAtRandomTask != null)
		{
			_moveAtRandomTask.cancel(true);
			_moveAtRandomTask = null;
		}
		
		// Delete monsters.
		for (L2Npc mob : _monsters)
		{
			mob.getSpawn().stopRespawn();
			mob.deleteMe();
		}
		_monsters.clear();
		
		// Delete teleport cube.
		for (L2Npc cube : _teleportCube)
		{
			cube.getSpawn().stopRespawn();
			cube.deleteMe();
		}
		_teleportCube.clear();
	}
	
	// Do spawn teleport cube.
	private class CubeSpawn implements Runnable
	{
		private int _type;
		
		CubeSpawn(int type)
		{
			_type = type;
		}
		
		public void run()
		{
			if (_type == 0)
			{
				spawnCube();
				_cubeSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new CubeSpawn(1), 1800000);
			}
			else
				setUnspawn();
		}
	}
	
	// UnLock Antharas.
	private static class UnlockAntharas implements Runnable
	{
		private int _bossId;
		
		public UnlockAntharas(int bossId)
		{
			_bossId = bossId;
		}
		
		public void run()
		{
			GrandBossManager.getInstance().setBossStatus(_bossId,DORMANT);
			if (FWA_DOSERVEREARTHQUAKE)
				for(L2PcInstance p : L2World.getInstance().getAllPlayersArray())
					p.broadcastPacket(new Earthquake(185708,114298,-8221,20,10));
		}
	}
	
	// Action is enabled the boss.
	private class SetMobilised implements Runnable
	{
		private L2GrandBossInstance _boss;
		
		public SetMobilised(L2GrandBossInstance boss)
		{
			_boss = boss;
		}
		
		public void run()
		{
			_boss.setIsImmobilized(false);
			
			// When it is possible to act, a social action is canceled.
			if (_socialTask != null)
			{
				_socialTask.cancel(true);
				_socialTask = null;
			}
		}
	}
	
	// Move at random on after Antharas appears.
	private static class MoveAtRandom implements Runnable
	{
		private L2Npc _npc;
		private L2CharPosition _pos;
		
		public MoveAtRandom(L2Npc npc, L2CharPosition pos)
		{
			_npc = npc;
			_pos = pos;
		}
		
		public void run()
		{
			_npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, _pos);
		}
	}
	
	@Override
	public String onAttack (L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if (npc.getNpcId() == 29019 || npc.getNpcId() == 29066 || npc.getNpcId() == 29067 || npc.getNpcId() == 29068)
		{
			_LastAction = System.currentTimeMillis();
			if (GrandBossManager.getInstance().getBossStatus(_antharas.getNpcId()) != FIGHTING)
			{
				_Zone.oustAllPlayers();
			}
			else if (!FWA_OLDANTHARAS && _mobsSpawnTask == null)
			{
				startMinionSpawns(npc.getNpcId());
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill (L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if (npc.getNpcId() == 29019 || npc.getNpcId() == 29066 || npc.getNpcId() == 29067 || npc.getNpcId() == 29068)
		{
			npc.broadcastPacket(new PlaySound(1, "BS01_D", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));
			_cubeSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new CubeSpawn(0), 10000);
			GrandBossManager.getInstance().setBossStatus(npc.getNpcId(),DEAD);
			long respawnTime = (long)Config.Interval_Of_Antharas_Spawn + Rnd.get(Config.Random_Of_Antharas_Spawn);
			ThreadPoolManager.getInstance().scheduleGeneral(new UnlockAntharas(npc.getNpcId()), respawnTime);
			// also save the respawn time so that the info is maintained past reboots
			StatsSet info = GrandBossManager.getInstance().getStatsSet(npc.getNpcId());
			info.set("respawn_time",(System.currentTimeMillis() + respawnTime));
			GrandBossManager.getInstance().setStatsSet(npc.getNpcId(),info);
		}
		else if (npc.getNpcId() == 29069)
		{
			int countHPHerb = Rnd.get(6, 18);
			int countMPHerb = Rnd.get(6, 18);
			for (int i = 0; i < countHPHerb; i++)
				((L2MonsterInstance)npc).dropItem(killer, 8602, 1);
			for (int i = 0; i < countMPHerb; i++)
				((L2MonsterInstance)npc).dropItem(killer, 8605, 1);
		}
		if (_monsters.contains(npc))
			_monsters.remove(npc);
		return super.onKill(npc,killer,isPet);
	}
}