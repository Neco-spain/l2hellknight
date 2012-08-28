package l2p.gameserver.model.entity.events.actions;

import l2p.gameserver.model.entity.events.EventAction;
import l2p.gameserver.model.entity.events.GlobalEvent;

public class OpenCloseAction
  implements EventAction
{
  private final boolean _open;
  private final String _name;

  public OpenCloseAction(boolean open, String name)
  {
    _open = open;
    _name = name;
  }

  public void call(GlobalEvent event)
  {
    event.doorAction(_name, _open);
  }
}