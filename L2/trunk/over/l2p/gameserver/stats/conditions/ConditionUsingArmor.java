package l2p.gameserver.stats.conditions;

import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Player;
import l2p.gameserver.stats.Env;
import l2p.gameserver.templates.item.ArmorTemplate.ArmorType;

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