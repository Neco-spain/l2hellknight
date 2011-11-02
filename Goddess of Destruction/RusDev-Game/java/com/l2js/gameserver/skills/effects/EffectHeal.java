/*
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */
package com.l2js.gameserver.skills.effects;

import com.l2js.gameserver.model.L2Effect;
import com.l2js.gameserver.network.SystemMessageId;
import com.l2js.gameserver.network.serverpackets.StatusUpdate;
import com.l2js.gameserver.network.serverpackets.SystemMessage;
import com.l2js.gameserver.skills.Env;
import com.l2js.gameserver.templates.effects.EffectTemplate;
import com.l2js.gameserver.templates.skills.L2EffectType;

public class EffectHeal extends L2Effect
{
	public EffectHeal(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.HEAL;
	}
	
	@Override
	public boolean onStart()
	{
		double hp = getEffected().getCurrentHp();
		double maxhp = getEffected().getMaxHp();
		double effhp = calc();
		if ((hp + effhp) >= maxhp)
			effhp = maxhp - hp;
		hp += effhp;
		getEffected().setCurrentHp(hp);
		StatusUpdate suhp = new StatusUpdate(getEffected().getObjectId());
		suhp.addAttribute(StatusUpdate.CUR_HP, (int) hp);
		getEffected().sendPacket(suhp);
		SystemMessage shp = SystemMessage.getSystemMessage(SystemMessageId.S1_HP_RESTORED);
		shp.addNumber((int) effhp);
		getEffected().sendPacket(shp);
		return true;
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}
