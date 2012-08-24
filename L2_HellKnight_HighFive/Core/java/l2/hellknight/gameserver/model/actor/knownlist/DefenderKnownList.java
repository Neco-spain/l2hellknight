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

import l2.hellknight.gameserver.ai.CtrlIntention;
import l2.hellknight.gameserver.instancemanager.TerritoryWarManager;
import l2.hellknight.gameserver.model.L2Object;
import l2.hellknight.gameserver.model.actor.L2Summon;
import l2.hellknight.gameserver.model.actor.instance.L2DefenderInstance;
import l2.hellknight.gameserver.model.actor.instance.L2FortCommanderInstance;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.entity.Castle;
import l2.hellknight.gameserver.model.entity.Fort;
import l2.hellknight.gameserver.model.entity.clanhall.SiegableHall;

public class DefenderKnownList extends AttackableKnownList
{
	public DefenderKnownList(L2DefenderInstance activeChar)
	{
		super(activeChar);
	}
	
	@Override
	public boolean addKnownObject(L2Object object)
	{
		if (!super.addKnownObject(object)) return false;
		
		Castle castle = getActiveChar().getCastle();
		Fort fortress = getActiveChar().getFort();
		SiegableHall hall = getActiveChar().getConquerableHall();
		// Check if siege is in progress
		if ((fortress != null && fortress.getZone().isActive())
				|| (castle != null && castle.getZone().isActive())
				|| (hall != null && hall.getSiegeZone().isActive()))
		{
			L2PcInstance player = null;
			if (object instanceof L2PcInstance)
				player = (L2PcInstance) object;
			else if (object instanceof L2Summon)
				player = ((L2Summon)object).getOwner();
			int activeSiegeId = (fortress != null ? fortress.getFortId() : (castle != null ? castle.getCastleId() : hall != null? hall.getId() : 0));
			
			// Check if player is an enemy of this defender npc
			if (player != null && ((player.getSiegeState() == 2 && !player.isRegisteredOnThisSiegeField(activeSiegeId))
					|| (player.getSiegeState() == 1 && !TerritoryWarManager.getInstance().isAllyField(player, activeSiegeId))
					|| player.getSiegeState() == 0))
			{
				if (getActiveChar().getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
					getActiveChar().getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
			}
		}
		return true;
	}
	
	@Override
	public final L2DefenderInstance getActiveChar()
	{
		if (super.getActiveChar() instanceof L2FortCommanderInstance)
			return (L2FortCommanderInstance)super.getActiveChar();
		return (L2DefenderInstance)super.getActiveChar();
	}
}
