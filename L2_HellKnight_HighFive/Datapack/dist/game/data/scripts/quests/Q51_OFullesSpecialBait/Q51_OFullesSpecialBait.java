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
package quests.Q51_OFullesSpecialBait;

import l2.hellknight.Config;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;

/**
 * O'Fulle's Special Bait (51)<br>
 * Original Jython script by Kilkenny
 * @author nonom
 */
public class Q51_OFullesSpecialBait extends Quest
{
	private static final String qn = "51_OFullesSpecialBait";
	
	// NPCs
	private static final int OFULLE = 31572;
	private static final int FETTERED_SOUL = 20552;
	
	// Items
	private static final int LOST_BAIT = 7622;
	private static final int ICY_AIR_LURE = 7611;
	
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
			case "31572-03.htm":
				st.set("cond", "1");
				st.setState(State.STARTED);
				st.playSound("ItemSound.quest_accept");
				break;
			case "31572-07.html":
				if ((st.getInt("cond") == 2) && (st.getQuestItemsCount(LOST_BAIT) >= 100))
				{
					htmltext = "31572-06.htm";
					st.giveItems(ICY_AIR_LURE, 4);
					st.takeItems(LOST_BAIT, -1);
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
				htmltext = (player.getLevel() >= 36) ? "31572-01.htm" : "31572-02.html";
				break;
			case State.STARTED:
				htmltext = (st.getInt("cond") == 1) ? "31572-04.html" : "31572-05.html";
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
		
		final long count = st.getQuestItemsCount(LOST_BAIT);
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
				}
				st.set("cond", "2");
				st.playSound("ItemSound.quest_middle");
			}
			else
			{
				st.playSound("ItemSound.quest_itemget");
			}
			st.giveItems(LOST_BAIT, (int) numItems);
		}
		
		return super.onKill(npc, player, isPet);
	}
	
	public Q51_OFullesSpecialBait(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(OFULLE);
		addTalkId(OFULLE);
		addKillId(FETTERED_SOUL);
	}
	
	public static void main(String[] args)
	{
		new Q51_OFullesSpecialBait(51, qn, "O'Fulle's Special Bait");
	}
}
