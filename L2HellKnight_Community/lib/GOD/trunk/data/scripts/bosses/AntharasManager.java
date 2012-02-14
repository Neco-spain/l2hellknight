package bosses;

import static l2rt.gameserver.ai.CtrlIntention.AI_INTENTION_ACTIVE;

import java.util.Date;
import java.util.concurrent.ScheduledFuture;

import l2rt.Config;
import l2rt.common.ThreadPoolManager;
import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.idfactory.IdFactory;
import l2rt.gameserver.instancemanager.ZoneManager;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Party;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.L2Zone;
import l2rt.gameserver.model.L2Zone.ZoneType;
import l2rt.gameserver.model.instances.L2BossInstance;
import l2rt.gameserver.model.instances.L2MonsterInstance;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.network.serverpackets.SocialAction;
import l2rt.gameserver.tables.NpcTable;
import l2rt.gameserver.tables.SkillTable;
import l2rt.util.GArray;
import l2rt.util.Location;
import l2rt.util.Log;
import l2rt.util.Rnd;
import bosses.EpicBossState.State;

public class AntharasManager extends Functions implements ScriptFile
{
	private static class AntharasSpawn implements Runnable
	{
		private int _distance = 2550;
		private int _taskId = 0;
		private GArray<L2Player> _players = getPlayersInside();

		AntharasSpawn(int taskId)
		{
			_taskId = taskId;
		}

		public void run()
		{
			int npcId;
			SocialAction sa = null;

			if(_socialTask != null)
			{
				_socialTask.cancel(false);
				_socialTask = null;
			}

			switch(_taskId)
			{
				case 1: // spawn.
					if(_antharas != null)
						return;
						
					npcId = 29068;
					Dying = false;

					// do spawn.
					_antharas = new L2BossInstance(IdFactory.getInstance().getNextId(), NpcTable.getTemplate(npcId));
					_antharas.setSpawnedLoc(_antharasLocation);
					_antharas.onSpawn();
					_antharas.spawnMe(_antharas.getSpawnedLoc());
					_antharas.setImobilised(true);

					_state.setRespawnDate(Rnd.get(FWA_FIXINTERVALOFANTHARAS, FWA_FIXINTERVALOFANTHARAS + FWA_RANDOMINTERVALOFANTHARAS));
					_state.setState(EpicBossState.State.ALIVE);
					_state.update();

					// setting 1st time of minions spawn task.
					int intervalOfBehemoth;
					int intervalOfBomber;

					// Interval of minions is decided by the number of players
					// that invaded the lair.
					if(_players.size() <= FWA_LIMITOFWEAK) // weak
					{
						intervalOfBehemoth = FWA_INTERVALOFBEHEMOTHONWEAK;
						intervalOfBomber = FWA_INTERVALOFBOMBERONWEAK;
					}
					else if(_players.size() >= FWA_LIMITOFNORMAL) // strong
					{
						intervalOfBehemoth = FWA_INTERVALOFBEHEMOTHONSTRONG;
						intervalOfBomber = FWA_INTERVALOFBOMBERONSTRONG;
					}
					else
					// normal
					{
						intervalOfBehemoth = FWA_INTERVALOFBEHEMOTHONNORMAL;
						intervalOfBomber = FWA_INTERVALOFBOMBERONNORMAL;
					}

					// spawn Behemoth.
					_behemothSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new BehemothSpawn(intervalOfBehemoth), 30000);

					// spawn Bomber.
					_bomberSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new BomberSpawn(intervalOfBomber), 30000);

					_socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(2), 16);
					break;
				case 2:
					// set camera.
					for(L2Player pc : _players)
						if(pc.getDistance(_antharas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_antharas, 700, 13, -19, 0, 10000);
						}
						else
							pc.leaveMovieMode();

