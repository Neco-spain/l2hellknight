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
package l2rt.gameserver.skills.effects;

import l2rt.gameserver.model.L2Clan;
import l2rt.gameserver.model.L2Effect;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.skills.AbnormalEffect;
import l2rt.gameserver.skills.EffectType;
import l2rt.gameserver.skills.Env;

/**
 * @author ZaKaX (Ghost @ L2D)
 */
public class EffectClanGate extends L2Effect
{
	public EffectClanGate(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	/**
	 * @see com.l2emu.gameserver.model.L2Effect#onStart()
	 */
	@Override
	public void onStart()
	{
		getEffected().startAbnormalEffect(AbnormalEffect.MAGIC_CIRCLE);
		if (getEffected() instanceof L2Player)
		{
			if (((L2Player) getEffected()).getClan() != null)
			{
				L2Clan clan = ((L2Player) getEffected()).getClan();
				SystemMessage msg = new SystemMessage(SystemMessage.COURT_MAGICIAN__THE_PORTAL_HAS_BEEN_CREATED);
				clan.broadcastToOtherOnlineMembers(msg, ((L2Player) getEffected()));
			}
		}
		return;
	}

	/**
	 * @see com.l2emu.gameserver.model.L2Effect#onActionTime()
	 */
	@Override
	public boolean onActionTime()
	{
		return false;
	}

	/**
	 * @see com.l2emu.gameserver.model.L2Effect#onExit()
	 */
	@Override
	public void onExit()
	{
		getEffected().stopAbnormalEffect(AbnormalEffect.MAGIC_CIRCLE);
        super.onExit();
	}

	/**
	 * @see com.l2emu.gameserver.model.L2Effect#getEffectType()
	 */
	@Override
	public EffectType getEffectType()
	{
		return EffectType.ClanGate;
	}
}
