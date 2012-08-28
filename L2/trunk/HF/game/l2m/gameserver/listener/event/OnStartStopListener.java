package l2m.gameserver.listener.event;

import l2m.gameserver.listener.EventListener;
import l2m.gameserver.model.entity.events.GlobalEvent;

public abstract interface OnStartStopListener extends EventListener
{
  public abstract void onStart(GlobalEvent paramGlobalEvent);

  public abstract void onStop(GlobalEvent paramGlobalEvent);
}