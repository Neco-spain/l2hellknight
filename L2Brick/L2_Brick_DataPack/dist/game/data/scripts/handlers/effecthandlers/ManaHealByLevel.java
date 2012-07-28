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

import l2.brick.gameserver.model.L2Effect;
import l2.brick.gameserver.model.actor.L2Character;
import l2.brick.gameserver.model.actor.instance.L2DoorInstance;
import l2.brick.gameserver.network.SystemMessageId;
import l2.brick.gameserver.network.serverpackets.StatusUpdate;
import l2.brick.gameserver.network.serverpackets.SystemMessage;
import l2.brick.gameserver.skills.Env;
import l2.brick.gameserver.skills.Stats;
import l2.brick.gameserver.templates.effects.EffectTemplate;
import l2.brick.gameserver.templates.skills.L2EffectType;

/**
 * @author UnAfraid
 *
 */
public class ManaHealByLevel extends L2Effect
{
	public ManaHealByLevel(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.MANAHEAL_BY_LEVEL;
	}
	
	@Override
	public boolean onStart()
	{
		L2Character target = getEffected();
		if (target == null || target.isDead() || target instanceof L2DoorInstance)
			return false;
	
		StatusUpdate su = new StatusUpdate(target);
		
		double amount = calc();
		
		//recharged mp influenced by difference between target level and skill level
		//if target is within 5 levels or lower then skill level there's no penalty.
		amount = target.calcStat(Stats.RECHARGE_MP_RATE, amount, null, null);  
		if (target.getLevel() > getSkill().getMagicLevel())
		{
			int lvlDiff = target.getLevel() - getSkill().getMagicLevel();
			//if target is too high compared to skill level, the amount of recharged mp gradually decreases.
			if (lvlDiff == 6)		//6 levels difference:
				amount *= 0.9;			//only 90% effective
			else if (lvlDiff == 7)
				amount *= 0.8;			//80%
			else if (lvlDiff == 8)
				amount *= 0.7;			//70%
			else if (lvlDiff == 9)
				amount *= 0.6;			//60%
			else if (lvlDiff == 10)
				amount *= 0.5;			//50%
			else if (lvlDiff == 11)
				amount *= 0.4;			//40%
			else if (lvlDiff == 12)
				amount *= 0.3;			//30%
			else if (lvlDiff == 13)
				amount *= 0.2;			//20%
			else if (lvlDiff == 14)
				amount *= 0.1;			//10%
			
			else if (lvlDiff >= 15)	//15 levels or more:
				amount = 0;				//0mp recharged
		}
		
		amount = Math.min(amount, target.getMaxRecoverableMp() - target.getCurrentMp());
		
		// Prevent negative amounts
		if (amount < 0)
			amount = 0;
		
		// To prevent -value heals, set the value only if current mp is less than max recoverable.
		if (target.getCurrentMp() < target.getMaxRecoverableMp())
			target.setCurrentMp(amount + target.getCurrentMp());
		
		SystemMessage sm;
		if (getEffector().getObjectId() != target.getObjectId())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.S2_MP_RESTORED_BY_C1);
			sm.addCharName(getEffector());
		}
		else
			sm = SystemMessage.getSystemMessage(SystemMessageId.S1_MP_RESTORED);
		sm.addNumber((int) amount);
		target.sendPacket(sm);
		su.addAttribute(StatusUpdate.CUR_MP, (int) target.getCurrentMp());
		target.sendPacket(su);
		
		return true;
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}
