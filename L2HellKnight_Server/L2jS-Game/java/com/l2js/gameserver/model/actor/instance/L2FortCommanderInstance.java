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
package com.l2js.gameserver.model.actor.instance;

import javolution.util.FastList;

import com.l2js.Config;
import com.l2js.gameserver.ThreadPoolManager;
import com.l2js.gameserver.ai.CtrlIntention;
import com.l2js.gameserver.instancemanager.FortSiegeManager;
import com.l2js.gameserver.instancemanager.FortSiegeManager.SiegeSpawn;
import com.l2js.gameserver.model.L2CharPosition;
import com.l2js.gameserver.model.L2Skill;
import com.l2js.gameserver.model.L2Spawn;
import com.l2js.gameserver.model.actor.L2Character;
import com.l2js.gameserver.model.actor.L2Summon;
import com.l2js.gameserver.network.NpcStringId;
import com.l2js.gameserver.network.serverpackets.NpcSay;
import com.l2js.gameserver.templates.chars.L2NpcTemplate;

public class L2FortCommanderInstance extends L2DefenderInstance
{
	
	private boolean _canTalk;
	
	public L2FortCommanderInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2FortCommanderInstance);
		_canTalk = true;
	}
	
	/**
	 * Return True if a siege is in progress and the L2Character attacker isn't a Defender.<BR><BR>
	 *
	 * @param attacker The L2Character that the L2CommanderInstance try to attack
	 *
	 */
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		if ( attacker == null || !(attacker instanceof L2PcInstance) )
			return false;
		
		boolean isFort = (getFort() != null && getFort().getFortId() > 0  && getFort().getSiege().getIsInProgress() && !getFort().getSiege().checkIsDefender(((L2PcInstance)attacker).getClan()));
		
		// Attackable during siege by all except defenders
		return (isFort);
	}
	
	@Override
	public void addDamageHate(L2Character attacker, int damage, int aggro)
	{
		if (attacker == null)
			return;
		
		if (!(attacker instanceof L2FortCommanderInstance))
		{
			super.addDamageHate(attacker, damage, aggro);
		}
	}
	
	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
			return false;
		
		if (getFort().getSiege().getIsInProgress())
		{
			getFort().getSiege().killedCommander(this);
			
		}
		
		return true;
	}
	
	/**
	 * This method forces guard to return to home location previously set
	 *
	 */
	@Override
	public void returnHome()
	{
		if (!isInsideRadius(getSpawn().getLocx(), getSpawn().getLocy(), 200, false))
		{
			if (Config.DEBUG) _log.info(getObjectId()+": moving home");
			setisReturningToSpawnPoint(true);
			clearAggroList();
			
			if (hasAI())
				getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(getSpawn().getLocx(), getSpawn().getLocy(), getSpawn().getLocz(), 0));
		}
	}
	@Override
	public final void addDamage(L2Character attacker, int damage, L2Skill skill)
	{
		L2Spawn spawn = getSpawn();
		if (spawn != null && canTalk())
		{
			FastList<SiegeSpawn> commanders = FortSiegeManager.getInstance().getCommanderSpawnList(getFort().getFortId());
			for (SiegeSpawn spawn2 : commanders)
			{
				if (spawn2.getNpcId() == spawn.getNpcid())
				{
					NpcStringId npcString = null;
					switch (spawn2.getId())
					{
						case 1:
							npcString = NpcStringId.ATTACKING_THE_ENEMYS_REINFORCEMENTS_IS_NECESSARY_TIME_TO_DIE;
							break;
						case 2:
							if (attacker instanceof L2Summon)
								attacker = ((L2Summon) attacker).getOwner();
							npcString = NpcStringId.EVERYONE_CONCENTRATE_YOUR_ATTACKS_ON_S1_SHOW_THE_ENEMY_YOUR_RESOLVE;
							break;
						case 3:
							npcString = NpcStringId.SPIRIT_OF_FIRE_UNLEASH_YOUR_POWER_BURN_THE_ENEMY;
							break;
					}
					if (npcString != null)
					{
						NpcSay ns = new NpcSay(getObjectId(), 1, getNpcId(), npcString);
						if (npcString.getParamCount() == 1)
							ns.addStringParameter(attacker.getName());
						
						broadcastPacket(ns);
						setCanTalk(false);
						ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleTalkTask(), 10000);
					}
				}
			}
		}
		super.addDamage(attacker, damage, skill);
	}
	
	private class ScheduleTalkTask implements Runnable
	{
		
		public ScheduleTalkTask()
		{
		}
		
		@Override
		public void run()
		{
			setCanTalk(true);
		}
	}
	
	void setCanTalk(boolean val)
	{
		_canTalk = val;
	}
	
	private boolean canTalk()
	{
		return _canTalk;
	}
	
	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}
}
