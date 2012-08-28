package l2m.gameserver.skills.skillclasses;

import java.util.List;
import l2m.gameserver.Config;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Skill;
import l2m.gameserver.templates.StatsSet;

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