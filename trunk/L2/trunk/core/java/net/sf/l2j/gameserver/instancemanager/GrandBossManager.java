package net.sf.l2j.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Map;
import java.util.logging.Logger;

import javolution.util.FastMap;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2GrandBossInstance;
import net.sf.l2j.gameserver.model.zone.type.L2BossZone;
import net.sf.l2j.gameserver.templates.StatsSet;
import net.sf.l2j.util.L2FastList;

public class GrandBossManager
{
	private static final String DELETE_GRAND_BOSS_LIST = "DELETE FROM grandboss_list";

	private static final String INSERT_GRAND_BOSS_LIST = "INSERT INTO grandboss_list (player_id,zone) VALUES (?,?)";

	private static final String UPDATE_GRAND_BOSS_DATA = "UPDATE grandboss_data set loc_x = ?, loc_y = ?, loc_z = ?, heading = ?, respawn_time = ?, currentHP = ?, currentMP = ?, status = ? where boss_id = ?";

	private static final String UPDATE_GRAND_BOSS_DATA2 = "UPDATE grandboss_data set status = ? where boss_id = ?";

	private static Logger _log = Logger.getLogger(GrandBossManager.class.getName());

	private static GrandBossManager _instance;

	protected static Map<Integer, L2GrandBossInstance> _bosses;

	protected static Map<Integer, StatsSet> _storedInfo;

	private Map<Integer, Integer> _bossStatus;

	private L2FastList<L2BossZone> _zones;

	public static final GrandBossManager getInstance()
	{
		if (_instance == null)
		{
			_log.info("Initializing GrandBossManager");
			_instance = new GrandBossManager();
		}
		return _instance;
	}

	public GrandBossManager()
	{
		init();
	}

	private void init()
	{
		_zones = new L2FastList<L2BossZone>();

		_bosses = new FastMap<Integer, L2GrandBossInstance>();
		_storedInfo = new FastMap<Integer, StatsSet>();
		_bossStatus = new FastMap<Integer, Integer>();
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement = con.prepareStatement("SELECT * from grandboss_data ORDER BY boss_id");
			ResultSet rset = statement.executeQuery();

			while (rset.next())
			{
				StatsSet info = new StatsSet();
				int bossId = rset.getInt("boss_id");
				info.set("loc_x", rset.getInt("loc_x"));
				info.set("loc_y", rset.getInt("loc_y"));
				info.set("loc_z", rset.getInt("loc_z"));
				info.set("heading", rset.getInt("heading"));
				info.set("respawn_time", rset.getLong("respawn_time"));
				double HP = rset.getDouble("currentHP"); //jython doesn't recognize doubles
				int true_HP = (int) HP; //so use java's ability to type cast
				info.set("currentHP", true_HP); //to convert double to int
				double MP = rset.getDouble("currentMP");
				int true_MP = (int) MP;
				info.set("currentMP", true_MP);
				_bossStatus.put(bossId, rset.getInt("status"));

				_storedInfo.put(bossId, info);
				info = null;
			}

			_log.info("GrandBossManager: Loaded " + _storedInfo.size() + " Instances");

			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.warning("GrandBossManager: Could not load grandboss_data table");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public void initZones()
	{
		Connection con = null;

		FastMap<Integer, L2FastList<Integer>> zones = new FastMap<Integer, L2FastList<Integer>>();

		if (_zones == null)
		{
			_log.warning("GrandBossManager: Could not read Grand Boss zone data");
			return;
		}

		for (L2BossZone zone : _zones)
		{
			if (zone == null)
				continue;
			zones.put(zone.getId(), new L2FastList<Integer>());
		}

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement = con.prepareStatement("SELECT * from grandboss_list ORDER BY player_id");
			ResultSet rset = statement.executeQuery();

			while (rset.next())
			{
				int id = rset.getInt("player_id");
				int zone_id = rset.getInt("zone");
				zones.get(zone_id).add(id);
			}

			rset.close();
			statement.close();

			_log.info("GrandBossManager: Initialized " + _zones.size() + " Grand Boss Zones");
		}
		catch (SQLException e)
		{
			_log.warning("GrandBossManager: Could not load grandboss_list table");
			e.getMessage();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e)
			{
			}
		}

		for (L2BossZone zone : _zones)
		{
			if (zone == null)
				continue;
			zone.setAllowedPlayers(zones.get(zone.getId()));
		}

