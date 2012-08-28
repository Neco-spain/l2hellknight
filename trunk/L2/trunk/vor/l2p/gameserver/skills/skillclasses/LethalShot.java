package l2p.gameserver.skills.skillclasses;

import java.util.List;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Skill;
import l2p.gameserver.stats.Formulas;
import l2p.gameserver.stats.Formulas.AttackInfo;
import l2p.gameserver.templates.StatsSet;

public class LethalShot extends Skill
{
  public LethalShot(StatsSet set)
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
        boolean reflected = target.checkReflectSkill(activeChar, this);
        Creature realTarget = reflected ? activeChar : target;

        if (getPower() > 0.0D)
        {
          Formulas.AttackInfo info = Formulas.calcPhysDam(activeChar, realTarget, this, false, false, ss, false);

          if (info.lethal_dmg > 0.0D) {
            realTarget.reduceCurrentHp(info.lethal_dmg, activeChar, this, true, true, false, false, false, false, false);
          }
          realTarget.reduceCurrentHp(info.damage, activeChar, this, true, true, false, true, false, false, true);
          if (!reflected) {
            realTarget.doCounterAttack(this, activeChar, false);
          }
        }
        getEffects(activeChar, target, getActivateRate() > 0, false, reflected);
      }
  }
}