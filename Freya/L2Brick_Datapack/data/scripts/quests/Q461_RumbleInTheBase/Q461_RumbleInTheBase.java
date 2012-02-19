package quests.Q461_RumbleInTheBase;

import l2.brick.Config;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.model.quest.QuestState;
import l2.brick.gameserver.model.quest.State;
import l2.brick.gameserver.util.Util;
import l2.brick.util.Rnd;

import java.util.Calendar;

public class Q461_RumbleInTheBase extends Quest
{
	private static final String qn = "461_RumbleInTheBase";
	private static final int STAN = 30200;
	private static final int CHIEF = 18908;
	private static final int FISH = 15503; //5
	private static final int STRING = 16382; //10
	private static final int[] MOBS =
	{
		22780, 22782, 22784, 22781, 22783, 22785
	};
	
	/*
	 * Reset time for Quest
	 * Default: 6:30AM on server time
	 */
	private static final int RESET_HOUR = 6;
	private static final int RESET_MIN = 30;
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		
		if (st == null)
			return htmltext;
		
		if (npc.getNpcId() == STAN)
		{
			if (event.equalsIgnoreCase("30200-03.htm"))
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.playSound("ItemSound.quest_accept");
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		QuestState _prev = player.getQuestState("252_ItSmellsDelicious");
		if (st == null)
			return htmltext;
		
		if (npc.getNpcId() == STAN)
		{
			switch(st.getState())
			{
				case State.CREATED :
					if (player.getLevel() >= 82 && (_prev != null && _prev.getState() == State.COMPLETED))
						htmltext = "30200-01.htm";
					else
						htmltext = "30200-00.htm";
					break;
				case State.STARTED :
					if (st.getInt("cond") == 1)
						htmltext = "30200-04.htm";
					else if (st.getInt("cond") == 2)
					{
						htmltext = "30200-05.htm";
						st.unset("cond");
						st.takeItems(FISH, -1);
						st.takeItems(STRING, -1);
						st.addExpAndSp(224784, 342528);
						st.playSound("ItemSound.quest_finish");
						st.exitQuest(false);
					
						Calendar reDo = Calendar.getInstance();
						reDo.set(Calendar.MINUTE, RESET_MIN);
						if (reDo.get(Calendar.HOUR_OF_DAY) >= RESET_HOUR)
							reDo.add(Calendar.DATE, 1);
						reDo.set(Calendar.HOUR_OF_DAY, RESET_HOUR);
						st.set("reDoTime", String.valueOf(reDo.getTimeInMillis()));
					}
					break;
				case State.COMPLETED :
					Long reDoTime = Long.parseLong(st.get("reDoTime"));
					if (reDoTime > System.currentTimeMillis())
						htmltext = "30200-06.htm";
					else
					{
						st.setState(State.CREATED);
						if (player.getLevel() >= 82 && (_prev != null && _prev.getState() == State.COMPLETED))
							htmltext = "30200-01.htm";
						else
							htmltext = "30200-00.htm";
					}
					break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet) 
	{
		QuestState st = player.getQuestState(getName());
		int npcId = npc.getNpcId();
		if (st == null || st.getState() != State.STARTED)
			return null;
		if (st.getInt("cond") == 1)
		{
			if ((Util.contains(MOBS, npcId)) && (Rnd.get(100) < 10) && (st.getQuestItemsCount(STRING) < 10))
			{
				st.giveItems(STRING, 1);
				st.playSound("ItemSound.quest_itemget");
				if ((st.getQuestItemsCount(STRING) >= 10) && (st.getQuestItemsCount(FISH) >= 5))
				{
					st.set("cond", "2");
					st.playSound("ItemSound.quest_itemget");
				}
			}
			else if ((npc.getNpcId() == CHIEF) && (Rnd.get(100) < 5) && (st.getQuestItemsCount(FISH) < 5))
			{
				st.giveItems(FISH, 1);
				st.playSound("ItemSound.quest_itemget");
				if ((st.getQuestItemsCount(STRING) >= 10) && (st.getQuestItemsCount(FISH) >= 5))
				{
					st.set("cond", "2");
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		return super.onKill(npc, player, isPet);
	}
	
	public Q461_RumbleInTheBase(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		questItemIds = new int[]{ FISH, STRING };
		addStartNpc(STAN);
		addTalkId(STAN);
		addKillId(CHIEF);
		for(int i : MOBS)
			addKillId(i);
	}
	
	public static void main(String[] args)
	{
		new Q461_RumbleInTheBase(461, qn, "Rumble In The Base");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Quest: Rumble In The Base");
	}
}