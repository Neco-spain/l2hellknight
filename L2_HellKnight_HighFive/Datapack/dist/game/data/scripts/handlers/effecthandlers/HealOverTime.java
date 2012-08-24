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

import l2.hellknight.gameserver.model.effects.EffectTemplate;
import l2.hellknight.gameserver.model.effects.L2Effect;
import l2.hellknight.gameserver.model.effects.L2EffectType;
import l2.hellknight.gameserver.model.stats.Env;
import l2.hellknight.gameserver.network.serverpackets.ExRegMax;
import l2.hellknight.gameserver.network.serverpackets.StatusUpdate;

public class HealOverTime extends L2Effect
{
	public HealOverTime(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	// Special constructor to steal this effect
	public HealOverTime(Env env, L2Effect effect)
	{
		super(env, effect);
	}
	
	@Override
	protected boolean effectCanBeStolen()
	{
		return true;
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.HEAL_OVER_TIME;
	}
	
	@Override
	public boolean onStart()
	{
		if (getEffected().isPlayer())
		{
			getEffected().sendPacket(new ExRegMax(calc(), getTotalCount() * getAbnormalTime(), getAbnormalTime()));
		}
		return true;
	}
	
	@Override
	public boolean onActionTime()
	{
		if (getEffected().isDead())
			return false;
		
		else if (getEffected().isDoor())
			return false;
		
		double hp = getEffected().getCurrentHp();
		double maxhp = getEffected().getMaxRecoverableHp();
		
		// Not needed to set the HP and send update packet if player is already at max HP
		if (hp >= maxhp)
			return true;
		
		hp += calc();
		if (hp > maxhp)
			hp = maxhp;
		
		getEffected().setCurrentHp(hp);
		StatusUpdate suhp = new StatusUpdate(getEffected());
		suhp.addAttribute(StatusUpdate.CUR_HP, (int) hp);
		getEffected().sendPacket(suhp);
		return true;
	}
}
