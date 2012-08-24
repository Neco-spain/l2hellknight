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
package quests.Q137_TempleChampionPart1;

import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;

/**
 * Temple Champion Part 1 (137)
 * @author nonom
 */
public class Q137_TempleChampionPart1 extends Quest
{
	private static final String qn = "137_TempleChampionPart1";
	
	// NPCs
	private static final int SYLVAIN = 30070;
	
	private static final int MOBS[] =
	{
		20083, // Granite Golem
		20144, // Hangman Tree
		20199, // Amber Basilisk
		20200, // Strain
		20201, // Ghoul
		20202, // Dead Seeker
	};
	
	// Items
	private static final int FRAGMENT = 10340;
	private static final int EXECUTOR = 10334;
	private static final int MISSIONARY = 10339;
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return getNoQuestMsg(player);
		}
		switch (event)
		{
			case "30070-02.htm":
				st.startQuest();
				break;
			case "30070-05.html":
				st.set("talk", "1");
				break;
			case "30070-06.html":
				st.set("talk", "2");
				break;
			case "30070-08.html":
				st.unset("talk");
				st.setCond(2, true);
				break;
			case "30070-16.html":
				if (st.isCond(2) && (st.hasQuestItems(EXECUTOR) && st.hasQuestItems(MISSIONARY)))
				{
					st.takeItems(EXECUTOR, -1);
					st.takeItems(MISSIONARY, -1);
					st.giveAdena(69146, true);
					if (player.getLevel() < 41)
					{
						st.addExpAndSp(219975, 13047);
					}
					st.exitQuest(false, true);
				}
				break;
		}
		return event;
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
		if (st.isCompleted())
		{
			return getAlreadyCompletedMsg(player);
		}
		switch (st.getInt("cond"))
		{
			case 1:
				switch (st.getInt("talk"))
				{
					case 1:
						htmltext = "30070-05.html";
						break;
					case 2:
						htmltext = "30070-06.html";
						break;
					default:
						htmltext = "30070-03.html";
						break;
				}
				break;
			case 2:
				htmltext = "30070-08.html";
				break;
			case 3:
				if (st.getInt("talk") == 1)
				{
					htmltext = "30070-10.html";
				}
				else if (st.getQuestItemsCount(FRAGMENT) >= 30)
				{
					st.set("talk", "1");
					htmltext = "30070-09.html";
					st.takeItems(FRAGMENT, -1);
				}
				break;
			default:
				htmltext = ((player.getLevel() >= 35) && st.hasQuestItems(EXECUTOR) && st.hasQuestItems(MISSIONARY)) ? "30070-01.htm" : "30070-00.html";
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		final QuestState st = player.getQuestState(qn);
		if ((st != null) && st.isStarted() && st.isCond(2) && (st.getQuestItemsCount(FRAGMENT) < 30))
		{
			st.giveItems(FRAGMENT, 1);
			if (st.getQuestItemsCount(FRAGMENT) >= 30)
			{
				st.setCond(3, true);
			}
			else
			{
				st.playSound("ItemSound.quest_itemget");
			}
		}
		return super.onKill(npc, player, isPet);
	}
	
	public Q137_TempleChampionPart1(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(SYLVAIN);
		addTalkId(SYLVAIN);
		addKillId(MOBS);
		questItemIds = new int[]
		{
			FRAGMENT
		};
	}
	
	public static void main(String[] args)
	{
		new Q137_TempleChampionPart1(137, qn, "Temple Champion Part 1");
	}
}
