package l2p.loginserver.accounts;

import l2p.commons.dbutils.DbUtils;
import l2p.loginserver.database.L2DatabaseFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: Cain
 * Date: 12.03.12
 * Time: 19:23
 */
public class SecondaryPasswordAuth {
    private static final Logger _log = Logger.getLogger(SecondaryPasswordAuth.class.getName());

    private static final String SELECT_PARAMS = "SELECT account_password, wrongAttempts, banTime FROM account_2ndAuth WHERE account_name=?";
    private static final String INSERT_PASSWORD = "INSERT INTO account_2ndAuth VALUES (?, ?, 0, 0) ON DUPLICATE KEY UPDATE account_password=?";
    private static final String UPDATE_WA = "UPDATE account_2ndAuth SET wrongAttempts=? WHERE account_name=?";
    private static final String UPDATE_BT = "UPDATE account_2ndAuth SET banTime=? WHERE account_name=?";

    public static long getBanTime(String login)
    {
        long _unBanTime = 0;

        Connection con = null;
        PreparedStatement statement = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(SELECT_PARAMS);
            statement.setString(1, login);
            for (ResultSet rs = statement.executeQuery();rs.next();)
                _unBanTime = rs.getLong("banTime");
        }
        catch (Exception e)
        {
            _log.log(Level.SEVERE, "Error while reading bantime from base.", e);
        }
        finally
        {
            DbUtils.closeQuietly(con, statement);
        }
        return _unBanTime;
    }

    public static void setBanTime(String login, int banTime)
    {
        long _unBanTime = System.currentTimeMillis()+(banTime*60000);
        Connection con = null;
        PreparedStatement statement = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(UPDATE_BT);
            statement.setLong(1, _unBanTime);
            statement.setString(2, login);
            statement.execute();
        }
        catch (Exception e)
        {
            _log.log(Level.SEVERE, "Error while writing unban time.", e);
        }
        finally
        {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public static int getLoginAttempts(String login)
    {
        int _loginAttempts = 0;

        Connection con = null;
        PreparedStatement statement = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(SELECT_PARAMS);
            statement.setString(1, login);
            for (ResultSet rs = statement.executeQuery();rs.next();)
                _loginAttempts = rs.getInt("wrongAttempts");
        }
        catch (Exception e)
        {
            _log.log(Level.SEVERE, "Error while reading login attempts from base.", e);
        }
        finally
        {
            DbUtils.closeQuietly(con, statement);
        }
        return _loginAttempts;
    }

    public static void setLoginAttempts(String login, int enterAttempts)
    {
        Connection con = null;
        PreparedStatement statement = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(UPDATE_WA);
            statement.setInt(1, enterAttempts);
            statement.setString(2, login);
            statement.execute();
        }
        catch (Exception e)
        {
            _log.log(Level.SEVERE, "Error while writing wrong attempts.", e);
        }
        finally
        {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public static String getPassword(String login)
    {
        String _password = null;
        Connection con = null;
        PreparedStatement statement = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(SELECT_PARAMS);
            statement.setString(1, login);
            for (ResultSet rs = statement.executeQuery();rs.next();)
                _password = rs.getString("account_password");
        }
        catch (Exception e)
        {
            _log.log(Level.SEVERE, "Error while reading password from base.", e);
        }
        finally
        {
            DbUtils.closeQuietly(con, statement);
        }
        return _password;
    }

    public static void setPassword(String login, String password)
    {
        Connection con = null;
        PreparedStatement statement = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(INSERT_PASSWORD);
            statement.setString(1, login);
            statement.setString(2, password);
            statement.setString(3, password);
            statement.execute();
        }
        catch (Exception e)
        {
            _log.log(Level.SEVERE, "Error while writing password.", e);
        }
        finally
        {
            DbUtils.closeQuietly(con, statement);
        }
    }
}
