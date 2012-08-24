/**
 * 
 */
package handlers.effecthandlers;

import l2.hellknight.gameserver.model.actor.L2Character;
import l2.hellknight.gameserver.model.effects.EffectTemplate;
import l2.hellknight.gameserver.model.effects.L2Effect;
import l2.hellknight.gameserver.model.effects.L2EffectType;
import l2.hellknight.gameserver.model.stats.Env;
import l2.hellknight.gameserver.model.stats.Stats;
import l2.hellknight.gameserver.network.SystemMessageId;
import l2.hellknight.gameserver.network.serverpackets.StatusUpdate;
import l2.hellknight.gameserver.network.serverpackets.SystemMessage;

/**
 * @author UnAfraid
 *
 */
public class ManaHeal extends L2Effect
{
	public ManaHeal(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.MANAHEAL;
	}
	
	@Override
	public boolean onStart()
	{
		L2Character target = getEffected();
		if (target == null || target.isDead() || target.isDoor())
			return false;
	
		StatusUpdate su = new StatusUpdate(target);
		
		double amount = calc();
		
		if (!getSkill().isStaticHeal())
			amount = target.calcStat(Stats.RECHARGE_MP_RATE, amount, null, null);
		
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
