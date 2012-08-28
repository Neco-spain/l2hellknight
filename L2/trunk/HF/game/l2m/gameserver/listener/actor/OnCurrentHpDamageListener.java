package l2m.gameserver.listener.actor;

import l2m.gameserver.listener.CharListener;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Skill;

public abstract interface OnCurrentHpDamageListener extends CharListener
{
  public abstract void onCurrentHpDamage(Creature paramCreature1, double paramDouble, Creature paramCreature2, Skill paramSkill);
}