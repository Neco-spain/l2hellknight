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

import java.util.Collection;
import java.util.List;

import javolution.util.FastList;

import l2.brick.gameserver.handler.ITargetTypeHandler;
import l2.brick.gameserver.model.L2Object;
import l2.brick.gameserver.model.L2Skill;
import l2.brick.gameserver.model.actor.L2Character;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.templates.skills.L2TargetType;
import l2.brick.gameserver.util.Util;

/**
 * @author UnAfraid
 */
public class TargetClanMember implements ITargetTypeHandler
{
	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		List<L2Character> targetList = new FastList<L2Character>();
		if (activeChar instanceof L2Npc)
		{
			// for buff purposes, returns friendly mobs nearby and mob itself
			final L2Npc npc = (L2Npc) activeChar;
			if (npc.getFactionId() == null || npc.getFactionId().isEmpty())
			{
				return new L2Character[] { activeChar };
			}
			final Collection<L2Object> objs = activeChar.getKnownList().getKnownObjects().values();
			for (L2Object newTarget : objs)
			{
				if (newTarget instanceof L2Npc && npc.getFactionId().equals(((L2Npc) newTarget).getFactionId()))
				{
					if (!Util.checkIfInRange(skill.getCastRange(), activeChar, newTarget, true))
						continue;
					if (((L2Npc) newTarget).getFirstEffect(skill) != null)
						continue;
					targetList.add((L2Npc) newTarget);
					break;
				}
			}
			if (targetList.isEmpty())
				targetList.add(npc);
		}
		else
			return _emptyTargetList;
		return targetList.toArray(new L2Character[targetList.size()]);
	}
	
	@Override
	public Enum<L2TargetType> getTargetType()
	{
		return L2TargetType.TARGET_CLAN_MEMBER;
	}
}
