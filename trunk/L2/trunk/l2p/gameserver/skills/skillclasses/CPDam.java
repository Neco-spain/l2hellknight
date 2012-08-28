package l2p.gameserver.skills.skillclasses;

import java.util.List;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Skill;
import l2p.gameserver.templates.StatsSet;

public class CPDam extends Skill
{
  public CPDam(StatsSet set)
  {
    super(set);
  }

  public void useSkill(Creature activeChar, List<Creature> targets)
  {
    boolean ss = (activeChar.getChargedSoulShot()) && (isSSPossible());
    if (ss) {
      activeChar.unChargeShots(false);
    }

    for (Creature target : targets)
      if (target != null)
      {
        if (target.isDead()) {
          continue;
        }
        target.doCounterAttack(this, activeChar, false);

        boolean reflected = target.checkReflectSkill(activeChar, this);
        Creature realTarget = reflected ? activeChar : target;

        if (realTarget.isCurrentCpZero()) {
          continue;
        }
        double damage = _power * realTarget.getCurrentCp();

        if (damage < 1.0D) {
          damage = 1.0D;
        }
        realTarget.reduceCurrentHp(damage, activeChar, this, true, true, false, true, false, false, true);

        getEffects(activeChar, target, getActivateRate() > 0, false, reflected);
      }
  }
}