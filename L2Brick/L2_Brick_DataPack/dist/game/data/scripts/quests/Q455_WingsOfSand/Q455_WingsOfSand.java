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
package quests.Q455_WingsOfSand;

import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.model.quest.QuestState;
import l2.brick.gameserver.model.quest.State;
import l2.brick.gameserver.util.Util;
import l2.brick.util.Rnd;

public class Q455_WingsOfSand extends Quest
{
	private static final String QUEST_NAME = "Q455_WingsOfSand";
	private static final int[] SEPARATED_SOUL = { 32864, 32865, 32866, 32867, 32868, 32869, 32870 };
	private static final int LARGE_BABY_DRAGON = 17250;
	private static final int DROP_CHANCE = 30;

	
	private static final int[] BOSS = {
		25718, 25719, 25720, 25721, 25722, 25723, 25724
	};

	private static final int[][] PIECES = {
		{ 15771, 1, 2 }, { 15770, 1, 2 }, { 15769, 1, 2 },
		{ 15637, 1, 2 }, { 15636, 1, 2 }, { 15634, 1, 2 },
		{ 15635, 1, 2 }, { 15644, 1, 2 }, { 15642, 1, 2 },
		{ 15640, 1, 2 }, { 15643, 1, 2 }, { 15641, 1, 2 },
		{ 15639, 1, 2 }, { 15638, 1, 2 }, { 15660, 1, 2 },
		{ 15663, 1, 2 }, { 15666, 1, 2 }, { 15667, 1, 2 },
		{ 15668, 1, 2 }, { 15669, 1, 2 }, { 15661, 1, 2 },
		{ 15664, 1, 2 }, { 15670, 1, 2 }, { 15671, 1, 2 },
		{ 15672, 1, 2 }, { 15662, 1, 2 }, { 15665, 1, 2 },
		{ 15673, 1, 2 }, { 15674, 1, 2 }, { 15675, 1, 2 },
		{ 15691, 1, 2 }
	};
	
	private static final int[][] RECIPES = {
		{ 15818, 1 }, { 15817, 1 }, { 15815, 1 },
		{ 15816, 1 }, { 15825, 1 }, { 15823, 1 },
		{ 15821, 1 }, { 15824, 1 }, { 15822, 1 },
		{ 15820, 1 }, { 15819, 1 }, { 15792, 1 },
		{ 15795, 1 }, { 15798, 1 }, { 15801, 1 },
		{ 15804, 1 }, { 15808, 1 }, { 15793, 1 },
		{ 15796, 1 }, { 15799, 1 }, { 15802, 1 },
		{ 15805, 1 }, { 15794, 1 }, { 15797, 1 },
		{ 15800, 1 }, { 15803, 1 }, { 15806, 1 },
		{ 15807, 1 }, { 15811, 1 }, { 15810, 1 },
		{ 15809, 1 }
	};
	
	private static final int[][] ATT_CRY = {
		{ 9552, 1 }, { 9553, 1 },
		{ 9554, 1 }, { 9555, 1 },
		{ 9557, 1 }, { 9556, 1 }
	};
	
	private static final int[][] BEWS_BEAS = {
		{ 6577, 1 }, { 6578, 1 }
	};
	
