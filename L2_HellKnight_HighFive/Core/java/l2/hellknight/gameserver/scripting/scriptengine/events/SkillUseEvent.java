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
package l2.hellknight.gameserver.scripting.scriptengine.events;

import l2.hellknight.gameserver.model.L2Object;
import l2.hellknight.gameserver.model.actor.L2Character;
import l2.hellknight.gameserver.model.skills.L2Skill;
import l2.hellknight.gameserver.scripting.scriptengine.events.impl.L2Event;

/**
 * @author TheOne
 */
public class SkillUseEvent implements L2Event
{
	private L2Character caster;
	private L2Skill skill;
	private L2Object[] targets;
	
	/**
	 * @return the caster
	 */
	public L2Character getCaster()
	{
		return caster;
	}
	
	/**
	 * @param caster the caster to set
	 */
	public void setCaster(L2Character caster)
	{
		this.caster = caster;
	}
	
	/**
	 * @return the targets
	 */
	public L2Object[] getTargets()
	{
		return targets;
	}
	
	/**
	 * @param targets the targets to set
	 */
	public void setTargets(L2Object[] targets)
	{
		this.targets = targets;
	}
	
	/**
	 * @return the skill
	 */
	public L2Skill getSkill()
	{
		return skill;
	}
	
	/**
	 * @param skill the skill to set
	 */
	public void setSkill(L2Skill skill)
	{
		this.skill = skill;
	}
}
