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

import l2.hellknight.gameserver.handler.ITargetTypeHandler;
import l2.hellknight.gameserver.model.L2Object;
import l2.hellknight.gameserver.model.L2Skill;
import l2.hellknight.gameserver.model.actor.L2Character;
import l2.hellknight.gameserver.model.actor.L2Summon;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.network.SystemMessageId;
import l2.hellknight.gameserver.network.serverpackets.SystemMessage;
import l2.hellknight.gameserver.templates.skills.L2TargetType;

/**
 * @author UnAfraid
 */
public class TargetPartyMember implements ITargetTypeHandler
{
	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		if ((target != null
				&& target == activeChar)
				|| (target != null
						&& activeChar.isInParty()
						&& target.isInParty()
						&& activeChar.getParty().getPartyLeaderOID() == target.getParty().getPartyLeaderOID())
						|| (target != null
								&& activeChar instanceof L2PcInstance
								&& target instanceof L2Summon
								&& activeChar.getPet() == target)
								|| (target != null
										&& activeChar instanceof L2Summon
										&& target instanceof L2PcInstance
										&& activeChar == target.getPet()))
		{
			if (!target.isDead())
			{
				// If a target is found, return it in a table else send a system message TARGET_IS_INCORRECT
				return new L2Character[] { target };
			}
			else
				return _emptyTargetList;
		}
		else
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
			return _emptyTargetList;
		}
	}
	
	@Override
	public Enum<L2TargetType> getTargetType()
	{
		return L2TargetType.TARGET_PARTY_MEMBER;
	}
}
