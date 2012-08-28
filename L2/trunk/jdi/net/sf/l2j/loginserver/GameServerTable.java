package net.sf.l2j.loginserver;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.RSAKeyGenParameterSpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import javolution.io.UTF8StreamReader;
import javolution.text.CharArray;
import javolution.util.FastMap;
import javolution.xml.stream.XMLStreamException;
import javolution.xml.stream.XMLStreamReaderImpl;
import net.sf.l2j.L2DatabaseFactory;

public class GameServerTable
{
  private static Logger _log = Logger.getLogger(GameServerTable.class.getName());
  private static GameServerTable _instance;
  private static Map<Integer, String> _serverNames = new FastMap();
  private Map<Integer, GameServerInfo> _gameServerTable = new FastMap().setShared(true);
  private static final int KEYS_SIZE = 10;
  private KeyPair[] _keyPairs;

  public static void load()
    throws SQLException, GeneralSecurityException
  {
    if (_instance == null)
    {
      _instance = new GameServerTable();
    }
    else
    {
      throw new IllegalStateException("Load can only be invoked a single time.");
    }
  }

  public static GameServerTable getInstance()
  {
    return _instance;
  }

  public GameServerTable() throws SQLException, NoSuchAlgorithmException, InvalidAlgorithmParameterException
  {
    loadServerNames();
    _log.info("Loaded " + _serverNames.size() + " server names");

    loadRegisteredGameServers();
    _log.info("Loaded " + _gameServerTable.size() + " registered Game Servers");

    loadRSAKeys();
    _log.info("Cached " + _keyPairs.length + " RSA keys for Game Server communication.");
  }

  private void loadRSAKeys() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException
  {
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
    RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(512, RSAKeyGenParameterSpec.F4);
    keyGen.initialize(spec);

    _keyPairs = new KeyPair[10];
    for (int i = 0; i < 10; i++)
    {
      _keyPairs[i] = keyGen.genKeyPair();
    }
  }

  private void loadServerNames()
  {
    InputStream in = null;
    XMLStreamReaderImpl xpp = new XMLStreamReaderImpl();
    try
    {
      in = new FileInputStream("./data/xml/servername.xml");
      xpp.setInput(new UTF8StreamReader().setInput(in));
      for (int e = xpp.getEventType(); e != 8; e = xpp.next())
      {
        if (e != 1)
          continue;
        if (!xpp.getLocalName().toString().equals("server"))
          continue;
        Integer id = new Integer(xpp.getAttributeValue(null, "id").toString());
        String name = xpp.getAttributeValue(null, "name").toString();
        _serverNames.put(id, name);
      }

    }
    catch (FileNotFoundException e)
    {
      _log.warning("servername.xml could not be loaded: file not found");
    }
    catch (XMLStreamException e)
    {
      xppe.printStackTrace();
    }
    finally {
      try {
        in.close(); xpp.close(); } catch (Exception e) {
      }
    }
  }

  private void loadRegisteredGameServers() throws SQLException {
    Connection con = null;
    PreparedStatement statement = null;

    con = L2DatabaseFactory.getInstance().getConnection();
    statement = con.prepareStatement("SELECT * FROM gameservers");
    ResultSet rset = statement.executeQuery();

    while (rset.next())
    {
      int id = rset.getInt("server_id");
      GameServerInfo gsi = new GameServerInfo(id, stringToHex(rset.getString("hexid")));
      _gameServerTable.put(Integer.valueOf(id), gsi);
    }
    rset.close();
    statement.close();
    con.close();
  }

  public Map<Integer, GameServerInfo> getRegisteredGameServers()
  {
    return _gameServerTable;
  }

  public GameServerInfo getRegisteredGameServerById(int id)
  {
    return (GameServerInfo)_gameServerTable.get(Integer.valueOf(id));
  }

  public boolean hasRegisteredGameServerOnId(int id)
  {
    return _gameServerTable.containsKey(Integer.valueOf(id));
  }

  public boolean registerWithFirstAvaliableId(GameServerInfo gsi)
  {
    synchronized (_gameServerTable)
    {
      for (Map.Entry entry : _serverNames.entrySet())
      {
        if (!_gameServerTable.containsKey(entry.getKey()))
        {
          _gameServerTable.put(entry.getKey(), gsi);
          gsi.setId(((Integer)entry.getKey()).intValue());
          return true;
        }
      }
    }
    return false;
  }

