package l2p.gameserver.handler.usercommands;

import gnu.trove.TIntObjectHashMap;
import l2p.commons.data.xml.AbstractHolder;
import l2p.gameserver.handler.usercommands.impl.ClanPenalty;
import l2p.gameserver.handler.usercommands.impl.ClanWarsList;
import l2p.gameserver.handler.usercommands.impl.CommandChannel;
import l2p.gameserver.handler.usercommands.impl.Escape;
import l2p.gameserver.handler.usercommands.impl.InstanceZone;
import l2p.gameserver.handler.usercommands.impl.Loc;
import l2p.gameserver.handler.usercommands.impl.MyBirthday;
import l2p.gameserver.handler.usercommands.impl.OlympiadStat;
import l2p.gameserver.handler.usercommands.impl.PartyInfo;
import l2p.gameserver.handler.usercommands.impl.Time;

public class UserCommandHandler extends AbstractHolder
{
  private static final UserCommandHandler _instance = new UserCommandHandler();

  private TIntObjectHashMap<IUserCommandHandler> _datatable = new TIntObjectHashMap();

  public static UserCommandHandler getInstance()
  {
    return _instance;
  }

  private UserCommandHandler()
  {
    registerUserCommandHandler(new ClanWarsList());
    registerUserCommandHandler(new ClanPenalty());
    registerUserCommandHandler(new CommandChannel());
    registerUserCommandHandler(new Escape());
    registerUserCommandHandler(new Loc());
    registerUserCommandHandler(new MyBirthday());
    registerUserCommandHandler(new OlympiadStat());
    registerUserCommandHandler(new PartyInfo());
    registerUserCommandHandler(new InstanceZone());
    registerUserCommandHandler(new Time());
  }

  public void registerUserCommandHandler(IUserCommandHandler handler)
  {
    int[] ids = handler.getUserCommandList();
    for (int element : ids)
      _datatable.put(element, handler);
  }

  public IUserCommandHandler getUserCommandHandler(int userCommand)
  {
    return (IUserCommandHandler)_datatable.get(userCommand);
  }

  public int size()
  {
    return _datatable.size();
  }

  public void clear()
  {
    _datatable.clear();
  }
}