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
package handlers.effecthandlers;

import l2.hellknight.gameserver.model.actor.L2Character;
import l2.hellknight.gameserver.model.effects.EffectTemplate;
import l2.hellknight.gameserver.model.effects.L2Effect;
import l2.hellknight.gameserver.model.effects.L2EffectType;
import l2.hellknight.gameserver.model.stats.Env;
import l2.hellknight.gameserver.network.SystemMessageId;
import l2.hellknight.gameserver.network.serverpackets.StatusUpdate;
import l2.hellknight.gameserver.network.serverpackets.SystemMessage;

/**
 * @author UnAfraid
 */
public class HealPercent extends L2Effect
{
	public HealPercent(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.HEAL_PERCENT;
	}
	
	@Override
	public boolean onStart()
	{
		L2Character target = getEffected();
		if (target == null || target.isDead() || target.isDoor())
			return false;
		
		StatusUpdate su = new StatusUpdate(target);
		double amount = 0;
		double power = calc();
		boolean full = (power == 100.0);
		
		if (full)
			amount = target.getMaxHp();
		else
			amount = target.getMaxHp() * power / 100.0;
		
		amount = Math.min(amount, target.getMaxRecoverableHp() - target.getCurrentHp());
		
		// Prevent negative amounts
		if (amount < 0)
			amount = 0;
		
		// To prevent -value heals, set the value only if current hp is less than max recoverable.
		if (target.getCurrentHp() < target.getMaxRecoverableHp())
			target.setCurrentHp(amount + target.getCurrentHp());
		
		SystemMessage sm;
		if (getEffector().getObjectId() != target.getObjectId())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.S2_HP_RESTORED_BY_C1);
			sm.addCharName(getEffector());
		}
		else
			sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HP_RESTORED);
		
		sm.addNumber((int)amount);
		target.sendPacket(sm);
		su.addAttribute(StatusUpdate.CUR_HP, (int) target.getCurrentHp());
		target.sendPacket(su);
		
		return true;
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}
