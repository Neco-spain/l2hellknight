package net.sf.l2j.loginserver.serverpackets;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.loginserver.GameServerTable;
import net.sf.l2j.loginserver.GameServerTable.GameServerInfo;
import net.sf.l2j.loginserver.L2LoginClient;

public final class ServerList extends L2LoginServerPacket
{
  private List<ServerData> _servers;
  private int _lastServer;

  public ServerList(L2LoginClient client)
  {
    _servers = new FastList();
    _lastServer = client.getLastServer();
    for (GameServerTable.GameServerInfo gsi : GameServerTable.getInstance().getRegisteredGameServers().values())
    {
      if ((gsi.getStatus() == 5) && (client.getAccessLevel() >= Config.GM_MIN))
      {
        addServer(client.usesInternalIP() ? gsi.getInternalHost() : gsi.getExternalHost(), gsi.getPort(), gsi.isPvp(), gsi.isTestServer(), gsi.getCurrentPlayerCount(), gsi.getMaxPlayers(), gsi.isShowingBrackets(), gsi.isShowingClock(), gsi.getStatus(), gsi.getId());
      }
      else if (gsi.getStatus() != 5)
      {
        addServer(client.usesInternalIP() ? gsi.getInternalHost() : gsi.getExternalHost(), gsi.getPort(), gsi.isPvp(), gsi.isTestServer(), gsi.getCurrentPlayerCount(), gsi.getMaxPlayers(), gsi.isShowingBrackets(), gsi.isShowingClock(), gsi.getStatus(), gsi.getId());
      }
      else
      {
        addServer(client.usesInternalIP() ? gsi.getInternalHost() : gsi.getExternalHost(), gsi.getPort(), gsi.isPvp(), gsi.isTestServer(), gsi.getCurrentPlayerCount(), gsi.getMaxPlayers(), gsi.isShowingBrackets(), gsi.isShowingClock(), 4, gsi.getId());
      }
    }
  }

  public void addServer(String ip, int port, boolean pvp, boolean testServer, int currentPlayer, int maxPlayer, boolean brackets, boolean clock, int status, int server_id)
  {
    _servers.add(new ServerData(ip, port, pvp, testServer, currentPlayer, maxPlayer, brackets, clock, status, server_id));
  }

  public void write()
  {
    writeC(4);
    writeC(_servers.size());
    writeC(_lastServer);
    for (ServerData server : _servers)
    {
      writeC(server._serverId);
      try
      {
        InetAddress i4 = InetAddress.getByName(server._ip);
        byte[] raw = i4.getAddress();
        writeC(raw[0] & 0xFF);
        writeC(raw[1] & 0xFF);
        writeC(raw[2] & 0xFF);
        writeC(raw[3] & 0xFF);
      }
      catch (UnknownHostException e)
      {
        e.printStackTrace();
        writeC(127);
        writeC(0);
        writeC(0);
        writeC(1);
      }

      writeD(server._port);
      writeC(0);
      writeC(server._pvp ? 1 : 0);
      writeH(server._currentPlayers);
      writeH(server._maxPlayers);
      writeC(server._status == 4 ? 0 : 1);
      int bits = 0;
      if (server._testServer)
      {
        bits |= 4;
      }
      if (server._clock)
      {
        bits |= 2;
      }
      writeD(bits);
      writeC(server._brackets ? 1 : 0);
    }
  }

  class ServerData
  {
    protected String _ip;
    protected int _port;
    protected boolean _pvp;
    protected int _currentPlayers;
    protected int _maxPlayers;
    protected boolean _testServer;
    protected boolean _brackets;
    protected boolean _clock;
    protected int _status;
    protected int _serverId;

    ServerData(String pIp, int pPort, boolean pPvp, boolean pTestServer, int pCurrentPlayers, int pMaxPlayers, boolean pBrackets, boolean pClock, int pStatus, int pServer_id)
    {
      _ip = pIp;
      _port = pPort;
      _pvp = pPvp;
      _testServer = pTestServer;
      _currentPlayers = pCurrentPlayers;
      _maxPlayers = pMaxPlayers;
      _brackets = pBrackets;
      _clock = pClock;
      _status = pStatus;
      _serverId = pServer_id;
    }
  }
}