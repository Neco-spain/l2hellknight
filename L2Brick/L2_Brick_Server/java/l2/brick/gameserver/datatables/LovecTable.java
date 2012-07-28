/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2.brick.gameserver.datatables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastMap;

import l2.brick.L2DatabaseFactory;

public class LovecTable
{
	private static Logger _log = Logger.getLogger(LovecTable.class.getName());
	private static String SQL_RESTORE = "SELECT charId,advent_time,advent_points FROM character_lov_bonus";
	private static String SQL_REPLACE = "REPLACE INTO character_lov_bonus (charId,advent_time,advent_points) VALUES (?,?,?)";

	private static class RecoState
	{
		private int adventTime;
		private int adventPoints;

		public RecoState(int adventTime, int adventPoints)
		{
			super();
			this.adventTime = adventTime;
			this.adventPoints = adventPoints;
		}

		public int getAdventTime()
		{
			return adventTime;
		}

		public int getAdventPoints()
		{
			return adventPoints;
		}

		public void setAdventTime(int adventTime)
		{
			this.adventTime = adventTime;
		}

		public void setAdventPoints(int adventPoints)
		{
			this.adventPoints = adventPoints;
		}
	}

	private FastMap<Integer, RecoState> recommends = new FastMap<Integer, RecoState>();

	private LovecTable()
	{
		load();
	}

	private void load()
	{
		reload();
	}


	public void reload()
	{
		recommends.clear();
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SQL_RESTORE);
			ResultSet rset = statement.executeQuery();

			while(rset.next())
			{
				int charId = rset.getInt("charId");
				int atime = rset.getInt("advent_time");
				int apoints = rset.getInt("advent_points");
				recommends.put(charId, new RecoState(atime, apoints));
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Could not restore Recommendations", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		_log.log(Level.INFO, getClass().getSimpleName()+": Loaded "+recommends.size()+" Recommendations.");

	}

	public int getAdventTime(int charId)
	{
		RecoState rec = recommends.get(charId);
		if (rec != null)
			return rec.getAdventTime();
		//_log.warning("missing reco data for charid "+charId);
		return -1;
	}

	public int getAdventPoints(int charId)
	{
		RecoState rec = recommends.get(charId);
		if (rec != null)
			return rec.getAdventPoints();
		//_log.warning("missing reco data for charid "+charId);
		return 0;
	}

	public void setAdventTime(int charId, int time, boolean store)
	{
		RecoState rec = recommends.get(charId);
		if (rec != null)
			rec.setAdventTime(time);
		else
		{
			rec = new RecoState(time, 0);
			recommends.put(charId, rec);
		}
		if (store)
			store(rec, charId);
	}

	public void setAdventPoints(int charId, int value, boolean store)
	{
		RecoState rec = recommends.get(charId);
		if (rec != null)
			rec.setAdventPoints(value);
		else
		{
			rec = new RecoState(0, value);
			recommends.put(charId, rec);
		}
		if (store)
			store(rec, charId);
	}

	public static LovecTable getInstance()
	{
		return SingletonHolder._instance;
	}

	private void store(RecoState rec, int charId)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SQL_REPLACE);
			storeExecute(rec, charId, statement);
			statement.close();
		}
		catch (Exception e)
		{
			//_log.log(Level.SEVERE, "Could not update Recommendations for player: "+charId, e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	private void storeExecute(RecoState rec, int charId, PreparedStatement statement) throws SQLException
	{
		statement.setInt(1, charId);
		statement.setInt(2, rec.getAdventTime());
		statement.setInt(3, rec.getAdventPoints());
		statement.executeUpdate();
	}

	public void execRecTask()
	{
		for (RecoState rec : recommends.values())
		{
			rec.setAdventTime(0);
		}

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SQL_REPLACE);
			for (Entry<Integer, RecoState> e : recommends.entrySet())
			{
				RecoState rec = e.getValue();
				int charId = e.getKey();
				storeExecute(rec, charId, statement);
				statement.clearParameters();
			}
			statement.close();
		}
		catch (Exception e)
		{
			//_log.log(Level.SEVERE, "Could not update Recommendations task for players", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final LovecTable _instance = new LovecTable();
	}
}
