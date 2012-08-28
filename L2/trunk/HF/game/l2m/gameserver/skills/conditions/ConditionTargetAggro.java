package l2m.gameserver.skills.conditions;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.instances.MonsterInstance;
import l2m.gameserver.skills.Env;

public class ConditionTargetAggro extends Condition
{
  private final boolean _isAggro;

  public ConditionTargetAggro(boolean isAggro)
  {
    _isAggro = isAggro;
  }

  protected boolean testImpl(Env env)
  {
    Creature target = env.target;
    if (target == null)
      return false;
    if (target.isMonster())
      return ((MonsterInstance)target).isAggressive() == _isAggro;
    if (target.isPlayer())
      return target.getKarma() > 0;
    return false;
  }
}