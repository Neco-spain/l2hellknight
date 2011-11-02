/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2js.gameserver.handler.skillhandlers;

import com.l2js.gameserver.datatables.SkillTable;
import com.l2js.gameserver.handler.ISkillHandler;
import com.l2js.gameserver.model.L2Object;
import com.l2js.gameserver.model.L2Skill;
import com.l2js.gameserver.model.actor.L2Character;
import com.l2js.gameserver.model.actor.instance.L2PcInstance;
import com.l2js.gameserver.network.SystemMessageId;
import com.l2js.gameserver.network.serverpackets.SystemMessage;
import com.l2js.gameserver.templates.skills.L2SkillType;

/**
 * 
 * @author nBd
 */

public class Soul implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.CHARGESOUL
	};
	
	/**
	 * 
	 * @see com.l2js.gameserver.handler.ISkillHandler#useSkill(com.l2js.gameserver.model.actor.L2Character, com.l2js.gameserver.model.L2Skill, com.l2js.gameserver.model.L2Object[])
	 */
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (!(activeChar instanceof L2PcInstance) || activeChar.isAlikeDead())
			return;
		
		L2PcInstance player = (L2PcInstance) activeChar;

		int level = player.getSkillLevel(467);
		if (level > 0)
		{
			L2Skill soulmastery = SkillTable.getInstance().getInfo(467, level);
			
			if (soulmastery != null)
			{
				if (player.getSouls() < soulmastery.getNumSouls())
				{
					int count = 0;
					
					if (player.getSouls() + skill.getNumSouls() <= soulmastery.getNumSouls())
						count = skill.getNumSouls();
					else
						count = soulmastery.getNumSouls() - player.getSouls();
					
					player.increaseSouls(count);
				}
				else
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.SOUL_CANNOT_BE_INCREASED_ANYMORE);
					player.sendPacket(sm);
					return;
				}
			}
		}
	}
	
	/**
	 * 
	 * @see com.l2js.gameserver.handler.ISkillHandler#getSkillIds()
	 */
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
