package l2rt.gameserver.instancemanager;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.Arrays;
import javolution.util.FastList;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Effect;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.L2Spawn;
import l2rt.gameserver.model.L2Summon;
import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.gameserver.network.serverpackets.ExCubeGameAddPlayer;
import l2rt.gameserver.network.serverpackets.ExCubeGameChangePoints;
import l2rt.gameserver.network.serverpackets.ExCubeGameChangeTimeToStart;
import l2rt.gameserver.network.serverpackets.ExCubeGameEnd;
import l2rt.gameserver.network.serverpackets.ExCubeGameExtendedChangePoints;
import l2rt.gameserver.network.serverpackets.ExCubeGameRemovePlayer;
import l2rt.gameserver.network.serverpackets.ExCubeGameTeamList;
import l2rt.gameserver.network.serverpackets.SkillList;
import l2rt.gameserver.tables.NpcTable;
import l2rt.gameserver.tables.SkillTable;
import l2rt.gameserver.templates.L2NpcTemplate;
import l2rt.util.Rnd;
import l2rt.common.ThreadPoolManager;
import l2rt.gameserver.model.instances.L2MonsterInstance;
import l2rt.gameserver.network.serverpackets.NpcInfo;
import l2rt.util.GArray;
import l2rt.extensions.scripts.Functions;

public class GameHandyManager
{
	private static GameHandyManager _instance;

	static int[][][] _spawnLocs =
	{
		{ // SpawnCoords for 1 arena
			{ -57814, -62257, -2370},
			{ -57817, -62734, -2370},
			{ -58365, -62634, -2370},
			{ -58359, -62181, -2370},
			{ -58053, -62180, -2370}
		},
		{ // SpawnCoords for 2 arena
			{ -57817, -63397, -2408},
			{ -57817, -63711, -2408},
			{ -58290, -62634, -2408},
			{ -58276, -63406, -2408},
			{ -58050, -63325, -2408}
		},
		{ // SpawnCoords for 3 arena
			{ -56654, -63388, -2411},
			{ -56649, -63709, -2411},
			{ -57112, -63702, -2411},
			{ -57112, -63404, -2411},
			{ -56885, -63332, -2411}
		},
		{ // SpawnCoords for 4 arena
			{ -56654, -62261, -2400},
			{ -56655, -62566, -2400},
			{ -57104, -62574, -2400},
			{ -57110, -62268, -2400},
			{ -56883, -62200, -2400}
		}
	};

	static int[][] _TpLocs =
	{
		{ -58051, -62424, -2408}, // TP arena 1
		{ -58051, -63562, -2408}, // TP arena 2
		{ -56888, -63562, -2408}, // TP arena 3
		{ -56888, -62424, -2408}, // TP arena 4
	};

	private static final int BLUE_TEAM = 1;
	private static final int RED_TEAM = 2;
	private static final int[] BUFFS_WHITELIST = {6035,6036};
	private static TIntObjectHashMap<HandyArena> _arenas = new TIntObjectHashMap<HandyArena>(4);

	static
	{
		_arenas.put(1, new HandyArena(1));
		_arenas.put(2, new HandyArena(2));
		_arenas.put(3, new HandyArena(3));
		_arenas.put(4, new HandyArena(4));
	}

	public static GameHandyManager getInstance()
	{
		if(_instance == null)
			_instance = new GameHandyManager();
		return _instance;
	}

	protected static final class HandyArena
	{
		public int _id = 0;
		public long _startTime = 0;
		public FastList<Integer> _RedTeamPlayers = new FastList<Integer>();
		public FastList<Integer> _BlueTeamPlayers = new FastList<Integer>();
		public FastList<Integer> _tempPlayers = new FastList<Integer>();
		public FastList<L2MonsterInstance> _mobs = new FastList<L2MonsterInstance>();
		public boolean _canRegister = true;
		public int _Red_playerTotalKill = 0;
		public int _Blue_playerTotalKill = 0;
		private GArray<L2MonsterInstance> _spawns1 = new GArray<L2MonsterInstance>();

		public HandyArena(int id)
		{
			_id = id;
		}
	}

