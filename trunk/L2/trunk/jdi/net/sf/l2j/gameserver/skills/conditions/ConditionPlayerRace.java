package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.base.Race;
import net.sf.l2j.gameserver.skills.Env;

public class ConditionPlayerRace extends Condition
{
  private final Race _race;

  public ConditionPlayerRace(Race race)
  {
    _race = race;
  }

  public boolean testImpl(Env env)
  {
    if (!(env.player instanceof L2PcInstance))
      return false;
    return ((L2PcInstance)env.player).getRace() == _race;
  }
}