		zones.clear();
	}

	public void addZone(L2BossZone zone)
	{
		if (_zones != null)
		{
			_zones.add(zone);
		}
	}

	public final L2BossZone getZone(L2Character character)
	{
		if (_zones != null)
			for (L2BossZone temp : _zones)
			{
				if (temp.isCharacterInZone(character))
				{
					return temp;
				}
			}
		return null;
	}

	public final L2BossZone getZone(int x, int y, int z)
	{
		if (_zones != null)
			for (L2BossZone temp : _zones)
			{
				if (temp.isInsideZone(x, y, z))
				{
					return temp;
				}
			}
		return null;
	}

	public boolean checkIfInZone(String zoneType, L2Object obj)
	{
		L2BossZone temp = getZone(obj.getX(), obj.getY(), obj.getZ());
		if (temp == null)
		{
			return false;
		}
		return temp.getZoneName().equalsIgnoreCase(zoneType);
	}

	/*
	 * The rest
	 */

	public int getBossStatus(int bossId)
	{
		return _bossStatus.get(bossId);
	}

	public void setBossStatus(int bossId, int status)
	{
		_bossStatus.put(bossId, status);
		_log.info(getClass().getSimpleName()+": Updated "+NpcTable.getInstance().getTemplate(bossId).getName()+" (npcID: " +bossId+ ") status to " +status);
		fastStoreToDb(bossId, true);
	}

	/*
	 * Adds a L2GrandBossInstance to the list of bosses.
	 */

	public void addBoss(L2GrandBossInstance boss)
	{
		if (boss != null)
		{
			if (_bosses.containsKey(boss.getNpcId()))
				_bosses.remove(boss.getNpcId());
			_bosses.put(boss.getNpcId(), boss);
		}
	}

	public L2GrandBossInstance getBoss(int bossId)
	{
		return _bosses.get(bossId);
	}

	public StatsSet getStatsSet(int bossId)
	{
		return _storedInfo.get(bossId);
	}

	public void setStatsSet(int bossId, StatsSet info)
	{
		_storedInfo.put(bossId, info);
		fastStoreToDb(bossId, false);
	}
	
	private void fastStoreToDb(int bossId, boolean statusOnly)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			L2GrandBossInstance boss = _bosses.get(bossId);
			StatsSet info = _storedInfo.get(bossId);
			
			if(statusOnly || boss == null || info == null)
			{
				statement = con.prepareStatement(UPDATE_GRAND_BOSS_DATA2);
				statement.setInt(1, _bossStatus.get(bossId));
				statement.setInt(2, bossId);
			}
			else
			{
				statement = con.prepareStatement(UPDATE_GRAND_BOSS_DATA);
				statement.setInt(1, boss.getX());
				statement.setInt(2, boss.getY());
				statement.setInt(3, boss.getZ());
				statement.setInt(4, boss.getHeading());
				statement.setLong(5, info.getLong("respawn_time"));
				double hp = boss.getCurrentHp();
				double mp = boss.getCurrentMp();
				if(boss.isDead())
				{
					hp = boss.getMaxHp();
					mp = boss.getMaxMp();
				}
				statement.setDouble(6, hp);
				statement.setDouble(7, mp);
				statement.setInt(8, _bossStatus.get(bossId));
				statement.setInt(9, bossId);
			}
			statement.executeUpdate();
			statement.close();
		}
		catch(SQLException e)
		{
			_log.warning("GrandBossManager[fastStoreToDb]: Couldn't store grandbosses to database:" + e);
		}
		finally
		{
			try { con.close(); } catch(Exception e) {}
		}
	}

	private void storeToDb()
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement(DELETE_GRAND_BOSS_LIST);
			statement.executeUpdate();
			statement.close();

			for (L2BossZone zone : _zones)
			{
				if (zone == null)
					continue;
				Integer id = zone.getId();
				L2FastList<Integer> list = zone.getAllowedPlayers();
				if (list == null || list.isEmpty())
					continue;
				for (Integer player : list)
				{
					statement = con.prepareStatement(INSERT_GRAND_BOSS_LIST);
					statement.setInt(1, player);
					statement.setInt(2, id);
					statement.executeUpdate();
					statement.close();
				}
			}

			for (Integer bossId : _storedInfo.keySet())
			{
				L2GrandBossInstance boss = _bosses.get(bossId);
				StatsSet info = _storedInfo.get(bossId);
				if (boss == null || info == null)
				{
					statement = con.prepareStatement("UPDATE grandboss_data set status = ?, respawn_time = ? where boss_id = ?");
					statement.setInt(1, _bossStatus.get(bossId));
					statement.setLong(2, info.getLong("respawn_time"));
					statement.setInt(3, bossId);
				}
				else
				{
					statement = con.prepareStatement(UPDATE_GRAND_BOSS_DATA);
					statement.setInt(1, boss.getX());
					statement.setInt(2, boss.getY());
					statement.setInt(3, boss.getZ());
					statement.setInt(4, boss.getHeading());
					statement.setLong(5, info.getLong("respawn_time"));
					double hp = boss.getCurrentHp();
					double mp = boss.getCurrentMp();
					if (boss.isDead())
					{
						hp = boss.getMaxHp();
						mp = boss.getMaxMp();
					}
					statement.setDouble(6, hp);
					statement.setDouble(7, mp);
					statement.setInt(8, _bossStatus.get(bossId));
					statement.setInt(9, bossId);
				}
				statement.executeUpdate();
				statement.close();
			}
		}
		catch (SQLException e)
		{
			_log.warning("GrandBossManager: Couldn't store grandbosses to database:" + e);
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public void cleanUp()
	{
		storeToDb();

		 _bosses.clear();
		 _storedInfo.clear();
		 _bossStatus.clear();
		 _zones.clear();
	}

	synchronized public void save()
	{
		storeToDb();
	}

	public long getInterval(int bossId)
	{
		long interval = this.getStatsSet(bossId).getLong("respawn_time") - Calendar.getInstance().getTimeInMillis();

		if(interval < 0)
			return 0;
		else
			return interval;
	}
}
