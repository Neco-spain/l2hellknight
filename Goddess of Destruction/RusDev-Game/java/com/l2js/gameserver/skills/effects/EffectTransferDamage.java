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

import com.l2js.gameserver.model.L2Effect;
import com.l2js.gameserver.model.actor.L2Playable;
import com.l2js.gameserver.model.actor.instance.L2PcInstance;
import com.l2js.gameserver.skills.Env;
import com.l2js.gameserver.templates.effects.EffectTemplate;
import com.l2js.gameserver.templates.skills.L2EffectType;

/**
 * @author UnAfraid
 */
public class EffectTransferDamage extends L2Effect
{
	public EffectTransferDamage(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	public EffectTransferDamage(Env env, L2Effect effect)
	{
		super(env, effect);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.DAMAGE_TRANSFER;
	}

	@Override
	public boolean onStart()
	{
		if (getEffected() instanceof L2Playable && getEffector() instanceof L2PcInstance)
			((L2Playable) getEffected()).setTransferDamageTo((L2PcInstance) getEffector());
		return true;
	}

	@Override
	public void onExit()
	{
		if (getEffected() instanceof L2Playable && getEffector() instanceof L2PcInstance)
			((L2Playable) getEffected()).setTransferDamageTo(null);
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}