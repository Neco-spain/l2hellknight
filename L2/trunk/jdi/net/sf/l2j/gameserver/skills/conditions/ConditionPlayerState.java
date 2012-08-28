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
      if ((env.player instanceof L2PcInstance)) {
        return ((L2PcInstance)env.player).isSitting() == _required;
      }
      return !_required;
    case 2:
      return env.player.isMoving() == _required;
    case 3:
      return (env.player.isMoving() == _required) && (env.player.isRunning() == _required);
    case 4:
      return env.player.isFlying() == _required;
    case 5:
      return env.player.isBehindTarget() == _required;
    case 6:
      return env.player.isFrontTarget() == _required;
    }
    return !_required;
  }

  public static enum CheckPlayerState
  {
    RESTING, MOVING, RUNNING, FLYING, BEHIND, FRONT;
  }
}