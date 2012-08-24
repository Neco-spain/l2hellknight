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
package quests.Q110_ToThePrimevalIsle;

import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;

/**
 * To the Primeval Isle (110)<br>
 * Original Jython script by Ethernaly, updated by Zoey76.
 * @author Adry_85
 */
public class Q110_ToThePrimevalIsle extends Quest
{
	private static final String qn = "110_ToThePrimevalIsle";
	
	// NPC
	private static final int ANTON = 31338;
	private static final int MARQUEZ = 32113;
	
	// Quest Item
	private static final int ANCIENT_BOOK = 8777;
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return getNoQuestMsg(player);
		}
		
		switch (event)
		{
			case "31338-1.html":
				st.giveItems(ANCIENT_BOOK, 1);
				st.startQuest();
				break;
			case "32113-2.html":
			case "32113-2a.html":
				st.takeItems(ANCIENT_BOOK, -1);
				st.giveAdena(191678, true);
				st.addExpAndSp(251602, 25245);
				st.exitQuest(false, true);
				break;
		}
		return event;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		switch (npc.getNpcId())
		{
			case ANTON:
				switch (st.getState())
				{
					case State.CREATED:
						htmltext = (player.getLevel() < 75) ? "31338-0a.htm" : "31338-0b.htm";
						break;
					case State.STARTED:
						htmltext = "31338-1a.html";
						break;
					case State.COMPLETED:
						htmltext = getAlreadyCompletedMsg(player);
						break;
				}
				break;
			case MARQUEZ:
				if (st.isCond(1))
				{
					htmltext = "32113-1.html";
				}
				break;
		}
		return htmltext;
	}
	
	public Q110_ToThePrimevalIsle(int id, String name, String descr)
	{
		super(id, name, descr);
		
		addStartNpc(ANTON);
		addTalkId(ANTON, MARQUEZ);
	}
	
	public static void main(String[] args)
	{
		new Q110_ToThePrimevalIsle(110, qn, "To the Primeval Isle");
	}
}
