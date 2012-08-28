package l2p.gameserver.skills.skillclasses;

import java.util.List;
import l2p.gameserver.ai.CharacterAI;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.Skill.SkillTargetType;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.stats.Formulas;
import l2p.gameserver.stats.Formulas.AttackInfo;
import l2p.gameserver.stats.Stats;
import l2p.gameserver.templates.StatsSet;

public class Drain extends Skill
{
  private double _absorbAbs;

  public Drain(StatsSet set)
  {
    super(set);
    _absorbAbs = set.getDouble("absorbAbs", 0.0D);
  }

  public void useSkill(Creature activeChar, List<Creature> targets)
  {
    int sps = isSSPossible() ? activeChar.getChargedSpiritShot() : 0;
    boolean ss = (isSSPossible()) && (activeChar.getChargedSoulShot());

    boolean corpseSkill = _targetType == Skill.SkillTargetType.TARGET_CORPSE;

    for (Creature target : targets) {
      if (target != null)
      {
        boolean reflected = (!corpseSkill) && (target.checkReflectSkill(activeChar, this));
        Creature realTarget = reflected ? activeChar : target;

        if ((getPower() > 0.0D) || (_absorbAbs > 0.0D))
        {
          if ((realTarget.isDead()) && (!corpseSkill)) {
            continue;
          }
          double hp = 0.0D;
          double targetHp = realTarget.getCurrentHp();

          if (!corpseSkill)
          {
            double damage;
            double damage;
            if (isMagic()) {
              damage = Formulas.calcMagicDam(activeChar, realTarget, this, sps);
            }
            else {
              Formulas.AttackInfo info = Formulas.calcPhysDam(activeChar, realTarget, this, false, false, ss, false);
              damage = info.damage;

              if (info.lethal_dmg > 0.0D)
                realTarget.reduceCurrentHp(info.lethal_dmg, activeChar, this, true, true, false, false, false, false, false);
            }
            double targetCP = realTarget.getCurrentCp();

            if ((damage > targetCP) || (!realTarget.isPlayer())) {
              hp = (damage - targetCP) * _absorbPart;
            }
            realTarget.reduceCurrentHp(damage, activeChar, this, true, true, false, true, false, false, true);
            if (!reflected) {
              realTarget.doCounterAttack(this, activeChar, false);
            }
          }
          if ((_absorbAbs == 0.0D) && (_absorbPart == 0.0D)) {
            continue;
          }
          hp += _absorbAbs;

          if ((hp > targetHp) && (!corpseSkill)) {
            hp = targetHp;
          }
          double addToHp = Math.max(0.0D, Math.min(hp, activeChar.calcStat(Stats.HP_LIMIT, null, null) * activeChar.getMaxHp() / 100.0D - activeChar.getCurrentHp()));

          if ((addToHp > 0.0D) && (!activeChar.isHealBlocked())) {
            activeChar.setCurrentHp(activeChar.getCurrentHp() + addToHp, false);
          }
          if ((realTarget.isDead()) && (corpseSkill) && (realTarget.isNpc()))
          {
            activeChar.getAI().setAttackTarget(null);
            ((NpcInstance)realTarget).endDecayTask();
          }
        }

        getEffects(activeChar, target, getActivateRate() > 0, false, reflected);
      }
    }
    if (isMagic() ? sps != 0 : ss)
      activeChar.unChargeShots(isMagic());
  }
}