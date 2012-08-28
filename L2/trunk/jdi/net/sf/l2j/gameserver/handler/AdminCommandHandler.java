package net.sf.l2j.gameserver.handler;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class AdminCommandHandler
{
  private static Logger _log = Logger.getLogger(AdminCommandHandler.class.getName());
  private static AdminCommandHandler _instance;
  private Map<String, IAdminCommandHandler> _datatable;
  private static Logger _priviLog = Logger.getLogger("AltPrivilegesAdmin");
  private static FastMap<String, Integer> _privileges;

  public static AdminCommandHandler getInstance()
  {
    if (_instance == null)
    {
      _instance = new AdminCommandHandler();
    }
    return _instance;
  }

  private AdminCommandHandler()
  {
    _datatable = new FastMap();
  }

  public void registerAdminCommandHandler(IAdminCommandHandler handler)
  {
    String[] ids = handler.getAdminCommandList();
    for (int i = 0; i < ids.length; i++)
    {
      if (Config.DEBUG) _log.fine(new StringBuilder().append("Adding handler for command ").append(ids[i]).toString());
      _datatable.put(ids[i], handler);
    }
  }

  public IAdminCommandHandler getAdminCommandHandler(String adminCommand)
  {
    String command = adminCommand;
    if (adminCommand.indexOf(" ") != -1) {
      command = adminCommand.substring(0, adminCommand.indexOf(" "));
    }
    if (Config.DEBUG) {
      _log.fine(new StringBuilder().append("getting handler for command: ").append(command).append(" -> ").append(_datatable.get(command) != null).toString());
    }
    return (IAdminCommandHandler)_datatable.get(command);
  }

  public int size()
  {
    return _datatable.size();
  }

  public final boolean checkPrivileges(L2PcInstance player, String adminCommand)
  {
    if (!player.isGM()) {
      return false;
    }

    if ((!Config.ALT_PRIVILEGES_ADMIN) || (Config.EVERYBODY_HAS_ADMIN_RIGHTS)) {
      return true;
    }
    if (_privileges == null) {
      _privileges = new FastMap();
    }
    String command = adminCommand;
    if (adminCommand.indexOf(" ") != -1) {
      command = adminCommand.substring(0, adminCommand.indexOf(" "));
    }

    if (!_datatable.containsKey(command)) {
      return false;
    }
    int requireLevel = 0;

    if (!_privileges.containsKey(command))
    {
      boolean isLoaded = false;
      try
      {
        Properties Settings = new Properties();
        InputStream is = new FileInputStream("./config/command-privileges.ini");
        Settings.load(is);
        is.close();

        String stringLevel = Settings.getProperty(command);

        if (stringLevel != null)
        {
          isLoaded = true;
          requireLevel = Integer.parseInt(stringLevel);
        }
      }
      catch (Exception e)
      {
      }
      if (!isLoaded)
      {
        if (Config.ALT_PRIVILEGES_SECURE_CHECK)
        {
          _priviLog.info(new StringBuilder().append("The command '").append(command).append("' haven't got a entry in the configuration file. The command cannot be executed!!").toString());
          return false;
        }

        requireLevel = Config.ALT_PRIVILEGES_DEFAULT_LEVEL;
      }

      _privileges.put(command, Integer.valueOf(requireLevel));
    }
    else
    {
      requireLevel = ((Integer)_privileges.get(command)).intValue();
    }

    if (player.getAccessLevel() < requireLevel)
    {
      _priviLog.warning(new StringBuilder().append("<GM>").append(player.getName()).append(": have not access level to execute the command '").append(command).append("'").toString());
      return false;
    }

    return true;
  }
}