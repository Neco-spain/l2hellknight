/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2js.gameserver.handler.skillhandlers;

import com.l2js.gameserver.datatables.SkillTable;
import com.l2js.gameserver.handler.ISkillHandler;
import com.l2js.gameserver.model.L2Object;
import com.l2js.gameserver.model.L2Skill;
import com.l2js.gameserver.model.actor.L2Character;
import com.l2js.gameserver.model.actor.instance.L2PcInstance;
import com.l2js.gameserver.templates.skills.L2SkillType;

public class TransferSoul implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS = { L2SkillType.TRANSFER_SOUL };
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (activeChar == null || activeChar.isAlikeDead())
			return;
		
		for (L2Object element : targets)
		{
			L2PcInstance target = (L2PcInstance) element;
			L2PcInstance caster = (L2PcInstance) activeChar;
			
			int casterSoul = 0;
			casterSoul = caster.getSouls();
			
			L2Skill soulmastery = SkillTable.getInstance().getInfo(L2Skill.SKILL_SOUL_MASTERY, caster.getSkillLevel(L2Skill.SKILL_SOUL_MASTERY));
			if (soulmastery != null)
			{
				if (casterSoul > 0)
				{
					L2Skill soulmasteryTarget = SkillTable.getInstance().getInfo(L2Skill.SKILL_SOUL_MASTERY, target.getSkillLevel(L2Skill.SKILL_SOUL_MASTERY));
					if (soulmasteryTarget != null)
					{
						target.increaseSouls(1);
						caster.decreaseSouls(1, skill);
					}
				}
			}
		}
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
