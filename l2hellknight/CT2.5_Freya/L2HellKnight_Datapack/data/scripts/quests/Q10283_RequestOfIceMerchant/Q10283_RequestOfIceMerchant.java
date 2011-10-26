package quests.Q10283_RequestOfIceMerchant;

import l2.hellknight.Config;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;

public class Q10283_RequestOfIceMerchant extends Quest
{
	private static final String qn = "Q10283_RequestOfIceMerchant";
	// NPC's
	private static final int _rafforty = 32020;
	private static final int _kier = 32022;
	private static final int _jinia = 32760;
	
	public Q10283_RequestOfIceMerchant(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(_rafforty);
		addTalkId(_rafforty);
		addTalkId(_kier);
		addFirstTalkId(_jinia);
		addTalkId(_jinia);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		
		if (st == null)
			return htmltext;
		
		if (npc.getNpcId() == _rafforty)
		{
			if (event.equalsIgnoreCase("32020-03.htm"))
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.playSound("ItemSound.quest_accept");
			}
			else if (event.equalsIgnoreCase("32020-07.htm"))
			{
				st.set("cond", "2");
				st.playSound("ItemSound.quest_middle");
			}
		}
		else if (npc.getNpcId() == _kier && event.equalsIgnoreCase("spawn"))
		{
			addSpawn(_jinia, 104322, -107669, -3680, 44954, false, 60000);
			return null;
		}
		else if (npc.getNpcId() == _jinia && event.equalsIgnoreCase("32760-04.html"))
		{
			st.giveItems(57, 190000);
			st.addExpAndSp(627000, 50300);
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(false);
			npc.deleteMe();
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
		
		if (npc.getNpcId() == _rafforty)
		{
			switch (st.getState())
			{
				case State.CREATED:
					QuestState _prev = player.getQuestState("115_TheOtherSideOfTruth");
					if ((_prev != null) && (_prev.getState() == State.COMPLETED) && (player.getLevel() >= 82))
						htmltext = "32020-01.htm";
					else
						htmltext = "32020-00.htm";
					break;
				case State.STARTED:
					if (st.getInt("cond") == 1)
						htmltext = "32020-04.htm";
					else if (st.getInt("cond") == 2)
						htmltext = "32020-08.htm";
					break;
				case State.COMPLETED:
					htmltext = "31350-08.htm";
					break;
			}
		}
		else if (npc.getNpcId() == _kier && st.getInt("cond") == 2)
		{
			htmltext = "32022-01.html";
		}
		else if (npc.getNpcId() == _jinia && st.getInt("cond") == 2)
		{
			htmltext = "32760-02.html";
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return null;
		if (npc.getNpcId() == _jinia && st.getInt("cond") == 2)
			return "32760-01.html";
		
		npc.showChatWindow(player);
		return null;
	}
	public static void main(String[] args)
	{
		new Q10283_RequestOfIceMerchant(10283, qn, "Request of Ice Merchant");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Quest: Request of Ice Merchant");
	}
}
