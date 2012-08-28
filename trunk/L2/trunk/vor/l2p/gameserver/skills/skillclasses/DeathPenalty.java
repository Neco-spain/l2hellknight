package l2p.gameserver.skills.skillclasses;

import java.util.List;
import l2p.gameserver.Config;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.templates.StatsSet;

public class DeathPenalty extends Skill
{
  public DeathPenalty(StatsSet set)
  {
    super(set);
  }

  public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
  {
    if ((activeChar.getKarma() > 0) && (!Config.ALT_DEATH_PENALTY_C5_CHAOTIC_RECOVERY))
    {
      activeChar.sendActionFailed();
      return false;
    }

    return super.checkCondition(activeChar, target, forceUse, dontMove, first);
  }

  public void useSkill(Creature activeChar, List<Creature> targets)
  {
    for (Creature target : targets)
      if (target != null)
      {
        if (!target.isPlayer())
          continue;
        ((Player)target).getDeathPenalty().reduceLevel();
      }
  }
}