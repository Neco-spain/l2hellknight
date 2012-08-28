package l2m.gameserver.skills.conditions;

import l2m.gameserver.model.instances.DoorInstance;
import l2m.gameserver.skills.Env;

public class ConditionTargetCastleDoor extends Condition
{
  private final boolean _isCastleDoor;

  public ConditionTargetCastleDoor(boolean isCastleDoor)
  {
    _isCastleDoor = isCastleDoor;
  }

  protected boolean testImpl(Env env)
  {
    return env.target instanceof DoorInstance == _isCastleDoor;
  }
}