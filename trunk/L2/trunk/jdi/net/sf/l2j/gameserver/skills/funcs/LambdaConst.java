package net.sf.l2j.gameserver.skills.funcs;

import net.sf.l2j.gameserver.skills.Env;

public final class LambdaConst extends Lambda
{
  private final double _value;

  public LambdaConst(double value)
  {
    _value = value;
  }

  public double calc(Env env) {
    return _value;
  }
}