package l2rt.gameserver.instancemanager;

import gnu.trove.map.hash.TIntIntHashMap;

import javolution.util.FastMap;
import l2rt.Config;
import l2rt.database.*;
import l2rt.gameserver.model.L2Clan;
import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Spawn;
import l2rt.gameserver.model.instances.L2RaidBossInstance;
import l2rt.gameserver.model.instances.L2ReflectionBossInstance;
import l2rt.gameserver.tables.ClanTable;
import l2rt.gameserver.tables.GmListTable;
import l2rt.gameserver.templates.StatsSet;
import l2rt.util.SqlBatch;
import l2rt.util.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

public class RaidBossSpawnManager
{
	private static Logger _log = Logger.getLogger(RaidBossSpawnManager.class.getName());

	private static RaidBossSpawnManager _instance;

	protected static FastMap<Integer, L2Spawn> _spawntable = new FastMap<Integer, L2Spawn>().setShared(true);
	protected static FastMap<Integer, L2RaidBossInstance> _bosses = new FastMap<Integer, L2RaidBossInstance>().setShared(true);
	protected static FastMap<Integer, StatsSet> _storedInfo;
	protected static FastMap<Integer, FastMap<Integer, Integer>> _points;
	protected static TIntIntHashMap _pointsReward = new TIntIntHashMap();

	public static enum Status
	{
		ALIVE,
		DEAD,
		UNDEFINED
	}

	private RaidBossSpawnManager()
	{
		_instance = this;
		if(!Config.DONTLOADSPAWN)
			reloadBosses();
	}

	public void reloadBosses()
	{
		loadRaidPoinsValuesPath();
		loadStatus();
		restorePointsTable();
		calculateRanking();
	}

	public void cleanUp()
	{
		updateAllStatusDb();
		updatePointsDb();

		_bosses.clear();
		_storedInfo.clear();
		_spawntable.clear();
		_points.clear();

		_log.fine("RaidBossSpawnManager: All raidboss info saved!");
	}

	public static RaidBossSpawnManager getInstance()
	{
		if(_instance == null)
			new RaidBossSpawnManager();
		return _instance;
	}

	private void loadStatus()
	{
		_storedInfo = new FastMap<Integer, StatsSet>().setShared(true);

		ThreadConnection con = null;
		FiltredStatement statement = null;
		ResultSet rset = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			rset = con.createStatement().executeQuery("SELECT * FROM `raidboss_status`");
			while(rset.next())
			{
				int id = rset.getInt("id");
				StatsSet info = new StatsSet();
				info.set("current_hp", rset.getDouble("current_hp"));
				info.set("current_mp", rset.getDouble("current_mp"));
				info.set("respawn_delay", rset.getInt("respawn_delay"));
				_storedInfo.put(id, info);
			}
		}
		catch(Exception e)
		{
			_log.warning("RaidBossSpawnManager: Couldnt load raidboss statuses");
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}

