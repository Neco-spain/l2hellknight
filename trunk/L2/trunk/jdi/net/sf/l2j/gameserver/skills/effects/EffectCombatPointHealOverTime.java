package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.skills.Env;

class EffectCombatPointHealOverTime extends L2Effect
{
  public EffectCombatPointHealOverTime(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.COMBAT_POINT_HEAL_OVER_TIME;
  }

  public boolean onActionTime()
  {
    if (getEffected().isDead()) {
      return false;
    }
    double cp = getEffected().getCurrentCp();
    double maxcp = getEffected().getMaxCp();
    cp += calc();
    if (cp > maxcp)
    {
      cp = maxcp;
    }
    getEffected().setCurrentCp(cp);
    StatusUpdate sump = new StatusUpdate(getEffected().getObjectId());
    sump.addAttribute(33, (int)cp);
    getEffected().sendPacket(sump);
    return true;
  }
}