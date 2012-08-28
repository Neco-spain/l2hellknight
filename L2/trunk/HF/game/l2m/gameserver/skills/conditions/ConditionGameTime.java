package l2m.gameserver.skills.conditions;

import l2m.gameserver.GameTimeController;
import l2m.gameserver.skills.Env;

public class ConditionGameTime extends Condition
{
  private final CheckGameTime _check;
  private final boolean _required;

  public ConditionGameTime(CheckGameTime check, boolean required)
  {
    _check = check;
    _required = required;
  }

  protected boolean testImpl(Env env)
  {
    switch (1.$SwitchMap$l2p$gameserver$stats$conditions$ConditionGameTime$CheckGameTime[_check.ordinal()])
    {
    case 1:
      return GameTimeController.getInstance().isNowNight() == _required;
    }
    return !_required;
  }

  public static enum CheckGameTime
  {
    NIGHT;
  }
}