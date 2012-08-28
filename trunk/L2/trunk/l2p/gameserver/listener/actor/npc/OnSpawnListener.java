package l2p.gameserver.listener.actor.npc;

import l2p.gameserver.listener.NpcListener;
import l2p.gameserver.model.instances.NpcInstance;

public abstract interface OnSpawnListener extends NpcListener
{
  public abstract void onSpawn(NpcInstance paramNpcInstance);
}