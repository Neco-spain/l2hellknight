package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.skills.Env;

final class EffectInvocation extends L2Effect
{
  public EffectInvocation(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.IMMOBILEUNTILATTACKED;
  }

  public void onStart()
  {
    getEffector().setIsParalyzed(true);
    getEffected().startAbnormalEffect(131072);
    getEffector().setIsImmobileUntilAttacked(true);
  }

  public void onExit()
  {
    getEffector().setIsParalyzed(false);
    getEffected().stopAbnormalEffect(131072);
    getEffector().setIsImmobileUntilAttacked(false);
  }

  public boolean onActionTime()
  {
    return false;
  }
}