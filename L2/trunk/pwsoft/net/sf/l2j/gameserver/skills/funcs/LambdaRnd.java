package net.sf.l2j.gameserver.skills.funcs;

import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.util.Rnd;

public final class LambdaRnd extends Lambda
{
  private final Lambda _max;
  private final boolean _linear;

  public LambdaRnd(Lambda max, boolean linear)
  {
    _max = max;
    _linear = linear;
  }

  public double calc(Env env) {
    if (_linear)
      return _max.calc(env) * Rnd.nextDouble();
    return _max.calc(env) * Rnd.nextGaussian();
  }
}