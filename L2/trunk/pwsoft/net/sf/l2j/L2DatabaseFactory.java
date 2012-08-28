package net.sf.l2j;

import com.jolbox.bonecp.BoneCPConfig;
import com.jolbox.bonecp.BoneCPDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.text.TextBuilder;
import net.sf.l2j.mysql.Connect;

public class L2DatabaseFactory
{
  static final Logger _log = Logger.getLogger(L2DatabaseFactory.class.getName());
  private static L2DatabaseFactory _cins;
  private ProviderType _a;
  private BoneCPDataSource connectionPool;
  private static final Map<String, Connect> cons = new ConcurrentHashMap();

  public L2DatabaseFactory()
    throws SQLException
  {
    try
    {
      BoneCPConfig config = new BoneCPConfig();
      config.setJdbcUrl(Config.DATABASE_URL);
      config.setUsername(Config.DATABASE_LOGIN);
      config.setPassword(Config.DATABASE_PASSWORD);
      config.setMinConnectionsPerPartition(Config.MINCONNECTIONSPERPARTITION);
      config.setMaxConnectionsPerPartition(Config.MAXCONNECTIONSPERPARTITION);
      config.setPartitionCount(Config.PARTITIONCOUNT);

      config.setAcquireIncrement(Config.ACQUIREINCREMENT);
      config.setIdleConnectionTestPeriod(Config.IDLECONNECTIONTESTPERIOD);
      config.setIdleMaxAge(Config.IDLEMAXAGE);
      config.setReleaseHelperThreads(Config.RELEASEHELPERTHREADS);
      config.setAcquireRetryDelay(Config.ACQUIRERETRYDELAY);
      config.setAcquireRetryAttempts(Config.ACQUIRERETRYATTEMPTS);
      config.setLazyInit(Config.LAZYINIT);
      config.setTransactionRecoveryEnabled(Config.TRANSACTIONRECOVERYENABLED);
      config.setQueryExecuteTimeLimit(Config.QUERYEXECUTETIMELIMIT);
      config.setConnectionTimeout(Config.CONNECTIONTIMEOUT);
      connectionPool = new BoneCPDataSource(config);
      connectionPool.getConnection().close();
    }
    catch (SQLException x) {
      throw x;
    }
  }

  public final String prepQuerySelect(String[] fields, String tableName, String whereClause, boolean returnOnlyTopRecord)
  {
    String msSqlTop1 = "";
    String mySqlTop1 = "";
    if (returnOnlyTopRecord) {
      if (getProviderType() == ProviderType.MsSql) {
        msSqlTop1 = " Top 1 ";
      }
      if (getProviderType() == ProviderType.MySql) {
        mySqlTop1 = " Limit 1 ";
      }
    }
    String query = "SELECT " + msSqlTop1 + safetyString(fields) + " FROM " + tableName + " WHERE " + whereClause + mySqlTop1;
    return query;
  }

  public final String safetyString(String[] whatToCheck)
  {
    String braceLeft = "`";
    String braceRight = "`";
    if (getProviderType() == ProviderType.MsSql) {
      braceLeft = "[";
      braceRight = "]";
    }

    TextBuilder result = new TextBuilder("");
    for (String word : whatToCheck) {
      if (!result.toString().equals("")) {
        result.append(", ");
      }
      result.append(braceLeft + word + braceRight);
    }
    return result.toString();
  }

  public static L2DatabaseFactory getInstance()
    throws SQLException
  {
    return _cins;
  }

  public static void init() throws SQLException {
    _cins = new L2DatabaseFactory();
  }

  public Connect getConnection() throws SQLException {
    String key = generateKey();
    Connect con = get(key);
    if (con == null)
      con = new Connect(connectionPool.getConnection());
    else {
      con.updateCounter();
    }

    put(key, con);
    return con;
  }

  public Map<String, Connect> getConnections() {
    return cons;
  }

  public int getCounts() {
    return cons.size();
  }

  private Connect get(String k) {
    return (Connect)cons.get(k);
  }

  private void put(String k, Connect c) {
    cons.put(k, c);
  }

  public void remove(String k) {
    cons.remove(k);
  }

  public void shutdown() {
    try {
      connectionPool.close();
    } catch (Exception e) {
      _log.log(Level.INFO, "", e);
    }
    cons.clear();
  }

  public String generateKey() {
    return String.valueOf(Thread.currentThread().hashCode());
  }

  public final ProviderType getProviderType()
  {
    return _a;
  }

  public static enum ProviderType
  {
    MySql, 
    MsSql;
  }
}