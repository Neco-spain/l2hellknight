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
package quests.Q690_JudesRequest;

import l2.hellknight.Config;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;

/**
 * Jude's Request (690)
 * @author malyelfik
 */
public class Q690_JudesRequest extends Quest
{
	private static final String qn = "Q690_JudesRequest";
	// NPC
	private static final int JUDE = 32356;
	private static final int LESSER_EVIL = 22398;
	private static final int GREATER_EVIL = 22399;
	
	// Items
	private static final int EVIL_WEAPON = 10327;
	private static final int[][] REWARDS =
	{
		{
			10373,
			10374,
			10375,
			10376,
			10377,
			10378,
			10379,
			10380,
			10381
		},
		{
			10397,
			10398,
			10399,
			10400,
			10401,
			10402,
			10403,
			10404,
			10405
		}
	};
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		
		if (st == null)
		{
			return getNoQuestMsg(player);
		}
		
		if (event.equalsIgnoreCase("32356-03.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("32356-07.htm"))
		{
			if (st.getQuestItemsCount(EVIL_WEAPON) >= 200)
			{
				st.giveItems(REWARDS[0][getRandom(REWARDS[0].length)], 1);
				st.takeItems(EVIL_WEAPON, 200);
				st.playSound("ItemSound.quest_middle");
				htmltext = "32356-07.htm";
			}
			else
			{
				htmltext = "32356-07a.htm";
			}
		}
		else if (event.equalsIgnoreCase("32356-08.htm"))
		{
			st.takeItems(EVIL_WEAPON, -1);
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(true);
		}
		else if (event.equalsIgnoreCase("32356-09.htm"))
		{
			if (st.getQuestItemsCount(EVIL_WEAPON) >= 5)
			{
				st.giveItems(REWARDS[1][getRandom(REWARDS[1].length)], 1);
				st.takeItems(EVIL_WEAPON, 5);
				st.playSound("ItemSound.quest_middle");
				htmltext = "32356-09.htm";
			}
			else
			{
				htmltext = "32356-09a.htm";
			}
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
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getLevel() >= 78)
				{
					htmltext = "32356-01.htm";
				}
				else
				{
					htmltext = "32356-02.htm";
				}
				break;
			case State.STARTED:
				if (st.getQuestItemsCount(EVIL_WEAPON) >= 200)
				{
					htmltext = "32356-04.htm";
				}
				else if (st.getQuestItemsCount(EVIL_WEAPON) < 5)
				{
					htmltext = "32356-05a.htm";
				}
				else
				{
					htmltext = "32356-05.htm";
				}
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		L2PcInstance partyMember = getRandomPartyMember(player, "1");
		if (partyMember == null)
		{
			return null;
		}
		final QuestState st = partyMember.getQuestState(qn);
		
		final int npcId = npc.getNpcId();
		int chance = 0;
		if (npcId == LESSER_EVIL)
		{
			chance = 173;
		}
		else if (npcId == GREATER_EVIL)
		{
			chance = 246;
		}
		// Apply the quest drop rate:
		chance *= Config.RATE_QUEST_DROP;
		// Normalize
		chance %= 1000;
		
		if (getRandom(1000) <= chance)
		{
			st.giveItems(EVIL_WEAPON, Math.max(chance / 1000, 1));
			st.playSound("ItemSound.quest_itemget");
		}
		return null;
	}
	
	public Q690_JudesRequest(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(JUDE);
		addTalkId(JUDE);
		addKillId(LESSER_EVIL);
		addKillId(GREATER_EVIL);
	}
	
	public static void main(String[] args)
	{
		new Q690_JudesRequest(690, qn, "Jude's Request");
	}
}
