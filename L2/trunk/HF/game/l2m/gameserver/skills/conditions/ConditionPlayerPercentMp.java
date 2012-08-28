package l2m.gameserver.skills.conditions;

import l2m.gameserver.model.Creature;
import l2m.gameserver.skills.Env;

public class ConditionPlayerPercentMp extends Condition
{
  private final double _mp;

  public ConditionPlayerPercentMp(int mp)
  {
    _mp = (mp / 100.0D);
  }

  protected boolean testImpl(Env env)
  {
    return env.character.getCurrentMpRatio() <= _mp;
  }
}