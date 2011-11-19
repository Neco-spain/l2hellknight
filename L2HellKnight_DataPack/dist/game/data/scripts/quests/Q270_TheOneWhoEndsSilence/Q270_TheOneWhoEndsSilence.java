package quests.Q270_TheOneWhoEndsSilence;

import l2.brick.Config;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.model.quest.QuestState;
import l2.brick.gameserver.model.quest.State;
import l2.brick.gameserver.util.Util;

public class Q270_TheOneWhoEndsSilence extends Quest
{
	public static final int GREYMORE	= 32757;
	public static final int CLOTHES		= 15526;

	public static final String qn = "Q270_TheOneWhoEndsSilence";
	
	private static final int[] MOBS1 =
	{
		22781, 22790, 22792, 22793
	};
	private static final int[] MOBS2 =
	{
		22794, 22795, 22796, 22797, 22798, 22799, 22800
	};

	private static final int[][] SP =
	{{5593, 1}, {5594, 1}, {5595, 1}, {9898, 1}};
	private static final int[][] REC =
	{{10373, 1}, {10374, 1}, {10375, 1}, {10376, 1}, {10377, 1}, {10378, 1}, {10379, 1}, {10380, 1}, {10381, 1}};
	private static final int[][] MAT =
	{{10397, 1}, {10398, 1}, {10399, 1}, {10400, 1}, {10401, 1}, {10402, 1}, {10403, 1}, {10404, 1}, {10405, 1}};
	private static final int[][] RECSP =
	{{10373, 1}, {10374, 1}, {10375, 1}, {10376, 1}, {10377, 1}, {10378, 1}, {10379, 1}, {10380, 1}, {10381, 1}, {5593, 1}, {5594, 1}, {5595, 1}, {9898, 1}};
	
	public Q270_TheOneWhoEndsSilence(int id, String name, String descr)
	{
		super(id,name,descr);
		questItemIds = new int[] { CLOTHES};
		addStartNpc(GREYMORE);
		addTalkId(GREYMORE);

		for (int i : MOBS1)
			addKillId(i);	
		for (int i : MOBS2)
			addKillId(i);
	}
	
