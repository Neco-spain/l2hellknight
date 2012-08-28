package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.skills.Formulas;

public class CpDam
  implements ISkillHandler
{
  private static final L2Skill.SkillType[] SKILL_IDS = { L2Skill.SkillType.CPDAM };

  public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
  {
    if (activeChar.isAlikeDead()) return;

    boolean ss = false;
    boolean sps = false;
    boolean bss = false;

    L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();

    if (weaponInst != null)
    {
      if (skill.isMagic())
      {
        if (weaponInst.getChargedSpiritshot() == 2)
        {
          bss = true;
        }
        else if (weaponInst.getChargedSpiritshot() == 1)
        {
          sps = true;
        }

      }
      else if (weaponInst.getChargedSoulshot() == 1)
      {
        ss = true;
      }

    }
    else if ((activeChar instanceof L2Summon))
    {
      L2Summon activeSummon = (L2Summon)activeChar;

      if (activeSummon.getChargedSpiritShot() == 2)
      {
        bss = true;
        activeSummon.setChargedSpiritShot(0);
      }
      else if (activeSummon.getChargedSpiritShot() == 1)
      {
        ss = true;
        activeSummon.setChargedSpiritShot(0);
      }
    }

    for (int index = 0; index < targets.length; index++)
    {
      L2Character target = (L2Character)targets[index];

      if (((activeChar instanceof L2PcInstance)) && ((target instanceof L2PcInstance)) && (target.isAlikeDead()) && (target.isFakeDeath()))
      {
        target.stopFakeDeath(null);
      }
      else if (target.isAlikeDead())
        {
          continue;
        }

      if (!Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
        return;
      int damage = (int)(target.getCurrentCp() * (1.0D - skill.getPower()));

      if ((!target.isRaid()) && (Formulas.getInstance().calcAtkBreak(target, damage)))
      {
        target.breakAttack();
        target.breakCast();
      }
      skill.getEffects(activeChar, target);
      activeChar.sendDamageMessage(target, damage, false, false, false);
      target.setCurrentCp(target.getCurrentCp() - damage);
    }
  }

  public L2Skill.SkillType[] getSkillIds()
  {
    return SKILL_IDS;
  }
}