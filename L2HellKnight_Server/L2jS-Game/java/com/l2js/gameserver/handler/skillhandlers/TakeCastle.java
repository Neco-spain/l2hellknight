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

import com.l2js.gameserver.handler.ISkillHandler;
import com.l2js.gameserver.instancemanager.CastleManager;
import com.l2js.gameserver.model.L2Object;
import com.l2js.gameserver.model.L2Skill;
import com.l2js.gameserver.model.actor.L2Character;
import com.l2js.gameserver.model.actor.instance.L2PcInstance;
import com.l2js.gameserver.model.entity.Castle;
import com.l2js.gameserver.templates.skills.L2SkillType;

/**
 * @author _drunk_
 *
 */
public class TakeCastle implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.TAKECASTLE
	};
	
	/**
	 * 
	 * @see com.l2js.gameserver.handler.ISkillHandler#useSkill(com.l2js.gameserver.model.actor.L2Character, com.l2js.gameserver.model.L2Skill, com.l2js.gameserver.model.L2Object[])
	 */
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (!(activeChar instanceof L2PcInstance))
			return;
		
		L2PcInstance player = (L2PcInstance) activeChar;
		
		if (player.getClan() == null || player.getClan().getLeaderId() != player.getObjectId())
			return;
		
		Castle castle = CastleManager.getInstance().getCastle(player);
		if (castle == null || !player.checkIfOkToCastSealOfRule(castle, true, skill))
			return;
		
		try
		{
			castle.engrave(player.getClan(), targets[0]);
		}
		catch (Exception e)
		{
			e.printStackTrace();
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
	
	
	
	public static void main(String[] args)
	{
		new TakeCastle();
	}
}
