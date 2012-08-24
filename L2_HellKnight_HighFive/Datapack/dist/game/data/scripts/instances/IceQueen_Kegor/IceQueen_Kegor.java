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
package instances.IceQueen_Kegor;

import l2.hellknight.gameserver.ai.CtrlIntention;
import l2.hellknight.gameserver.datatables.SkillTable;
import l2.hellknight.gameserver.instancemanager.InstanceManager;
import l2.hellknight.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2.hellknight.gameserver.model.L2CharPosition;
import l2.hellknight.gameserver.model.skills.L2Skill;
import l2.hellknight.gameserver.model.actor.L2Attackable;
import l2.hellknight.gameserver.model.actor.L2Character;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2MonsterInstance;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.entity.Instance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.network.NpcStringId;
import l2.hellknight.gameserver.network.SystemMessageId;
import l2.hellknight.gameserver.network.clientpackets.Say2;
import l2.hellknight.gameserver.network.serverpackets.NpcSay;
import l2.hellknight.gameserver.network.serverpackets.SystemMessage;
import l2.hellknight.gameserver.util.Util;

/**
 * @author GKR
 */

public class IceQueen_Kegor extends Quest
{
	private static final String qn = "IceQueen_Kegor";
	private static final int INSTANCEID = 138;
	
	private static final int KROON = 32653;
	private static final int TAROON = 32654;
	private static final int KEGOR_IN_CAVE = 18846;
	private static final int MONSTER = 22766;
	
	private static final int ANTIDOTE = 15514;
	
	private static final int BUFF = 6286;

	private static final int[][] MOB_SPAWNS = 
	{
		{ 185216, -184112, -3308, -15396 },
		{ 185456, -184240, -3308, -19668 },
		{ 185712, -184384, -3308, -26696 },
		{ 185920, -184544, -3308, -32544 },
		{ 185664, -184720, -3308, 27892 },
	};
	
	private static final int[] ENTRY_POINT = { 186852, -173492, -3879 };
	
	private class teleCoord {int instanceId; int x; int y; int z;}
	
  public IceQueen_Kegor(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addFirstTalkId(KEGOR_IN_CAVE);
		addStartNpc(KROON, TAROON);
		addTalkId(KEGOR_IN_CAVE, KROON, TAROON);
		addKillId(KEGOR_IN_CAVE);
		addSpawnId(KEGOR_IN_CAVE);
	}

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
			if (world.templateId != INSTANCEID)
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return 0;
			}

			teleto.instanceId = world.instanceId;
			teleportplayer(player, teleto);
			return instanceId;
		}
		//New instance
		if (!checkConditions(player))
			return 0;

		instanceId = InstanceManager.getInstance().createDynamicInstance(template);
		final Instance inst = InstanceManager.getInstance().getInstance(instanceId);
		inst.setSpawnLoc(new int[] { player.getX(), player.getY(), player.getZ() });
		world = new InstanceWorld();
		world.instanceId = instanceId;
		world.templateId = INSTANCEID;
		world.status = 0;
		
		InstanceManager.getInstance().addWorld(world);
		_log.info("Mithril Mines (Kegor) instance started " + template + " Instance: " + instanceId + " created by player: " + player.getName());
		teleto.instanceId = instanceId;
		teleportplayer(player,teleto);
		world.allowed.add(player.getObjectId());
		return instanceId;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (npc != null && !npc.isDead()) // NPC check
		{
			InstanceWorld world = InstanceManager.getInstance().getWorld(npc.getInstanceId());
			if (world != null && world.templateId == INSTANCEID) // Instance check
			{
				if (npc.getNpcId() == KEGOR_IN_CAVE)
				{
					// Spawn mobs, set them busy and store spawned count
					if (event.equalsIgnoreCase("spawn"))
					{
						for(int[] spawn : MOB_SPAWNS)
						{
							L2Npc mob = addSpawn(MONSTER, spawn[0], spawn[1], spawn[2], spawn[3], false, 0, false, world.instanceId);
							mob.setBusy(true); // Mark this group to recognize 
						}
					}
				
					else if (event.equalsIgnoreCase("buff") && world.status > 0) 
					{
						// Rebuff player
						L2PcInstance pl = npc.getSummoner().getActingPlayer();
						if (pl != null && !pl.isDead() && pl.getInstanceId() == npc.getInstanceId()) 
						{
							L2Skill buff = SkillTable.getInstance().getInfo(BUFF,1);
							if (buff != null)
							{
								if (Util.checkIfInRange(buff.getCastRange(), npc, pl, false))
								{
									npc.setTarget(pl);
									npc.doCast(buff);
								}
							}
						}
						
						// Update bug's aggro on Kegor
						Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
						for (L2Npc mob : inst.getNpcs())
						{
							if (mob != null && mob.getNpcId() == MONSTER && mob.isBusy() && !mob.isDead())
							{
								((L2Attackable)mob).addDamageHate(npc, 0, 1000);
								if (mob.getKnownList().knowsObject(npc) && mob.getAI().getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
										mob.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, npc);
								else
									mob.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(npc.getX(), npc.getY(), npc.getZ(), 0));
							}
						}
						startQuestTimer("buff", 30000, npc, null);
					}

					// Update aggro on monsters
					else if (event.equalsIgnoreCase("attack_mobs") && !checkFinish(world))
					{
						for (L2Character ch : npc.getKnownList().getKnownCharactersInRadius(1500))
						{
							if (ch != null && !ch.isDead() && ch instanceof L2MonsterInstance && ((L2MonsterInstance)ch).getNpcId() == MONSTER && 
								((L2MonsterInstance)ch).isBusy()) 
							{
								((L2Attackable)npc).addDamageHate(ch, 0, 1000);
								if (npc.getAI().getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
									npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, ch);
								
								if (!ch.getKnownList().knowsObject(npc))
									ch.getKnownList().addKnownObject(npc);
							}
						}
						startQuestTimer("attack_mobs", 10000, npc, null);
					}
				}
			}
		}
			
		return null;
	}

  @Override
