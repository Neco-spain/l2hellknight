package l2p.gameserver.listener.actor;

import l2p.gameserver.listener.CharListener;
import l2p.gameserver.model.Creature;

public abstract interface OnAttackListener extends CharListener
{
  public abstract void onAttack(Creature paramCreature1, Creature paramCreature2);
}