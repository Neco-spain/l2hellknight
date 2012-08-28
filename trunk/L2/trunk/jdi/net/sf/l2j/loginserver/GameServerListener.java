package net.sf.l2j.loginserver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import java.util.logging.Logger;
import javolution.util.FastList;
import net.sf.l2j.Config;

public class GameServerListener extends FloodProtectedListener
{
  private static Logger _log = Logger.getLogger(GameServerListener.class.getName());
  private static List<GameServerThread> _gameServers = new FastList();

  public GameServerListener() throws IOException
  {
    super(Config.GAME_SERVER_LOGIN_HOST, Config.GAME_SERVER_LOGIN_PORT);
  }

  public void addClient(Socket s)
  {
    if (Config.DEBUG)
    {
      _log.info("Received gameserver connection from: " + s.getInetAddress().getHostAddress());
    }
    GameServerThread gst = new GameServerThread(s);
    _gameServers.add(gst);
  }

  public void removeGameServer(GameServerThread gst)
  {
    _gameServers.remove(gst);
  }
}