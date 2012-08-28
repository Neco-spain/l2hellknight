package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.skills.Env;

class EffectManaDamOverTime extends L2Effect
{
  public EffectManaDamOverTime(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.MANA_DMG_OVER_TIME;
  }

  public boolean onActionTime()
  {
    if (getEffected().isDead()) {
      return false;
    }
    double manaDam = calc();

    if (manaDam > getEffected().getCurrentMp())
    {
      if (getSkill().isToggle())
      {
        getEffected().sendPacket(Static.SKILL_REMOVED_DUE_LACK_MP);
        return false;
      }
    }

    getEffected().reduceCurrentMp(manaDam);
    return true;
  }
}