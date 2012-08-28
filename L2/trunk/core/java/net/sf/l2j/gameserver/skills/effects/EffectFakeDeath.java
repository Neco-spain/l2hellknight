/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Env;

/**
 * @author mkizub
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
final class EffectFakeDeath extends L2Effect {

	public EffectFakeDeath(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public EffectType getEffectType()
	{
		return EffectType.FAKE_DEATH;
	}

	/** Notify started */
	@Override
	public boolean onStart() 
	{
		getEffected().startFakeDeath();
		return true;
	}

	/** Notify exited */
	@Override
	public void onExit() {
		getEffected().stopFakeDeath(this);
	}

    @Override
	public boolean onActionTime()
    {
		if(getEffected().isDead())
			return false;

		double manaDam = calc();

		if(manaDam > getEffected().getCurrentMp())
		{
			if(getSkill().isToggle())
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP);
				getEffected().sendPacket(sm);
				return false;
			}
		}

		getEffected().reduceCurrentMp(manaDam);
		return true;
    }
}


