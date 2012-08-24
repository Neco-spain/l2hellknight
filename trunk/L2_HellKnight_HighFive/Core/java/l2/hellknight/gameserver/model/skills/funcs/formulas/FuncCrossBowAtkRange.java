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
package l2.hellknight.gameserver.model.skills.funcs.formulas;

import l2.hellknight.gameserver.model.conditions.ConditionUsingItemType;
import l2.hellknight.gameserver.model.items.type.L2WeaponType;
import l2.hellknight.gameserver.model.skills.funcs.Func;
import l2.hellknight.gameserver.model.stats.Env;
import l2.hellknight.gameserver.model.stats.Stats;

/**
 * @author UnAfraid
 */
public class FuncCrossBowAtkRange extends Func
{
	private static final FuncCrossBowAtkRange _fcb_instance = new FuncCrossBowAtkRange();
	
	public static Func getInstance()
	{
		return _fcb_instance;
	}
	
	private FuncCrossBowAtkRange()
	{
		super(Stats.POWER_ATTACK_RANGE, 0x10, null);
		setCondition(new ConditionUsingItemType(L2WeaponType.CROSSBOW.mask()));
	}
	
	@Override
	public void calc(Env env)
	{
		if (!cond.test(env))
		{
			return;
		}
		// default is 40 and with crossbow should be 400
		env.addValue(360);
	}
}