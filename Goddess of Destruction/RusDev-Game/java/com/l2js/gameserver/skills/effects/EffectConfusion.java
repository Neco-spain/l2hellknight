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
package com.l2js.gameserver.skills.effects;

import java.util.Collection;
import java.util.List;

import javolution.util.FastList;

import com.l2js.gameserver.ai.CtrlIntention;
import com.l2js.gameserver.model.CharEffectList;
import com.l2js.gameserver.model.L2Effect;
import com.l2js.gameserver.model.L2Object;
import com.l2js.gameserver.model.actor.L2Character;
import com.l2js.gameserver.skills.Env;
import com.l2js.gameserver.templates.effects.EffectTemplate;
import com.l2js.gameserver.templates.skills.L2EffectType;
import com.l2js.util.Rnd;

/**
 * @author littlecrow
 * 
 *         Implementation of the Confusion Effect
 */
public class EffectConfusion extends L2Effect
{
	
	public EffectConfusion(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	/**
	 * 
	 * @see com.l2js.gameserver.model.L2Effect#getEffectType()
	 */
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.CONFUSION;
	}
	
	/**
	 * 
	 * @see com.l2js.gameserver.model.L2Effect#onStart()
	 */
	@Override
	public boolean onStart()
	{
		getEffected().startConfused();
		onActionTime();
		return true;
	}
	
	/**
	 * 
	 * @see com.l2js.gameserver.model.L2Effect#onExit()
	 */
	@Override
	public void onExit()
	{
		getEffected().stopConfused(this);
	}
	
	/**
	 * 
	 * @see com.l2js.gameserver.model.L2Effect#onActionTime()
	 */
	@Override
	public boolean onActionTime()
	{
		List<L2Character> targetList = new FastList<L2Character>();
		
		// Getting the possible targets
		
		Collection<L2Object> objs = getEffected().getKnownList().getKnownObjects().values();
		// synchronized (getEffected().getKnownList().getKnownObjects())
		{
			for (L2Object obj : objs)
			{
				if ((obj instanceof L2Character) && (obj != getEffected()))
					targetList.add((L2Character) obj);
			}
		}
		// if there is no target, exit function
		if (targetList.isEmpty())
			return true;
		
		// Choosing randomly a new target
		int nextTargetIdx = Rnd.nextInt(targetList.size());
		L2Object target = targetList.get(nextTargetIdx);
		
		// Attacking the target
		getEffected().setTarget(target);
		getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
		
		return true;
	}

	@Override
	public int getEffectFlags()
	{
		return CharEffectList.EFFECT_FLAG_CONFUSED;
	}
}
