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
package quests.Q15_SweetWhispers;

import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;

/**
 * Sweet Whisper (15).<br>
 * Original jython script by disKret.
 * @author nonom
 */
public class Q15_SweetWhispers extends Quest
{
	private static final String qn = "15_SweetWhispers";
	
	// NPCs
	private static final int VLADIMIR = 31302;
	private static final int HIERARCH = 31517;
	private static final int M_NECROMANCER = 31518;
	
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
			case "31302-01.html":
				st.set("cond", "1");
				st.setState(State.STARTED);
				st.playSound("ItemSound.quest_accept");
				break;
			case "31518-01.html":
				if (cond == 1)
				{
					st.set("cond", "2");
					st.playSound("ItemSound.quest_middle");
				}
				break;
			case "31517-01.html":
				if (cond == 2)
				{
					st.addExpAndSp(350531, 28204);
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
		
		final int npcId = npc.getNpcId();
		switch (st.getState())
		{
			case State.COMPLETED:
				htmltext = getAlreadyCompletedMsg(player);
				break;
			case State.CREATED:
				if (npcId == VLADIMIR)
				{
					htmltext = (player.getLevel() >= 60) ? "31302-00.htm" : "31302-00a.html";
				}
				break;
			case State.STARTED:
				final int cond = st.getInt("cond");
				switch (npcId)
				{
					case VLADIMIR:
						if (cond == 1)
						{
							htmltext = "31302-01a.html";
						}
						break;
					case M_NECROMANCER:
						switch (cond)
						{
							case 1:
								htmltext = "31518-00.html";
								break;
							case 2:
								htmltext = "31518-01a.html";
								break;
						}
						break;
					case HIERARCH:
						if (cond == 2)
						{
							htmltext = "31517-00.html";
						}
						break;
				}
				break;
		}
		return htmltext;
	}
	
	public Q15_SweetWhispers(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(VLADIMIR);
		
		addTalkId(VLADIMIR, HIERARCH, M_NECROMANCER);
	}
	
	public static void main(String[] args)
	{
		new Q15_SweetWhispers(15, qn, "Sweet Whispers");
	}
}
