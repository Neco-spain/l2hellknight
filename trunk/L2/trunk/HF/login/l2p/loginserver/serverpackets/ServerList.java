package l2m.loginserver.serverpackets;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import l2m.commons.net.utils.NetUtils;
import l2m.loginserver.GameServerManager;
import l2m.loginserver.accounts.Account;
import l2m.loginserver.gameservercon.GameServer;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

public final class ServerList extends L2LoginServerPacket
{
  private List<ServerData> _servers = new ArrayList();
  private int _lastServer;

  public ServerList(Account account)
  {
    _lastServer = account.getLastServer();

    for (GameServer gs : GameServerManager.getInstance().getGameServers())
    {
      InetAddress ip;
      try {
        ip = NetUtils.isInternalIP(account.getLastIP()) ? gs.getInternalHost() : gs.getExternalHost();
      }
      catch (UnknownHostException e)
      {
        continue;
      }

      Pair entry = account.getAccountInfo(gs.getId());

      _servers.add(new ServerData(gs.getId(), ip, gs.getPort(), gs.isPvp(), gs.isShowingBrackets(), gs.getServerType(), gs.getOnline(), gs.getMaxPlayers(), gs.isOnline(), entry == null ? 0 : ((Integer)entry.getKey()).intValue(), gs.getAgeLimit(), entry == null ? ArrayUtils.EMPTY_INT_ARRAY : (int[])entry.getValue()));
    }
  }

  protected void writeImpl()
  {
    writeC(4);
    writeC(_servers.size());
    writeC(_lastServer);
    for (ServerData server : _servers)
    {
      writeC(server.serverId);
      InetAddress i4 = server.ip;
      byte[] raw = i4.getAddress();
      writeC(raw[0] & 0xFF);
      writeC(raw[1] & 0xFF);
      writeC(raw[2] & 0xFF);
      writeC(raw[3] & 0xFF);
      writeD(server.port);
      writeC(server.ageLimit);
      writeC(server.pvp ? 1 : 0);
      writeH(server.online);
      writeH(server.maxPlayers);
      writeC(server.status ? 1 : 0);
      writeD(server.type);
      writeC(server.brackets ? 1 : 0);
    }

    writeH(0);
    writeC(_servers.size());
    for (ServerData server : _servers)
    {
      writeC(server.serverId);
      writeC(server.playerSize);
      writeC(server.deleteChars.length);
      for (int t : server.deleteChars)
        writeD((int)(t - System.currentTimeMillis() / 1000L));
    }
  }

  private static class ServerData
  {
    int serverId;
    InetAddress ip;
    int port;
    int online;
    int maxPlayers;
    boolean status;
    boolean pvp;
    boolean brackets;
    int type;
    int ageLimit;
    int playerSize;
    int[] deleteChars;

    ServerData(int serverId, InetAddress ip, int port, boolean pvp, boolean brackets, int type, int online, int maxPlayers, boolean status, int size, int ageLimit, int[] d)
    {
      this.serverId = serverId;
      this.ip = ip;
      this.port = port;
      this.pvp = pvp;
      this.brackets = brackets;
      this.type = type;
      this.online = online;
      this.maxPlayers = maxPlayers;
      this.status = status;
      playerSize = size;
      this.ageLimit = ageLimit;
      deleteChars = d;
    }
  }
}