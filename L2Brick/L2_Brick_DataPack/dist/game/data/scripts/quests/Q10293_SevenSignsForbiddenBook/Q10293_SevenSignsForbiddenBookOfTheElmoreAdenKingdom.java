package quests.Q10293_SevenSignsForbiddenBook;

import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.model.quest.QuestState;
import l2.brick.gameserver.model.quest.State;

public class Q10293_SevenSignsForbiddenBookOfTheElmoreAdenKingdom extends Quest
{
	private static final String qn = "Q10293_SevenSignsForbiddenBook";
	// NPC
	private static final int Sophia1 = 32596;
	private static final int Elcadia = 32784;
	private static final int Elcadia_Support = 32785;
	private static final int Books = 32809;
	private static final int Books1 = 32810;
	private static final int Books2 = 32811;
	private static final int Books3 = 32812;
	private static final int Books4 = 32813;
	private static final int Sophia2 = 32861;
	private static final int Sophia3 = 32863;
	// Item
	private static final int SolinasBiography = 17213;
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (npc.getNpcId() == Elcadia)
		{
			if (event.equalsIgnoreCase("32784-04.html"))
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.playSound("ItemSound.quest_accept");
			}
			else if (event.equalsIgnoreCase("32784-09.html"))
			{
				if (player.isSubClassActive())
				{
					htmltext = "32784-10.html";
				}
				else
				{
					st.playSound("ItemSound.quest_finish");
					st.addExpAndSp(15000000, 1500000);
					st.exitQuest(false);
					htmltext = "32784-09.html";
				}
			}
		}
		else if (npc.getNpcId() == Sophia2)
		{
			if (event.equalsIgnoreCase("32861-04.html"))
			{
				st.set("cond", "2");
				st.playSound("ItemSound.quest_middle");
			}
			if (event.equalsIgnoreCase("32861-08.html"))
			{
				st.set("cond", "4");
				st.playSound("ItemSound.quest_middle");
			}
			if (event.equalsIgnoreCase("32861-11.html"))
			{
				st.set("cond", "6");
				st.playSound("ItemSound.quest_middle");
			}
		}
		else if (npc.getNpcId() == Elcadia_Support)
		{
			if (event.equalsIgnoreCase("32785-07.html"))
			{
				st.set("cond", "5");
				st.playSound("ItemSound.quest_middle");
			}
		}
		else if (npc.getNpcId() == Books)
		{
			if (event.equalsIgnoreCase("32809-02.html"))
			{
				st.set("cond", "7");
				st.giveItems(SolinasBiography, 1);
				st.playSound("ItemSound.quest_middle");
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
		{
			return htmltext;
		}
		else if (npc.getNpcId() == Elcadia)
		{
			if (st.getState() == State.COMPLETED)
				htmltext = "32784-02.html";
			else if (player.getLevel() < 81)
				htmltext = "32784-11.htm";
			else if (player.getQuestState("Q10292_SevenSignsGirlofDoubt") == null || player.getQuestState("Q10292_SevenSignsGirlofDoubt").getState() != State.COMPLETED)
				htmltext = "32784-11.htm";
			else if (st.getState() == State.CREATED)
				htmltext = "32784-01.htm";
			else if (st.getInt("cond") == 1)
				htmltext = "32784-06.html";
			else if (st.getInt("cond") >= 8)
				htmltext = "32784-07.html";
		}
		else if (npc.getNpcId() == Elcadia_Support)
		{
			switch (st.getInt("cond"))
			{
				case 1:
					htmltext = "32785-01.html";
					break;
				case 2:
					htmltext = "32785-04.html";
					st.set("cond", "3");
					st.playSound("ItemSound.quest_middle");
					break;
				case 3:
					htmltext = "32785-05.html";
					break;
				case 4:
					htmltext = "32785-06.html";
					break;
				case 5:
					htmltext = "32785-08.html";
					break;
				case 6:
					htmltext = "32785-09.html";
					break;
				case 7:
					htmltext = "32785-11.html";
					st.set("cond", "8");
					st.playSound("ItemSound.quest_middle");
					break;
				case 8:
					htmltext = "32785-12.html";
					break;
			}
		}
		else if (npc.getNpcId() == Sophia1)
		{
			switch (st.getInt("cond"))
			{
				case 1:
				case 2:
				case 3:
				case 4:
				case 5:
				case 6:
				case 7:
					htmltext = "32596-01.html";
					break;
				case 8:
					htmltext = "32596-05.html";
					break;
			}
		}
		else if (npc.getNpcId() == Sophia2)
		{
			switch (st.getInt("cond"))
			{
				case 1:
					htmltext = "32861-01.html";
					break;
				case 2:
					htmltext = "32861-05.html";
					break;
				case 3:
					htmltext = "32861-06.html";
					break;
				case 4:
					htmltext = "32861-09.html";
					break;
				case 5:
					htmltext = "32861-10.html";
					break;
				case 6:
				case 7:
					htmltext = "32861-12.html";
					break;
				case 8:
					htmltext = "32861-14.html";
					break;
			}
		}
		else if (npc.getNpcId() == Books)
		{
			if (st.getInt("cond") == 6)
				htmltext = "32809-01.html";
		}
		else if (npc.getNpcId() == Books1)
		{
			if (st.getInt("cond") == 6)
				htmltext = "32810-01.html";
		}
		else if (npc.getNpcId() == Books2)
		{
			if (st.getInt("cond") == 6)
				htmltext = "32811-01.html";
		}
		else if (npc.getNpcId() == Books3)
		{
			if (st.getInt("cond") == 6)
				htmltext = "32812-01.html";
		}
		else if (npc.getNpcId() == Books4)
		{
			if (st.getInt("cond") == 6)
				htmltext = "32813-01.html";
		}
		return htmltext;
	}
	
	@Override
	public final String onFirstTalk (L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		if (npc.getNpcId() == Sophia3)
		{
			switch (st.getInt("cond"))
			{
				case 1:
				case 2:
				case 3:
				case 4:
				case 5:
				case 6:
				case 7:
					htmltext = "32863-01.html";
					break;
				case 8:
					htmltext = "32863-04.html";
					break;
			}
		}
		return htmltext;
	}

	
	public Q10293_SevenSignsForbiddenBookOfTheElmoreAdenKingdom(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(Elcadia);
		addTalkId(Elcadia);
		addTalkId(Sophia1);
		addTalkId(Elcadia_Support);
		addTalkId(Books);
		addTalkId(Books1);
		addTalkId(Books2);
		addTalkId(Books3);
		addTalkId(Books4);
		addTalkId(Sophia2);
		addTalkId(Sophia3);
		addStartNpc(Sophia3);
		addFirstTalkId(Sophia3);

		
		questItemIds = new int[] { SolinasBiography };
	}
	
	public static void main(String[] args)
	{
		new Q10293_SevenSignsForbiddenBookOfTheElmoreAdenKingdom(10293, qn, "Seven Signs, Forbidden Book of the Elmore Aden Kingdom");
	}
}
