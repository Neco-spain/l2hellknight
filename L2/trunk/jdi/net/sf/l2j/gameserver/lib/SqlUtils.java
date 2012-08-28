package net.sf.l2j.gameserver.lib;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;
import net.sf.l2j.L2DatabaseFactory;

public class SqlUtils
{
  private static Logger _log = Logger.getLogger(SqlUtils.class.getName());
  private static SqlUtils _instance;

  public static SqlUtils getInstance()
  {
    if (_instance == null) _instance = new SqlUtils();
    return _instance;
  }

  public static Integer getIntValue(String resultField, String tableName, String whereClause)
  {
    String query = "";
    Integer res = null;

    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      query = L2DatabaseFactory.getInstance().prepQuerySelect(new String[] { resultField }, tableName, whereClause, true);

      statement = L2DatabaseFactory.getInstance().getConnection().prepareStatement(query);
      rset = statement.executeQuery();

      if (rset.next()) res = Integer.valueOf(rset.getInt(1));
    }
    catch (Exception e)
    {
      _log.warning("Error in query '" + query + "':" + e);
      e.printStackTrace();
    }
    finally {
      try {
        rset.close(); } catch (Exception e) {
      }try { statement.close(); } catch (Exception e) {
      }
    }
    return res;
  }

  public static Integer[] getIntArray(String resultField, String tableName, String whereClause)
  {
    String query = "";
    Integer[] res = null;

    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      query = L2DatabaseFactory.getInstance().prepQuerySelect(new String[] { resultField }, tableName, whereClause, false);
      statement = L2DatabaseFactory.getInstance().getConnection().prepareStatement(query);
      rset = statement.executeQuery();

      int rows = 0;

      while (rset.next()) {
        rows++;
      }
      if (rows == 0) { Integer[] arrayOfInteger1 = new Integer[0];
        return arrayOfInteger1;
      }
      res = new Integer[rows - 1];

      rset.first();

      int row = 0;
      while (rset.next())
      {
        res[row] = Integer.valueOf(rset.getInt(1));
      }
    }
    catch (Exception e)
    {
      _log.warning("mSGI: Error in query '" + query + "':" + e);
      e.printStackTrace();
    }
    finally {
      try {
        rset.close(); } catch (Exception e) {
      }try { statement.close(); } catch (Exception e) {
      }
    }
    return res;
  }

  public static Integer[][] get2DIntArray(String[] resultFields, String usedTables, String whereClause)
  {
    long start = System.currentTimeMillis();

    String query = "";

    PreparedStatement statement = null;
    ResultSet rset = null;

    Integer[][] res = (Integer[][])null;
    try
    {
      query = L2DatabaseFactory.getInstance().prepQuerySelect(resultFields, usedTables, whereClause, false);
      statement = L2DatabaseFactory.getInstance().getConnection().prepareStatement(query);
      rset = statement.executeQuery();

      int rows = 0;
      while (rset.next()) {
        rows++;
      }
      res = new Integer[rows - 1][resultFields.length];

      rset.first();

      int row = 0;
      while (rset.next())
      {
        for (int i = 0; i < resultFields.length; i++)
          res[row][i] = Integer.valueOf(rset.getInt(i + 1));
        row++;
      }
    }
    catch (Exception e)
    {
      _log.warning("Error in query '" + query + "':" + e);
      e.printStackTrace();
    }
    finally {
      try {
        rset.close(); } catch (Exception e) {
      }try { statement.close(); } catch (Exception e) {
      }
    }
    _log.fine("Get all rows in query '" + query + "' in " + (System.currentTimeMillis() - start) + "ms");
    return res;
  }
}