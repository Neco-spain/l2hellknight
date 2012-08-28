package l2m.gameserver.model.entity.events.actions;

import l2m.gameserver.model.entity.events.EventAction;
import l2m.gameserver.model.entity.events.GlobalEvent;

public class AnnounceAction
  implements EventAction
{
  private int _id;

  public AnnounceAction(int id)
  {
    _id = id;
  }

  public void call(GlobalEvent event)
  {
    event.announce(_id);
  }
}