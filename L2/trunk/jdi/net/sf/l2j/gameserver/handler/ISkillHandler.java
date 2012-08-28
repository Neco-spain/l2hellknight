package net.sf.l2j.gameserver.handler;

import java.io.IOException;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;

public abstract interface ISkillHandler
{
  public abstract void useSkill(L2Character paramL2Character, L2Skill paramL2Skill, L2Object[] paramArrayOfL2Object)
    throws IOException;

  public abstract L2Skill.SkillType[] getSkillIds();
}