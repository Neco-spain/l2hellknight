package scripts.skills.skillhandlers;

import javolution.util.FastList;
import javolution.util.FastList.Node;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.util.Rnd;
import scripts.skills.ISkillHandler;

public class Mdam
  implements ISkillHandler
{
  private static final L2Skill.SkillType[] SKILL_IDS = { L2Skill.SkillType.MDAM, L2Skill.SkillType.DEATHLINK };

  public void useSkill(L2Character activeChar, L2Skill skill, FastList<L2Object> targets)
  {
    if (activeChar.isAlikeDead()) {
      return;
    }

    boolean ss = false;
    boolean bss = false;

    L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();

    if (weaponInst != null) {
      if (weaponInst.getChargedSpiritshot() == 2) {
        bss = true;
        weaponInst.setChargedSpiritshot(0);
      } else if (weaponInst.getChargedSpiritshot() == 1) {
        ss = true;
        weaponInst.setChargedSpiritshot(0);
      }
    }
    else if ((activeChar instanceof L2Summon)) {
      L2Summon activeSummon = (L2Summon)activeChar;

      if (activeSummon.getChargedSpiritShot() == 2) {
        bss = true;
        activeSummon.setChargedSpiritShot(0);
      } else if (activeSummon.getChargedSpiritShot() == 1) {
        ss = true;
        activeSummon.setChargedSpiritShot(0);
      }
    }

    FastList.Node n = targets.head(); for (FastList.Node end = targets.tail(); (n = n.getNext()) != end; ) {
      L2Character target = (L2Character)n.getValue();

      if (target == null)
      {
        continue;
      }
      if ((activeChar.isPlayer()) && (target.isPlayer()) && (target.isAlikeDead()) && (target.isFakeDeath()))
        target.stopFakeDeath(null);
      else if (target.isAlikeDead())
        {
          continue;
        }


      boolean mcrit = false;
      if ((skill.getId() == 1265) && (Rnd.get(100) < 3))
        mcrit = true;
      else {
        mcrit = Formulas.calcMCrit(activeChar.getMCriticalHit(target, skill));
      }

      int damage = (int)Formulas.calcMagicDam(activeChar, target, skill, ss, bss, mcrit);

      if (damage > 0)
      {
        if ((!target.isRaid()) && (Formulas.calcAtkBreak(target, damage))) {
          target.breakAttack();
          target.breakCast();
        }

        activeChar.sendDamageMessage(target, damage, mcrit, false, false);

        if (skill.hasEffects()) {
          if (target.reflectSkill(skill)) {
            activeChar.stopSkillEffects(skill.getId());
            skill.getEffects(null, activeChar);
            activeChar.sendPacket(SystemMessage.id(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(skill.getId()));
          }
          else {
            L2Effect scuko = target.getFirstEffect(skill.getId());

            if ((scuko == null) && (Formulas.calcSkillSuccess(activeChar, target, skill, false, ss, bss)))
              skill.getEffects(activeChar, target);
            else {
              activeChar.sendPacket(SystemMessage.id(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2).addString(target.getName()).addSkillName(skill.getDisplayId()));
            }
          }
        }
        target.reduceCurrentHp(damage, activeChar);
      }

    }

    L2Effect effect = activeChar.getFirstEffect(skill.getId());
    if ((effect != null) && (effect.isSelfEffect()))
    {
      effect.exit();
    }
    skill.getEffectsSelf(activeChar);

    if (skill.isSuicideAttack()) {
      activeChar.doDie(null);
      activeChar.setCurrentHp(0.0D);
    }
  }

  public L2Skill.SkillType[] getSkillIds()
  {
    return SKILL_IDS;
  }
}