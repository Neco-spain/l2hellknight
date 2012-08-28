package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.skills.Env;

public class ConditionPlayerState extends Condition
{
  private final CheckPlayerState _check;
  private final boolean _required;

  public ConditionPlayerState(CheckPlayerState check, boolean required)
  {
    _check = check;
    _required = required;
  }

  public boolean testImpl(Env env)
  {
    switch (1.$SwitchMap$net$sf$l2j$gameserver$skills$conditions$ConditionPlayerState$CheckPlayerState[_check.ordinal()])
    {
    case 1:
      if (env.cha.isPlayer()) {
        return env.cha.getPlayer().isSitting() == _required;
      }
      return !_required;
    case 2:
      return env.cha.isMoving() == _required;
    case 3:
      return (env.cha.isMoving() == _required) && (env.cha.isRunning() == _required);
    case 4:
      return env.cha.isFlying() == _required;
    case 5:
      return env.cha.isBehindTarget() == _required;
    case 6:
      return env.cha.isFrontTarget() == _required;
    }
    return !_required;
  }

  public static enum CheckPlayerState
  {
    RESTING, MOVING, RUNNING, FLYING, BEHIND, FRONT;
  }
}