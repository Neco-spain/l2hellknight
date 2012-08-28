package net.sf.l2j.loginserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.Map;
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
  public static final int PROTOCOL_REV = 258;
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
    Server.serverMode = 2;
    String LOG_FOLDER = "logs";
    String LOG_NAME = "./config/log.cfg";
    File logFolder = new File(Config.DATAPACK_ROOT, "logs");
    logFolder.mkdir();
    InputStream is = null;
    try
    {
      is = new FileInputStream(new File("./config/log.cfg"));
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
      _log.severe(new StringBuilder().append("FATAL: Failed initializing database. Reason: ").append(e.getMessage()).toString());
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
      _log.severe(new StringBuilder().append("FATAL: Failed initializing LoginController. Reason: ").append(e.getMessage()).toString());
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
      _log.severe(new StringBuilder().append("FATAL: Failed to load GameServerTable. Reason: ").append(e.getMessage()).toString());
      if (Config.DEVELOPER)
      {
        e.printStackTrace();
      }
      System.exit(1);
    }
    catch (SQLException e)
    {
      _log.severe(new StringBuilder().append("FATAL: Failed to load GameServerTable. Reason: ").append(e.getMessage()).toString());
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
        _log.severe(new StringBuilder().append("WARNING: The LoginServer bind address is invalid, using all avaliable IPs. Reason: ").append(e1.getMessage()).toString());
        if (Config.DEVELOPER)
        {
          e1.printStackTrace();
        }
      }
    }

    L2LoginPacketHandler loginPacketHandler = new L2LoginPacketHandler();
    SelectorHelper sh = new SelectorHelper();
    SelectorConfig ssc = new SelectorConfig(null, null, sh, loginPacketHandler);
    try
    {
      _selectorThread = new SelectorThread(ssc, sh, sh, sh);
      _selectorThread.setAcceptFilter(sh);
    }
    catch (IOException e)
    {
      _log.severe(new StringBuilder().append("FATAL: Failed to open Selector. Reason: ").append(e.getMessage()).toString());
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
      _log.info(new StringBuilder().append("Listening for GameServers on ").append(Config.GAME_SERVER_LOGIN_HOST).append(":").append(Config.GAME_SERVER_LOGIN_PORT).toString());
    }
    catch (IOException e)
    {
      _log.severe(new StringBuilder().append("FATAL: Failed to start the Game Server Listener. Reason: ").append(e.getMessage()).toString());
      if (Config.DEVELOPER)
      {
        e.printStackTrace();
      }
      System.exit(1);
    }

    if (Config.IS_TELNET_ENABLED)
    {
      try
      {
        _statusServer = new Status(Server.serverMode);
        _statusServer.start();
      }
      catch (IOException e)
      {
        _log.severe(new StringBuilder().append("Failed to start the Telnet Server. Reason: ").append(e.getMessage()).toString());
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
      _log.severe(new StringBuilder().append("FATAL: Failed to open server socket. Reason: ").append(e.getMessage()).toString());
      if (Config.DEVELOPER)
      {
        e.printStackTrace();
      }
      System.exit(1);
    }
    _selectorThread.start();
    _log.info(new StringBuilder().append("Login Server ready on ").append(bindAddress == null ? "*" : bindAddress.getHostAddress()).append(":").append(Config.PORT_LOGIN).toString());
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
    if ((bannedFile.exists()) && (bannedFile.isFile()))
    {
      FileInputStream fis = null;
      LineNumberReader reader = null;
      try
      {
        fis = new FileInputStream(bannedFile);
        reader = new LineNumberReader(new InputStreamReader(fis));
      }
      catch (IOException e)
      {
        _log.warning(new StringBuilder().append("Failed to load banned IPs file (").append(bannedFile.getName()).append(") for reading. Reason: ").append(e.getMessage()).toString());
        if (Config.DEVELOPER)
        {
          e.printStackTrace();
        }
        return;
      }
      try
      {
        String line;
        while ((line = reader.readLine()) != null)
        {
          line = line.trim();
          if ((line.length() <= 0) || (line.charAt(0) == '#'))
            continue;
          String[] parts = line.split("#");
          line = parts[0];

          parts = line.split(" ");

          String address = parts[0];

          long duration = 0L;

          if (parts.length > 1)
          {
            try
            {
              duration = Long.parseLong(parts[1]);
            }
            catch (NumberFormatException e)
            {
              _log.warning(new StringBuilder().append("Skipped: Incorrect ban duration (").append(parts[1]).append(") on (").append(bannedFile.getName()).append("). Line: ").append(reader.getLineNumber()).toString());
            }continue;
          }

          try
          {
            LoginController.getInstance().addBanForAddress(address, duration);
          }
          catch (UnknownHostException e)
          {
            _log.warning(new StringBuilder().append("Skipped: Invalid address (").append(parts[0]).append(") on (").append(bannedFile.getName()).append("). Line: ").append(reader.getLineNumber()).toString());
          }
        }

        reader.close();
      }
      catch (IOException e1)
      {
        _log.warning(new StringBuilder().append("Error while reading the bans file (").append(bannedFile.getName()).append("). Details: ").append(e.getMessage()).toString());
        if (Config.DEVELOPER)
        {
          e.printStackTrace();
        }
      }
      finally
      {
        try
        {
          fis.close();
        }
        catch (IOException e1)
        {
          e1.printStackTrace();
        }
      }
      _log.config(new StringBuilder().append("Loaded ").append(LoginController.getInstance().getBannedIps().size()).append(" IP Bans.").toString());
    }
    else
    {
      _log.config(new StringBuilder().append("IP Bans file (").append(bannedFile.getName()).append(") is missing or is a directory, skipped.").toString());
    }
  }

  public void shutdown(boolean restart)
  {
    Runtime.getRuntime().exit(restart ? 2 : 0);
  }
}