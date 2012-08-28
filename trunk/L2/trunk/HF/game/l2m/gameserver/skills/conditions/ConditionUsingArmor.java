package l2m.gameserver.skills.conditions;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Player;
import l2m.gameserver.skills.Env;
import l2m.gameserver.templates.item.ArmorTemplate.ArmorType;

public class ConditionUsingArmor extends Condition
{
  private final ArmorTemplate.ArmorType _armor;

  public ConditionUsingArmor(ArmorTemplate.ArmorType armor)
  {
    _armor = armor;
  }

  protected boolean testImpl(Env env)
  {
    return (env.character.isPlayer()) && (((Player)env.character).isWearingArmor(_armor));
  }
}