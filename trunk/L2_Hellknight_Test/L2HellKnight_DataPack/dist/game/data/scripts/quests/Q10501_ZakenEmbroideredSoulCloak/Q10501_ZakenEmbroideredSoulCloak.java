package quests.Q10501_ZakenEmbroideredSoulCloak;

import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;
import l2.hellknight.util.Rnd;

public class Q10501_ZakenEmbroideredSoulCloak extends Quest
{
	private static final String qn = "Q10501_ZakenEmbroideredSoulCloak";
	//NPCs 
	private static final int Olfadams = 32612;
	private static final int Zaken = 29181;
	//ITEMS 
	private static final int Zakensoulfragment = 21722;
	//REWARD 
	private static final int Cloakofzaken = 21719;
	
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
			if (st.getQuestItemsCount(Zakensoulfragment) < 20)
			{
				st.set("cond","1");
			    st.playSound("ItemSound.quest_middle");
			    htmltext = "32612-error.htm";
			}
			else
			{
				st.giveItems(Cloakofzaken, 1);
				st.takeItems(Zakensoulfragment, 20);
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
		    if (player.getLevel() < 78)
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
			if (st.getQuestItemsCount(Zakensoulfragment) <= 19)
			{
				if (st.getQuestItemsCount(Zakensoulfragment) == 18)
				{
					st.giveItems(Zakensoulfragment, Rnd.get(1, 2));
					st.playSound("ItemSound.quest_itemget");
				}
				else if (st.getQuestItemsCount(Zakensoulfragment) == 19)
				{
					st.giveItems(Zakensoulfragment, 1);
					st.playSound("ItemSound.quest_itemget");
				}
				else
				{
					st.giveItems(Zakensoulfragment, Rnd.get(1, 3));
					st.playSound("ItemSound.quest_itemget");
				}
				if (st.getQuestItemsCount(Zakensoulfragment) >= 20)
				{
					st.set("cond","2");
				    st.playSound("ItemSound.quest_middle");
				}
			}
		}
		
		if (player.getParty() != null)
		{
			QuestState st2;
			for(L2PcInstance pmember : player.getParty().getPartyMembers())
			{
				st2 = pmember.getQuestState(qn);
				
				if(st2 != null && st2.getInt("cond") == 1 && pmember.getObjectId() != partyMember.getObjectId())
				{
					if (st2.getQuestItemsCount(Zakensoulfragment) <= 19)
					{
						if (st2.getQuestItemsCount(Zakensoulfragment) == 18)
						{
							st2.giveItems(Zakensoulfragment, Rnd.get(1, 2));
							st2.playSound("ItemSound.quest_itemget");
						}
						else if (st2.getQuestItemsCount(Zakensoulfragment) == 19)
						{
							st2.giveItems(Zakensoulfragment, 1);
							st2.playSound("ItemSound.quest_itemget");
						}
						else
						{
							st2.giveItems(Zakensoulfragment, Rnd.get(1, 3));
							st2.playSound("ItemSound.quest_itemget");
						}
						if (st2.getQuestItemsCount(Zakensoulfragment) >= 20)
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
	
	public Q10501_ZakenEmbroideredSoulCloak(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(Olfadams);
		addTalkId(Olfadams);
		addKillId(Zaken);
		
		questItemIds = new int[] { Zakensoulfragment };
	}
	
	public static void main(String[] args)
	{
		new Q10501_ZakenEmbroideredSoulCloak(10501, qn, "Zaken Embroidered Soul Cloak");
	}
}