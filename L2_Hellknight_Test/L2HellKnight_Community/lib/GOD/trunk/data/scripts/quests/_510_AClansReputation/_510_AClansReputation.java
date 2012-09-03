package quests._510_AClansReputation;

import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.model.L2Clan;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.quest.Quest;
import l2rt.gameserver.model.quest.QuestState;
import l2rt.gameserver.network.serverpackets.SystemMessage;

public class _510_AClansReputation extends Quest implements ScriptFile
{
	private static final int VALDIS = 31331;
	private static final int CLAW = 8767;
	private static final int CLAN_POINTS_REWARD = 50;

	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}

	public _510_AClansReputation()
	{
		super(PARTY_ALL);

		addStartNpc(VALDIS);

		for(int npc = 22215; npc <= 22217; npc++)
			addKillId(npc);
	}

	@Override
	public String onEvent(String event, QuestState st, L2NpcInstance npc)
	{
		int cond = st.getInt("cond");
		String htmltext = event;
		if(event.equals("31331-3.htm"))
		{
			if(cond == 0)
			{
				st.set("cond", "1");
				st.setState(STARTED);
			}
		}
		else if(event.equals("31331-6.htm"))
		{
			st.playSound(SOUND_FINISH);
			st.exitCurrentQuest(true);
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2NpcInstance npc, QuestState st)
	{
		String htmltext = "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>";
		L2Player player = st.getPlayer();
		L2Clan clan = player.getClan();
		if(player.getClan() == null || !player.isClanLeader())
		{
			st.exitCurrentQuest(true);
			htmltext = "31331-0.htm";
		}
		else if(player.getClan().getLevel() < 5)
		{
			st.exitCurrentQuest(true);
			htmltext = "31331-0.htm";
		}
		else
		{
			int cond = st.getInt("cond");
			int id = st.getState();
			if(id == CREATED && cond == 0)
				htmltext = "31331-1.htm";
			else if(id == STARTED && cond == 1)
			{
				long count = st.getQuestItemsCount(CLAW);
				if(count == 0)
					htmltext = "31331-4.htm";
				else if(count >= 1)
				{
					htmltext = "31331-7.htm";// custom html
					st.takeItems(CLAW, -1);
					int increasedPoints = clan.incReputation(CLAN_POINTS_REWARD * (int) count, true, "_510_AClansReputation");
					player.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_SUCCESSFULLY_COMPLETED_A_CLAN_QUEST_S1_POINTS_HAVE_BEEN_ADDED_TO_YOUR_CLAN_REPUTATION_SCORE).addNumber(increasedPoints));
				}
			}
		}
		return htmltext;
	}

	@Override
	public String onKill(L2NpcInstance npc, QuestState st)
	{
		if(!st.getPlayer().isClanLeader())
			st.exitCurrentQuest(true);
		else if(st.getState() == STARTED)
		{
			int npcId = npc.getNpcId();
			if(npcId >= 22215 && npcId <= 22218)
			{
				st.giveItems(CLAW, 1);
				st.playSound(SOUND_ITEMGET);
			}
		}
		return null;
	}
}
