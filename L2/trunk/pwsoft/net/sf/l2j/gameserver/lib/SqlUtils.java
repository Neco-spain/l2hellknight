package net.sf.l2j.gameserver.lib;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.mysql.Close;
import net.sf.l2j.mysql.Connect;
import net.sf.l2j.util.log.AbstractLogger;

public class SqlUtils
{
  private static Logger _log = AbstractLogger.getLogger(SqlUtils.class.getName());
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

    PreparedStatement s = null;
    ResultSet rs = null;
    try
    {
      query = L2DatabaseFactory.getInstance().prepQuerySelect(new String[] { resultField }, tableName, whereClause, true);

      s = L2DatabaseFactory.getInstance().getConnection().prepareStatement(query);
      rs = s.executeQuery();

      if (rs.next()) res = Integer.valueOf(rs.getInt(1));
    }
    catch (Exception e)
    {
      _log.warning("Error in query '" + query + "':" + e);
      e.printStackTrace();
    }
    finally
    {
      Close.SR(s, rs);
    }

    return res;
  }

  public static Integer[] getIntArray(String resultField, String tableName, String whereClause)
  {
    String query = "";
    Integer[] res = null;

    PreparedStatement s = null;
    ResultSet rs = null;
    try
    {
      query = L2DatabaseFactory.getInstance().prepQuerySelect(new String[] { resultField }, tableName, whereClause, false);
      s = L2DatabaseFactory.getInstance().getConnection().prepareStatement(query);
      rs = s.executeQuery();

      int rows = 0;

      while (rs.next()) {
        rows++;
      }
      if (rows == 0) { Integer[] arrayOfInteger1 = new Integer[0];
        return arrayOfInteger1;
      }
      res = new Integer[rows - 1];

      rs.first();

      int row = 0;
      while (rs.next())
      {
        res[row] = Integer.valueOf(rs.getInt(1));
      }
    }
    catch (Exception e)
    {
      _log.warning("mSGI: Error in query '" + query + "':" + e);
      e.printStackTrace();
    }
    finally
    {
      Close.SR(s, rs);
    }

    return res;
  }

  public static Integer[][] get2DIntArray(String[] resultFields, String usedTables, String whereClause)
  {
    long start = System.currentTimeMillis();

    String query = "";

    PreparedStatement s = null;
    ResultSet rs = null;

    Integer[][] res = (Integer[][])null;
    try
    {
      query = L2DatabaseFactory.getInstance().prepQuerySelect(resultFields, usedTables, whereClause, false);
      s = L2DatabaseFactory.getInstance().getConnection().prepareStatement(query);
      rs = s.executeQuery();

      int rows = 0;
      while (rs.next()) {
        rows++;
      }
      res = new Integer[rows - 1][resultFields.length];

      rs.first();

      int row = 0;
      while (rs.next())
      {
        for (int i = 0; i < resultFields.length; i++)
          res[row][i] = Integer.valueOf(rs.getInt(i + 1));
        row++;
      }
    }
    catch (Exception e)
    {
      _log.warning("Error in query '" + query + "':" + e);
      e.printStackTrace();
    }
    finally
    {
      Close.SR(s, rs);
    }

    _log.fine("Get all rows in query '" + query + "' in " + (System.currentTimeMillis() - start) + "ms");
    return res;
  }
}