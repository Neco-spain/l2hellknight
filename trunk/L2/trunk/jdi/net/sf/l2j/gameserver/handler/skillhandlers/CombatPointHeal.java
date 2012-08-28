package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.handler.SkillHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class CombatPointHeal
  implements ISkillHandler
{
  private static final L2Skill.SkillType[] SKILL_IDS = { L2Skill.SkillType.COMBATPOINTHEAL };

  public void useSkill(L2Character actChar, L2Skill skill, L2Object[] targets)
  {
    try
    {
      ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(L2Skill.SkillType.BUFF);

      if (handler != null) {
        handler.useSkill(actChar, skill, targets);
      }
    }
    catch (Exception e)
    {
    }
    L2Character target = null;

    for (int index = 0; index < targets.length; index++)
    {
      target = (L2Character)targets[index];

      double cp = skill.getPower();

      if ((target == null) || (target.isDead()) || (target.getCurrentHp() == 0.0D))
        continue;
      SystemMessage sm = new SystemMessage(SystemMessageId.S1_CP_WILL_BE_RESTORED);
      sm.addNumber((int)cp);
      target.sendPacket(sm);
      target.setCurrentCp(cp + target.getCurrentCp());
      StatusUpdate sump = new StatusUpdate(target.getObjectId());
      sump.addAttribute(33, (int)target.getCurrentCp());
      target.sendPacket(sump);
    }
  }

  public L2Skill.SkillType[] getSkillIds()
  {
    return SKILL_IDS;
  }
}