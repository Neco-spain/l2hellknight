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

import l2.brick.gameserver.skills.Env;

/**
 * The Class ConditionTargetNone.
 *
 * @author mkizub
 */
public class ConditionTargetNone extends Condition
{
	
	/**
	 * Instantiates a new condition target none.
	 */
	public ConditionTargetNone()
	{
	}
	
	/* (non-Javadoc)
	 * @see l2.brick.gameserver.skills.conditions.Condition#testImpl(l2.brick.gameserver.skills.Env)
	 */
	@Override
	public boolean testImpl(Env env)
	{
		return (env.target == null);
	}
}
