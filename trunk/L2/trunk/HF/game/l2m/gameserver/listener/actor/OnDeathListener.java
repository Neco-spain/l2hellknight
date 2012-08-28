package l2m.gameserver.listener.actor;

import l2m.gameserver.listener.CharListener;
import l2m.gameserver.model.Creature;

public abstract interface OnDeathListener extends CharListener
{
  public abstract void onDeath(Creature paramCreature1, Creature paramCreature2);
}