package l2p.gameserver.listener.actor;

import l2p.gameserver.listener.CharListener;
import l2p.gameserver.model.Creature;

public abstract interface OnKillListener extends CharListener
{
  public abstract void onKill(Creature paramCreature1, Creature paramCreature2);

  public abstract boolean ignorePetOrSummon();
}