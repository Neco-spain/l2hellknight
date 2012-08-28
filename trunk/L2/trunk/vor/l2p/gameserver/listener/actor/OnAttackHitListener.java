package l2p.gameserver.listener.actor;

import l2p.gameserver.listener.CharListener;
import l2p.gameserver.model.Creature;

public abstract interface OnAttackHitListener extends CharListener
{
  public abstract void onAttackHit(Creature paramCreature1, Creature paramCreature2);
}