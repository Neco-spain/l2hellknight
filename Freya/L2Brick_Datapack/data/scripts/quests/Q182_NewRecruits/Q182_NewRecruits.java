package quests.Q182_NewRecruits;

import l2.brick.Config;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.model.quest.QuestState;
import l2.brick.gameserver.model.quest.State;

public class Q182_NewRecruits extends Quest
{
	private static final String qn = "182_NewRecruits";
	// NPC's
	private static final int _kekropus = 32138;
	private static final int _nornil = 32258;
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		
		if (st == null)
			return htmltext;
		
		if (npc.getNpcId() == _kekropus)
		{
			if (event.equalsIgnoreCase("32138-03.htm"))
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.playSound("ItemSound.quest_accept");
			}
		}
		else if (npc.getNpcId() == _nornil)
		{
			if (event.equalsIgnoreCase("32258-04.htm"))
			{
				st.giveItems(847, 2);
				st.playSound("ItemSound.quest_finish");
				st.exitQuest(false);
			}
			else if (event.equalsIgnoreCase("32258-05.htm"))
			{
				st.giveItems(890, 2);
				st.playSound("ItemSound.quest_finish");
				st.exitQuest(false);
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
		
		if(player.getRace().ordinal() == 5)
		{
			htmltext = "32138-00.htm";
		}
		else
		{
			if (npc.getNpcId() == _kekropus)
			{
				switch(st.getState())
				{
					case State.CREATED :
							htmltext = "32138-01.htm";
						break;
					case State.STARTED :
						if (st.getInt("cond") == 1)
							htmltext = "32138-03.htm";
						break;
					case State.COMPLETED :
						htmltext = getAlreadyCompletedMsg(player);
						break;
				}
			}
			else if (npc.getNpcId() == _nornil && st.getState() == State.STARTED)
			{
				htmltext = "32258-01.htm";
			}
		}
		
		return htmltext;
	}
	
	public Q182_NewRecruits(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(_kekropus);
		addTalkId(_kekropus);
		addTalkId(_nornil);
	}
	
	public static void main(String[] args)
	{
		new Q182_NewRecruits(182, qn, "New Recruits");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Quest: New Recruits");
	}
}