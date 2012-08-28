package l2m.gameserver.listener.actor;

import l2m.gameserver.listener.CharListener;
import l2m.gameserver.model.Creature;

public abstract interface OnAttackHitListener extends CharListener
{
  public abstract void onAttackHit(Creature paramCreature1, Creature paramCreature2);
}