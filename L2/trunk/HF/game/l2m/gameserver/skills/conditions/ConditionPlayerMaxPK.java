package l2m.gameserver.skills.conditions;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Player;
import l2m.gameserver.skills.Env;

public class ConditionPlayerMaxPK extends Condition
{
  private final int _pk;

  public ConditionPlayerMaxPK(int pk)
  {
    _pk = pk;
  }

  protected boolean testImpl(Env env)
  {
    if (env.character.isPlayer())
      return ((Player)env.character).getPkKills() <= _pk;
    return false;
  }
}