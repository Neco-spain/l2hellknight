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
package quests.Q242_PossessorOfAPreciousSoul_2;

import l2.hellknight.Config;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;

public class Q242_PossessorOfAPreciousSoul_2 extends Quest
{
	private static final String _qn = "242_PossessorOfAPreciousSoul_2";
	// NPC's
	private static final int VIRGIL = 31742;
	private static final int KASSANDRA = 31743;
	private static final int OGMAR = 31744;
	private static final int FALLEN_UNICORN = 31746;
	private static final int PURE_UNICORN = 31747;
	private static final int CORNERSTONE = 31748;
	private static final int MYSTERIOUS_KNIGHT = 31751;
	private static final int ANGEL_CORPSE = 31752;
	private static final int KALIS = 30759;
	private static final int MATILD = 30738;
	private final int NPCS[] = { VIRGIL, KASSANDRA, OGMAR, FALLEN_UNICORN, PURE_UNICORN, CORNERSTONE, MYSTERIOUS_KNIGHT, ANGEL_CORPSE, KALIS, MATILD };	

	// Quest Item's
	private static final int VIRGILS_LETTER = 7677;
	private static final int GOLDEN_HAIR = 7590;
	private static final int ORB_OF_BINDING = 7595;
	private static final int SORCERY_INGREDIENT = 7596;
	private static final int CARADINE_LETTER = 7678;

	// Chances
	private static final int CHANCE_FOR_HAIR = (int) (20 * Config.RATE_QUEST_DROP);

	// Mob
    private static final int RESTRAINER_OF_GLORY = 27317;
	
