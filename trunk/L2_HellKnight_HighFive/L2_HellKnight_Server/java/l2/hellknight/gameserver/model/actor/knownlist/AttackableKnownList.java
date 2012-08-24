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
package l2.hellknight.gameserver.model.actor.knownlist;

import java.util.Collection;

import l2.hellknight.gameserver.ai.CtrlIntention;
import l2.hellknight.gameserver.instancemanager.WalkingManager;
import l2.hellknight.gameserver.model.L2Object;
import l2.hellknight.gameserver.model.actor.L2Attackable;
import l2.hellknight.gameserver.model.actor.L2Character;
import l2.hellknight.gameserver.model.actor.L2Playable;
import l2.hellknight.gameserver.model.actor.instance.L2NpcInstance;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;

public class AttackableKnownList extends NpcKnownList
{
	public AttackableKnownList(L2Attackable activeChar)
	{
		super(activeChar);
	}
	
	@Override
	protected boolean removeKnownObject(L2Object object, boolean forget)
	{
		if (!super.removeKnownObject(object, forget))
			return false;
		
		// Remove the L2Object from the _aggrolist of the L2Attackable
		if (object instanceof L2Character)
			getActiveChar().getAggroList().remove(object);
		// Set the L2Attackable Intention to AI_INTENTION_IDLE
		final Collection<L2PcInstance> known = getKnownPlayers().values();
		
		//FIXME: This is a temporary solution && support for Walking Manager
		if (getActiveChar().hasAI() && (known == null || known.isEmpty()) && !WalkingManager.getInstance().isRegistered(getActiveChar()))
			getActiveChar().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null);
		
		return true;
	}
	
	@Override
	public L2Attackable getActiveChar()
	{
		return (L2Attackable)super.getActiveChar();
	}
	
	@Override
	public int getDistanceToForgetObject(L2Object object)
	{
		if (getActiveChar().getAggroList().get(object) != null)
			return 3000;
		
		return Math.min(2200, 2 * getDistanceToWatchObject(object));
	}
	
	@Override
	public int getDistanceToWatchObject(L2Object object)
	{
		if (object instanceof L2NpcInstance
				|| !(object instanceof L2Character))
			return 0;
		
		if (object instanceof L2Playable)
			return object.getKnownList().getDistanceToWatchObject(getActiveObject());
		
		int max = Math.max(300, Math.max(getActiveChar().getAggroRange(), Math.max(getActiveChar().getFactionRange(), getActiveChar().getEnemyRange())));
		
		return max;
	}
}
