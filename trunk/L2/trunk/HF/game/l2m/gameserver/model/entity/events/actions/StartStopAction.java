package l2m.gameserver.model.entity.events.actions;

import l2m.gameserver.model.entity.events.EventAction;
import l2m.gameserver.model.entity.events.GlobalEvent;

public class StartStopAction
  implements EventAction
{
  public static final String EVENT = "event";
  private final String _name;
  private final boolean _start;

  public StartStopAction(String name, boolean start)
  {
    _name = name;
    _start = start;
  }

  public void call(GlobalEvent event)
  {
    event.action(_name, _start);
  }
}