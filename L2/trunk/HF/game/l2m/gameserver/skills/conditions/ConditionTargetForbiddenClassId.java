package l2m.gameserver.skills.conditions;

import gnu.trove.TIntHashSet;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Player;
import l2m.gameserver.skills.Env;

public class ConditionTargetForbiddenClassId extends Condition
{
  private TIntHashSet _classIds = new TIntHashSet();

  public ConditionTargetForbiddenClassId(String[] ids)
  {
    for (String id : ids)
      _classIds.add(Integer.parseInt(id));
  }

  protected boolean testImpl(Env env)
  {
    Creature target = env.target;
    if (!target.isPlayable())
      return false;
    return (!target.isPlayer()) || (!_classIds.contains(target.getPlayer().getActiveClassId()));
  }
}