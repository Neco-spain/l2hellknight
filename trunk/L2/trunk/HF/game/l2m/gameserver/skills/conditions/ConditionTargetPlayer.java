package l2m.gameserver.skills.conditions;

import l2m.gameserver.model.Creature;
import l2m.gameserver.skills.Env;

public class ConditionTargetPlayer extends Condition
{
  private final boolean _flag;

  public ConditionTargetPlayer(boolean flag)
  {
    _flag = flag;
  }

  protected boolean testImpl(Env env)
  {
    Creature target = env.target;
    return (target != null) && (target.isPlayer() == _flag);
  }
}