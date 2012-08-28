package l2m.gameserver.model.entity.events.actions;

import l2m.gameserver.model.entity.events.EventAction;
import l2m.gameserver.model.entity.events.GlobalEvent;

public class ActiveDeactiveAction
  implements EventAction
{
  private final boolean _active;
  private final String _name;

  public ActiveDeactiveAction(boolean active, String name)
  {
    _active = active;
    _name = name;
  }

  public void call(GlobalEvent event)
  {
    event.zoneAction(_name, _active);
  }
}