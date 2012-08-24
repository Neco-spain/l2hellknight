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
import java.util.concurrent.ScheduledFuture;

import l2.hellknight.gameserver.ThreadPoolManager;
import l2.hellknight.gameserver.ai.CtrlIntention;
import l2.hellknight.gameserver.instancemanager.WalkingManager;
import l2.hellknight.gameserver.model.L2Object;
import l2.hellknight.gameserver.model.actor.L2Attackable;
import l2.hellknight.gameserver.model.actor.L2Character;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.L2Playable;
import l2.hellknight.gameserver.model.actor.instance.L2CabaleBufferInstance;
import l2.hellknight.gameserver.model.actor.instance.L2FestivalGuideInstance;
import l2.hellknight.gameserver.model.actor.instance.L2NpcInstance;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;

public class NpcKnownList extends CharKnownList
{
	private ScheduledFuture<?> _trackingTask = null;
	
	public NpcKnownList(L2Npc activeChar)
	{
		super(activeChar);
	}
	
	@Override
	public L2Npc getActiveChar()
	{
		return (L2Npc) super.getActiveChar();
	}
	
	@Override
	public int getDistanceToForgetObject(L2Object object)
	{
		return 2 * getDistanceToWatchObject(object);
	}
	
	@Override
	public int getDistanceToWatchObject(L2Object object)
	{
		if (object instanceof L2FestivalGuideInstance)
		{
			return 4000;
		}
		
		if ((object instanceof L2NpcInstance) || !(object instanceof L2Character))
		{
			return 0;
		}
		
		if (object instanceof L2CabaleBufferInstance)
		{
			return 900;
		}
		
		if (object instanceof L2Playable)
		{
			return 1500;
		}
		
		return 500;
	}
	
	//L2Master mod - support for Walking monsters aggro
	public void startTrackingTask()
	{
		if ((_trackingTask == null) && (getActiveChar().getAggroRange() > 0))
		{
			_trackingTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new TrackingTask(), 2000, 2000);
		}
	}
	
	//L2Master mod - support for Walking monsters aggro
	public void stopTrackingTask()
	{
		if (_trackingTask != null)
		{
			_trackingTask.cancel(true);
			_trackingTask = null;
		}
	}
	
	//L2Master mod - support for Walking monsters aggro
	private class TrackingTask implements Runnable
	{
		public TrackingTask()
		{
			//
		}
		
		@Override
		public void run()
		{
			if (getActiveChar() instanceof L2Attackable)
			{
				final L2Attackable monster = (L2Attackable) getActiveChar();
				if (monster.getAI().getIntention() == CtrlIntention.AI_INTENTION_MOVE_TO)
				{
					final Collection<L2PcInstance> players = getKnownPlayers().values();
					if (players != null)
					{
						for (L2PcInstance pl : players)
						{
							if (pl.isInsideRadius(monster, monster.getAggroRange(), true, false) && !pl.isDead() && !pl.isInvul())
							{
								WalkingManager.getInstance().stopMoving(getActiveChar(), false);
								monster.addDamageHate(pl, 0, 100);
								monster.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, pl, null);
								break;
							}
						}
					}
				}
			}
		}
	}
}
