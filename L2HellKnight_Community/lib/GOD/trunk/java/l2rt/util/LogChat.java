package l2rt.util;

import l2rt.Config;
import l2rt.database.DatabaseUtils;
import l2rt.database.FiltredPreparedStatement;
import l2rt.database.L2DatabaseFactory;
import l2rt.database.ThreadConnection;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class LogChat
{
	private static final Logger _logChat = Logger.getLogger("chat");
	private static final Logger _log = Logger.getLogger(LogChat.class.getName());

	public static void add(String text, String type, String from, String to)
	{
		if(Config.LOG_CHAT)
		{
			LogRecord record = new LogRecord(Level.INFO, text);
			record.setLoggerName("chat");
			if(to != null && !to.isEmpty())
				record.setParameters(new Object[] { type, "[" + from + " to " + to + "]" });
			else
				record.setParameters(new Object[] { type, "[" + from + "]" });
			_logChat.log(record);
		}

		if(Config.LOG_CHAT_DB != null && !Config.LOG_CHAT_DB.isEmpty())
		{
			ThreadConnection con = null;
			FiltredPreparedStatement statement = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("INSERT DELAYED INTO " + Config.LOG_CHAT_DB + " (`type`,`text`,`from`,`to`) VALUES(?,?,?,?);");
				statement.setString(1, type.trim());
				statement.setString(2, text);
				statement.setString(3, from);
				statement.setString(4, to != null ? to : "");
				statement.execute();
			}
			catch(Exception e)
			{
				_log.warning("fail to sql log chat[" + type + "|" + text + "|" + from + "|" + to + "]: " + e);
				e.printStackTrace();
			}
			finally
			{
				DatabaseUtils.closeDatabaseCS(con, statement);
			}
		}
	}
}