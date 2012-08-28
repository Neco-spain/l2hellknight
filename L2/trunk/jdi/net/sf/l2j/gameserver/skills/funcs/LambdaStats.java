package net.sf.l2j.gameserver.skills.funcs;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.skills.Env;

public final class LambdaStats extends Lambda
{
  private final StatsType _stat;

  public LambdaStats(StatsType stat)
  {
    _stat = stat;
  }

  public double calc(Env env) {
    switch (1.$SwitchMap$net$sf$l2j$gameserver$skills$funcs$LambdaStats$StatsType[_stat.ordinal()])
    {
    case 1:
      if (env.player == null)
        return 1.0D;
      return env.player.getLevel();
    case 2:
      if (env.target == null)
        return 1.0D;
      return env.target.getLevel();
    case 3:
      if (env.player == null)
        return 1.0D;
      return env.player.getMaxHp();
    case 4:
      if (env.player == null)
        return 1.0D;
      return env.player.getMaxMp();
    }
    return 0.0D;
  }

  public static enum StatsType
  {
    PLAYER_LEVEL, 
    TARGET_LEVEL, 
    PLAYER_MAX_HP, 
    PLAYER_MAX_MP;
  }
}