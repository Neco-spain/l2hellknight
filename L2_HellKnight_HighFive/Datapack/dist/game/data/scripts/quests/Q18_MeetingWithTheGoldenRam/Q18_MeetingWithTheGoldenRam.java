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
package quests.Q18_MeetingWithTheGoldenRam;

import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;

/**
 * Meeting With The Golden Ram (18).<br>
 * Original jython script by disKret.
 * @author nonom
 */
public class Q18_MeetingWithTheGoldenRam extends Quest
{
	private static final String qn = "18_MeetingWithTheGoldenRam";
	
	// NPCs
	private static final int DONAL = 31314;
	private static final int DAISY = 31315;
	private static final int ABERCROMBIE = 31555;
	
	// Items
	private static final int BOX = 7245;
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		final int npcId = npc.getNpcId();
		switch (st.getState())
		{
			case State.COMPLETED:
				htmltext = getAlreadyCompletedMsg(player);
				break;
			case State.CREATED:
				if (npcId == DONAL)
				{
					htmltext = "31314-01.htm";
				}
				break;
			case State.STARTED:
				final int cond = st.getInt("cond");
				if (npcId == DONAL)
				{
					htmltext = "31314-04.html";
				}
				else if (npcId == DAISY)
				{
					htmltext = (cond < 2) ? "31315-01.html" : "31315-03.html";
				}
				else if ((npcId == ABERCROMBIE) && (cond == 2) && st.hasQuestItems(BOX))
				{
					htmltext = "31555-01.html";
				}
				break;
		}
		return htmltext;
	}
	
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
			case "31314-03.html":
				if (player.getLevel() >= 66)
				{
					st.set("cond", "1");
					st.setState(State.STARTED);
					st.playSound("ItemSound.quest_accept");
				}
				else
				{
					htmltext = "31314-02.html";
				}
				break;
			case "31315-02.html":
				st.set("cond", "2");
				st.giveItems(BOX, 1);
				break;
			case "31555-02.html":
				if (st.hasQuestItems(BOX))
				{
					st.giveAdena(40000, true);
					st.takeItems(BOX, -1);
					st.addExpAndSp(126668, 11731);
					st.playSound("ItemSound.quest_finish");
					st.exitQuest(false);
				}
				break;
		}
		return htmltext;
	}
	
	public Q18_MeetingWithTheGoldenRam(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(DONAL);
		
		addTalkId(DONAL, DAISY, ABERCROMBIE);
	}
	
	public static void main(String[] args)
	{
		new Q18_MeetingWithTheGoldenRam(18, qn, "Meeting With The Golden Ram");
	}
}
