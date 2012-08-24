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
package quests.Q251_NoSecrets;

import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;
import l2.hellknight.gameserver.util.Util;

/**
 * No Secrets (251)
 * @author Dumpster
 */
public class Q251_NoSecrets extends Quest
{
	public static final int PINAPS = 30201;
	public static final int DIARY = 15508;
	public static final int TABLE = 15509;
	
	public static final String qn = "251_NoSecrets";
	
	private static final int[] MOBS =
	{
		22783,
		22785,
		22780,
		22782,
		22784
	};
	
	private static final int[] MOBS2 =
	{
		22775,
		22776,
		22778
	};
	
	public Q251_NoSecrets(int id, String name, String descr)
	{
		super(id, name, descr);
		addStartNpc(PINAPS);
		addTalkId(PINAPS);
		addKillId(MOBS);
		addKillId(MOBS2);
		questItemIds = new int[]
		{
			DIARY,
			TABLE
		};
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return getNoQuestMsg(player);
		}
		
		if (event.equals("30201-03.htm"))
		{
			st.startQuest();
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
		
		switch (st.getState())
		{
			case State.CREATED:
				htmltext = (player.getLevel() > 81) ? "30201-01.htm" : "30201-00.htm";
				break;
			case State.STARTED:
				int cond = st.getInt("cond");
				if (cond == 1)
				{
					htmltext = "30201-05.htm";
				}
				else if ((cond == 2) && (st.getQuestItemsCount(DIARY) >= 10) && (st.getQuestItemsCount(TABLE) >= 5))
				{
					htmltext = "30201-04.htm";
					st.rewardItems(57, 313355);
					st.addExpAndSp(56787, 160578);
					st.exitQuest(false, true);
				}
				break;
			case State.COMPLETED:
				htmltext = "30201-06.htm";
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		final QuestState st = player.getQuestState(getName());
		if ((st != null) && st.isStarted() && st.isCond(1))
		{
			final int npcId = npc.getNpcId();
			
			if (Util.contains(MOBS, npcId) && (getRandom(100) < 10) && (st.getQuestItemsCount(DIARY) < 10))
			{
				st.giveItems(DIARY, 1);
				if ((st.getQuestItemsCount(DIARY) >= 10) && (st.getQuestItemsCount(TABLE) >= 5))
				{
					st.setCond(2, true);
				}
				else
				{
					st.playSound("ItemSound.quest_itemget");
				}
			}
			else if (Util.contains(MOBS2, npcId) && (getRandom(100) < 5) && (st.getQuestItemsCount(TABLE) < 5))
			{
				st.giveItems(TABLE, 1);
				if ((st.getQuestItemsCount(DIARY) >= 10) && (st.getQuestItemsCount(TABLE) >= 5))
				{
					st.setCond(2, true);
				}
				else
				{
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		return super.onKill(npc, player, isPet);
	}
	
	public static void main(String[] args)
	{
		new Q251_NoSecrets(251, qn, "No Secrets");
	}
}