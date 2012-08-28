package l2m.gameserver.skills.conditions;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.entity.residence.ResidenceType;
import l2m.gameserver.model.pledge.Clan;
import l2m.gameserver.skills.Env;

public class ConditionPlayerResidence extends Condition
{
  private final int _id;
  private final ResidenceType _type;

  public ConditionPlayerResidence(int id, ResidenceType type)
  {
    _id = id;
    _type = type;
  }

  protected boolean testImpl(Env env)
  {
    if (!env.character.isPlayer())
      return false;
    Player player = (Player)env.character;
    Clan clan = player.getClan();
    if (clan == null) {
      return false;
    }
    int residenceId = clan.getResidenceId(_type);

    return residenceId == _id;
  }
}