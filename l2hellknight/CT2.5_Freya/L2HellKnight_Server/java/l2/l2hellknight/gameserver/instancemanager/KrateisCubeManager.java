package l2.hellknight.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.concurrent.ScheduledFuture;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;
import javolution.util.FastMap;
import javolution.util.FastList;
import l2.hellknight.L2DatabaseFactory;
import l2.hellknight.gameserver.Announcements;
import l2.hellknight.gameserver.ThreadPoolManager;
import l2.hellknight.gameserver.datatables.DoorTable;
import l2.hellknight.gameserver.datatables.NpcTable;
import l2.hellknight.gameserver.datatables.SpawnTable;
import l2.hellknight.gameserver.model.L2Spawn;
import l2.hellknight.gameserver.model.L2World;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.L2Summon;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.templates.L2NpcTemplate;
import l2.hellknight.gameserver.network.serverpackets.ExPVPMatchCCRecord;
import l2.hellknight.gameserver.network.serverpackets.ExPVPMatchCCMyRecord;
import l2.hellknight.gameserver.network.serverpackets.ExShowScreenMessage;
import l2.hellknight.util.Rnd;
import l2.hellknight.util.ValueSortMap;

public class KrateisCubeManager
{
	protected static final Logger 		_log 			= Logger.getLogger(KrateisCubeManager.class.getName());
	private static boolean 				_started	 	= false;
	private static boolean 				_canRegister 	= true;
	private static int 					_rotation 		= 0;
	private static int 					_level 			= 0;
	protected ScheduledFuture<?> 		_rotateRoomTask	= null;
	private static L2Npc				_manager;
	private static int					_playersToReward = 0;
	private static int					_playerTotalKill = 0;
	private static double				_playerTotalCoin = 0;
	private static int					_matchDuration	= 20; // 20
	private static int					_waitingTime	= 3;
	
	private final FastList<L2Npc> 		_watchers		= new FastList<L2Npc>();
	private final FastList<L2Npc> 		_mobs			= new FastList<L2Npc>();
	public static FastList<Integer>		_players		= new FastList<Integer>();
	private static FastList<Integer>	_tempPlayers	= new FastList<Integer>();
	private static Map<Integer, Integer>    _killList		 = new FastMap<Integer, Integer>();
	
	public static final CCPlayer[] krateisScore 		= new CCPlayer[24];
	
	private static int 					_redWatcher 	= 18601;
	private static int 					_blueWatcher 	= 18602;
	private static int					_fantasyCoin 	= 13067;

	private static int[] 				_doorlistA 		= {17150014,17150013,17150019,17150024,17150039,17150044,17150059,17150064,17150079,17150084,17150093,17150094,17150087,17150088,17150082,17150077,17150062,17150057,17150008,17150007,17150018,17150023,17150038,17150043,17150058,17150063,17150078,17150083,17150030,17150029,17150028,17150027,17150036,17150041,17150056,17150061};
	private static int[] 				_doorlistB 		= {17150020,17150025,17150034,17150033,17150032,17150031,17150012,17150011,17150010,17150009,17150017,17150022,17150016,17150021,17150037,17150042,17150054,17150053,17150052,17150051,17150050,17150049,17150048,17150047,17150085,17150080,17150074,17150073,17150072,17150071,17150070,17150069,17150068,17150067,17150076,17150081,17150092,17150091,17150090,17150089};

	private static int[] 				_level70Mobs 	= {18587,18580,18581,18584,18591,18589,18583};
	private static int[] 				_level76Mobs 	= {18590,18591,18589,18585,18586,18583,18592,18582};
	private static int[] 				_level80Mobs 	= {18595,18597,18596,18598,18593,18600,18594,18599};
	private static int[] 				_maxlevelMobs	= {};

