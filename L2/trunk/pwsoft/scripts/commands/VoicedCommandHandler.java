package scripts.commands;

import java.util.Map;
import java.util.logging.Logger;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.util.log.AbstractLogger;
import scripts.commands.voicedcommandhandlers.AdenaCol;
import scripts.commands.voicedcommandhandlers.BlockBuff;
import scripts.commands.voicedcommandhandlers.Events;
import scripts.commands.voicedcommandhandlers.Menu;
import scripts.commands.voicedcommandhandlers.ModBanChat;
import scripts.commands.voicedcommandhandlers.ModHelp;
import scripts.commands.voicedcommandhandlers.ModKick;
import scripts.commands.voicedcommandhandlers.ModSpecial;
import scripts.commands.voicedcommandhandlers.ModTitle;
import scripts.commands.voicedcommandhandlers.Offline;
import scripts.commands.voicedcommandhandlers.Security;
import scripts.commands.voicedcommandhandlers.Silence;
import scripts.commands.voicedcommandhandlers.Wedding;

public class VoicedCommandHandler
{
  private static Logger _log = AbstractLogger.getLogger(VoicedCommandHandler.class.getName());
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
    if (Config.CMD_ADENA_COL)
      registerVoicedCommandHandler(new AdenaCol());
    if (Config.CMD_EVENTS)
      registerVoicedCommandHandler(new Events());
    if (Config.CMD_MENU)
      registerVoicedCommandHandler(new Menu());
    if (Config.ALT_ALLOW_OFFLINE_TRADE)
      registerVoicedCommandHandler(new Offline());
    if (Config.L2JMOD_ALLOW_WEDDING)
      registerVoicedCommandHandler(new Wedding());
    registerVoicedCommandHandler(new Silence());
    registerVoicedCommandHandler(new BlockBuff());
    registerVoicedCommandHandler(new Security());

    registerVoicedCommandHandler(new ModBanChat());
    registerVoicedCommandHandler(new ModKick());
    registerVoicedCommandHandler(new ModTitle());
    registerVoicedCommandHandler(new ModHelp());
    registerVoicedCommandHandler(new ModSpecial());
    _log.config("VoicedCommandHandler: Loaded " + _datatable.size() + " handlers.");
  }

  public void registerVoicedCommandHandler(IVoicedCommandHandler handler)
  {
    String[] ids = handler.getVoicedCommandList();
    for (int i = 0; i < ids.length; i++)
    {
      _datatable.put(ids[i], handler);
    }
  }

  public IVoicedCommandHandler getVoicedCommandHandler(String voicedCommand)
  {
    String command = voicedCommand;
    if (voicedCommand.indexOf(" ") != -1) {
      command = voicedCommand.substring(0, voicedCommand.indexOf(" "));
    }

    return (IVoicedCommandHandler)_datatable.get(command);
  }

  public int size()
  {
    return _datatable.size();
  }
}