					_socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(3), 3000);
					break;
				case 3:
					// do social.
					sa = new SocialAction(_antharas.getObjectId(), 1);
					_antharas.broadcastPacket(sa);

					// set camera.
					for(L2Player pc : _players)
						if(pc.getDistance(_antharas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_antharas, 700, 13, 0, 6000, 10000);
						}
						else
							pc.leaveMovieMode();

					_socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(4), 10000);
					break;
				case 4:
					// set camera.
					for(L2Player pc : _players)
						if(pc.getDistance(_antharas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_antharas, 3800, 0, -3, 0, 10000);
						}
						else
							pc.leaveMovieMode();

					_socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(5), 200);
					break;
				case 5:
					// do social.
					sa = new SocialAction(_antharas.getObjectId(), 2);
					_antharas.broadcastPacket(sa);

					// set camera.
					for(L2Player pc : _players)
						if(pc.getDistance(_antharas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_antharas, 1200, 0, -3, 22000, 11000);
						}
						else
							pc.leaveMovieMode();

					_socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(6), 10800);
					break;
				case 6:
					// set camera.
					for(L2Player pc : _players)
						if(pc.getDistance(_antharas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_antharas, 1200, 0, -3, 300, 2000);
						}
						else
							pc.leaveMovieMode();

					_socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(7), 1900);
					break;
				case 7:
					_antharas.abortCast(true);
					// reset camera.
					for(L2Player pc : _players)
						pc.leaveMovieMode();

					_mobiliseTask = ThreadPoolManager.getInstance().scheduleGeneral(new SetMobilised(_antharas), 16);

					_antharas.setRunning();

					// move at random.
					if(FWA_MOVEATRANDOM)
					{
						Location pos = new Location(Rnd.get(175000, 178500), Rnd.get(112400, 116000), -7707, 0);
						_moveAtRandomTask = ThreadPoolManager.getInstance().scheduleGeneral(new MoveAtRandom(_antharas, pos), 32);
					}

					_sleepCheckTask = ThreadPoolManager.getInstance().scheduleGeneral(new CheckLastAttack(), 600000);
					break;
			}
		}
	}

	// do spawn Behemoth.
	private static class BehemothSpawn implements Runnable
	{
		private int _interval;

		public BehemothSpawn(int interval)
		{
			_interval = interval;
		}

		public void run()
		{
			L2MonsterInstance behemoth = new L2MonsterInstance(IdFactory.getInstance().getNextId(), NpcTable.getTemplate(29069));
			behemoth.onSpawn();
			behemoth.setSpawnedLoc(new Location(Rnd.get(175000, 179900), Rnd.get(112400, 116000), -7709));
			behemoth.spawnMe(behemoth.getSpawnedLoc());
			_monsters.add(behemoth);

			if(_behemothSpawnTask != null)
			{
				_behemothSpawnTask.cancel(false);
				_behemothSpawnTask = null;
			}

			// repeat.
			_behemothSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new BehemothSpawn(_interval), _interval);
		}
	}

	// do spawn Bomber.
	private static class BomberSpawn implements Runnable
	{
		private int _interval;

		public BomberSpawn(int interval)
		{
			_interval = interval;
		}

		public void run()
		{
			int npcId = Rnd.get(29070, 29076);

			L2MonsterInstance bomber = new L2MonsterInstance(IdFactory.getInstance().getNextId(), NpcTable.getTemplate(npcId));
			bomber.onSpawn();
			bomber.setSpawnedLoc(new Location(Rnd.get(175000, 179900), Rnd.get(112400, 116000), -7709));
			bomber.spawnMe(bomber.getSpawnedLoc());
			_monsters.add(bomber);
			// set self destruction.
			_selfDestructionTask = ThreadPoolManager.getInstance().scheduleGeneral(new SelfDestructionOfBomber(bomber), 3000);

			if(_bomberSpawnTask != null)
			{
				_bomberSpawnTask.cancel(false);
				_bomberSpawnTask = null;
			}

			// repeat.
			_bomberSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new BomberSpawn(_interval), _interval);
		}
	}

	private static class CheckLastAttack implements Runnable
	{
		public void run()
		{
			if(_state.getState().equals(EpicBossState.State.ALIVE))
				if(_lastAttackTime + FWA_LIMITUNTILSLEEP < System.currentTimeMillis())
					sleep();
				else
					_sleepCheckTask = ThreadPoolManager.getInstance().scheduleGeneral(new CheckLastAttack(), 60000);
		}
	}

	// do spawn teleport cube.
	private static class CubeSpawn implements Runnable
	{
		public void run()
		{
			if(_behemothSpawnTask != null)
			{
				_behemothSpawnTask.cancel(false);
				_behemothSpawnTask = null;
			}
			if(_bomberSpawnTask != null)
			{
				_bomberSpawnTask.cancel(false);
				_bomberSpawnTask = null;
			}
			if(_selfDestructionTask != null)
			{
				_selfDestructionTask.cancel(false);
				_selfDestructionTask = null;
			}

			_teleportCube = new L2NpcInstance(IdFactory.getInstance().getNextId(), NpcTable.getTemplate(_teleportCubeId));
			_teleportCube.setCurrentHpMp(_teleportCube.getMaxHp(), _teleportCube.getMaxMp(), true);
			_teleportCube.setSpawnedLoc(_teleportCubeLocation);
			_teleportCube.spawnMe(_teleportCube.getSpawnedLoc());
		}
	}

	// at end of interval.
	private static class IntervalEnd implements Runnable
	{
		public void run()
		{
			_state.setState(EpicBossState.State.NOTSPAWN);
			_state.update();
		}
	}

	// Move at random on after Antharas appears.
	private static class MoveAtRandom implements Runnable
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

	private static class onAnnihilated implements Runnable
	{
		public void run()
		{
			sleep();
		}
	}

	// do self destruction.
	private static class SelfDestructionOfBomber implements Runnable
	{
		private L2NpcInstance _bomber;

		public SelfDestructionOfBomber(L2NpcInstance bomber)
		{
			_bomber = bomber;
		}

		public void run()
		{
			L2Skill skill = null;
			switch(_bomber.getNpcId())
			{
				case 29070:
				case 29071:
				case 29072:
				case 29073:
				case 29074:
				case 29075:
					skill = SkillTable.getInstance().getInfo(5097, 1);
					break;
				case 29076:
					skill = SkillTable.getInstance().getInfo(5094, 1);
					break;
			}

			_bomber.doCast(skill, null, false);
		}
	}

	// action is enabled the boss.
	private static class SetMobilised implements Runnable
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

	// location of teleport cube.
	private static final int _teleportCubeId = 31859;
	private static final int _antharasId = 29019;

	private static final Location _teleportCubeLocation = new Location(177615, 114941, -7709, 0);
	private static final Location _antharasLocation = new Location(181323, 114850, -7623, 32542);

	private static L2BossInstance _antharas = null;
	private static L2NpcInstance _teleportCube = null;

	// instance of monsters.
	private static GArray<L2NpcInstance> _monsters = new GArray<L2NpcInstance>();

	// tasks.
	private static ScheduledFuture<?> _cubeSpawnTask = null;
	private static ScheduledFuture<?> _monsterSpawnTask = null;
	private static ScheduledFuture<?> _intervalEndTask = null;
	private static ScheduledFuture<?> _socialTask = null;
	private static ScheduledFuture<?> _mobiliseTask = null;
	private static ScheduledFuture<?> _behemothSpawnTask = null;
	private static ScheduledFuture<?> _bomberSpawnTask = null;
	private static ScheduledFuture<?> _selfDestructionTask = null;
	private static ScheduledFuture<?> _moveAtRandomTask = null;
	private static ScheduledFuture<?> _sleepCheckTask = null;
	private static ScheduledFuture<?> _onAnnihilatedTask = null;

	private static final int ANTHARAS_CIRCLET = 8568;

	private static EpicBossState _state;
	private static L2Zone _zone;
	private static long _lastAttackTime = 0;

	private static final boolean FWA_MOVEATRANDOM = true;

	private static final int FWA_LIMITUNTILSLEEP = 30 * 60000;
	private static final int FWA_FIXINTERVALOFANTHARAS = 11 * 24 * 60 * 60000; // 11 суток
	private static final int FWA_RANDOMINTERVALOFANTHARAS = 0 * 24 * 60 * 60000; // без разброса
	private static final int FWA_APPTIMEOFANTHARAS = 20 * 60000; // 20 минут ожидание перед респом
	private static final int FWA_LIMITOFWEAK = 299;
	private static final int FWA_LIMITOFNORMAL = 399;
	private static final int FWA_INTERVALOFBEHEMOTHONWEAK = 8 * 60000;
	private static final int FWA_INTERVALOFBEHEMOTHONNORMAL = 5 * 60000;
	private static final int FWA_INTERVALOFBEHEMOTHONSTRONG = 3 * 60000;
	private static final int FWA_INTERVALOFBOMBERONWEAK = 6 * 60000;
	private static final int FWA_INTERVALOFBOMBERONNORMAL = 4 * 60000;
	private static final int FWA_INTERVALOFBOMBERONSTRONG = 3 * 60000;

	private static final int ANTHARAS_WEAK = 29066;
	private static final int ANTHARAS_NORMAL = 29067;
	private static final int ANTHARAS_STRONG = 29068;

	private static boolean Dying = false;

	private static void banishForeigners()
	{
		for(L2Player player : getPlayersInside())
			player.teleToClosestTown();
	}

	private synchronized static void checkAnnihilated()
	{
		if(_onAnnihilatedTask == null && isPlayersAnnihilated())
			_onAnnihilatedTask = ThreadPoolManager.getInstance().scheduleGeneral(new onAnnihilated(), 5000);
	}

	private static GArray<L2Player> getPlayersInside()
	{
		return getZone().getInsidePlayers();
	}

	private static int getRespawnInterval()
	{
		return (int) (Config.ALT_RAID_RESPAWN_MULTIPLIER * (FWA_FIXINTERVALOFANTHARAS + Rnd.get(0, FWA_RANDOMINTERVALOFANTHARAS)));
	}

	public static L2Zone getZone()
	{
		return _zone;
	}

	private static boolean isPlayersAnnihilated()
	{
		for(L2Player pc : getPlayersInside())
			if(!pc.isDead())
				return false;
		return true;
	}

	private static void onAntharasDie(L2Character killer)
	{
		if(Dying)
			return;

		Dying = true;
		_state.setRespawnDate(getRespawnInterval());
		_state.setState(EpicBossState.State.INTERVAL);
		_state.update();

		Log.add("Antharas died", "bosses");

		_cubeSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new CubeSpawn(), 10000);

		if(killer != null && killer.isPlayable())
		{
			L2Player pc = killer.getPlayer();
			if(pc == null)
				return;
			L2Party party = pc.getParty();
			if(party != null)
			{
				for(L2Player partyMember : party.getPartyMembers())
					if(partyMember != null && pc.isInRange(partyMember, 5000) && partyMember.getInventory().getItemByItemId(ANTHARAS_CIRCLET) == null)
						partyMember.getInventory().addItem(ANTHARAS_CIRCLET, 1);
			}
			else if(pc.getInventory().getItemByItemId(ANTHARAS_CIRCLET) == null)
				pc.getInventory().addItem(ANTHARAS_CIRCLET, 1);
		}
	}

	public static void OnDie(L2Character self, L2Character killer)
	{
		if(self == null)
			return;
		if(self.isPlayer() && _state != null && _state.getState() == State.ALIVE && _zone != null && _zone.checkIfInZone(self.getX(), self.getY()))
			checkAnnihilated();
		else if(self.isNpc() && (self.getNpcId() == ANTHARAS_WEAK || self.getNpcId() == ANTHARAS_NORMAL || self.getNpcId() == ANTHARAS_STRONG))
			onAntharasDie(killer);
	}

	private static void setIntervalEndTask()
	{
		setUnspawn();

		if(_state.getState().equals(EpicBossState.State.ALIVE))
		{
			_state.setState(EpicBossState.State.NOTSPAWN);
			_state.update();
			return;
		}

		if(!_state.getState().equals(EpicBossState.State.INTERVAL))
		{
			_state.setRespawnDate(getRespawnInterval());
			_state.setState(EpicBossState.State.INTERVAL);
			_state.update();
		}

		_intervalEndTask = ThreadPoolManager.getInstance().scheduleGeneral(new IntervalEnd(), _state.getInterval());
	}

	// clean Antharas's lair.
	private static void setUnspawn()
	{
		// eliminate players.
		banishForeigners();

		if(_antharas != null)
			_antharas.deleteMe();
		_antharas = null;

		if(_teleportCube != null)
			_teleportCube.deleteMe();
		_teleportCube = null;

		for(L2NpcInstance mob : _monsters)
			mob.deleteMe();
		_monsters.clear();

		// not executed tasks is canceled.
		if(_cubeSpawnTask != null)
		{
			_cubeSpawnTask.cancel(false);
			_cubeSpawnTask = null;
		}
		if(_monsterSpawnTask != null)
		{
			_monsterSpawnTask.cancel(false);
			_monsterSpawnTask = null;
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
		if(_behemothSpawnTask != null)
		{
			_behemothSpawnTask.cancel(false);
			_behemothSpawnTask = null;
		}
		if(_bomberSpawnTask != null)
		{
			_bomberSpawnTask.cancel(false);
			_bomberSpawnTask = null;
		}
		if(_selfDestructionTask != null)
		{
			_selfDestructionTask.cancel(false);
			_selfDestructionTask = null;
		}
		if(_moveAtRandomTask != null)
		{
			_moveAtRandomTask.cancel(false);
			_moveAtRandomTask = null;
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

	private void init()
	{
		_state = new EpicBossState(_antharasId);
		_zone = ZoneManager.getInstance().getZoneById(ZoneType.epic, 702002, false);

		System.out.println("AntharasManager: State of Antharas is " + _state.getState() + ".");
		if(!_state.getState().equals(EpicBossState.State.NOTSPAWN))
			setIntervalEndTask();

		Date dt = new Date(_state.getRespawnDate());
		System.out.println("AntharasManager: Next spawn date of Antharas is " + dt + ".");
	}

	private static void sleep()
	{
		setUnspawn();
		if(_state.getState().equals(EpicBossState.State.ALIVE))
		{
			_state.setState(EpicBossState.State.NOTSPAWN);
			_state.update();
		}
	}

	public static void setLastAttackTime()
	{
		_lastAttackTime = System.currentTimeMillis();
	}

	// setting Antharas spawn task.
	public synchronized static void setAntharasSpawnTask()
	{
		if(_monsterSpawnTask == null)
			_monsterSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(1), FWA_APPTIMEOFANTHARAS);
	}

	public static boolean isEnableEnterToLair()
	{
		return _state.getState() == EpicBossState.State.NOTSPAWN;
	}

	public void onLoad()
	{
		init();
	}

	public void onReload()
	{
		sleep();
	}

	public void onShutdown()
	{}
}