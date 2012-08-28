package l2m.gameserver.skills.skillclasses;

import java.util.List;
import l2p.commons.util.Rnd;
import l2m.gameserver.Config;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Skill;
import l2m.gameserver.model.Skill.SkillType;
import l2m.gameserver.network.serverpackets.components.SystemMsg;
import l2m.gameserver.skills.Stats;
import l2m.gameserver.templates.StatsSet;

public class Continuous extends Skill
{
  private final int _lethal1;
  private final int _lethal2;

  public Continuous(StatsSet set)
  {
    super(set);
    _lethal1 = set.getInteger("lethal1", 0);
    _lethal2 = set.getInteger("lethal2", 0);
  }

  public void useSkill(Creature activeChar, List<Creature> targets)
  {
    for (Creature target : targets) {
      if (target != null)
      {
        if ((getSkillType() == Skill.SkillType.BUFF) && (target != activeChar) && (
          (target.isCursedWeaponEquipped()) || (activeChar.isCursedWeaponEquipped()))) {
          continue;
        }
        boolean reflected = target.checkReflectSkill(activeChar, this);
        Creature realTarget = reflected ? activeChar : target;

        double mult = 0.01D * realTarget.calcStat(Stats.DEATH_VULNERABILITY, activeChar, this);
        double lethal1 = _lethal1 * mult;
        double lethal2 = _lethal2 * mult;

        if ((lethal1 > 0.0D) && (Rnd.chance(lethal1)))
        {
          if (realTarget.isPlayer())
          {
            realTarget.reduceCurrentHp(realTarget.getCurrentCp(), activeChar, this, true, true, false, true, false, false, true);
            realTarget.sendPacket(SystemMsg.LETHAL_STRIKE);
            activeChar.sendPacket(SystemMsg.YOUR_LETHAL_STRIKE_WAS_SUCCESSFUL);
          }
          else if ((realTarget.isNpc()) && (!realTarget.isLethalImmune()))
          {
            realTarget.reduceCurrentHp(realTarget.getCurrentHp() / 2.0D, activeChar, this, true, true, false, true, false, false, true);
            activeChar.sendPacket(SystemMsg.YOUR_LETHAL_STRIKE_WAS_SUCCESSFUL);
          }
        }
        else if ((lethal2 > 0.0D) && (Rnd.chance(lethal2))) {
          if (realTarget.isPlayer())
          {
            realTarget.reduceCurrentHp(realTarget.getCurrentHp() + realTarget.getCurrentCp() - 1.0D, activeChar, this, true, true, false, true, false, false, true);
            realTarget.sendPacket(SystemMsg.LETHAL_STRIKE);
            activeChar.sendPacket(SystemMsg.YOUR_LETHAL_STRIKE_WAS_SUCCESSFUL);
          }
          else if ((realTarget.isNpc()) && (!realTarget.isLethalImmune()))
          {
            realTarget.reduceCurrentHp(realTarget.getCurrentHp() - 1.0D, activeChar, this, true, true, false, true, false, false, true);
            activeChar.sendPacket(SystemMsg.YOUR_LETHAL_STRIKE_WAS_SUCCESSFUL);
          }
        }
        getEffects(activeChar, target, getActivateRate() > 0, false, reflected);
      }
    }
    if ((isSSPossible()) && (
      (!Config.SAVING_SPS) || (_skillType != Skill.SkillType.BUFF)))
      activeChar.unChargeShots(isMagic());
  }
}