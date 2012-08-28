package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.skills.Env;

final class EffectHalishaPara extends L2Effect
{
  public EffectHalishaPara(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.PARALYZE;
  }

  public void onStart()
  {
    getEffected().startAbnormalEffect(131072);
    getEffected().setIsParalyzed(true);
  }

  public void onExit()
  {
    getEffected().stopAbnormalEffect(131072);
    getEffected().setIsParalyzed(false);
  }

  public boolean onActionTime()
  {
    return false;
  }
}