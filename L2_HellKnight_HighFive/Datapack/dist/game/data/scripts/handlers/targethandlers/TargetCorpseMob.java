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

import l2.hellknight.Config;
import l2.hellknight.gameserver.handler.ITargetTypeHandler;
import l2.hellknight.gameserver.model.L2Object;
import l2.hellknight.gameserver.model.actor.L2Attackable;
import l2.hellknight.gameserver.model.actor.L2Character;
import l2.hellknight.gameserver.model.actor.instance.L2ServitorInstance;
import l2.hellknight.gameserver.model.skills.L2Skill;
import l2.hellknight.gameserver.model.skills.targets.L2TargetType;
import l2.hellknight.gameserver.network.SystemMessageId;

/**
 * @author UnAfraid
 */
public class TargetCorpseMob implements ITargetTypeHandler
{
	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		List<L2Character> targetList = new FastList<>();
		final boolean isSummon = target instanceof L2ServitorInstance;
		if (!(isSummon || target instanceof L2Attackable) || !target.isDead())
		{
			activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			return _emptyTargetList;
		}
		
		// Corpse mob only available for half time
		switch (skill.getSkillType())
		{
			case SUMMON:
			{
				if (isSummon && ((L2ServitorInstance)target).getOwner() != null
						&& ((L2ServitorInstance)target).getOwner().getObjectId() == activeChar.getObjectId())
					return _emptyTargetList;
				
				break;
			}
			case DRAIN:
			{
				if (!((L2Attackable) target).checkCorpseTime(activeChar.getActingPlayer(), (Config.NPC_DECAY_TIME / 2), true))
				{
					return _emptyTargetList;
				}
				break;
			}
			default:
				break;
		}
		
		if (!onlyFirst)
		{
			targetList.add(target);
			return targetList.toArray(new L2Object[targetList.size()]);
		}
		return new L2Character[] { target };
	}
	
	@Override
	public Enum<L2TargetType> getTargetType()
	{
		return L2TargetType.TARGET_CORPSE_MOB;
	}
}
