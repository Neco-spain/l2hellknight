package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.skills.Env;

public class ConditionTargetNone extends Condition
{
  public boolean testImpl(Env env)
  {
    return env.target == null;
  }
}