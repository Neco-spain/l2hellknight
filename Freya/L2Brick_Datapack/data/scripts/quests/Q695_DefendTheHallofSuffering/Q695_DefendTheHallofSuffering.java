package quests.Q695_DefendTheHallofSuffering;

import l2.brick.Config;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.QuestState;
import l2.brick.gameserver.model.quest.State;
import l2.brick.gameserver.model.quest.jython.QuestJython;


public final class Q695_DefendTheHallofSuffering extends QuestJython
{
	private static final String	QN				= "Q695_DefendTheHallofSuffering";

	// NPCs
	private static final int	TEPIOS			= 32603;
	private static final int	TEPIOSINST		= 32530;
	private static final int	MOUTHOFEKIMUS	= 32537;

	// Quest Item
	private static final int	MARK			= 13691;

	public Q695_DefendTheHallofSuffering(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(TEPIOS);
		addStartNpc(MOUTHOFEKIMUS);

		addTalkId(TEPIOS);
		addTalkId(MOUTHOFEKIMUS);

		questItemIds = new int[]
		{ MARK };
	}

	@Override
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

	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		int npcId = npc.getNpcId();
		byte state = st.getState();
		if (state == State.COMPLETED)
			htmltext = "32603-03.htm";
		else if (state == State.CREATED && npcId == TEPIOS)
		{
			boolean readLvl = (player.getLevel() >= 75 && player.getLevel() <= 82);
			if (readLvl && st.getQuestItemsCount(MARK) == 1)
				htmltext = "32603-01.htm";
			else if (readLvl && st.getQuestItemsCount(MARK) == 0)
				htmltext = "32603-05.htm";
			else
				htmltext = "32603-00.htm";
		}
		else if (state == State.STARTED)
		{
			switch (npcId)
			{
				case MOUTHOFEKIMUS:
					htmltext = "32537-01.htm";
					break;
				case TEPIOSINST:
					htmltext = "32530-01.htm";
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
		new Q695_DefendTheHallofSuffering(695, QN, "Defend The Hall of Suffering");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Quest: Defend The Hall of Suffering");
	}
}
