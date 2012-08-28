package l2p.gameserver.stats.conditions;

import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.entity.residence.ResidenceType;
import l2p.gameserver.model.pledge.Clan;
import l2p.gameserver.stats.Env;

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