	private static final String			GET_PLAYED_MATCH = "SELECT played_Matches, total_kills, total_coins FROM krateis_cube WHERE charId=?";
	private static final String			SAVE_PLAYED_MATCH = "INSERT INTO krateis_cube (charId,played_Matches,total_kills,total_coins) VALUES (?,?,?,?)";
	private static final String			UPDATE_PLAYED_MATCH = "UPDATE krateis_cube SET played_Matches = ?, total_kills = ?, total_coins = ? WHERE charId = ?";

	
	private static int[][][] 			_spawnLocs = {
		{{-77663, -85716, -8365},{-77701, -85948, -8365},{-77940, -86090, -8365},{-78142, -85934, -8365},{-78180, -85659, -8365}},
		{{-79653, -85689, -8365},{-79698, -86017, -8365},{-80003, -86025, -8365},{-80102, -85880, -8365},{-80061, -85603, -8365}},
		{{-81556, -85765, -8365},{-81794, -85528, -8365},{-82111, -85645, -8365},{-82044, -85928, -8364},{-81966, -86116, -8365}},
		{{-83750, -85882, -8365},{-84079, -86021, -8365},{-84123, -85663, -8365},{-83841, -85748, -8364},{-83951, -86120, -8365}},
		{{-85785, -85943, -8364},{-86088, -85626, -8365},{-85698, -85678, -8365},{-86154, -85879, -8365},{-85934, -85961, -8365}},
		{{-85935, -84131, -8365},{-86058, -83921, -8365},{-85841, -83684, -8364},{-86082, -83557, -8365},{-85680, -83816, -8365}},
		{{-84128, -83747, -8365},{-83877, -83597, -8365},{-83609, -83946, -8365},{-83911, -83955, -8364},{-83817, -83888, -8364}},
		{{-82039, -83971, -8365},{-81815, -83972, -8365},{-81774, -83742, -8364},{-81996, -83733, -8364},{-82124, -83589, -8365}},
		{{-80098, -83862, -8365},{-79973, -84058, -8365},{-79660, -83848, -8365},{-79915, -83570, -8365},{-79803, -83832, -8364}},
		{{-78023, -84066, -8365},{-77869, -83891, -8364},{-77674, -83757, -8365},{-77861, -83540, -8365},{-78107, -83660, -8365}},
		{{-77876, -82141, -8365},{-77674, -81822, -8365},{-77885, -81607, -8365},{-78078, -81779, -8365},{-78071, -81874, -8365}},
		{{-79740, -81636, -8365},{-80094, -81713, -8365},{-80068, -82004, -8365},{-79677, -81987, -8365},{-79891, -81734, -8364}},
		{{-81703, -81748, -8365},{-81857, -81661, -8364},{-82058, -81863, -8365},{-81816, -82011, -8365},{-81600, -81809, -8365}},
		{{-83669, -82007, -8365},{-83815, -81965, -8365},{-84121, -81805, -8365},{-83962, -81626, -8365},{-83735, -81625, -8365}},
		{{-85708, -81838, -8365},{-86062, -82009, -8365},{-86129, -81814, -8365},{-85957, -81634, -8365},{-85929, -81460, -8365}},
		{{-86160, -79933, -8365},{-85766, -80061, -8365},{-85723, -79691, -8365},{-85922, -79623, -8365},{-85941, -79879, -8364}},
		{{-84082, -79638, -8365},{-83923, -80082, -8365},{-83687, -79778, -8365},{-83863, -79619, -8365},{-83725, -79942, -8365}},
		{{-81963, -80020, -8365},{-81731, -79707, -8365},{-81957, -79589, -8365},{-82151, -79788, -8365},{-81837, -79868, -8364}},
		{{-80093, -80020, -8365},{-80160, -79716, -8365},{-79727, -79699, -8365},{-79790, -80049, -8365},{-79942, -79594, -8365}},
		{{-78113, -79658, -8365},{-77967, -80022, -8365},{-77692, -79779, -8365},{-77728, -79603, -8365},{-78078, -79857, -8365}},
		{{-77648, -77923, -8365},{-77714, -77742, -8365},{-78109, -77640, -8365},{-78114, -77904, -8365},{-77850, -77816, -8364}},
		{{-79651, -77492, -8365},{-79989, -77613, -8365},{-80134, -77981, -8365},{-79759, -78011, -8365},{-79644, -77779, -8365}},
		{{-81672, -77966, -8365},{-81867, -77536, -8365},{-82129, -77926, -8365},{-82057, -78064, -8365},{-82114, -77608, -8365}},
		{{-83938, -77574, -8365},{-84129, -77924, -8365},{-83909, -78111, -8365},{-83652, -78006, -8365},{-83855, -77756, -8364}},
		{{-85660, -78078, -8365},{-85842, -77649, -8365},{-85989, -77556, -8365},{-86075, -77783, -8365},{-86074, -78132, -8365}}};

