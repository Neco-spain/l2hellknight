package scripts.commands;

import java.util.Map;
import java.util.logging.Logger;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.util.log.AbstractLogger;
import scripts.commands.usercommandhandlers.ChannelCreate;
import scripts.commands.usercommandhandlers.ChannelDelete;
import scripts.commands.usercommandhandlers.ChannelLeave;
import scripts.commands.usercommandhandlers.ChannelListUpdate;
import scripts.commands.usercommandhandlers.ClanPenalty;
import scripts.commands.usercommandhandlers.ClanWarsList;
import scripts.commands.usercommandhandlers.DisMount;
import scripts.commands.usercommandhandlers.Escape;
import scripts.commands.usercommandhandlers.Loc;
import scripts.commands.usercommandhandlers.Mount;
import scripts.commands.usercommandhandlers.OlympiadStat;
import scripts.commands.usercommandhandlers.PartyInfo;
import scripts.commands.usercommandhandlers.Time;

public class UserCommandHandler
{
  private static Logger _log = AbstractLogger.getLogger(UserCommandHandler.class.getName());
  private static UserCommandHandler _instance;
  private Map<Integer, IUserCommandHandler> _datatable;

  public static UserCommandHandler getInstance()
  {
    if (_instance == null)
    {
      _instance = new UserCommandHandler();
    }
    return _instance;
  }

  private UserCommandHandler()
  {
    _datatable = new FastMap();
  }

  public void registerUserCommandHandler(IUserCommandHandler handler)
  {
    int[] ids = handler.getUserCommandList();
    for (int i = 0; i < ids.length; i++)
    {
      if (Config.DEBUG) _log.fine("Adding handler for user command " + ids[i]);
      _datatable.put(Integer.valueOf(ids[i]), handler);
    }
  }

  public IUserCommandHandler getUserCommandHandler(int userCommand)
  {
    if (Config.DEBUG) _log.fine("getting handler for user command: " + userCommand);
    registerUserCommandHandler(new ClanPenalty());
    registerUserCommandHandler(new ClanWarsList());
    registerUserCommandHandler(new DisMount());
    registerUserCommandHandler(new Escape());
    registerUserCommandHandler(new Loc());
    registerUserCommandHandler(new Mount());
    registerUserCommandHandler(new PartyInfo());
    registerUserCommandHandler(new Time());
    registerUserCommandHandler(new OlympiadStat());
    registerUserCommandHandler(new ChannelCreate());
    registerUserCommandHandler(new ChannelLeave());
    registerUserCommandHandler(new ChannelDelete());
    registerUserCommandHandler(new ChannelListUpdate());

    return (IUserCommandHandler)_datatable.get(Integer.valueOf(userCommand));
  }

  public int size()
  {
    return _datatable.size();
  }
}