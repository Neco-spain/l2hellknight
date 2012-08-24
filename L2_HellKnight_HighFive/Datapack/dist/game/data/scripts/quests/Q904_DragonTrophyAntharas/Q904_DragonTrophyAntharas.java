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
package quests.Q904_DragonTrophyAntharas;

import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;

public class Q904_DragonTrophyAntharas extends Quest
{
	private static final String qn = "Q904_DragonTrophyAntharas";
	
	private static final int Theodric = 30755;
	private static final int AntharasMax = 29068;
	private static final int MedalofGlory = 21874;

	public String onEvent(String event, QuestState st, L2Npc npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("30755-04.htm"))
		{
			st.setState(State.STARTED);
			//st.setCond(1);
			//st.playSound(SOUND_ACCEPT);
			st.set("cond", "1");
			st.playSound("ItemSound.quest_accept");
		}
		else if(event.equalsIgnoreCase("30755-07.htm"))
		{
			st.giveItems(MedalofGlory, 30);
			st.setState(State.COMPLETED);
			//st.playSound(SOUND_FINISH);
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(true);
		}

		return htmltext;
	}

	public String onTalk(L2Npc npc, QuestState st)
	{
		String htmltext = "noquest";
		int cond = st.getInt("cond");
		if(npc.getNpcId() == Theodric)
		{
			switch(st.getState())
			{
				case State.CREATED:
					if(st.getPlayer().getLevel() >= 84)
					{
						if(st.getQuestItemsCount(3865) > 0)
							htmltext = "30755-01.htm";
						else
							htmltext = "30755-00b.htm";
					}
					else
					{
						htmltext = "30755-00.htm";
						st.exitQuest(true);
					}
					break;
				case State.STARTED:
					if(cond == 1)
						htmltext = "30755-05.htm";
					else if(cond == 2)
						htmltext = "30755-06.htm";
					break;
			}
		}

		return htmltext;
	}

	public String onKill(L2Npc npc, QuestState st)
	{
		int cond = st.getInt("cond");
		if(cond == 1)
		{
			if(npc.getNpcId() == AntharasMax)
				st.set("cond","2");	
		}
		return null;
	}
	
	public Q904_DragonTrophyAntharas(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(Theodric);
		addKillId(AntharasMax);
	}
	
	public static void main(String[] args)
	{
		new Q904_DragonTrophyAntharas(904, qn, "Dragon Trophy Antharas");
	}
	
}