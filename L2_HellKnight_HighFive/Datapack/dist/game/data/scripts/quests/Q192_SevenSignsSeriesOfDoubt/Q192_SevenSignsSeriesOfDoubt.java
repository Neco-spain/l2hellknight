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
package quests.Q192_SevenSignsSeriesOfDoubt;

import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;

/**
 * @author Plim
 */

public class Q192_SevenSignsSeriesOfDoubt extends Quest
{
	private static final String qn = "192_SevenSignsSeriesOfDoubt";
	
	//NPCs
	private static final int CROOP = 30676;
	private static final int HECTOR = 30197;
	private static final int STAN = 30200;
	private static final int CORPSE = 32568;
	private static final int HOLLINT = 30191;
	
	//ITEMS
	private static final int CROOP_INTRO = 13813;
	private static final int JACOB_NECK = 13814;
	private static final int CROOP_LETTER = 13815;
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		
		if (st == null)
			return htmltext;
		
		if (npc.getNpcId() == CROOP)
		{
			if (event.equalsIgnoreCase("30676-03.htm"))
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.playSound("ItemSound.quest_accept");
			}
			
			else if (event.equals("8"))
			{
				st.set("cond", "2");
				st.playSound("ItemSound.quest_middle");
				player.showQuestMovie(8);
				startQuestTimer("playertele", 32000, npc, player);
				return "";
			}
			
			else if (event.equalsIgnoreCase("playertele"))
			{
				player.teleToLocation(81654,54848,-1514);
				return "";
			}
			
			else if (event.equalsIgnoreCase("30676-12.htm"))
			{
				st.set("cond", "7");
				st.takeItems(JACOB_NECK, 1);
				st.giveItems(CROOP_LETTER, 1);
				st.playSound("ItemSound.quest_middle");
			}
		}
		
		else if (npc.getNpcId() == HECTOR)
		{
			if (event.equalsIgnoreCase("30197-03.htm"))
			{
				st.set("cond", "4");
				st.takeItems(CROOP_INTRO, 1);
				st.playSound("ItemSound.quest_middle");
			}
		}
		
		else if (npc.getNpcId() == STAN)
		{
			if (event.equalsIgnoreCase("30200-04.htm"))
			{
				st.set("cond", "5");
				st.playSound("ItemSound.quest_middle");
			}
		}
		
		else if (npc.getNpcId() == CORPSE)
		{
			if (event.equalsIgnoreCase("32568-02.htm"))
			{
				st.set("cond", "6");
				st.giveItems(JACOB_NECK, 1);
				st.playSound("ItemSound.quest_middle");
			}
		}
		
		else if (npc.getNpcId() == HOLLINT)
		{
			if (event.equalsIgnoreCase("30191-03.htm"))
			{
				if (st.getQuestItemsCount(CROOP_LETTER) >= 1 && player.getLevel() >= 79)
				{
					st.addExpAndSp(25000000, 2500000);
					st.takeItems(CROOP_LETTER, 1);
					st.unset("cond");
					st.setState(State.COMPLETED);
					st.exitQuest(false);
					st.playSound("ItemSound.quest_finish");
				}
				else if(st.getQuestItemsCount(CROOP_LETTER) >= 1 && player.getLevel() < 79)
					htmltext = "30191-00.htm";
					
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
		
		if (npc.getNpcId() == CROOP)
		{
			switch (st.getState())
			{
				case State.CREATED:
					if (st.getState() == State.CREATED && player.getLevel() >= 79)
						htmltext = "30676-01.htm";
					else
					{
						htmltext = "30676-00.htm";
						st.exitQuest(true);
					}
					break;
				case State.STARTED:
					if (st.getInt("cond") == 1)
						htmltext = "30676-04.htm";
					else if (st.getInt("cond") == 2)
					{
						htmltext = "30676-05.htm";
						st.set("cond", "3");
						st.playSound("ItemSound.quest_middle");
						st.giveItems(CROOP_INTRO, 1);
					}
					else if (st.getInt("cond") >= 3 && st.getInt("cond") <= 5)
						htmltext = "30676-06.htm";
					else if (st.getInt("cond") == 6)
						htmltext = "30676-07.htm";
					break;
				case State.COMPLETED:
					htmltext = "30676-13.htm";
			}
		}
		
		else if (npc.getNpcId() == HECTOR)
		{
			if (st.getState() == State.STARTED)
			{
				if (st.getInt("cond") == 3)
					htmltext = "30197-01.htm";
				else if (st.getInt("cond") >= 4 && st.getInt("cond") <= 7)
					htmltext = "30197-04.htm";
			}
		}
		
		else if (npc.getNpcId() == STAN)
		{
			if (st.getInt("cond") == 4)
				htmltext = "30200-01.htm";
			else if (st.getInt("cond") >= 5 && st.getInt("cond") <= 7)
				htmltext = "30200-05.htm";
		}
		
		else if (npc.getNpcId() == CORPSE)
		{
			if (st.getInt("cond") == 0)
				htmltext = "32568-04.htm";
			else if (st.getInt("cond") == 5)
				htmltext = "32568-01.htm";
			else if (st.getInt("cond") < 5)
				htmltext = "32568-03.htm";
		}
		
		else if (npc.getNpcId() == HOLLINT)
		{
			if (st.getInt("cond") == 7)
				htmltext = "30191-01.htm";
		}
		
		return htmltext;
	}
	
	public Q192_SevenSignsSeriesOfDoubt(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(CROOP);
		addTalkId(CROOP);
		addTalkId(HECTOR);
		addTalkId(STAN);
		addTalkId(CORPSE);
		addTalkId(HOLLINT);
	}
	
	public static void main(String[] args)
	{
		new Q192_SevenSignsSeriesOfDoubt(192, qn, "Seven Signs, Series of Doubt");
	}
}
