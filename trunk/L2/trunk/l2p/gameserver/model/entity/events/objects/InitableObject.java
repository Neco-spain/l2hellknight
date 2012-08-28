package l2p.gameserver.model.entity.events.objects;

import java.io.Serializable;
import l2p.gameserver.model.entity.events.GlobalEvent;

public abstract interface InitableObject extends Serializable
{
  public static final long serialVersionUID = 1L;

  public abstract void initObject(GlobalEvent paramGlobalEvent);
}