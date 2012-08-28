package l2m.gameserver.listener.actor;

import l2m.gameserver.listener.CharListener;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Skill;

public abstract interface OnMagicUseListener extends CharListener
{
  public abstract void onMagicUse(Creature paramCreature1, Skill paramSkill, Creature paramCreature2, boolean paramBoolean);
}