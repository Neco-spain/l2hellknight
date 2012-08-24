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
package quests.Q50_LanoscosSpecialBait;

import l2.hellknight.Config;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;

/**
 * Lanosco's Special Bait (50)<br>
 * Original Jython script by Kilkenny
 * @author nonom
 */
public class Q50_LanoscosSpecialBait extends Quest
{
	private static final String qn = "50_LanoscosSpecialBait";
	
	// NPCs
	private static final int LANOSCO = 31570;
	private static final int SINGING_WIND = 21026;
	
	// Items
	private static final int ESSENCE_OF_WIND = 7621;
	private static final int WIND_FISHING_LURE = 7610;
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		final QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		switch (event)
		{
			case "31570-03.htm":
				st.set("cond", "1");
				st.setState(State.STARTED);
				st.playSound("ItemSound.quest_accept");
				break;
			case "31570-07.html":
				if ((st.getInt("cond") == 2) && (st.getQuestItemsCount(ESSENCE_OF_WIND) >= 100))
				{
					htmltext = "31570-06.htm";
					st.giveItems(WIND_FISHING_LURE, 4);
					st.takeItems(ESSENCE_OF_WIND, -1);
					st.playSound("ItemSound.quest_finish");
					st.exitQuest(false);
				}
				break;
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
		
		switch (st.getState())
		{
			case State.COMPLETED:
				htmltext = getAlreadyCompletedMsg(player);
				break;
			case State.CREATED:
				htmltext = (player.getLevel() >= 27) ? "31570-01.htm" : "31570-02.html";
				break;
			case State.STARTED:
				htmltext = (st.getInt("cond") == 2) ? "31570-04.html" : "31570-05.html";
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
		if (st == null)
		{
			return null;
		}
		
		final long count = st.getQuestItemsCount(ESSENCE_OF_WIND);
		if ((st.getInt("cond") == 1) && (count < 100))
		{
			float chance = 33 * Config.RATE_QUEST_DROP;
			float numItems = chance / 100;
			chance = chance % 100;
			
			if (getRandom(100) < chance)
			{
				numItems += 1;
			}
			if (numItems > 0)
			{
				if ((count + numItems) >= 100)
				{
					numItems = 100 - count;
					st.set("cond", "2");
					st.playSound("ItemSound.quest_middle");
				}
				else
				{
					st.playSound("ItemSound.quest_itemget");
				}
				st.giveItems(ESSENCE_OF_WIND, (int) numItems);
			}
		}
		
		return super.onKill(npc, player, isPet);
	}
	
	public Q50_LanoscosSpecialBait(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(LANOSCO);
		addTalkId(LANOSCO);
		addKillId(SINGING_WIND);
	}
	
	public static void main(String[] args)
	{
		new Q50_LanoscosSpecialBait(50, qn, "Lanosco's Special Bait");
	}
}
