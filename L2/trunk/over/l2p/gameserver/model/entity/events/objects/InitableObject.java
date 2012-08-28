package l2p.gameserver.model.entity.events.objects;

import java.io.Serializable;
import l2p.gameserver.model.entity.events.GlobalEvent;

public abstract interface InitableObject extends Serializable
{
  public abstract void initObject(GlobalEvent paramGlobalEvent);
}