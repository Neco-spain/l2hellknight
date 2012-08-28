package net.sf.l2j.loginserver;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javolution.util.FastSet;
import net.sf.l2j.Config;
import net.sf.l2j.loginserver.crypt.NewCrypt;
import net.sf.l2j.loginserver.gameserverpackets.BlowFishKey;
import net.sf.l2j.loginserver.gameserverpackets.ChangeAccessLevel;
import net.sf.l2j.loginserver.gameserverpackets.GameServerAuth;
import net.sf.l2j.loginserver.gameserverpackets.PlayerAuthRequest;
import net.sf.l2j.loginserver.gameserverpackets.PlayerInGame;
import net.sf.l2j.loginserver.gameserverpackets.PlayerLogout;
import net.sf.l2j.loginserver.gameserverpackets.ServerStatus;
import net.sf.l2j.loginserver.loginserverpackets.AuthResponse;
import net.sf.l2j.loginserver.loginserverpackets.InitLS;
import net.sf.l2j.loginserver.loginserverpackets.KickPlayer;
import net.sf.l2j.loginserver.loginserverpackets.LoginServerFail;
import net.sf.l2j.loginserver.loginserverpackets.PlayerAuthResponse;
import net.sf.l2j.loginserver.serverpackets.ServerBasePacket;
import net.sf.l2j.status.Status;
import net.sf.l2j.util.Util;

public class GameServerThread extends Thread
{
  protected static final Logger _log = Logger.getLogger(GameServerThread.class.getName());
  private Socket _connection;
  private InputStream _in;
  private OutputStream _out;
  private RSAPublicKey _publicKey;
  private RSAPrivateKey _privateKey;
  private NewCrypt _blowfish;
  private byte[] _blowfishKey;
  private String _connectionIp;
  private GameServerTable.GameServerInfo _gsi;
  private Set<String> _accountsOnGameServer = new FastSet();
  private String _connectionIPAddress;

  public void run()
  {
    _connectionIPAddress = _connection.getInetAddress().getHostAddress();
    if (isBannedGameserverIP(_connectionIPAddress))
    {
      _log.info("GameServerRegistration: IP Address " + _connectionIPAddress + " is on Banned IP list.");
      forceClose(1);

      return;
    }

    InitLS startPacket = new InitLS(_publicKey.getModulus().toByteArray());
    try
    {
      sendPacket(startPacket);

      int lengthHi = 0;
      int lengthLo = 0;
      int length = 0;
      boolean checksumOk = false;
      while (true)
      {
        lengthLo = _in.read();
        lengthHi = _in.read();
        length = lengthHi * 256 + lengthLo;

        if ((lengthHi < 0) || (_connection.isClosed()))
        {
          _log.finer("LoginServerThread: Login terminated the connection.");
          break;
        }

        byte[] data = new byte[length - 2];

        int receivedBytes = 0;
        int newBytes = 0;
        while ((newBytes != -1) && (receivedBytes < length - 2))
        {
          newBytes = _in.read(data, 0, length - 2);
          receivedBytes += newBytes;
        }

        if (receivedBytes != length - 2)
        {
          _log.warning("Incomplete Packet is sent to the server, closing connection.(LS)");
          break;
        }

        data = _blowfish.decrypt(data);
        checksumOk = NewCrypt.verifyChecksum(data);
        if (!checksumOk) {
          _log.warning("Incorrect packet checksum, closing connection (LS)");
          return;
        }
        if (Config.DEBUG)
        {
          _log.warning("[C]\n" + Util.printData(data));
        }

        int packetType = data[0] & 0xFF;
        switch (packetType)
        {
        case 0:
          onReceiveBlowfishKey(data);
          break;
        case 1:
          onGameServerAuth(data);
          break;
        case 2:
          onReceivePlayerInGame(data);
          break;
        case 3:
          onReceivePlayerLogOut(data);
          break;
        case 4:
          onReceiveChangeAccessLevel(data);
          break;
        case 5:
          onReceivePlayerAuthRequest(data);
          break;
        case 6:
          onReceiveServerStatus(data);
          break;
        default:
          _log.warning("Unknown Opcode (" + Integer.toHexString(packetType).toUpperCase() + ") from GameServer, closing connection.");
          forceClose(6);
        }
      }

    }
    catch (IOException e)
    {
      String serverName = "(" + _connectionIPAddress + ")";
      String msg = "GameServer " + serverName + ": Connection lost: " + e.getMessage();
      _log.info(msg);
      broadcastToTelnet(msg);
    }
    finally
    {
      if (isAuthed())
      {
        _gsi.setDown();
        _log.info("Server [" + getServerId() + "] " + GameServerTable.getInstance().getServerNameById(getServerId()) + " is now set as disconnected");
      }
      L2LoginServer.getInstance().getGameServerListener().removeGameServer(this);
      L2LoginServer.getInstance().getGameServerListener().removeFloodProtection(_connectionIp);
    }
  }

