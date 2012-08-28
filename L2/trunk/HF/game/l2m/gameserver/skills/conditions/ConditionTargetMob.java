package l2m.gameserver.skills.conditions;

import l2m.gameserver.model.Creature;
import l2m.gameserver.skills.Env;

public class ConditionTargetMob extends Condition
{
  private final boolean _isMob;

  public ConditionTargetMob(boolean isMob)
  {
    _isMob = isMob;
  }

  protected boolean testImpl(Env env)
  {
    return (env.target != null) && (env.target.isMonster() == _isMob);
  }
}