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

import l2.hellknight.gameserver.handler.ITargetTypeHandler;
import l2.hellknight.gameserver.model.L2Object;
import l2.hellknight.gameserver.model.actor.L2Attackable;
import l2.hellknight.gameserver.model.actor.L2Character;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.L2Playable;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.skills.L2Skill;
import l2.hellknight.gameserver.model.skills.L2SkillType;
import l2.hellknight.gameserver.model.skills.targets.L2TargetType;

/**
 * @author UnAfraid
 */
public class TargetAura implements ITargetTypeHandler
{
	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		List<L2Character> targetList = new FastList<>();
		final boolean srcInArena = (activeChar.isInsideZone(L2Character.ZONE_PVP) && !activeChar.isInsideZone(L2Character.ZONE_SIEGE));
		
		final L2PcInstance sourcePlayer = activeChar.getActingPlayer();
		
		final Collection<L2Character> objs = activeChar.getKnownList().getKnownCharactersInRadius(skill.getSkillRadius());
		
		if (skill.getSkillType() == L2SkillType.DUMMY)
		{
			if (onlyFirst)
				return new L2Character[] { activeChar };
			
			targetList.add(activeChar);
			for (L2Character obj : objs)
			{
				if (!(obj == activeChar || obj == sourcePlayer || obj instanceof L2Npc || obj instanceof L2Attackable))
					continue;
				
				if (skill.getMaxTargets() > -1 && targetList.size() >= skill.getMaxTargets())
					break;
				
				targetList.add(obj);
			}
		}
		else
		{
			for (L2Character obj : objs)
			{
				if (obj instanceof L2Attackable || obj instanceof L2Playable)
				{
					if (!L2Skill.checkForAreaOffensiveSkills(activeChar, obj, skill, srcInArena))
						continue;
					
					if (onlyFirst)
						return new L2Character[] { obj };
					
					if (skill.getMaxTargets() > -1 && targetList.size() >= skill.getMaxTargets())
						break;
					targetList.add(obj);
				}
			}
		}
		return targetList.toArray(new L2Character[targetList.size()]);
	}
	
	@Override
	public Enum<L2TargetType> getTargetType()
	{
		return L2TargetType.TARGET_AURA;
	}
}
