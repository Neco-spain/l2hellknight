package quests._10291_FireDragonDestroyer;

import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.quest.Quest;
import l2rt.gameserver.model.quest.QuestState;

/**
 * Fire Dragon Destroyer (10291)
 * @author malyelfik
 */
public class _10291_FireDragonDestroyer extends Quest implements ScriptFile
{
	// NPC
	private static final int Klein = 31540;
	private static final int Valakas = 29028;
	// Item
	private static final int FloatingStone = 7267;
	private static final int PoorNecklace = 15524;
	private static final int ValorNecklace = 15525;
	private static final int ValakaSlayerCirclet = 8567;
	
	public _10291_FireDragonDestroyer()
	{
		super(false);
		
		addStartNpc(Klein);
		addTalkId(Klein);
		addKillId(Valakas);
		
	}
	
	@Override
	public String onEvent(String event, QuestState st, L2NpcInstance npc)
	{
		String htmltext = event;
		
		
		if (event.equalsIgnoreCase("31540-07.htm"))
		{
			st.setState(STARTED);
			st.set("cond", "1");
			st.giveItems(PoorNecklace, 1);
			st.playSound(SOUND_ACCEPT);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		
		switch (st.getState())
		{
			case CREATED:
			{
				if (st.getPlayer().getLevel() >= 83 && st.getQuestItemsCount(FloatingStone) >= 1)
					htmltext = "31540-01.htm";
				else if (st.getPlayer().getLevel() < 83)
					htmltext = "31540-02.htm";
				else
					htmltext = "31540-04.htm";
				break;
			}
			case STARTED:
			{
				if (st.getInt("cond") == 1 && st.getQuestItemsCount(PoorNecklace) >= 1)
					htmltext = "31540-08.htm";
				else if (st.getInt("cond") == 1 && st.getQuestItemsCount(PoorNecklace) == 0)
				{
					st.giveItems(PoorNecklace, 1);
					htmltext = "31540-09.htm";
				}
				else if (st.getInt("cond") == 2)
				{
					st.takeItems(ValorNecklace, 1);
					st.giveItems(57, 126549);
					st.addExpAndSp(717291, 77397);
					st.giveItems(ValakaSlayerCirclet, 1);
					st.unset("cond");
					st.exitCurrentQuest(false);
					st.playSound("ItemSound.quest_finish");
					htmltext = "31540-10.htm";
				}
				break;
			}
			case COMPLETED:
			{
				htmltext = "31540-03.htm";
				break;
			}
		}
		
		return htmltext;
	}
	/*
	@Override
	public String onKill(L2NpcInstance npc, QuestState st)
	{
		if (st.getPlayer().getParty() != null)
		{
			for (L2Player partyMember : st.getPlayer().getParty().getPartyMembers());
			rewardPlayer(_party);
		}
		else
			rewardPlayer(player);
		return null;
	}
	
	private void rewardPlayer(L2Player player)
	{
		QuestState st = player.getQuestState("_10291_FireDragonDestroyer");
		
		if (st != null && st.getInt("cond") == 1)
		{
			st.takeItems(PoorNecklace, 1);
			st.giveItems(ValorNecklace, 1);
			st.playSound("ItemSound.quest_middle");
			st.set("cond", "2");
		}
	}
	*/
	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}
	
	
}