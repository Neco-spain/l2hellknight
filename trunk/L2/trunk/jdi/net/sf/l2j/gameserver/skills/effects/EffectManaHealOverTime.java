package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.skills.Env;

class EffectManaHealOverTime extends L2Effect
{
  public EffectManaHealOverTime(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.MANA_HEAL_OVER_TIME;
  }

  public boolean onActionTime()
  {
    if (getEffected().isDead()) {
      return false;
    }
    double mp = getEffected().getCurrentMp();
    double maxmp = getEffected().getMaxMp();
    mp += calc();
    if (mp > maxmp)
    {
      mp = maxmp;
    }
    getEffected().setCurrentMp(mp);
    StatusUpdate sump = new StatusUpdate(getEffected().getObjectId());
    sump.addAttribute(11, (int)mp);
    getEffected().sendPacket(sump);
    return true;
  }
}