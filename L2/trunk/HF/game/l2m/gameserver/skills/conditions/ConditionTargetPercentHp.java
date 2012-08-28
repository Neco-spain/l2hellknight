package l2m.gameserver.skills.conditions;

import l2m.gameserver.model.Creature;
import l2m.gameserver.skills.Env;

public class ConditionTargetPercentHp extends Condition
{
  private final double _hp;

  public ConditionTargetPercentHp(int hp)
  {
    _hp = (hp / 100.0D);
  }

  protected boolean testImpl(Env env)
  {
    return (env.target != null) && (env.target.getCurrentHpRatio() <= _hp);
  }
}