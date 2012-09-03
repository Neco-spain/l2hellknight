package l2rt.gameserver.loginservercon;

import javolution.util.FastMap;
import l2rt.Config;
import l2rt.Server;
import l2rt.common.ThreadPoolManager;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.loginservercon.gspackets.GameServerBasePacket;
import l2rt.gameserver.loginservercon.gspackets.PlayerAuthRequest;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.network.L2GameClient;
import l2rt.gameserver.network.serverpackets.LoginFail;
import l2rt.util.BannedIp;
import l2rt.util.GArray;
import l2rt.util.Util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Logger;

public class LSConnection extends Thread
{
	private static final Logger log = Logger.getLogger(LSConnection.class.getName());
	private static final LSConnection instance = new LSConnection();

	private Selector selector;

	private final FastMap<String, L2GameClient> waitingClients = FastMap.newInstance();
	private final FastMap<String, L2GameClient> accountsInGame = FastMap.newInstance();

	private SelectionKey key;
	private SocketChannel channel;

	private volatile boolean shutdown;
	private volatile boolean restart = true;
	private GArray<BannedIp> bannedIpList;

	public static LSConnection getInstance()
	{
		return instance;
	}

	private LSConnection()
	{
		try
		{
			selector = Selector.open();
		}
		catch(IOException e)
		{
			e.printStackTrace();
			log.warning("LSConnection: Can't open selector, restarting.");
			Server.exit(2, "LSConnection: Can't open selector, restarting.");
		}

		if(Config.DEBUG_GS_LS)
			log.info("GS Debug: Selector started.");
	}

	private void reconnect()
	{
		try
		{
			log.info("GameServer: Connecting to LoginServer on " + Config.GAME_SERVER_LOGIN_HOST + ":" + Config.GAME_SERVER_LOGIN_PORT);
			selector.close();
			selector = Selector.open();
			channel = SocketChannel.open();
			channel.configureBlocking(false);
			channel.register(selector, SelectionKey.OP_CONNECT);
			channel.connect(new InetSocketAddress(Config.GAME_SERVER_LOGIN_HOST, Config.GAME_SERVER_LOGIN_PORT));
			key = channel.keyFor(selector);
			restart = false;
		}
		catch(Exception e)
		{
			log.info("Cant connect to server: " + e.getMessage());
		}
	}

	private void readSelected()
	{
		while(!(shutdown || restart))
			try
			{
				if(key == null || !key.isValid())
					return;

				AttLS att = (AttLS) key.attachment();
				if(att != null)
				{
					ArrayDeque<GameServerBasePacket> sendQueue = att.getSendPacketQueue();
					synchronized (sendQueue)
					{
						if(sendQueue.isEmpty())
						{
							key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
							key.interestOps(key.interestOps() | SelectionKey.OP_READ);
						}
						else
							key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
					}
				}

				int keyNum = selector.selectNow();

				if(keyNum > 0)
				{
					Iterator<?> keys = selector.selectedKeys().iterator();
					while(keys.hasNext())
					{
						SelectionKey key = (SelectionKey) keys.next();
						keys.remove();

						if(!key.isValid())
						{
							close(key);
							continue;
						}

						int opts = key.readyOps();

						if(Config.DEBUG_GS_LS)
							log.info("GS Debug: key selected, readyOpts: " + opts);

						switch(opts)
						{
							case SelectionKey.OP_CONNECT:
								connect(key);
								break;
							case SelectionKey.OP_WRITE:
								write(key);
								break;
							case SelectionKey.OP_READ:
								read(key);
								break;
							case SelectionKey.OP_READ | SelectionKey.OP_WRITE:
								write(key);
								read(key);
								break;
							default:
								log.warning("LSConnection: unknown readyOpts: " + opts);
						}
					}
				}

				Thread.sleep(1);
			}
			catch(Exception e)
			{
				System.out.println("Disconnected from LoginServer:");
				e.printStackTrace();
				close(key);
				break;
			}
	}

	@Override
	public void run()
	{
		while(restart)
		{
			reconnect();
			readSelected();
			close(null);
			try
			{
				Thread.sleep(1000);
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}

			if(shutdown)
				break;
		}
	}