		_log.info("RaidBossSpawnManager: Loaded " + _storedInfo.size() + " Statuses");
	}

	private void loadRaidPoinsValuesPath()
	{
		LineNumberReader lnr = null;
		try
		{
			File data = new File(Config.DATAPACK_ROOT, "data/raidpoints.csv");
			lnr = new LineNumberReader(new BufferedReader(new FileReader(data)));
			String line = null;
			while((line = lnr.readLine()) != null)
			{
				if(line.trim().length() == 0 || line.startsWith("#"))
					continue;
				StringTokenizer st = new StringTokenizer(line, ";");
				_pointsReward.put(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()));
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if(lnr != null)
					lnr.close();
			}
			catch(Exception e1)
			{ /* ignore problems */}
		}
	}

	private void updateAllStatusDb()
	{
		for(int id : _storedInfo.keySet())
			updateStatusDb(id);
	}

	private void updateStatusDb(int id)
	{
		if(!_spawntable.containsKey(id))
			return;

		StatsSet info = _storedInfo.get(id);
		if(info == null)
			_storedInfo.put(id, info = new StatsSet());

		L2RaidBossInstance raidboss = _bosses.get(id);
		if(raidboss instanceof L2ReflectionBossInstance)
			return;

		if(raidboss != null && raidboss.getRaidStatus() == Status.ALIVE)
		{
			info.set("current_hp", raidboss.getCurrentHp());
			info.set("current_mp", raidboss.getCurrentMp());
			info.set("respawn_delay", 0);
		}
		else
		{
			info.set("current_hp", 0);
			info.set("current_mp", 0);
			if(raidboss != null && raidboss.getSpawn() != null)
				info.set("respawn_delay", raidboss.getSpawn().getRespawnTime());
		}

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("REPLACE INTO `raidboss_status` (id, current_hp, current_mp, respawn_delay) VALUES (?,?,?,?)");
			statement.setInt(1, id);
			statement.setDouble(2, info.getDouble("current_hp"));
			statement.setDouble(3, info.getDouble("current_mp"));
			statement.setInt(4, info.getInteger("respawn_delay", 0));
			statement.execute();

			if(raidboss != null)
				_log.fine("RaidBossSpawnManager: Scheduled " + raidboss.getName() + " for respawn in " + Util.formatTime(raidboss.getSpawn().getRespawnTime()));
			else
				_log.fine("RaidBossSpawnManager: Saved respawn time for raidboss " + id);
		}
		catch(SQLException e)
		{
			_log.warning("RaidBossSpawnManager: Couldnt update raidboss_status table");
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public void addNewSpawn(L2Spawn spawnDat)
	{
		if(spawnDat == null)
			return;

		int bossId = spawnDat.getNpcId();

		if(_spawntable.containsKey(bossId))
			return;

		_spawntable.put(bossId, spawnDat);

		StatsSet info = _storedInfo.get(bossId);
		if(info != null)
			spawnDat.setRespawnTime(info.getInteger("respawn_delay", 0));
	}

	public void onBossSpawned(L2RaidBossInstance raidboss)
	{
		raidboss.setRaidStatus(Status.ALIVE);

		int bossId = raidboss.getNpcId();
		if(!_spawntable.containsKey(bossId))
			return;

		StatsSet info = _storedInfo.get(bossId);
		if(info != null && info.getDouble("current_hp") > 1)
		{
			raidboss.setCurrentHp(info.getDouble("current_hp"), false);
			raidboss.setCurrentMp(info.getDouble("current_mp"));
		}

		if(raidboss.getNpcId() == 25328) // TODO Сделать поле isNight
			GmListTable.broadcastMessageToGMs("Spawning night RaidBoss " + raidboss.getName());
		else
			GmListTable.broadcastMessageToGMs("Spawning RaidBoss " + raidboss.getName());

		_bosses.put(raidboss.getNpcId(), raidboss);
	}

	public void onBossDespawned(L2RaidBossInstance raidboss)
	{
		updateStatusDb(raidboss.getNpcId());
	}

	public Status getRaidBossStatusId(int bossId)
	{
		if(_bosses.containsKey(bossId))
			return _bosses.get(bossId).getRaidStatus();
		else if(_spawntable.containsKey(bossId))
			return Status.DEAD;
		else
			return Status.UNDEFINED;
	}

	public boolean isDefined(int bossId)
	{
		return _spawntable.containsKey(bossId);
	}

	public Map<Integer, L2Spawn> getSpawnTable()
	{
		return _spawntable;
	}

	// ----------- Points & Ranking -----------

	public static final Integer KEY_RANK = new Integer(-1);
	public static final Integer KEY_TOTAL_POINTS = new Integer(0);

	private ReentrantLock pointsLock = new ReentrantLock();

	private void restorePointsTable()
	{
		pointsLock.lock();
		_points = new FastMap<Integer, FastMap<Integer, Integer>>().setShared(true);

		ThreadConnection con = null;
		FiltredStatement statement = null;
		ResultSet rset = null;
		try
		{
			//read raidboss points
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();
			rset = statement.executeQuery("SELECT owner_id, boss_id, points FROM `raidboss_points` ORDER BY owner_id ASC");
			int currentOwner = 0;
			FastMap<Integer, Integer> score = null;
			while(rset.next())
			{
				if(currentOwner != rset.getInt("owner_id"))
				{
					currentOwner = rset.getInt("owner_id");
					score = new FastMap<Integer, Integer>();
					_points.put(currentOwner, score);
				}

				assert score != null;
				int bossId = rset.getInt("boss_id");
				if(bossId != KEY_RANK && bossId != KEY_TOTAL_POINTS && _pointsReward.containsKey(bossId))
					score.put(bossId, rset.getInt("points"));
			}
		}
		catch(Exception e)
		{
			_log.warning("RaidBossSpawnManager: Couldnt load raidboss points");
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
		pointsLock.unlock();
	}

	public void updatePointsDb()
	{
		pointsLock.lock();
		if(!mysql.set("TRUNCATE `raidboss_points`"))
			_log.warning("RaidBossSpawnManager: Couldnt empty raidboss_points table");

		if(_points.isEmpty())
		{
			pointsLock.unlock();
			return;
		}

		ThreadConnection con = null;
		FiltredStatement statement = null;
		StringBuilder sb;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();
			SqlBatch b = new SqlBatch("INSERT INTO `raidboss_points` (owner_id, boss_id, points) VALUES");

			for(Entry<Integer, FastMap<Integer, Integer>> pointEntry : _points.entrySet())
			{
				FastMap<Integer, Integer> tmpPoint = pointEntry.getValue();
				if(tmpPoint == null || tmpPoint.isEmpty())
					continue;

				for(Entry<Integer, Integer> pointListEntry : tmpPoint.entrySet())
				{
					if(KEY_RANK.equals(pointListEntry.getKey()) || KEY_TOTAL_POINTS.equals(pointListEntry.getKey()) || pointListEntry.getValue() == null || pointListEntry.getValue() == 0)
						continue;

					sb = new StringBuilder("(");
					sb.append(pointEntry.getKey()).append(","); // игрок
					sb.append(pointListEntry.getKey()).append(","); // босс
					sb.append(pointListEntry.getValue()).append(")"); // количество очков
					b.write(sb.toString());
				}
			}

			if(!b.isEmpty())
				statement.executeUpdate(b.close());
		}
		catch(SQLException e)
		{
			_log.warning("RaidBossSpawnManager: Couldnt update raidboss_points table");
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
		pointsLock.unlock();
	}

	public void addPoints(int ownerId, int bossId, int points)
	{
		if(points <= 0 || ownerId <= 0 || bossId <= 0)
			return;

		pointsLock.lock();
		// ищем этого игрока в таблице рейтинга
		FastMap<Integer, Integer> pointsTable = _points.get(ownerId);

		// не нашли? добавляем
		if(pointsTable == null)
		{
			pointsTable = new FastMap<Integer, Integer>();
			_points.put(ownerId, pointsTable);
		}

		// его таблица пуста? добавляем новую запись
		if(pointsTable.isEmpty())
			pointsTable.put(bossId, points);
		else
		// нет? сперва ищем старую
		{
			Integer currentPoins = pointsTable.get(bossId);
			pointsTable.put(bossId, currentPoins == null ? points : currentPoins.intValue() + points);
		}
		pointsLock.unlock();
	}

	public TreeMap<Integer, Integer> calculateRanking()
	{
		// таблица PlayerId - Rank для внутреннего пользования 
		TreeMap<Integer, Integer> tmpRanking = new TreeMap<Integer, Integer>();

		pointsLock.lock();

		// берем существующую таблицу с информацией о поинтах и перебираем по строкам
		for(Entry<Integer, FastMap<Integer, Integer>> point : _points.entrySet())
		{
			// получаем таблицу пар <BossId - Points>
			FastMap<Integer, Integer> tmpPoint = point.getValue();

			// ранг и сумма нам тут не нужны, мы их пересчитываем
			tmpPoint.remove(KEY_RANK);
			tmpPoint.remove(KEY_TOTAL_POINTS);
			int totalPoints = 0;

			// собираем всю сумму для игрока
			for(Entry<Integer, Integer> e : tmpPoint.entrySet())
				totalPoints += e.getValue();

			// вдруг кто левый затесался
			if(totalPoints != 0)
			{
				// кладем в кучу сумму
				tmpPoint.put(KEY_TOTAL_POINTS, totalPoints);
				// а это пригодится чуть позже
				tmpRanking.put(totalPoints, point.getKey());
			}
		}

		// перебираем таблицу рангов и сливаем ее с общей таблицей
		int ranking = 1;
		for(Entry<Integer, Integer> entry : tmpRanking.descendingMap().entrySet())
		{
			// ищем соответствующую строку из основной таблицы
			FastMap<Integer, Integer> tmpPoint = _points.get(entry.getValue());

			// и добавляем туда ранг
			tmpPoint.put(KEY_RANK, ranking);
			ranking++;
		}

		pointsLock.unlock();

		return tmpRanking;
	}

	/*
	Rank 1 = 2,500 Clan Reputation Points
	Rank 2 = 1,800 Clan Reputation Points
	Rank 3 = 1,400 Clan Reputation Points
	Rank 4 = 1,200 Clan Reputation Points
	Rank 5 = 900 Clan Reputation Points
	Rank 6 = 700 Clan Reputation Points
	Rank 7 = 600 Clan Reputation Points
	Rank 8 = 400 Clan Reputation Points
	Rank 9 = 300 Clan Reputation Points
	Rank 10 = 200 Clan Reputation Points
	Rank 11~50 = 50 Clan Reputation Points
	Rank 51~100 = 25 Clan Reputation Points
	*/
	public void distributeRewards()
	{
		pointsLock.lock();
		TreeMap<Integer, Integer> ranking = calculateRanking();
		Iterator<Integer> e = ranking.descendingMap().values().iterator();
		int counter = 1;
		while(e.hasNext() && counter <= 100)
		{
			int reward = 0;
			int playerId = e.next();
			if(counter == 1)
				reward = 2500;
			else if(counter == 2)
				reward = 1800;
			else if(counter == 3)
				reward = 1400;
			else if(counter == 4)
				reward = 1200;
			else if(counter == 5)
				reward = 900;
			else if(counter == 6)
				reward = 700;
			else if(counter == 7)
				reward = 600;
			else if(counter == 8)
				reward = 400;
			else if(counter == 9)
				reward = 300;
			else if(counter == 10)
				reward = 200;
			else if(counter <= 50)
				reward = 50;
			else if(counter <= 100)
				reward = 25;
			L2Player player = L2ObjectsStorage.getPlayer(playerId);
			L2Clan clan = null;
			if(player != null)
				clan = player.getClan();
			else
				clan = ClanTable.getInstance().getClan(mysql.simple_get_int("clanid", "characters", "obj_Id=" + playerId));
			if(clan != null)
				clan.incReputation(reward, true, "RaidPoints");
			counter++;
		}
		_points.clear();
		updatePointsDb();
		pointsLock.unlock();
	}

	public Map<Integer, FastMap<Integer, Integer>> getPoints()
	{
		return _points;
	}

	public FastMap<Integer, Integer> getPointsForOwnerId(int ownerId)
	{
		return _points.get(ownerId);
	}

	public int getPoinstForRaid(int raid)
	{
		return _pointsReward.get(raid);
	}
}