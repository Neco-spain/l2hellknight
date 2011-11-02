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
import com.l2js.gameserver.skills.Env;

/**
 * The Class ConditionPlayerAgathionId.
 */
public class ConditionPlayerAgathionId extends Condition
{
	private final int _agathionId;
	
	/**
	 * Instantiates a new condition player agathion id.
	 *
	 * @param agathionId the agathion id
	 */
	public ConditionPlayerAgathionId(int agathionId)
	{
		_agathionId = agathionId;
	}

	@Override
	public boolean testImpl(Env env)
	{
		return ((L2PcInstance)env.player).getAgathionId() == _agathionId;
	}
}