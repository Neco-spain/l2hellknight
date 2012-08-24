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
package handlers.skillhandlers;

import l2.hellknight.gameserver.handler.ISkillHandler;
import l2.hellknight.gameserver.handler.SkillHandler;
import l2.hellknight.gameserver.model.L2Object;
import l2.hellknight.gameserver.model.actor.L2Character;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.skills.L2Skill;
import l2.hellknight.gameserver.model.skills.L2SkillType;
import l2.hellknight.gameserver.network.serverpackets.StatusUpdate;

/**
 * @author earendil
 * @version $Revision: 1.1.2.2.2.4 $ $Date: 2005/04/06 16:13:48 $
 */
public class BalanceLife implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.BALANCE_LIFE
	};
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		// L2Character activeChar = activeChar;
		// check for other effects
		ISkillHandler handler = SkillHandler.getInstance().getHandler(L2SkillType.BUFF);
		
		if (handler != null)
			handler.useSkill(activeChar, skill, targets);
		
		L2PcInstance player = null;
		if (activeChar.isPlayer())
			player = activeChar.getActingPlayer();
		
		double fullHP = 0;
		double currentHPs = 0;
		
		for (L2Character target: (L2Character[]) targets)
		{
			// We should not heal if char is dead/
			if (target == null || target.isDead())
				continue;
			
			// Player holding a cursed weapon can't be healed and can't heal
			if (target != activeChar)
			{
				if (target.isPlayer() && target.getActingPlayer().isCursedWeaponEquipped())
					continue;
				else if (player != null && player.isCursedWeaponEquipped())
					continue;
			}
			
			fullHP += target.getMaxHp();
			currentHPs += target.getCurrentHp();
		}
		
		double percentHP = currentHPs / fullHP;
		
		for (L2Character target: (L2Character[]) targets)
		{
			if (target == null || target.isDead())
				continue;
			
			// Player holding a cursed weapon can't be healed and can't heal
			if (target != activeChar)
			{
				if (target.isPlayer() && target.getActingPlayer().isCursedWeaponEquipped())
					continue;
				else if (player != null && player.isCursedWeaponEquipped())
					continue;
			}
			
			double newHP = target.getMaxHp() * percentHP;
			
			if (newHP > target.getCurrentHp()) // The target gets healed
			{
				// The heal will be blocked if the current hp passes the limit
				if (target.getCurrentHp() > target.getMaxRecoverableHp())
					newHP = target.getCurrentHp();
				// Else dont let the newHP pass the limit
				else if (newHP > target.getMaxRecoverableHp())
					newHP = target.getMaxRecoverableHp();
			}
			
			target.setCurrentHp(newHP);
			StatusUpdate su = new StatusUpdate(target);
			su.addAttribute(StatusUpdate.CUR_HP, (int) target.getCurrentHp());
			target.sendPacket(su);
		}
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
