package net.sf.l2j.gameserver.idfactory;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Stack;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;

public class StackIDFactory extends IdFactory
{
  private static Logger _log = Logger.getLogger(IdFactory.class.getName());
  private int _curOID;
  private int _tempOID;
  private Stack<Integer> _freeOIDStack = new Stack();

  protected StackIDFactory()
  {
    _curOID = 268435456;
    _tempOID = 268435456;

    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      int[] tmp_obj_ids = extractUsedObjectIDTable();
      if (tmp_obj_ids.length > 0)
      {
        _curOID = tmp_obj_ids[(tmp_obj_ids.length - 1)];
      }
      System.out.println("Max Id = " + _curOID);

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
        con.close(); } catch (Exception e) {
      }
    }
  }

  private int insertUntil(int[] tmp_obj_ids, int idx, int N, Connection con) throws SQLException {
    int id = tmp_obj_ids[idx];
    if (id == _tempOID)
    {
      _tempOID += 1;
      return N;
    }

    if (Config.BAD_ID_CHECKING)
    {
      for (String check : ID_CHECKS)
      {
        PreparedStatement ps = con.prepareStatement(check);
        ps.setInt(1, _tempOID);

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

    int hole = id - _tempOID;
    if (hole > N - idx) hole = N - idx;
    for (int i = 1; i <= hole; i++)
    {
      _freeOIDStack.push(Integer.valueOf(_tempOID));
      _tempOID += 1;
    }

    if (hole < N - idx) _tempOID += 1;
    return N - hole;
  }

  public static IdFactory getInstance()
  {
    return _instance;
  }

  public synchronized int getNextId()
  {
    int id;
    int id;
    if (!_freeOIDStack.empty()) {
      id = ((Integer)_freeOIDStack.pop()).intValue();
    }
    else {
      id = _curOID;
      _curOID += 1;
    }
    return id;
  }

  public synchronized void releaseId(int id)
  {
    _freeOIDStack.push(Integer.valueOf(id));
  }

  public int size()
  {
    return 1879048191 - _curOID + 268435456 + _freeOIDStack.size();
  }
}