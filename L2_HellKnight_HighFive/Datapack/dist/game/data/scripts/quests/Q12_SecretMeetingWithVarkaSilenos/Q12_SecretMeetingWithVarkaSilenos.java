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
package quests.Q12_SecretMeetingWithVarkaSilenos;

import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;

/**
 * Secret Meeting With Varka Silenos (12).<br>
 * Original Jython script by Emperorc.
 * @author nonom
 */
public class Q12_SecretMeetingWithVarkaSilenos extends Quest
{
	private static final String qn = "12_SecretMeetingWithVarkaSilenos";
	
	// NPCs
	private static final int CADMON = 31296;
	private static final int HELMUT = 31258;
	private static final int NARAN = 31378;
	
	// Items
	private static final int BOX = 7232;
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		final QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		final int cond = st.getInt("cond");
		switch (event)
		{
			case "31296-03.html":
				st.set("cond", "1");
				st.setState(State.STARTED);
				st.playSound("ItemSound.quest_accept");
				break;
			case "31258-02.html":
				if (cond == 1)
				{
					st.set("cond", "2");
					st.giveItems(BOX, 1);
					st.playSound("ItemSound.quest_middle");
				}
				break;
			case "31378-02.html":
				if ((cond == 2) && (st.hasQuestItems(BOX)))
				{
					st.takeItems(BOX, -1);
					st.addExpAndSp(233125, 18142);
					st.playSound("ItemSound.quest_finish");
					st.exitQuest(false);
				}
				else
				{
					htmltext = "31378-03.html";
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
		
		final int npcId = npc.getNpcId();
		switch (st.getState())
		{
			case State.COMPLETED:
				htmltext = getAlreadyCompletedMsg(player);
				break;
			case State.CREATED:
				if (npcId == CADMON)
				{
					htmltext = (player.getLevel() >= 74) ? "31296-01.htm" : "31296-02.html";
				}
				break;
			case State.STARTED:
				final int cond = st.getInt("cond");
				if ((npcId == CADMON) && (cond == 1))
				{
					htmltext = "31296-04.html";
				}
				else if (npcId == HELMUT)
				{
					if (cond == 1)
					{
						htmltext = "31258-01.html";
					}
					else if (cond == 2)
					{
						htmltext = "31258-03.html";
					}
				}
				else if ((npcId == NARAN) && (cond == 2))
				{
					htmltext = "31378-01.html";
				}
				break;
		}
		return htmltext;
	}
	
	public Q12_SecretMeetingWithVarkaSilenos(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(CADMON);
		
		addTalkId(CADMON, HELMUT, NARAN);
	}
	
	public static void main(String[] args)
	{
		new Q12_SecretMeetingWithVarkaSilenos(12, qn, "Secret Meeting With Varka Silenos");
	}
}
