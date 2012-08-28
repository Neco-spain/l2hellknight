package l2m.loginserver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import l2m.commons.dbutils.DbUtils;
import l2m.loginserver.database.L2DatabaseFactory;
import l2m.loginserver.gameservercon.GameServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameServerManager
{
  private static Logger _log = LoggerFactory.getLogger(GameServerManager.class);

  private static final GameServerManager _instance = new GameServerManager();

  private final Map<Integer, GameServer> _gameServers = new TreeMap();
  private final ReadWriteLock _lock = new ReentrantReadWriteLock();
  private final Lock _readLock = _lock.readLock();
  private final Lock _writeLock = _lock.writeLock();

  public static final GameServerManager getInstance()
  {
    return _instance;
  }

  public GameServerManager()
  {
    load();
    _log.info("Loaded " + _gameServers.size() + " registered GameServer(s).");
  }

  private void load()
  {
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT server_id FROM gameservers");
      rset = statement.executeQuery();

      while (rset.next())
      {
        int id = rset.getInt("server_id");

        GameServer gs = new GameServer(id);

        _gameServers.put(Integer.valueOf(id), gs);
      }
    }
    catch (Exception e)
    {
      _log.error("", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement, rset);
    }
  }

  public GameServer[] getGameServers()
  {
    _readLock.lock();
    try
    {
      GameServer[] arrayOfGameServer = (GameServer[])_gameServers.values().toArray(new GameServer[_gameServers.size()]);
      return arrayOfGameServer; } finally { _readLock.unlock(); } throw localObject;
  }

  public GameServer getGameServerById(int id)
  {
    _readLock.lock();
    try
    {
      GameServer localGameServer = (GameServer)_gameServers.get(Integer.valueOf(id));
      return localGameServer; } finally { _readLock.unlock(); } throw localObject;
  }

  public boolean registerGameServer(GameServer gs)
  {
    if (!Config.ACCEPT_NEW_GAMESERVER) {
      return false;
    }
    _writeLock.lock();
    try
    {
      int id = 1;
      while (id++ > 0)
      {
        GameServer pgs = (GameServer)_gameServers.get(Integer.valueOf(id));
        if ((pgs == null) || (!pgs.isAuthed()))
        {
          _gameServers.put(Integer.valueOf(id), gs);
          gs.setId(id);
          int i = 1;
          return i;
        }
      } } finally { _writeLock.unlock();
    }
    return false;
  }

  public boolean registerGameServer(int id, GameServer gs)
  {
    _writeLock.lock();
    try
    {
      GameServer pgs = (GameServer)_gameServers.get(Integer.valueOf(id));
      int i;
      if ((!Config.ACCEPT_NEW_GAMESERVER) && (pgs == null)) {
        i = 0;
        return i;
      }
      if ((pgs == null) || (!pgs.isAuthed()))
      {
        _gameServers.put(Integer.valueOf(id), gs);
        gs.setId(id);
        i = 1;
        return i;
      } } finally { _writeLock.unlock();
    }
    return false;
  }
}