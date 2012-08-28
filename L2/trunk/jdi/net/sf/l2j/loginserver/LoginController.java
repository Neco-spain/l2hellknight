package net.sf.l2j.loginserver;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.RSAKeyGenParameterSpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javolution.util.FastCollection.Record;
import javolution.util.FastMap;
import javolution.util.FastMap.Entry;
import javolution.util.FastSet;
import net.sf.l2j.Base64;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.lib.Log;
import net.sf.l2j.loginserver.crypt.ScrambledKeyPair;
import net.sf.l2j.loginserver.serverpackets.LoginFail.LoginFailReason;
import net.sf.l2j.util.Rnd;
import org.mmocore.network.ISocket;
import org.mmocore.network.MMOConnection;

public class LoginController
{
  protected static final Logger _log = Logger.getLogger(LoginController.class.getName());
  private static LoginController _instance;
  private static final int LOGIN_TIMEOUT = 60000;
  protected FastSet<L2LoginClient> _clients = new FastSet();

  protected FastMap<String, L2LoginClient> _loginServerClients = new FastMap().setShared(true);

  private Map<InetAddress, BanInfo> _bannedIps = new FastMap().setShared(true);
  private Map<InetAddress, FailedLoginAttempt> _hackProtection;
  protected ScrambledKeyPair[] _keyPairs;
  protected byte[][] _blowfishKeys;
  private static final int BLOWFISH_KEYS = 20;

  public static void load()
    throws GeneralSecurityException
  {
    if (_instance == null)
    {
      _instance = new LoginController();
    }
    else
    {
      throw new IllegalStateException("LoginController can only be loaded a single time.");
    }
  }

  public static LoginController getInstance()
  {
    return _instance;
  }

  private LoginController() throws GeneralSecurityException
  {
    _log.info("Loading LoginContoller...");

    _hackProtection = new FastMap();

    _keyPairs = new ScrambledKeyPair[10];

    KeyPairGenerator keygen = null;

    keygen = KeyPairGenerator.getInstance("RSA");
    RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(1024, RSAKeyGenParameterSpec.F4);
    keygen.initialize(spec);

    for (int i = 0; i < 10; i++)
    {
      _keyPairs[i] = new ScrambledKeyPair(keygen.generateKeyPair());
    }
    _log.info("Cached 10 KeyPairs for RSA communication");

    testCipher((RSAPrivateKey)_keyPairs[0]._pair.getPrivate());

    generateBlowFishKeys();
  }

  private void testCipher(RSAPrivateKey key)
    throws GeneralSecurityException
  {
    Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
    rsaCipher.init(2, key);
  }

  private void generateBlowFishKeys()
  {
    _blowfishKeys = new byte[20][16];

    for (int i = 0; i < 20; i++)
    {
      for (int j = 0; j < _blowfishKeys[i].length; j++)
      {
        _blowfishKeys[i][j] = (byte)(Rnd.nextInt(255) + 1);
      }
    }
    _log.info(new StringBuilder().append("Stored ").append(_blowfishKeys.length).append(" keys for Blowfish communication").toString());
  }

  public byte[] getBlowfishKey()
  {
    return _blowfishKeys[(int)(java.lang.Math.random() * 20.0D)];
  }

  public void addLoginClient(L2LoginClient client)
  {
    synchronized (_clients)
    {
      _clients.add(client);
    }
  }

  public void removeLoginClient(L2LoginClient client)
  {
    synchronized (_clients)
    {
      _clients.remove(client);
    }
  }

  public SessionKey assignSessionKeyToClient(String account, L2LoginClient client)
  {
    SessionKey key = new SessionKey(Rnd.nextInt(), Rnd.nextInt(), Rnd.nextInt(), Rnd.nextInt());
    _loginServerClients.put(account, client);
    return key;
  }

  public void removeAuthedLoginClient(String account)
  {
    _loginServerClients.remove(account);
  }

  public boolean isAccountInLoginServer(String account)
  {
    return _loginServerClients.containsKey(account);
  }

  public L2LoginClient getAuthedClient(String account)
  {
    return (L2LoginClient)_loginServerClients.get(account);
  }

  public AuthLoginResult tryAuthLogin(String account, String password, L2LoginClient client)
    throws HackingException
  {
    AuthLoginResult ret = AuthLoginResult.INVALID_PASSWORD;

    if (loginValid(account, password, client))
    {
      ret = AuthLoginResult.ALREADY_ON_GS;
      if (!isAccountInAnyGameServer(account))
      {
        ret = AuthLoginResult.ALREADY_ON_LS;

        synchronized (_loginServerClients)
        {
          if (!_loginServerClients.containsKey(account))
          {
            _loginServerClients.put(account, client);
            ret = AuthLoginResult.AUTH_SUCCESS;

            removeLoginClient(client);
          }
        }

      }

    }
    else if (client.getAccessLevel() < 0)
    {
      ret = AuthLoginResult.ACCOUNT_BANNED;
    }

    return ret;
  }

