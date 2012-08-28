package l2m.gameserver.skills.conditions;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.base.Race;
import l2m.gameserver.skills.Env;

public class ConditionTargetPlayerRace extends Condition
{
  private final Race _race;

  public ConditionTargetPlayerRace(String race)
  {
    _race = Race.valueOf(race.toLowerCase());
  }

  protected boolean testImpl(Env env)
  {
    Creature target = env.target;
    return (target != null) && (target.isPlayer()) && (_race == ((Player)target).getRace());
  }
}