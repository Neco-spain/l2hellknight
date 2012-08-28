package l2p.gameserver.listener.actor;

import l2p.gameserver.listener.CharListener;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Skill;

public abstract interface OnCurrentHpDamageListener extends CharListener
{
  public abstract void onCurrentHpDamage(Creature paramCreature1, double paramDouble, Creature paramCreature2, Skill paramSkill);
}