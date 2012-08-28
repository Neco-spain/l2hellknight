package l2m.gameserver.skills.conditions;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Player;
import l2m.gameserver.skills.Env;

public class ConditionPlayerAgathion extends Condition
{
  private final int _agathionId;

  public ConditionPlayerAgathion(int agathionId)
  {
    _agathionId = agathionId;
  }

  protected boolean testImpl(Env env)
  {
    if (!env.character.isPlayer())
      return false;
    if ((((Player)env.character).getAgathionId() > 0) && (_agathionId == -1))
      return true;
    return ((Player)env.character).getAgathionId() == _agathionId;
  }
}