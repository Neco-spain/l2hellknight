package l2m.gameserver.skills.conditions;

import java.util.Collection;
import l2m.gameserver.cache.Msg;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Player;
import l2m.gameserver.skills.Env;
import l2m.gameserver.skills.Stats;

public class ConditionPlayerCubic extends Condition
{
  private int _id;

  public ConditionPlayerCubic(int id)
  {
    _id = id;
  }

  protected boolean testImpl(Env env)
  {
    if ((env.target == null) || (!env.target.isPlayer())) {
      return false;
    }
    Player targetPlayer = (Player)env.target;
    if (targetPlayer.getCubic(_id) != null) {
      return true;
    }
    int size = (int)targetPlayer.calcStat(Stats.CUBICS_LIMIT, 1.0D);
    if (targetPlayer.getCubics().size() >= size)
    {
      if (env.character == targetPlayer) {
        targetPlayer.sendPacket(Msg.CUBIC_SUMMONING_FAILED);
      }
      return false;
    }

    return true;
  }
}