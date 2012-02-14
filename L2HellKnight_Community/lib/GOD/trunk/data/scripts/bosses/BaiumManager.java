package bosses;

import static l2rt.gameserver.ai.CtrlIntention.AI_INTENTION_ACTIVE;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import javolution.util.FastMap;
import l2rt.Config;
import l2rt.common.ThreadPoolManager;
import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.instancemanager.ZoneManager;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.L2Spawn;
import l2rt.gameserver.model.L2Zone;
import l2rt.gameserver.model.L2Zone.ZoneType;
import l2rt.gameserver.model.instances.L2BossInstance;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.network.serverpackets.Earthquake;
import l2rt.gameserver.network.serverpackets.PlaySound;
import l2rt.gameserver.network.serverpackets.SocialAction;
import l2rt.gameserver.tables.NpcTable;
import l2rt.gameserver.tables.SkillTable;
import l2rt.gameserver.templates.L2NpcTemplate;
import l2rt.util.GArray;
import l2rt.util.Location;
import l2rt.util.Log;
import l2rt.util.Rnd;
import bosses.EpicBossState.State;

public class BaiumManager extends Functions implements ScriptFile
{
	// call Arcangels
	public static class CallArchAngel implements Runnable
	{
		public void run()
		{
			for(L2Spawn spawn : _angelSpawns)
				_angels.add(spawn.doSpawn(true));
		}
	}

	public static class CheckLastAttack implements Runnable
	{
		public void run()
		{
			if(_state.getState().equals(EpicBossState.State.ALIVE))
				if(_lastAttackTime + FWB_LIMITUNTILSLEEP < System.currentTimeMillis())
					sleepBaium();
				else
					_sleepCheckTask = ThreadPoolManager.getInstance().scheduleGeneral(new CheckLastAttack(), 60000);
		}
	}

	// do spawn teleport cube.
	public static class CubeSpawn implements Runnable
	{
		public void run()
		{
			_teleportCube = _teleportCubeSpawn.doSpawn(true);
		}
	}

	// at end of interval.
	public static class IntervalEnd implements Runnable
	{
		public void run()
		{
			_state.setState(EpicBossState.State.NOTSPAWN);
			_state.update();

			// statue of Baium respawn.
			_statueSpawn.doSpawn(true);
		}
	}

	// kill pc
	public static class KillPc implements Runnable
	{
		private L2BossInstance _boss;
		private L2Player _target;

		public KillPc(L2Player target, L2BossInstance boss)
		{
			_target = target;
			_boss = boss;
		}

		public void run()
		{
			L2Skill skill = SkillTable.getInstance().getInfo(4136, 1);
			if(_target != null && skill != null)
			{
				_boss.setTarget(_target);
				_boss.doCast(skill, _target, false);
			}
		}
	}

	// Move at random on after Baium appears.
	public static class MoveAtRandom implements Runnable
	{
		private L2NpcInstance _npc;
		private Location _pos;

		public MoveAtRandom(L2NpcInstance npc, Location pos)
		{
			_npc = npc;
			_pos = pos;
		}

		public void run()
		{
			if(_npc.getAI().getIntention() == AI_INTENTION_ACTIVE)
				_npc.moveToLocation(_pos, 0, false);
		}
	}

	public static class SetMobilised implements Runnable
	{
		private L2BossInstance _boss;

		public SetMobilised(L2BossInstance boss)
		{
			_boss = boss;
		}

		public void run()
		{
			_boss.setImobilised(false);
		}
	}

	// do social.
	public static class Social implements Runnable
	{
		private int _action;
		private L2NpcInstance _npc;

		public Social(L2NpcInstance npc, int actionId)
		{
			_npc = npc;
			_action = actionId;
		}

		public void run()
		{
			SocialAction sa = new SocialAction(_npc.getObjectId(), _action);
			_npc.broadcastPacket(sa);
		}
	}

	// tasks.
	private static ScheduledFuture<?> _callAngelTask = null;
	private static ScheduledFuture<?> _cubeSpawnTask = null;
	private static ScheduledFuture<?> _intervalEndTask = null;
	private static ScheduledFuture<?> _killPcTask = null;
	private static ScheduledFuture<?> _mobiliseTask = null;
	private static ScheduledFuture<?> _moveAtRandomTask = null;
	private static ScheduledFuture<?> _sleepCheckTask = null;
	private static ScheduledFuture<?> _socialTask = null;
	private static ScheduledFuture<?> _socialTask2 = null;
	private static ScheduledFuture<?> _onAnnihilatedTask = null;

