package l2m.gameserver.model.entity.events.actions;

import l2m.gameserver.model.entity.events.EventAction;
import l2m.gameserver.model.entity.events.GlobalEvent;

public class RefreshAction
  implements EventAction
{
  private final String _name;

  public RefreshAction(String name)
  {
    _name = name;
  }

  public void call(GlobalEvent event)
  {
    event.refreshAction(_name);
  }
}