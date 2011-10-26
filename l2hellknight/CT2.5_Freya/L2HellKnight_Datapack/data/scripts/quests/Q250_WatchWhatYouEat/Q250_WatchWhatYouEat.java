package quests.Q250_WatchWhatYouEat;

import l2.hellknight.Config;
import l2.hellknight.gameserver.instancemanager.QuestManager;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;

public class Q250_WatchWhatYouEat extends Quest
{
	private static final String qn = "250_WatchWhatYouEat";
	// NPCs
	private static final int _sally = 32743;
	// Mobs - Items
	private static final int[][] _mobs = {
		{ 18864, 15493 },
		{ 18865, 15494 },
		{ 18868, 15495 }
	};
	
	@Override
	public String onAdvEvent (String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		
		if (st == null)
			return htmltext;
		
		if (npc.getNpcId() == _sally)
		{
			if (event.equalsIgnoreCase("32743-03.htm"))
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.playSound("ItemSound.quest_accept");
			}
			else if (event.equalsIgnoreCase("32743-end.htm"))
			{
				st.unset("cond");
				st.rewardItems(57,135661);
				st.addExpAndSp(698334,76369);
				st.playSound("ItemSound.quest_finish");
				st.exitQuest(false);
			}
			else if (event.equalsIgnoreCase("32743-22.html") && st.getState() == State.COMPLETED)
			{
				htmltext = "32743-23.html";
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
		
		if (npc.getNpcId() == _sally)
		{
			switch(st.getState())
			{
				case State.CREATED :
					if (player.getLevel() >= 82)
						htmltext = "32743-01.htm";
					else
						htmltext = "32743-00.htm";
					break;
				case State.STARTED :
					if (st.getInt("cond") == 1)
					{
						htmltext = "32743-04.htm";
					}
					else if (st.getInt("cond") == 2)
					{
						if(st.hasQuestItems(_mobs[0][1]) && st.hasQuestItems(_mobs[1][1]) && st.hasQuestItems(_mobs[2][1]))
						{
							htmltext = "32743-05.htm";
							for(int items[] : _mobs)
								st.takeItems(items[1], -1);
						}
						else
							htmltext = "32743-06.htm";
					}
					break;
				case State.COMPLETED :
					htmltext = "32743-done.htm";
					break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return null;
		if (st.getState() == State.STARTED && st.getInt("cond") == 1)
		{
			for(int mob[] : _mobs)
			{
				if (npc.getNpcId() == mob[0])
				{
					if (!st.hasQuestItems(mob[1]))
					{
						st.giveItems(mob[1], 1);
						st.playSound("ItemSound.quest_itemget");
					}
				}
			}
			if(st.hasQuestItems(_mobs[0][1]) && st.hasQuestItems(_mobs[1][1]) && st.hasQuestItems(_mobs[2][1]))
			{
				st.set("cond", "2");
				st.playSound("ItemSound.quest_middle");
			}
		}
		return null;
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
		
		if (npc.getNpcId() == _sally)
			return "32743-20.html";
		
		return null;
	}
	
	public Q250_WatchWhatYouEat(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		questItemIds = new int[]{15493,15494,15495};

		addStartNpc(_sally);
		addFirstTalkId(_sally);
		addTalkId(_sally);
		
		for(int i[] : _mobs)
			addKillId(i[0]);
	}
	
	public static void main(String[] args)
	{
		new Q250_WatchWhatYouEat(250, qn, "Watch What You Eat");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Quest: Watch What You Eat");
	}
}
