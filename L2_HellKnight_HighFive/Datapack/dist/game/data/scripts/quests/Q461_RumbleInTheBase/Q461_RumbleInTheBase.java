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
package quests.Q461_RumbleInTheBase;

import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.QuestState.QuestType;
import l2.hellknight.gameserver.model.quest.State;

/**
 * Rumble in the Base (461).
 * @author malyelfik
 */
public class Q461_RumbleInTheBase extends Quest
{
	public static final String qn = "461_RumbleInTheBase";
	
	// NPC
	public static final int Stan = 30200;
	public static final int[] Monsters =
	{
		22780,
		22781,
		22782,
		2278,
		22784,
		22785,
		18908
	};
	
	// Item
	public static final int ShinySalmon = 15503;
	public static final int ShoesStringOfSelMahum = 16382;
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		final QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		if (event.equalsIgnoreCase("30200-05.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		final QuestState prev = player.getQuestState("252_ItSmellsDelicious");
		switch (st.getState())
		{
			case State.CREATED:
				htmltext = ((player.getLevel() >= 82) && (prev != null) && prev.isCompleted()) ? "30200-01.htm" : "30200-02.htm";
				break;
			case State.STARTED:
				if (st.getInt("cond") == 1)
				{
					htmltext = "30200-06.html";
				}
				else
				{
					st.takeItems(ShinySalmon, -1);
					st.takeItems(ShoesStringOfSelMahum, -1);
					st.addExpAndSp(224784, 342528);
					st.playSound("ItemSound.quest_finish");
					st.exitQuest(QuestType.DAILY);
					htmltext = "30200-07.html";
				}
				break;
			case State.COMPLETED:
				if (!st.isNowAvailable())
				{
					htmltext = "30200-03.htm";
				}
				else
				{
					st.setState(State.CREATED);
					htmltext = ((player.getLevel() >= 82) && (prev != null) && (prev.getState() == State.COMPLETED)) ? "30200-01.htm" : "30200-02.htm";
				}
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		final L2PcInstance partyMember = getRandomPartyMember(player, "1");
		if (partyMember == null)
		{
			return null;
		}
		
		final QuestState st = partyMember.getQuestState(qn);
		int chance = getRandom(1000);
		boolean giveItem = false;
		switch (npc.getNpcId())
		{
			case 22780:
				if (chance < 581)
				{
					giveItem = true;
				}
				break;
			case 22781:
				if (chance < 772)
				{
					giveItem = true;
				}
				break;
			case 22782:
				if (chance < 581)
				{
					giveItem = true;
				}
				break;
			case 22783:
				if (chance < 563)
				{
					giveItem = true;
				}
				break;
			case 22784:
				if (chance < 581)
				{
					giveItem = true;
				}
				break;
			case 22785:
				if (chance < 271)
				{
					giveItem = true;
				}
				break;
			case 18908:
				if ((chance < 271) && (st.getQuestItemsCount(ShinySalmon) < 5))
				{
					st.giveItems(ShinySalmon, 1);
					st.playSound("ItemSound.quest_itemget");
				}
				break;
		}
		
		if (giveItem && (st.getQuestItemsCount(ShoesStringOfSelMahum) < 10))
		{
			st.giveItems(ShoesStringOfSelMahum, 1);
			st.playSound("ItemSound.quest_itemget");
		}
		
		if ((st.getQuestItemsCount(ShinySalmon) == 5) && (st.getQuestItemsCount(ShoesStringOfSelMahum) == 10))
		{
			st.set("cond", "2");
			st.playSound("ItemSound.quest_middle");
		}
		return null;
	}
	
	public Q461_RumbleInTheBase(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(Stan);
		addTalkId(Stan);
		addKillId(Monsters);
		
		questItemIds = new int[]
		{
			ShinySalmon,
			ShoesStringOfSelMahum
		};
	}
	
	public static void main(String[] args)
	{
		new Q461_RumbleInTheBase(461, qn, "Rumble in the Base");
	}
}