  private void onReceiveBlowfishKey(byte[] data)
  {
    BlowFishKey bfk = new BlowFishKey(data, _privateKey);
    _blowfishKey = bfk.getKey();
    _blowfish = new NewCrypt(_blowfishKey);
    if (Config.DEBUG)
    {
      _log.info("New BlowFish key received, Blowfih Engine initialized:");
    }
  }

  private void onGameServerAuth(byte[] data)
    throws IOException
  {
    GameServerAuth gsa = new GameServerAuth(data);
    if (Config.DEBUG)
    {
      _log.info("Auth request received");
    }
    handleRegProcess(gsa);
    if (isAuthed())
    {
      AuthResponse ar = new AuthResponse(getGameServerInfo().getId());
      sendPacket(ar);
      if (Config.DEBUG)
      {
        _log.info("Authed: id: " + getGameServerInfo().getId());
      }
      broadcastToTelnet("GameServer [" + getServerId() + "] " + GameServerTable.getInstance().getServerNameById(getServerId()) + " is connected");
    }
  }

  private void onReceivePlayerInGame(byte[] data)
  {
    if (isAuthed())
    {
      PlayerInGame pig = new PlayerInGame(data);
      List newAccounts = pig.getAccounts();
      for (String account : newAccounts)
      {
        _accountsOnGameServer.add(account);
        if (Config.DEBUG)
        {
          _log.info("Account " + account + " logged in GameServer: [" + getServerId() + "] " + GameServerTable.getInstance().getServerNameById(getServerId()));
        }

        broadcastToTelnet("Account " + account + " logged in GameServer " + getServerId());
      }

    }
    else
    {
      forceClose(6);
    }
  }

  private void onReceivePlayerLogOut(byte[] data)
  {
    if (isAuthed())
    {
      PlayerLogout plo = new PlayerLogout(data);
      _accountsOnGameServer.remove(plo.getAccount());
      if (Config.DEBUG)
      {
        _log.info("Player " + plo.getAccount() + " logged out from gameserver [" + getServerId() + "] " + GameServerTable.getInstance().getServerNameById(getServerId()));
      }

      broadcastToTelnet("Player " + plo.getAccount() + " disconnected from GameServer " + getServerId());
    }
    else
    {
      forceClose(6);
    }
  }

  private void onReceiveChangeAccessLevel(byte[] data)
  {
    if (isAuthed())
    {
      ChangeAccessLevel cal = new ChangeAccessLevel(data);
      LoginController.getInstance().setAccountAccessLevel(cal.getAccount(), cal.getLevel());
      _log.info("Changed " + cal.getAccount() + " access level to " + cal.getLevel());
    }
    else
    {
      forceClose(6);
    }
  }

