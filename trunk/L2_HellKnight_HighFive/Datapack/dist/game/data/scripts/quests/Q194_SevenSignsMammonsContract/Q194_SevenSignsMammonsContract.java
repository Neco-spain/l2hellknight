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
package quests.Q194_SevenSignsMammonsContract;

import l2.hellknight.gameserver.datatables.SkillTable;
import l2.hellknight.gameserver.model.effects.L2Effect;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;

/**
 * @author Plim
 */

public class Q194_SevenSignsMammonsContract extends Quest
{
	private static final String qn = "194_SevenSignsMammonsContract";
	
	//NPCs
	private static final int ATHEBALDT = 30760;
	private static final int COLIN = 32571;
	private static final int FROG = 32572;
	private static final int TESS = 32573;
	private static final int KUTA = 32574;
	private static final int CLAUDIA = 31001;
	
	//ITEMS
	private static final int INTRODUCTION = 13818;
	private static final int FROG_KING_BEAD = 13820;
	private static final int CANDY_POUCH = 13821;
	private static final int NATIVES_GLOVE = 13819;
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		
		if (st == null)
			return htmltext;
		
		if (npc.getNpcId() == ATHEBALDT)
		{
			if (event.equalsIgnoreCase("30760-02.htm"))
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.playSound("ItemSound.quest_accept");
			}
			
			else if (event.equalsIgnoreCase("30760-07.htm"))
			{
				st.set("cond", "3");
				st.giveItems(INTRODUCTION, 1);
				st.playSound("ItemSound.quest_middle");
			}
			
