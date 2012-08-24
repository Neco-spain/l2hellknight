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
package quests.Q10504_JewelOfAntharas;

import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;

public class Q10504_JewelOfAntharas extends Quest
{
	private static final String qn = "Q10504_JewelOfAntharas";

	// NPC's
	private static final int THEODRICK = 30755;
	 private static final int[] ANTHARAS = { 29019, 29066, 29067, 29068 };

	 // Item's
	private static final int EMPTY_CRYSTAL = 21905; //ok
	private static final int FILLED_CRYSTAL_ANTHARAS = 21907; //ok
	private static final int PORTAL_STONE = 3865;  //ok
	private static final int JEWEL_OF_ANTHARAS = 21898; //ok

	
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
			case "30755-04.htm":
				st.set("cond", "1");
				st.setState(State.STARTED);
				st.playSound("ItemSound.quest_accept");
				st.giveItems(EMPTY_CRYSTAL, 1);
				break;
			case "30755-07.htm":
				st.takeItems(FILLED_CRYSTAL_ANTHARAS,1);
				st.giveItems(JEWEL_OF_ANTHARAS, 1);
				st.playSound("ItemSound.quest_finish");
				st.setState(State.COMPLETED);
				st.exitQuest(false);
				break;
			case "30755-08.htm":
				st.giveItems(EMPTY_CRYSTAL, 1);
				break;
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);

		if (st == null)
		{
			return htmltext;
		}
		
		if(npc.getNpcId() == THEODRICK)
		{
			switch (st.getInt("cond"))
			{
			case 0:
				if(st.getPlayer().getLevel() < 84)
					htmltext = "30755-00.htm";
				else if(st.getQuestItemsCount(PORTAL_STONE) < 1)
					htmltext = "30755-00a.htm";
				else if(st.isNowAvailable())
					htmltext = "30755-01.htm";
				else
					htmltext = "30755-09.htm";
				break;
			case 1:
				if(st.getQuestItemsCount(EMPTY_CRYSTAL) < 1)
					htmltext = "30755-08.htm";
				else
					htmltext = "30755-05.htm";
				break;
			case 2:
				if(st.getQuestItemsCount(FILLED_CRYSTAL_ANTHARAS) >= 1)
					htmltext = "30755-07.htm";
				else
					htmltext = "30755-06.htm";
				break;
			}
		}
		return htmltext;
	}

	public String onKill(L2Npc npc, QuestState st)
	{
		if((st.getInt("cond") == 1) && (npc.getNpcId() == ANTHARAS[0] || npc.getNpcId() == ANTHARAS[1] || npc.getNpcId() == ANTHARAS[2] || npc.getNpcId() == ANTHARAS[3]))
		{
			st.takeItems(EMPTY_CRYSTAL,1);
			st.giveItems(FILLED_CRYSTAL_ANTHARAS, 1);
			st.set("cond", "2");
		}
		return null;
	}
	public Q10504_JewelOfAntharas(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(THEODRICK);
		addTalkId(THEODRICK, EMPTY_CRYSTAL, FILLED_CRYSTAL_ANTHARAS);
		addKillId(ANTHARAS);
	}
	
	public static void main(String[] args)
	{
		new Q10504_JewelOfAntharas(10504, qn, "Jewel Of Antharas");
	}
}