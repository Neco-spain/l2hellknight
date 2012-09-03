package l2rt.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

public class ThreadConnection
{
	static Logger _log = Logger.getLogger(ThreadConnection.class.getName());

	private final Connection myConnection;
	private final L2DatabaseFactory myFactory;
	private int counter = 1;

	public ThreadConnection(Connection con, L2DatabaseFactory f)
	{
		myConnection = con;
		myFactory = f;
	}

	public void updateCounter()
	{
		counter++;
	}

	public FiltredPreparedStatement prepareStatement(String sql) throws SQLException
	{
		return new FiltredPreparedStatement(myConnection.prepareStatement(sql));
	}

	public void close()
	{
		counter--;
		if(counter == 0)
			try
			{
				synchronized (myFactory.getConnections())
				{
					myConnection.close();
					String key = myFactory.generateKey();
					myFactory.getConnections().remove(key);
				}
			}
			catch(Exception e)
			{
				_log.warning("Couldn't close connection. Cause: " + e.getMessage());
			}
	}

	public FiltredStatement createStatement() throws SQLException
	{
		return new FiltredStatement(myConnection.createStatement());
	}
}