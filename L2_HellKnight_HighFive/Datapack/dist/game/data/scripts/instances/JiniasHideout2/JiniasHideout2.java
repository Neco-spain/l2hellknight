package instances.JiniasHideout2;

import l2.hellknight.gameserver.ai.CtrlIntention;
import l2.hellknight.gameserver.instancemanager.InstanceManager;
import l2.hellknight.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.entity.Instance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;
import l2.hellknight.gameserver.network.SystemMessageId;
import l2.hellknight.gameserver.network.serverpackets.SystemMessage;

public class JiniasHideout2 extends Quest
{
	private class JiniasWorld extends InstanceWorld
	{
		public long[] storeTime = {0,0}; 
		public JiniasWorld()
		{	
		}
	}
	
	private static final String qn = "JiniasHideout2";
	private static final int INSTANCEID = 141;
	
	private static final int RAFFORTY = 32020;
	private static final int JINIA = 32760;
	
	private static final int[] ENTRY_POINT = { -23530, -8963, -5413 };
	
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
			if (!(world instanceof JiniasWorld))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return 0;
			}
			teleto.instanceId = world.instanceId;
			teleportplayer(player,teleto);
			return instanceId;
		}
		//New instance
		if (!checkConditions(player))
			return 0;

		instanceId = InstanceManager.getInstance().createDynamicInstance(template);
		final Instance inst = InstanceManager.getInstance().getInstance(instanceId);
		inst.setSpawnLoc(new int[] { player.getX(), player.getY(), player.getZ() });
		world = new JiniasWorld();
		world.instanceId = instanceId;
		world.templateId = INSTANCEID;
		world.status = 0;
		((JiniasWorld)world).storeTime[0] = System.currentTimeMillis();
		InstanceManager.getInstance().addWorld(world);
		_log.info("JiniasWorld started " + template + " Instance: " + instanceId + " created by player: " + player.getName());
		teleto.instanceId = instanceId;
		teleportplayer(player,teleto);
		world.allowed.add(player.getObjectId());
		return instanceId;
	}

  	@Override
	public String onTalk ( L2Npc npc, L2PcInstance player)
	{
		int npcId = npc.getNpcId();
		QuestState st = player.getQuestState(qn);
		if (st == null)
			st = newQuestState(player);
		if (npcId == RAFFORTY)
		{
			teleCoord tele = new teleCoord();
			tele.x = ENTRY_POINT[0];      
			tele.y = ENTRY_POINT[1];
			tele.z = ENTRY_POINT[2];

			QuestState hostQuest = player.getQuestState("Q10285_MeetingSirra");

			if (hostQuest != null && hostQuest.getState() == State.STARTED && hostQuest.getInt("progress") == 1)
			{
				hostQuest.set("cond", "2");
				hostQuest.playSound("ItemSound.quest_middle");
			}

			enterInstance(player, "JiniasHideout2.xml", tele);
		}
		
		else if (npcId == JINIA)
		{
			InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
			world.allowed.remove(world.allowed.indexOf(player.getObjectId()));
			final Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
			teleCoord tele = new teleCoord();
			tele.instanceId = 0;
			tele.x = inst.getSpawnLoc()[0];    
			tele.y = inst.getSpawnLoc()[1];
			tele.z = inst.getSpawnLoc()[2];
			exitInstance(player,tele);

			QuestState hostQuest = player.getQuestState("Q10285_MeetingSirra");

			if (hostQuest != null && hostQuest.getState() == State.STARTED && hostQuest.getInt("progress") == 2)
				return "32760-15.htm";
		}	
		return "";
	}

  	public JiniasHideout2(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(RAFFORTY);
		addTalkId(RAFFORTY);
		addTalkId(JINIA);
	}
	
	public static void main(String[] args)
	{
		new JiniasHideout2(-1, qn, "instances");
	}
}