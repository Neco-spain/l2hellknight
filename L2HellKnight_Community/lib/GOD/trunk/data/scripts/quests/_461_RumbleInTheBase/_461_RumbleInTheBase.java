package quests._461_RumbleInTheBase;

import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.quest.Quest;
import l2rt.gameserver.model.quest.QuestState;
import l2rt.util.Rnd;
import quests._252_ItSmellsDelicious._252_ItSmellsDelicious;

import java.util.Calendar;

public class _461_RumbleInTheBase extends Quest implements ScriptFile
{
	//NPC
	private static final int STAN = 30200;
	//MOB
	private static final int COOK = 18908;
	private static final int[] MOB = { 22780, 22782, 22784 };
	//Item
	private static final int fish = 15503;
	private static final int shoes = 16382;

	/*
	 * Reset time for Quest
	 * Default: 6:30AM on server time
	 */
	private static final int RESET_HOUR = 6;
	private static final int RESET_MIN = 30;

	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}

	public _461_RumbleInTheBase()
	{
		super(false);
		addStartNpc(STAN);
		for(int npcId : MOB)
			addKillId(npcId);
		addKillId(COOK);
		addQuestItem(fish, shoes);
	}

	@Override
	public String onEvent(String event, QuestState st, L2NpcInstance npc)
	{
		String htmltext = event;
		int npcId = npc.getNpcId();

		if(npcId != STAN)
			return event;

		if(event.equalsIgnoreCase("stan_q0461_05.htm"))
		{
			st.setState(STARTED);
			st.set("cond", "1");
			st.playSound(SOUND_ACCEPT);
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int id = st.getState();
		int cond = st.getInt("cond");

		if(npcId != STAN)
			return htmltext;

		if(id == CREATED)
		{
			String req = (st.getPlayer().getVar("NextQuest461") == null || st.getPlayer().getVar("NextQuest461").equalsIgnoreCase("null")) ? "0" : st.getPlayer().getVar("NextQuest461");
			if(Long.parseLong(req) > System.currentTimeMillis())
			{
				htmltext = "stan_q0461_03.htm";
			}
			else
			{
				QuestState qs = st.getPlayer().getQuestState(_252_ItSmellsDelicious.class);
				if(qs != null && qs.isCompleted() && st.getPlayer().getLevel() >= 82)
					htmltext = "stan_q0461_01.htm";
				else
				{
					htmltext = "stan_q0461_02.htm";
					st.exitCurrentQuest(true);
				}
			}
		}
		if(id == STARTED)
			if(cond == 1)
				htmltext = "stan_q0461_06.htm";
		if(cond == 2)
		{
			st.unset("cond");
			st.addExpAndSp(224784, 342528);
			st.playSound(SOUND_FINISH);
			Calendar reDo = Calendar.getInstance();
			reDo.set(Calendar.MINUTE, RESET_MIN);
			if(reDo.get(Calendar.HOUR_OF_DAY) >= RESET_HOUR)
				reDo.add(Calendar.DATE, 1);
			reDo.set(Calendar.HOUR_OF_DAY, RESET_HOUR);
			st.getPlayer().setVar("NextQuest461", "" + (reDo.getTimeInMillis()));
			st.setState(CREATED);
			st.takeItems(fish, -1);
			st.takeItems(shoes, -1);
			htmltext = "stan_q0461_07.htm";
		}
		if(id == COMPLETED)
			htmltext = "stan_q0252_03.htm";

		return htmltext;
	}

	@Override
	public String onKill(L2NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int id = st.getState();
		int cond = st.getInt("cond");
		if(id == STARTED)
		{
			if(cond == 1)
			{
				if((npcId == MOB[0] || npcId == MOB[1] || npcId == MOB[2]) && st.getQuestItemsCount(shoes) < 10 && Rnd.chance(10))
				{
					st.giveItems(shoes, 1, false);
					st.playSound(SOUND_ITEMGET);
				}
				if(npcId == COOK && st.getQuestItemsCount(fish) < 5 && Rnd.chance(5))
				{
					st.giveItems(fish, 1, false);
					st.playSound(SOUND_ITEMGET);
				}
				if(st.getQuestItemsCount(shoes) == 10 && st.getQuestItemsCount(fish) == 5)
				{
					st.set("cond", "2");
					st.playSound(SOUND_MIDDLE);
				}
			}
		}
		return null;
	}
}