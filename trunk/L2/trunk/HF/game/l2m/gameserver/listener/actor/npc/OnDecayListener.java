package l2m.gameserver.listener.actor.npc;

import l2m.gameserver.listener.NpcListener;
import l2m.gameserver.model.instances.NpcInstance;

public abstract interface OnDecayListener extends NpcListener
{
  public abstract void onDecay(NpcInstance paramNpcInstance);
}