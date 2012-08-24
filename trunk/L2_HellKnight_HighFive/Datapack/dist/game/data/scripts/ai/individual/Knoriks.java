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
package ai.individual;

import ai.group_template.L2AttackableAIScript;

import l2.hellknight.gameserver.ai.CtrlIntention;
import l2.hellknight.gameserver.model.L2CharPosition;
import l2.hellknight.gameserver.model.actor.L2Attackable;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;

public class Knoriks extends L2AttackableAIScript
{
	private static final int KNORIKS = 22857;

	private L2Npc princess1;
	  private static final L2CharPosition princess1_1 = new L2CharPosition(145180, 113285, -3725, 0);
	  private static final L2CharPosition princess1_2 = new L2CharPosition(145890, 112583, -3725, 0);
	  private static final L2CharPosition princess1_3 = new L2CharPosition(147249, 112436, -3725, 0);
	  private static final L2CharPosition princess1_4 = new L2CharPosition(148885, 113022, -3725, 0);
	  private static final L2CharPosition princess1_5 = new L2CharPosition(149144, 114033, -3725, 0);
	  private static final L2CharPosition princess1_6 = new L2CharPosition(149074, 115274, -3725, 0);
	  private static final L2CharPosition princess1_7 = new L2CharPosition(148226, 115923, -3725, 0);
	  private static final L2CharPosition princess1_8 = new L2CharPosition(146786, 116347, -3725, 0);
	  private static final L2CharPosition princess1_9 = new L2CharPosition(145571, 115830, -3725, 0);
	  private static final L2CharPosition princess1_10 = new L2CharPosition(145080, 114976, -3725, 0);
	  private static final L2CharPosition princess1_11 = new L2CharPosition(144484, 114552, -3725, 0);
	  private static final L2CharPosition princess1_12 = new L2CharPosition(145128, 113925, -3725, 0);

	
	public Knoriks (int id, String name, String descr)
	{
		super(id,name,descr);
	    addKillId(KNORIKS);
	    addAggroRangeEnterId(KNORIKS);

	    startQuestTimer("princess1_spawn", 7000L, null, null);
		

	}
	