  public boolean register(int id, GameServerInfo gsi)
  {
    synchronized (_gameServerTable)
    {
      if (!_gameServerTable.containsKey(Integer.valueOf(id)))
      {
        _gameServerTable.put(Integer.valueOf(id), gsi);
        gsi.setId(id);
        return true;
      }
    }
    return false;
  }

  public void registerServerOnDB(GameServerInfo gsi)
  {
    registerServerOnDB(gsi.getHexId(), gsi.getId(), gsi.getExternalHost());
  }

  public void registerServerOnDB(byte[] hexId, int id, String externalHost)
  {
    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("INSERT INTO gameservers (hexid,server_id,host) values (?,?,?)");
      statement.setString(1, hexToString(hexId));
      statement.setInt(2, id);
      statement.setString(3, externalHost);
      statement.executeUpdate();
      statement.close();
    }
    catch (SQLException e)
    {
      _log.warning("SQL error while saving gameserver: " + e);
    }
    finally {
      try {
        statement.close(); } catch (Exception e) {
      }try { con.close(); } catch (Exception e) {
      }
    }
  }

  public String getServerNameById(int id) {
    return (String)getServerNames().get(Integer.valueOf(id));
  }

  public Map<Integer, String> getServerNames()
  {
    return _serverNames;
  }

  public KeyPair getKeyPair()
  {
    return _keyPairs[net.sf.l2j.util.Rnd.nextInt(10)];
  }

  private byte[] stringToHex(String string)
  {
    return new BigInteger(string, 16).toByteArray();
  }

  private String hexToString(byte[] hex)
  {
    if (hex == null)
      return "null";
    return new BigInteger(hex).toString(16); } 
  public static class GameServerInfo { private int _id;
    private byte[] _hexId;
    private boolean _isAuthed;
    private GameServerThread _gst;
    private int _status;
    private String _internalIp;
    private String _externalIp;
    private String _externalHost;
    private int _port;
    private boolean _isPvp = true;
    private boolean _isTestServer;
    private boolean _isShowingClock;
    private boolean _isShowingBrackets;
    private int _maxPlayers;

    public GameServerInfo(int id, byte[] hexId, GameServerThread gst) { _id = id;
      _hexId = hexId;
      _gst = gst;
      _status = 4;
    }

    public GameServerInfo(int id, byte[] hexId)
    {
      this(id, hexId, null);
    }

    public void setId(int id)
    {
      _id = id;
    }

    public int getId()
    {
      return _id;
    }

    public byte[] getHexId()
    {
      return _hexId;
    }

    public void setAuthed(boolean isAuthed)
    {
      _isAuthed = isAuthed;
    }

    public boolean isAuthed()
    {
      return _isAuthed;
    }

    public void setGameServerThread(GameServerThread gst)
    {
      _gst = gst;
    }

    public GameServerThread getGameServerThread()
    {
      return _gst;
    }

    public void setStatus(int status)
    {
      _status = status;
    }

    public int getStatus()
    {
      return _status;
    }

    public int getCurrentPlayerCount()
    {
      if (_gst == null)
        return 0;
      return _gst.getPlayerCount();
    }

    public void setInternalIp(String internalIp)
    {
      _internalIp = internalIp;
    }

    public String getInternalHost()
    {
      return _internalIp;
    }

    public void setExternalIp(String externalIp)
    {
      _externalIp = externalIp;
    }

    public String getExternalIp()
    {
      return _externalIp;
    }

    public void setExternalHost(String externalHost)
    {
      _externalHost = externalHost;
    }

    public String getExternalHost()
    {
      return _externalHost;
    }

    public int getPort()
    {
      return _port;
    }

    public void setPort(int port)
    {
      _port = port;
    }

    public void setMaxPlayers(int maxPlayers)
    {
      _maxPlayers = maxPlayers;
    }

    public int getMaxPlayers()
    {
      return _maxPlayers;
    }

    public boolean isPvp()
    {
      return _isPvp;
    }

    public void setTestServer(boolean val)
    {
      _isTestServer = val;
    }

    public boolean isTestServer()
    {
      return _isTestServer;
    }

    public void setShowingClock(boolean clock)
    {
      _isShowingClock = clock;
    }

    public boolean isShowingClock()
    {
      return _isShowingClock;
    }

    public void setShowingBrackets(boolean val)
    {
      _isShowingBrackets = val;
    }

    public boolean isShowingBrackets()
    {
      return _isShowingBrackets;
    }

    public void setDown()
    {
      setAuthed(false);
      setPort(0);
      setGameServerThread(null);
      setStatus(4);
    }
  }
}