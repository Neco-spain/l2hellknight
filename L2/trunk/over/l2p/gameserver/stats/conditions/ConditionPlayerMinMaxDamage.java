package l2p.gameserver.stats.conditions;

import l2p.gameserver.stats.Env;

public class ConditionPlayerMinMaxDamage extends Condition
{
  private final double _min;
  private final double _max;

  public ConditionPlayerMinMaxDamage(double min, double max)
  {
    _min = min;
    _max = max;
  }

  protected boolean testImpl(Env env)
  {
    if ((_min > 0.0D) && (env.value < _min)) {
      return false;
    }
    return (_max <= 0.0D) || (env.value <= _max);
  }
}