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
package quests.Q254_LegendaryTales;

import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.model.quest.QuestState;
import l2.brick.gameserver.model.quest.State;
import l2.brick.gameserver.util.Util;

public class Q254_LegendaryTales extends Quest
{
	private static final String QUEST_NAME = "Q254_LegendaryTales";
	// NPC
	private static final int GILMORE = 30754;
	// Items
	private static final int LARGE_DRAGON_SKULL = 17249;
	// Mobs
	private static final int EMERALD_HORN = 25718;
	private static final int DUST_RIDER = 25719;
	private static final int BLEEDING_FLY = 25720;
	private static final int BLACKDAGGER_WING = 25721;
	private static final int SHADOW_SUMMONER = 25722;
	private static final int SPIKE_SLASHER = 25723;
	private static final int MUSCLE_BOMBER = 25724;

	private static final int[] BOSS = {25718, 25719, 25720, 25721, 25722, 25723, 25724};

	private static final int[] REWARDS = {0, 13457, 13458, 13459, 13460, 13461, 13462, 13463, 13464, 13465, 13466, 13467};

	@Override
	public String onAdvEvent (String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(QUEST_NAME);

		if (st == null)
			return htmltext;

		if (npc.getNpcId() == GILMORE)
		{
			if (event.equalsIgnoreCase("accept"))
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.playSound("ItemSound.quest_accept");
				htmltext = "30754-7.htm";
			}
			
			else if (event.equalsIgnoreCase("emerald"))
			{
				if (st.getInt("emerald") != 1)
					htmltext = "30754-16.html";
				else
					htmltext = "30754-22.html";
			}
			else if (event.equalsIgnoreCase("dust"))
			{
				if (st.getInt("dust") != 1)
					htmltext = "30754-17.html";
				else
					htmltext = "30754-23.html";
			}
			else if (event.equalsIgnoreCase("bleeding"))
			{
				if (st.getInt("bleeding") != 1)
					htmltext = "30754-18.html";
				else
					htmltext = "30754-24.html";
			}
			else if (event.equalsIgnoreCase("daggerwyrm"))
			{
				if (st.getInt("blackdagger") != 1)
					htmltext = "30754-19.html";
				else
					htmltext = "30754-25.html";
			}
			else if (event.equalsIgnoreCase("shadowsummoner"))
			{
				if (st.getInt("shadow") != 1)
					htmltext = "30754-16.html";
				else
					htmltext = "30754-26.html";
			}
			else if (event.equalsIgnoreCase("spikeslasher"))
			{
				if (st.getInt("spike") != 1)
					htmltext = "30754-17.html";
				else
					htmltext = "30754-27.html";
			}
			else if (event.equalsIgnoreCase("muclebomber"))
			{
				if (st.getInt("muscle") != 1)
					htmltext = "30754-18.html";
				else
					htmltext = "30754-28.html";
			}
			
			else if (Util.isDigit(event))
			{
				final int reward_id = Integer.parseInt(event);
				if (reward_id > 0)
				{
					if (st.getQuestItemsCount(LARGE_DRAGON_SKULL) == 7)
					{
						int REWARD = REWARDS[reward_id];
						
						st.takeItems(LARGE_DRAGON_SKULL, -1);
						st.giveItems(REWARD, 1);
						htmltext = "30754-13.html";
						st.playSound("ItemSound.quest_finish");
						st.unset("emerald");
						st.unset("dust");
						st.unset("bleeding");
						st.unset("blackdagger");
						st.unset("shadow");
						st.unset("spike");
						st.unset("muscle");
						st.exitQuest(false);
					}
					else
						htmltext = "30754-12.htm";
				}
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

		if (npc.getNpcId() == GILMORE)
		{
			switch (st.getState())
			{
				case State.CREATED:
				{
					if (player.getLevel() < 80)
						htmltext = "30754-3.html";
					else
						htmltext = "30754-1.htm";
					break;
				}
				case State.STARTED:
				{
					if (st.getInt("cond") == 1 && (st.getInt("emerald") != 1 || st.getInt("dust") != 1 || st.getInt("bleeding") != 1 || st.getInt("blackdagger") != 1 || st.getInt("shadow") != 1 || st.getInt("spike") != 1 || st.getInt("muscle") != 1))
						htmltext = "30754-9.htm";
					else if (st.getInt("cond") == 2 && st.getInt("emerald") == 1 && st.getInt("dust") == 1 && st.getInt("bleeding") == 1 && st.getInt("blackdagger") == 1 && st.getInt("shadow") == 1 && st.getInt("spike") == 1 && st.getInt("muscle") == 1)
						htmltext = "30754-10.html";
					break;
				}
				case State.COMPLETED:
				{
					htmltext = "30754-2.html";
					break;
				}
			}
		}
		return htmltext;
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
					rewardPlayer(npc, memb);
			}
			else
				rewardPlayer(npc, player);
		}
		return super.onKill(npc, player, isPet);
	}
	
	private void rewardPlayer(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(QUEST_NAME);
		int rb = npc.getNpcId();
		if (st != null && st.getState() == State.STARTED && player.isInsideRadius(npc, 2000, false, false))
		{
			if (rb == EMERALD_HORN && st.getInt("emerald") != 1)
			{
				st.giveItems(LARGE_DRAGON_SKULL, 1);
				st.set("emerald", "1");
			}
			else if (rb == DUST_RIDER && st.getInt("dust") != 1)
			{
				st.giveItems(LARGE_DRAGON_SKULL, 1);
				st.set("dust", "1");
			}
			else if (rb == BLEEDING_FLY && st.getInt("bleeding") != 1)
			{
				st.giveItems(LARGE_DRAGON_SKULL, 1);
				st.set("bleeding", "1");
			}
			else if (rb == BLACKDAGGER_WING && st.getInt("blackdagger") != 1)
			{
				st.giveItems(LARGE_DRAGON_SKULL, 1);
				st.set("blackdagger", "1");
			}
			else if (rb == SHADOW_SUMMONER && st.getInt("shadow") != 1)
			{
				st.giveItems(LARGE_DRAGON_SKULL, 1);
				st.set("shadow", "1");
			}
			else if (rb == SPIKE_SLASHER && st.getInt("spike") != 1)
			{
				st.giveItems(LARGE_DRAGON_SKULL, 1);
				st.set("spike", "1");
			}
			else if (rb == MUSCLE_BOMBER && st.getInt("muscle") != 1)
			{
				st.giveItems(LARGE_DRAGON_SKULL, 1);
				st.set("muscle", "1");
			}
			
			if (st.getInt("emerald") == 1 && st.getInt("dust") == 1 && st.getInt("bleeding") == 1 && st.getInt("blackdagger") == 1 && st.getInt("shadow") == 1 && st.getInt("spike") == 1 && st.getInt("muscle") == 1)
			{
				st.set("cond", "2");
				st.playSound("ItemSound.quest_middle");
			}
		}
	}
	
	public Q254_LegendaryTales(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		questItemIds = new int[] { LARGE_DRAGON_SKULL };
		
		addStartNpc(GILMORE);
		addTalkId(GILMORE);

		for(int mob : BOSS)
			addKillId(mob);
	}

	public static void main(String[] args)
	{
		new Q254_LegendaryTales(254, QUEST_NAME, "Legendary Tales");
	}
}