package net.sf.l2j.gameserver.handler;

import java.util.Map;
import java.util.logging.Logger;
import javolution.util.FastMap;
import net.sf.l2j.Config;

public class UserCommandHandler
{
  private static Logger _log = Logger.getLogger(UserCommandHandler.class.getName());
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
      _datatable.put(new Integer(ids[i]), handler);
    }
  }

  public IUserCommandHandler getUserCommandHandler(int userCommand)
  {
    if (Config.DEBUG) _log.fine("getting handler for user command: " + userCommand);
    return (IUserCommandHandler)_datatable.get(new Integer(userCommand));
  }

  public int size()
  {
    return _datatable.size();
  }
}