	private static EpicBossState _state;
	private static long _lastAttackTime = 0;

	private static L2NpcInstance _npcBaium;
	private static L2Spawn _statueSpawn = null;

	private static L2NpcInstance _teleportCube = null;
	private static L2Spawn _teleportCubeSpawn = null;

	private static GArray<L2NpcInstance> _monsters = new GArray<L2NpcInstance>();
	private static Map<Integer, L2Spawn> _monsterSpawn = new FastMap<Integer, L2Spawn>().setShared(true);

	private static GArray<L2NpcInstance> _angels = new GArray<L2NpcInstance>();
	private static GArray<L2Spawn> _angelSpawns = new GArray<L2Spawn>();

	private static L2Zone _zone;

	private final static int ARCHANGEL = 29021;
	private final static int BAIUM = 29020;
	private final static int BAIUM_NPC = 29025;

	private static boolean Dying = false;

	// location of arcangels.
	private final static Location[] ANGEL_LOCATION = new Location[] { new Location(113004, 16209, 10076, 60242),
			new Location(114053, 16642, 10076, 4411), new Location(114563, 17184, 10076, 49241),
			new Location(116356, 16402, 10076, 31109), new Location(115015, 16393, 10076, 32760),
			new Location(115481, 15335, 10076, 16241), new Location(114680, 15407, 10051, 32485),
			new Location(114886, 14437, 10076, 16868), new Location(115391, 17593, 10076, 55346),
			new Location(115245, 17558, 10076, 35536) };

	// location of teleport cube.
	private final static Location CUBE_LOCATION = new Location(115203, 16620, 10078, 0);
	private final static Location STATUE_LOCATION = new Location(115996, 17417, 10106, 41740);
	private final static int TELEPORT_CUBE = 31759;

	private final static int FWB_LIMITUNTILSLEEP = 30 * 60000;
	private final static int FWB_FIXINTERVALOFBAIUM = 5 * 24 * 60 * 60000;
	private final static int FWB_RANDOMINTERVALOFBAIUM = 8 * 60 * 60000;

	private static void banishForeigners()
	{
		for(L2Player player : getPlayersInside())
			player.teleToClosestTown();
	}

	public static class onAnnihilated implements Runnable
	{
		public void run()
		{
			sleepBaium();
		}
	}

	private synchronized static void checkAnnihilated()
	{
		if(_onAnnihilatedTask == null && isPlayersAnnihilated())
			_onAnnihilatedTask = ThreadPoolManager.getInstance().scheduleGeneral(new onAnnihilated(), 5000);
	}

	// Archangel ascension.
	private static void deleteArchangels()
	{
		for(L2NpcInstance angel : _angels)
			if(angel != null && angel.getSpawn() != null)
			{
				angel.getSpawn().stopRespawn();
				angel.deleteMe();
			}
		_angels.clear();
	}

	private static GArray<L2Player> getPlayersInside()
	{
		return getZone().getInsidePlayersIncludeZ();
	}

	public static L2Zone getZone()
	{
		return _zone;
	}

