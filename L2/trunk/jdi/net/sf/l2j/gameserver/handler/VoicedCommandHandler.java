package net.sf.l2j.gameserver.handler;

import java.util.Map;
import java.util.logging.Logger;
import javolution.util.FastMap;
import net.sf.l2j.Config;

public class VoicedCommandHandler
{
  private static Logger _log = Logger.getLogger(ItemHandler.class.getName());
  private static VoicedCommandHandler _instance;
  private Map<String, IVoicedCommandHandler> _datatable;

  public static VoicedCommandHandler getInstance()
  {
    if (_instance == null)
    {
      _instance = new VoicedCommandHandler();
    }
    return _instance;
  }

  private VoicedCommandHandler()
  {
    _datatable = new FastMap();
  }

  public void registerVoicedCommandHandler(IVoicedCommandHandler handler)
  {
    String[] ids = handler.getVoicedCommandList();
    for (int i = 0; i < ids.length; i++)
    {
      if (Config.DEBUG) _log.fine(new StringBuilder().append("Adding handler for command ").append(ids[i]).toString());
      _datatable.put(ids[i], handler);
    }
  }

  public IVoicedCommandHandler getVoicedCommandHandler(String voicedCommand)
  {
    String command = voicedCommand;
    if (voicedCommand.indexOf(" ") != -1) {
      command = voicedCommand.substring(0, voicedCommand.indexOf(" "));
    }
    if (Config.DEBUG) {
      _log.fine(new StringBuilder().append("getting handler for command: ").append(command).append(" -> ").append(_datatable.get(command) != null).toString());
    }
    return (IVoicedCommandHandler)_datatable.get(command);
  }

  public int size()
  {
    return _datatable.size();
  }
}