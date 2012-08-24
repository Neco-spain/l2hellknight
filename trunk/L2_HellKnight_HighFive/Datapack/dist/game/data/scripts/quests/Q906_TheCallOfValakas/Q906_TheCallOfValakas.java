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
package quests.Q906_TheCallOfValakas;

import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;

public class Q906_TheCallOfValakas extends Quest
{
	private static final String qn = "906_TheCallOfValakas";
	// NPC
	private static final int Klein = 31540;
	// Monster
	private static final int Lavasaurus_Alpha = 29029;
	// Needed Item
	private static final int Floating_Stone = 7267;
	// Quest Items
	private static final int Lavasaurus_Alpha_Fragment = 21993;	
	// Reward Scroll:Valakas Call
	private static final int Scroll = 21895;
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		int npcId = npc.getNpcId();

		if (st == null)
			return htmltext;
			
		if (npcId == Klein)
		{
			if (event.equalsIgnoreCase("accept"))
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.playSound("ItemSound.quest_accept");
				htmltext = "TheCallOfValakas-05.htm";
			}
			if (event.equalsIgnoreCase("reward"))
			{
				st.playSound("ItemSound.quest_finish");
				st.takeItems(Lavasaurus_Alpha_Fragment, 1);
				st.giveItems(Scroll, 1);
				st.unset("cond");
				st.exitQuest(false);		
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		int npcId = npc.getNpcId();
		if (st == null)
			return htmltext;
		if(npcId == Klein)
		{
			switch(st.getState())
			{		
				case State.CREATED :
					if (player.getLevel() >= 83 && st.getQuestItemsCount(Floating_Stone) >= 1)
						htmltext = "TheCallOfValakas-02.htm"; // Quest begining
					else if (player.getLevel() < 83)
						htmltext = "TheCallOfValakas-01.htm"; // Lvl
					else
						htmltext = "TheCallOfValakas-00.htm"; // No Floating Stone
					break;
				case State.STARTED :
						if(st.getInt("cond") == 1)
							htmltext = "TheCallOfValakas-06.htm";
						if(st.getInt("cond") == 2)
							htmltext = "TheCallOfValakas-07.htm"; //Collected
						break;
				case State.COMPLETED :
						htmltext = "TheCallOfValakas-08.htm";					
				break;
			}
		}
		
		return htmltext;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{	
		QuestState st = player.getQuestState(qn);
		int npcId = npc.getNpcId();
		if(st.getInt("cond") == 1)
		{
			if(npcId == Lavasaurus_Alpha)
			{	
				st.giveItems(Lavasaurus_Alpha_Fragment, 1);
				st.playSound("ItemSound.quest_itemget");					
				st.playSound("ItemSound.quest_middle");
				st.set("cond","2");				
			}				
		}	
		return super.onKill(npc, player, isPet);
	}
	
	public Q906_TheCallOfValakas(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(Klein);
		addTalkId(Klein);
		addKillId(Lavasaurus_Alpha);
	}

	public static void main(String[] args)
	{
		new Q906_TheCallOfValakas(906, qn, "The Call Of Valakas");
	}
}
