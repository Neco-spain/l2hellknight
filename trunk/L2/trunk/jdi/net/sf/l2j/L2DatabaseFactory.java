package net.sf.l2j;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.l2j.gameserver.ThreadPoolManager;

public class L2DatabaseFactory
{
  static Logger _log = Logger.getLogger(L2DatabaseFactory.class.getName());
  private static L2DatabaseFactory _instance;
  private ProviderType _providerType;
  private ComboPooledDataSource _source;

  public L2DatabaseFactory()
    throws SQLException
  {
    try
    {
      if (Config.DATABASE_MAX_CONNECTIONS < 2)
      {
        Config.DATABASE_MAX_CONNECTIONS = 2;
        _log.warning("at least " + Config.DATABASE_MAX_CONNECTIONS + " db connections are required.");
      }

      _source = new ComboPooledDataSource();
      _source.setAutoCommitOnClose(true);

      _source.setInitialPoolSize(10);
      _source.setMinPoolSize(10);
      _source.setMaxPoolSize(Config.DATABASE_MAX_CONNECTIONS);

      _source.setAcquireRetryAttempts(0);
      _source.setAcquireRetryDelay(500);
      _source.setCheckoutTimeout(0);

      _source.setAcquireIncrement(5);

      _source.setAutomaticTestTable("connection_test_table");
      _source.setTestConnectionOnCheckin(false);

      _source.setIdleConnectionTestPeriod(3600);
      _source.setMaxIdleTime(0);

      _source.setMaxStatementsPerConnection(100);

      _source.setBreakAfterAcquireFailure(false);

      _source.setDriverClass(Config.DATABASE_DRIVER);
      _source.setJdbcUrl(Config.DATABASE_URL);
      _source.setUser(Config.DATABASE_LOGIN);
      _source.setPassword(Config.DATABASE_PASSWORD);

      _source.getConnection().close();

      if (Config.DEBUG) _log.fine("Database Connection Working");

      if (Config.DATABASE_DRIVER.toLowerCase().contains("microsoft"))
        _providerType = ProviderType.MsSql;
      else
        _providerType = ProviderType.MySql;
    }
    catch (SQLException x)
    {
      if (Config.DEBUG) _log.fine("Database Connection FAILED");

      throw x;
    }
    catch (Exception e)
    {
      if (Config.DEBUG) _log.fine("Database Connection FAILED");
      throw new SQLException("could not init DB connection:" + e);
    }
  }

  public final String prepQuerySelect(String[] fields, String tableName, String whereClause, boolean returnOnlyTopRecord)
  {
    String msSqlTop1 = "";
    String mySqlTop1 = "";
    if (returnOnlyTopRecord)
    {
      if (getProviderType() == ProviderType.MsSql) msSqlTop1 = " Top 1 ";
      if (getProviderType() == ProviderType.MySql) mySqlTop1 = " Limit 1 ";
    }
    String query = "SELECT " + msSqlTop1 + safetyString(fields) + " FROM " + tableName + " WHERE " + whereClause + mySqlTop1;
    return query;
  }

  public void shutdown()
  {
    try {
      _source.close(); } catch (Exception e) {
      _log.log(Level.INFO, "", e);
    }try {
      _source = null; } catch (Exception e) {
      _log.log(Level.INFO, "", e);
    }
  }

  public final String safetyString(String[] whatToCheck)
  {
    String braceLeft = "`";
    String braceRight = "`";
    if (getProviderType() == ProviderType.MsSql)
    {
      braceLeft = "[";
      braceRight = "]";
    }

    String result = "";
    for (String word : whatToCheck)
    {
      if (result != "") result = result + ", ";
      result = result + braceLeft + word + braceRight;
    }
    return result;
  }

  public static L2DatabaseFactory getInstance()
    throws SQLException
  {
    if (_instance == null)
    {
      _instance = new L2DatabaseFactory();
    }
    return _instance;
  }

  public Connection getConnection()
  {
    Connection con = null;

    while (con == null)
    {
      try
      {
        con = _source.getConnection();
        if (Server.serverMode == 1)
          ThreadPoolManager.getInstance().scheduleGeneral(new Proverka(con, new RuntimeException()), 300000L);
      }
      catch (SQLException e)
      {
        _log.warning("L2DatabaseFactory: getConnection() failed, trying again " + e);
      }
    }
    return con;
  }

  public int getBusyConnectionCount() throws SQLException
  {
    return _source.getNumBusyConnectionsDefaultUser();
  }

  public int getIdleConnectionCount() throws SQLException
  {
    return _source.getNumIdleConnectionsDefaultUser();
  }
  public final ProviderType getProviderType() {
    return _providerType;
  }

  private class Proverka
    implements Runnable
  {
    private Connection c;
    private RuntimeException exp;

    public Proverka(Connection con, RuntimeException e)
    {
      c = con;
      exp = e;
    }

    public void run()
    {
      try
      {
        if (!c.isClosed())
        {
          L2DatabaseFactory._log.log(Level.WARNING, "Ne zakritiy connect: " + exp.getStackTrace()[1], exp);
        }

      }
      catch (SQLException e2)
      {
        e2.printStackTrace();
      }
    }
  }

  public static enum ProviderType
  {
    MySql, 
    MsSql;
  }
}