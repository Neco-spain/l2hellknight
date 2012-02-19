package quests.Q19_GoToThePastureland;

import l2.brick.Config;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.model.quest.QuestState;
import l2.brick.gameserver.model.quest.State;

public class Q19_GoToThePastureland extends Quest
{
	private static final String qn = "19_GoToThePastureland";
	// NPC
	private static final int Vladimir = 31302;
	private static final int Tunatun = 31537;
	// Items
	private static final int Veal = 15532;
	private static final int YoungWildBeastMeat = 7547;
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		
		if (st == null)
			return getNoQuestMsg(player);
		
		if (event.equalsIgnoreCase("31302-2.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
			st.giveItems(Veal, 1);
		}
		else if (event.equalsIgnoreCase("31537-2.html"))
		{
			if (st.getQuestItemsCount(YoungWildBeastMeat) >= 1)
			{
				st.takeItems(YoungWildBeastMeat, 1);
				st.giveItems(57, 50000);
				st.addExpAndSp(136766, 12688);
				st.playSound("ItemSound.quest_finish");
				st.exitQuest(false);
				htmltext = "31537-2.html";
			}
			else if (st.getQuestItemsCount(Veal) >= 1)
			{
				st.takeItems(Veal, 1);
				st.giveItems(57, 147200);
				st.addExpAndSp(385040, 75250);
				st.playSound("ItemSound.quest_finish");
				st.exitQuest(false);
				htmltext = "31537-2.html";
			}
			else
			{
				htmltext = "31537-3.html";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		
		if (st == null)
			return htmltext;
		
		if (npc.getNpcId() == Vladimir)
		{
			switch (st.getState())
			{
				case State.CREATED:
					if (player.getLevel() >= 82)
						htmltext = "31302-1.htm";
					else
						htmltext = "31302-3.html";
					break;
				case State.STARTED:
					htmltext = "31302-4.html";
					break;
				case State.COMPLETED:
					htmltext = getAlreadyCompletedMsg(player);
					break;
			}
		}
		else if (npc.getNpcId() == Tunatun && st.getInt("cond") == 1)
		{
			htmltext = "31537-1.html";
		}
		return htmltext;
	}
	
	public Q19_GoToThePastureland(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(Vladimir);
		addTalkId(Vladimir);
		addTalkId(Tunatun);
		
		questItemIds = new int[] { Veal, YoungWildBeastMeat };
	}
	
	public static void main(String[] args)
	{
		new Q19_GoToThePastureland(19, qn, "Go to the Pastureland");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Quest: Go to the Pastureland");
	}
}