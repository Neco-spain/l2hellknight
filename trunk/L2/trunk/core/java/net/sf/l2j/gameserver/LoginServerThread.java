package net.sf.l2j.gameserver;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.gameserverpackets.AuthRequest;
import net.sf.l2j.gameserver.gameserverpackets.BlowFishKey;
import net.sf.l2j.gameserver.gameserverpackets.ChangeAccessLevel;
import net.sf.l2j.gameserver.gameserverpackets.GameServerBasePacket;
import net.sf.l2j.gameserver.gameserverpackets.PlayerAuthRequest;
import net.sf.l2j.gameserver.gameserverpackets.PlayerInGame;
import net.sf.l2j.gameserver.gameserverpackets.PlayerLogout;
import net.sf.l2j.gameserver.gameserverpackets.ServerStatus;
import net.sf.l2j.gameserver.loginserverpackets.AuthResponse;
import net.sf.l2j.gameserver.loginserverpackets.InitLS;
import net.sf.l2j.gameserver.loginserverpackets.KickPlayer;
import net.sf.l2j.gameserver.loginserverpackets.LoginServerFail;
import net.sf.l2j.gameserver.loginserverpackets.PlayerAuthResponse;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.L2GameClient.GameClientState;
import net.sf.l2j.gameserver.network.serverpackets.AuthLoginFail;
import net.sf.l2j.gameserver.network.serverpackets.CharSelectInfo;
import net.sf.l2j.loginserver.crypt.NewCrypt;
import net.sf.l2j.util.Rnd;
import net.sf.l2j.util.Util;

public class LoginServerThread extends Thread
{
	protected static final Logger _log = Logger.getLogger(LoginServerThread.class.getName());

	/** The LoginServerThread singleton */
	private static LoginServerThread	_instance;

	/** {@see net.sf.l2j.loginserver.LoginServer#PROTOCOL_REV } */
	private static final int			REVISION = 0x0102;
	private RSAPublicKey				_publicKey;
	private String						_hostname;
	private int							_port;
	private int							_gamePort;
	private Socket						_loginSocket;
	private InputStream 				_in;
	private OutputStream 				_out;

	/**
	 * The BlowFish engine used to encrypt packets<br>
	 * It is first initialized with a unified key:<br>
	 * "_;v.]05-31!|+-%xT!^[$\00"<br>
	 * <br>
	 * and then after handshake, with a new key sent by<br>
	 * loginserver during the handshake. This new key is stored<br>
	 * in {@link #_blowfishKey}
	 */
	private NewCrypt 					_blowfish;
	private byte[]			 			_blowfishKey;
	private byte[] 						_hexID;
	private boolean			 			_acceptAlternate;
	private int							_requestID;
	private int							_serverID;
	private boolean 					_reserveHost;
	private int							_maxPlayer;
	private List<WaitingClient>			_waitingClients;
	private Map<String, L2GameClient>	_accountsInGameServer;
	private int							_status;
	private String						_serverName;
	private String						_gameExternalHost;
	private String						_gameInternalHost;

	public LoginServerThread()
	{
		super("LoginServerThread");
		_port = Config.GAME_SERVER_LOGIN_PORT;
		_gamePort = Config.PORT_GAME;
		_hostname = Config.GAME_SERVER_LOGIN_HOST;
		_hexID = Config.HEX_ID;
		if(_hexID == null)
		{
			_requestID = Config.REQUEST_ID;
			_hexID = generateHex(16);
		}
		else
		{
			_requestID = Config.SERVER_ID;
		}
		_acceptAlternate = Config.ACCEPT_ALTERNATE_ID;
		_reserveHost = Config.RESERVE_HOST_ON_LOGIN;
		_gameExternalHost = Config.EXTERNAL_HOSTNAME;
		_gameInternalHost = Config.INTERNAL_HOSTNAME;
		_waitingClients = new FastList<WaitingClient>();
		_accountsInGameServer = new FastMap<String, L2GameClient>().setShared(true);
		_maxPlayer = Config.MAXIMUM_ONLINE_USERS;
	}

	public static LoginServerThread getInstance()
	{
		if(_instance == null)
		{
			_instance = new LoginServerThread();
		}
		return _instance;
	}

