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
package quests.Q30_ChestCaughtWithABaitOfFire;

import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;

/**
 * Chest Caught With A Bait Of Fire (30)<br>
 * Original Jython script by Ethernaly
 * @author nonom
 */
public class Q30_ChestCaughtWithABaitOfFire extends Quest
{
	private static final String qn = "30_ChestCaughtWithABaitOfFire";
	
	// NPCs
	private static final int LINNAEUS = 31577;
	private static final int RUKAL = 30629;
	
	// Items
	private static final int RED_TREASURE_BOX = 6511;
	private static final int RUKAL_MUSICAL = 7628;
	private static final int PROTECTION_NECKLACE = 916;
	
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
			case "31577-02.htm":
				st.set("cond", "1");
				st.setState(State.STARTED);
				st.playSound("ItemSound.quest_accept");
				break;
			case "31577-04a.htm":
				if ((st.getInt("cond") == 1) && (st.hasQuestItems(RED_TREASURE_BOX)))
				{
					htmltext = "31577-04.htm";
					st.set("cond", "2");
					st.giveItems(RUKAL_MUSICAL, 1);
					st.takeItems(RED_TREASURE_BOX, -1);
					st.playSound("ItemSound.quest_middle");
				}
				break;
			case "30629-02.htm":
				if ((st.getInt("cond") == 2) && (st.hasQuestItems(RUKAL_MUSICAL)))
				{
					htmltext = "30629-03.htm";
					st.giveItems(PROTECTION_NECKLACE, 1);
					st.takeItems(RUKAL_MUSICAL, -1);
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
				final QuestState qs = player.getQuestState("53_LinnaeusSpecialBait");
				if (npcId == LINNAEUS)
				{
					htmltext = "31577-00.htm";
					if (qs != null)
					{
						htmltext = ((player.getLevel() >= 61) && qs.isCompleted()) ? "31577-01.htm" : htmltext;
					}
				}
				break;
			case State.STARTED:
				final int cond = st.getInt("cond");
				switch (npcId)
				{
					case LINNAEUS:
						switch (cond)
						{
							case 1:
								htmltext = "31577-03a.htm";
								if (st.hasQuestItems(RED_TREASURE_BOX))
								{
									htmltext = "31577-03.htm";
								}
								break;
							case 2:
								htmltext = "31577-05.htm";
								break;
						}
						break;
					case RUKAL:
						if (cond == 2)
						{
							htmltext = "30629-01.htm";
						}
						break;
				}
				break;
		}
		return htmltext;
	}
	
	public Q30_ChestCaughtWithABaitOfFire(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(LINNAEUS);
		addTalkId(LINNAEUS, RUKAL);
	}
	
	public static void main(String[] args)
	{
		new Q30_ChestCaughtWithABaitOfFire(30, qn, "Chest Caught With A Bait Of Fire");
	}
}
