package l2m.gameserver.model.entity.events.actions;

import l2m.gameserver.model.entity.events.EventAction;
import l2m.gameserver.model.entity.events.GlobalEvent;

public class TeleportPlayersAction
  implements EventAction
{
  private String _name;

  public TeleportPlayersAction(String name)
  {
    _name = name;
  }

  public void call(GlobalEvent event)
  {
    event.teleportPlayers(_name);
  }
}