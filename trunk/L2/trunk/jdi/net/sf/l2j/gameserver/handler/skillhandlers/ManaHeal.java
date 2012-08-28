package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Stats;

public class ManaHeal
  implements ISkillHandler
{
  private static final L2Skill.SkillType[] SKILL_IDS = { L2Skill.SkillType.MANAHEAL, L2Skill.SkillType.MANARECHARGE, L2Skill.SkillType.MANAHEAL_PERCENT };

  public void useSkill(L2Character actChar, L2Skill skill, L2Object[] targets)
  {
    L2Character target = null;

    for (int index = 0; index < targets.length; index++)
    {
      target = (L2Character)targets[index];
      double mp = skill.getPower();
      if (skill.getSkillType() == L2Skill.SkillType.MANAHEAL_PERCENT)
      {
        mp = target.getMaxMp() * mp / 100.0D;
      }
      else {
        mp = skill.getSkillType() == L2Skill.SkillType.MANARECHARGE ? target.calcStat(Stats.RECHARGE_MP_RATE, mp, null, null) : mp;
      }

      target.setLastHealAmount((int)mp);
      target.setCurrentMp(mp + target.getCurrentMp());
      StatusUpdate sump = new StatusUpdate(target.getObjectId());
      sump.addAttribute(11, (int)target.getCurrentMp());
      target.sendPacket(sump);

      if (((actChar instanceof L2PcInstance)) && (actChar != target))
      {
        SystemMessage sm = new SystemMessage(SystemMessageId.S2_MP_RESTORED_BY_S1);
        sm.addString(actChar.getName());
        sm.addNumber((int)mp);
        target.sendPacket(sm);
      }
      else
      {
        SystemMessage sm = new SystemMessage(SystemMessageId.S1_MP_RESTORED);
        sm.addNumber((int)mp);
        target.sendPacket(sm);
      }
    }
  }

  public L2Skill.SkillType[] getSkillIds()
  {
    return SKILL_IDS;
  }
}