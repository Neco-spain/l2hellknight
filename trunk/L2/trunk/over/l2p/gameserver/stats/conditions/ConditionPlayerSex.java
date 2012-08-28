package l2p.gameserver.stats.conditions;

import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Player;
import l2p.gameserver.stats.Env;

public class ConditionPlayerSex extends Condition
{
  private final int _sex;

  public ConditionPlayerSex(int sex)
  {
    _sex = sex;
  }

  protected boolean testImpl(Env env)
  {
    if (!env.character.isPlayer())
      return false;
    return ((Player)env.character).getSex() == _sex;
  }
}