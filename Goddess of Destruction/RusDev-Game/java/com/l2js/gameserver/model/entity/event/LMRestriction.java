/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2js.gameserver.model.entity.event;

import com.l2js.gameserver.model.actor.instance.L2PcInstance;
import com.l2js.gameserver.model.restriction.AbstractRestriction;

/**
 * @author L0ngh0rn
 */
public final class LMRestriction extends AbstractRestriction
{
	private static final class SingletonHolder
	{
		private static final LMRestriction INSTANCE = new LMRestriction();
	}
	
	public static LMRestriction getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	public LMRestriction()
	{
	}
	
	@Override
	public boolean fakePvPZone(L2PcInstance activeChar, L2PcInstance target)
	{
		if ((LMEvent.isStarted()) && (LMEvent.isPlayerParticipant(activeChar) && LMEvent.isPlayerParticipant(target)))
			return true;
		
		return false;
	}
}
