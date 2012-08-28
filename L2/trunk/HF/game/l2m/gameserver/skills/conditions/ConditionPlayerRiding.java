package l2m.gameserver.skills.conditions;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Player;
import l2m.gameserver.skills.Env;

public class ConditionPlayerRiding extends Condition
{
  private final CheckPlayerRiding _riding;

  public ConditionPlayerRiding(CheckPlayerRiding riding)
  {
    _riding = riding;
  }

  protected boolean testImpl(Env env)
  {
    if (!env.character.isPlayer())
      return false;
    if ((_riding == CheckPlayerRiding.STRIDER) && (((Player)env.character).isRiding()))
      return true;
    if ((_riding == CheckPlayerRiding.WYVERN) && (((Player)env.character).isFlying())) {
      return true;
    }
    return (_riding == CheckPlayerRiding.NONE) && (!((Player)env.character).isRiding()) && (!((Player)env.character).isFlying());
  }

  public static enum CheckPlayerRiding
  {
    NONE, 
    STRIDER, 
    WYVERN;
  }
}