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
package com.l2js.gameserver.skills.conditions;

import com.l2js.gameserver.model.actor.instance.L2PcInstance;
import com.l2js.gameserver.model.entity.event.LMEvent;
import com.l2js.gameserver.skills.Env;

/**
 * @author L0ngh0rn
 *
 */
public class ConditionPlayerLMEvent extends Condition
{
	private final boolean _val;

	/**
	 * Instantiates a new condition player lm event.
	 *
	 * @param val the boolean
	 */
	public ConditionPlayerLMEvent(boolean val)
	{
		_val = val;
	}

	@Override
	public boolean testImpl(Env env)
	{
		final L2PcInstance player = env.player.getActingPlayer();
		if (player == null || !LMEvent.isStarted())
			return !_val;

		return (LMEvent.isPlayerParticipant(player) == _val);
	}
}
