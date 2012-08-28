package l2p.gameserver.listener.event;

import l2p.gameserver.listener.EventListener;
import l2p.gameserver.model.entity.events.GlobalEvent;

public abstract interface OnStartStopListener extends EventListener
{
  public abstract void onStart(GlobalEvent paramGlobalEvent);

  public abstract void onStop(GlobalEvent paramGlobalEvent);
}