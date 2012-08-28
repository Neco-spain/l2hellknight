package l2m.gameserver.skills.conditions;

import l2m.gameserver.skills.Env;
import l2m.gameserver.utils.PositionUtils;
import l2m.gameserver.utils.PositionUtils.TargetDirection;

public class ConditionTargetDirection extends Condition
{
  private final PositionUtils.TargetDirection _dir;

  public ConditionTargetDirection(PositionUtils.TargetDirection direction)
  {
    _dir = direction;
  }

  protected boolean testImpl(Env env)
  {
    return PositionUtils.getDirectionTo(env.target, env.character) == _dir;
  }
}