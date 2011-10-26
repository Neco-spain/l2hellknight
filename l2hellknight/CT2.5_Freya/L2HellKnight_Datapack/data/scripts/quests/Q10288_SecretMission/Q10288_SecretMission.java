package quests.Q10288_SecretMission;

import l2.hellknight.Config;
import l2.hellknight.gameserver.instancemanager.QuestManager;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;

public class Q10288_SecretMission extends Quest
{
	private static final String qn = "10288_SecretMission";
	// NPC's
	private static final int _dominic  = 31350;
	private static final int _aquilani = 32780;
	private static final int _greymore = 32757;
	// Items
	private static final int _letter = 15529;
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		
		if (st == null)
			return htmltext;
		
		if (npc.getNpcId() == _dominic)
		{
			if (event.equalsIgnoreCase("31350-05.htm"))
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.giveItems(_letter, 1);
				st.playSound("ItemSound.quest_accept");
			}
		}
		else if (npc.getNpcId() == _greymore && event.equalsIgnoreCase("32757-03.htm"))
		{
			st.unset("cond");
			st.takeItems(_letter, -1);
			st.giveItems(57, 106583);
			st.addExpAndSp(417788, 46320);
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(false);
		}
		else if (npc.getNpcId() == _aquilani)
		{
			if (st.getState() == State.STARTED)
			{
				if (event.equalsIgnoreCase("32780-05.html"))
				{
					st.set("cond", "2");
					st.playSound("ItemSound.quest_middle");
				}
			}
			else if (st.getState() == State.COMPLETED && event.equalsIgnoreCase("teleport"))
			{
				player.teleToLocation(118833, -80589, -2688);
				return null;
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
		
		if (npc.getNpcId() == _dominic)
		{
			switch(st.getState())
			{
				case State.CREATED :
					if (player.getLevel() >= 82)
						htmltext = "31350-01.htm";
					else
						htmltext = "31350-00.htm";
					break;
				case State.STARTED :
					if (st.getInt("cond") == 1)
						htmltext = "31350-06.htm";
					else if (st.getInt("cond") == 2)
						htmltext = "31350-07.htm";
					break;
				case State.COMPLETED :
					htmltext = "31350-08.htm";
					break;
			}
		}
		else if (npc.getNpcId() == _aquilani)
		{
			if (st.getInt("cond") == 1)
			{
				htmltext = "32780-03.html";
			}
			else if (st.getInt("cond") == 2)
			{
				htmltext = "32780-06.html";
			}
		}
		else if (npc.getNpcId() == _greymore && st.getInt("cond") == 2)
		{
			return "32757-01.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			Quest q = QuestManager.getInstance().getQuest(qn);
			st = q.newQuestState(player);
		}
		if (npc.getNpcId() == _aquilani)
		{
			if (st.getState() == State.COMPLETED)
				return "32780-01.html";
			else
				return "32780-00.html";
		}
		return null;
	}
	
	public Q10288_SecretMission(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(_dominic);
		addStartNpc(_aquilani);
		addTalkId(_dominic);
		addTalkId(_greymore);
		addTalkId(_aquilani);
		addFirstTalkId(_aquilani);
	}
	
	public static void main(String[] args)
	{
		new Q10288_SecretMission(10288, qn, "Secret Mission");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Quest: Secret Mission");
	}
}