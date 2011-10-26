package quests.Q17_LightAndDarkness;

import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;

/**
 * @author l2.hellknightTeam
 */

public class Q17_LightAndDarkness extends Quest
{
	//NPC
	private static final int HIERARCH = 31517;
	private static final int SAINT_ALTAR_1 = 31508;
	private static final int SAINT_ALTAR_2 = 31509;
	private static final int SAINT_ALTAR_3 = 31510;
	private static final int SAINT_ALTAR_4 = 31511;
	//ITEMS
	private static final int BLOOD_OF_SAINT = 7168;

	public Q17_LightAndDarkness(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(HIERARCH);
		addTalkId(HIERARCH);
		addTalkId(SAINT_ALTAR_1);
		addTalkId(SAINT_ALTAR_2);
		addTalkId(SAINT_ALTAR_3);
		addTalkId(SAINT_ALTAR_4);
		
		questItemIds = new int[] {BLOOD_OF_SAINT};
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		if (st == null) 
			return event;
		
		if (event.equalsIgnoreCase("31517-02.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.giveItems(BLOOD_OF_SAINT, 4);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("31508-02.htm"))
		{
			st.takeItems(BLOOD_OF_SAINT, 1);
			st.set("cond", "2");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("31509-02.htm"))
		{
			st.takeItems(BLOOD_OF_SAINT, 1);
			st.set("cond", "3");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("31510-02.htm"))
		{
			st.takeItems(BLOOD_OF_SAINT, 1);
			st.set("cond", "4");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("31511-02.htm"))
		{
			st.takeItems(BLOOD_OF_SAINT, 1);
			st.set("cond", "5");
			st.playSound("ItemSound.quest_middle");
		}
		return event;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(getName());
		if (st == null)
			return htmltext;
		
		final int cond = st.getInt("cond");		
		
		switch (st.getState())
		{
			case State.COMPLETED:
				htmltext = getAlreadyCompletedMsg(player);
				break;
			case State.CREATED:
				QuestState st2 = player.getQuestState("_015_SweetWhisper");
				if (st2 != null && st2.getState() == State.COMPLETED)
				{
					if(st.getPlayer().getLevel() >= 61)					
						htmltext = "31517-00.htm";					
					else
					{
						htmltext = "31517-02a.htm";
						st.exitQuest(true);
					}
				}
				else
				{
					htmltext = "<html><body>Quest Sweet Whisper need to be finished first.</body></html>";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				switch (npc.getNpcId())
				{
					case HIERARCH:
						if (cond > 0 && cond < 5)
						{
							if (st.getQuestItemsCount(BLOOD_OF_SAINT) > 0)
								htmltext = "31517-04.htm";
							else
							{
								htmltext = "31517-05.htm";
								st.exitQuest(true);
								st.playSound("ItemSound.quest_giveup");
							}
						}
						else if (cond == 5 && st.getQuestItemsCount(BLOOD_OF_SAINT) == 0)
						{
							htmltext = "31517-03.htm";
							st.addExpAndSp(697040,54887);
							st.unset("cond");
							st.setState(State.COMPLETED);
							st.playSound("ItemSound.quest_finish");
							st.exitQuest(false);
						}
						break;
					case SAINT_ALTAR_1:
						switch (cond)
						{
							case 1:
								if (st.getQuestItemsCount(BLOOD_OF_SAINT) != 0)
									htmltext = "31508-00.htm";
								else
									htmltext = "31508-02.htm";
								break;
							case 2:
								htmltext = "31508-03.htm";
								break;
						}
						break;
					case SAINT_ALTAR_2:
						switch (cond)
						{
							case 2:
								if (st.getQuestItemsCount(BLOOD_OF_SAINT) != 0)
									htmltext = "31509-00.htm";
								else
									htmltext = "31509-02.htm";
								break;
							case 3:
								htmltext = "31509-03.htm";
								break;
						}
						break;
					case SAINT_ALTAR_3:
						switch (cond)
						{
							case 3:
								if (st.getQuestItemsCount(BLOOD_OF_SAINT) != 0)
									htmltext = "31510-00.htm";
								else
									htmltext = "31510-02.htm";
								break;
							case 4:
								htmltext = "31510-03.htm";
								break;
						}
						break;
					case SAINT_ALTAR_4:
						switch (cond)
						{
							case 4:
								if (st.getQuestItemsCount(BLOOD_OF_SAINT) != 0)
									htmltext = "31511-00.htm";
								else
									htmltext = "31511-02.htm";
								break;
							case 5:
								htmltext = "31511-03.htm";
								break;
						}
						break;
				}
				break;
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new Q17_LightAndDarkness(17, "17_LightAndDarkness", "Light And Darkness");    	
	}
}