	public void read(SelectionKey key)
	{
		AttLS att = (AttLS) key.attachment();
		SocketChannel channel = (SocketChannel) key.channel();

		int numRead;

		try
		{
			numRead = channel.read(att.getReadBuffer());
		}
		catch(IOException e)
		{
			close(key);
			return;
		}

		if(numRead == -1)
		{
			close(key);
			return;
		}

		if(numRead == 0)
			return;

		att.processData();

		if(Config.DEBUG_GS_LS)
			log.info("GS Debug: data readed");
	}

	public void write(SelectionKey key)
	{
		AttLS att = (AttLS) key.attachment();
		SocketChannel channel = (SocketChannel) key.channel();

		ArrayDeque<GameServerBasePacket> sendPacketQueue = att.getSendPacketQueue();
		if(!att.isCryptInitialized())
			return;

		try
		{
			GameServerBasePacket packet;
			byte[] data;
			synchronized (sendPacketQueue)
			{
				while((packet = sendPacketQueue.poll()) != null)
				{
					data = packet.getBytes();
					if(!(packet instanceof l2rt.gameserver.loginservercon.gspackets.BlowFishKey))
						data = att.encrypt(data);
					data = Util.writeLenght(data);
					channel.write(ByteBuffer.wrap(data));

					if(Config.DEBUG_GS_LS)
						log.info("GameServer -> LoginServer: Sending packet: " + packet.getClass().getSimpleName());
				}
			}
		}
		catch(Exception e)
		{
			close(key);
			return;
		}

		key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);