	/**
	 * @param questId
	 * @param name
	 * @param descr
	 */
	public Q242_PossessorOfAPreciousSoul_2(int questId, String name, String descr)
	{
		super(questId, name, descr);		
		addStartNpc(VIRGIL);	
		for (int npcId : NPCS)
			addTalkId(npcId);			
		addKillId(RESTRAINER_OF_GLORY);
		questItemIds = new int[5];
		questItemIds[0] = VIRGILS_LETTER;
		questItemIds[1] = GOLDEN_HAIR;
		questItemIds[2] = ORB_OF_BINDING;
		questItemIds[3] = SORCERY_INGREDIENT;
		questItemIds[4] = CARADINE_LETTER;
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(_qn);		
		if (st == null)
			return null;	
		int cond = st.getInt("cond");
		
		if (!st.getPlayer().isSubClassActive())
			return null;
		
		if (event.equalsIgnoreCase("31742-3.htm"))
		{
			if (cond == 0)
			{
				st.setState(State.STARTED);
				st.takeItems(VIRGILS_LETTER,1);
				st.set("cond","1");
				st.playSound("ItemSound.quest_accept");		
			}
		}
		else if (event.equalsIgnoreCase("31743-5.htm"))
		{
			if (cond == 1)
			{
				st.set("cond","2");
				st.setState(State.STARTED);
				st.playSound("ItemSound.quest_accept");
			}
		}
		else if (event.equalsIgnoreCase("31744-2.htm"))
		{
			if (cond == 2)
			{
				st.set("cond","3");
				st.playSound("ItemSound.quest_middle");
			}
		}
		else if (event.equalsIgnoreCase("31751-2.htm"))
		{
			if (cond == 3)
			{
				st.set("cond","4");
				st.playSound("ItemSound.quest_middle");
			}
		}
		else if (event.equalsIgnoreCase("30759-2.htm"))
		{
			if (cond == 6)
			{
				st.set("cond","7");
				st.playSound("ItemSound.quest_middle");
			}
		}
		else if (event.equalsIgnoreCase("30738-2.htm"))
		{
			if (cond == 7)
			{
				st.set("cond","8");
				st.giveItems(SORCERY_INGREDIENT,1);
				st.playSound("ItemSound.quest_middle");
			}
		}
		else if (event.equalsIgnoreCase("30759-5.htm"))
		{
			if (cond == 8)
			{
				st.set("cond","9");
				st.set("awaitsDrops","1");
				st.takeItems(GOLDEN_HAIR,1);
				st.takeItems(SORCERY_INGREDIENT,1);
				st.playSound("ItemSound.quest_middle");
			}
		}
		else if (event.equalsIgnoreCase("1"))
		{
			npc.getSpawn().stopRespawn();
			npc.deleteMe();
			st.addSpawn(PURE_UNICORN, 85884, -76588, -3470, 0, false, 60000, true);
		}
		else if (event.equalsIgnoreCase("2"))
		{
			npc.doDie(npc);
			L2Npc npc2 = st.addSpawn(FALLEN_UNICORN, 85884, -76588, -3470, 0, false, 1000, true);
			npc2.getSpawn().startRespawn();
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
		int cornerstones = st.getInt("cornerstones");		
		byte id = st.getState();
		if (npcId != VIRGIL && id != State.STARTED)
			return htmltext;
		if (id == State.CREATED)
		{
			st.set("cond","0");
			st.set("cornerstones","0");	
		}
		if (talker.isSubClassActive())
		{		
			switch (npcId)
			{
				case VIRGIL:
					if (cond == 0 && st.getQuestItemsCount(VIRGILS_LETTER) == 1)
					{
						if(id == State.COMPLETED)
						{
							htmltext = Quest.getAlreadyCompletedMsg(talker);
						}
						else if(talker.getLevel() < 60)
						{
							htmltext = "31742-2.htm";
							st.exitQuest(true);
						}
						else if(talker.getLevel() >= 60)
						{
							htmltext = "31742-1.htm";
						}	
					}
					else if(cond == 1)
					{
						htmltext = "31742-4.htm";
					}
					else if(cond == 11)
					{
						htmltext = "31742-6.htm";
					}
				break;
				case KASSANDRA:
					if(cond == 1)
						htmltext = "31743-1.htm";
					else if(cond == 2)
						htmltext = "31743-6.htm";
					else if(cond == 11)
					{
						htmltext = "31743-7.htm";
						st.set("cond","0");
						st.set("cornerstones","0");
						st.addExpAndSp(455764,0);
						st.playSound("ItemSound.quest_finish");
						st.exitQuest(false);
						st.giveItems(CARADINE_LETTER,1);
					}
				break;		
				case OGMAR:
					if(cond == 2)
						htmltext = "31744-1.htm";
					else if(cond == 3)
					htmltext = "31744-3.htm";
				break;
				case MYSTERIOUS_KNIGHT:
					if(cond == 3)
						htmltext = "31751-1.htm";
					else if(cond == 4)
						htmltext = "31751-3.htm";
					else if(cond == 5 && st.getQuestItemsCount(GOLDEN_HAIR) == 1)
						htmltext = "31751-4.htm";
						st.set("cond","6");
						st.playSound("ItemSound.quest_middle");
					if(cond == 6)
						htmltext = "31751-5.htm";
				break;		
				case ANGEL_CORPSE:
					if(cond == 4)
						npc.doDie(npc);
						int chance = getRandom(100);
					if (CHANCE_FOR_HAIR < chance)
					{
						htmltext = "31752-2.htm";
					}	
					else
					{
						st.set("cond","5");
						st.giveItems(GOLDEN_HAIR,1);
						st.playSound("ItemSound.quest_middle");
						htmltext = "31752-1.htm";
					}	
					if(cond == 5)
						htmltext = "31752-2.htm";
				break;
				case KALIS:
					if(cond == 6)
						htmltext = "30759-1.htm";
					else if(cond == 7)
						htmltext = "30759-3.htm";
					else if(cond == 8 && st.getQuestItemsCount(SORCERY_INGREDIENT) == 1)
						htmltext = "30759-4.htm";
					else if(cond == 9)
						htmltext = "30759-6.htm";
				break;		
				case MATILD:
					if(cond == 7)
						htmltext = "30738-1.htm";
					else if(cond == 8)
						htmltext = "30738-3.htm";
				break;		
				case FALLEN_UNICORN:
					if(cond == 9)
						htmltext = "31746-1.htm";
					else if(cond == 10)
						htmltext = "31746-2.htm";
							startQuestTimer("1",3000,npc,talker);
				break;		
				case CORNERSTONE:
					if(cond == 9 && st.getQuestItemsCount(ORB_OF_BINDING) == 0)
						htmltext = "31748-1.htm";
					else if(cond == 9 && st.getQuestItemsCount(ORB_OF_BINDING) >= 1)
						htmltext = "31748-2.htm";
						st.takeItems(ORB_OF_BINDING,1);
						npc.doDie(npc);
						st.set("cornerstones",Integer.toString(cornerstones+1));
						st.playSound("ItemSound.quest_middle");
					if(cornerstones == 3)
						st.set("cond","10");
						st.playSound("ItemSound.quest_middle");
						startQuestTimer("2",3000,npc,talker);
				break;		
				case PURE_UNICORN:
					if(cond == 10)
					{
						st.set("cond","11");
						st.playSound("ItemSound.quest_middle");
						htmltext = "31747-1.htm";
						startQuestTimer("2",3000,npc,talker);
					}	
					else if(cond == 11)
						htmltext = "31747-2.htm";
				break;				
			}
		}
		else
			htmltext = "sub.htm";

		return htmltext;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)	
	{
		int npcId = npc.getNpcId();
		QuestState st = killer.getQuestState(_qn);
		L2PcInstance partyMember;	
		switch (npcId)
		{
			case RESTRAINER_OF_GLORY:
				//get a random party member who is doing this quest and needs this drop 
				partyMember = getRandomPartyMember(killer, "awaitsDrops","1");
				if (partyMember == null)
						return null;				
				st = partyMember.getQuestState(_qn);
				if(st.getInt("cond") == 9 && st.getQuestItemsCount(ORB_OF_BINDING) <= 4)
				{
					st.giveItems(ORB_OF_BINDING,1);
					st.playSound("ItemSound.quest_itemget");					
				}
				if(st.getQuestItemsCount(ORB_OF_BINDING) == 5)
				{
					st.unset("awaitsDrops");
				}	
			break;
		}
		return null;
	}

	/**
	 * @param args
	 */	
	public static void main(String[] args)
	{
		new Q242_PossessorOfAPreciousSoul_2(242, _qn, "Possessor Of A Precious Soul - 2");
	}
}	
