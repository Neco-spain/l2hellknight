package l2p.gameserver.stats.conditions;

import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.base.TeamType;
import l2p.gameserver.stats.Env;

public class ConditionTargetRelation extends Condition
{
  private final Relation state;

  public ConditionTargetRelation(Relation state)
  {
    this.state = state;
  }

  protected boolean testImpl(Env env)
  {
    return getRelation(env.character, env.target) == state;
  }

  public static Relation getRelation(Creature activeChar, Creature aimingTarget)
  {
    if ((activeChar.isPlayable()) && (activeChar.getPlayer() != null))
    {
      if (aimingTarget.isMonster()) {
        return Relation.Enemy;
      }
      Player player = activeChar.getPlayer();

      if ((aimingTarget.isPlayable()) && (aimingTarget.getPlayer() != null))
      {
        Player target = aimingTarget.getPlayer();

        if ((player == target) && (player.getParty() != null) && (player.getParty() == target.getParty())) {
          return Relation.Friend;
        }
        if ((player.isInOlympiadMode()) && (player.isOlympiadCompStart()) && (player.getOlympiadSide() == target.getOlympiadSide())) {
          return Relation.Friend;
        }
        if ((player.getTeam() != TeamType.NONE) && (target.getTeam() != TeamType.NONE) && (player.getTeam() == target.getTeam())) {
          return Relation.Friend;
        }
        if (activeChar.isInZoneBattle()) {
          return Relation.Enemy;
        }
        if ((player.getClanId() != 0) && (player.getClanId() == target.getClanId())) {
          return Relation.Friend;
        }
        if (activeChar.isInZonePeace()) {
          return Relation.Neutral;
        }
        if (player.atMutualWarWith(target)) {
          return Relation.Enemy;
        }
        if (target.getKarma() > 0)
          return Relation.Enemy;
      }
    }
    return Relation.Neutral;
  }

  public static enum Relation
  {
    Neutral, 
    Friend, 
    Enemy;
  }
}