public String onTalk ( L2Npc npc, L2PcInstance player)
	{
		int npcId = npc.getNpcId();
		String htmltext = getNoQuestMsg(player);
		
		QuestState hostQuest = player.getQuestState("Q10284_AcquisitionOfDivineSword");
		
		if (hostQuest == null)
		{
			System.out.println("null host quest");
			return htmltext;
		}

		if (npcId == KROON || npcId == TAROON)
		{
			teleCoord tele = new teleCoord();
			tele.x = ENTRY_POINT[0];      
			tele.y = ENTRY_POINT[1];
			tele.z = ENTRY_POINT[2];

			htmltext = npcId == KROON ? "32653-07.htm" : "32654-07.htm";
			if (enterInstance(player, "IceQueen_Kegor.xml", tele) > 0)
			{
				htmltext = "";
				if (hostQuest.getInt("progress") == 2 && hostQuest.getQuestItemsCount(ANTIDOTE) == 0)
				{
					hostQuest.giveItems(ANTIDOTE, 1);
					hostQuest.playSound("ItemSound.quest_middle");
					hostQuest.set("cond", "4");
				}
			}
		}

		else if (npc.getNpcId() == KEGOR_IN_CAVE)
		{
			InstanceWorld world = InstanceManager.getInstance().getWorld(player.getInstanceId());
			if (world != null && world.templateId == INSTANCEID)
			{
				if (hostQuest.getInt("progress") == 2 && hostQuest.hasQuestItems(ANTIDOTE) && world.status == 0)
				{
					hostQuest.takeItems(ANTIDOTE, -1);
					hostQuest.playSound("ItemSound.quest_middle");
					hostQuest.set("cond", "5");
					htmltext = "18846-01.htm";
					npc.setSummoner(player); // Store player for future needs
					world.status = 1; // set fight flag
					startQuestTimer("spawn", 3000, npc, null);
					startQuestTimer("buff", 3500, npc, null);
					startQuestTimer("attack_mobs", 10000, npc, null);
				}

				else if (hostQuest.isCompleted())
				{
					world.allowed.remove(world.allowed.indexOf(player.getObjectId()));
					Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
					teleCoord tele = new teleCoord();
					tele.instanceId = 0;
					tele.x = inst.getSpawnLoc()[0];    
					tele.y = inst.getSpawnLoc()[1];
					tele.z = inst.getSpawnLoc()[2];
					exitInstance(player,tele);
					htmltext = "";
				}
			}
		}
		
		return htmltext;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		InstanceWorld world = InstanceManager.getInstance().getWorld(player.getInstanceId());
		if (world != null && world.templateId == INSTANCEID)
		{
			QuestState hostQuest = player.getQuestState("10284_AcquisitionOfDivineSword");
			
			if (hostQuest != null)
			{
				if (!hostQuest.isStarted())
					return "18846-04.htm";
			
				if (hostQuest.getInt("progress") == 2)
				{
					if (world.status == 0)
						return "18846-00.htm";
					else if (world.status > 0)
						return "18846-02.htm";
				}
			
				else if (hostQuest.getInt("progress") == 3)
				{
					hostQuest.giveItems(57, 296425);
					hostQuest.addExpAndSp(921805, 82230);
					hostQuest.playSound("ItemSound.quest_finish");
					hostQuest.exitQuest(false);
					return "18846-03.htm";
				}
			}
		}
		
		return null;
	}

	@Override
	public String onAttack(final L2Npc npc, final L2PcInstance attacker, final int damage, final boolean isPet, final L2Skill skill)
	{
		InstanceWorld world = InstanceManager.getInstance().getWorld(attacker.getInstanceId());
		if (world != null && world.templateId == INSTANCEID)
		{
			L2Attackable kegor = (L2Attackable) InstanceManager.getInstance().getInstance(world.instanceId).getNpc(KEGOR_IN_CAVE);
			
			if (kegor != null && !kegor.isDead())
			{
				kegor.addDamageHate(npc, 0, 1000);
				
				if (kegor.getAI().getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
					kegor.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, npc);
			}
		}

		return super.onAttack(npc, attacker, damage, isPet, skill);
	}

	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		InstanceWorld world = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (world != null && world.templateId == INSTANCEID)
		{
			world.status = -1;
			NpcSay cs = new NpcSay(npc.getObjectId(), Say2.ALL, npc.getNpcId(), NpcStringId.HOW_COULD_I_FALL_IN_A_PLACE_LIKE_THIS);
			npc.broadcastPacket(cs);
			cancelQuestTimer("buff", npc, null);
			cancelQuestTimer("attack_mobs", npc, null);

			// destroy instance after 1 min
			Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
			inst.setDuration(60000);
			inst.setEmptyDestroyTime(0);
		}

		return super.onKill(npc, player, isPet);
	}

	@Override
	public final String onSpawn(L2Npc npc)
	{
		npc.setRHandId(15280);
		
		return super.onSpawn(npc);
	}

	// Since mob can be killed by Kegor too - it is not possible to track live mob's count in onKill.
	// So it is checked by timer
	private boolean checkFinish(InstanceWorld world)
	{
		try
		{
			if (world.status < 0) // Kegor is dead
				return false;

			int count = 0;
			final Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
			for (L2Npc npc : inst.getNpcs())
			{
				if (npc != null && npc.getNpcId() == MONSTER && npc.isBusy() && !npc.isDead())
					count++;
			}
			
			if (count == 0)
			{
				world.status = 0;
				// Get Kegor
				L2Attackable kegor = (L2Attackable) inst.getNpc(KEGOR_IN_CAVE);
				if (kegor != null && !kegor.isDead())
				{
					cancelQuestTimer("buff", kegor, null);
					cancelQuestTimer("attack_mobs", kegor, null);
					// Get player
					L2PcInstance pl = kegor.getSummoner().getActingPlayer();
					if (pl != null && !pl.isDead() && pl.getInstanceId() == world.instanceId)
					{
						kegor.stopMove(null);
						kegor.clearAggroList();
						kegor.setRHandId(0);
						kegor.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, pl, null);
						NpcSay cs = new NpcSay(kegor.getObjectId(), Say2.ALL, kegor.getNpcId(), NpcStringId.I_CAN_FINALLY_TAKE_A_BREATHER_BY_THE_WAY_WHO_ARE_YOU_HMM_I_THINK_I_KNOW_WHO_SENT_YOU);
						kegor.broadcastPacket(cs);
						QuestState hostQuest = pl.getQuestState("10284_AcquisitionOfDivineSword");
						if (hostQuest != null && hostQuest.getInt("progress") == 2)
						{
							hostQuest.set("progress", "3");
							hostQuest.set("cond", "6");
							hostQuest.playSound("ItemSound.quest_middle");
						}
					}
				}
				// destroy instance after 3 min
				inst.setDuration(3 * 60000);
				inst.setEmptyDestroyTime(0);
				
				return true;
			}
		}
		catch(Exception e)
		{
			return true; // Stop timer if there is any exception
		}
		
		return false;
	}

	public static void main(String[] args)
	{
		new IceQueen_Kegor(-1,qn,"instances");
	}
}