  private void onReceivePlayerAuthRequest(byte[] data) throws IOException
  {
    if (isAuthed())
    {
      PlayerAuthRequest par = new PlayerAuthRequest(data);

      if (Config.DEBUG)
      {
        _log.info("auth request received for Player " + par.getAccount());
      }
      SessionKey key = LoginController.getInstance().getKeyForAccount(par.getAccount());
      PlayerAuthResponse authResponse;
      PlayerAuthResponse authResponse;
      if ((key != null) && (key.equals(par.getKey())))
      {
        if (Config.DEBUG)
        {
          _log.info("auth request: OK");
        }
        LoginController.getInstance().removeAuthedLoginClient(par.getAccount());
        authResponse = new PlayerAuthResponse(par.getAccount(), true);
      }
      else
      {
        if (Config.DEBUG)
        {
          _log.info("auth request: NO");
          _log.info("session key from self: " + key);
          _log.info("session key sent: " + par.getKey());
        }
        authResponse = new PlayerAuthResponse(par.getAccount(), false);
      }
      sendPacket(authResponse);
    }
    else
    {
      forceClose(6);
    }
  }

  private void onReceiveServerStatus(byte[] data)
  {
    ServerStatus ss;
    if (isAuthed())
    {
      if (Config.DEBUG)
      {
        _log.info("ServerStatus received");
      }

      ss = new ServerStatus(data, getServerId());
    }
    else
    {
      forceClose(6);
    }
  }

  private void handleRegProcess(GameServerAuth gameServerAuth)
  {
    GameServerTable gameServerTable = GameServerTable.getInstance();

    int id = gameServerAuth.getDesiredID();
    byte[] hexId = gameServerAuth.getHexID();

    GameServerTable.GameServerInfo gsi = gameServerTable.getRegisteredGameServerById(id);

    if (gsi != null)
    {
      if (Arrays.equals(gsi.getHexId(), hexId))
      {
        synchronized (gsi)
        {
          if (gsi.isAuthed())
          {
            forceClose(7);
          }
          else
          {
            attachGameServerInfo(gsi, gameServerAuth);
          }

        }

      }
      else if ((Config.ACCEPT_NEW_GAMESERVER) && (gameServerAuth.acceptAlternateID()))
      {
        gsi = new GameServerTable.GameServerInfo(id, hexId, this);
        if (gameServerTable.registerWithFirstAvaliableId(gsi))
        {
          attachGameServerInfo(gsi, gameServerAuth);
          gameServerTable.registerServerOnDB(gsi);
        }
        else
        {
          forceClose(5);
        }

      }
      else
      {
        forceClose(3);
      }

    }
    else if (Config.ACCEPT_NEW_GAMESERVER)
    {
      gsi = new GameServerTable.GameServerInfo(id, hexId, this);
      if (gameServerTable.register(id, gsi))
      {
        attachGameServerInfo(gsi, gameServerAuth);
        gameServerTable.registerServerOnDB(gsi);
      }
      else
      {
        forceClose(4);
      }
    }
    else
    {
      forceClose(3);
    }
  }

  public boolean hasAccountOnGameServer(String account)
  {
    return _accountsOnGameServer.contains(account);
  }

  public int getPlayerCount()
  {
    return _accountsOnGameServer.size();
  }

  private void attachGameServerInfo(GameServerTable.GameServerInfo gsi, GameServerAuth gameServerAuth)
  {
    setGameServerInfo(gsi);
    gsi.setGameServerThread(this);
    gsi.setPort(gameServerAuth.getPort());
    setGameHosts(gameServerAuth.getExternalHost(), gameServerAuth.getInternalHost());
    gsi.setMaxPlayers(gameServerAuth.getMaxPlayers());
    gsi.setAuthed(true);
  }

  private void forceClose(int reason)
  {
    LoginServerFail lsf = new LoginServerFail(reason);
    try
    {
      sendPacket(lsf);
    }
    catch (IOException e)
    {
      _log.finer("GameServerThread: Failed kicking banned server. Reason: " + e.getMessage());
    }

    try
    {
      _connection.close();
    }
    catch (IOException e)
    {
      _log.finer("GameServerThread: Failed disconnecting banned server, server already disconnected.");
    }
  }