  public void addBanForAddress(String address, long expiration)
    throws UnknownHostException
  {
    InetAddress netAddress = InetAddress.getByName(address);
    _bannedIps.put(netAddress, new BanInfo(netAddress, expiration));
  }

  public void addBanForAddress(InetAddress address, long duration)
  {
    _bannedIps.put(address, new BanInfo(address, System.currentTimeMillis() + duration));
  }

  public boolean isBannedAddress(InetAddress address)
  {
    BanInfo bi = (BanInfo)_bannedIps.get(address);
    if (bi != null)
    {
      if (bi.hasExpired())
      {
        _bannedIps.remove(address);
        return false;
      }

      return true;
    }

    return false;
  }

  public Map<InetAddress, BanInfo> getBannedIps()
  {
    return _bannedIps;
  }

  public boolean removeBanForAddress(InetAddress address)
  {
    return _bannedIps.remove(address) != null;
  }

  public boolean removeBanForAddress(String address)
  {
    try
    {
      return removeBanForAddress(InetAddress.getByName(address));
    }
    catch (UnknownHostException e) {
    }
    return false;
  }

  public SessionKey getKeyForAccount(String account)
  {
    L2LoginClient client = (L2LoginClient)_loginServerClients.get(account);
    if (client != null)
    {
      return client.getSessionKey();
    }
    return null;
  }

  public int getOnlinePlayerCount(int serverId)
  {
    GameServerTable.GameServerInfo gsi = GameServerTable.getInstance().getRegisteredGameServerById(serverId);
    if ((gsi != null) && (gsi.isAuthed()))
    {
      return gsi.getCurrentPlayerCount();
    }
    return 0;
  }

  public boolean isAccountInAnyGameServer(String account)
  {
    Collection serverList = GameServerTable.getInstance().getRegisteredGameServers().values();
    for (GameServerTable.GameServerInfo gsi : serverList)
    {
      GameServerThread gst = gsi.getGameServerThread();
      if ((gst != null) && (gst.hasAccountOnGameServer(account)))
      {
        return true;
      }
    }
    return false;
  }

  public GameServerTable.GameServerInfo getAccountOnGameServer(String account)
  {
    Collection serverList = GameServerTable.getInstance().getRegisteredGameServers().values();
    for (GameServerTable.GameServerInfo gsi : serverList)
    {
      GameServerThread gst = gsi.getGameServerThread();
      if ((gst != null) && (gst.hasAccountOnGameServer(account)))
      {
        return gsi;
      }
    }
    return null;
  }

  public int getTotalOnlinePlayerCount()
  {
    int total = 0;
    Collection serverList = GameServerTable.getInstance().getRegisteredGameServers().values();
    for (GameServerTable.GameServerInfo gsi : serverList)
    {
      if (gsi.isAuthed())
      {
        total += gsi.getCurrentPlayerCount();
      }
    }
    return total;
  }

  public int getMaxAllowedOnlinePlayers(int id)
  {
    GameServerTable.GameServerInfo gsi = GameServerTable.getInstance().getRegisteredGameServerById(id);
    if (gsi != null)
    {
      return gsi.getMaxPlayers();
    }
    return 0;
  }

  public boolean isLoginPossible(L2LoginClient client, int serverId)
  {
    GameServerTable.GameServerInfo gsi = GameServerTable.getInstance().getRegisteredGameServerById(serverId);
    int access = client.getAccessLevel();
    if ((gsi != null) && (gsi.isAuthed()))
    {
      boolean loginOk = ((gsi.getCurrentPlayerCount() < gsi.getMaxPlayers()) && (gsi.getStatus() != 5)) || (access >= Config.GM_MIN);

      if ((loginOk) && (client.getLastServer() != serverId))
      {
        Connection con = null;
        PreparedStatement statement = null;
        try
        {
          con = L2DatabaseFactory.getInstance().getConnection();

          String stmt = "UPDATE accounts SET lastServer = ? WHERE login = ?";
          statement = con.prepareStatement(stmt);
          statement.setInt(1, serverId);
          statement.setString(2, client.getAccount());
          statement.executeUpdate();
          statement.close();
        }
        catch (Exception e)
        {
          _log.warning(new StringBuilder().append("Could not set lastServer: ").append(e).toString());
        }
        finally {
          try {
            con.close(); } catch (Exception e) {
          }try { statement.close(); } catch (Exception e) {
          }
        }
      }
      return loginOk;
    }
    return false;
  }