	private static void init()
	{
		_state = new EpicBossState(BAIUM);
		_zone = ZoneManager.getInstance().getZoneById(ZoneType.epic, 702001, false);

		try
		{
			L2Spawn tempSpawn;

			// Statue of Baium
			_statueSpawn = new L2Spawn(NpcTable.getTemplate(BAIUM_NPC));
			_statueSpawn.setAmount(1);
			_statueSpawn.setLoc(STATUE_LOCATION);
			_statueSpawn.stopRespawn();

			// Baium
			tempSpawn = new L2Spawn(NpcTable.getTemplate(BAIUM));
			tempSpawn.setAmount(1);
			_monsterSpawn.put(BAIUM, tempSpawn);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		// Teleport Cube
		try
		{
			L2NpcTemplate Cube = NpcTable.getTemplate(TELEPORT_CUBE);
			L2Spawn _teleportCubeSpawn = new L2Spawn(Cube);
			_teleportCubeSpawn.setAmount(1);
			_teleportCubeSpawn.setLoc(CUBE_LOCATION);
			_teleportCubeSpawn.setRespawnDelay(60);
			_teleportCubeSpawn.setLocation(0);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		// Archangels
		try
		{
			L2NpcTemplate angel = NpcTable.getTemplate(ARCHANGEL);
			L2Spawn spawnDat;
			_angelSpawns.clear();

			// 5 random numbers of 10, no duplicates
			GArray<Integer> random = new GArray<Integer>();
			for(int i = 0; i < 5; i++)
			{
				int r = -1;
				while(r == -1 || random.contains(r))
					r = Rnd.get(10);
				random.add(r);
			}

			for(int i : random)
			{
				spawnDat = new L2Spawn(angel);
				spawnDat.setAmount(1);
				spawnDat.setLoc(ANGEL_LOCATION[i]);
				spawnDat.setRespawnDelay(300000);
				spawnDat.setLocation(0);
				_angelSpawns.add(spawnDat);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		System.out.println("BaiumManager: State of Baium is " + _state.getState() + ".");
		if(_state.getState().equals(EpicBossState.State.NOTSPAWN))
			_statueSpawn.doSpawn(true);
		else if(_state.getState().equals(EpicBossState.State.ALIVE))
		{
			_state.setState(EpicBossState.State.NOTSPAWN);
			_state.update();
			_statueSpawn.doSpawn(true);
		}
		else if(_state.getState().equals(EpicBossState.State.INTERVAL) || _state.getState().equals(EpicBossState.State.DEAD))
			setIntervalEndTask();

		Date dt = new Date(_state.getRespawnDate());
		System.out.println("Loaded Boss: Baium. Next spawn date: " + dt);
	}

	private static boolean isPlayersAnnihilated()
	{
		for(L2Player pc : getPlayersInside())
			if(!pc.isDead())
				return false;
		return true;
	}

	public static void OnDie(L2Character self, L2Character killer)
	{
		if(self == null)
			return;
		if(self.isPlayer() && _state != null && _state.getState() == State.ALIVE && _zone != null && _zone.checkIfInZone(self))
			checkAnnihilated();
		else if(self.isNpc() && self.getNpcId() == BAIUM)
			onBaiumDie(self);
	}

	public static void onBaiumDie(L2Character self)
	{
		if(Dying)
			return;

		Dying = true;
		self.broadcastPacket(new PlaySound(1, "BS02_D", 1, 0, self.getLoc()));
		_state.setRespawnDate(getRespawnInterval());
		_state.setState(EpicBossState.State.INTERVAL);
		_state.update();

		Log.add("Baium died", "bosses");

		deleteArchangels();

		_cubeSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new CubeSpawn(), 10000);
	}

	private static int getRespawnInterval()
	{
		return (int) (Config.ALT_RAID_RESPAWN_MULTIPLIER * (FWB_FIXINTERVALOFBAIUM + Rnd.get(0, FWB_RANDOMINTERVALOFBAIUM)));
	}

	// start interval.
	private static void setIntervalEndTask()
	{
		setUnspawn();

		//init state of Baium's lair.  
		if(!_state.getState().equals(EpicBossState.State.INTERVAL))
		{
			_state.setRespawnDate(getRespawnInterval());
			_state.setState(EpicBossState.State.INTERVAL);
			_state.update();
		}

		_intervalEndTask = ThreadPoolManager.getInstance().scheduleGeneral(new IntervalEnd(), _state.getInterval());
	}

	public static void setLastAttackTime()
	{
		_lastAttackTime = System.currentTimeMillis();
	}

	// clean Baium's lair.
	public static void setUnspawn()
	{
		// eliminate players.
		banishForeigners();

		// delete monsters.
		deleteArchangels();
		for(L2NpcInstance mob : _monsters)
		{
			mob.getSpawn().stopRespawn();
			mob.deleteMe();
		}
		_monsters.clear();

		// delete teleport cube.
		if(_teleportCube != null)
		{
			_teleportCube.getSpawn().stopRespawn();
			_teleportCube.deleteMe();
			_teleportCube = null;
		}

		// not executed tasks is canceled.
		if(_cubeSpawnTask != null)
		{
			_cubeSpawnTask.cancel(false);
			_cubeSpawnTask = null;
		}
		if(_intervalEndTask != null)
		{
			_intervalEndTask.cancel(false);
			_intervalEndTask = null;
		}
		if(_socialTask != null)
		{
			_socialTask.cancel(false);
			_socialTask = null;
		}
		if(_mobiliseTask != null)
		{
			_mobiliseTask.cancel(false);
			_mobiliseTask = null;
		}
		if(_moveAtRandomTask != null)
		{
			_moveAtRandomTask.cancel(false);
			_moveAtRandomTask = null;
		}
		if(_socialTask2 != null)
		{
			_socialTask2.cancel(false);
			_socialTask2 = null;
		}
		if(_killPcTask != null)
		{
			_killPcTask.cancel(false);
			_killPcTask = null;
		}
		if(_callAngelTask != null)
		{
			_callAngelTask.cancel(false);
			_callAngelTask = null;
		}
		if(_sleepCheckTask != null)
		{
			_sleepCheckTask.cancel(false);
			_sleepCheckTask = null;
		}
		if(_onAnnihilatedTask != null)
		{
			_onAnnihilatedTask.cancel(false);
			_onAnnihilatedTask = null;
		}
	}

	// Baium sleeps if not attacked for 30 minutes.
	private static void sleepBaium()
	{
		setUnspawn();
		Log.add("Baium going to sleep, spawning statue", "bosses");
		_state.setState(EpicBossState.State.NOTSPAWN);
		_state.update();

		// statue of Baium respawn.
		_statueSpawn.doSpawn(true);
	}

	public static class EarthquakeTask implements Runnable
	{
		private final L2BossInstance baium;

		public EarthquakeTask(L2BossInstance _baium)
		{
			baium = _baium;
		}

		public void run()
		{
			Earthquake eq = new Earthquake(baium.getLoc(), 40, 5);
			baium.broadcastPacket(eq);
		}
	}

	// do spawn Baium.
	public static void spawnBaium(L2NpcInstance NpcBaium, L2Player awake_by)
	{
		Dying = false;
		_npcBaium = NpcBaium;

		// do spawn.
		L2Spawn baiumSpawn = _monsterSpawn.get(BAIUM);
		baiumSpawn.setLoc(_npcBaium.getLoc());

		// delete statue
		_npcBaium.getSpawn().stopRespawn();
		_npcBaium.deleteMe();

		final L2BossInstance baium = (L2BossInstance) baiumSpawn.doSpawn(true);
		_monsters.add(baium);

		_state.setRespawnDate(getRespawnInterval());
		_state.setState(EpicBossState.State.ALIVE);
		_state.update();

		Log.add("Spawned Baium, awake by: " + awake_by, "bosses");

		// set last attack time.
		setLastAttackTime();

		baium.setImobilised(true);
		baium.broadcastPacket(new PlaySound(1, "BS02_A", 1, 0, baium.getLoc()));
		baium.broadcastPacket(new SocialAction(baium.getObjectId(), 2));

		_socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new Social(baium, 3), 15000);

		ThreadPoolManager.getInstance().scheduleGeneral(new EarthquakeTask(baium), 25000);

		_socialTask2 = ThreadPoolManager.getInstance().scheduleGeneral(new Social(baium, 1), 25000);
		_killPcTask = ThreadPoolManager.getInstance().scheduleGeneral(new KillPc(awake_by, baium), 26000);
		_callAngelTask = ThreadPoolManager.getInstance().scheduleGeneral(new CallArchAngel(), 35000);
		_mobiliseTask = ThreadPoolManager.getInstance().scheduleGeneral(new SetMobilised(baium), 35500);

		// move at random.
		Location pos = new Location(Rnd.get(112826, 116241), Rnd.get(15575, 16375), 10078, 0);
		_moveAtRandomTask = ThreadPoolManager.getInstance().scheduleGeneral(new MoveAtRandom(baium, pos), 36000);

		_sleepCheckTask = ThreadPoolManager.getInstance().scheduleGeneral(new CheckLastAttack(), 600000);
	}

	public void onLoad()
	{
		init();
	}

	public void onReload()
	{
		sleepBaium();
	}

	public void onShutdown()
	{}
}