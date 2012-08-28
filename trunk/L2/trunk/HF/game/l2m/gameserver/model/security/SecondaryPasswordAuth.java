package l2m.gameserver.model.security;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import jonelo.sugar.util.Base64;
import l2p.commons.dbutils.DbUtils;
import l2m.gameserver.Config;
import l2m.gameserver.database.DatabaseFactory;
import l2m.gameserver.loginservercon.LoginServerCommunication;
import l2m.gameserver.loginservercon.gspackets.ChangeAccessLevel;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.Ex2ndPasswordAck;
import l2m.gameserver.network.serverpackets.Ex2ndPasswordCheck;
import l2m.gameserver.network.serverpackets.Ex2ndPasswordVerify;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecondaryPasswordAuth
{
  private static final Logger _log = LoggerFactory.getLogger(SecondaryPasswordAuth.class);
  private final GameClient _activeClient;
  private String _password;
  private int _wrongAttempts;
  private boolean _authed;
  private static final String SELECT_PASSWORD = "SELECT attempts, password FROM account_sauth WHERE account_name=?";
  private static final String INSERT_PASSWORD = "INSERT INTO account_sauth VALUES (?, ?, ?)";
  private static final String UPDATE_PASSWORD = "UPDATE account_sauth SET password=? WHERE account_name=?";
  private static final String UPDATE_COUNT_WRONG = "UPDATE account_sauth SET attempts=? WHERE account_name=?";

  public SecondaryPasswordAuth(GameClient activeClient)
  {
    _activeClient = activeClient;
    _password = null;
    _wrongAttempts = 0;
    _authed = false;
    loadPassword();
  }

  private void loadPassword()
  {
    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT attempts, password FROM account_sauth WHERE account_name=?");
      statement.setString(1, _activeClient.getLogin());
      ResultSet rs = statement.executeQuery();
      while (rs.next())
      {
        _password = rs.getString("password");
        _wrongAttempts = rs.getInt("attempts");
      }
      statement.close();
    }
    catch (Exception e)
    {
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }
  }

  public boolean savePassword(String password)
  {
    if (passwordExist())
    {
      _log.warn("[SecondaryPasswordAuth]" + _activeClient.getLogin() + " forced savePassword");
      _activeClient.closeNow(true);
      return false;
    }

    if (!validatePassword(password))
    {
      _activeClient.sendPacket(new Ex2ndPasswordAck(1));
      return false;
    }

    password = cryptPassword(password);

    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("INSERT INTO account_sauth VALUES (?, ?, ?)");
      statement.setString(1, _activeClient.getLogin());
      statement.setInt(2, 0);
      statement.setString(3, password);
      statement.execute();
      statement.close();
    }
    catch (Exception e)
    {
      _log.error("Error while writing password.", e);
    } finally {
      DbUtils.closeQuietly(con, statement);
    }
    _password = password;
    return true;
  }

  public boolean insertWrongAttempt(int attempts)
  {
    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("UPDATE account_sauth SET attempts=? WHERE account_name=?");
      statement.setInt(1, attempts);
      statement.setString(2, _activeClient.getLogin());
      statement.execute();
      statement.close();
    }
    catch (Exception e)
    {
      _log.error("Error while writing wrong attempts.", e);
    } finally {
      DbUtils.closeQuietly(con, statement);
    }
    return true;
  }

  public boolean changePassword(String oldPassword, String newPassword) {
    if (!passwordExist())
    {
      _log.warn("[SecondaryPasswordAuth]" + _activeClient.getLogin() + " forced changePassword");
      _activeClient.closeNow(true);
      return false;
    }

    if (!checkPassword(oldPassword, true)) {
      return false;
    }
    if (!validatePassword(newPassword))
    {
      _activeClient.sendPacket(new Ex2ndPasswordAck(1));
      return false;
    }

    newPassword = cryptPassword(newPassword);

    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("UPDATE account_sauth SET password=? WHERE account_name=?");
      statement.setString(1, newPassword);
      statement.setString(2, _activeClient.getLogin());
      statement.execute();
      statement.close();
    }
    catch (Exception e)
    {
      _log.error("Error while reading password.", e);
    } finally {
      DbUtils.closeQuietly(con, statement);
    }
    _password = newPassword;
    _authed = false;
    return true;
  }

  public boolean checkPassword(String password, boolean skipAuth)
  {
    password = cryptPassword(password);
    if (!password.equals(_password))
    {
      _wrongAttempts += 1;
      if (_wrongAttempts < Config.EX_SECOND_AUTH_MAX_ATTEMPTS)
      {
        _activeClient.sendPacket(new Ex2ndPasswordVerify(1, _wrongAttempts));
        insertWrongAttempt(_wrongAttempts);
      }
      else
      {
        insertWrongAttempt(0);
        LoginServerCommunication.getInstance().sendPacket(new ChangeAccessLevel(_activeClient.getLogin(), 0, (int)(System.currentTimeMillis() / 1000L) + Config.EX_SECOND_AUTH_BAN_TIME * 60));
        _activeClient.close(new Ex2ndPasswordVerify(2, Config.EX_SECOND_AUTH_MAX_ATTEMPTS));
      }
      return false;
    }
    if (!skipAuth)
    {
      _authed = true;
      _activeClient.sendPacket(new Ex2ndPasswordVerify(0, _wrongAttempts));
    }
    insertWrongAttempt(0);
    return true;
  }

  public boolean passwordExist() {
    return _password != null;
  }

  public void openDialog() {
    if (passwordExist())
      _activeClient.sendPacket(new Ex2ndPasswordCheck(1));
    else
      _activeClient.sendPacket(new Ex2ndPasswordCheck(0));
  }

  public boolean isAuthed() {
    return _authed;
  }

  private String cryptPassword(String password)
  {
    try
    {
      MessageDigest md = MessageDigest.getInstance("SHA");
      byte[] raw = password.getBytes("UTF-8");
      byte[] hash = md.digest(raw);
      return Base64.encodeBytes(hash);
    }
    catch (NoSuchAlgorithmException e)
    {
      _log.error("[SecondaryPasswordAuth]Unsupported Algorythm");
    }
    catch (UnsupportedEncodingException e)
    {
      _log.error("[SecondaryPasswordAuth]Unsupported Encoding");
    }

    return null;
  }

  private boolean validatePassword(String password)
  {
    if ((password.length() < 6) || (password.length() > 8))
    {
      return false;
    }
    if (Config.EX_SECOND_AUTH_HARD_PASS)
    {
      for (int i = 0; i < password.length() - 1; i++)
      {
        char curCh = password.charAt(i);
        char nxtCh = password.charAt(i + 1);

        if (curCh + '\001' == nxtCh)
          return false;
        if (curCh - '\001' == nxtCh)
          return false;
        if (curCh == nxtCh) {
          return false;
        }
      }
      for (int i = 0; i < password.length() - 2; i++)
      {
        String toChk = password.substring(i + 1);
        StringBuffer chkEr = new StringBuffer(password.substring(i, i + 2));

        if (toChk.contains(chkEr))
          return false;
        if (toChk.contains(chkEr.reverse()))
          return false;
      }
    }
    _wrongAttempts = 0;
    return true;
  }
}