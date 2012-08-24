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
package quests.Q14_WhereaboutsOfTheArchaeologist;

import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;

/**
 * Where abouts Of The Archaeologist (14).<br>
 * Original Jython script by disKret.
 * @author nonom
 */
public class Q14_WhereaboutsOfTheArchaeologist extends Quest
{
	private static final String qn = "14_WhereaboutsOfTheArchaeologist";
	
	// NPCs
	private static final int LIESEL = 31263;
	private static final int GHOST_OF_ADVENTURER = 31538;
	
	// Items
	private static final int LETTER = 7253;
	
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
			case "31263-02.html":
				st.set("cond", "1");
				st.setState(State.STARTED);
				st.giveItems(LETTER, 1);
				st.playSound("ItemSound.quest_accept");
				break;
			case "31538-01.html":
				if ((st.getInt("cond") == 1) && st.hasQuestItems(LETTER))
				{
					st.takeItems(LETTER, -1);
					st.giveItems(57, 136928);
					st.addExpAndSp(325881, 32524);
					st.playSound("ItemSound.quest_finish");
					st.exitQuest(false);
				}
				else
				{
					htmltext = "31538-02.html";
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
				if (npcId == LIESEL)
				{
					htmltext = (player.getLevel() < 74) ? "31263-01.html" : "31263-00.htm";
				}
				break;
			case State.STARTED:
				if (st.getInt("cond") == 1)
				{
					switch (npcId)
					{
						case LIESEL:
							htmltext = "31263-02.html";
							break;
						case GHOST_OF_ADVENTURER:
							htmltext = "31538-00.html";
							break;
					}
				}
				break;
		}
		return htmltext;
	}
	
	public Q14_WhereaboutsOfTheArchaeologist(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(LIESEL);
		addTalkId(LIESEL);
		addTalkId(GHOST_OF_ADVENTURER);
	}
	
	public static void main(String[] args)
	{
		new Q14_WhereaboutsOfTheArchaeologist(14, qn, "Whereabouts Of The Archaeologist");
	}
}
