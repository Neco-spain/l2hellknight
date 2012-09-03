package l2rt.loginserver;

import l2rt.Config;
import l2rt.Server;
import l2rt.config.ConfigSystem;
import l2rt.database.L2DatabaseFactory;
import l2rt.extensions.network.SelectorConfig;
import l2rt.extensions.network.SelectorThread;
import l2rt.gameserver.GameServer;
import l2rt.gameserver.taskmanager.MemoryWatchDog;
import l2rt.loginserver.gameservercon.GSConnection;
import l2rt.status.Status;
import l2rt.util.Log;
import l2rt.util.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.net.InetAddress;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class L2LoginServer
{
	protected static L2LoginServer _instance;
	private Logger _log = Logger.getLogger(L2LoginServer.class.getName());
	private GSConnection _gameServerListener;
	private SelectorThread<L2LoginClient> _selectorThread;
	public static Status statusServer;
	public LoginController loginController;

	//private GameServer gameServer;

	public static L2LoginServer getInstance()
	{
		return _instance;
	}

	public L2LoginServer()
	{
		Server.SERVER_MODE = Server.MODE_LOGINSERVER;
		//      Local Constants
		final String LOG_FOLDER = "log"; // Name of folder for log file
		final String LOG_NAME = "./config/log.ini"; // Name of log file

		/*** Main ***/
		// Create log folder
		File logFolder = new File("./", LOG_FOLDER);
		logFolder.mkdir();

		// Create input stream for log file -- or store file data into memory
		InputStream is = null;
		try
		{
			is = new FileInputStream(new File(LOG_NAME));
			LogManager.getLogManager().readConfiguration(is);
			is.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if(is != null)
					is.close();
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}

		// Load Config
		Config.load();
        ConfigSystem.load();
		if(Config.COMBO_MODE)
		{
			Server.SERVER_MODE = Server.MODE_COMBOSERVER;
			Log.InitGSLoggers();
			Config.load();
		}
		
		// Prepare Firewall
		if (Config.LOAD_FIREWALL)
			{
			LineNumberReader lnr = null;
			BufferedReader br = null;
			FileReader fr = null;
			try
			{
				File Data = new File("./config/iptables.txt");
				if (!Data.exists())
					return;
				fr = new FileReader(Data);
				br = new BufferedReader(fr);
				lnr = new LineNumberReader(br);
				
				Runtime r = Runtime.getRuntime();
				String line;
				while ((line = lnr.readLine()) != null)
				{
					if ((line.trim().length() == 0) || (line.startsWith("#"))) 
					{
						continue;
					}
					try
					{
						r.exec(line);
					}
					catch (Exception e1)
					{
						this._log.severe("pwLogin: [ERROR] Can't exec: " + line);
						this._log.severe("Reason: " + e1.getMessage());
					}
				}
				this._log.info("config/iptables.txt loaded.");
			}
			catch (Exception e2)
			{
				this._log.severe("pwLogin: [ERROR] Can't load config/iptables.txt");
				this._log.severe("Reason: " + e2.getMessage());
			}
			finally
			{
				try
				{
					if (fr != null)
						fr.close();
					if (br != null)
						br.close();
					if (lnr != null) 
					{
						lnr.close();
					}
				}
				catch (Exception e2)
				{
				}
			}
			}
		
		// Prepare Database
		try
		{
			L2DatabaseFactory.getInstance();
		}
		catch(SQLException e)
		{
			_log.severe("FATAL: Failed initializing database. Reason: " + e.getMessage());
			if(Config.LOGIN_DEBUG)
				e.printStackTrace();
			Server.exit(1, "FATAL: Failed initializing database. Reason: " + e.getMessage());
		}

		try
		{
			LoginController.load();
		}
		catch(GeneralSecurityException e)
		{
			_log.severe("FATAL: Failed initializing LoginController. Reason: " + e.getMessage());
			if(Config.LOGIN_DEBUG)
				e.printStackTrace();
			Server.exit(1, "FATAL: Failed initializing LoginController. Reason: " + e.getMessage());
		}

		try
		{
			GameServerTable.load();
		}
		catch(GeneralSecurityException e)
		{
			_log.severe("FATAL: Failed to load GameServerTable. Reason: " + e.getMessage());
			if(Config.LOGIN_DEBUG)
				e.printStackTrace();
			Server.exit(1, "FATAL: Failed to load GameServerTable. Reason: " + e.getMessage());
		}
		catch(SQLException e)
		{
			_log.severe("FATAL: Failed to load GameServerTable. Reason: " + e.getMessage());
			if(Config.LOGIN_DEBUG)
				e.printStackTrace();
			Server.exit(1, "FATAL: Failed to load GameServerTable. Reason: " + e.getMessage());
		}

		//this.loadBanFile();

		/* Accepting connections from players */
		Util.waitForFreePorts(Config.LOGIN_HOST, Config.PORT_LOGIN);
		InetAddress ad = null;
		try
		{
			ad = InetAddress.getByName(Config.LOGIN_HOST);
		}
		catch(Exception e)
		{}

		L2LoginPacketHandler loginPacketHandler = new L2LoginPacketHandler();
		SelectorHelper sh = new SelectorHelper();
		SelectorConfig<L2LoginClient> sc = new SelectorConfig<L2LoginClient>(sh);
		try
		{
			_selectorThread = new SelectorThread<L2LoginClient>(sc, loginPacketHandler, sh, sh, sh);
			_selectorThread.setAcceptFilter(sh);
		}
		catch(IOException e)
		{
			_log.severe("FATAL: Failed to open Selector. Reason: " + e.getMessage());
			if(Config.LOGIN_DEBUG)
				e.printStackTrace();
			Server.exit(1, "FATAL: Failed to open Selector. Reason: " + e.getMessage());
		}

		_gameServerListener = GSConnection.getInstance();
		_gameServerListener.start();
		_log.info("Listening for GameServers on " + Config.GAME_SERVER_LOGIN_HOST + ":" + Config.GAME_SERVER_LOGIN_PORT);

		if(Config.IS_LOGIN_TELNET_ENABLED)
			try
			{
				statusServer = new Status(Server.MODE_LOGINSERVER);
				statusServer.start();
			}
			catch(IOException e)
			{
				_log.severe("Failed to start the Telnet Server. Reason: " + e.getMessage());
				if(Config.LOGIN_DEBUG)
					e.printStackTrace();
			}
		else
			_log.info("LoginServer Telnet server is currently disabled.");

		try
		{
			SelectorThread.setAntiFlood(Config.ANTIFLOOD_ENABLE);
			SelectorThread.setAntiFloodSocketsConf(Config.MAX_UNHANDLED_SOCKETS_PER_IP, Config.UNHANDLED_SOCKET_MIN_TTL);
			_selectorThread.openServerSocket(ad, Config.PORT_LOGIN);
		}
		catch(IOException e)
		{
			_log.severe("FATAL: Failed to open server socket on " + ad + ":" + Config.PORT_LOGIN + ". Reason: " + e.getMessage());
			if(Config.LOGIN_DEBUG)
				e.printStackTrace();
			Server.exit(1, "FATAL: Failed to open server socket on " + ad + ":" + Config.PORT_LOGIN + ". Reason: " + e.getMessage());
		}
		_selectorThread.start();
		_log.info("Login Server ready on port " + Config.PORT_LOGIN);
		_log.info(IpManager.getInstance().getBannedCount() + " banned IPs defined");

		if(Config.COMBO_MODE)
			try
			{
				Util.waitForFreePorts(Config.GAMESERVER_HOSTNAME, Config.PORTS_GAME);

				if(Config.IS_TELNET_ENABLED)
				{
					Status _statusServer = new Status(Server.MODE_GAMESERVER);
					_statusServer.start();
				}
				else
					_log.info("GameServer Telnet server is currently disabled.");
				new GameServer();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}

		Shutdown.getInstance().startShutdownH(Config.LRESTART_TIME, true);

		Util.gc(3, 333);
		_log.info("Free memory " + MemoryWatchDog.getMemFreeMb() + " of " + MemoryWatchDog.getMemMaxMb());
	}

	public GSConnection getGameServerListener()
	{
		return _gameServerListener;
	}

	public boolean unblockIp(String ipAddress)
	{
		return loginController.ipBlocked(ipAddress);
	}

	public boolean setPassword(String account, String password)
	{
		return loginController.setPassword(account, password);
	}
}