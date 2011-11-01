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

import l2.hellknight.gameserver.ai.CtrlIntention;
import l2.hellknight.gameserver.model.L2Effect;
import l2.hellknight.gameserver.model.actor.L2Character;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.network.serverpackets.DeleteObject;
import l2.hellknight.gameserver.network.serverpackets.L2GameServerPacket;
import l2.hellknight.gameserver.skills.AbnormalEffect;
import l2.hellknight.gameserver.skills.Env;
import l2.hellknight.gameserver.templates.effects.EffectTemplate;
import l2.hellknight.gameserver.templates.skills.L2EffectType;

/**
 *
 * @author ZaKaX - nBd
 */
public class EffectHide extends L2Effect
{
	public EffectHide(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	public EffectHide(Env env, L2Effect effect)
	{
		super(env, effect);
	}
	
	/**
	 * 
	 * @see l2.hellknight.gameserver.model.L2Effect#getEffectType()
	 */
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.HIDE;
	}
	
	/**
	 * 
	 * @see l2.hellknight.gameserver.model.L2Effect#onStart()
	 */
	@Override
	public boolean onStart()
	{
		if (getEffected() instanceof L2PcInstance)
		{
			L2PcInstance activeChar = ((L2PcInstance) getEffected());
			activeChar.getAppearance().setInvisible();
			activeChar.startAbnormalEffect(AbnormalEffect.STEALTH);
			
			if (activeChar.getAI().getNextIntention() != null
					&& activeChar.getAI().getNextIntention().getCtrlIntention() == CtrlIntention.AI_INTENTION_ATTACK)
				activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			
			L2GameServerPacket del = new DeleteObject(activeChar);
			for (L2Character target : activeChar.getKnownList().getKnownCharacters())
			{
				try
				{
					if (target.getTarget() == activeChar)
					{
						target.setTarget(null);
						target.abortAttack();
						target.abortCast();
						target.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
					}
					
					if (target instanceof L2PcInstance)
						target.sendPacket(del);
				}
				catch (NullPointerException e)
				{
				}
			}
		}
		return true;
	}
	
	/**
	 * 
	 * @see l2.hellknight.gameserver.model.L2Effect#onExit()
	 */
	@Override
	public void onExit()
	{
		if (getEffected() instanceof L2PcInstance)
		{
			L2PcInstance activeChar = ((L2PcInstance) getEffected());
			if (!activeChar.inObserverMode())
				activeChar.getAppearance().setVisible();
			activeChar.stopAbnormalEffect(AbnormalEffect.STEALTH);
		}
	}
	
	/**
	 * 
	 * @see l2.hellknight.gameserver.model.L2Effect#onActionTime()
	 */
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}