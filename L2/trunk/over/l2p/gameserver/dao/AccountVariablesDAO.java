package l2p.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import l2p.commons.dbutils.DbUtils;
import l2p.gameserver.database.DatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountVariablesDAO
{
  private static final Logger _log = LoggerFactory.getLogger(AccountVariablesDAO.class);
  private static final AccountVariablesDAO _instance = new AccountVariablesDAO();
  public static final String SELECT_SQL_QUERY = "SELECT var, value FROM account_variables WHERE account_name=?";
  public static final String DELETE_SQL_QUERY = "DELETE FROM account_variables WHERE account=? AND var=?";
  public static final String INSERT_SQL_QUERY = "REPLACE INTO account_variables(account_name, var, value) VALUES (?,?,?)";
  public static final String UPDATE_SQL_QUERY = "UPDATE account_variables SET value=? WHERE account=? AND var=?";

  public static AccountVariablesDAO getInstance()
  {
    return _instance;
  }

  public String select(String account, String var)
  {
    String result_value = "";
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT var, value FROM account_variables WHERE account_name=?");
      statement.setString(1, account);
      statement.setString(2, var);
      rset = statement.executeQuery();
      if (rset.next())
        result_value = rset.getString("value");
    }
    catch (Exception e)
    {
      _log.info("AccountVariablesDAO.select(String, String): " + e, e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement, rset);
    }
    return result_value;
  }

  public void delete(String account, String var)
  {
    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("DELETE FROM account_variables WHERE account=? AND var=?");
      statement.setString(1, account);
      statement.setString(2, var);
      statement.execute();
    }
    catch (Exception e)
    {
      _log.info("AccountVariablesDAO.delete(String, String): " + e, e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }
  }

  public void insert(String account, String var, String value)
  {
    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("REPLACE INTO account_variables(account_name, var, value) VALUES (?,?,?)");
      statement.setString(1, account);
      statement.setString(2, var);
      statement.setString(3, value);
      statement.execute();
    }
    catch (Exception e)
    {
      _log.info("AccountVariablesDAO.insert(String, String, String): " + e, e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }
  }

  public void update(String account, String var, String value)
  {
    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("UPDATE account_variables SET value=? WHERE account=? AND var=?");
      statement.setString(1, value);
      statement.setString(2, account);
      statement.setString(3, var);
      statement.execute();
    }
    catch (Exception e)
    {
      _log.info("AccountVariablesDAO.update(String, String, String): " + e, e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }
  }
}