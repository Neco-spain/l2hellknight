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
import l2rt.gameserver.model.L2Party;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Spawn;
import l2rt.gameserver.model.L2Zone;
import l2rt.gameserver.model.L2Zone.ZoneType;
import l2rt.gameserver.model.instances.L2BossInstance;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.network.serverpackets.SocialAction;
import l2rt.gameserver.tables.NpcTable;
import l2rt.gameserver.templates.L2NpcTemplate;
import l2rt.util.GArray;
import l2rt.util.Location;
import l2rt.util.Log;
import l2rt.util.Rnd;
import bosses.EpicBossState.State;

public class ValakasManager extends Functions implements ScriptFile
{
	private static class CheckLastAttack implements Runnable
	{
		public void run()
		{
			if(_state.getState().equals(EpicBossState.State.ALIVE))
				if(_lastAttackTime + FWV_LIMITUNTILSLEEP < System.currentTimeMillis())
					sleep();
				else
					_sleepCheckTask = ThreadPoolManager.getInstance().scheduleGeneral(new CheckLastAttack(), 60000);
		}
	}

	private static class CubeSpawn implements Runnable
	{
		public void run()
		{
			for(L2Spawn spawnDat : _teleportCubeSpawn)
				_teleportCube.add(spawnDat.doSpawn(true));
		}
	}

	private static class IntervalEnd implements Runnable
	{
		public void run()
		{
			_state.setState(EpicBossState.State.NOTSPAWN);
			_state.update();
		}
	}

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

	// Do spawn Valakas.
	private static class ValakasSpawn implements Runnable
	{
		private int _distance = 2550;
		private int _taskId;
		private L2BossInstance _valakas = null;
		private GArray<L2Player> _players = getPlayersInside();

		ValakasSpawn(int taskId, L2BossInstance valakas)
		{
			_taskId = taskId;
			_valakas = valakas;
		}

		public void run()
		{
			SocialAction sa = null;

			if(_socialTask != null)
			{
				_socialTask.cancel(false);
				_socialTask = null;
			}

			switch(_taskId)
			{
				case 1:
					Dying = false;

					// Do spawn.
					L2Spawn valakasSpawn = _monsterSpawn.get(Valakas);
					_valakas = (L2BossInstance) valakasSpawn.doSpawn(true);
					_monsters.add(_valakas);
					_valakas.setImobilised(true);

					_state.setRespawnDate(Rnd.get(FWV_FIXINTERVALOFVALAKAS, FWV_FIXINTERVALOFVALAKAS + FWV_RANDOMINTERVALOFVALAKAS));
					_state.setState(EpicBossState.State.ALIVE);
					_state.update();

					_socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new ValakasSpawn(2, _valakas), 16);
					break;
				case 2:
					// Do social.
					sa = new SocialAction(_valakas.getObjectId(), 1);
					_valakas.broadcastPacket(sa);

					// Set camera.
					for(L2Player pc : _players)
						if(pc.getDistance(_valakas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 1800, 180, -1, 1500, 15000);
						}
						else
							pc.leaveMovieMode();

					_socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new ValakasSpawn(3, _valakas), 1500);
					break;
				case 3:
					// Set camera.
					for(L2Player pc : _players)
						if(pc.getDistance(_valakas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 1300, 180, -5, 3000, 15000);
						}
						else
							pc.leaveMovieMode();

					_socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new ValakasSpawn(4, _valakas), 3300);
					break;
				case 4:
					// Set camera.
					for(L2Player pc : _players)
						if(pc.getDistance(_valakas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 500, 180, -8, 600, 15000);
						}
						else
							pc.leaveMovieMode();

