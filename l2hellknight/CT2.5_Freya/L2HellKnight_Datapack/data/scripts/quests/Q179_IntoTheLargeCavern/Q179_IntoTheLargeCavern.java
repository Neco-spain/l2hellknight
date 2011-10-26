package quests.Q179_IntoTheLargeCavern;

import l2.hellknight.Config;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;

public class Q179_IntoTheLargeCavern extends Quest
{
	private static final String qn = "179_IntoTheLargeCavern";
	// NPC's
	private static final int _kekropus = 32138;
	private static final int _nornil = 32258;

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);

		if (st == null)
			return htmltext;

		if (npc.getNpcId() == _kekropus)
		{
			if (event.equalsIgnoreCase("32138-03.htm"))
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.playSound("ItemSound.quest_accept");
			}
		}
		else if (npc.getNpcId() == _nornil)
		{
			if (event.equalsIgnoreCase("32258-08.htm"))
			{
				st.giveItems(391, 1);
				st.giveItems(413, 1);
				st.playSound("ItemSound.quest_finish");
				st.exitQuest(false);
			}
			else if (event.equalsIgnoreCase("32258-09.htm"))
			{
				st.giveItems(847, 2);
				st.giveItems(890, 2);
				st.giveItems(910, 1);
				st.playSound("ItemSound.quest_finish");
				st.exitQuest(false);
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

		QuestState _prev = player.getQuestState("178_IconicTrinity");
		if (_prev != null
			&& _prev.getState() == State.COMPLETED
			&& player.getLevel() >= 17
			&& player.getRace().ordinal() == 5
			&& player.getClassId().level() == 0)
		{
			if (npc.getNpcId() == _kekropus)
			{
				switch(st.getState())
				{
					case State.CREATED :
							htmltext = "32138-01.htm";
						break;
					case State.STARTED :
						if (st.getInt("cond") == 1)
							htmltext = "32138-03.htm";
						break;
					case State.COMPLETED :
						htmltext = getAlreadyCompletedMsg(player);
						break;
				}
			}
			else if (npc.getNpcId() == _nornil && st.getState() == State.STARTED)
			{
				htmltext = "32258-01.htm";
			}
		}
		else
			htmltext = "32138-00.htm";

		return htmltext;
	}

	public Q179_IntoTheLargeCavern(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(_kekropus);
		addTalkId(_kekropus);
		addTalkId(_nornil);
	}

	public static void main(String[] args)
	{
		new Q179_IntoTheLargeCavern(179, qn, "Into The Large Cavern");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Quest: Into The Large Cavern");
	}
}