	@Override
	public String onAdvEvent (String event, L2Npc npc, L2PcInstance player)
	{
			  if ((event.equalsIgnoreCase("princess1_spawn")) && (this.princess1 == null))
			  {
				  this.princess1 = addSpawn(KNORIKS, 145180, 113285, -3725, 0, false, 0L);
				  this.princess1.setIsNoRndWalk(true);
				  this.princess1.setRunning();
				  startQuestTimer("trasa_1", 7000L, this.princess1, null);
			  }
			  else if ((event.equalsIgnoreCase("trasa_nova")) && (this.princess1 != null))
			  {
				  if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
				  {
					  startQuestTimer("trasa_nova", 7000L, this.princess1, null);
				  }
				  else	
				  {
					  this.princess1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess1_1);
					  this.princess1.setRunning();
					  startQuestTimer("trasa_1", 7000L, this.princess1, null);
				  }	
			  }
			  else if ((event.equalsIgnoreCase("trasa_1")) && (this.princess1 != null))
			  {
				  if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
				  {
					  startQuestTimer("trasa_1", 7000L, this.princess1, null);
				  }
				  else
				  {
					  if (!this.princess1.isInsideRadius(princess1_1.x, princess1_1.y, princess1_1.z, 100, true, false))
						  this.princess1.teleToLocation(princess1_1.x, princess1_1.y, princess1_1.z);
					  this.princess1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess1_2);
					  this.princess1.setRunning();
					  startQuestTimer("trasa_2", 7000L, this.princess1, null);
		      		}
			  }
			  else if ((event.equalsIgnoreCase("trasa_2")) && (this.princess1 != null))
			  {
				  if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
				  {
					  startQuestTimer("trasa_2", 7000L, this.princess1, null);
				  }
				  else
				  {
					  this.princess1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess1_3);
					  this.princess1.setRunning();
					  startQuestTimer("trasa_3", 7000L, this.princess1, null);
				  }
		    }
		    else if ((event.equalsIgnoreCase("trasa_3")) && (this.princess1 != null))
		    {
		    	if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
		    	{
		    		startQuestTimer("trasa_3", 7000L, this.princess1, null);
		    	}
		    	else
		    	{
		    		this.princess1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess1_4);
		    		this.princess1.setRunning();
		    		startQuestTimer("trasa_4", 7000L, this.princess1, null);
		    	}
		    }
		    else if ((event.equalsIgnoreCase("trasa_4")) && (this.princess1 != null))
		    {
		    	if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
		    	{
		    		startQuestTimer("trasa_4", 7000L, this.princess1, null);
		    	}
		    	else
		    	{
		    		this.princess1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess1_5);
		    		this.princess1.setRunning();
		    		startQuestTimer("trasa_5", 7000L, this.princess1, null);
		    	}
		    }
		    else if ((event.equalsIgnoreCase("trasa_5")) && (this.princess1 != null))
		    {
		    	if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
		    	{
		    		startQuestTimer("trasa_5", 7000L, this.princess1, null);
		    	}
		    	else
		    	{
		    		this.princess1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess1_6);
		    		this.princess1.setRunning();
		    		startQuestTimer("trasa_6", 7000L, this.princess1, null);
		    	}
		    }
		    else if ((event.equalsIgnoreCase("trasa_6")) && (this.princess1 != null))
		    {
		    	if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
		    	{
		    		startQuestTimer("trasa_6", 7000L, this.princess1, null);
		    	}
		    	else
		    	{
		    		this.princess1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess1_7);
		    		this.princess1.setRunning();
		    		startQuestTimer("trasa_7", 7000L, this.princess1, null);
		    	}
		    }
		    else if ((event.equalsIgnoreCase("trasa_7")) && (this.princess1 != null))
		    {
		    	if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
		    	{
		    		startQuestTimer("trasa_7", 7000L, this.princess1, null);
		    	}
		    	else
		    	{
		    		this.princess1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess1_8);
		    		this.princess1.setRunning();
		    		startQuestTimer("trasa_8", 7000L, this.princess1, null);
		    	}
		    }
		    else if ((event.equalsIgnoreCase("trasa_8")) && (this.princess1 != null))
		    {
		    	if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
		    	{
		    		startQuestTimer("trasa_8", 7000L, this.princess1, null);
		    	}
		    	else
		    	{
		    		this.princess1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess1_9);
		    		this.princess1.setRunning();
		    		startQuestTimer("trasa_9", 7000L, this.princess1, null);
		    	}
		    }
		    else if ((event.equalsIgnoreCase("trasa_9")) && (this.princess1 != null))
		    {
		    	if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
		    	{
		    		startQuestTimer("trasa_9", 7000L, this.princess1, null);
		    	}
		    	else
		    	{
		    		this.princess1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess1_10);
		    		this.princess1.setRunning();
		    		startQuestTimer("trasa_10", 7000L, this.princess1, null);
		    	}
		    }
		    else if ((event.equalsIgnoreCase("trasa_10")) && (this.princess1 != null))
		    {
		    	if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
		    	{
		    		startQuestTimer("trasa_10", 7000L, this.princess1, null);
		    	}
		    	else
		    	{
		    		this.princess1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess1_11);
		    		this.princess1.setRunning();
		    		startQuestTimer("trasa_11", 7000L, this.princess1, null);
		    	}
		    }
		    else if ((event.equalsIgnoreCase("trasa_11")) && (this.princess1 != null))
		    {
		    	if ((npc.isCastingNow()) || (npc.isAttackingNow()) || (npc.isInCombat()))
		    	{
		    		startQuestTimer("trasa_11", 7000L, this.princess1, null);
		    	}
		    	else
		    	{
		    		this.princess1.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, princess1_12);
		    		this.princess1.setRunning();
		    		startQuestTimer("trasa_nova", 7000L, this.princess1, null);
		     	}
		    }
		return super.onAdvEvent(event, npc, player);
	}
	

	@Override
	public String onKill (L2Npc npc, L2PcInstance killer, boolean isPet)
	{
	    if (npc == null) 
	    {
	    	return super.onKill(npc, killer, isPet);
	    }
	    if (npc.getNpcId() == KNORIKS)
	    {
	      if (npc == this.princess1)
	      {
	        startQuestTimer("princess1_spawn", 300000L, null, null);
	        this.princess1 = null;
	      }
	    }
		return super.onKill(npc,killer,isPet);
	}
	
  	@Override
  	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
  	{
  		if ((!npc.isCastingNow()) && (!npc.isAttackingNow()) && (!npc.isInCombat()) && (!player.isDead()))
  		{
  			((L2Attackable)npc).addDamageHate(player, 0, 999);
  			((L2Attackable)npc).getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
  		}
  		return super.onAggroRangeEnter(npc, player, isPet);
  	}

	public static void main(String[] args)
	{
		new Knoriks(-1,"Knoriks","ai");
	}
}