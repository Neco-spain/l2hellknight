package quests.Q10503_FrintezzaEmbroideredSoulCloak;

import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;
import l2.hellknight.util.Rnd;

public class Q10503_FrintezzaEmbroideredSoulCloak extends Quest
{
	private static final String qn = "Q10503_FrintezzaEmbroideredSoulCloak";
	//NPCs 
	private static final int Olfadams = 32612;
	private static final int Frintezza = 29045;
	//ITEMS 
	private static final int Frintezzasoulfragment = 21724;
	//REWARD 
	private static final int CloakofFrintezza = 21721;
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		
		if (st == null)
			htmltext = getNoQuestMsg(player);
		
		if (event.equalsIgnoreCase("32612-01.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
			htmltext = "32612-01.htm";
		}
		else if (event.equalsIgnoreCase("32612-03.htm"))
		{
			if (st.getQuestItemsCount(Frintezzasoulfragment) < 20)
			{
				st.set("cond","1");
			    st.playSound("ItemSound.quest_middle");
			    htmltext = "32612-error.htm";
			}
			else
			{
				st.giveItems(CloakofFrintezza, 1);
				st.takeItems(Frintezzasoulfragment, 20);
				st.playSound("ItemSound.quest_finish");
				st.exitQuest(false);
				htmltext = "32612-reward.htm";
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
			return htmltext;
		
		if (st.isCompleted())
		    htmltext = getAlreadyCompletedMsg(player);
		else if (st.isCreated())
		{
		    if (player.getLevel() < 80)
		        htmltext = "32612-level_error.htm";
		    else
		        htmltext = "32612-00.htm";
		}
		else if (st.getInt("cond") == 2)
		    htmltext = "32612-02.htm";
		else
		    htmltext = "32612-01.htm";
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		L2PcInstance partyMember = getRandomPartyMember(player,"1");
		
		if (partyMember == null)
			return super.onKill(npc, player, isPet);
		
		QuestState st = partyMember.getQuestState(qn);
		
		if (st != null)
		{
			if (st.getQuestItemsCount(Frintezzasoulfragment) <= 19)
			{
				if (st.getQuestItemsCount(Frintezzasoulfragment) == 18)
				{
					st.giveItems(Frintezzasoulfragment, Rnd.get(1, 2));
					st.playSound("ItemSound.quest_itemget");
				}
				else if (st.getQuestItemsCount(Frintezzasoulfragment) == 19)
				{
					st.giveItems(Frintezzasoulfragment, 1);
					st.playSound("ItemSound.quest_itemget");
				}
				else
				{
					st.giveItems(Frintezzasoulfragment, Rnd.get(1, 3));
					st.playSound("ItemSound.quest_itemget");
				}
				if (st.getQuestItemsCount(Frintezzasoulfragment) >= 20)
				{
					st.set("cond","2");
				    st.playSound("ItemSound.quest_middle");
				}
			}
		}
		
		if (player.getParty() != null)
		{
			QuestState st2;
			for(L2PcInstance pmember : player.getParty().getMembers())
			{
				st2 = pmember.getQuestState(qn);
				
				if(st2 != null && st2.getInt("cond") == 1 && pmember.getObjectId() != partyMember.getObjectId())
				{
					if (st2.getQuestItemsCount(Frintezzasoulfragment) <= 19)
					{
						if (st2.getQuestItemsCount(Frintezzasoulfragment) == 18)
						{
							st2.giveItems(Frintezzasoulfragment, Rnd.get(1, 2));
							st2.playSound("ItemSound.quest_itemget");
						}
						else if (st2.getQuestItemsCount(Frintezzasoulfragment) == 19)
						{
							st2.giveItems(Frintezzasoulfragment, 1);
							st2.playSound("ItemSound.quest_itemget");
						}
						else
						{
							st2.giveItems(Frintezzasoulfragment, Rnd.get(1, 3));
							st2.playSound("ItemSound.quest_itemget");
						}
						if (st2.getQuestItemsCount(Frintezzasoulfragment) >= 20)
						{
							st2.set("cond","2");
						    st2.playSound("ItemSound.quest_middle");
						}
					}
				}
			}
		}
		
		return super.onKill(npc, player, isPet);
	}
	
	public Q10503_FrintezzaEmbroideredSoulCloak(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(Olfadams);
		addTalkId(Olfadams);
		addKillId(Frintezza);
		
		questItemIds = new int[] { Frintezzasoulfragment };
	}
	
	public static void main(String[] args)
	{
		new Q10503_FrintezzaEmbroideredSoulCloak(10503, qn, "Frintezza Embroidered Soul Cloak");
	}
}