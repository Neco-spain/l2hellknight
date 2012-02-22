package zone_scripts.GraciaContinent.SecretAreaKeucereus;

import l2.hellknight.Config;
import l2.hellknight.gameserver.ai.CtrlIntention;
import l2.hellknight.gameserver.instancemanager.InstanceManager;
import l2.hellknight.gameserver.instancemanager.QuestManager;
import l2.hellknight.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.State;
import l2.hellknight.gameserver.network.SystemMessageId;
import l2.hellknight.gameserver.network.serverpackets.SystemMessage;

public class SecretAreaKeucereus extends Quest
{
	private static final String qn = "SecretAreaKeucereus";
	
	private class KSAWorld extends InstanceWorld
	{
		public KSAWorld()
		{
			//InstanceManager.getInstance().super();
		}
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("enter"))
		{
			enterInstance(player, "SecretAreaKeucereus.xml");
		}
		else if (event.equalsIgnoreCase("enter_118"))
		{
			enterInstance118(player, "SecretAreaKeucereus.xml");
		}
		else if (event.equalsIgnoreCase("exit"))
		{
			player.teleToLocation(-184997, 242818, 1578);
			player.setInstanceId(0);
		}
		return "";
	}
	
	public SecretAreaKeucereus(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addTalkId(32566);
		addTalkId(32567);
	}
	
	private void enterInstance(L2PcInstance player, String template)
	{
		int instanceId = 0;
		//check for existing instances for this player
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		//existing instance
		if (world != null)
		{
			if (!(world instanceof KSAWorld))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return;
			}
			teleportPlayer(player,(KSAWorld)world);
			return;
		}
		//New instance
		else
		{
			if (!checkCond(player))
				return;
			instanceId = InstanceManager.getInstance().createDynamicInstance(template);
			world = new KSAWorld();
			
			world.instanceId = instanceId;
			world.templateId = 117;
			world.status = 0;
			
			InstanceManager.getInstance().addWorld(world);
			_log.info("SecretAreaKeucereus started " + template + " Instance: " + instanceId + " created by player: " + player.getName());
			teleportPlayer(player, (KSAWorld)world);
		}
	}
	
	private void enterInstance118(L2PcInstance player, String template)
	{
		int instanceId = 0;
		//check for existing instances for this player
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		//existing instance
		if (world != null)
		{
			if (!(world instanceof KSAWorld))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return;
			}
			teleportPlayer(player,(KSAWorld)world);
			return;
		}
		//New instance
		else
		{
			if (!checkCond118(player))
				return;
			instanceId = InstanceManager.getInstance().createDynamicInstance(template);
			world = new KSAWorld();
			
			world.instanceId = instanceId;
			world.templateId = 118;
			world.status = 0;
			
			InstanceManager.getInstance().addWorld(world);
			_log.info("SecretAreaKeucereus started " + template + " Instance: " + instanceId + " created by player: " + player.getName());
			teleportPlayer(player, (KSAWorld)world);
		}
	}
	
	private void teleportPlayer(L2PcInstance player, KSAWorld world)
	{
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.teleToLocation(-23530, -8963, -5413);
		player.setInstanceId(world.instanceId);
		if(player.getPet() != null)
		{
			player.getPet().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			player.getPet().setInstanceId(world.instanceId);
			player.getPet().teleToLocation(-23530, -8963, -5413);
		}
	}
	
	private boolean checkCond(L2PcInstance player)
	{
		if (QuestManager.getInstance().getQuest(10270) != null)
		{
			if (player.getQuestState(QuestManager.getInstance().getQuest(10270).getName()).getState() == State.STARTED
					&& player.getQuestState(QuestManager.getInstance().getQuest(10270).getName()).getInt("cond") == 4)
				return true;
		}
		return false;
	}
	
	private boolean checkCond118(L2PcInstance player)
	{
		if (QuestManager.getInstance().getQuest(10272) != null)
		{
			if (player.getQuestState(QuestManager.getInstance().getQuest(10272).getName()).getState() == State.STARTED
					&& player.getQuestState(QuestManager.getInstance().getQuest(10272).getName()).getInt("cond") == 3)
				return true;
		}
		return false;
	}

	public static void main(String[] args)
	{
		new SecretAreaKeucereus(-1, qn, "");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Gracia Area: Secret Area Keucereus");
	}
}