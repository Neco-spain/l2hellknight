package net.sf.l2j.gameserver.skills.conditions;

import javolution.util.FastList;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.skills.Env;

public class ConditionTargetClassIdRestriction extends Condition
{
  private final FastList<Integer> _classIds;

  public ConditionTargetClassIdRestriction(FastList<Integer> classId)
  {
    _classIds = classId;
  }

  public boolean testImpl(Env env)
  {
    if (!(env.target instanceof L2PcInstance))
      return true;
    return !_classIds.contains(Integer.valueOf(((L2PcInstance)env.target).getClassId().getId()));
  }
}