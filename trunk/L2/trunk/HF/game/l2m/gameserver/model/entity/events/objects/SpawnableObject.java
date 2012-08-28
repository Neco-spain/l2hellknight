package l2m.gameserver.model.entity.events.objects;

import java.io.Serializable;
import l2m.gameserver.model.entity.events.GlobalEvent;

public abstract interface SpawnableObject extends Serializable
{
  public abstract void spawnObject(GlobalEvent paramGlobalEvent);

  public abstract void despawnObject(GlobalEvent paramGlobalEvent);

  public abstract void refreshObject(GlobalEvent paramGlobalEvent);
}