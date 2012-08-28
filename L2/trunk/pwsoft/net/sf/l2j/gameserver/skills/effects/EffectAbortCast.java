package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.skills.Env;

public class EffectAbortCast extends L2Effect
{
  public EffectAbortCast(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.REMOVE_TARGET;
  }

  public void onStart()
  {
    getEffected().abortAttack();
    getEffected().abortCast();
  }

  public void onExit()
  {
  }

  public boolean onActionTime()
  {
    return false;
  }
}