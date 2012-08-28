package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.skills.Env;

public class ConditionPlayerHpPercentage extends Condition
{
  private double _p;

  public ConditionPlayerHpPercentage(double p)
  {
    _p = p;
  }

  public boolean testImpl(Env env)
  {
    return env.cha.getCurrentHp() <= env.cha.getMaxHp() * _p;
  }
}