package net.sf.l2j.gameserver;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.math.BigInteger;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.lib.Log;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.L2GameClient.GameClientState;
import net.sf.l2j.gameserver.network.gameserverpackets.AuthRequest;
import net.sf.l2j.gameserver.network.gameserverpackets.BlowFishKey;
import net.sf.l2j.gameserver.network.gameserverpackets.ChangeAccessLevel;
import net.sf.l2j.gameserver.network.gameserverpackets.GameServerBasePacket;
import net.sf.l2j.gameserver.network.gameserverpackets.PlayerAuthRequest;
import net.sf.l2j.gameserver.network.gameserverpackets.PlayerInGame;
import net.sf.l2j.gameserver.network.gameserverpackets.PlayerLogout;
import net.sf.l2j.gameserver.network.gameserverpackets.ServerStatus;
import net.sf.l2j.gameserver.network.gameserverpackets.SetHwid;
import net.sf.l2j.gameserver.network.gameserverpackets.SetLastHwid;
import net.sf.l2j.gameserver.network.gameserverpackets.SetNewEmail;
import net.sf.l2j.gameserver.network.gameserverpackets.SetNewPassword;
import net.sf.l2j.gameserver.network.gameserverpackets.WebStatAccountsRequest;
import net.sf.l2j.gameserver.network.loginserverpackets.AcceptPlayer;
import net.sf.l2j.gameserver.network.loginserverpackets.AuthResponse;
import net.sf.l2j.gameserver.network.loginserverpackets.InitLS;
import net.sf.l2j.gameserver.network.loginserverpackets.KickPlayer;
import net.sf.l2j.gameserver.network.loginserverpackets.LoginServerFail;
import net.sf.l2j.gameserver.network.loginserverpackets.PlayerAuthResponse;
import net.sf.l2j.gameserver.network.loginserverpackets.WebStatAccounts;
import net.sf.l2j.gameserver.network.serverpackets.AuthLoginFail;
import net.sf.l2j.gameserver.network.serverpackets.CharSelectInfo;
import net.sf.l2j.util.Rnd;
import net.sf.l2j.util.TimeLogger;
import net.sf.l2j.util.crypt.NewCrypt;
import net.sf.l2j.util.log.AbstractLogger;
import org.mmocore.network.MMOConnection;
import org.mmocore.network.SelectorThread;

public class LoginServerThread extends Thread
{
  protected static final Logger _log = AbstractLogger.getLogger(LoginServerThread.class.getName());
  private static LoginServerThread _instance;
  private static final int REVISION = 258;
  private RSAPublicKey _publicKey;
  private String _hostname;
  private int _port;
  private int _gamePort;
  private Socket _loginSocket;
  private InputStream _in;
  private OutputStream _out;
  private NewCrypt _blowfish;
  private byte[] _blowfishKey;
  private byte[] _hexID;
  private boolean _acceptAlternate;
  private int _requestID;
  private int _serverID;
  private boolean _reserveHost;
  private int _maxPlayer;
  private Map<String, WaitingClient> _waitingClients;
  private Map<String, L2GameClient> _accountsInGameServer;
  private int _status;
  private String _serverName;
  private String _gameExternalHost;
  private String _gameInternalHost;
  private int _totalAccs = 0;

  public LoginServerThread()
  {
    super("LoginServerThread");
    _port = Config.GAME_SERVER_LOGIN_PORT;
    _gamePort = Config.PORT_GAME;
    _hostname = Config.GAME_SERVER_LOGIN_HOST;
    _hexID = Config.HEX_ID;
    if (_hexID == null) {
      _requestID = Config.REQUEST_ID;
      _hexID = generateHex(16);
    } else {
      _requestID = Config.SERVER_ID;
    }

    _acceptAlternate = Config.ACCEPT_ALTERNATE_ID;
    _reserveHost = false;
    _gameExternalHost = Config.EXTERNAL_HOSTNAME;
    _gameInternalHost = Config.INTERNAL_HOSTNAME;
    _waitingClients = new ConcurrentHashMap();
    _accountsInGameServer = new ConcurrentHashMap();
    _maxPlayer = Config.MAXIMUM_ONLINE_USERS;
  }

