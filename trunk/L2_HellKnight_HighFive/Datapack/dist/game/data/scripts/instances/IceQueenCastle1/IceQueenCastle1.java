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
package instances.IceQueenCastle1;

import l2.hellknight.gameserver.ai.CtrlIntention;
import l2.hellknight.gameserver.datatables.SkillTable;
import l2.hellknight.gameserver.instancemanager.InstanceManager;
import l2.hellknight.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2.hellknight.gameserver.model.L2CharPosition;
import l2.hellknight.gameserver.model.L2World;
import l2.hellknight.gameserver.model.actor.L2Character;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2DoorInstance;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.entity.Instance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;
import l2.hellknight.gameserver.model.skills.L2Skill;
import l2.hellknight.gameserver.model.zone.L2ZoneType;
import l2.hellknight.gameserver.network.SystemMessageId;
import l2.hellknight.gameserver.network.clientpackets.Say2;
import l2.hellknight.gameserver.network.serverpackets.NpcSay;
import l2.hellknight.gameserver.network.serverpackets.SystemMessage;

import java.util.Arrays;

public class IceQueenCastle1 extends Quest
{
	private class IQWorld extends InstanceWorld
	{
		public long[] storeTime = {0,0};
		public boolean showIsInProgress = false; 
		public IQWorld()
		{
			
		}
	}
	
	private static final String qn = "IceQueenCastle1";
	private static final int INSTANCEID = 137;
	
	//NPC's, mobs
	private static final int JINIA2 = 32781;
	private static final int FREYA = 18847;
	private static final int[] CROWD = { 18848, 18849, 18926, 22767};
	
	//Freya Skills
	//private static final L2Skill _attackSkill = SkillTable.getInstance().getInfo(6278, 1);
	private static final L2Skill _showBlizzard = SkillTable.getInstance().getInfo(6276, 1);
	
	private static final int[] ENTRY_POINT = { 114000, -112357, -11200 };
	private int[] RETURN_POINT = { 113883, -108777, -848 };
	
	private class teleCoord {int instanceId; int x; int y; int z;}
	
	private void teleportplayer(L2PcInstance player, teleCoord teleto)
	{
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.setInstanceId(teleto.instanceId);
		player.teleToLocation(teleto.x, teleto.y, teleto.z);
		return;
	}
	
	private boolean checkConditions(L2PcInstance player)
	{
		if (player.getLevel() < 82 || player.getLevel() > 85)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
			sm.addPcName(player);
			player.sendPacket(sm);
			return false;
		}
		
