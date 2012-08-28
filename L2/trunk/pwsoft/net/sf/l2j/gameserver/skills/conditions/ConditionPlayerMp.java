package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.skills.Env;

public class ConditionPlayerMp extends Condition
{
  private final int _mp;

  public ConditionPlayerMp(int mp)
  {
    _mp = mp;
  }

  public boolean testImpl(Env env)
  {
    return env.cha.getCurrentMp() * 100.0D / env.cha.getMaxMp() <= _mp;
  }
}