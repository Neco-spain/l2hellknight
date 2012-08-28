package l2p.gameserver.model.entity.events;

public abstract interface EventAction
{
  public abstract void call(GlobalEvent paramGlobalEvent);
}