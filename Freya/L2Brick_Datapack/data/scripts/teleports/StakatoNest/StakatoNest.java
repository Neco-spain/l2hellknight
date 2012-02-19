package teleports.StakatoNest;

import l2.brick.Config;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.model.quest.QuestState;
import l2.brick.gameserver.model.quest.State;

public class StakatoNest extends Quest
{
	private final static int[][] data =
	{
		{80456, -52322, -5640},
		{88718, -46214, -4640},
		{87464, -54221, -5120},
		{80848, -49426, -5128},
		{87682, -43291, -4128}
	};
	
	private final static int npcId = 32640;
	
	public StakatoNest(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(npcId);
		addTalkId(npcId);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(getName());
		if (st == null)
			st = newQuestState(player);
		
		int loc = Integer.parseInt(event) - 1;
		
		if (data.length > loc)
		{
			int x = data[loc][0];
			int y = data[loc][1];
			int z = data[loc][2];
			
			if (player.getParty() != null)
			{
				for (L2PcInstance partyMember : player.getParty().getPartyMembers())
				{
					if (partyMember.isInsideRadius(player, 1000, true, true))
						partyMember.teleToLocation(x, y, z);
				}
			}
			player.teleToLocation(x, y, z);
			st.exitQuest(true);
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		QuestState accessQuest = player.getQuestState("240_ImTheOnlyOneYouCanTrust");
		if (accessQuest != null && accessQuest.getState() == State.COMPLETED)
			htmltext = "32640.htm";
		else
			htmltext = "32640-no.htm";
		
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new StakatoNest(-1, "StakatoNest", "teleports");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Teleport: Stakato Nest");
	}
}