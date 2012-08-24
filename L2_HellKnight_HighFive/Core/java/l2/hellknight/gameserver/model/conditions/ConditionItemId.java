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
package l2.hellknight.gameserver.model.conditions;

import l2.hellknight.gameserver.model.stats.Env;

/**
 * The Class ConditionItemId.
 * @author mkizub
 */
public final class ConditionItemId extends Condition
{
	private final int _itemId;
	
	/**
	 * Instantiates a new condition item id.
	 * @param itemId the item id
	 */
	public ConditionItemId(int itemId)
	{
		_itemId = itemId;
	}
	
	/**
	 * Test impl.
	 * @param env the env
	 * @return true, if successful
	 * @see l2.hellknight.gameserver.model.conditions.Condition#testImpl(l2.hellknight.gameserver.model.stats.Env)
	 */
	@Override
	public boolean testImpl(Env env)
	{
		return (env.getItem() != null) && (env.getItem().getItemId() == _itemId);
	}
}
