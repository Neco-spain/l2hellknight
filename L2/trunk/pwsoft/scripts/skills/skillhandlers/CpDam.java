package scripts.skills.skillhandlers;

import javolution.util.FastList;
import javolution.util.FastList.Node;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.skills.Formulas;
import scripts.skills.ISkillHandler;

public class CpDam
  implements ISkillHandler
{
  private static final L2Skill.SkillType[] SKILL_IDS = { L2Skill.SkillType.CPDAM };

  public void useSkill(L2Character activeChar, L2Skill skill, FastList<L2Object> targets)
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

    FastList.Node n = targets.head(); for (FastList.Node end = targets.tail(); (n = n.getNext()) != end; )
    {
      L2Character target = (L2Character)n.getValue();
      if ((target == null) || (target.isAlikeDead())) {
        continue;
      }
      if ((activeChar.isPlayer()) && (target.isPlayer()) && (target.isAlikeDead()) && (target.isFakeDeath())) {
        target.stopFakeDeath(null);
      }
      if (!Formulas.calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
        return;
      int damage = (int)(target.getCurrentCp() * (1.0D - skill.getPower()));

      if ((!target.isRaid()) && (Formulas.calcAtkBreak(target, damage)))
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