  public void setAccountAccessLevel(String account, int banLevel)
  {
    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      String stmt = "UPDATE accounts SET access_level=? WHERE login=?";
      statement = con.prepareStatement(stmt);
      statement.setInt(1, banLevel);
      statement.setString(2, account);
      statement.executeUpdate();
      statement.close();
    }
    catch (Exception e)
    {
      _log.warning(new StringBuilder().append("Could not set accessLevel: ").append(e).toString());
    }
    finally
    {
      try
      {
        statement.close();
        con.close();
      }
      catch (Exception e)
      {
      }
    }
  }

  public boolean isGM(String user)
  {
    boolean ok = false;
    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT access_level FROM accounts WHERE login=?");
      statement.setString(1, user);
      ResultSet rset = statement.executeQuery();
      if (rset.next())
      {
        int accessLevel = rset.getInt(1);
        if (accessLevel >= Config.GM_MIN)
        {
          ok = true;
        }
      }
      rset.close();
      statement.close();
    }
    catch (Exception e)
    {
      _log.warning(new StringBuilder().append("could not check gm state:").append(e).toString());
      ok = false;
    }
    finally
    {
      try
      {
        con.close();
      }
      catch (Exception e)
      {
      }
      try
      {
        statement.close();
      }
      catch (Exception e)
      {
      }
    }
    return ok;
  }

  public ScrambledKeyPair getScrambledRSAKeyPair()
  {
    return _keyPairs[Rnd.nextInt(10)];
  }

  public boolean loginValid(String user, String password, L2LoginClient client)
  {
    boolean ok = false;
    InetAddress address = client.getConnection().getSocket().getInetAddress();

    Log.add(new StringBuilder().append("'").append(user == null ? "null" : user).append("' ").append(address == null ? "null" : address.getHostAddress()).toString(), "logins_ip");

    if (address == null)
    {
      return false;
    }

    Connection con = null;
    try
    {
      MessageDigest md = MessageDigest.getInstance("SHA");
      byte[] raw = password.getBytes("UTF-8");
      byte[] hash = md.digest(raw);

      byte[] expected = null;
      int access = 0;
      int lastServer = 1;

      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("SELECT password, access_level, lastServer FROM accounts WHERE login=?");
      statement.setString(1, user);
      ResultSet rset = statement.executeQuery();
      if (rset.next())
      {
        expected = Base64.decode(rset.getString("password"));
        access = rset.getInt("access_level");
        lastServer = rset.getInt("lastServer");
        if (lastServer <= 0) lastServer = 1;
        if (Config.DEBUG) _log.fine("account exists");
      }
      rset.close();
      statement.close();
      int i;
      if (expected == null)
      {
        if (Config.AUTO_CREATE_ACCOUNTS)
        {
          if ((user.length() >= 2) && (user.length() <= 14))
          {
            statement = con.prepareStatement("INSERT INTO accounts (login,password,lastactive,access_level,lastIP) values(?,?,?,?,?)");
            statement.setString(1, user);
            statement.setString(2, Base64.encodeBytes(hash));
            statement.setLong(3, System.currentTimeMillis());
            statement.setInt(4, 0);
            statement.setString(5, address.getHostAddress());
            statement.execute();
            statement.close();

            _log.info(new StringBuilder().append("created new account for ").append(user).toString());
            i = 1;
            return i;
          }
          _log.warning(new StringBuilder().append("Invalid username creation/use attempt: ").append(user).toString());
          i = 0;
          return i;
        }
        _log.warning(new StringBuilder().append("account missing for user ").append(user).toString());
        i = 0;
        return i;
      }
      if (access < 0)
      {
        client.setAccessLevel(access);
        i = 0;
        return i;
      }
      ok = true;
      for (int i = 0; i < expected.length; i++)
      {
        if (hash[i] == expected[i])
          continue;
        ok = false;
        break;
      }

      PreparedStatement preparedstatement1 = con.prepareStatement(new StringBuilder().append(new StringBuilder().append("SELECT * FROM accounts WHERE login='")).append(user).append("';".toString()).toString());
      ResultSet resultset1 = preparedstatement1.executeQuery();
      resultset1.next();
      if ((!address.getHostAddress().equalsIgnoreCase(resultset1.getString("lastIP"))) && (resultset1.getBoolean("IPBlock")))
      {
        ok = false;
        _log.warning(new StringBuilder().append(new StringBuilder().append("ip blocker: failed attempt: account ")).append(user).append(" ip: ").append(address.getHostAddress()).toString());
      }
      if (ok)
      {
        client.setAccessLevel(access);
        client.setLastServer(lastServer);
        statement = con.prepareStatement("UPDATE accounts SET lastactive=?, lastIP=? WHERE login=?");
        statement.setLong(1, System.currentTimeMillis());
        statement.setString(2, address.getHostAddress());
        statement.setString(3, user);
        statement.execute();
        statement.close();
      }
    }
    catch (Exception e)
    {
      _log.warning(new StringBuilder().append("Could not check password:").append(e).toString());
      ok = false;
    }
    finally
    {
      try
      {
        con.close();
      }
      catch (Exception e)
      {
      }
    }

    if (!ok)
    {
      Log.add(new StringBuilder().append("'").append(user).append("' ").append(address.getHostAddress()).toString(), "logins_ip_fails");

      FailedLoginAttempt failedAttempt = (FailedLoginAttempt)_hackProtection.get(address);
      int failedCount;
      int failedCount;
      if (failedAttempt == null)
      {
        _hackProtection.put(address, new FailedLoginAttempt(address, password));
        failedCount = 1;
      }
      else
      {
        failedAttempt.increaseCounter(password);
        failedCount = failedAttempt.getCount();
      }

      if (failedCount >= Config.LOGIN_TRY_BEFORE_BAN)
      {
        _log.info(new StringBuilder().append("Banning '").append(address.getHostAddress()).append("' for ").append(Config.LOGIN_BLOCK_AFTER_BAN).append(" seconds due to ").append(failedCount).append(" invalid user/pass attempts").toString());
        addBanForAddress(address, Config.LOGIN_BLOCK_AFTER_BAN * 1000);
      }
    }
    else
    {
      _hackProtection.remove(address);
      Log.add(new StringBuilder().append("'").append(user).append("' ").append(address.getHostAddress()).toString(), "logins_ip");
    }

    return ok;
  }

  public boolean loginBanned(String user)
  {
    boolean ok = false;

    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("SELECT access_level FROM accounts WHERE login=?");
      statement.setString(1, user);
      ResultSet rset = statement.executeQuery();
      if (rset.next())
      {
        int accessLevel = rset.getInt(1);
        if (accessLevel < 0) ok = true;
      }
      rset.close();
      statement.close();
    }
    catch (Exception e)
    {
      _log.warning(new StringBuilder().append("could not check ban state:").append(e).toString());
      ok = false;
    }
    finally
    {
      try
      {
        con.close();
      }
      catch (Exception e)
      {
      }
    }

    return ok;
  }

  class PurgeThread extends Thread
  {
    PurgeThread()
    {
    }

    public void run()
    {
      while (true)
      {
        synchronized (_clients)
        {
          FastCollection.Record e = _clients.head(); FastCollection.Record end = _clients.tail(); if ((e = e.getNext()) == end)
            continue;
          L2LoginClient client = (L2LoginClient)_clients.valueOf(e);
          if (client.getConnectionStartTime() + 60000L < System.currentTimeMillis())
            continue;
          client.close(LoginFail.LoginFailReason.REASON_ACCESS_FAILED);

          continue;
        }
        FastMap.Entry e;
        synchronized (_loginServerClients)
        {
          e = _loginServerClients.head(); for (FastMap.Entry end = _loginServerClients.tail(); (e = e.getNext()) != end; )
          {
            L2LoginClient client = (L2LoginClient)e.getValue();
            if (client.getConnectionStartTime() + 60000L >= System.currentTimeMillis())
            {
              client.close(LoginFail.LoginFailReason.REASON_ACCESS_FAILED);
            }
          }
        }

        try
        {
          Thread.sleep(120000L);
        }
        catch (InterruptedException e)
        {
          e.printStackTrace();
        }
      }
    }
  }

  class BanInfo
  {
    private InetAddress _ipAddress;
    private long _expiration;

    public BanInfo(InetAddress ipAddress, long expiration)
    {
      _ipAddress = ipAddress;
      _expiration = expiration;
    }

    public InetAddress getAddress()
    {
      return _ipAddress;
    }

    public boolean hasExpired()
    {
      return (System.currentTimeMillis() > _expiration) && (_expiration > 0L);
    }
  }

  class FailedLoginAttempt
  {
    private int _count;
    private long _lastAttempTime;
    private String _lastPassword;

    public FailedLoginAttempt(InetAddress address, String lastPassword)
    {
      _count = 1;
      _lastAttempTime = System.currentTimeMillis();
      _lastPassword = lastPassword;
    }

    public void increaseCounter(String password)
    {
      if (!_lastPassword.equals(password))
      {
        if (System.currentTimeMillis() - _lastAttempTime < 300000L)
        {
          _count += 1;
        }
        else
        {
          _count = 1;
        }

        _lastPassword = password;
        _lastAttempTime = System.currentTimeMillis();
      }
      else
      {
        _lastAttempTime = System.currentTimeMillis();
      }
    }

    public int getCount()
    {
      return _count;
    }
  }

  public static enum AuthLoginResult
  {
    INVALID_PASSWORD, ACCOUNT_BANNED, ALREADY_ON_LS, ALREADY_ON_GS, AUTH_SUCCESS;
  }
}