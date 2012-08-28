package net.sf.l2j.gameserver.util.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ScheduledFuture;
import javolution.util.FastList;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.util.CloseUtil;

public class SQLQueue
  implements Runnable
{
  private static SQLQueue _instance = null;
  private FastList<SQLQuery> _query;
  private ScheduledFuture<?> _task;
  private boolean _inShutdown;
  private boolean _isRuning;

  public static SQLQueue getInstance()
  {
    if (_instance == null)
      _instance = new SQLQueue();
    return _instance;
  }

  private SQLQueue()
  {
    _query = new FastList();
    _task = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(this, 60000L, 60000L);
  }

  public void shutdown() {
    _inShutdown = true;
    _task.cancel(false);
    if ((!_isRuning) && (_query.size() > 0))
      run();
  }

  public void add(SQLQuery q) {
    if (!_inShutdown)
      _query.addLast(q);
  }

  public void run()
  {
    _isRuning = true;
    synchronized (_query)
    {
      while (_query.size() > 0)
      {
        SQLQuery q = (SQLQuery)_query.removeFirst();
        Connection _con = null;
        try {
          _con = L2DatabaseFactory.getInstance().getConnection();

          q.execute(_con);
        }
        catch (SQLException e)
        {
        }
        finally
        {
          CloseUtil.close(_con);
          _con = null;
        }
      }
    }
    Connection _con = null;
    try {
      _con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement stm = _con.prepareStatement("select * from characters where char_name is null");
      stm.execute();
      stm.close();
    }
    catch (SQLException e)
    {
    }
    finally
    {
      CloseUtil.close(_con);
      _con = null;
    }
    _isRuning = false;
  }
}