package l2m.gameserver.model.entity.events;

import l2m.gameserver.taskmanager.actionrunner.ActionWrapper;

public class EventWrapper extends ActionWrapper
{
  private final GlobalEvent _event;
  private final int _time;

  public EventWrapper(String name, GlobalEvent event, int time)
  {
    super(name);
    _event = event;
    _time = time;
  }

  public void runImpl0()
    throws Exception
  {
    _event.timeActions(_time);
  }
}