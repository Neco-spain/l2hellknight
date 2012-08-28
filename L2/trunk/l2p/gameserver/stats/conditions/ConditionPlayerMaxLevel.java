package l2p.gameserver.stats.conditions;

import l2p.gameserver.model.Creature;
import l2p.gameserver.stats.Env;

public class ConditionPlayerMaxLevel extends Condition
{
  private final int _level;

  public ConditionPlayerMaxLevel(int level)
  {
    _level = level;
  }

  protected boolean testImpl(Env env)
  {
    return env.character.getLevel() <= _level;
  }
}