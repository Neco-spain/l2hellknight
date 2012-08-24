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
package quests.Q29_ChestCaughtWithABaitOfEarth;

import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;

/**
 * Chest Caught With A Bait Of Earth (29)<br>
 * Original Jython script by Skeleton
 * @author nonom
 */
public class Q29_ChestCaughtWithABaitOfEarth extends Quest
{
	private static final String qn = "29_ChestCaughtWithABaitOfEarth";
	
	// NPCs
	private static final int WILLIE = 31574;
	private static final int ANABEL = 30909;
	
	// Items
	private static final int PURPLE_TREASURE_BOX = 6507;
	private static final int SMALL_GLASS_BOX = 7627;
	private static final int PLATED_LEATHER_GLOVES = 2455;
	
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
			case "31574-04.htm":
				st.set("cond", "1");
				st.setState(State.STARTED);
				st.playSound("ItemSound.quest_accept");
				break;
			case "31574-08.htm":
				if ((st.getInt("cond") == 1) && (st.hasQuestItems(PURPLE_TREASURE_BOX)))
				{
					htmltext = "31574-07.htm";
					st.set("cond", "2");
					st.giveItems(SMALL_GLASS_BOX, 1);
					st.takeItems(PURPLE_TREASURE_BOX, -1);
					st.playSound("ItemSound.quest_middle");
				}
				break;
			case "30909-03.htm":
				if ((st.getInt("cond") == 2) && (st.hasQuestItems(SMALL_GLASS_BOX)))
				{
					htmltext = "30909-02.htm";
					st.giveItems(PLATED_LEATHER_GLOVES, 1);
					st.takeItems(SMALL_GLASS_BOX, -1);
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
				final QuestState qs = player.getQuestState("52_WilliesSpecialBait");
				if (npcId == WILLIE)
				{
					htmltext = "31574-02.htm";
					if (qs != null)
					{
						htmltext = ((player.getLevel() >= 48) && qs.isCompleted()) ? "31574-01.htm" : htmltext;
					}
				}
				break;
			case State.STARTED:
				final int cond = st.getInt("cond");
				switch (npcId)
				{
					case WILLIE:
						switch (cond)
						{
							case 1:
								htmltext = "31574-06.htm";
								if (st.hasQuestItems(PURPLE_TREASURE_BOX))
								{
									htmltext = "31574-05.htm";
								}
								break;
							case 2:
								htmltext = "31574-09.htm";
								break;
						}
						break;
					case ANABEL:
						if (cond == 2)
						{
							htmltext = "30909-01.htm";
						}
						break;
				}
				break;
		}
		return htmltext;
	}
	
	public Q29_ChestCaughtWithABaitOfEarth(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(WILLIE);
		addTalkId(WILLIE, ANABEL);
	}
	
	public static void main(String[] args)
	{
		new Q29_ChestCaughtWithABaitOfEarth(29, qn, "Chest Caught With A Bait Of Earth");
	}
}