  public static LoginServerThread getInstance() {
    return _instance;
  }

  public static void init() {
    _instance = new LoginServerThread();
  }

  public void run()
  {
    while (true) {
      int lengthHi = 0;
      int lengthLo = 0;
      int length = 0;
      boolean checksumOk = false;
      try
      {
        _log.info(TimeLogger.getLogTime() + "Connecting to login on " + _hostname + ":" + _port);
        _loginSocket = new Socket(_hostname, _port);
        _in = _loginSocket.getInputStream();
        _out = new BufferedOutputStream(_loginSocket.getOutputStream());

        _blowfishKey = generateHex(40);
        _blowfish = new NewCrypt("");
        while (true) {
          lengthLo = _in.read();
          lengthHi = _in.read();
          length = lengthHi * 256 + lengthLo;

          if (lengthHi < 0) {
            _log.finer("LoginServerThread: Login terminated the connection.");
            break;
          }

          byte[] incoming = new byte[length];
          incoming[0] = (byte)lengthLo;
          incoming[1] = (byte)lengthHi;

          int receivedBytes = 0;
          int newBytes = 0;
          while ((newBytes != -1) && (receivedBytes < length - 2)) {
            newBytes = _in.read(incoming, 2, length - 2);
            receivedBytes += newBytes;
          }

          if (receivedBytes != length - 2) {
            _log.warning("Incomplete Packet is sent to the server, closing connection.(LS)");
            break;
          }

          byte[] decrypt = new byte[length - 2];
          System.arraycopy(incoming, 2, decrypt, 0, decrypt.length);

          decrypt = _blowfish.decrypt(decrypt);
          checksumOk = NewCrypt.verifyChecksum(decrypt);

          if (!checksumOk) {
            _log.warning("Incorrect packet checksum, ignoring packet (LS)");
            break;
          }

          int packetType = decrypt[0] & 0xFF;

          switch (packetType) {
          case 0:
            InitLS init = new InitLS(decrypt);

            if (init.getRevision() != 258)
            {
              _log.warning("/!\\ Revision mismatch between LS and GS /!\\");
            }
            else {
              try {
                KeyFactory kfac = KeyFactory.getInstance("RSA");
                BigInteger modulus = new BigInteger(init.getRSAKey());
                RSAPublicKeySpec kspec1 = new RSAPublicKeySpec(modulus, RSAKeyGenParameterSpec.F4);
                _publicKey = ((RSAPublicKey)kfac.generatePublic(kspec1));
              }
              catch (GeneralSecurityException e) {
                _log.warning("Troubles while init the public key send by login");
                break;
              }

              sendPacket(new BlowFishKey(_blowfishKey, _publicKey));

              _blowfish = new NewCrypt(_blowfishKey);

              sendPacket(new AuthRequest(_requestID, _acceptAlternate, _hexID, _gameExternalHost, _gameInternalHost, _gamePort, _reserveHost, _maxPlayer, Config.SERVER_SERIAL_KEY));
            }
            break;
          case 1:
            LoginServerFail lsf = new LoginServerFail(decrypt);
            _log.info(TimeLogger.getLogTime() + "Damn! Registeration Failed: " + lsf.getReasonString());

            break;
          case 3:
            PlayerAuthResponse par = new PlayerAuthResponse(decrypt);
            String account = par.getAccount();
            WaitingClient wcToRemove = (WaitingClient)_waitingClients.get(account);
            if (wcToRemove == null) break;
            if ((par.isAuthed()) && (wcToRemove.gameClient.acceptHWID(par.getHWID())))
            {
              sendPacket(new PlayerInGame(par.getAccount()));
              wcToRemove.gameClient.setState(L2GameClient.GameClientState.AUTHED);
              wcToRemove.gameClient.setSessionId(wcToRemove.session);
              CharSelectInfo cl = new CharSelectInfo(wcToRemove.account, wcToRemove.gameClient.getSessionId().playOkID1);
              wcToRemove.gameClient.getConnection().sendPacket(cl);
              wcToRemove.gameClient.setCharSelection(cl.getCharInfo());

              wcToRemove.gameClient.setHasEmail(par.hasEmail());
            } else {
              _log.warning(TimeLogger.getLogTime() + "session key is not correct. closing connection; account: " + account);
              wcToRemove.gameClient.getConnection().sendPacket(new AuthLoginFail(1));
              wcToRemove.gameClient.closeNow();
              Log.add(TimeLogger.getTime() + "# " + account + " " + par.getHWID(), "wrong_hwid");
            }
            _waitingClients.remove(account); break;
          case 4:
            KickPlayer kp = new KickPlayer(decrypt);
            doKickPlayer(kp.getAccount());
            break;
          case 6:
            AuthResponse aresp = new AuthResponse(decrypt);
            _serverID = aresp.getServerId();
            _serverName = aresp.getServerName();
            Config.saveHexid(_serverID, hexToString(_hexID));

            _log.info(TimeLogger.getLogTime() + "#Server " + _serverName + " ready on " + _gameExternalHost + ":" + _gamePort);
            ServerStatus st = new ServerStatus();
            if (Config.SERVER_LIST_BRACKET)
              st.addAttribute(3, 1);
            else {
              st.addAttribute(3, 0);
            }

            if (Config.SERVER_LIST_CLOCK)
              st.addAttribute(2, 1);
            else {
              st.addAttribute(2, 0);
            }

            if (Config.SERVER_LIST_TESTSERVER)
              st.addAttribute(5, 1);
            else {
              st.addAttribute(5, 0);
            }

            if (Config.SERVER_GMONLY)
              st.addAttribute(1, 5);
            else {
              st.addAttribute(1, 0);
            }

            sendPacket(st);
            if (L2World.getInstance().getAllPlayersCount() <= 0) break;
            FastList pl = new FastList();
            for (L2PcInstance p : L2World.getInstance().getAllPlayers()) {
              if ((p == null) || 
                (p.isFantome()) || 
                (p.getAccountName().equalsIgnoreCase("N/A")))
              {
                continue;
              }
              pl.add(p.getAccountName());
            }
            sendPacket(new PlayerInGame(pl));
            pl.clear();
            pl = null;
            break;
          case 7:
            AcceptPlayer ap = new AcceptPlayer(decrypt);
            acceptPlayer(ap.getIp());
            break;
          case 190:
            WebStatAccounts wsa = new WebStatAccounts(decrypt);
            _totalAccs = wsa.getCount();
          }
        }
      }
      catch (UnknownHostException e) {
      }
      catch (IOException e) {
        _log.info(TimeLogger.getLogTime() + "Deconnected from Login, Trying to reconnect:");
        _log.info(e.toString());
      } finally {
        try {
          _loginSocket.close();
        }
        catch (Exception e) {
        }
      }
      try {
        Thread.sleep(5000L);
      }
      catch (InterruptedException e) {
      }
    }
  }