	private static int[][] 				_teleports = {
		{-77906, -85809, -8362},
		{-79903, -85807, -8364},
		{-81904, -85807, -8364},
		{-83901, -85806, -8364},
		{-85903, -85807, -8364},
		{-77904, -83808, -8364},
		{-79904, -83807, -8364},
		{-81905, -83810, -8364},
		{-83903, -83807, -8364},
		{-85899, -83807, -8364},
		{-77903, -81808, -8364},
		{-79906, -81807, -8364},
		{-81901, -81808, -8364},
		{-83905, -81805, -8364},
		{-85907, -81809, -8364},
		{-77904, -79807, -8364},
		{-79905, -79807, -8364},
		{-81908, -79808, -8364},
		{-83907, -79806, -8364},
		{-85912, -79806, -8364},
		{-77905, -77808, -8364},
		{-79902, -77805, -8364},
		{-81904, -77808, -8364},
		{-83904, -77808, -8364},
		{-85904, -77807, -8364}};

	public static final KrateisCubeManager getInstance()
	{
		return SingletonHolder._instance;
	}

	public void init()
	{
		Calendar cal = Calendar.getInstance();
		
		if (cal.get(Calendar.MINUTE) >= 57)
		{
			cal.add(Calendar.HOUR, 1);
			cal.set(Calendar.MINUTE, 27);
		}
		else if (cal.get(Calendar.MINUTE) >= 0 && cal.get(Calendar.MINUTE) <= 26)
			cal.set(Calendar.MINUTE, 27);
		else
			cal.set(Calendar.MINUTE, 57);

		cal.set(Calendar.SECOND, 0);

		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new checkRegistered(), cal.getTimeInMillis() - System.currentTimeMillis(), 30*60000);
		