			else if (event.equalsIgnoreCase("10"))
			{
				st.set("cond", "2");
				st.playSound("ItemSound.quest_middle");
				player.showQuestMovie(10);
				return "";
			}
		}
		
		else if (npc.getNpcId() == COLIN)
		{
			if (event.equalsIgnoreCase("32571-04.htm"))
			{
				st.set("cond", "4");
				st.takeItems(INTRODUCTION, 1);
				transformPlayer(npc, player, 6201);
				st.playSound("ItemSound.quest_middle");
			}
			
			if (event.equalsIgnoreCase("32571-06.htm") || event.equalsIgnoreCase("32571-14.htm") || event.equalsIgnoreCase("32571-22.htm"))
			{
				if (player.isTransformed())
					player.untransform();
			}
			
			else if (event.equalsIgnoreCase("32571-08.htm"))
			{
				transformPlayer(npc, player, 6201);
			}
			
			else if (event.equalsIgnoreCase("32571-10.htm"))
			{
				st.set("cond", "6");
				st.takeItems(FROG_KING_BEAD, 1);
				st.playSound("ItemSound.quest_middle");
			}
			
			else if (event.equalsIgnoreCase("32571-12.htm"))
			{
				st.set("cond", "7");
				transformPlayer(npc, player, 6202);
				st.playSound("ItemSound.quest_middle");
			}
			
			else if (event.equalsIgnoreCase("32571-16.htm"))
				transformPlayer(npc, player, 6202);
			
			else if (event.equalsIgnoreCase("32571-18.htm"))
			{
				st.set("cond", "9");
				st.takeItems(CANDY_POUCH, 1);
				st.playSound("ItemSound.quest_middle");
			}
			
			else if (event.equalsIgnoreCase("32571-20.htm"))
			{
				st.set("cond", "10");
				transformPlayer(npc, player, 6203);
				st.playSound("ItemSound.quest_middle");
			}
			
			else if (event.equalsIgnoreCase("32571-24.htm"))
				transformPlayer(npc, player, 6203);
			
			else if (event.equalsIgnoreCase("32571-26.htm"))
			{
				st.set("cond", "12");
				st.takeItems(NATIVES_GLOVE, 1);
				st.playSound("ItemSound.quest_middle");
			}
		}
		
		else if (npc.getNpcId() == FROG)
		{
			if (event.equalsIgnoreCase("32572-04.htm"))
			{
				st.set("cond", "5");
				st.giveItems(FROG_KING_BEAD, 1);
				st.playSound("ItemSound.quest_middle");
			}
		}
		
		else if (npc.getNpcId() == TESS)
		{
			if (event.equalsIgnoreCase("32573-03.htm"))
			{
				st.set("cond", "8");
				st.giveItems(CANDY_POUCH, 1);
				st.playSound("ItemSound.quest_middle");
			}
		}
		
		else if (npc.getNpcId() == KUTA)
		{
			if (event.equalsIgnoreCase("32574-04.htm"))
			{
				st.set("cond", "11");
				st.giveItems(NATIVES_GLOVE, 1);
				st.playSound("ItemSound.quest_middle");
			}
		}
		
		else if (npc.getNpcId() == CLAUDIA)
		{
			if (event.equalsIgnoreCase("31001-03.htm"))
			{
				st.addExpAndSp(25000000, 2500000);
				st.unset("cond");
				st.setState(State.COMPLETED);
				st.exitQuest(false);
				st.playSound("ItemSound.quest_finish");
			}
		}
		
		return htmltext;
	}
	
	private void transformPlayer(L2Npc npc, L2PcInstance player, int transId)
	{
		if (player.isTransformed())
		{
			player.untransform();
			
			try
			{
				Thread.sleep(2000);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		
		for (L2Effect effect : player.getAllEffects())
		{
			if (effect.getAbnormalType().equalsIgnoreCase("speed_up"))
				effect.exit();
		}
		
		npc.setTarget(player);
		npc.doCast(SkillTable.getInstance().getInfo(transId, 1));
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		QuestState second = player.getQuestState("193_SevenSignsDyingMessage");
		
		if (st == null)
			return htmltext;
		
		if (npc.getNpcId() == ATHEBALDT)
		{
			switch (st.getState())
			{
				case State.CREATED :
					if (second != null && second.getState() == State.COMPLETED && player.getLevel() >= 79)
						htmltext = "30760-01.htm";
					
					else
					{
						htmltext = "30760-00.htm";
						st.exitQuest(true);
					}
					break;
					
				case State.STARTED :
					if (st.getInt("cond") == 1)
						htmltext = "30760-02.htm";
					
					else if (st.getInt("cond") == 2)
						htmltext = "30760-05.htm";
					
					else if (st.getInt("cond") == 3 && st.getQuestItemsCount(INTRODUCTION) >= 1)
						htmltext = "30760-08.htm";
					break;
				case State.COMPLETED :
					htmltext = getAlreadyCompletedMsg(player);
			}
		}
		
		else if (npc.getNpcId() == COLIN)
		{
			if (st.getState() == State.STARTED)
			{
				if (st.getInt("cond") == 3)
					htmltext = "32571-01.htm";
				
				else if (st.getInt("cond") == 4)
				{
					if (checkPlayer(player, 6201))
						htmltext = "32571-05.htm";
					
					else
						htmltext = "32571-07.htm";
				}
				
				else if (st.getInt("cond") == 5)
					htmltext = "32571-09.htm";
				
				else if (st.getInt("cond") == 6)
					htmltext = "32571-11.htm";
				
				else if (st.getInt("cond") == 7)
				{
					if (checkPlayer(player, 6202))
						htmltext = "32571-13.htm";
					
					else
						htmltext = "32571-15.htm";
				}
				else if (st.getInt("cond") == 8)
					htmltext = "32571-17.htm";
				
				else if (st.getInt("cond") == 9)
					htmltext = "32571-19.htm";
				
				else if (st.getInt("cond") == 10)
				{
					if (checkPlayer(player, 6203))
						htmltext = "32571-21.htm";
					
					else
						htmltext = "32571-23.htm";
				}
				
				else if (st.getInt("cond") == 11)
					htmltext = "32571-25.htm";
				
				else if (st.getInt("cond") == 12)
					htmltext = "32571-27.htm";
			}
		}
		
		else if (npc.getNpcId() == FROG)
		{
			if (checkPlayer(player, 6201))
			{
				if (st.getInt("cond") == 4)
					htmltext = "32572-01.htm";
				
				else if (st.getInt("cond") == 5)
					htmltext = "32572-05.htm";
			}
			
			else
				htmltext = "32572-00.htm";
		}
		
		else if (npc.getNpcId() == TESS)
		{
			if (checkPlayer(player, 6202))
			{
				if (st.getInt("cond") == 7)
					htmltext = "32573-01.htm";
				
				else if (st.getInt("cond") == 8)
					htmltext = "32573-04.htm";
			}
			
			else
				htmltext = "32573-00.htm";
		}
		
		else if (npc.getNpcId() == KUTA)
		{
			if (checkPlayer(player, 6203))
			{
				if (st.getInt("cond") == 10)
					htmltext = "32574-01.htm";
				
				else if (st.getInt("cond") == 11)
					htmltext = "32574-05.htm";
			}
			
			else
				htmltext = "32574-00.htm";
		}
		
		else if (npc.getNpcId() == CLAUDIA)
		{
			if (st.getInt("cond") == 12)
				htmltext = "31001-01.htm";
		}
		
		return htmltext;
	}
	
	private boolean checkPlayer(L2PcInstance player, int transId)
	{
		L2Effect effect = player.getFirstEffect(transId);
		
		if (effect != null)
			return true;
		
		return false;
	}
	
	public Q194_SevenSignsMammonsContract(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(ATHEBALDT);
		addTalkId(ATHEBALDT);
		addTalkId(COLIN);
		addTalkId(FROG);
		addTalkId(TESS);
		addTalkId(KUTA);
		addTalkId(CLAUDIA);
	}
	
	public static void main(String[] args)
	{
		new Q194_SevenSignsMammonsContract(194, qn, "Seven Signs, Mammon's Contract");
	}
}
