package l2rt.loginserver.serverpackets;

import javolution.util.FastList;
import l2rt.gameserver.loginservercon.AdvIP;
import l2rt.gameserver.model.L2World;
import l2rt.loginserver.GameServerTable;
import l2rt.loginserver.L2LoginClient;
import l2rt.loginserver.gameservercon.GameServerInfo;
import l2rt.loginserver.gameservercon.gspackets.ServerStatus;
import l2rt.util.Util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * ServerList
 * Format: cc [cddcchhcdc]
 *
 * c: server list size (number of servers)
 * c: last server
 * [ (repeat for each servers)
 * c: server id (ignored by client?)
 * d: server ip
 * d: server port
 * c: age limit (used by client?)
 * c: pvp or not (used by client?)
 * h: current number of players
 * h: max number of players
 * c: 0 if server is down
 * d: 2nd bit: clock
 *    3rd bit: wont dsiplay server name
 *    4th bit: test server (used by client?)
 * c: 0 if you dont want to display brackets in front of sever name
 * ]
 *
 * Server will be considered as Good when the number of  online players
 * is less than half the maximum. as Normal between half and 4/5
 * and Full when there's more than 4/5 of the maximum number of players
 */
public final class ServerList extends L2LoginServerPacket
{
	private List<ServerData> _servers;
	private int _lastServer;

	class ServerData
	{
		String ip;
		int port;
		boolean pvp;
		int currentPlayers;
		int maxPlayers;
		boolean testServer;
		boolean brackets;
		boolean clock;
		int status;
		public int server_id;

		ServerData(String pIp, int pPort, boolean pPvp, boolean pTestServer, int pCurrentPlayers, int pMaxPlayers, boolean pBrackets, boolean pClock, int pStatus, int pServer_id)
		{
			ip = pIp;
			port = pPort;
			pvp = pPvp;
			testServer = pTestServer;
			currentPlayers = pCurrentPlayers;
			maxPlayers = pMaxPlayers;
			brackets = pBrackets;
			clock = pClock;
			status = pStatus;
			server_id = pServer_id;
		}
	}

	public ServerList(L2LoginClient client)
	{
		_servers = new FastList<ServerData>();
		_lastServer = client.getLastServer();
		
		for(GameServerInfo gsi : GameServerTable.getInstance().getRegisteredGameServers().values())
		{
			Boolean added = false;
			if(client.getIpAddress().equals("Null IP"))
				continue;
			if(gsi.isTestServer() && client.getAccessLevel() < 100)
				continue;
			String ipAddr = Util.isInternalIP(client.getIpAddress()) ? gsi.getInternalHost() : gsi.getExternalHost();
			if(ipAddr == null || ipAddr.equals("Null IP"))
				continue;
			if(gsi.getAdvIP() != null)
				for(AdvIP ip : gsi.getAdvIP())
					if(!added && GameServerTable.getInstance().CheckSubNet(client.getConnection().getSocket().getInetAddress().getHostAddress(), ip))
					{
						added = true;
						addServer(ip.ipadress, gsi.getPort(), gsi.isPvp(), gsi.isTestServer(), gsi.getCurrentPlayerCount(), gsi.getMaxPlayers(), gsi.isShowingBrackets(), gsi.isShowingClock(), gsi.getStatus(), gsi.getId());
					}
			if(!added)
				if(ipAddr.equals("*"))
					addServer(client.getConnection().getSocket().getLocalAddress().getHostAddress(), gsi.getPort(), gsi.isPvp(), gsi.isTestServer(), gsi.getCurrentPlayerCount(), gsi.getMaxPlayers(), gsi.isShowingBrackets(), gsi.isShowingClock(), gsi.getStatus(), gsi.getId());
				else
					addServer(ipAddr, gsi.getPort(), gsi.isPvp(), gsi.isTestServer(), gsi.getCurrentPlayerCount(), gsi.getMaxPlayers(), gsi.isShowingBrackets(), gsi.isShowingClock(), gsi.getStatus(), gsi.getId());
		}
	}

	public void addServer(String ip, int port, boolean pvp, boolean testServer, int currentPlayer, int maxPlayer, boolean brackets, boolean clock, int status, int server_id)
	{
		_servers.add(new ServerData(ip, port, pvp, testServer, currentPlayer, maxPlayer, brackets, clock, status, server_id));
	}

	@Override
	public void write()
	{
		writeC(0x04);
		writeC(_servers.size());
		writeC(_lastServer);
		for(ServerData server : _servers)
		{
			writeC(server.server_id); // server id

			try
			{
				InetAddress i4 = InetAddress.getByName(server.ip);
				byte[] raw = i4.getAddress();
				writeC(raw[0] & 0xff);
				writeC(raw[1] & 0xff);
				writeC(raw[2] & 0xff);
				writeC(raw[3] & 0xff);
			}
			catch(UnknownHostException e)
			{
				e.printStackTrace();
				writeC(127);
				writeC(0);
				writeC(0);
				writeC(1);
			}

			writeD(server.port);
			writeC(0x00); // age limit         writeC(0x00);
			writeC(0x00);//writeC(server.pvp ? 0x01 : 0x00)
			writeH(server.currentPlayers);
			writeH(server.maxPlayers);
			writeC(server.status == ServerStatus.STATUS_DOWN ? 0x00 : 0x01);
			int bits = 0;
			if(server.testServer)
				bits = 0x04;
			if(server.clock)
				bits = 0x02;
			// 0x10 - Character creation restricted
			// 0x20 - Event Server
			// 0x40 - Free Server
			writeD(bits);
			writeC(server.brackets ? 0x01 : 0x00);
		}
		writeH(0x00);
		writeC(0x00);
	}
}