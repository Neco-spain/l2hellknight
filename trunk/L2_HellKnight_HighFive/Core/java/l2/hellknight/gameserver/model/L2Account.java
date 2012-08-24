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

package l2.hellknight.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import l2.hellknight.L2DatabaseFactory;

/**
 * L2Account
 * 
 * @author BiggBoss
 */

public class L2Account
{
	private static Logger _log = Logger.getLogger(L2Account.class.getName());
	
	private int _reportBotPoints;

	public L2Account(String accName)
	{
		loadBotPoints(accName);
	}
	
	private void loadBotPoints(String accName)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement =  con.prepareStatement("SELECT bot_report_points FROM characters WHERE account_name = ?");
			statement.setString(1, accName);
			
			ResultSet rset = statement.executeQuery();
			while(rset.next())
			{
				_reportBotPoints = rset.getInt("bot_report_points");
			}
			rset.close();
			statement.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				L2DatabaseFactory.close(con);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public synchronized void updatePoints(String accName)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET bot_report_points = ? WHERE account_name = ?");
			statement.setInt(1, _reportBotPoints);
			statement.setString(2, accName);
			statement.execute();
			statement.close();
		}
		catch(SQLException e)
		{
			_log.log(Level.SEVERE, "Couldnt save bot reports points for player "+accName);
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
	
	public synchronized int getReportsPoints()
	{
		return _reportBotPoints;
	}
	
	public synchronized void reducePoints()
	{
		_reportBotPoints--;
	}
}
