package quests._10290_LandDragonConqueror;

import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.quest.Quest;
import l2rt.gameserver.model.quest.QuestState;

public class _10290_LandDragonConqueror extends Quest implements ScriptFile
{
	// NPC
	private static final int Theodoric = 30755;
	private static final int[] Antharas = { 29019, 29066, 29067, 29068 }; //Old, Weak, Normal, Strong
	// Item
	private static final int PortalStone = 3865;
	private static final int ShabbyNecklace = 15522;
	private static final int MiracleNecklace = 15523;
	private static final int AntharaSlayerCirclet = 8568;
	
	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}
	
	public _10290_LandDragonConqueror()
	{
		super(false);
		
		addStartNpc(Theodoric);
		addTalkId(Theodoric);
		for (int i : Antharas)
			addKillId(i);
		
		//questItemIds = new int[] { MiracleNecklace, ShabbyNecklace };
	}
	
	@Override
	public String onEvent(String event, QuestState st, L2NpcInstance npc)
	{
		String htmltext = event;
		
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30755-07.htm"))
		{
			st.setState(STARTED);
			st.set("cond", "1");
			st.giveItems(ShabbyNecklace, 1);
			st.playSound(SOUND_ACCEPT);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		
		if (st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case CREATED:
			{
				if (st.getPlayer().getLevel() >= 83 && st.getQuestItemsCount(PortalStone) >= 1)
					htmltext = "30755-01.htm";
				else if (st.getPlayer().getLevel() < 83)
					htmltext = "30755-02.htm";
				else
					htmltext = "30755-04.htm";
				break;
			}
			case STARTED:
			{
				if (st.getInt("cond") == 1 && st.getQuestItemsCount(ShabbyNecklace) >= 1)
					htmltext = "30755-08.htm";
				else if (st.getInt("cond") == 1 && st.getQuestItemsCount(ShabbyNecklace) == 0)
				{
					st.giveItems(ShabbyNecklace, 1);
					htmltext = "30755-09.htm";
				}
				else if (st.getInt("cond") == 2)
				{
					st.takeItems(MiracleNecklace, 1);
					st.giveItems(57, 131236);
					st.addExpAndSp(702557, 76334);
					st.giveItems(AntharaSlayerCirclet, 1);
					st.unset("cond");
					st.exitCurrentQuest(false);
					st.playSound(SOUND_FINISH);
					htmltext = "30755-10.htm";
				}
				break;
			}
			case COMPLETED:
			{
				htmltext = "30755-03.htm";
				break;
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(L2NpcInstance npc, QuestState st)
	{
		/*if (st.getPlayer().getParty() != null)
		{
			for (L2PcInstance partyMember : st.getPlayer().getParty().getPartyMembers())
				rewardPlayer(partyMember);
		}
		else
			rewardPlayer(st.getPlayer());*/
		return null;
	}
	
	/*private void rewardPlayer(L2NpcInstance npc, QuestState st)
	{
		
		if (st != null && st.getInt("cond") == 1)
		{
			st.takeItems(ShabbyNecklace, 1);
			st.giveItems(MiracleNecklace, 1);
			st.playSound(SOUND_MIDDLE);
			st.set("cond", "2");
		}
	}*/
}