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
package l2.hellknight.gameserver.skills.effects;

import l2.hellknight.gameserver.model.CharEffectList;
import l2.hellknight.gameserver.model.L2Effect;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.network.serverpackets.EtcStatusUpdate;
import l2.hellknight.gameserver.skills.Env;
import l2.hellknight.gameserver.templates.EffectTemplate;
import l2.hellknight.gameserver.templates.L2EffectType;

/**
 * 
 * @author nBd
 */
public class EffectCharmOfCourage extends L2Effect
{
	public EffectCharmOfCourage(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	/**
	 * @see l2.hellknight.gameserver.model.L2Effect#getEffectType()
	 */
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.CHARMOFCOURAGE;
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
			getEffected().broadcastPacket(new EtcStatusUpdate((L2PcInstance) getEffected()));
			return true;
		}
		return false;
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
			getEffected().broadcastPacket(new EtcStatusUpdate((L2PcInstance) getEffected()));
		}
	}
	
	/**
	 * @see l2.hellknight.gameserver.model.L2Effect#onActionTime()
	 */
	@Override
	public boolean onActionTime()
	{
		return false;
	}

	/* (non-Javadoc)
	 * @see l2.hellknight.gameserver.model.L2Effect#getEffectFlags()
	 */
	@Override
	public int getEffectFlags()
	{
		return CharEffectList.EFFECT_FLAG_CHARM_OF_COURAGE;
	}
}