	public static boolean registerPlayer(L2Player player, int arenaId)
	{
		int objectId = player.getObjectId();
		int TeamId = 1;

		if(getPlayerArena(player) != 0)
			return false;

		HandyArena arena = _arenas.get(arenaId);

		if(arena._tempPlayers.contains(objectId) || !arena._canRegister)
			return false;

		if(arena._RedTeamPlayers.size() > 5 && arena._BlueTeamPlayers.size() > 5)
			return false;

		if(arena._RedTeamPlayers.size() == arena._BlueTeamPlayers.size())
			TeamId = Rnd.get(1, 2);
		else
			TeamId = (arena._RedTeamPlayers.size() > arena._BlueTeamPlayers.size()) ? BLUE_TEAM : RED_TEAM;
		arena._tempPlayers.add(objectId);

		if(TeamId == RED_TEAM)
			arena._RedTeamPlayers.add(objectId);
		else
			arena._BlueTeamPlayers.add(objectId);

		if(arena._RedTeamPlayers.size() >= 2 && arena._BlueTeamPlayers.size() >= 2) //for test maybe correct!
		{
			for(int objId : arena._BlueTeamPlayers)
			{
				L2Player plr = L2ObjectsStorage.getPlayer(objId);
				if(plr != null)
					plr.sendPacket(new ExCubeGameChangeTimeToStart(120));
			}
			for(int objId : arena._RedTeamPlayers)
			{
				L2Player plr = L2ObjectsStorage.getPlayer(objId);
				if(plr != null)
					plr.sendPacket(new ExCubeGameChangeTimeToStart(120));
			}
			ThreadPoolManager.getInstance().scheduleGeneral(new startGamesHandy(arena._id), 120000); //for test only init time 120 sec
		}

		return true;
	}

	public static void StartHandyGame(int arenaId)
	{
		HandyArena arena = _arenas.get(arenaId);
		arena._canRegister = false;
		arena._startTime = System.currentTimeMillis();
		FastList<L2Player> allPlayers = new FastList<L2Player>();
		FastList<L2Player> bluePlayers = new FastList<L2Player>();
		FastList<L2Player> redPlayers = new FastList<L2Player>();

		for(int objId : arena._BlueTeamPlayers)
		{
			L2Player player = L2ObjectsStorage.getPlayer(objId);
			if(player != null)
			{
				//player._HandyGamePoints = 0;
				player.setTransformation(122);
				player.altUseSkill(SkillTable.getInstance().getInfo(6036, 1), player);
				
				player.sendPacket(new SkillList(null));
				
				player.setTeam(BLUE_TEAM, false);
				doTeleport(player, _TpLocs[arena._id][0], _TpLocs[arena._id][1], _TpLocs[arena._id][2]);
				removeBuffs(player);
				allPlayers.add(player);
				bluePlayers.add(player);
				player.sendPacket(new ExCubeGameChangePoints(300, arena._Blue_playerTotalKill, arena._Red_playerTotalKill));
				player.removeSkill(SkillTable.getInstance().getInfo(619, 1), false);
				player.sendPacket(new SkillList(player));
			}
		}
		for(int objId : arena._RedTeamPlayers)
		{
			L2Player player = L2ObjectsStorage.getPlayer(objId);
			if(player != null)
			{
				//player._HandyGamePoints = 0;
				player.setTransformation(121);
				player.altUseSkill(SkillTable.getInstance().getInfo(6035, 1), player);
				player.sendPacket(new SkillList(null));
				player.setTeam(RED_TEAM, false);
				doTeleport(player, _TpLocs[arena._id][0], _TpLocs[arena._id][1], _TpLocs[arena._id][2]);
				removeBuffs(player);
				allPlayers.add(player);
				redPlayers.add(player);
				player.sendPacket(new ExCubeGameChangePoints(300, arena._Blue_playerTotalKill, arena._Red_playerTotalKill));
				player.removeSkill(SkillTable.getInstance().getInfo(619, 1), false);
				player.sendPacket(new SkillList(player));
			}
		}

		for(L2Player player : allPlayers)
		{
			for(L2Player plr : allPlayers)
				player.sendPacket(new ExCubeGameAddPlayer(plr, plr.getTeam() == RED_TEAM));
		}

		for(L2Player player : allPlayers)
			player.sendPacket(new ExCubeGameTeamList(redPlayers, bluePlayers, arena._id));

		ThreadPoolManager.getInstance().scheduleGeneral(new spawnMobs(arena._id), 10000L);
		ThreadPoolManager.getInstance().scheduleGeneral(new finishGameHandy(arena._id), 300000L); //test end game
	}

