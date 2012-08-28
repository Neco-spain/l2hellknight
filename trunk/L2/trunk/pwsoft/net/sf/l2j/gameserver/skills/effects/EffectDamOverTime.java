package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillTargetType;
import net.sf.l2j.gameserver.skills.Env;

class EffectDamOverTime extends L2Effect
{
  private static boolean _first = false;

  public EffectDamOverTime(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.DMG_OVER_TIME;
  }

  public boolean onActionTime()
  {
    if (getEffected().isDead()) {
      return false;
    }
    double damage = calc();

    if (damage >= getEffected().getCurrentHp())
    {
      if (getSkill().isToggle())
      {
        getEffected().sendPacket(Static.SKILL_REMOVED_DUE_LACK_HP);
        return false;
      }

      if (getSkill().getId() != 4082) damage = getEffected().getCurrentHp() - 1.0D;
    }

    boolean awake = (!(getEffected() instanceof L2Attackable)) && ((getSkill().getTargetType() != L2Skill.SkillTargetType.TARGET_SELF) || (!getSkill().isToggle()));

    getEffected().reduceCurrentHp(damage, getEffector(), awake);
    return true;
  }
}