	@SuppressWarnings("static-access")
	@Override
	public void run()
	{
		while(true)
		{
			int lengthHi =0;
			int lengthLo =0;
			int length = 0;
			boolean checksumOk = false;
			try
			{
				// Connection
				_log.info("Connecting to login on "+_hostname+":"+_port);
				_loginSocket = new Socket(_hostname,_port);
				_in = _loginSocket.getInputStream();
				_out = new BufferedOutputStream(_loginSocket.getOutputStream());

				//init Blowfish
				_blowfishKey = generateHex(40);
				_blowfish = new NewCrypt("_;v.]05-31!|+-%xT!^[$\00");
				while (true)
				{
					lengthLo = _in.read();
					lengthHi = _in.read();
					length= lengthHi*256 + lengthLo;

					if (lengthHi < 0 )
					{
						_log.finer("LoginServerThread: Login terminated the connection.");
						break;
					}

					byte[] incoming = new byte[length];
					incoming[0] = (byte) lengthLo;
					incoming[1] = (byte) lengthHi;

					int receivedBytes = 0;
					int newBytes = 0;
					while (newBytes != -1 && receivedBytes<length-2)
					{
						newBytes =  _in.read(incoming, 2, length-2);
						receivedBytes = receivedBytes + newBytes;
					}

					if (receivedBytes != length-2)
					{
						_log.warning("Incomplete Packet is sent to the server, closing connection.(LS)");
						break;
					}

					byte[] decrypt = new byte[length - 2];
					System.arraycopy(incoming, 2, decrypt, 0, decrypt.length);
					// decrypt if we have a key
					decrypt = _blowfish.decrypt(decrypt);
					checksumOk = NewCrypt.verifyChecksum(decrypt);

					if (!checksumOk)
					{
						_log.warning("Incorrect packet checksum, ignoring packet (LS)");
						break;
					}

					if (Config.DEBUG)
						_log.warning("[C]\n"+Util.printData(decrypt));

					int packetType = decrypt[0]&0xff;
					switch (packetType)
					{
					case 00:
						InitLS init = new InitLS(decrypt);
						if (Config.DEBUG) _log.info("Init received");
						if(init.getRevision() != REVISION)
						{
							//TODO: revision mismatch
							_log.warning("/!\\ Revision mismatch between LS and GS /!\\");
							break;
						}
						try
						{
							KeyFactory kfac = KeyFactory.getInstance("RSA");
							BigInteger modulus = new BigInteger(init.getRSAKey());
							RSAPublicKeySpec kspec1 = new RSAPublicKeySpec(modulus, RSAKeyGenParameterSpec.F4);
							_publicKey = (RSAPublicKey)kfac.generatePublic(kspec1);
							if (Config.DEBUG) _log.info("RSA key set up");
						}

						catch (GeneralSecurityException e)
						{
							_log.warning("Troubles while init the public key send by login");
							break;
						}
						//send the blowfish key through the rsa encryption
						BlowFishKey bfk = new BlowFishKey(_blowfishKey,_publicKey);
						sendPacket(bfk);
						if (Config.DEBUG)_log.info("Sent new blowfish key");
						//now, only accept paket with the new encryption
						_blowfish = new NewCrypt(_blowfishKey);
						if (Config.DEBUG)_log.info("Changed blowfish key");
						AuthRequest ar = new AuthRequest(_requestID, _acceptAlternate, _hexID, _gameExternalHost, _gameInternalHost, _gamePort, _reserveHost, _maxPlayer);
						sendPacket(ar);
						if (Config.DEBUG)_log.info("Sent AuthRequest to login");
						break;
					case 01:
						LoginServerFail lsf = new LoginServerFail(decrypt);
						_log.info("Damn! Registeration Failed: "+lsf.getReasonString());
						// login will close the connection here
						break;
					case 02:
						AuthResponse aresp = new AuthResponse(decrypt);
						_serverID = aresp.getServerId();
						_serverName = aresp.getServerName();
						Config.saveHexid(_serverID, hexToString(_hexID));
						_log.info("Registered on login as Server "+_serverID+" : "+_serverName);
						ServerStatus st = new ServerStatus();
						if(Config.SERVER_LIST_BRACKET)
						{
							st.addAttribute(ServerStatus.SERVER_LIST_SQUARE_BRACKET,ServerStatus.ON);
						}
						else
						{
							st.addAttribute(ServerStatus.SERVER_LIST_SQUARE_BRACKET,ServerStatus.OFF);
						}
						if(Config.SERVER_LIST_CLOCK)
						{
							st.addAttribute(ServerStatus.SERVER_LIST_CLOCK,ServerStatus.ON);
						}
						else
						{
							st.addAttribute(ServerStatus.SERVER_LIST_CLOCK,ServerStatus.OFF);
						}
						if(Config.SERVER_LIST_TESTSERVER)
						{
							st.addAttribute(ServerStatus.TEST_SERVER,ServerStatus.ON);
						}
						else
						{
							st.addAttribute(ServerStatus.TEST_SERVER,ServerStatus.OFF);
						}
						if(Config.SERVER_GMONLY)
						{
							st.addAttribute(ServerStatus.SERVER_LIST_STATUS,ServerStatus.STATUS_GM_ONLY);
						}
						else
						{
							st.addAttribute(ServerStatus.SERVER_LIST_STATUS,ServerStatus.STATUS_AUTO);
						}
						sendPacket(st);
						if(L2World.getInstance().getAllPlayersCount() > 0)
						{
							FastList<String> playerList = new FastList<String>();
							for(L2PcInstance player : L2World.getInstance().getAllPlayers())
							{
								playerList.add(player.getAccountName());
							}
							PlayerInGame pig = new PlayerInGame(playerList);
							sendPacket(pig);
						}
						break;
					case 03:
						PlayerAuthResponse par = new PlayerAuthResponse(decrypt);
						String account = par.getAccount();
						WaitingClient wcToRemove = null;
						synchronized(_waitingClients)
						{
							for(WaitingClient wc : _waitingClients)
							{
								if(wc.account.equals(account))
								{
									wcToRemove = wc;
								}
							}
						}
						if(wcToRemove != null)
						{
							if (par.isAuthed())
							{
								if (Config.DEBUG)_log.info("Login accepted player "+wcToRemove.account+" waited("+(GameTimeController.getGameTicks()-wcToRemove.timestamp)+"ms)");
								PlayerInGame pig = new PlayerInGame(par.getAccount());
								sendPacket(pig);
								wcToRemove.gameClient.setState(GameClientState.AUTHED);
								wcToRemove.gameClient.setSessionId(wcToRemove.session);
								CharSelectInfo cl = new CharSelectInfo(wcToRemove.account, wcToRemove.gameClient.getSessionId().playOkID1);
								wcToRemove.gameClient.getConnection().sendPacket(cl);
								wcToRemove.gameClient.setCharSelection(cl.getCharInfo());
							}
							else
							{
								_log.warning("session key is not correct. closing connection");
								wcToRemove.gameClient.getConnection().sendPacket(new AuthLoginFail(1));
								wcToRemove.gameClient.closeNow();
							}
							_waitingClients.remove(wcToRemove);
						}
						break;
					case 04:
						KickPlayer kp = new KickPlayer(decrypt);
						doKickPlayer(kp.getAccount());
						break;
					}
				}
			}
			catch (UnknownHostException e)
			{
				if (Config.DEBUG) e.printStackTrace();
			}
			catch (IOException e)
			{
				_log.info("Deconnected from Login, Trying to reconnect:");
				_log.info(e.toString());
			}
			finally
			{
				try { _loginSocket.close(); } catch (Exception e) {}
			}

			try
			{
				Thread.sleep(5000); // 5 seconds tempo.
			}
			catch(InterruptedException e)
			{
				//
			}
		}
	}

