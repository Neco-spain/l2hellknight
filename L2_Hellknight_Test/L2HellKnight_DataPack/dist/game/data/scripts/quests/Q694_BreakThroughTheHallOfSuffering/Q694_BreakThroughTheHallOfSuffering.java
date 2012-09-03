package quests.Q694_BreakThroughTheHallOfSuffering;

import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;
import l2.hellknight.gameserver.model.quest.jython.QuestJython;

public final class Q694_BreakThroughTheHallOfSuffering extends QuestJython
{
	private static final String	QN				= "Q694_BreakThroughtheHallofSuffering";
	private static final int	TEPIOS			= 32603;
	private static final int	TEPIOSINST		= 32530;
	private static final int	MOUTHOFEKIMUS	= 32537;
	private static final int	MARK			= 13691;

	public Q694_BreakThroughTheHallOfSuffering(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(TEPIOS);
		addStartNpc(MOUTHOFEKIMUS);

		addTalkId(TEPIOS);
		addTalkId(MOUTHOFEKIMUS);

		questItemIds = new int[]
		{ MARK };
	}
	
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		if (event.equalsIgnoreCase("32603-02.htm"))
		{
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		return htmltext;
	}
	
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		byte state = st.getState();
		if (state == State.COMPLETED)
			htmltext = "32603-03.htm";
		else if (state == State.CREATED && npc.getNpcId() == TEPIOS)
		{
			if (player.getLevel() >= 75 && player.getLevel() <= 82)
				htmltext = "32603-01.htm";
			else if (player.getLevel() > 82 && st.getQuestItemsCount(MARK) == 0)
			{
				st.giveItems(13691, 1);
				st.playSound("ItemSound.quest_middle");
				st.setState(State.COMPLETED);
				htmltext = "32603-05.htm";
			}
			else
				htmltext = "32603-00.htm";
		}
		else if (state == State.STARTED)
		{
			switch (npc.getNpcId())
			{
				case MOUTHOFEKIMUS:
					htmltext = "32537-01.htm";
					break;
				case TEPIOSINST:
					htmltext = "32530-1.htm";
					break;
				case TEPIOS:
					htmltext = "32603-04.htm";
					st.exitQuest(true);
					if (st.getQuestItemsCount(MARK) == 0)
						st.giveItems(13691, 1);
					st.giveItems(736, 1);
					st.playSound("ItemSound.quest_finish");
					break;
			}
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new Q694_BreakThroughTheHallOfSuffering(694, QN, "Break Through the Hall of Suffering");
	}
}
