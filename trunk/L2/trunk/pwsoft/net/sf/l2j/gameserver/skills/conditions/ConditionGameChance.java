package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.util.Rnd;

public class ConditionGameChance extends Condition
{
  private final int _chance;

  public ConditionGameChance(int chance)
  {
    _chance = chance;
  }

  public boolean testImpl(Env env)
  {
    return Rnd.get(100) < _chance;
  }
}