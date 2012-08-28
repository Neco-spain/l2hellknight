package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.skills.Env;

public class ConditionPlayerBaseStats extends Condition
{
  private final BaseStat _stat;
  private final int _value;

  public ConditionPlayerBaseStats(L2Character player, BaseStat stat, int value)
  {
    _stat = stat;
    _value = value;
  }

  public boolean testImpl(Env env)
  {
    if (!(env.player instanceof L2PcInstance))
      return false;
    L2PcInstance player = (L2PcInstance)env.player;
    switch (1.$SwitchMap$net$sf$l2j$gameserver$skills$conditions$BaseStat[_stat.ordinal()]) {
    case 1:
      return player.getINT() >= _value;
    case 2:
      return player.getSTR() >= _value;
    case 3:
      return player.getCON() >= _value;
    case 4:
      return player.getDEX() >= _value;
    case 5:
      return player.getMEN() >= _value;
    case 6:
      return player.getWIT() >= _value;
    }
    return false;
  }
}