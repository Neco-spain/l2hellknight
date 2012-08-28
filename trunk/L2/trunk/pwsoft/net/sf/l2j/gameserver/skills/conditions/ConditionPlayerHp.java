package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.skills.Env;

public class ConditionPlayerHp extends Condition
{
  private final int _hp;

  public ConditionPlayerHp(int hp)
  {
    _hp = hp;
  }

  public boolean testImpl(Env env)
  {
    return env.cha.getCurrentHp() * 100.0D / env.cha.getMaxHp() <= _hp;
  }
}