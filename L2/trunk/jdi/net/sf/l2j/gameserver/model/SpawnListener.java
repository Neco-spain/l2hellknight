package net.sf.l2j.gameserver.model;

import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;

public abstract interface SpawnListener
{
  public abstract void npcSpawned(L2NpcInstance paramL2NpcInstance);
}