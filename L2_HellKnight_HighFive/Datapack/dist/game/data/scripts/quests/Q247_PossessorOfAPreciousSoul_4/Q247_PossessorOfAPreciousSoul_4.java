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
package quests.Q247_PossessorOfAPreciousSoul_4;

import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;

public class Q247_PossessorOfAPreciousSoul_4 extends Quest
{
	private static final String _qn = "247_PossessorOfAPreciousSoul_4";
	//NPC's
	private static final int CARADINE = 31740;
	private static final int LADY_OF_LAKE = 31745;
	private static final int CARADINE_LETTER_LAST = 7679;
    private static final int NOBLESS_TIARA = 7694;
	private final int NPCS[] = { CARADINE, LADY_OF_LAKE };	
	
	/**
	 * @param questId
	 * @param name
	 * @param descr
	 */
	public Q247_PossessorOfAPreciousSoul_4(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(CARADINE);		
		for (int npcId : NPCS)
			addTalkId(npcId);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(_qn);		
		if (st == null)
			return null;
		int cond = st.getInt("cond");
		
		if (event.equalsIgnoreCase("31740-3.htm"))
		{
			if (cond == 0)
			{
				st.set("cond","1");
				st.setState(State.STARTED);
				st.playSound("ItemSound.quest_accept");
			}
		}
		if (event.equalsIgnoreCase("31740-5.htm"))
		{
			if (cond == 1)
			{
				st.set("cond","2");
				st.takeItems(CARADINE_LETTER_LAST,1);
				st.getPlayer().teleToLocation(143209,43968,-3038);
			}
		}	
		if (event.equalsIgnoreCase("31745-5.htm"))
		{
			if (cond == 2)
			{
				st.set("cond","0");
				st.getPlayer().setNoble(true);
				st.addExpAndSp(93836,0);
				st.giveItems(NOBLESS_TIARA,1);
				st.playSound("ItemSound.quest_finish");
				st.exitQuest(false);
			}
		}			
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance talker)
	{
		String htmltext = getNoQuestMsg(talker);		
		QuestState st = talker.getQuestState(_qn);
		if (st == null)
			return htmltext;
			
		int npcId = npc.getNpcId();
		int cond = st.getInt("cond");			
		byte id = st.getState();
		if (npcId != CARADINE && id != State.STARTED)
			return htmltext;
		if (id == State.CREATED)
		{
			st.set("cond","0");
		}
		if (talker.isSubClassActive())
		{
			switch (npcId)
			{
				case CARADINE:
				if (st.getQuestItemsCount(CARADINE_LETTER_LAST) == 1)
				{
					if(cond == 0 || cond == 1)
					{
						if(id == State.COMPLETED)
						{
							htmltext = Quest.getAlreadyCompletedMsg(talker);
						}
						else if(talker.getLevel() < 75)
						{
							htmltext = "31740-2.htm";
							st.exitQuest(true);
						}
						else if(talker.getLevel() >= 75)
						{
							htmltext = "31740-1.htm";
						}
					}			
				}
				else if(cond == 2)
					htmltext = "31740-6.htm";
                break;
				case LADY_OF_LAKE:
					if(cond == 2)
						htmltext = "31745-1.htm";
				break;		
			}
				
		}
		else
			htmltext = "sub.htm"; // No Sub		

		return htmltext;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		new Q247_PossessorOfAPreciousSoul_4(247, _qn, "Possessor Of A Precious Soul - 4");
	}	
}	
