package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;

public class GiveSp
  implements ISkillHandler
{
  private static final L2Skill.SkillType[] SKILL_IDS = { L2Skill.SkillType.GIVE_SP };

  public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
  {
    for (L2Object obj : targets)
    {
      L2Character target = (L2Character)obj;
      if (target == null)
        continue;
      int spToAdd = (int)skill.getPower();
      target.addExpAndSp(0L, spToAdd);
    }
  }

  public L2Skill.SkillType[] getSkillIds()
  {
    return SKILL_IDS;
  }
}