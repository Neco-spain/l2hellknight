package quests.Q20_BringUpWithLove;

import l2.brick.Config;
import l2.brick.gameserver.instancemanager.QuestManager;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.model.quest.QuestState;
import l2.brick.gameserver.model.quest.State;

public class Q20_BringUpWithLove extends Quest
{
	private static final String qn = "20_BringUpWithLove";
	// Npc
	private static final int _tunatun = 31537;
	// Item
	private static final int _beast_whip = 15473;
	private static final int _crystal = 9553;
	private static final int _jewel = 7185;
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		
		if (st == null)
			return htmltext;
		
		if (npc.getNpcId() == _tunatun)
		{
			if (event.equalsIgnoreCase("31537-12.htm"))
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.playSound("ItemSound.quest_accept");
			}
			else if (event.equalsIgnoreCase("31537-03.htm"))
			{
				if (st.hasQuestItems(_beast_whip))
					return "31537-03a.htm";
				else
					st.giveItems(_beast_whip, 1);
			}
			else if (event.equalsIgnoreCase("31537-15.htm"))
			{
				st.unset("cond");
				st.takeItems(_jewel, -1);
				st.giveItems(_crystal, 1);
				st.playSound("ItemSound.quest_finish");
				st.exitQuest(false);
			}
			else if (event.equalsIgnoreCase("31537-21.html"))
			{
				if (player.getLevel() < 82)
					return "31537-23.html";
				if (st.hasQuestItems(_beast_whip))
					return "31537-22.html";
				st.giveItems(_beast_whip, 1);
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
		
		if (npc.getNpcId() == _tunatun)
		{
			switch(st.getState())
			{
				case State.CREATED :
					if (player.getLevel() >= 82)
						htmltext = "31537-01.htm";
					else
						htmltext = "31537-00.htm";
					break;
				case State.STARTED :
					if (st.getInt("cond") == 1)
						htmltext = "31537-13.htm";
					else if (st.getInt("cond") == 2)
						htmltext = "31537-14.htm";
					break;
			}
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
		return "31537-20.html";
	}
	
	public Q20_BringUpWithLove(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(_tunatun);
		addTalkId(_tunatun);
		addFirstTalkId(_tunatun);
	}
	
	public static void main(String[] args)
	{
		new Q20_BringUpWithLove(20, qn, "Bring Up With Love");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Quest: Bring Up With Love");
	}
}