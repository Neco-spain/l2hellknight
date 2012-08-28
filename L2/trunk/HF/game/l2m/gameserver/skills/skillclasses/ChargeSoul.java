package l2m.gameserver.skills.skillclasses;

import java.util.List;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Skill;
import l2m.gameserver.model.Skill.SkillTargetType;
import l2m.gameserver.skills.Formulas;
import l2m.gameserver.skills.Formulas.AttackInfo;
import l2m.gameserver.templates.StatsSet;

public class ChargeSoul extends Skill
{
  private int _numSouls;

  public ChargeSoul(StatsSet set)
  {
    super(set);
    _numSouls = set.getInteger("numSouls", getLevel());
  }

  public void useSkill(Creature activeChar, List<Creature> targets)
  {
    if (!activeChar.isPlayer()) {
      return;
    }
    boolean ss = (activeChar.getChargedSoulShot()) && (isSSPossible());
    if ((ss) && (getTargetType() != Skill.SkillTargetType.TARGET_SELF)) {
      activeChar.unChargeShots(false);
    }

    for (Creature target : targets) {
      if (target != null)
      {
        if (target.isDead()) {
          continue;
        }
        boolean reflected = (target != activeChar) && (target.checkReflectSkill(activeChar, this));
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
        if ((realTarget.isPlayable()) || (realTarget.isMonster())) {
          activeChar.setConsumedSouls(activeChar.getConsumedSouls() + _numSouls, null);
        }
        getEffects(activeChar, target, getActivateRate() > 0, false, reflected);
      }
    }
    if (isSSPossible())
      activeChar.unChargeShots(isMagic());
  }
}