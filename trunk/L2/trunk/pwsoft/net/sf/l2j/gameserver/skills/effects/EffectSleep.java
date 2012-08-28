package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.skills.Env;

final class EffectSleep extends L2Effect
{
  public EffectSleep(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.SLEEP;
  }

  public void onStart()
  {
    getEffected().startSleeping();
  }

  public void onExit()
  {
    getEffected().stopSleeping(this);
  }

  public boolean onActionTime()
  {
    getEffected().stopSleeping(this);

    return false;
  }
}