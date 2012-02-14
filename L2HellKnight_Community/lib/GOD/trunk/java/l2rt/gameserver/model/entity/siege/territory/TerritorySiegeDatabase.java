package l2rt.gameserver.model.entity.siege.territory;

import javolution.util.FastMap;
import l2rt.Config;
import l2rt.database.DatabaseUtils;
import l2rt.database.FiltredPreparedStatement;
import l2rt.database.L2DatabaseFactory;
import l2rt.database.ThreadConnection;
import l2rt.gameserver.instancemanager.CastleManager;
import l2rt.gameserver.model.entity.residence.Castle;
import l2rt.gameserver.model.entity.siege.SiegeClan;
import l2rt.gameserver.model.entity.siege.SiegeSpawn;
import l2rt.util.GArray;
import l2rt.util.Location;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.ResultSet;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Logger;

public class TerritorySiegeDatabase
{
	private static Logger _log = Logger.getLogger(TerritorySiegeDatabase.class.getName());

	public static void loadSiegeMembers()
	{
		TerritorySiege.getPlayers().clear();
		TerritorySiege.getClans().clear();

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT obj_Id, side, type FROM siege_territory_members");
			rset = statement.executeQuery();

			while(rset.next())
				if(rset.getInt("type") == 0)
					TerritorySiege.getPlayers().put(rset.getInt("obj_Id"), rset.getInt("side"));
				else
					TerritorySiege.getClans().put(new SiegeClan(rset.getInt("obj_Id"), null), rset.getInt("side"));
		}
		catch(Exception e)
		{
			_log.warning("Exception: loadSiegeMembers(): " + e);
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	public static void saveSiegeMembers()
	{
		clearSiegeMembers();
		for(Entry<Integer, Integer> entry : TerritorySiege.getPlayers().entrySet())
			saveSiegeMember(entry.getKey(), entry.getValue(), 0);
		for(Entry<SiegeClan, Integer> entry : TerritorySiege.getClans().entrySet())
			saveSiegeMember(entry.getKey().getClanId(), entry.getValue(), 1);
	}

	/**
	 * type: 1 - clan, 0 - mercenary
	 */
	public static void saveSiegeMember(int obj_Id, int side, int type)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("REPLACE INTO siege_territory_members (obj_Id, side, type) VALUES (?, ?, ?)");
			statement.setInt(1, obj_Id);
			statement.setInt(2, side);
			statement.setInt(3, type);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.warning("Exception: saveSiegeMember: " + e);
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public static void clearSiegeMembers()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM siege_territory_members");
			statement.execute();
		}
		catch(Exception e)
		{
			_log.warning("Exception: clearSiegeMembers(): " + e);
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	private static FastMap<Integer, GArray<SiegeSpawn>> _flagSpawnList = new FastMap<Integer, GArray<SiegeSpawn>>().setShared(true);

	public static void loadSiegeFlags()
	{
		try
		{
			InputStream is = new FileInputStream(new File(Config.SIEGE_TERRITORY_CONFIGURATION_FILE));
			Properties siegeSettings = new Properties();
			siegeSettings.load(is);
			is.close();

			for(Castle castle : CastleManager.getInstance().getCastles().values())
			{
				int flagItemId = Integer.parseInt(siegeSettings.getProperty(castle.getName() + "FlagItemId", ""));
				int flagNpcId = Integer.parseInt(siegeSettings.getProperty(castle.getName() + "FlagNpcId", ""));
				String spawnParams = siegeSettings.getProperty(castle.getName() + "FlagPos", "");
				if(spawnParams.length() > 0)
				{
					StringTokenizer st = new StringTokenizer(spawnParams.trim(), ",");
					int xc = Integer.parseInt(st.nextToken());
					int yc = Integer.parseInt(st.nextToken());
					int zc = Integer.parseInt(st.nextToken());

					GArray<SiegeSpawn> flagSpawns = new GArray<SiegeSpawn>();
					for(int x = xc - 150; x <= xc + 150; x += 150)
						for(int y = yc - 150; y <= yc + 150; y += 150)
							flagSpawns.add(new SiegeSpawn(castle.getId(), new Location(x, y, zc), flagNpcId, flagItemId));

					_flagSpawnList.put(castle.getId(), flagSpawns);
				}
				else
					_log.warning("Not found flags for " + castle.getName());
			}
		}
		catch(Exception e)
		{
			System.err.println("Error while loading siege data.");
			e.printStackTrace();
		}
	}

	public static FastMap<Integer, GArray<SiegeSpawn>> getSiegeFlags()
	{
		return _flagSpawnList;
	}
}