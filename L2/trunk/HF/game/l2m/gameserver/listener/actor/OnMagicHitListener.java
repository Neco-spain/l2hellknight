package l2m.gameserver.listener.actor;

import l2m.gameserver.listener.CharListener;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Skill;

public abstract interface OnMagicHitListener extends CharListener
{
  public abstract void onMagicHit(Creature paramCreature1, Skill paramSkill, Creature paramCreature2);
}