	public void addWaitingClientAndSendRequest(String acc, L2GameClient client, SessionKey key)
	{
		if(Config.DEBUG) System.out.println(key);
		WaitingClient wc = new WaitingClient(acc, client, key);
		synchronized(_waitingClients)
		{
			_waitingClients.add(wc);
		}
		PlayerAuthRequest par = new PlayerAuthRequest(acc,key);
		try
		{
			sendPacket(par);
		}
		catch (IOException e)
		{
			_log.warning("Error while sending player auth request");
			if (Config.DEBUG) e.printStackTrace();
		}
	}

	public void removeWaitingClient(L2GameClient client)
	{
		WaitingClient toRemove = null;
		synchronized(_waitingClients)
		{
			for(WaitingClient c :_waitingClients)
			{
				if(c.gameClient == client)
				{
					toRemove = c;
				}
			}
			if(toRemove != null)
				_waitingClients.remove(toRemove);
		}
	}

	public void sendLogout(String account)
	{
		PlayerLogout pl = new PlayerLogout(account);
		try
		{
			sendPacket(pl);
		}
		catch (IOException e)
		{
			_log.warning("Error while sending logout packet to login");
			if (Config.DEBUG) e.printStackTrace();
		}
	}

    public void addGameServerLogin(String account, L2GameClient client)
    {
		_accountsInGameServer.put(account, client);
    }

	public void sendAccessLevel(String account, int level)
	{
		ChangeAccessLevel cal = new ChangeAccessLevel(account, level);
		try
		{
			sendPacket(cal);
		}
		catch (IOException e)
		{
			if (Config.DEBUG)
				e.printStackTrace();
		}
	}

	private String hexToString(byte[] hex)
	{
		return new BigInteger(hex).toString(16);
	}

	public void doKickPlayer(String account)
	{
		if(_accountsInGameServer.get(account) != null)
		{
			_accountsInGameServer.get(account).closeNow();
			LoginServerThread.getInstance().sendLogout(account);
		}
	}

