//L2DDT
package net.sf.l2j.loginserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.Server;
import net.sf.l2j.status.Status;

import org.mmocore.network.SelectorConfig;
import org.mmocore.network.SelectorThread;

public class L2LoginServer
{
	public static final int PROTOCOL_REV = 0x0102;

	private static L2LoginServer _instance;
	private Logger _log = Logger.getLogger(L2LoginServer.class.getName());
	private GameServerListener _gameServerListener;
	private SelectorThread<L2LoginClient> _selectorThread;
	private Status _statusServer;

	public static void main(String[] args)
	{
		_instance = new L2LoginServer();
	}

	public static L2LoginServer getInstance()
	{
		return _instance;
	}

	public L2LoginServer()
	{
		Server.serverMode = Server.MODE_LOGINSERVER;
		final String LOG_FOLDER = "logs";
		final String LOG_NAME   = "./config/log.cfg";
		File logFolder = new File(Config.DATAPACK_ROOT, LOG_FOLDER);
		logFolder.mkdir();
		InputStream is = null;
		try
		{
			is = new FileInputStream(new File(LOG_NAME));
			LogManager.getLogManager().readConfiguration(is);
			is.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (is != null)
				{
					is.close();
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		Config.load();
		try
		{
			L2DatabaseFactory.getInstance();
		}
		catch (SQLException e)
		{
			_log.severe("FATAL: Failed initializing database. Reason: "+e.getMessage());
			if (Config.DEVELOPER)
			{
				e.printStackTrace();
			}
			System.exit(1);
		}

		try
		{
			LoginController.load();
		}
		catch (GeneralSecurityException e)
		{
			_log.severe("FATAL: Failed initializing LoginController. Reason: "+e.getMessage());
			if (Config.DEVELOPER)
			{
				e.printStackTrace();
			}
			System.exit(1);
		}

		try
		{
			GameServerTable.load();
		}
		catch (GeneralSecurityException e)
		{
			_log.severe("FATAL: Failed to load GameServerTable. Reason: "+e.getMessage());
			if (Config.DEVELOPER)
			{
				e.printStackTrace();
			}
			System.exit(1);
		}
		catch (SQLException e)
		{
			_log.severe("FATAL: Failed to load GameServerTable. Reason: "+e.getMessage());
			if (Config.DEVELOPER)
			{
				e.printStackTrace();
			}
			System.exit(1);
		}

		loadBanFile();

		InetAddress bindAddress = null;
		if (!Config.LOGIN_BIND_ADDRESS.equals("*"))
		{
			try
			{
				bindAddress = InetAddress.getByName(Config.LOGIN_BIND_ADDRESS);
			}
			catch (UnknownHostException e1)
			{
				_log.severe("WARNING: The LoginServer bind address is invalid, using all avaliable IPs. Reason: "+e1.getMessage());
				if (Config.DEVELOPER)
				{
					e1.printStackTrace();
				}
			}
		}

		L2LoginPacketHandler loginPacketHandler = new L2LoginPacketHandler();
		SelectorHelper sh = new SelectorHelper();
		SelectorConfig<L2LoginClient> ssc = new SelectorConfig<L2LoginClient>(null, null, sh, loginPacketHandler);
		try
		{
			_selectorThread = new SelectorThread<L2LoginClient>(ssc, sh, sh, sh);
			_selectorThread.setAcceptFilter(sh);
		}
		catch (IOException e)
		{
			_log.severe("FATAL: Failed to open Selector. Reason: "+e.getMessage());
			if (Config.DEVELOPER)
			{
				e.printStackTrace();
			}
			System.exit(1);
		}

		try
		{
			_gameServerListener = new GameServerListener();
			_gameServerListener.start();
			_log.info("Listening for GameServers on "+Config.GAME_SERVER_LOGIN_HOST+":"+Config.GAME_SERVER_LOGIN_PORT);
		}
		catch (IOException e)
		{
			_log.severe("FATAL: Failed to start the Game Server Listener. Reason: "+e.getMessage());
			if (Config.DEVELOPER)
			{
				e.printStackTrace();
			}
			System.exit(1);
		}

		if ( Config.IS_TELNET_ENABLED )
		{
			try
			{
				_statusServer = new Status(Server.serverMode);
				_statusServer.start();
			}
			catch (IOException e)
			{
				_log.severe("Failed to start the Telnet Server. Reason: "+e.getMessage());
				if (Config.DEVELOPER)
				{
					e.printStackTrace();
				}
			}
		}
		else
		{
			_log.info("Telnet server is currently disabled.");
		}

		try
		{
			_selectorThread.openServerSocket(bindAddress, Config.PORT_LOGIN);
		}
		catch (IOException e)
		{
			_log.severe("FATAL: Failed to open server socket. Reason: " + e.getMessage());
			if (Config.DEVELOPER)
			{
				e.printStackTrace();
			}
			System.exit(1);
		}
		_selectorThread.start();
		_log.info("Login Server ready on "+(bindAddress == null ? "*" : bindAddress.getHostAddress())+":"+Config.PORT_LOGIN);
	}

	public Status getStatusServer()
	{
		return _statusServer;
	}

	public GameServerListener getGameServerListener()
	{
		return _gameServerListener;
	}

	private void loadBanFile()
	{
		File bannedFile = new File("./config/banned_ip.ini");
		if (bannedFile.exists() && bannedFile.isFile())
		{
			FileInputStream fis = null;
			try
			{
				fis = new FileInputStream(bannedFile);
			}
			catch (FileNotFoundException e)
			{
				_log.warning("Failed to load banned IPs file ("+bannedFile.getName()+") for reading. Reason: "+e.getMessage());
				if (Config.DEVELOPER)
				{
					e.printStackTrace();
				}
				return;
			}

			LineNumberReader reader = new LineNumberReader(new InputStreamReader(fis));

			String line;
			String[] parts;
			try
			{

				while ((line = reader.readLine()) != null)
				{
					line = line.trim();
					if (line.length() > 0 && line.charAt(0) != '#')
					{
						parts = line.split("#");
						line = parts[0];

						parts = line.split(" ");

						String address = parts[0];

						long duration = 0;

						if (parts.length > 1)
						{
							try
							{
								duration = Long.parseLong(parts[1]);
							}
							catch (NumberFormatException e)
							{
								_log.warning("Skipped: Incorrect ban duration ("+parts[1]+") on ("+bannedFile.getName()+"). Line: "+reader.getLineNumber());
								continue;
							}
						}

						try
						{
							LoginController.getInstance().addBanForAddress(address, duration);
						}
						catch (UnknownHostException e)
						{
							_log.warning("Skipped: Invalid address ("+parts[0]+") on ("+bannedFile.getName()+"). Line: "+reader.getLineNumber());
						}
					}
				}
			}
			catch (IOException e)
			{
				_log.warning("Error while reading the bans file ("+bannedFile.getName()+"). Details: "+e.getMessage());
				if (Config.DEVELOPER)
				{
					e.printStackTrace();
				}
			}
			_log.config("Loaded "+LoginController.getInstance().getBannedIps().size()+" IP Bans.");
		}
		else
		{
			_log.config("IP Bans file ("+bannedFile.getName()+") is missing or is a directory, skipped.");
		}
	}

	public void shutdown(boolean restart)
	{
		Runtime.getRuntime().exit(restart ? 2 : 0);
	}
}
