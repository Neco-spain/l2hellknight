package net.sf.l2j.gameserver.idfactory;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;

public class CompactionIDFactory extends IdFactory
{
  private static Logger _log = Logger.getLogger(CompactionIDFactory.class.getName());
  private int _curOID;
  private int _freeSize;

  protected CompactionIDFactory()
  {
    _curOID = 268435456;
    _freeSize = 0;

    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      int[] tmp_obj_ids = extractUsedObjectIDTable();

      int N = tmp_obj_ids.length;
      for (int idx = 0; idx < N; idx++)
      {
        N = insertUntil(tmp_obj_ids, idx, N, con);
      }
      _curOID += 1;
      _log.config("IdFactory: Next usable Object ID is: " + _curOID);
      _initialized = true;
    }
    catch (Exception e)
    {
      e1.printStackTrace();
      _log.severe("ID Factory could not be initialized correctly:" + e1);
    }
    finally {
      try {
        con.close();
      } catch (Exception e) {
      }
    }
  }

  private int insertUntil(int[] tmp_obj_ids, int idx, int N, Connection con) throws SQLException {
    int id = tmp_obj_ids[idx];
    if (id == _curOID)
    {
      _curOID += 1;
      return N;
    }

    if (Config.BAD_ID_CHECKING)
    {
      for (String check : ID_CHECKS)
      {
        PreparedStatement ps = con.prepareStatement(check);
        ps.setInt(1, _curOID);
        ps.setInt(2, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next())
        {
          int badId = rs.getInt(1);
          _log.severe("Bad ID " + badId + " in DB found by: " + check);
          throw new RuntimeException();
        }
        rs.close();
        ps.close();
      }
    }

    int hole = id - _curOID;
    if (hole > N - idx) hole = N - idx;
    for (int i = 1; i <= hole; i++)
    {
      id = tmp_obj_ids[(N - i)];
      System.out.println("Compacting DB object ID=" + id + " into " + _curOID);
      for (String update : ID_UPDATES)
      {
        PreparedStatement ps = con.prepareStatement(update);
        ps.setInt(1, _curOID);
        ps.setInt(2, id);
        ps.execute();
        ps.close();
      }
      _curOID += 1;
    }
    if (hole < N - idx) _curOID += 1;
    return N - hole;
  }

  public synchronized int getNextId()
  {
    return _curOID++;
  }

  public synchronized void releaseId(int id)
  {
  }

  public int size()
  {
    return _freeSize + 2147483647 - 268435456;
  }
}