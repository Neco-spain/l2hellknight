package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.skills.Env;

final class EffectParalyze extends L2Effect
{
  public EffectParalyze(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.PARALYZE;
  }

  public boolean onStart()
  {
    getEffected().startAbnormalEffect(1024);
    getEffected().setIsParalyzed(true);
    return true;
  }

  public void onExit()
  {
    getEffected().stopAbnormalEffect(1024);
    getEffected().setIsParalyzed(false);
  }

  public boolean onActionTime()
  {
    return false;
  }
}