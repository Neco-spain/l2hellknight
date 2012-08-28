package scripts.skills.skillhandlers;

import javolution.util.FastList;
import javolution.util.FastList.Node;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Formulas;
import scripts.skills.ISkillHandler;

public class Manadam
  implements ISkillHandler
{
  private static final L2Skill.SkillType[] SKILL_IDS = { L2Skill.SkillType.MANADAM };

  public void useSkill(L2Character activeChar, L2Skill skill, FastList<L2Object> targets)
  {
    L2Character target = null;

    if (activeChar.isAlikeDead()) return;

    boolean ss = false;
    boolean bss = false;

    L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();

    if (weaponInst != null)
    {
      if (weaponInst.getChargedSpiritshot() == 2)
      {
        bss = true;
        weaponInst.setChargedSpiritshot(0);
      }
      else if (weaponInst.getChargedSpiritshot() == 1)
      {
        ss = true;
        weaponInst.setChargedSpiritshot(0);
      }
    }

    FastList.Node n = targets.head(); for (FastList.Node end = targets.tail(); (n = n.getNext()) != end; )
    {
      target = (L2Character)n.getValue();

      if (target.reflectSkill(skill)) {
        target = activeChar;
      }
      boolean acted = Formulas.calcMagicAffected(activeChar, target, skill);
      if ((target.isInvul()) || (!acted)) {
        activeChar.sendPacket(Static.MISSED_TARGET);
      }
      else {
        double damage = Formulas.calcManaDam(activeChar, target, skill, ss, bss);
        if (damage > target.getCurrentMp()) {
          damage = target.getCurrentMp();
        }
        target.reduceCurrentMp(damage);

        if ((damage > 0.0D) && (target.isSleeping())) {
          target.stopSleeping(null);
        }

        target.sendPacket(SystemMessage.id(SystemMessageId.MP_WAS_REDUCED_BY_S1).addNumber((int)damage));
        if (activeChar.isPlayer())
          activeChar.sendPacket(SystemMessage.id(SystemMessageId.YOUR_OPPONENTS_MP_WAS_REDUCED_BY_S1).addNumber((int)damage));
      }
    }
  }

  public L2Skill.SkillType[] getSkillIds()
  {
    return SKILL_IDS;
  }
}