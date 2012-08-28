package l2m.gameserver.model.entity.events.objects;

import java.io.Serializable;
import l2m.gameserver.model.entity.events.GlobalEvent;

public abstract interface InitableObject extends Serializable
{
  public abstract void initObject(GlobalEvent paramGlobalEvent);
}