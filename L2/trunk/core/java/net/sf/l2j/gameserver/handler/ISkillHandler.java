package net.sf.l2j.gameserver.handler;

import java.io.IOException;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;

public interface ISkillHandler
{
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets) throws IOException;
	public SkillType[] getSkillIds();
}
