package net.sf.l2j.gameserver.skills.conditions;

import javolution.util.FastList;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.templates.L2NpcTemplate.Race;

public class ConditionTargetRaceId extends Condition
{
  private final FastList<Integer> _raceIds;

  public ConditionTargetRaceId(FastList<Integer> raceId)
  {
    _raceIds = raceId;
  }

  public boolean testImpl(Env env)
  {
    if (!(env.target instanceof L2NpcInstance)) {
      return false;
    }
    return _raceIds.contains(Integer.valueOf(((L2NpcInstance)env.target).getTemplate().race.ordinal()));
  }
}