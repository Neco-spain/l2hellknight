package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.network.serverpackets.ExRegenMax;
import net.sf.l2j.gameserver.skills.Env;

class EffectHealOverTime extends L2Effect
{
  public EffectHealOverTime(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.HEAL_OVER_TIME;
  }

  public void onStart()
  {
    if (getEffected().isPlayer())
      getEffected().sendPacket(new ExRegenMax(calc(), getCount() * getPeriod() / 1000, getPeriod() / 1000));
  }

  public boolean onActionTime()
  {
    if (getEffected().isDead()) {
      return false;
    }
    if (getEffected().isL2Door()) {
      return false;
    }

    getEffected().setCurrentHp(getEffected().getCurrentHp() + calc());

    return true;
  }
}