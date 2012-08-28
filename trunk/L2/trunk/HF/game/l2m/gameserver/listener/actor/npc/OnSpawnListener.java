package l2m.gameserver.listener.actor.npc;

import l2m.gameserver.listener.NpcListener;
import l2m.gameserver.model.instances.NpcInstance;

public abstract interface OnSpawnListener extends NpcListener
{
  public abstract void onSpawn(NpcInstance paramNpcInstance);
}