					_socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new ValakasSpawn(5, _valakas), 1300);
					break;
				case 5:
					// Set camera.
					for(L2Player pc : _players)
						if(pc.getDistance(_valakas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 1200, 180, -5, 300, 15000);
						}
						else
							pc.leaveMovieMode();

					_socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new ValakasSpawn(6, _valakas), 1600);
					break;
				case 6:
					// Set camera.
					for(L2Player pc : _players)
						if(pc.getDistance(_valakas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 2800, 250, 70, 0, 15000);
						}
						else
							pc.leaveMovieMode();

					_socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new ValakasSpawn(7, _valakas), 200);
					break;
				case 7:
					// Set camera.
					for(L2Player pc : _players)
						if(pc.getDistance(_valakas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 2600, 30, 60, 3400, 15000);
						}
						else
							pc.leaveMovieMode();

					_socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new ValakasSpawn(8, _valakas), 5700);
					break;
				case 8:
					// Set camera.
					for(L2Player pc : _players)
						if(pc.getDistance(_valakas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 700, 150, -65, 0, 15000);
						}
						else
							pc.leaveMovieMode();

					_socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new ValakasSpawn(9, _valakas), 1400);
					break;
				case 9:
					// Set camera.
					for(L2Player pc : _players)
						if(pc.getDistance(_valakas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 1200, 150, -55, 2900, 15000);
						}
						else
							pc.leaveMovieMode();

					_socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new ValakasSpawn(10, _valakas), 6700);
					break;
				case 10:
					// Set camera.
					for(L2Player pc : _players)
						if(pc.getDistance(_valakas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 750, 170, -10, 1700, 5700);
						}
						else
							pc.leaveMovieMode();

					_socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new ValakasSpawn(11, _valakas), 3700);
					break;
				case 11:
					// Set camera.
					for(L2Player pc : _players)
						if(pc.getDistance(_valakas) <= _distance)
						{
							pc.enterMovieMode();
							pc.specialCamera(_valakas, 840, 170, -5, 1200, 2000);
						}
						else
							pc.leaveMovieMode();

					_socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new ValakasSpawn(12, _valakas), 2000);
					break;
				case 12:
					// Reset camera.
					for(L2Player pc : _players)
						pc.leaveMovieMode();

					_mobiliseTask = ThreadPoolManager.getInstance().scheduleGeneral(new SetMobilised(_valakas), 16);

					// Move at random.
					if(FWV_MOVEATRANDOM)
					{
						Location pos = new Location(Rnd.get(211080, 214909), Rnd.get(-115841, -112822), -1662, 0);
						_moveAtRandomTask = ThreadPoolManager.getInstance().scheduleGeneral(new MoveAtRandom(_valakas, pos), 32);
					}

					_sleepCheckTask = ThreadPoolManager.getInstance().scheduleGeneral(new CheckLastAttack(), 600000);
					break;
			}
		}
	}

	private static final int _teleportCubeLocation[][] = { { 214880, -116144, -1644, 0 }, { 213696, -116592, -1644, 0 },
			{ 212112, -116688, -1644, 0 }, { 211184, -115472, -1664, 0 }, { 210336, -114592, -1644, 0 },
			{ 211360, -113904, -1644, 0 }, { 213152, -112352, -1644, 0 }, { 214032, -113232, -1644, 0 },
			{ 214752, -114592, -1644, 0 }, { 209824, -115568, -1421, 0 }, { 210528, -112192, -1403, 0 },
			{ 213120, -111136, -1408, 0 }, { 215184, -111504, -1392, 0 }, { 215456, -117328, -1392, 0 },
			{ 213200, -118160, -1424, 0 } };

	private static GArray<L2Spawn> _teleportCubeSpawn = new GArray<L2Spawn>();
	private static GArray<L2NpcInstance> _teleportCube = new GArray<L2NpcInstance>();
	private static Map<Integer, L2Spawn> _monsterSpawn = new FastMap<Integer, L2Spawn>().setShared(true);
	private static GArray<L2NpcInstance> _monsters = new GArray<L2NpcInstance>();

	// Tasks.
	private static ScheduledFuture<?> _cubeSpawnTask = null;
	private static ScheduledFuture<?> _monsterSpawnTask = null;
	private static ScheduledFuture<?> _intervalEndTask = null;
	private static ScheduledFuture<?> _socialTask = null;
	private static ScheduledFuture<?> _mobiliseTask = null;
	private static ScheduledFuture<?> _moveAtRandomTask = null;
	private static ScheduledFuture<?> _respawnValakasTask = null;
	private static ScheduledFuture<?> _sleepCheckTask = null;
	private static ScheduledFuture<?> _onAnnihilatedTask = null;

	private static final int Valakas = 29028;
	private static final int ValakasDummy = 32123;
	private static final int _teleportCubeId = 31759;
	private static final int VALAKAS_CIRCLET = 8567;
	private static EpicBossState _state;
	private static L2Zone _zone;

	private static long _lastAttackTime = 0;

	private static final boolean FWV_MOVEATRANDOM = true;

	private static final int FWV_LIMITUNTILSLEEP = 30 * 60000;
	private static final int FWV_APPTIMEOFVALAKAS = 20 * 60000;
	private static final int FWV_FIXINTERVALOFVALAKAS = 11 * 24 * 60 * 60000;
	private static final int FWV_RANDOMINTERVALOFVALAKAS = 0 * 24 * 60 * 60000;

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
		return (int) (Config.ALT_RAID_RESPAWN_MULTIPLIER * (FWV_FIXINTERVALOFVALAKAS + Rnd.get(0, FWV_RANDOMINTERVALOFVALAKAS)));
	}

	public static L2Zone getZone()
	{
		return _zone;
	}

	private static void init()
	{
		_state = new EpicBossState(Valakas);
		_zone = ZoneManager.getInstance().getZoneById(ZoneType.epic, 702003, false);

		// Setting spawn data of monsters.
		try
		{
			L2NpcTemplate template1;
			L2Spawn tempSpawn;

			// Valakas.
			template1 = NpcTable.getTemplate(Valakas);
			tempSpawn = new L2Spawn(template1);
			tempSpawn.setLocx(212852);
			tempSpawn.setLocy(-114842);
			tempSpawn.setLocz(-1632);
			//tempSpawn.setHeading(22106);
			tempSpawn.setHeading(833);
			tempSpawn.setAmount(1);
			tempSpawn.stopRespawn();
			_monsterSpawn.put(Valakas, tempSpawn);

			// Dummy Valakas.
			template1 = NpcTable.getTemplate(ValakasDummy);
			tempSpawn = new L2Spawn(template1);
			tempSpawn.setLocx(212852);
			tempSpawn.setLocy(-114842);
			tempSpawn.setLocz(-1632);
			//tempSpawn.setHeading(22106);
			tempSpawn.setHeading(833);
			tempSpawn.setAmount(1);
			tempSpawn.stopRespawn();
			_monsterSpawn.put(ValakasDummy, tempSpawn);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		// Setting spawn data of teleport cube.
		try
		{
			L2NpcTemplate Cube = NpcTable.getTemplate(_teleportCubeId);
			L2Spawn spawnDat;
			for(int[] element : _teleportCubeLocation)
			{
				spawnDat = new L2Spawn(Cube);
				spawnDat.setAmount(1);
				spawnDat.setLocx(element[0]);
				spawnDat.setLocy(element[1]);
				spawnDat.setLocz(element[2]);
				spawnDat.setHeading(element[3]);
				spawnDat.setRespawnDelay(60);
				spawnDat.setLocation(0);
				_teleportCubeSpawn.add(spawnDat);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		System.out.println("ValakasManager: State of Valakas is " + _state.getState() + ".");
		if(!_state.getState().equals(EpicBossState.State.NOTSPAWN))
			setIntervalEndTask();

		Date dt = new Date(_state.getRespawnDate());
		System.out.println("ValakasManager: Next spawn date of Valakas is " + dt + ".");
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
		if(self.isPlayer() && _state != null && _state.getState() == State.ALIVE && _zone != null && _zone.checkIfInZone(self.getX(), self.getY()))
			checkAnnihilated();
		else if(self.isNpc() && self.getNpcId() == Valakas)
			onValakasDie(killer);
	}

	private static void onValakasDie(L2Character killer)
	{
		if(Dying)
			return;

		Dying = true;
		_state.setRespawnDate(getRespawnInterval());
		_state.setState(EpicBossState.State.INTERVAL);
		_state.update();

		Log.add("Valakas died", "bosses");

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
					if(partyMember != null && pc.isInRange(partyMember, 5000) && partyMember.getInventory().getItemByItemId(VALAKAS_CIRCLET) == null)
						partyMember.getInventory().addItem(VALAKAS_CIRCLET, 1);
			}
			else if(pc.getInventory().getItemByItemId(VALAKAS_CIRCLET) == null)
				pc.getInventory().addItem(VALAKAS_CIRCLET, 1);
		}
	}

	// Start interval.
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

	// Clean Valakas's lair.
	private static void setUnspawn()
	{
		// Eliminate players.
		banishForeigners();

		// Delete monsters.
		for(L2NpcInstance mob : _monsters)
		{
			mob.getSpawn().stopRespawn();
			mob.deleteMe();
		}
		_monsters.clear();

		// Delete teleport cube.
		for(L2NpcInstance cube : _teleportCube)
		{
			cube.getSpawn().stopRespawn();
			cube.deleteMe();
		}
		_teleportCube.clear();

		// Not executed tasks is canceled.
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
		if(_respawnValakasTask != null)
		{
			_respawnValakasTask.cancel(false);
			_respawnValakasTask = null;
		}
		if(_onAnnihilatedTask != null)
		{
			_onAnnihilatedTask.cancel(false);
			_onAnnihilatedTask = null;
		}
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

	// Setting Valakas spawn task.
	public synchronized static void setValakasSpawnTask()
	{
		if(_monsterSpawnTask == null)
			_monsterSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new ValakasSpawn(1, null), FWV_APPTIMEOFVALAKAS);
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