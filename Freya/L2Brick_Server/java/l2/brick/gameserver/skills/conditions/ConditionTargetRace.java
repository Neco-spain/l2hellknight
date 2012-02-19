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
package l2.brick.gameserver.skills.conditions;

import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.base.Race;
import l2.brick.gameserver.skills.Env;
import l2.brick.gameserver.util.Util;

/**
 * The Class ConditionTargetRace.
 *
 * @author mkizub
 */
public class ConditionTargetRace extends Condition
{
	
	private final Race[] _races;
	
	/**
	 * Instantiates a new condition target race.
	 *
	 * @param race the race
	 */
	public ConditionTargetRace(Race[] races)
	{
		_races = races;
	}
	
	/* (non-Javadoc)
	 * @see l2.brick.gameserver.skills.conditions.Condition#testImpl(l2.brick.gameserver.skills.Env)
	 */
	@Override
	public boolean testImpl(Env env)
	{
		if (!(env.target instanceof L2PcInstance))
			return false;
		return Util.contains(_races, env.target.getActingPlayer().getRace());
	}
}