		return true; 
	}

	protected void exitInstance(L2PcInstance player, teleCoord tele)
	{
		player.setInstanceId(0);
		player.teleToLocation(tele.x, tele.y, tele.z);
	}
	 
	protected int enterInstance(L2PcInstance player, String template, teleCoord teleto)
	{
		int instanceId = 0;
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		//existing instance
		if (world != null)
		{
			//this instance
			if (!(world instanceof IQWorld))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return 0;
			}
			teleto.instanceId = world.instanceId;
			teleportplayer(player,teleto);
			return instanceId;
		}

		if (!checkConditions(player))
			return 0;

		instanceId = InstanceManager.getInstance().createDynamicInstance(template);
		world = new IQWorld();
		world.instanceId = instanceId;
		world.templateId = INSTANCEID;
		world.status = 0;
		((IQWorld)world).storeTime[0] = System.currentTimeMillis();
		InstanceManager.getInstance().addWorld(world);
		_log.info("IQWorld started " + template + " Instance: " + instanceId + " created by player: " + player.getName());
		teleto.instanceId = instanceId;
		teleportplayer(player,teleto);
		world.allowed.add(player.getObjectId());
			//Open door
		L2DoorInstance door = InstanceManager.getInstance().getInstance(instanceId).getDoor(23140101);
		if (door != null)
			door.openMe();			

		return instanceId;
	}

  @Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld != null && tmpworld instanceof IQWorld && tmpworld.templateId == 137)
		{
			IQWorld world = (IQWorld) tmpworld;
			L2PcInstance pl = L2World.getInstance().getPlayer(world.allowed.get(0));

			if (npc.getNpcId() == FREYA)
			{
				if (event.equalsIgnoreCase("moving"))
				{
					npc.setIsNoRndWalk(false);
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(114730, -114805, -11200, 0));
					return null;
				}
		
				else if (event.equalsIgnoreCase("blizzard"))
				{
					//NpcSay cs = new NpcSay(npc.getObjectId(), Say2.ALL, npc.getNpcId(), 1801125);
					NpcSay cs = new NpcSay(npc.getObjectId(), Say2.ALL, npc.getNpcId(), "I can no longer stand by.");
					npc.broadcastPacket(cs);
					npc.stopMove(null);
					npc.setIsImmobilized(true);
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
					npc.setIsInvul(true);
					npc.setTarget(npc);
					if (_showBlizzard != null)
						npc.doCast(_showBlizzard);
					return null;
				}

				else if (event.equalsIgnoreCase("movie") && pl != null)
				{
					pl.showQuestMovie(21);
					npc.deleteMe();
					startQuestTimer("movie_end", 24000, npc, null);
				}

				else if (event.equalsIgnoreCase("movie_end") && pl != null)
				{
					QuestState hostQuest = pl.getQuestState("10285_MeetingSirra");

					if (hostQuest != null && hostQuest.getState() == State.STARTED && hostQuest.getInt("progress") == 2)
					{
						hostQuest.set("cond", "10");
						hostQuest.playSound("ItemSound.quest_middle");
						hostQuest.set("progress", "3");
					}
				
					//Leave instance
					world.allowed.remove(world.allowed.indexOf(pl.getObjectId()));
					teleCoord tele = new teleCoord();
					tele.instanceId = 0;
					tele.x = RETURN_POINT[0];    
					tele.y = RETURN_POINT[1];
					tele.z = RETURN_POINT[2];
					exitInstance(pl,tele);

					// destroy instance after 1 min
					Instance inst = InstanceManager.getInstance().getInstance(npc.getInstanceId());
					inst.setDuration(60000);
					inst.setEmptyDestroyTime(0);
				}
			}
			
			else if (!world.showIsInProgress && event.equalsIgnoreCase("immobilize") && Arrays.binarySearch(CROWD, npc.getNpcId()) >= 0)
				npc.setIsImmobilized(true);
		}

		return null;
	}
			
	@Override
  	public String onTalk ( L2Npc npc, L2PcInstance player)
	{
		int npcId = npc.getNpcId();
		QuestState st = player.getQuestState(qn);
		if (st == null)
			st = newQuestState(player);

		if (npcId == JINIA2)
		{
			teleCoord tele = new teleCoord();
			tele.x = ENTRY_POINT[0];      
			tele.y = ENTRY_POINT[1];
			tele.z = ENTRY_POINT[2];

			QuestState hostQuest = player.getQuestState("10285_MeetingSirra");

			if (hostQuest != null && hostQuest.getState() == State.STARTED && hostQuest.getInt("progress") == 2)
			{
				hostQuest.set("cond", "9");
				hostQuest.playSound("ItemSound.quest_middle");
			}

			if (enterInstance(player, "IceQueenCastle1.xml", tele) <= 0)
				return "32781-10.htm";
		}
		
		return "";
	}

	@Override
	public String onEnterZone(L2Character character, L2ZoneType zone)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(character.getInstanceId());
		if (tmpworld != null && tmpworld instanceof IQWorld && tmpworld.templateId == 137)
		{
			IQWorld world = (IQWorld) tmpworld;
			if (!world.showIsInProgress && character instanceof L2PcInstance) //triggers show begin
			{
				addSpawn(FREYA, 114720,-117085,-11088,15956, false, 0, false, character.getInstanceId());
				Instance inst = InstanceManager.getInstance().getInstance(character.getInstanceId());
				for (L2Npc npc : inst.getNpcs())
				{
					if (Arrays.binarySearch(CROWD, npc.getNpcId()) >= 0)
					{
						npc.setIsImmobilized(false); //Fight!!!
					}
				}
				world.showIsInProgress = true;
			}
		}
		
		return null;
	}

	@Override
	public final String onSpawn(L2Npc npc)
	{
		if (npc.getNpcId() == FREYA)
		{
			startQuestTimer("moving", 60000, npc, null);
			startQuestTimer("blizzard", 180000, npc, null);
			npc.setIsNoRndWalk(true);
		}
		
		//Immobilize mobs until trigger will not switched on
		//else if (!world.showIsInProgress && Arrays.binarySearch(CROWD, npc.getNpcId()) >= 0)
		else if (Arrays.binarySearch(CROWD, npc.getNpcId()) >= 0)
			startQuestTimer("immobilize", 1000, npc, null);

		
		return null;
	}

	@Override
	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		if (skill == _showBlizzard)
			startQuestTimer("movie", 1000, npc, null);

		return null;
	}

	public IceQueenCastle1(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(JINIA2);
		addTalkId(JINIA2);
		addEnterZoneId(200010);
		addSpawnId(FREYA);
		for (int i = 0; i < CROWD.length; i++)
			addSpawnId(CROWD[i]);
		addSpellFinishedId(FREYA);
	}
	
	public static void main(String[] args)
	{
		new IceQueenCastle1(-1,qn,"instances");
	}
}