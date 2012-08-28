package net.sf.l2j.gameserver.datatables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;
import net.sf.l2j.L2DatabaseFactory;

public class CharNameTable
{
  private static Logger _log = Logger.getLogger(CharNameTable.class.getName());
  private static CharNameTable _instance;

  public static CharNameTable getInstance()
  {
    if (_instance == null)
    {
      _instance = new CharNameTable();
    }
    return _instance;
  }

  public boolean doesCharNameExist(String name)
  {
    boolean result = true;
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("SELECT account_name FROM characters WHERE char_name=?");
      statement.setString(1, name);
      ResultSet rset = statement.executeQuery();
      result = rset.next();
      rset.close();
      statement.close();
    }
    catch (SQLException e)
    {
      _log.warning("could not check existing charname:" + e.getMessage());
    }
    finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
    return result;
  }

  public int accountCharNumber(String account)
  {
    Connection con = null;
    int number = 0;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("SELECT COUNT(char_name) FROM characters WHERE account_name=?");
      statement.setString(1, account);
      ResultSet rset = statement.executeQuery();
      while (rset.next())
      {
        number = rset.getInt(1);
      }
      rset.close();
      statement.close();
    }
    catch (SQLException e)
    {
      _log.warning("could not check existing char number:" + e.getMessage());
    }
    finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
    return number;
  }
}