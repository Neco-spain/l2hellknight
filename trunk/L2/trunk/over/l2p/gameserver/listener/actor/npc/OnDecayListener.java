package l2p.gameserver.listener.actor.npc;

import l2p.gameserver.listener.NpcListener;
import l2p.gameserver.model.instances.NpcInstance;

public abstract interface OnDecayListener extends NpcListener
{
  public abstract void onDecay(NpcInstance paramNpcInstance);
}