package l2m.gameserver.skills.conditions;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Player;
import l2m.gameserver.skills.Env;

public class ConditionPlayerState extends Condition
{
  private final CheckPlayerState _check;
  private final boolean _required;

  public ConditionPlayerState(CheckPlayerState check, boolean required)
  {
    _check = check;
    _required = required;
  }

  protected boolean testImpl(Env env)
  {
    switch (1.$SwitchMap$l2p$gameserver$stats$conditions$ConditionPlayerState$CheckPlayerState[_check.ordinal()])
    {
    case 1:
      if (env.character.isPlayer())
        return ((Player)env.character).isSitting() == _required;
      return !_required;
    case 2:
      return env.character.isMoving == _required;
    case 3:
      return ((env.character.isMoving) && (env.character.isRunning())) == _required;
    case 4:
      if (env.character.isPlayer())
        return (((Player)env.character).isSitting() != _required) && (env.character.isMoving != _required);
      return env.character.isMoving != _required;
    case 5:
      if (env.character.isPlayer())
        return env.character.isFlying() == _required;
      return !_required;
    case 6:
      if (env.character.isPlayer())
        return ((Player)env.character).isInFlyingTransform() == _required;
      return !_required;
    }
    return !_required;
  }

  public static enum CheckPlayerState
  {
    RESTING, 
    MOVING, 
    RUNNING, 
    STANDING, 
    FLYING, 
    FLYING_TRANSFORM;
  }
}