	private static void globalDespawn(HandyArena arena)
	{
		
		for(L2MonsterInstance mob : arena._spawns1)
		{
			//DayNightSpawnManager.deleteMobs(_spawns1);
			//SpawnTable.getInstance().deleteSpawn(mob);
			//mob.doDie(killer)
			//mob = null;
			//SpawnTable.getInstance().deleteSpawn(mob, false);
			//mob.deleteMe();
			Functions.npcSay(mob, "You're a hard worker, Rayla!");
		}
		
		arena._mobs.clear();
	}

	public static void doTeleport(L2Player player, int x, int y, int z)
	{
		if( !player.isOnline())
			return;
		player.teleToLocation(x, y, z, 0);

		L2Summon pet = player.getPet();
		if(pet != null)
			pet.teleToLocation(x, y, z, 0);
	}
	private static final void removeBuffs(L2Character ch)
	{
		L2Skill skill;
		for(L2Effect e : ch.getEffectList().getAllEffects())
		{
			if(e == null)
				continue;
			skill = e.getSkill();
			//if(skill.getSkillsByType(SkillType.DEBUFF) != null)
				//continue;
			if(Arrays.binarySearch(BUFFS_WHITELIST, skill.getId()) >= 0)
				continue;
			e.exit();
		}
		if(ch.getPet() == null)
			return;
		for(L2Effect e : ch.getPet().getEffectList().getAllEffects())
		{
			if(e == null)
				continue;
			skill = e.getSkill();
			//if(skill.getSkillsByType(SkillType.DEBUFF) != null)
				//continue;
			if(Arrays.binarySearch(BUFFS_WHITELIST, skill.getId()) >= 0)
				continue;
			e.exit();
		}
	}

