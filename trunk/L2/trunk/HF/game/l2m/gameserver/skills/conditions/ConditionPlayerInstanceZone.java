package l2m.gameserver.skills.conditions;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.entity.Reflection;
import l2m.gameserver.skills.Env;

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