package l2p.gameserver.skills.skillclasses;

import java.util.List;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Skill;
import l2p.gameserver.stats.Formulas;
import l2p.gameserver.templates.StatsSet;

public class MDam extends Skill
{
  public MDam(StatsSet set)
  {
    super(set);
  }

  public void useSkill(Creature activeChar, List<Creature> targets)
  {
    int sps = isSSPossible() ? 0 : activeChar.getChargedSoulShot() ? 2 : isMagic() ? activeChar.getChargedSpiritShot() : 0;

    for (Creature target : targets) {
      if (target != null)
      {
        if (target.isDead()) {
          continue;
        }
        boolean reflected = target.checkReflectSkill(activeChar, this);
        Creature realTarget = reflected ? activeChar : target;

        double damage = Formulas.calcMagicDam(activeChar, realTarget, this, sps);
        if (damage >= 1.0D) {
          realTarget.reduceCurrentHp(damage, activeChar, this, true, true, false, true, false, false, true);
        }
        getEffects(activeChar, target, getActivateRate() > 0, false, reflected);
      }
    }
    if (isSuicideAttack())
      activeChar.doDie(null);
    else if (isSSPossible())
      activeChar.unChargeShots(isMagic());
  }
}