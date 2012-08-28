package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.skills.Env;

public class ConditionTargetAggro extends Condition
{
  private final boolean _isAggro;

  public ConditionTargetAggro(boolean isAggro)
  {
    _isAggro = isAggro;
  }

  public boolean testImpl(Env env)
  {
    L2Character target = env.target;
    if ((target instanceof L2MonsterInstance))
    {
      return ((L2MonsterInstance)target).isAggressive() == _isAggro;
    }
    if ((target instanceof L2PcInstance))
    {
      return ((L2PcInstance)target).getKarma() > 0;
    }
    return false;
  }
}