  public static boolean isBannedGameserverIP(String ipAddress)
  {
    return false;
  }

  public GameServerThread(Socket con)
  {
    _connection = con;
    _connectionIp = con.getInetAddress().getHostAddress();
    try
    {
      _in = _connection.getInputStream();
      _out = new BufferedOutputStream(_connection.getOutputStream());
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    KeyPair pair = GameServerTable.getInstance().getKeyPair();
    _privateKey = ((RSAPrivateKey)pair.getPrivate());
    _publicKey = ((RSAPublicKey)pair.getPublic());
    _blowfish = new NewCrypt("");
    start();
  }

  private void sendPacket(ServerBasePacket sl)
    throws IOException
  {
    byte[] data = sl.getContent();
    NewCrypt.appendChecksum(data);
    if (Config.DEBUG)
    {
      _log.finest("[S] " + sl.getClass().getSimpleName() + ":\n" + Util.printData(data));
    }
    data = _blowfish.crypt(data);

    int len = data.length + 2;
    synchronized (_out)
    {
      _out.write(len & 0xFF);
      _out.write(len >> 8 & 0xFF);
      _out.write(data);
      _out.flush();
    }
  }

  private void broadcastToTelnet(String msg)
  {
    if (L2LoginServer.getInstance().getStatusServer() != null)
    {
      L2LoginServer.getInstance().getStatusServer().sendMessageToTelnets(msg);
    }
  }

  public void kickPlayer(String account)
  {
    KickPlayer kp = new KickPlayer(account);
    try
    {
      sendPacket(kp);
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  public void setGameHosts(String gameExternalHost, String gameInternalHost)
  {
    String oldInternal = _gsi.getInternalHost();
    String oldExternal = _gsi.getExternalHost();

    _gsi.setExternalHost(gameExternalHost);
    _gsi.setInternalIp(gameInternalHost);

    if (!gameExternalHost.equals("*"))
    {
      try
      {
        _gsi.setExternalIp(InetAddress.getByName(gameExternalHost).getHostAddress());
      }
      catch (UnknownHostException e)
      {
        _log.warning("Couldn't resolve hostname \"" + gameExternalHost + "\"");
      }
    }
    else
    {
      _gsi.setExternalIp(_connectionIp);
    }
    if (!gameInternalHost.equals("*"))
    {
      try
      {
        _gsi.setInternalIp(InetAddress.getByName(gameInternalHost).getHostAddress());
      }
      catch (UnknownHostException e)
      {
        _log.warning("Couldn't resolve hostname \"" + gameInternalHost + "\"");
      }
    }
    else
    {
      _gsi.setInternalIp(_connectionIp);
    }

    _log.info("Updated Gameserver [" + getServerId() + "] " + GameServerTable.getInstance().getServerNameById(getServerId()) + " IP's:");
    if ((oldInternal == null) || (!oldInternal.equalsIgnoreCase(gameInternalHost)))
      _log.info("InternalIP: " + gameInternalHost);
    if ((oldExternal == null) || (!oldExternal.equalsIgnoreCase(gameExternalHost)))
      _log.info("ExternalIP: " + gameExternalHost);
  }

  public boolean isAuthed()
  {
    if (getGameServerInfo() == null)
      return false;
    return getGameServerInfo().isAuthed();
  }

  public void setGameServerInfo(GameServerTable.GameServerInfo gsi)
  {
    _gsi = gsi;
  }

  public GameServerTable.GameServerInfo getGameServerInfo()
  {
    return _gsi;
  }

  public String getConnectionIpAddress()
  {
    return _connectionIPAddress;
  }

  private int getServerId()
  {
    if (getGameServerInfo() != null)
    {
      return getGameServerInfo().getId();
    }
    return -1;
  }
}