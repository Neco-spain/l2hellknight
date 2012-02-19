package l2.brick.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import l2.brick.L2DatabaseFactory;

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
				con.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public synchronized void updatePoints(String accName) throws SQLException
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
			_log.log(Level.SEVERE, "Couldnt save bot reports points for "+accName);
			e.printStackTrace();
		}
		finally
		{
			con.close();
			if(!con.isClosed())
				throw new SQLException();
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