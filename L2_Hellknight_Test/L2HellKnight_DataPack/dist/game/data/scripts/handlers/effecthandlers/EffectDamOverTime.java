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
package handlers.effecthandlers;

import l2.hellknight.gameserver.model.L2Effect;
import l2.hellknight.gameserver.network.SystemMessageId;
import l2.hellknight.gameserver.network.serverpackets.SystemMessage;
import l2.hellknight.gameserver.skills.Env;
import l2.hellknight.gameserver.templates.effects.EffectTemplate;
import l2.hellknight.gameserver.templates.skills.L2EffectType;

public class EffectDamOverTime extends L2Effect
{
	public EffectDamOverTime(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	/**
	 * 
	 * @see l2.hellknight.gameserver.model.L2Effect#getEffectType()
	 */
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.DMG_OVER_TIME;
	}
	
	/**
	 * 
	 * @see l2.hellknight.gameserver.model.L2Effect#onActionTime()
	 */
	@Override
	public boolean onActionTime()
	{
		if (getEffected().isDead())
			return false;
		
		double damage = calc();
		if (damage >= getEffected().getCurrentHp() - 1)
		{
			if (getSkill().isToggle())
			{
				getEffected().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SKILL_REMOVED_DUE_LACK_HP));
				return false;
			}
			
			// For DOT skills that will not kill effected player.
			if (!getSkill().killByDOT())
			{
				// Fix for players dying by DOTs if HP < 1 since reduceCurrentHP method will kill them
				if (getEffected().getCurrentHp() <= 1)
					return true;
				
				damage = getEffected().getCurrentHp() - 1;
			}
		}
		getEffected().reduceCurrentHpByDOT(damage, getEffector(), getSkill());
		
		return true;
	}
}
