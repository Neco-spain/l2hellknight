package quests._453_NotStrongEnoughAlone;

import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.quest.Quest;
import l2rt.gameserver.model.quest.QuestState;
import l2rt.util.Rnd;
import quests._10282_ToTheSeedOfAnnihilation._10282_ToTheSeedOfAnnihilation;

import java.util.Calendar;

public class _453_NotStrongEnoughAlone extends Quest implements ScriptFile
{
	//NPC
	private static final int KLEMIS = 32734;
	//MOB
	private static final int[] BISTAKON = { 22750, 22746, 22747, 22751, 22748, 22752 };
	private static final int[] REPTILIKON = { 22754, 22757, 22755, 22758, 22756, 22759 };
	private static final int[] KOKRAKON = { 22760, 22763, 22761, 22764, 22762, 22765 };
	//ITEM
	private static final int[] REWARD_REC = { 15815, 15816, 15817, 15818, 15819, 15820, 15821, 15822, 15823, 15824, 15825 };
	private static final int[] REWARD_PIECE = { 15634, 15635, 15636, 15637, 15638, 15639, 15640, 15641, 15642, 15643,
			15644 };
	private static final int[][] REWARD = { REWARD_REC, REWARD_PIECE };
	/*
	 * Reset time for Quest
	 * Default: 6:30AM on server time
	 */
	private static final int RESET_HOUR = 6;
	private static final int RESET_MIN = 30;

	public _453_NotStrongEnoughAlone()
	{
		super(false);
		addStartNpc(KLEMIS);
		for(int i : BISTAKON)
			addKillId(i);
		for(int i : REPTILIKON)
			addKillId(i);
		for(int i : KOKRAKON)
			addKillId(i);
	}

	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}

	@Override
	public String onEvent(String event, QuestState st, L2NpcInstance npc)
	{
		String htmltext = event;
		int npcId = npc.getNpcId();

		if(npcId != KLEMIS)
			return event;

		if(event.equalsIgnoreCase("clemis_q0453_06.htm"))
		{
			st.setState(STARTED);
			st.set("cond", "1");
			st.playSound(SOUND_ACCEPT);
		}

		if(event.equalsIgnoreCase("clemis_q0453_07.htm")) //Bistakon
		{
			st.set("cond", "2");
			st.playSound(SOUND_MIDDLE);
		}

		if(event.equalsIgnoreCase("clemis_q0453_08.htm")) // Reptilikon
		{
			st.set("cond", "3");
			st.playSound(SOUND_MIDDLE);
		}

		if(event.equalsIgnoreCase("clemis_q0453_09.htm")) //Cokrakon
		{
			st.set("cond", "4");
			st.playSound(SOUND_MIDDLE);
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

		if(npcId != KLEMIS)
			return htmltext;

		if(id == CREATED)
		{
			String req = (st.getPlayer().getVar("NextQuest453") == null || st.getPlayer().getVar("NextQuest453").equalsIgnoreCase("null")) ? "0" : st.getPlayer().getVar("NextQuest453");
			if(Long.parseLong(req) > System.currentTimeMillis())
				return "clemis_q0453_02.htm";
			else
			{
				QuestState qs = st.getPlayer().getQuestState(_10282_ToTheSeedOfAnnihilation.class);
				if(qs != null && qs.isCompleted() && st.getPlayer().getLevel() >= 84)
					htmltext = "clemis_q0453_04.htm";
				else
				{
					htmltext = "clemis_q0453_03.htm";
					st.exitCurrentQuest(true);
				}
			}
		}
		if(id == STARTED)
			if(cond == 1) // Select zone for farm
				htmltext = "clemis_q0453_06.htm";
		if(cond == 2) //Bistakon
			htmltext = "clemis_q0453_11.htm";
		if(cond == 3) //Reptilikon
			htmltext = "clemis_q0453_12.htm";
		if(cond == 4) //Cokrakon
			htmltext = "clemis_q0453_13.htm";
		if(cond == 5) //complete
		{
			int i = Rnd.get(REWARD.length);
			if(i == 0)
			{
				int j = Rnd.get(REWARD_REC.length);
				st.giveItems(REWARD_REC[j], 1);
			}
			else if(i == 1)
			{
				int k = Rnd.get(REWARD_PIECE.length);
				st.giveItems(REWARD_PIECE[k], 5);
			}
			Calendar reDo = Calendar.getInstance();
			reDo.set(Calendar.MINUTE, RESET_MIN);
			if(reDo.get(Calendar.HOUR_OF_DAY) >= RESET_HOUR)
				reDo.add(Calendar.DATE, 1);
			reDo.set(Calendar.HOUR_OF_DAY, RESET_HOUR);
			st.getPlayer().setVar("NextQuest453", "" + (reDo.getTimeInMillis()));
			st.unset("cond");
			st.unset("kill");
			st.unset("kill1");
			st.unset("kill2");
			st.setState(CREATED);
			htmltext = "clemis_q0453_14.htm";
		}

		return htmltext;
	}

	@Override
	public String onKill(L2NpcInstance npc, QuestState st)
	{
		int id = st.getState();
		int cond = st.getInt("cond");
		int npcId = npc.getNpcId();
		int kill = st.getInt("kill");
		int kill1 = st.getInt("kill1");
		int kill2 = st.getInt("kill2");
		if(id == STARTED)
		{
			if(cond == 2)
			{
				if(npcId == BISTAKON[0] || npcId == BISTAKON[1])
					if(kill < 15)
					{
						kill++;
						st.set("kill", kill);
					}
				if(npcId == BISTAKON[2] || npcId == BISTAKON[3])
					if(kill1 < 15)
					{
						kill1++;
						st.set("kill1", kill1);
					}
				if(npcId == BISTAKON[4] || npcId == BISTAKON[5])
					if(kill2 < 15)
					{
						kill2++;
						st.set("kill2", kill2);
					}
				if(kill == 15 && kill1 == 15 && kill2 == 15)
				{
					st.set("cond", "5");
					st.playSound(SOUND_MIDDLE);
				}
			}
			if(cond == 3)
			{
				if(npcId == REPTILIKON[0] || npcId == REPTILIKON[1])
					if(kill < 20)
					{
						kill++;
						st.set("kill", kill);
					}
				if(npcId == REPTILIKON[2] || npcId == REPTILIKON[3])
					if(kill1 < 20)
					{
						kill1++;
						st.set("kill1", kill1);
					}
				if(npcId == REPTILIKON[4] || npcId == REPTILIKON[5])
					if(kill2 < 20)
					{
						kill2++;
						st.set("kill2", kill2);
					}
				if(kill == 20 && kill1 == 20 && kill2 == 20)
				{
					st.set("cond", "5");
					st.playSound(SOUND_MIDDLE);
				}
			}
			if(cond == 4)
			{
				if(npcId == KOKRAKON[0] || npcId == KOKRAKON[1])
					if(kill < 20)
					{
						kill++;
						st.set("kill", kill);
					}
				if(npcId == KOKRAKON[2] || npcId == KOKRAKON[3])
					if(kill1 < 20)
					{
						kill1++;
						st.set("kill1", kill1);
					}
				if(npcId == KOKRAKON[4] || npcId == KOKRAKON[5])
					if(kill2 < 20)
					{
						kill2++;
						st.set("kill2", kill2);
					}
				if(kill == 20 && kill1 == 20 && kill2 == 20)
				{
					st.set("cond", "5");
					st.playSound(SOUND_MIDDLE);
				}
			}
		}
		return null;
	}
}