	private static L2Spawn spawnNpc(int npcId, int x, int y, int z, int heading, int respawnTime, int instanceId)
	{
		NpcTable.getInstance();
		L2NpcTemplate npcTemplate = NpcTable.getTemplate(npcId);
		L2Spawn spawnDat = null;
		try
		{
			spawnDat = new L2Spawn(npcTemplate);
			spawnDat.setAmount(1);
			spawnDat.setLocx(x);
			spawnDat.setLocy(y);
			spawnDat.setLocz(z);
			spawnDat.setHeading(heading);
			spawnDat.setRespawnDelay(respawnTime);
			spawnDat.setReflection(instanceId);
			//RaidBossSpawnManager.getInstance().addNewSpawn(spawnDat);
			DayNightSpawnManager.getInstance().addDayMob(spawnDat);
			spawnDat.init();
			spawnDat.startRespawn();
			if(respawnTime == 0)
				spawnDat.stopRespawn();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return spawnDat;
	}
	public static void increaseKill(int Team, int arenaId, L2Player player)
	{
		HandyArena arena = _arenas.get(arenaId);
		if(Team == RED_TEAM)
			arena._Blue_playerTotalKill += 1;
		else if(Team == BLUE_TEAM)
			arena._Red_playerTotalKill += 1;

		int timeLeft = (int) ((arena._startTime + 300000 - System.currentTimeMillis()) / 1000);
		for(int objId : arena._RedTeamPlayers)
		{
			L2Player plr = L2ObjectsStorage.getPlayer(objId);
			if(player != null)
			{
				plr.sendPacket(new ExCubeGameChangePoints(timeLeft, arena._Blue_playerTotalKill, arena._Red_playerTotalKill));
				plr.sendPacket(new ExCubeGameExtendedChangePoints(timeLeft, arena._Blue_playerTotalKill, arena._Red_playerTotalKill, player.getTeam() == RED_TEAM, player, 0));
			}
		}
		for(int objId : arena._BlueTeamPlayers)
		{
			L2Player plr = L2ObjectsStorage.getPlayer(objId);
			if(plr != null)
			{
				plr.sendPacket(new ExCubeGameChangePoints(timeLeft, arena._Blue_playerTotalKill, arena._Red_playerTotalKill));
				plr.sendPacket(new ExCubeGameExtendedChangePoints(timeLeft, arena._Blue_playerTotalKill, arena._Red_playerTotalKill, player.getTeam() == RED_TEAM, player, 0)); //points problem!
			}
		}
	}

	public static void onLogin(L2Player player)
	{
		for(HandyArena arena : _arenas.values())
		{
			int playerId = player.getObjectId();
			Integer playerKey = new Integer(playerId);
			if(arena._BlueTeamPlayers.contains(playerId))
			{
				player.teleToLocation(-57470, -60637, -2364);
				arena._tempPlayers.remove(playerKey);
				arena._BlueTeamPlayers.remove(playerKey);
				for(int objId : arena._tempPlayers)
				{
					L2Player plr = L2ObjectsStorage.getPlayer(objId);
					if(plr != null)
						plr.sendPacket(new ExCubeGameRemovePlayer(player, false));
				}
			}
			else if(arena._RedTeamPlayers.contains(playerId))
			{
				player.teleToLocation(-57470, -60637, -2364);
				arena._tempPlayers.remove(playerKey);
				arena._RedTeamPlayers.remove(playerKey);
				for(int objId : arena._tempPlayers)
				{
					L2Player plr = L2ObjectsStorage.getPlayer(objId);
					if(plr != null)
						plr.sendPacket(new ExCubeGameRemovePlayer(player, true));
				}
			}
		}
	}

	public static int getPlayerArena(L2Player player)
	{
		int arenaId = 0;
		for(HandyArena arena : _arenas.values())
		{
			if(arena._tempPlayers.contains(player.getObjectId()))
				arenaId = arena._id;
		}
		return arenaId;
	}

	private static class spawnMobs implements Runnable
	{
		private int _arenaId;

		public spawnMobs(int arenaId)
		{
			_arenaId = arenaId;
		}

		public void run()
		{
			HandyArena arena = _arenas.get(_arenaId);
			int _instanceId = 0;
			int baseX = _TpLocs[_arenaId][0];
			int baseY = _TpLocs[_arenaId][1];
			int z = _TpLocs[_arenaId][2];
			// Blue cubs
			for(int j = 0; j < 12; ++j)
			{
				int x = baseX + Rnd.get( -400, 400);
				int y = baseY + Rnd.get( -400, 400);
				L2Spawn spawnDat = spawnNpc(18671, x, y, z, 0, 60, _instanceId);
				L2MonsterInstance mob = (L2MonsterInstance) spawnDat.doSpawn(false);
				//L2ooInstance block = (L2ooInstance)spawnDat.getLastSpawn();
				//block.setColorHandyCubik(0, mob);
				//L2Character target;
				//L2MonsterInstance actor = (L2MonsterInstance) getActor();
				mob.setChampion(1);
				arena._mobs.add(mob);
				//arena._spawns1.add(mob);
				mob.broadcastPacket(new NpcInfo(mob, null));
			}
			// Red cubs
			for(int j = 0; j < 12; ++j)
			{
				int x = baseX + Rnd.get( -400, 400);
				int y = baseY + Rnd.get( -400, 400);
				L2Spawn spawnDat = spawnNpc(18671, x, y, z, 0, 60, _instanceId);
				L2MonsterInstance mob = (L2MonsterInstance) spawnDat.doSpawn(false);
				//arena._spawn1.add(mob2);	
				//L2ooInstance block = (L2ooInstance)spawnDat.getLastSpawn();
				//block.setColorHandyCubik(1, mob);
				mob.setChampion(2);
				arena._mobs.add(mob);
				arena._spawns1.add(mob);
				//L2Character target;
				mob.broadcastPacket(new NpcInfo(mob, null));				
			}
			/*
			  for (int j = 0; j < 5; ++j)
			  {
			  L2Spawn spawnDat = spawnNpc(18671, _spawnLocs[_arenaId][j][0], _spawnLocs[_arenaId][j][1], _spawnLocs[_arenaId][j][2], 0, 60, _instanceId);
			  arena._mobs.add(spawnDat.doSpawn());
			  }
			*/
		}
	}

	private static class finishGameHandy implements Runnable
	{
		private int _arenaId;

		public finishGameHandy(int arenaId)
		{
			_arenaId = arenaId;
		}

		public void run()
		{
			HandyArena arena = _arenas.get(_arenaId);
			boolean isRedTeamWin = arena._Red_playerTotalKill > arena._Blue_playerTotalKill;

			for(int objId : arena._RedTeamPlayers)
			{
				L2Player player = L2ObjectsStorage.getPlayer(objId);
				if(player != null)
				{
					player.setTransformation(0);
					player.setTeam(0, false);
					//player.removeSkill(SkillTable.getInstance().getInfo(5853, 1), false);
					player.teleToLocation(-57470, -60637, -2364);
					//player.sendPacket(new SkillList(null));
					player.sendPacket(new ExCubeGameEnd(isRedTeamWin));
					// player.sendPacket(new ExCubeGameCloseUI());
					// player.sendPacket(new ExCubeGameAddPlayer(player, false));
					
					// * if (arena._Red_playerTotalKill == arena._Blue_playerTotalKill)
					// * player.addItem("Handy", 13067, 2, player, true);
					 if (arena._Red_playerTotalKill > arena._Blue_playerTotalKill)
					 //* {
					 //* int reward = 0;
					 //* if (arena._Red_playerTotalKill / arena._RedTeamPlayers.size() > 25)
					 //* reward = 25;
					 //* else
					 //* reward = 10 + arena._Red_playerTotalKill / arena._RedTeamPlayers.size();
						//player.addItem("Handy", 13067, 10, player, true);
						player.getInventory().addItem(13067, 10);
					 //* }
					 //* else
					 //* player.addItem("Handy", 13067, 2, player, true);
					 //*/
				}
			}
			for(int objId : arena._BlueTeamPlayers)
			{
				L2Player player = L2ObjectsStorage.getPlayer(objId);
				if(player != null)
				{
					player.setTransformation(0);
					player.setTeam(0, false);
					//player.removeSkill(SkillTable.getInstance().getInfo(5852, 1), false);
					player.teleToLocation(-57470, -60637, -2364);
					//player.sendPacket(new SkillList(null));
					player.sendPacket(new ExCubeGameEnd(isRedTeamWin));
					// player.sendPacket(new ExCubeGameCloseUI());
					// player.sendPacket(new ExCubeGameAddPlayer(player, true));
					
					 //if (arena._Blue_playerTotalKill == arena._Red_playerTotalKill)
						//player.addItem("Handy", 13067, 2, player, true);
					if (arena._Blue_playerTotalKill > arena._Red_playerTotalKill)
					
						//int reward = 0;
						//if (arena._Blue_playerTotalKill / arena._BlueTeamPlayers.size() > 25)
					 //* reward = 25;
					 //* else
					 //* reward = 10 + arena._Blue_playerTotalKill / arena._BlueTeamPlayers.size();
						//player.addItem("Handy", 13067, 10, player, true);
						player.getInventory().addItem(13067, 10);
					// * else
					 //* player.addItem("Handy", 13067, 2, player, true);
					 //*/
				}
			}		
			arena._canRegister = true;
			arena._BlueTeamPlayers.clear();
			arena._RedTeamPlayers.clear();
			arena._tempPlayers.clear();
			arena._Red_playerTotalKill = 0;
			arena._Blue_playerTotalKill = 0;
			globalDespawn(arena);
			//arena._mobs.clear();			
		}
	}

	private static class startGamesHandy implements Runnable
	{
		private int _arenaId;

		public startGamesHandy(int arenaId)
		{
			_arenaId = arenaId;
		}

		public void run()
		{
			StartHandyGame(_arenaId);
		}
	}
}