		if(Config.DEBUG_GS_LS)
			log.info("GS Debug: Data sended");
	}

	public void connect(SelectionKey key)
	{
		SocketChannel channel = (SocketChannel) key.channel();

		try
		{
			channel.finishConnect();
		}
		catch(IOException e)
		{
			close(key);
			return;
		}

		key.attach(new AttLS(key, this));
		key.interestOps(key.interestOps() & ~SelectionKey.OP_CONNECT);
		key.interestOps(key.interestOps() | SelectionKey.OP_READ);

		if(Config.DEBUG_GS_LS)
			log.info("GS Debug: connection established");
	}

	public void close(SelectionKey key)
	{
		if(Config.DEBUG_GS_LS)
			log.info("GS Debug: closing connection");

		if(key == null)
			key = this.key;

		if(key == null && channel != null)
			key = channel.keyFor(selector);

		if(key != null)
		{
			key.cancel();

			AttLS att = (AttLS) key.attachment();
			if(att != null)
				att.close();
		}

		try
		{
			if(channel != null)
				channel.close();
		}
		catch(IOException e)
		{}

		this.key = null;
		channel = null;

		if(shutdown)
			return;

		restart = true;

		synchronized (waitingClients)
		{
			Collection<L2GameClient> wc = waitingClients.values();

			for(L2GameClient c : wc)
			{
				c.sendPacket(new LoginFail(LoginFail.SYSTEM_ERROR_LOGIN_LATER));
				ThreadPoolManager.getInstance().scheduleGeneral(new KickWaitingClientTask(c), 1000);
			}

			waitingClients.clear();
		}

		GArray<L2GameClient> accountsToClose = new GArray<L2GameClient>(accountsInGame.size());
		synchronized (accountsInGame)
		{
			for(L2GameClient client : accountsInGame.values())
				if(client.getActiveChar() == null)
					accountsToClose.add(client);
			accountsInGame.clear();
		}
		for(L2GameClient client : accountsToClose)
			if(client.getActiveChar() == null)
				client.closeNow(false);
	}

	public void sendPacket(GameServerBasePacket packet)
	{
		if(shutdown || key == null || key.attachment() == null)
			return;

		AttLS att = (AttLS) key.attachment();
		att.sendPacket(packet);
	}

	public void addWaitingClient(L2GameClient client)
	{
		synchronized (waitingClients)
		{
			// Если идет процесс выключения даного трида, то не позволяем сюда логинится.
			if(shutdown || key == null || key.attachment() == null)
			{
				client.sendPacket(new LoginFail(LoginFail.SYSTEM_ERROR_LOGIN_LATER));
				ThreadPoolManager.getInstance().scheduleGeneral(new KickWaitingClientTask(client), 1000);
				return;
			}

			L2GameClient sameClient = waitingClients.remove(client.getLoginName());

			if(sameClient != null)
			{
				sameClient.sendPacket(new LoginFail(LoginFail.ACOUNT_ALREADY_IN_USE));
				ThreadPoolManager.getInstance().scheduleGeneral(new KickWaitingClientTask(sameClient), 1000);
			}

			waitingClients.put(client.getLoginName(), client);
			sendPacket(new PlayerAuthRequest(client));
		}

		if(Config.DEBUG_GS_LS)
			log.info("GameServer: Adding client to waiting list: " + client.getLoginName());
	}

	public L2GameClient removeWaitingClient(String account)
	{
		L2GameClient client;
		synchronized (waitingClients)
		{
			client = waitingClients.remove(account);
		}
		return client;
	}

	public void addAccountInGame(L2GameClient client)
	{
		if(client == null)
			return;

		synchronized (accountsInGame)
		{
			// Если идет процесс выключения даного трида, то не позволяем сюда логинится.
			if(shutdown || key == null || key.attachment() == null)
			{
				client.sendPacket(new LoginFail(LoginFail.SYSTEM_ERROR_LOGIN_LATER));
				ThreadPoolManager.getInstance().scheduleGeneral(new KickWaitingClientTask(client), 1000);
				return;
			}

			L2GameClient oldClient = null;

			if(client.getLoginName() != null)
				oldClient = accountsInGame.remove(client.getLoginName());

			if(oldClient != null)
			{
				L2Player activeChar = oldClient.getActiveChar();
				if(activeChar != null)
				{
					activeChar.sendPacket(Msg.ANOTHER_PERSON_HAS_LOGGED_IN_WITH_THE_SAME_ACCOUNT);
					oldClient.sendPacket(Msg.ServerClose);
				}
				else
					oldClient.sendPacket(Msg.ServerClose);

				ThreadPoolManager.getInstance().scheduleGeneral(new KickPlayerInGameTask(oldClient), 1000);
			}

			if(client.getLoginName() != null)
				accountsInGame.put(client.getLoginName(), client);
		}
	}

	public void removeAccountInGame(L2GameClient client)
	{
		synchronized (accountsInGame)
		{
			String loginName = client.getLoginName();
			L2GameClient oldClient = accountsInGame.get(loginName);

			if(client.equals(oldClient))
				accountsInGame.remove(loginName);
		}
	}

	public L2GameClient getAccountInGame(String account)
	{
		synchronized (accountsInGame)
		{
			return accountsInGame.get(account);
		}
	}

	public void kickAccountInGame(String account)
	{
		synchronized (accountsInGame)
		{
			L2GameClient client = accountsInGame.get(account);

			if(client != null)
			{
				L2Player activeChar = client.getActiveChar();
				if(activeChar != null)
					activeChar.sendPacket(Msg.ANOTHER_PERSON_HAS_LOGGED_IN_WITH_THE_SAME_ACCOUNT);
				else
					client.sendPacket(Msg.ServerClose);

				ThreadPoolManager.getInstance().scheduleGeneral(new KickPlayerInGameTask(client), 1000);
			}
		}
	}

	public void removeAccount(L2GameClient client)
	{
		if(client.getState() == L2GameClient.GameClientState.CONNECTED)
			removeWaitingClient(client.getLoginName());
		else
			removeAccountInGame(client);
	}

	public void shutdown()
	{
		shutdown = true;
	}

	public boolean isShutdown()
	{
		return shutdown;
	}

	public void restart()
	{
		restart = true;
	}

	public GArray<BannedIp> getBannedIpList()
	{
		return bannedIpList;
	}

	public void setBannedIpList(GArray<BannedIp> bannedIpList)
	{
		this.bannedIpList = bannedIpList;
	}
}