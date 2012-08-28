package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.gameserver.ai.CtrlEvent;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Formulas;

public class Spoil
  implements ISkillHandler
{
  private static final L2Skill.SkillType[] SKILL_IDS = { L2Skill.SkillType.SPOIL };

  public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
  {
    if (!(activeChar instanceof L2PcInstance)) {
      return;
    }
    L2Object[] targetList = skill.getTargetList(activeChar);

    if (targetList == null)
    {
      return;
    }

    for (int index = 0; index < targetList.length; index++)
    {
      if (!(targetList[index] instanceof L2MonsterInstance)) {
        continue;
      }
      L2MonsterInstance target = (L2MonsterInstance)targetList[index];

      if (target.isSpoil()) {
        activeChar.sendPacket(new SystemMessage(SystemMessageId.ALREDAY_SPOILED));
      }
      else
      {
        boolean spoil = false;
        if (target.isDead())
          continue;
        spoil = Formulas.getInstance().calcMagicSuccess(activeChar, (L2Character)targetList[index], skill);

        if (spoil)
        {
          target.setSpoil(true);
          target.setIsSpoiledBy(activeChar.getObjectId());
          activeChar.sendPacket(new SystemMessage(SystemMessageId.SPOIL_SUCCESS));
        }
        else
        {
          SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
          sm.addString(target.getName());
          sm.addSkillName(skill.getDisplayId());
          activeChar.sendPacket(sm);
        }
        target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, activeChar);
      }
    }
  }

  public L2Skill.SkillType[] getSkillIds()
  {
    return SKILL_IDS;
  }
}