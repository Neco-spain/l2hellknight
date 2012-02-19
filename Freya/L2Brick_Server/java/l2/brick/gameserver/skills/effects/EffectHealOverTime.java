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
package l2.brick.gameserver.skills.effects;

import l2.brick.gameserver.model.L2Effect;
import l2.brick.gameserver.model.actor.instance.L2DoorInstance;
import l2.brick.gameserver.network.serverpackets.ExRegMax;
import l2.brick.gameserver.network.serverpackets.StatusUpdate;
import l2.brick.gameserver.skills.Env;
import l2.brick.gameserver.templates.EffectTemplate;
import l2.brick.gameserver.templates.L2EffectType;

public class EffectHealOverTime extends L2Effect
{
	public EffectHealOverTime(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	// Special constructor to steal this effect
	public EffectHealOverTime(Env env, L2Effect effect)
	{
		super(env, effect);
	}

	/**
	 * 
	 * @see l2.brick.gameserver.model.L2Effect#effectCanBeStolen()
	 */
	@Override
	protected boolean effectCanBeStolen()
	{
		return true;
	}

	/**
	 * 
	 * @see l2.brick.gameserver.model.L2Effect#getEffectType()
	 */
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.HEAL_OVER_TIME;
	}
	
	/**
	 * 
	 * @see l2.brick.gameserver.model.L2Effect#onStart()
	 */
	@Override
	public boolean onStart()
	{
		getEffected().sendPacket(new ExRegMax(calc(), getTotalCount() * getAbnormalTime(), getAbnormalTime()));
		return true;
	}
	
	/**
	 * 
	 * @see l2.brick.gameserver.model.L2Effect#onActionTime()
	 */
	@Override
	public boolean onActionTime()
	{
		if (getEffected().isDead())
			return false;
		
		if (getEffected() instanceof L2DoorInstance)
			return false;
		
		double hp = getEffected().getCurrentHp();
		double maxhp = getEffected().getMaxHp();
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
