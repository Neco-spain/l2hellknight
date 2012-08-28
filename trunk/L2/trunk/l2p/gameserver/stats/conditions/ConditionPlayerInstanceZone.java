package l2p.gameserver.stats.conditions;

import l2p.gameserver.model.Creature;
import l2p.gameserver.model.entity.Reflection;
import l2p.gameserver.stats.Env;

public class ConditionPlayerInstanceZone extends Condition
{
  private final int _id;

  public ConditionPlayerInstanceZone(int id)
  {
    _id = id;
  }

  protected boolean testImpl(Env env)
  {
    Reflection ref = env.character.getReflection();

    return ref.getInstancedZoneId() == _id;
  }
}