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
package quests.Q903_TheCallOfAntharas;

import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;

public class Q903_TheCallOfAntharas extends Quest
{
	private static final String qn = "Q903_TheCallOfAntharas";
	// NPC
	private static final int Theodric = 30755;
	// Monster
	private static final int Tarask_Dragon = 29190;
	private static final int Behemoth_Dragon = 29069;
	// Needed Item
	private static final int Portal_stone = 3865;
	// Quest Items
	private static final int Tarask_Dragon_Leather = 21991;
	private static final int Behemot_Dragon_Leather = 21992;	
	// Reward Scroll:Antharas Call
	private static final int Scroll = 21897;
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		int npcId = npc.getNpcId();

		if (st == null)
			return htmltext;
			
		if (npcId == Theodric)
		{
			if (event.equalsIgnoreCase("accept"))
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.playSound("ItemSound.quest_accept");
				htmltext = "TheCallOfAntharas-04.htm";
			}
			if (event.equalsIgnoreCase("reward"))
			{
				st.playSound("ItemSound.quest_finish");
				st.takeItems(Tarask_Dragon_Leather,1);
				st.takeItems(Behemot_Dragon_Leather,1);
				st.giveItems(Scroll,1);
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
		if(npcId == Theodric)
		{
			switch(st.getState())
			{		
				case State.CREATED :
					if (player.getLevel() >= 83 && st.getQuestItemsCount(Portal_stone) >= 1)
						htmltext = "TheCallOfAntharas-02.htm"; // Quest begining
					else if (player.getLevel() < 83)
						htmltext = "TheCallOfAntharas-01.htm"; // Lvl
					else
						htmltext = "TheCallOfAntharas-00.htm"; // No Portal Stone
					break;
				case State.STARTED :
						if(st.getInt("cond") == 1)
						{
							htmltext = "TheCallOfAntharas-05.htm";
						}
						if(st.getInt("cond") == 2)
						{
							htmltext = "TheCallOfAntharas-07.htm"; //Collected
						}
						else
							htmltext = "TheCallOfAntharas-06.htm"; //Not Enough Collected Items
						break;
				case State.COMPLETED :
						htmltext = "TheCallOfAntharas-no.htm";					
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
			if(npcId == Tarask_Dragon)
			{	
				if(st.hasQuestItems(Behemot_Dragon_Leather))
				{
					st.giveItems(Tarask_Dragon_Leather, 1);
					st.playSound("ItemSound.quest_itemget");					
					st.playSound("ItemSound.quest_middle");
					st.set("cond","2");				
				}
				else
				{
					st.giveItems(Tarask_Dragon_Leather, 1);
					st.playSound("ItemSound.quest_itemget");					
				}
		
			}
			else if (npcId == Behemoth_Dragon)
			{
				if (st.hasQuestItems(Tarask_Dragon_Leather))
				{
					st.giveItems(Behemot_Dragon_Leather, 1);
					st.playSound("ItemSound.quest_itemget");					
					st.playSound("ItemSound.quest_middle");
					st.set("cond","2");
				}
				else
				{
					st.giveItems(Behemot_Dragon_Leather, 1);
					st.playSound("ItemSound.quest_itemget");					
				}
			}				
		}	
		return super.onKill(npc, player, isPet);
	}
	
	public Q903_TheCallOfAntharas(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(Theodric);
		addTalkId(Theodric);
		addKillId(Tarask_Dragon);
		addKillId(Behemoth_Dragon);
	}

	public static void main(String[] args)
	{
		new Q903_TheCallOfAntharas(903, qn, "The Call Of Antharas");
	}
}
