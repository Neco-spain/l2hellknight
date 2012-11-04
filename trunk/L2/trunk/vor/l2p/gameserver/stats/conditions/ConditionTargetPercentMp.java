package l2p.gameserver.stats.conditions;

import l2p.gameserver.model.Creature;
import l2p.gameserver.stats.Env;

public class ConditionTargetPercentMp extends Condition
{
  private final double _mp;

  public ConditionTargetPercentMp(int mp)
  {
    _mp = (mp / 100.0D);
  }

  protected boolean testImpl(Env env)
  {
    return (env.target != null) && (env.target.getCurrentMpRatio() <= _mp);
  }
}