	@Override
	public String onAdvEvent (String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(QUEST_NAME);

		if (st == null)
			return htmltext;

		if (Util.contains(SEPARATED_SOUL, npc.getNpcId()))
		{
			if (event.equalsIgnoreCase("accept"))
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.playSound("ItemSound.quest_accept");
				htmltext = "WingsOfSand-accept.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(QUEST_NAME);
		if (st == null)
			return htmltext;

		if (Util.contains(SEPARATED_SOUL, npc.getNpcId()))
		{
			switch (st.getState())
			{
				case State.CREATED:
				{
					if (player.getLevel() < 80)
						htmltext = "WingsOfSand-level.html";
					else
						htmltext = "WingsOfSand-1.htm";
					break;
				}
				case State.STARTED:
				{
					if (st.getInt("cond") == 1 && !st.hasQuestItems(LARGE_BABY_DRAGON))
						htmltext = "WingsOfSand-gokill.html";
					else if (st.getInt("cond") == 1 && st.hasQuestItems(LARGE_BABY_DRAGON))
					{
						st.takeItems(LARGE_BABY_DRAGON, -1);
						rewardPlayer(npc, player);
						st.unset("cond");
						st.exitQuest(false);
						st.playSound("ItemSound.quest_finish");
						htmltext = "WingsOfSand-reward.html";					
					}
					else if (st.getInt("cond") == 2 && st.getQuestItemsCount(LARGE_BABY_DRAGON) == 1)
					{
						st.takeItems(LARGE_BABY_DRAGON, -1);
						rewardPlayer(npc, player);
						st.unset("cond");
						st.exitQuest(false);
						st.playSound("ItemSound.quest_finish");
						htmltext = "WingsOfSand-reward.html";					
					}
					else if (st.getInt("cond") == 3 && st.getQuestItemsCount(LARGE_BABY_DRAGON) > 1)
					{
						st.takeItems(LARGE_BABY_DRAGON, -1);
						rewardPlayer2(npc, player);
						st.unset("cond");
						st.exitQuest(false);
						st.playSound("ItemSound.quest_finish");
						htmltext = "WingsOfSand-reward.html";
					}
					break;
				}
				case State.COMPLETED:
				{
					htmltext = "WingsOfSand-completed.html";
					break;
				}
			}
		}
		return htmltext;
	}
	
	private void rewardPlayer(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(QUEST_NAME);
		if (st != null && player.isInsideRadius(npc, 2000, false, false))
		{
			int random = Rnd.get(100);

			if (random < 10)
			{
				int[] rnd_reward = BEWS_BEAS[Rnd.get(0, BEWS_BEAS.length-1)];
				st.giveItems(rnd_reward[0], rnd_reward[1]);
			}
			else if (random < 30)
			{
				int[] rnd_reward = RECIPES[Rnd.get(0, RECIPES.length-1)];
				st.giveItems(rnd_reward[0], rnd_reward[1]);
			}
			else if (random < 50)
			{
				int[] rnd_reward = ATT_CRY[Rnd.get(0, ATT_CRY.length-1)];
				st.giveItems(rnd_reward[0], rnd_reward[1]);
			}
			else
			{
				int[] rnd_reward = PIECES[Rnd.get(0, PIECES.length-1)];
				st.giveItems(rnd_reward[0], Rnd.get(rnd_reward[1], rnd_reward[2]));
			}
		}
	}
	
	private void rewardPlayer2(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(QUEST_NAME);
		if (st != null && player.isInsideRadius(npc, 2000, false, false))
		{
			int random = Rnd.get(100);

			if (random < 10)
			{
				int[] rnd_reward = BEWS_BEAS[Rnd.get(0, BEWS_BEAS.length-1)];
				st.giveItems(rnd_reward[0], rnd_reward[1]);
			}
			else if (random < 40)
			{
				int[] rnd_reward = RECIPES[Rnd.get(0, RECIPES.length-1)];
				st.giveItems(rnd_reward[0], rnd_reward[1]);
			}
			else if (random < 80)
			{
				int[] rnd_reward = ATT_CRY[Rnd.get(0, ATT_CRY.length-1)];
				st.giveItems(rnd_reward[0], rnd_reward[1]);
			}
			else
			{
				int[] rnd_reward = PIECES[Rnd.get(0, PIECES.length-1)];
				st.giveItems(rnd_reward[0], Rnd.get(rnd_reward[1], rnd_reward[2]));
			}
		}		
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(QUEST_NAME);
		
		if (Util.contains(BOSS, npc.getNpcId()) && st != null)
		{
			if (player.isInParty())
			{
				for(L2PcInstance memb : player.getParty().getPartyMembers())
					giveItem(npc, memb);
			}
			else
				giveItem(npc, player);
		}
		return super.onKill(npc, player, isPet);
	}
	
	private void giveItem(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(QUEST_NAME);
		if (Rnd.get(100) < DROP_CHANCE)
		{
			if (st != null && st.getState() == State.STARTED && player.isInsideRadius(npc, 2000, false, false))
			{
				if (st.getInt("cond") == 1 && !st.hasQuestItems(LARGE_BABY_DRAGON))
				{
					st.giveItems(LARGE_BABY_DRAGON, 1);
					st.set("cond", "2");
					st.playSound("ItemSound.quest_middle");
				}
				else if (st.getInt("cond") == 2 && st.getQuestItemsCount(LARGE_BABY_DRAGON) == 1)
				{
					st.giveItems(LARGE_BABY_DRAGON, 1);
					st.set("cond", "3");
					st.playSound("ItemSound.quest_middle");
				}
			}
		}
	}
	
	public Q455_WingsOfSand(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		questItemIds = new int[] { LARGE_BABY_DRAGON };

		for(int npc : SEPARATED_SOUL)
		{
			addStartNpc(npc);
			addTalkId(npc);
		}

		for(int mob : BOSS)
			addKillId(mob);
	}

	public static void main(String[] args)
	{
		new Q455_WingsOfSand(455, QUEST_NAME, "Wings of Sand");
	}
}