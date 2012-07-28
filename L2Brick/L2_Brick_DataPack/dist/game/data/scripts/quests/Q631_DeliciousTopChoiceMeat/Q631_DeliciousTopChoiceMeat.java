/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package quests.Q631_DeliciousTopChoiceMeat;

import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.model.quest.QuestState;
import l2.brick.gameserver.model.quest.State;
import l2.brick.gameserver.util.Util;
import l2.brick.util.Rnd;

public class Q631_DeliciousTopChoiceMeat extends Quest
{
	private static final String qn = "Q631_DeliciousTopChoiceMeat";
	private static final int TUNATUN = 31537;
	private static final int MEAT = 15534;
	private static final int[] MONSTERS = {18898, 18876, 18891, 18884};
	private static final int CHANCE = 172;
	private static final int[] PIECES_REWARD = {10397, 10398, 10399, 10400, 10401, 10402, 10403, 10404, 10405};
	private static final int[]RECIPE_REWARD = {10373, 10374, 10375, 10376, 10377, 10378, 10379, 10380, 10381};

	@Override
	public String onAdvEvent (String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);

		if (st == null)
			return htmltext;

		if (npc.getNpcId() == TUNATUN)
		{
			if (event.equalsIgnoreCase("31537-02.htm"))
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.playSound("ItemSound.quest_accept");
			}
			else if (event.equalsIgnoreCase("31537-05.htm"))
			{
				int i0 = Rnd.get(10);
				if (i0 == 0)
				{
					int RND = RECIPE_REWARD[Rnd.get(0, RECIPE_REWARD.length-1)];
					st.giveItems(RND, 1);
				}
				else if (i0 == 1)
				{
					int RND = PIECES_REWARD[Rnd.get(0, PIECES_REWARD.length-1)];
					st.giveItems(RND, 1);
				}
				else if (i0 == 2)
				{
					int RND = PIECES_REWARD[Rnd.get(0, PIECES_REWARD.length-1)];
					st.giveItems(RND, 2);
				}
				else if (i0 == 3)
				{
					int RND = PIECES_REWARD[Rnd.get(0, PIECES_REWARD.length-1)];
					st.giveItems(RND, 3);
				}
				else if (i0 == 4)
				{
					int RND = PIECES_REWARD[Rnd.get(0, PIECES_REWARD.length-1)];
					st.giveItems(RND, 2 + Rnd.get(5));
				}
				else if (i0 == 5)
				{
					int RND = PIECES_REWARD[Rnd.get(0, PIECES_REWARD.length-1)];
					st.giveItems(RND, 2 + Rnd.get(7));
				}
				else if (i0 == 6)
				{
					st.giveItems(15482, 1);
				}
				else if (i0 == 7)
				{
					st.giveItems(15482, 2);
				}
				else if (i0 == 8)
				{
					st.giveItems(15483, 1);
				}
				else
				{
					st.giveItems(15483, 2);
				}
				st.unset("cond");
				st.takeItems(MEAT, -1);
				st.playSound("ItemSound.quest_finish");
				st.exitQuest(true);
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

		if (npc.getNpcId() == TUNATUN)
		{
			if (player.getLevel() < 82)
				htmltext = "31537-00.htm";
			else if (st.getState() == State.CREATED)
				htmltext = "31537-01.htm";
			else if (st.getState() == State.STARTED && st.getInt("cond") == 1)
				htmltext = "31537-03.htm";
			else if (st.getState() == State.STARTED && st.getInt("cond") == 2)
				htmltext = "31537-04.htm";
		}
		return htmltext;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return null;

		if (st.getState() == State.STARTED && st.getInt("cond") == 1)
		{
			if (Util.contains(MONSTERS, npc.getNpcId()) && st.getQuestItemsCount(MEAT) < 120 && Rnd.get(1000) < CHANCE)
			{
				st.giveItems(MEAT, 1);
				st.playSound("ItemSound.quest_itemget");
			}
			if(st.getQuestItemsCount(MEAT) == 120)
			{
				st.set("cond", "2");
				st.playSound("ItemSound.quest_middle");
			}
		}
		return super.onKill(npc, player, isPet);
	}

	public Q631_DeliciousTopChoiceMeat(int questId, String name, String descr)
	{
		super(questId, name, descr);

		questItemIds = new int[] { MEAT };

		addStartNpc(TUNATUN);
		addTalkId(TUNATUN);
		for(int mob : MONSTERS)
			addKillId(mob);
	}

	public static void main(String[] args)
	{
		new Q631_DeliciousTopChoiceMeat(631, qn, "Delicious Top Choice Meat");
	}
}