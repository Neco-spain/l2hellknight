package net.sf.l2j.loginserver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.util.FastMap;
import net.sf.l2j.Config;

public abstract class FloodProtectedListener extends Thread
{
  private Logger _log = Logger.getLogger(FloodProtectedListener.class.getName());
  private Map<String, ForeignConnection> _floodProtection = new FastMap();
  private String _listenIp;
  private int _port;
  private ServerSocket _serverSocket;

  public FloodProtectedListener(String listenIp, int port)
    throws IOException
  {
    _port = port;
    _listenIp = listenIp;
    if (_listenIp.equals("*"))
    {
      _serverSocket = new ServerSocket(_port);
    }
    else
    {
      _serverSocket = new ServerSocket(_port, 50, InetAddress.getByName(_listenIp));
    }
  }

  public void run()
  {
    Socket connection = null;
    while (true)
    {
      try
      {
        connection = _serverSocket.accept();
        if (!Config.FLOOD_PROTECTION)
          continue;
        ForeignConnection fConnection = (ForeignConnection)_floodProtection.get(connection.getInetAddress().getHostAddress());
        if (fConnection == null)
          continue;
        fConnection.connectionNumber += 1;
        if (((fConnection.connectionNumber <= Config.FAST_CONNECTION_LIMIT) || (System.currentTimeMillis() - fConnection.lastConnection >= Config.NORMAL_CONNECTION_TIME)) && (System.currentTimeMillis() - fConnection.lastConnection >= Config.FAST_CONNECTION_TIME) && (fConnection.connectionNumber <= Config.MAX_CONNECTION_PER_IP))
        {
          continue;
        }

        fConnection.lastConnection = System.currentTimeMillis();
        connection.close();
        fConnection.connectionNumber -= 1;
        if (fConnection.isFlooding) continue; _log.warning("Potential Flood from " + connection.getInetAddress().getHostAddress());
        fConnection.isFlooding = true;
        continue;

        if (!fConnection.isFlooding)
          continue;
        fConnection.isFlooding = false;
        _log.info(connection.getInetAddress().getHostAddress() + " is not considered as flooding anymore.");

        fConnection.lastConnection = System.currentTimeMillis(); continue;

        fConnection = new ForeignConnection(System.currentTimeMillis());
        _floodProtection.put(connection.getInetAddress().getHostAddress(), fConnection);

        addClient(connection);

        continue;
      }
      catch (Exception e)
      {
        try
        {
          connection.close(); } catch (Exception e2) {
        }if (isInterrupted())
          try
          {
            _serverSocket.close();
          }
          catch (IOException io) {
            _log.log(Level.INFO, "", io);
          }
      }
    }
  }

  public abstract void addClient(Socket paramSocket);

  public void removeFloodProtection(String ip)
  {
    if (!Config.FLOOD_PROTECTION)
      return;
    ForeignConnection fConnection = (ForeignConnection)_floodProtection.get(ip);
    if (fConnection != null)
    {
      fConnection.connectionNumber -= 1;
      if (fConnection.connectionNumber == 0)
      {
        _floodProtection.remove(fConnection);
      }
    }
    else
    {
      _log.warning("Removing a flood protection for a GameServer that was not in the connection map??? :" + ip);
    }
  }

  public void close()
  {
    try
    {
      _serverSocket.close();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  protected static class ForeignConnection
  {
    public int connectionNumber;
    public long lastConnection;
    public boolean isFlooding = false;

    public ForeignConnection(long time)
    {
      lastConnection = time;
      connectionNumber = 1;
    }
  }
}