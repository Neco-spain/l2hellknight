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
package quests.Q691_MatrasSuspiciousRequest;

import l2.brick.Config;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.QuestState;
import l2.brick.gameserver.model.quest.State;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.util.Rnd;

import java.util.Map;
import javolution.util.FastMap;

/**
 * @author GKR
 */

public final class Q691_MatrasSuspiciousRequest extends Quest
{
	private static final String	QN = "691_MatrasSuspiciousRequest";
	
	// NPCs
	private static final int	MATRAS = 32245;

	//Items
	private static final int RED_GEM = 10372;
	private static final int DYNASTY_SOUL_II = 10413;
	
	private static final Map<Integer, Integer> REWARD_CHANCES = new FastMap<Integer, Integer>();
	
	static
	{
		REWARD_CHANCES.put(22363, 890);
		REWARD_CHANCES.put(22364, 261);
		REWARD_CHANCES.put(22365, 560);
		REWARD_CHANCES.put(22366, 560);
		REWARD_CHANCES.put(22367, 190);
		REWARD_CHANCES.put(22368, 129);
		REWARD_CHANCES.put(22369, 210);
		REWARD_CHANCES.put(22370, 787);
		REWARD_CHANCES.put(22371, 257);
		REWARD_CHANCES.put(22372, 656);
	}
	
	public Q691_MatrasSuspiciousRequest(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(MATRAS);
		addTalkId(MATRAS);
		
		for (int npcId : REWARD_CHANCES.keySet())
			addKillId(npcId);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(QN);
		
		if (st == null)
			return getNoQuestMsg(player);

		if (event.equalsIgnoreCase("32245-04.htm"))
		{
			if (player.getLevel() >= 76)
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.playSound("ItemSound.quest_accept");
			}
			else
				htmltext = getNoQuestMsg(player);
		}

		else if (event.equalsIgnoreCase("take_reward"))
		{
			int gemsCount = st.getInt("submitted_gems");
			if (gemsCount >= 744)
			{
				st.set("submitted_gems", Integer.toString(gemsCount - 744));
				st.giveItems(DYNASTY_SOUL_II, 1);
				htmltext = "32245-09.htm";
			}
			else
				htmltext = getHtm(player.getHtmlPrefix(), "32245-06.htm").replace("%itemcount%", st.get("submitted_gems"));
		}
		
		else if (event.equalsIgnoreCase("32245-08.htm"))
		{
			int submittedCount = st.getInt("submitted_gems");
			int broughtCount = (int) st.getQuestItemsCount(RED_GEM);
			st.takeItems(RED_GEM, broughtCount);
			st.set("submitted_gems", Integer.toString(submittedCount + broughtCount));
			htmltext = getHtm(player.getHtmlPrefix(), "32245-08.htm").replace("%itemcount%", Integer.toString(submittedCount + broughtCount));
		}

		else if (event.equalsIgnoreCase("32245-12.htm"))
		{
			st.giveItems(57, (int) (st.getInt("submitted_gems") * 10000));
			st.playSound("IItemSound.quest_finish");
			st.exitQuest(true);
		}

		return htmltext;
	}

	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = Quest.getNoQuestMsg(player);
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;
		
		if (st.getState() == State.CREATED)
		{ 
			if (player.getLevel() >= 76)
				htmltext = "32245-01.htm";
			else
				htmltext = "32245-03.htm";
		}

		else if (st.getState() == State.STARTED)
		{
		 	if (st.getQuestItemsCount(RED_GEM) > 0)
		 		htmltext = "32245-05.htm";
		 	
		 	else if (st.getQuestItemsCount(RED_GEM) == 0)
		 		htmltext = "32245-06.htm";
		 	
		 	else if (st.getInt("submitted_gems") > 0)
				htmltext = getHtm(player.getHtmlPrefix(), "32245-06.htm").replace("%itemcount%", st.get("submitted_gems"));
		}

		return htmltext;
	}

	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		L2PcInstance pl = getRandomPartyMember(player, "1");
		if (pl == null)
			return null;

		final QuestState st = pl.getQuestState(QN);
		
		int chance = (int)(Config.RATE_QUEST_DROP * REWARD_CHANCES.get(npc.getNpcId()));
		int numItems = Math.max((int) (chance / 1000), 1);
		chance = chance % 1000;
		if (Rnd.get(1000) <= chance)
		{
			st.giveItems(RED_GEM, numItems);
			st.playSound("ItemSound.quest_itemget"); 
		}

		return null;
	}

	public static void main(String[] args)
	{
		new Q691_MatrasSuspiciousRequest(691, QN, "Matras' Suspicious Request");
	}
}