  private void acceptPlayer(String ip) {
    SelectorThread.getInstance().accept(ip);
  }

  public void addWaitingClientAndSendRequest(String acc, L2GameClient client, SessionKey key) {
    _waitingClients.put(acc, new WaitingClient(acc, client, key));
    try
    {
      sendPacket(new PlayerAuthRequest(acc, key));
    } catch (IOException e) {
      _log.warning("Error while sending player auth request");
    }
  }

  public void sendLogout(String account)
  {
    try
    {
      sendPacket(new PlayerLogout(account));
    } catch (IOException e) {
      _log.warning(TimeLogger.getLogTime() + "Error while sending logout packet to login. " + e);
      _log.warning(TimeLogger.getLogTime() + "Deconnected from Login, Trying to reconnect:");
      reConnect();
    }
  }

  public void reConnect() {
    try {
      if (_loginSocket != null)
        _loginSocket.close();
    }
    catch (Exception ignored) {
    }
    try {
      _instance.interrupt();
    }
    catch (Exception ignored) {
    }
    _instance = new LoginServerThread();
    _instance.start();
  }

  public void addGameServerLogin(String account, L2GameClient client) {
    _accountsInGameServer.put(account, client);
  }

  public void sendAccessLevel(String account, int level)
  {
    try {
      sendPacket(new ChangeAccessLevel(account, level));
    }
    catch (IOException e)
    {
    }
  }

