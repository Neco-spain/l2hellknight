package l2m.gameserver.skills.conditions;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.base.Race;
import l2m.gameserver.skills.Env;

public class ConditionPlayerRace extends Condition
{
  private final Race _race;

  public ConditionPlayerRace(String race)
  {
    _race = Race.valueOf(race.toLowerCase());
  }

  protected boolean testImpl(Env env)
  {
    if (!env.character.isPlayer())
      return false;
    return ((Player)env.character).getRace() == _race;
  }
}