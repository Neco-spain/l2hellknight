package l2p.gameserver.skills.skillclasses;

import java.util.List;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Skill;
import l2p.gameserver.stats.Stats;
import l2p.gameserver.templates.StatsSet;

public class Balance extends Skill
{
  public Balance(StatsSet set)
  {
    super(set);
  }

  public void useSkill(Creature activeChar, List<Creature> targets)
  {
    double summaryCurrentHp = 0.0D;
    int summaryMaximumHp = 0;

    for (Creature target : targets) {
      if (target != null)
      {
        if (target.isAlikeDead())
          continue;
        summaryCurrentHp += target.getCurrentHp();
        summaryMaximumHp += target.getMaxHp();
      }
    }
    double percent = summaryCurrentHp / summaryMaximumHp;

    for (Creature target : targets) {
      if (target != null)
      {
        if (target.isAlikeDead()) {
          continue;
        }
        double hp = target.getMaxHp() * percent;
        if (hp > target.getCurrentHp())
        {
          double limit = target.calcStat(Stats.HP_LIMIT, null, null) * target.getMaxHp() / 100.0D;
          if (target.getCurrentHp() < limit)
            target.setCurrentHp(Math.min(hp, limit), false);
        }
        else
        {
          target.setCurrentHp(Math.max(1.01D, hp), false);
        }
        getEffects(activeChar, target, getActivateRate() > 0, false);
      }
    }
    if (isSSPossible())
      activeChar.unChargeShots(isMagic());
  }
}