  public void setLastHwid(String account, String hwid)
  {
    try {
      sendPacket(new SetLastHwid(account, hwid));
    }
    catch (IOException e)
    {
    }
  }

  public void updateWebStatAccounts()
  {
    try {
      sendPacket(new WebStatAccountsRequest());
    }
    catch (IOException e)
    {
    }
  }

  public void setHwid(String account, String hwid)
  {
    try {
      sendPacket(new SetHwid(account, hwid));
    }
    catch (IOException e)
    {
    }
  }

  public void setNewPassword(String account, String pwd)
  {
    try {
      sendPacket(new SetNewPassword(account, pwd));
    }
    catch (IOException e)
    {
    }
  }

  public void setNewEmail(String account, String email)
  {
    try {
      sendPacket(new SetNewEmail(account, email));
    }
    catch (IOException e)
    {
    }
  }

  private String hexToString(byte[] hex) {
    return new BigInteger(hex).toString(16);
  }

  public void doKickPlayer(String account) {
    if (_accountsInGameServer.get(account) != null)
      ((L2GameClient)_accountsInGameServer.get(account)).kick(account);
  }

  public static byte[] generateHex(int size)
  {
    byte[] array = new byte[size];
    Rnd.nextBytes(array);

    return array;
  }

  public static void knockKnock(String ip) {
    System.out.println("###" + ip);
  }

  private void sendPacket(GameServerBasePacket sl)
    throws IOException
  {
    byte[] data = sl.getContent();
    NewCrypt.appendChecksum(data);

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

  public void setMaxPlayer(int maxPlayer)
  {
    sendServerStatus(4, maxPlayer);
    _maxPlayer = maxPlayer;
  }

  public int getMaxPlayer()
  {
    return _maxPlayer;
  }

  public void sendServerStatus(int id, int value)
  {
    ServerStatus ss = new ServerStatus();
    ss.addAttribute(id, value);
    try {
      sendPacket(ss);
    }
    catch (IOException e)
    {
    }
  }

  public String getStatusString()
  {
    return ServerStatus.STATUS_STRING[_status];
  }

  public boolean isClockShown()
  {
    return Config.SERVER_LIST_CLOCK;
  }

  public boolean isBracketShown()
  {
    return Config.SERVER_LIST_BRACKET;
  }

  public String getServerName()
  {
    return _serverName;
  }

  public void setServerStatus(int status) {
    switch (status) {
    case 0:
      sendServerStatus(1, 0);
      _status = status;
      break;
    case 4:
      sendServerStatus(1, 4);
      _status = status;
      break;
    case 3:
      sendServerStatus(1, 3);
      _status = status;
      break;
    case 5:
      sendServerStatus(1, 5);
      _status = status;
      break;
    case 1:
      sendServerStatus(1, 1);
      _status = status;
      break;
    case 2:
      sendServerStatus(1, 2);
      _status = status;
      break;
    default:
      throw new IllegalArgumentException("Status does not exists:" + status);
    }
  }

  public int getTotalAccs()
  {
    return _totalAccs;
  }

  private static class WaitingClient
  {
    public int timestamp;
    public String account;
    public L2GameClient gameClient;
    public LoginServerThread.SessionKey session;

    public WaitingClient(String acc, L2GameClient client, LoginServerThread.SessionKey key)
    {
      account = acc;
      timestamp = GameTimeController.getGameTicks();
      gameClient = client;
      session = key;
    }
  }

  public static class SessionKey
  {
    public int playOkID1;
    public int playOkID2;
    public int loginOkID1;
    public int loginOkID2;
    public int clientKey;

    public SessionKey(int loginOK1, int loginOK2, int playOK1, int playOK2)
    {
      playOkID1 = playOK1;
      playOkID2 = playOK2;
      loginOkID1 = loginOK1;
      loginOkID2 = loginOK2;
    }

    public String toString()
    {
      return "PlayOk: " + playOkID1 + " " + playOkID2 + " LoginOk:" + loginOkID1 + " " + loginOkID2;
    }
  }
}