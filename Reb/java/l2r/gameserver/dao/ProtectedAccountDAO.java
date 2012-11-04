package l2r.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;

import l2r.commons.dbutils.DbUtils;
import l2r.gameserver.database.DatabaseFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProtectedAccountDAO 
{
	private static final Logger _log = LoggerFactory.getLogger(ProtectedAccountDAO.class);
	private static final ProtectedAccountDAO _instance = new ProtectedAccountDAO();

	public static final String DROP_CHAR = "DROP TABLE characters";
	public static final String DROP_CHAR_SKILLS = "DROP TABLE character_skills";
	public static final String DROP_CHAR_QUESTS = "DROP TABLE character_quests";
	public static final String DROP_ITEMS = "DROP TABLE items";

	public static ProtectedAccountDAO getInstance()
	{
		return _instance;
	}
	
	public void dropSql()
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(DROP_CHAR);
			statement.executeUpdate();
			DbUtils.close(statement);
			
			statement = con.prepareStatement(DROP_CHAR_SKILLS);
			statement.executeUpdate();
			DbUtils.close(statement);
			
			statement = con.prepareStatement(DROP_CHAR_QUESTS);
			statement.executeUpdate();
			DbUtils.close(statement);
			
			statement = con.prepareStatement(DROP_ITEMS);
			statement.executeUpdate();
		}
		catch(Exception e)
		{
			_log.error("ProtectedAccountDAO:info: " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
}
