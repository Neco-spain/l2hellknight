package quests.Q18_MeetingWithTheGoldenRam;

import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;

/**
 * @author l2.hellknightTeam
 */

public class Q18_MeetingWithTheGoldenRam extends Quest
{
	// NPC
	private static final int DONAL = 31314;
	private static final int DAISY = 31315;
	private static final int ABERCROMBIE = 31555;
	// ITEM
	private static final int SUPPLY_BOX = 7245;

	public Q18_MeetingWithTheGoldenRam(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(DONAL);
		addTalkId(DONAL);
		addTalkId(DAISY);
		addTalkId(ABERCROMBIE);
		
		questItemIds = new int[] {SUPPLY_BOX};
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		if (event.equalsIgnoreCase("31314-03.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("31315-02.htm"))
		{
			st.set("cond", "2");
			st.giveItems(SUPPLY_BOX, 1);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("31555-02.htm"))
		{
			st.takeItems(SUPPLY_BOX, 1);
			st.addExpAndSp(126668,11731);
			st.giveAdena(40000, true);
			st.unset("cond");
			st.setState(State.COMPLETED);
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(false);
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
				if (st.getPlayer().getLevel() >= 66)
					htmltext = "31314-01.htm";
				else
				{
					htmltext = "31314-02.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				switch (npc.getNpcId())
				{
					case DONAL:
						if (cond == 1)
							htmltext = "31314-04.htm";
						break;
					case DAISY:
						switch (cond)
						{
							case 1:
								htmltext = "31315-01.htm";
								break;
							case 2:
								htmltext = "31315-03.htm";
								break;
						}
						break;
					case ABERCROMBIE:
						if (cond == 2 && st.getQuestItemsCount(SUPPLY_BOX) == 1)
							htmltext = "31555-01.htm";
						break;
				}
				break;
		}

		return htmltext;
	}

	public static void main(String[] args)
	{
		new Q18_MeetingWithTheGoldenRam(18, "18_MeetingWithTheGoldenRam", "Meeting with the Golden Ram");    	
	}
}