		Date date = new Date(cal.getTimeInMillis());
		_log.info("Krateis Cube initialized, next match: " + date);
	}

	protected class checkRegistered implements Runnable
	{
		public void run()
		{
			if (!_started)
			{
				if (_tempPlayers.isEmpty())
				{
					_log.info("Krateis Cube: Match canceled due to lack of participant, next round in 30 minutes.");
					return;
				}
				else
				{
					_log.info("Krateis Cube: Match started.");
					_canRegister = false;
					teleportToWaitRoom();
				}
			}
		}
	}

	protected class startKrateisCube implements Runnable
	{
		public void run()
		{
			_canRegister = true;

			closeAllDoors();
			L2PcInstance player;
			int i = 0;
			int temp = 0;
			for (int objectId : _players)
			{
				player = L2World.getInstance().findPlayer(objectId);
				if (player != null)
				{
					doTeleport(player, _teleports[i][0], _teleports[i][1], _teleports[i][2]);
					temp = player.getLevel();
					if (_level < temp)
						_level = temp;
					i++;
					// Send packets
					player.sendPacket(new ExPVPMatchCCMyRecord(0)); // Score on Top screen
					player.sendPacket(new ExPVPMatchCCRecord(1, KrateisCubeManager.krateisScore)); // Score on Click to button
				}
			}
			if (_level < 75)
				_maxlevelMobs = _level70Mobs;
			if (_level > 74)
				_maxlevelMobs = _level76Mobs;
			if (_level > 79)
				_maxlevelMobs = _level80Mobs;
				
			L2Spawn spawnDat;
			spawnDat = spawnNpc(32504, -86804, -81974, -8361, 34826, 60, 0);
			_manager = spawnDat.doSpawn();

			_rotateRoomTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new rotateRooms(), 10000, 50000);
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnMobs(), 10000);
			ThreadPoolManager.getInstance().scheduleGeneral(new finishCube(), _matchDuration*60000);
		}
	}

	protected class finishCube implements Runnable
	{
		public void run()
		{
			L2PcInstance player;

			_log.info("Krateis Cube match ended.");
			Announcements.getInstance().announceToAll("Krateis Cube: 3 minutes to the end of the logged.");
			
			if (_rotateRoomTask != null)
				_rotateRoomTask.cancel(true);

			closeAllDoors();
			globalDespawn();
			rewardPlayers();

			for (int objectId : _players)
			{
				player = L2World.getInstance().findPlayer(objectId);
				if (player != null)
				{
					doTeleport(player, -70381, -70937, -1428);
					player.setIsInKrateisCube(false);

					// Send Score Table on finish
					player.sendPacket(new ExPVPMatchCCRecord(2, KrateisCubeManager.krateisScore));
				}
			}

			_killList.clear();
			_players.clear();
			_started = false;
		}
	}

	protected class spawnMobs implements Runnable
	{
		public void run()
		{
			int npcId;
			int _instanceId = 0;			
			L2Spawn spawnDat;

			for (int i = 0; i <= 24; i++)
			{
				for (int j = 0; j <= 4; j++)
				{
					npcId = _maxlevelMobs[Rnd.get(_maxlevelMobs.length)];
					spawnDat = spawnNpc(npcId, _spawnLocs[i][j][0], _spawnLocs[i][j][1], _spawnLocs[i][j][2], 0, 60, _instanceId);
					_mobs.add(spawnDat.doSpawn());
				}				
			}
		}
	}

	protected class rotateRooms implements Runnable
	{
		public void run()
		{
			L2Spawn spawnDat;
			int instanceId = 0;
			int watcherA;
			int watcherB;

			watcherA = (_rotation == 0) ? _blueWatcher : _redWatcher;
			watcherB = (_rotation == 0) ? _redWatcher : _blueWatcher;

			spawnDat = spawnNpc(watcherA, -77906, -85809, -8362, 34826, 60, instanceId); //1
			_watchers.add(spawnDat.doSpawn());
			spawnDat = spawnNpc(watcherA, -79903, -85807, -8364, 32652, 60, instanceId); //2
			_watchers.add(spawnDat.doSpawn());
			spawnDat = spawnNpc(watcherB, -81904, -85807, -8364, 32839, 60, instanceId); //3
			_watchers.add(spawnDat.doSpawn());
			spawnDat = spawnNpc(watcherA, -83901, -85806, -8364, 33336, 60, instanceId); //4
			_watchers.add(spawnDat.doSpawn());
			spawnDat = spawnNpc(watcherA, -85903, -85807, -8364, 32571, 60, instanceId); //5
			_watchers.add(spawnDat.doSpawn());
			spawnDat = spawnNpc(watcherB, -77904, -83808, -8364, 32933, 60, instanceId); //6
			_watchers.add(spawnDat.doSpawn());
			spawnDat = spawnNpc(watcherA, -79904, -83807, -8364, 33055, 60, instanceId); //7
			_watchers.add(spawnDat.doSpawn());
			spawnDat = spawnNpc(watcherB, -81905, -83810, -8364, 32767, 60, instanceId); //8
			_watchers.add(spawnDat.doSpawn());
			spawnDat = spawnNpc(watcherB, -83903, -83807, -8364, 32676, 60, instanceId); //9
			_watchers.add(spawnDat.doSpawn());
			spawnDat = spawnNpc(watcherB, -85899, -83807, -8364, 33005, 60, instanceId); //10
			_watchers.add(spawnDat.doSpawn());
			spawnDat = spawnNpc(watcherB, -77903, -81808, -8364, 32664, 60, instanceId); //11
			_watchers.add(spawnDat.doSpawn());
			spawnDat = spawnNpc(watcherA, -79906, -81807, -8364, 32647, 60, instanceId); //12
			_watchers.add(spawnDat.doSpawn());
			spawnDat = spawnNpc(watcherB, -81901, -81808, -8364, 33724, 60, instanceId); //13
			_watchers.add(spawnDat.doSpawn());
			spawnDat = spawnNpc(watcherA, -83905, -81805, -8364, 32926, 60, instanceId); //14
			_watchers.add(spawnDat.doSpawn());
			spawnDat = spawnNpc(watcherB, -85907, -81809, -8364, 34248, 60, instanceId); //15
			_watchers.add(spawnDat.doSpawn());
			spawnDat = spawnNpc(watcherB, -77904, -79807, -8364, 32905, 60, instanceId); //16
			_watchers.add(spawnDat.doSpawn());
			spawnDat = spawnNpc(watcherA, -79905, -79807, -8364, 32767, 60, instanceId); //17
			_watchers.add(spawnDat.doSpawn());
			spawnDat = spawnNpc(watcherB, -81908, -79808, -8364, 32767, 60, instanceId); //18
			_watchers.add(spawnDat.doSpawn());
			spawnDat = spawnNpc(watcherA, -83907, -79806, -8364, 32767, 60, instanceId); //19
			_watchers.add(spawnDat.doSpawn());
			spawnDat = spawnNpc(watcherB, -85912, -79806, -8364, 29025, 60, instanceId); //20
			_watchers.add(spawnDat.doSpawn());
			spawnDat = spawnNpc(watcherA, -77905, -77808, -8364, 32767, 60, instanceId); //21
			_watchers.add(spawnDat.doSpawn());
			spawnDat = spawnNpc(watcherA, -79902, -77805, -8364, 32767, 60, instanceId); //22
			_watchers.add(spawnDat.doSpawn());
			spawnDat = spawnNpc(watcherB, -81904, -77808, -8364, 32478, 60, instanceId); //23
			_watchers.add(spawnDat.doSpawn());
			spawnDat = spawnNpc(watcherA, -83904, -77808, -8364, 32698, 60, instanceId); //24
			_watchers.add(spawnDat.doSpawn());
			spawnDat = spawnNpc(watcherA, -85904, -77807, -8364, 32612, 60, instanceId); //25
			_watchers.add(spawnDat.doSpawn());

			openDoors();
			 
			_rotation = (_rotation == 0) ? 1 : 0;
		}
	}

	protected class CloseDoors implements Runnable
	{
		public void run()
		{
			closeAllDoors();

			for (L2Npc npc : _watchers)
			{
				npc.getSpawn().stopRespawn();
				npc.deleteMe();
			}
			_watchers.clear();
		}
	}

	public boolean teleportToWaitRoom()
	{
		if (_tempPlayers.size() >= 1)
		{
			L2PcInstance player;
			for (int objectId : _tempPlayers)
			{
				_players.add(objectId);
	 
				player = L2World.getInstance().findPlayer(objectId);
				if (player != null)
				{
					doTeleport(player, -87028, -81780, -8365);
					player.setIsInKrateisCube(true);
					player.sendPacket(new ExShowScreenMessage("3 Minutes to enter Kratei's Cube.",15000));
				}
			}
			_tempPlayers.clear();
			_started = true;
			ThreadPoolManager.getInstance().scheduleGeneral(new startKrateisCube(), _waitingTime*60000);
			return true;
		}
		else
			return false;
	}

	@SuppressWarnings("unchecked")
	private void rewardPlayers()
	{
		int kills = 0;
		int i = 0;
		double amount = 0;
		L2PcInstance player;
		
		_playersToReward = getNumberPlayerToReward();
		_killList = ValueSortMap.sortMapByValue(_killList, false);
		
		for (int objectId : _killList.keySet())
		{
			player = L2World.getInstance().getPlayer(objectId);
			if (player != null)
			{
				kills = _killList.get(objectId);
				if (kills >= 10)
				{
					amount = getRewardAmount(player, i);
					int coinAmount = (int)amount;
					player.addItem("Krateis Cube Reward", _fantasyCoin, coinAmount, player, true);
					player.getInventory().updateDatabase();
					i++;
				}
			}
		}
		_playersToReward = 0;
		_playerTotalKill = 0;
		_playerTotalCoin = 0;
	}

	private double getRewardAmount(L2PcInstance player, int place)
	{
		int playedMatches = getPlayedMatches(player);
		int n = Math.round(_playersToReward / 10);
		double reward;

		if (playedMatches == 0)
		{
			savePlayedMatches(player);
		}

		playedMatches++;

		switch (place)
		{
			case 0:
			reward = Math.floor(40 + (n * 2) + (playedMatches / 4));
			if (reward > 50)
				reward = (50 * 1); //default 50
			break;
		case 1:
			reward = Math.floor(18 + (n * 2) + (playedMatches / 4));
			if (reward > 20)
				reward = (20 * 1); //default 20
			break;
		default:
			reward = Math.floor(11 + n + (playedMatches / 6));
			if (reward > 5)
				reward = (5 * 1); //default 5
			break;
		}

		updatePlayedMatches(player, playedMatches, reward);
		
		return reward;
	}

	public void updatePlayedMatches(L2PcInstance player, int playedMatches, double amount)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(UPDATE_PLAYED_MATCH);

			statement.setInt(1, playedMatches);
			statement.setInt(2, _playerTotalKill + _killList.get(player.getObjectId()));
			statement.setDouble(3, _playerTotalCoin + amount);
			statement.setInt(4, player.getObjectId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Could not update character played Krateis Cube Matches: ", e);
		}
		finally
		{
			try { con.close(); } catch (Exception e) {}
		}
	}

	private void savePlayedMatches(L2PcInstance player)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SAVE_PLAYED_MATCH);

			statement.setInt(1, player.getObjectId());
			statement.setInt(2, 0);
			statement.setInt(3, _killList.get(player.getObjectId()));
			statement.setDouble(4, 0);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Could not store character krateis cube played Matches: ", e);
		}
		finally
		{
			try { con.close(); } catch (Exception e) {}
		}
	}

	private int getPlayedMatches(L2PcInstance player)
	{
		Connection con = null;
		int playedMatches = 0;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(GET_PLAYED_MATCH);

			statement.setInt(1, player.getObjectId());

			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				playedMatches = rset.getInt("played_Matches");
				_playerTotalKill = rset.getInt("total_kills");
				_playerTotalCoin = rset.getDouble("total_coins");
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Could not get player total Krateis Cube played Matches: ", e);
		}
		finally
		{
			try { con.close(); } catch (Exception e) {}
		}
		
		return playedMatches;
	}

	private int getNumberPlayerToReward()
	{
		int number = 0;
		int kills = 0;

		for (int objectId : _killList.keySet())
		{
			kills = _killList.get(objectId);
			
			if (kills >= 10)
				number++;
		}
		return number;
	}

	private void globalDespawn()
	{
		for (L2Npc npc : _mobs)
		{
			npc.getSpawn().stopRespawn();
			npc.deleteMe();
		}
		_mobs.clear();

		for (L2Npc npc : _watchers)
		{
			npc.getSpawn().stopRespawn();
			npc.deleteMe();
		}

		_manager.getSpawn().stopRespawn();
		_manager.deleteMe();

		_manager = null;
		_watchers.clear();
	}

	private void openDoors()
	{
		int[] doorToOpen = (_rotation == 1) ? _doorlistB : _doorlistA;

		closeAllDoors();

		for (int doorId : doorToOpen)
			DoorTable.getInstance().getDoor(doorId).openMe();

		ThreadPoolManager.getInstance().scheduleGeneral(new CloseDoors(), 25000);
	}

	private void closeAllDoors()
	{
		int doorId = 17150001;
		
		while (doorId <= 17150103)
		{
			DoorTable.getInstance().getDoor(doorId).closeMe();
			doorId += 1;
		}
	}
	
	// Teleports player and his summon to given coords
	private void doTeleport(L2PcInstance player, int x, int y, int z)
	{

		if (!player.isOnline())
			return;

		player.teleToLocation(x, y, z, false);

		L2Summon pet = player.getPet();
		if (pet != null)
			pet.teleToLocation(x, y, z, false);
	}
	
	private L2Spawn spawnNpc(int npcId, int x, int y, int z, int heading, int respawnTime, int instanceId)
	{
		L2NpcTemplate npcTemplate;
		npcTemplate = NpcTable.getInstance().getTemplate(npcId);
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
			spawnDat.setInstanceId(instanceId);
			SpawnTable.getInstance().addNewSpawn(spawnDat, false);
			spawnDat.init();
			spawnDat.startRespawn();
			if (respawnTime == 0)
				spawnDat.stopRespawn();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return spawnDat;
	}

	public boolean registerPlayer(L2PcInstance player)
	{
		int objectId = player.getObjectId();

		if (_tempPlayers.contains(objectId) || _tempPlayers.size() >= 25)
			return false;

		_tempPlayers.add(objectId);
		return true;
	}

	public boolean removePlayer(L2PcInstance player)
	{
		int objectId = player.getObjectId();
		
		if (_tempPlayers.contains(objectId))
		{
      _tempPlayers.remove(_tempPlayers.indexOf(objectId));
			return true;
		}
		else
		{
		return false;
		}
	}
	
	// Add one kill to this player
	public void addKill(L2PcInstance player)
	{
		addKills(player, 1);
	}

	public boolean addKills(L2PcInstance player, int value)
	{
		int objectId = player.getObjectId();
		int kills = 0;

		if (_players.contains(objectId))
		{

			if (_killList.containsKey(objectId))
				kills = _killList.get(objectId);

			kills += value;
			_killList.put(objectId, kills);
			
			// Send player score on screen
			player.sendPacket(new ExPVPMatchCCMyRecord(kills));
			return true;
		}
		return false;
	}

	public int getKills(L2PcInstance player)
	{
		int objectId = player.getObjectId();
		int kills = 0;
		
		if (_players.contains(objectId))
		{
			if (_killList.containsKey(objectId))
				kills = _killList.get(objectId);
		}

		return kills;
	}
	
	public boolean isTimeToRegister()
	{
		return _canRegister;
	}
	
		// This one is used to control if the player is registered in Krateis Cube on enterWorld
	public boolean isRegistered(L2PcInstance player)
	{
		int objectId = player.getObjectId();
		if (_players.contains(objectId))
			return true;
		return false;
	}

	public class CCPlayer
	{
		private final String _name;
		private int _killPoints;

		public CCPlayer(L2PcInstance player)
		{
			this(player.getName());
		}
		
		private CCPlayer(String name)
		{
			_name = name;
			_killPoints = 0;
		}

		public final String getName()
		{
			return _name;
		}

		public final int getPoints()
		{
			return _killPoints;
		}

	public final void setPoints(int killPoints)
		{
			_killPoints = killPoints;
		}
	}
	
	// Used to teleport players in the cube from the waiting room after a death
	public void teleportPlayerIn(L2PcInstance player)
	{
		int i = Rnd.get(_teleports.length);
		doTeleport(player, _teleports[i][0], _teleports[i][1], _teleports[i][2]);
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final KrateisCubeManager _instance = new KrateisCubeManager();
	}
}