	@Override
	public String onAdvEvent (String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		int a = st.getRandom(13);
		int b = st.getRandom(4);
		int c = st.getRandom(9);
		int d = st.getRandom(9);
		int e = st.getRandom(9);
		int f = st.getRandom(4);
		

		if (npc.getNpcId() == GREYMORE)
		{
			if (event.equalsIgnoreCase("32757-04.htm"))
			{
				st.set("cond","1");
				st.setState(State.STARTED);
				st.playSound("ItemSound.quest_accept");
			}
			else if (event.equalsIgnoreCase("32757-08.htm"))
			{
				if (st.getQuestItemsCount(CLOTHES) == 0)
					htmltext = "32757-06.htm";
				else if (st.getQuestItemsCount(CLOTHES) < 100)
					htmltext = "32757-07.htm";
				else if (st.getQuestItemsCount(CLOTHES) >= 100)
					htmltext = "32757-08.htm";
			}
			else if (event.equalsIgnoreCase("32757-13.htm"))
			{
				st.unset("cond");
				st.exitQuest(true);
				htmltext = "32757-13.htm";
			}
			else if (event.equalsIgnoreCase("32757-05.htm"))
			{
				if (st.getQuestItemsCount(CLOTHES) < 100)
					htmltext = "32757-10.htm";
				else if (st.getQuestItemsCount(CLOTHES) >= 100)
					htmltext = "32757-05.htm";
			}
			else if (event.equalsIgnoreCase("32757-100.htm"))
			{
				if (st.getQuestItemsCount(CLOTHES) >= 100)
				{
					st.giveItems(RECSP[a][0],RECSP[a][1]);
					st.takeItems(CLOTHES, 100);
					st.playSound("ItemSound.quest_finish");
					htmltext = "32757-09.htm";
				}
				else
					htmltext = "32757-10.htm";
			}
			else if (event.equalsIgnoreCase("32757-200.htm"))
			{
				if (st.getQuestItemsCount(CLOTHES) >= 200)
				{
					st.giveItems(SP[b][0],SP[b][1]);
					st.giveItems(REC[c][0],REC[c][1]);
					st.takeItems(CLOTHES, 200);
					st.playSound("ItemSound.quest_finish");
					htmltext = "32757-09.htm";
				}
				else
					htmltext = "32757-10.htm";
			}
			else if (event.equalsIgnoreCase("32757-300.htm"))
			{
				if (st.getQuestItemsCount(CLOTHES) >= 300)
				{
					st.giveItems(SP[b][0],SP[b][1]);
					st.giveItems(REC[c][0],REC[c][1]);
					st.giveItems(MAT[d][0],MAT[d][1]);
					st.takeItems(CLOTHES, 300);
					st.playSound("ItemSound.quest_finish");
					htmltext = "32757-09.htm";
				}
				else
					htmltext = "32757-10.htm";
			}
			else if (event.equalsIgnoreCase("32757-400.htm"))
			{
				if (st.getQuestItemsCount(CLOTHES) >= 400)
				{
					st.giveItems(SP[b][0],SP[b][1]);
					st.giveItems(REC[c][0],REC[c][1]);
					st.giveItems(REC[d][0],REC[d][1]);
					st.giveItems(MAT[e][0],MAT[e][1]);
					st.takeItems(CLOTHES, 400);
					st.playSound("ItemSound.quest_finish");
					htmltext = "32757-09.htm";
				}
				else
					htmltext = "32757-10.htm";
			}
			else if (event.equalsIgnoreCase("32757-500.htm"))
			{
				if (st.getQuestItemsCount(CLOTHES) >= 500)
				{
					st.giveItems(SP[b][0],SP[b][1]);
					st.giveItems(SP[f][0],SP[f][1]);
					st.giveItems(REC[c][0],REC[c][1]);
					st.giveItems(REC[d][0],REC[d][1]);
					st.giveItems(MAT[e][0],MAT[e][1]);
					st.takeItems(CLOTHES, 500);
					st.playSound("ItemSound.quest_finish");
					htmltext = "32757-09.htm";
				}
				else
					htmltext = "32757-10.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc,L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		QuestState _prev = player.getQuestState("10288_SecretMission");
		if (st == null)
			return htmltext;
		
		if(npc.getNpcId() == GREYMORE)
		{
			switch (st.getState())
			{
				case State.CREATED:
					if (player.getLevel() >= 82 && (_prev != null && _prev.getState() == State.COMPLETED))
						htmltext = "32757-01.htm";
					else
						htmltext = "32757-03.htm";
				break;
				case State.STARTED:
					if (st.getInt("cond") == 1)
							htmltext = "32757-05.htm";
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet) 
	{
		QuestState st = player.getQuestState(getName());
		int npcId = npc.getNpcId();
		if (st == null || st.getState() != State.STARTED)
			return null;
		if (Util.contains(MOBS1, npcId))
		{
			int chance = (int) (25 * Config.RATE_QUEST_DROP);
			int numItems = (int) (chance / 100);
			chance = chance % 100;
			if (st.getRandom(100) < chance)
				numItems++;
			if (numItems > 0)
			{
				st.playSound("ItemSound.quest_itemget");
				st.giveItems(CLOTHES, numItems);
			}
		}
		else if (Util.contains(MOBS2, npcId))
		{
			int chance = (int) (100 * Config.RATE_QUEST_DROP);
			int numItems = (int) (chance / 100);
			chance = chance % 100;
			if (st.getRandom(100) < chance)
				numItems++;
			if (numItems > 0)
			{
				st.playSound("ItemSound.quest_itemget");
				st.giveItems(CLOTHES, numItems);
			}
		}
		return super.onKill(npc, player, isPet);
	}

	public static void main(String[] args)
	{
		new Q270_TheOneWhoEndsSilence(270, qn, "The One Who Ends Silence");
	}
}