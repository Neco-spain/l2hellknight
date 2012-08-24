package quests.Q195_SevenSignSecretRitualOfThePriests;

import l2.hellknight.gameserver.datatables.SkillTable;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;

/**
 * @author Plim
 */

public class Q195_SevenSignSecretRitualOfThePriests extends Quest
{
	private static final String qn = "195_SevenSignSecretRitualOfThePriests";
	
	//NPCs
	private static final int CLAUDIA = 31001;
	private static final int JOHN = 32576;
	private static final int RAYMOND = 30289;
	private static final int LIGHT_DAWN = 32575;
	private static final int DEVICE = 32578;
	private static final int IASON_HEINE = 30969;
	private static final int PASSWORD_DEVICE = 32577;
	private static final int SHELF = 32580;
	private static final int DARKNESS_DAWN = 32579;
	
	//ITEMS
	private static final int ESCAPE_HEINE = 7128;
	private static final int SHUNAIMAN_CONTRACT = 13823;
	private static final int IDENTITY_CARD = 13822;
	
	//SKILLS
	private static final int GUARD_DAWN = 6204;
	
	//Check
	private boolean escape_given = false;
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		
		if (st == null)
			return htmltext;
		
		if (npc.getNpcId() == CLAUDIA)
		{
			if (event.equalsIgnoreCase("31001-02.htm"))
			{
				st.setState(State.STARTED);
				st.playSound("ItemSound.quest_accept");
			}
			
			else if (event.equalsIgnoreCase("31001-05.htm"))
			{
				st.set("cond", "1");
				st.playSound("ItemSound.quest_middle");
			}
		}
		
		else if (npc.getNpcId() == JOHN)
		{
			if (event.equalsIgnoreCase("32576-02.htm"))
			{
				 st.giveItems(IDENTITY_CARD,1);
			     st.set("cond","2");
			     st.playSound("ItemSound.quest_middle");
			}
		}
		
		else if (npc.getNpcId() == RAYMOND)
		{
			if (event.equalsIgnoreCase("30289-04.htm"))
			{
				player.stopAllEffects();
				SkillTable.getInstance().getInfo(6204,1).getEffects(player,player);
			    st.set("cond","3");
			}
			
			else if (event.equalsIgnoreCase("30289-07.htm"))
				player.stopAllEffects();
		}
		
		else if (npc.getNpcId() == IASON_HEINE)
		{
			if (event.equalsIgnoreCase("30969-03.htm"))
			{
				st.addExpAndSp(52518015, 5817677);
			    st.unset("cond"); 
			    st.exitQuest(false);
			    st.playSound("ItemSound.quest_finish");
			}
		}
		
		return htmltext;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		QuestState contractOfMammon = player.getQuestState("194_SevenSignsMammonsContract");
		
		if (st == null)
			return htmltext;
		
		if (st.getState() == State.COMPLETED)
			return getAlreadyCompletedMsg(player);
		
		if (npc.getNpcId() == CLAUDIA)
		{
			if(contractOfMammon != null && contractOfMammon.getState() == State.COMPLETED)
			{
				if (st.getInt("cond") == 0)
				{
					if (player.getLevel() >= 79)
						htmltext = "31001-01.htm";
					
					else
					{
						htmltext = "31001-0a.htm";
						st.exitQuest(true);
					}
				}
				
				else if (st.getInt("cond") == 1)
					htmltext = "31001-06.htm";
			}
			
			else
				htmltext = "31001-0b.htm";
		}
		
		else if (npc.getNpcId() == JOHN)
		{
			if (st.getInt("cond") == 1)
				htmltext = "32576-01.htm";
			
			else if (st.getInt("cond") == 2)
				htmltext = "32576-03.htm";
		}
		
		else if (npc.getNpcId() == RAYMOND)
		{
			if (st.getInt("cond") == 2)
				htmltext = "30289-01.htm";
			
			else if (st.getInt("cond") == 3)
				htmltext = "30289-06.htm";
			
			else if (st.getInt("cond") == 4 && !escape_given)			
			{
				escape_given = true;
				htmltext = "30289-08.htm";
				player.stopAllEffects();
				st.giveItems(ESCAPE_HEINE,1);
				st.playSound("ItemSound.quest_middle");   
			}
		}
		
		else if (npc.getNpcId() == LIGHT_DAWN)
		{
			if (st.getInt("cond") == 3 && st.getQuestItemsCount(IDENTITY_CARD) == 1)
				htmltext = "32575-02.htm";
			else
				htmltext = "32575-01.htm";
		}
		
		else if (npc.getNpcId() == IASON_HEINE && st.getQuestItemsCount(SHUNAIMAN_CONTRACT) == 1)
			htmltext = "30969-01.htm";
		
		return htmltext;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		
		if (npc.getNpcId() == DEVICE)
		{
			if (player.getFirstEffect(GUARD_DAWN) != null)
				htmltext = "32578-01.htm";
			else
				htmltext = "32578-02.htm";
		}
		
		else if (npc.getNpcId() == PASSWORD_DEVICE)
		{
			if (player.getFirstEffect(GUARD_DAWN) != null)
				htmltext = "32577-01.htm";
			else
				htmltext = "32577-03.htm";
		}
		
		else if (npc.getNpcId() == DARKNESS_DAWN)
			htmltext = "32579-01.htm";
		
		else if (npc.getNpcId() == SHELF && st.getQuestItemsCount(SHUNAIMAN_CONTRACT) == 0)
		{
			htmltext = "32580-01.htm";
			st.giveItems(SHUNAIMAN_CONTRACT,1);
			st.set("cond","4");
		}
		
		return htmltext;
	}

	public Q195_SevenSignSecretRitualOfThePriests(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(CLAUDIA);
		addTalkId(CLAUDIA);
		addTalkId(JOHN);
		addTalkId(RAYMOND);
		addTalkId(LIGHT_DAWN);
		addStartNpc(DEVICE);
		addFirstTalkId(DEVICE);
		addTalkId(DEVICE);
		addFirstTalkId(PASSWORD_DEVICE);
		addTalkId(PASSWORD_DEVICE);
		addFirstTalkId(SHELF);
		addFirstTalkId(DARKNESS_DAWN);
		addTalkId(DARKNESS_DAWN);
		addTalkId(IASON_HEINE);
	}
	
	public static void main(String[] args)
	{
		new Q195_SevenSignSecretRitualOfThePriests(195, qn, "Seven Sign Secret Ritual Of The Priests");
	}
}
