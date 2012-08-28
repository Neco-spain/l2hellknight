package l2m.gameserver.listener.actor;

import l2m.gameserver.listener.CharListener;
import l2m.gameserver.model.Creature;

public abstract interface OnKillListener extends CharListener
{
  public abstract void onKill(Creature paramCreature1, Creature paramCreature2);

  public abstract boolean ignorePetOrSummon();
}