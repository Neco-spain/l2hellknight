package quests.Q238_SuccesFailureOfBusiness;

import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;

public final class Q238_SuccesFailureOfBusiness extends Quest
{
	private static final String	qn			= "238_SuccesFailureOfBusiness";

	// NPCs
	private static final int	HELVETICA		= 32641;

	// MOBs
	private static final int	BRAZIER_OF_PURITY	= 18806;
	private static final int	EVIL_SPIRITS		= 22658;
	private static final int	GUARDIAN_SPIRITS	= 22659;
	// Quest item
	private static final int	VICINITY_OF_FOS				= 14865;
	private static final int	BROKEN_PIECE_OF_MAGIC_FORCE	= 14867;
	private static final int	GUARDIAN_SPIRIT_FRAGMENT	= 14868;

	public Q238_SuccesFailureOfBusiness(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(HELVETICA);
		addTalkId(HELVETICA);
		addKillId(BRAZIER_OF_PURITY);
		addKillId(EVIL_SPIRITS);
		addKillId(GUARDIAN_SPIRITS);

		questItemIds = new int[] { BROKEN_PIECE_OF_MAGIC_FORCE, GUARDIAN_SPIRIT_FRAGMENT };
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		if (event.equalsIgnoreCase("32641-03.htm"))
		{
			st.set("cond","1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("32641-06.htm"))
		{
			st.set("cond","3");
			st.playSound("ItemSound.quest_middle");
		}
		return htmltext;
	}

	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		QuestState qs = player.getQuestState("237_WindsOfChange");
		QuestState qs2 = player.getQuestState("239_WontYouJoinUs");

		if (npc.getNpcId() == HELVETICA)
		{
			switch(st.getState())
			{
				case State.CREATED :
					if (player.getLevel() >= 82 && st.getQuestItemsCount(VICINITY_OF_FOS) == 1 && qs != null && qs.getState() == State.COMPLETED)
					{
						if (qs2 != null && qs2.getState() == State.COMPLETED)
						{
							htmltext = "32641-10.htm";
							st.exitQuest(true);
						}
						else
						{
							htmltext = "32641-01.htm";
						}
					}
					else 
					{
						htmltext = "32641-00.htm";
						st.exitQuest(true);
					}
				break;
				case State.STARTED :
					if (st.getInt("cond") == 1)
					{
						htmltext = "32641-04.htm";
					}
					else if (st.getInt("cond") == 2)
					{
						htmltext = "32641-05.htm";
						st.takeItems(BROKEN_PIECE_OF_MAGIC_FORCE,-1);
					}
					else if (st.getInt("cond") == 3)
					{
						htmltext = "32641-07.htm";
					}
					else if (st.getInt("cond") == 4)
					{
						if (st.getQuestItemsCount(GUARDIAN_SPIRIT_FRAGMENT) == 20)
						{
							htmltext = "32641-08.htm";
							st.giveAdena(283346, true);
							st.takeItems(GUARDIAN_SPIRIT_FRAGMENT,-1);
							st.takeItems(VICINITY_OF_FOS,1);
							st.addExpAndSp(1319736,103553);
							st.setState(State.COMPLETED);
							st.exitQuest(false);
							st.playSound("ItemSound.quest_finish");
						}
						else
							htmltext = "32641-11.htm";
					}
				break;
				case State.COMPLETED :
					htmltext = "32641-09.htm";
				break;
			}
		}
		return htmltext;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(qn);

		if (st == null)
			return null;

		if ((npc.getNpcId() == BRAZIER_OF_PURITY) && (st.getInt("cond") == 1) && (st.getQuestItemsCount(BROKEN_PIECE_OF_MAGIC_FORCE) < 10))
		{
			st.giveItems(BROKEN_PIECE_OF_MAGIC_FORCE,1);
			st.playSound("ItemSound.quest_itemget");
			if (st.getQuestItemsCount(BROKEN_PIECE_OF_MAGIC_FORCE) >= 10)
			{
				st.set("cond","2");
				st.playSound("ItemSound.quest_middle");
			}
		}
		else if ((npc.getNpcId() == EVIL_SPIRITS && st.getInt("cond") == 3) || (npc.getNpcId() == GUARDIAN_SPIRITS && st.getInt("cond") == 3))
			if (st.getQuestItemsCount(GUARDIAN_SPIRIT_FRAGMENT) < 20)
				if (st.getRandom(100) < 80)
				{
					st.giveItems(GUARDIAN_SPIRIT_FRAGMENT,1);
					st.playSound("ItemSound.quest_itemget");
					if (st.getQuestItemsCount(GUARDIAN_SPIRIT_FRAGMENT) >= 20)
					{
						st.set("cond","4");
						st.playSound("ItemSound.quest_middle");
					}
				}
		return null;
	}

	public static void main(String[] args)
	{
		new Q238_SuccesFailureOfBusiness(238, qn, "Failure of Business");
	}
}