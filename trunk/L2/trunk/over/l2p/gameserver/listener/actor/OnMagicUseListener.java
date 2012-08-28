package l2p.gameserver.listener.actor;

import l2p.gameserver.listener.CharListener;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Skill;

public abstract interface OnMagicUseListener extends CharListener
{
  public abstract void onMagicUse(Creature paramCreature1, Skill paramSkill, Creature paramCreature2, boolean paramBoolean);
}