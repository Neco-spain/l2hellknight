package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Formulas;

public class Manadam
  implements ISkillHandler
{
  private static final L2Skill.SkillType[] SKILL_IDS = { L2Skill.SkillType.MANADAM };

  public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
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
    for (int index = 0; index < targets.length; index++)
    {
      target = (L2Character)targets[index];

      if (target.reflectSkill(skill)) {
        target = activeChar;
      }
      boolean acted = Formulas.getInstance().calcMagicAffected(activeChar, target, skill);
      if ((target.isInvul()) || (!acted))
      {
        activeChar.sendPacket(new SystemMessage(SystemMessageId.MISSED_TARGET));
      }
      else {
        double damage = Formulas.getInstance().calcManaDam(activeChar, target, skill, ss, bss);
        if (Formulas.getInstance().calcMCrit(activeChar.getMCriticalHit(target, skill)))
          damage *= Config.M_CRIT_DAMAGE;
        double mp = damage > target.getCurrentMp() ? target.getCurrentMp() : damage;
        target.reduceCurrentMp(mp);
        if (damage > 0.0D)
        {
          if (target.isSleeping()) target.stopSleeping(null);
          if (target.isMeditation()) target.stopMeditation(null);
        }

        StatusUpdate sump = new StatusUpdate(target.getObjectId());
        sump.addAttribute(11, (int)target.getCurrentMp());

        target.sendPacket(sump);
        SystemMessage sm = new SystemMessage(SystemMessageId.S2_MP_HAS_BEEN_DRAINED_BY_S1);
        if ((activeChar instanceof L2NpcInstance))
        {
          int mobId = ((L2NpcInstance)activeChar).getNpcId();
          sm.addNpcName(mobId);
        }
        else if ((activeChar instanceof L2Summon))
        {
          int mobId = ((L2Summon)activeChar).getNpcId();
          sm.addNpcName(mobId);
        }
        else
        {
          sm.addString(activeChar.getName());
        }
        sm.addNumber((int)mp);
        target.sendPacket(sm);
        if (!(activeChar instanceof L2PcInstance))
          continue;
        SystemMessage sm2 = new SystemMessage(SystemMessageId.YOUR_OPPONENTS_MP_WAS_REDUCED_BY_S1);
        sm2.addNumber((int)mp);
        activeChar.sendPacket(sm2);
      }
    }
  }

  public L2Skill.SkillType[] getSkillIds()
  {
    return SKILL_IDS;
  }
}