	public static byte[] generateHex(int size)
	{
		byte [] array = new byte[size];
		Rnd.nextBytes(array);
		if (Config.DEBUG)_log.fine("Generated random String:  \""+array+"\"");
		return array;
	}

	/**
	 * @param sl
	 * @throws IOException
	 */
	private void sendPacket(GameServerBasePacket sl) throws IOException
	{
		byte[] data = sl.getContent();
		NewCrypt.appendChecksum(data);
		if (Config.DEBUG) _log.finest("[S]\n"+Util.printData(data));
		data = _blowfish.crypt(data);

		int len = data.length+2;
		synchronized (_out) //avoids tow threads writing in the mean time
		{
			_out.write(len & 0xff);
			_out.write(len >> 8 &0xff);
			_out.write(data);
			_out.flush();
		}
	}

	/**
	 * @param maxPlayer The maxPlayer to set.
	 */
	public void setMaxPlayer(int maxPlayer)
	{
		sendServerStatus(ServerStatus.MAX_PLAYERS,maxPlayer);
		_maxPlayer = maxPlayer;
	}

	/**
	 * @return Returns the maxPlayer.
	 */
	public int getMaxPlayer()
	{
		return _maxPlayer;
	}

	/**
	 * @param server_gm_only
	 */
	public void sendServerStatus(int id, int value)
	{
		ServerStatus ss = new ServerStatus();
		ss.addAttribute(id,value);
		try
		{
			sendPacket(ss);
		}
		catch (IOException e)
		{
			if (Config.DEBUG) e.printStackTrace();
		}
	}

	/**
	 * @return
	 */
	public String getStatusString()
	{
		return ServerStatus.STATUS_STRING[_status];
	}

	/**
	 * @return
	 */
	public boolean isClockShown()
	{
		return Config.SERVER_LIST_CLOCK;
	}

	/**
	 * @return
	 */
	public boolean isBracketShown()
	{
		return Config.SERVER_LIST_BRACKET;
	}

	/**
	 * @return Returns the serverName.
	 */
	public String getServerName()
	{
		return _serverName;
	}

	public void setServerStatus(int status)
	{
		switch(status)
		{
		case ServerStatus.STATUS_AUTO:
			sendServerStatus(ServerStatus.SERVER_LIST_STATUS,ServerStatus.STATUS_AUTO);
			_status = status;
			break;
		case ServerStatus.STATUS_DOWN:
			sendServerStatus(ServerStatus.SERVER_LIST_STATUS,ServerStatus.STATUS_DOWN);
			_status = status;
			break;
		case ServerStatus.STATUS_FULL:
			sendServerStatus(ServerStatus.SERVER_LIST_STATUS,ServerStatus.STATUS_FULL);
			_status = status;
			break;
		case ServerStatus.STATUS_GM_ONLY:
			sendServerStatus(ServerStatus.SERVER_LIST_STATUS,ServerStatus.STATUS_GM_ONLY);
			_status = status;
			break;
		case ServerStatus.STATUS_GOOD:
			sendServerStatus(ServerStatus.SERVER_LIST_STATUS,ServerStatus.STATUS_GOOD);
			_status = status;
			break;
		case ServerStatus.STATUS_NORMAL:
			sendServerStatus(ServerStatus.SERVER_LIST_STATUS,ServerStatus.STATUS_NORMAL);
			_status = status;
			break;
		default:
			throw new IllegalArgumentException("Status does not exists:"+status);
		}
	}

	public static class SessionKey
	{
		public int playOkID1;
		public int playOkID2;
		public int loginOkID1;
		public int loginOkID2;
		public int clientKey = -1;

		public SessionKey(int loginOK1, int loginOK2, int playOK1, int playOK2)
		{
			playOkID1 = playOK1;
			playOkID2 = playOK2;
			loginOkID1 = loginOK1;
			loginOkID2 = loginOK2;
		}

		@Override
		public String toString()
		{
			return "PlayOk: "+playOkID1+" "+playOkID2+" LoginOk:"+loginOkID1+" "+loginOkID2;
		}
	}

	private class WaitingClient
	{
		public int timestamp;
		public String account;
		public L2GameClient gameClient;
		public SessionKey session;

		public WaitingClient(String acc, L2GameClient client, SessionKey key)
		{
			account = acc;
			timestamp = GameTimeController.getGameTicks();
			gameClient = client;
			session = key;
		}
	}

	public void removeAccount(L2GameClient client)
    {
        if(client.getState() == net.sf.l2j.gameserver.network.L2GameClient.GameClientState.CONNECTED)
            removeWaitingClient(client);
        else
        if(_accountsInGameServer.containsKey(client.getAccountName()))
            _accountsInGameServer.remove(client.getAccountName());
    }
}