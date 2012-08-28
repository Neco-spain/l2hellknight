package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.skills.Env;

public class ConditionGameTime extends Condition
{
  private final CheckGameTime _check;
  private final boolean _required;

  public ConditionGameTime(CheckGameTime check, boolean required)
  {
    _check = check;
    _required = required;
  }

  public boolean testImpl(Env env)
  {
    switch (1.$SwitchMap$net$sf$l2j$gameserver$skills$conditions$ConditionGameTime$CheckGameTime[_check.ordinal()])
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