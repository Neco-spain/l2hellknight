package quests.Q10_IntoTheWorld;

import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.model.quest.QuestState;
import l2.brick.gameserver.model.quest.State;

/**
 * @author l2.brickTeam
 */
public class Q10_IntoTheWorld extends Quest
{
	private final static String QN = "10_IntoTheWorld";
	
	public static void main(String[] args)
	{
		new Q10_IntoTheWorld(10, QN, "Letters Of Love");
	}
	
	private final int VERY_EXPENSIVE_NECKLACE = 7574;
	private final int SCROLL_OF_ESCAPE_GIRAN = 7559;
	private final int MARK_OF_TRAVELER = 7570;
	
	/**
	 * @param questId
	 * @param name
	 * @param descr
	 */
	public Q10_IntoTheWorld(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(30533);
		
		addTalkId(30533);
		addTalkId(30520);
		addTalkId(30650);
		
		questItemIds = new int[] { VERY_EXPENSIVE_NECKLACE };
	}
	
	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		final QuestState st = player.getQuestState(QN);
		if (st == null)
		{
			return htmltext;
		}
		if (event.equalsIgnoreCase("30533-03.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("30520-02.htm"))
		{
			st.set("cond", "2");
			st.giveItems(VERY_EXPENSIVE_NECKLACE, 1);
		}
		else if (event.equalsIgnoreCase("30650-02.htm"))
		{
			st.set("cond", "3");
			st.takeItems(VERY_EXPENSIVE_NECKLACE, 1);
		}
		else if (event.equalsIgnoreCase("30533-06.htm"))
		{
			st.rewardItems(SCROLL_OF_ESCAPE_GIRAN, 1);
			st.giveItems(MARK_OF_TRAVELER, 1);
			st.unset("cond");
			st.exitQuest(false);
			st.playSound("ItemSound.quest_finish");
		}
		return htmltext;
	}
	
	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		int npcId = npc.getNpcId();
		String htmltext = "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>";
		
		final QuestState st = player.getQuestState(QN);
		if (st == null)
		{
			return htmltext;
		}
		byte id = st.getState();
		int cond = st.getInt("cond");
		if ((npcId == 30533) && (id == State.COMPLETED))
		{
			htmltext = "<html><body>I can't supply you with another Giran Scroll of Escape. Sorry traveller.</body></html>";
		}
		else if (id == State.CREATED)
		{
			if (player.getRace().ordinal() == 4)
			{
				htmltext = "30533-02.htm";
			}
			else
			{
				htmltext = "30533-01.htm";
			}
			st.exitQuest(true);
		}
		else if (id == State.STARTED)
		{
			if ((npcId == 30533) && (cond == 1))
			{
				htmltext = "30533-04.htm";
			}
			else if ((npcId == 30520) && (st.getInt("cond") == 3))
			{
				htmltext = "30520-04.htm";
				st.set("cond", "4");
			}
			else if ((npcId == 30520) && (cond > 0))
			{
				if (st.getQuestItemsCount(VERY_EXPENSIVE_NECKLACE) == 0)
				{
					htmltext = "30520-01.htm";
				}
				else
				{
					htmltext = "30520-03.htm";
				}
			}
			else if ((npcId == 30650) && (cond == 2))
			{
				if (st.getQuestItemsCount(VERY_EXPENSIVE_NECKLACE) > 0)
				{
					htmltext = "30650-01.htm";
				}
			}
			else if ((npcId == 30533) && (cond == 4))
			{
				htmltext = "30533-05.htm";
			}
		}
		return htmltext;
	}
	
}
