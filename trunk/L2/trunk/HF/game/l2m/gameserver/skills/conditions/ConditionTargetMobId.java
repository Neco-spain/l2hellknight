package l2m.gameserver.skills.conditions;

import l2m.gameserver.model.Creature;
import l2m.gameserver.skills.Env;

public class ConditionTargetMobId extends Condition
{
  private final int _mobId;

  public ConditionTargetMobId(int mobId)
  {
    _mobId = mobId;
  }

  protected boolean testImpl(Env env)
  {
    return (env.target != null) && (env.target.getNpcId() == _mobId);
  }
}