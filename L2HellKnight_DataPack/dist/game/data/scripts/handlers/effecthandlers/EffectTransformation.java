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

import l2.hellknight.gameserver.instancemanager.TransformationManager;
import l2.hellknight.gameserver.model.L2Effect;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.network.SystemMessageId;
import l2.hellknight.gameserver.network.serverpackets.SystemMessage;
import l2.hellknight.gameserver.skills.Env;
import l2.hellknight.gameserver.templates.effects.EffectTemplate;
import l2.hellknight.gameserver.templates.skills.L2EffectType;

/**
 * 
 * @author nBd
 */
public class EffectTransformation extends L2Effect
{
	public EffectTransformation(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	// Special constructor to steal this effect
	public EffectTransformation(Env env, L2Effect effect)
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
		return L2EffectType.TRANSFORMATION;
	}
	
	/**
	 * 
	 * @see l2.hellknight.gameserver.model.L2Effect#onStart()
	 */
	@Override
	public boolean onStart()
	{
		if (!(getEffected() instanceof L2PcInstance))
			return false;
		
		L2PcInstance trg = (L2PcInstance) getEffected();
		if (trg == null)
			return false;
		
		if (trg.isAlikeDead() || trg.isCursedWeaponEquipped())
			return false;
		
		if (trg.getTransformation() != null)
		{
			trg.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_ALREADY_POLYMORPHED_AND_CANNOT_POLYMORPH_AGAIN));
			return false;
		}
		
		TransformationManager.getInstance().transformPlayer(getSkill().getTransformId(), trg);
		return true;
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
	
	@Override
	public void onExit()
	{
		getEffected().stopTransformation(false);
	}
}
