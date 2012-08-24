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
package handlers.targethandlers;

import java.util.List;

import javolution.util.FastList;

import l2.hellknight.gameserver.handler.ITargetTypeHandler;
import l2.hellknight.gameserver.model.L2Object;
import l2.hellknight.gameserver.model.actor.L2Character;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.actor.instance.L2PetInstance;
import l2.hellknight.gameserver.model.skills.L2Skill;
import l2.hellknight.gameserver.model.skills.L2SkillType;
import l2.hellknight.gameserver.model.skills.targets.L2TargetType;
import l2.hellknight.gameserver.network.SystemMessageId;

/**
 * @author UnAfraid
 */
public class TargetCorpsePlayer implements ITargetTypeHandler
{
	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		List<L2Character> targetList = new FastList<>();
		if (target != null && target.isDead())
		{
			final L2PcInstance player;
			if (activeChar instanceof L2PcInstance)
				player = (L2PcInstance) activeChar;
			else
				player = null;
			
			final L2PcInstance targetPlayer;
			if (target instanceof L2PcInstance)
				targetPlayer = (L2PcInstance) target;
			else
				targetPlayer = null;
			
			final L2PetInstance targetPet;
			if (target instanceof L2PetInstance)
				targetPet = (L2PetInstance) target;
			else
				targetPet = null;
			
			if (player != null && (targetPlayer != null || targetPet != null))
			{
				boolean condGood = true;
				
				if (skill.getSkillType() == L2SkillType.RESURRECT)
				{
					if (targetPlayer != null)
					{
						// check target is not in a active siege zone
						if (targetPlayer.isInsideZone(L2Character.ZONE_SIEGE) && !targetPlayer.isInSiege())
						{
							condGood = false;
							activeChar.sendPacket(SystemMessageId.CANNOT_BE_RESURRECTED_DURING_SIEGE);
						}
						
						if (targetPlayer.isFestivalParticipant()) // Check to see if the current player target is in a festival.
						{
							condGood = false;
							activeChar.sendMessage("You may not resurrect participants in a festival.");
						}
						if (targetPlayer.isReviveRequested())
						{
							if (targetPlayer.isRevivingPet())
								player.sendPacket(SystemMessageId.MASTER_CANNOT_RES); // While a pet is attempting to resurrect, it cannot help in resurrecting its master.
							else
								player.sendPacket(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED); // Resurrection is already been proposed.
							condGood = false;
						}
					}
					else if (targetPet != null)
					{
						if (targetPet.getOwner() != player)
						{
							if (targetPet.getOwner().isReviveRequested())
							{
								if (targetPet.getOwner().isRevivingPet())
									player.sendPacket(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED); // Resurrection is already been proposed.
								else
									player.sendPacket(SystemMessageId.CANNOT_RES_PET2); // A pet cannot be resurrected while it's owner is in the process of resurrecting.
								condGood = false;
							}
						}
					}
				}
				
				if (condGood)
				{
					if (!onlyFirst)
					{
						targetList.add(target);
						return targetList.toArray(new L2Object[targetList.size()]);
					}
					return new L2Character[] { target };
				}
			}
		}
		activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
		return _emptyTargetList;
	}
	
	@Override
	public Enum<L2TargetType> getTargetType()
	{
		return L2TargetType.TARGET_CORPSE_PLAYER;
	}
}
