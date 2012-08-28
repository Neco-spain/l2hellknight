package l2m.gameserver.skills.conditions;

import l2m.gameserver.model.Creature;
import l2m.gameserver.skills.Env;

public class ConditionTargetPercentCp extends Condition
{
  private final double _cp;

  public ConditionTargetPercentCp(int cp)
  {
    _cp = (cp / 100.0D);
  }

  protected boolean testImpl(Env env)
  {
    return (env.target != null) && (env.target.getCurrentCpRatio() <= _cp);
  }
}