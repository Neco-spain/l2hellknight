package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.skills.Env;

final class EffectRoot extends L2Effect
{
  public EffectRoot(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.ROOT;
  }

  public void onStart()
  {
    getEffected().startRooted();
  }

  public void onExit()
  {
    getEffected().stopRooting(this);
  }

  public boolean onActionTime()
  {
    return false;
  }
}