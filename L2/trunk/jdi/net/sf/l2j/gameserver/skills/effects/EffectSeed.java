package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.skills.Env;

public final class EffectSeed extends L2Effect
{
  private int _power = 1;

  public EffectSeed(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.SEED;
  }

  public boolean onActionTime()
  {
    return false;
  }

  public int getPower()
  {
    return _power;
  }

  public void increasePower()
  {
    _power += 1;
  }
}