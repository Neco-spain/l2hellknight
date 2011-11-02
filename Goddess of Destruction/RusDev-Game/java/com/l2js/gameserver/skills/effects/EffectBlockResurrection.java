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

import com.l2js.gameserver.model.CharEffectList;
import com.l2js.gameserver.model.L2Effect;
import com.l2js.gameserver.skills.Env;
import com.l2js.gameserver.templates.effects.EffectTemplate;
import com.l2js.gameserver.templates.skills.L2EffectType;

/**
 * @author UnAfraid
 */
public class EffectBlockResurrection extends L2Effect
{
	public EffectBlockResurrection(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.BLOCK_RESURRECTION;
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}

	@Override
	public int getEffectFlags()
	{
		return CharEffectList.EFFECT_FLAG_BLOCK_RESURRECTION;
	}
}
