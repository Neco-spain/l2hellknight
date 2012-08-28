package l2p.gameserver.stats.conditions;

import l2p.gameserver.model.Creature;
import l2p.gameserver.stats.Env;

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