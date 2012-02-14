package quests._369_CollectorOfJewels;

import javolution.util.FastMap;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.quest.Quest;
import l2rt.gameserver.model.quest.QuestState;

public class _369_CollectorOfJewels extends Quest implements ScriptFile
{
	// NPCs
	private static int NELL = 30376;
	// Mobs
	private static int Roxide = 20747;
	private static int Rowin_Undine = 20619;
	private static int Lakin_Undine = 20616;
	private static int Salamander_Rowin = 20612;
	private static int Lakin_Salamander = 20609;
	private static int Death_Fire = 20749;
	// Quest Items
	private static int FLARE_SHARD = 5882;
	private static int FREEZING_SHARD = 5883;

	private final FastMap<Integer, int[]> DROPLIST = new FastMap<Integer, int[]>();

	public _369_CollectorOfJewels()
	{
		super(false);
		addStartNpc(NELL);
		addKillId(Roxide);
		addKillId(Rowin_Undine);
		addKillId(Lakin_Undine);
		addKillId(Salamander_Rowin);
		addKillId(Lakin_Salamander);
		addKillId(Death_Fire);
		addQuestItem(FLARE_SHARD);
		addQuestItem(FREEZING_SHARD);

		DROPLIST.put(Roxide, new int[] { FREEZING_SHARD, 85 });
		DROPLIST.put(Rowin_Undine, new int[] { FREEZING_SHARD, 73 });
		DROPLIST.put(Lakin_Undine, new int[] { FREEZING_SHARD, 60 });
		DROPLIST.put(Salamander_Rowin, new int[] { FLARE_SHARD, 77 });
		DROPLIST.put(Lakin_Salamander, new int[] { FLARE_SHARD, 77 });
		DROPLIST.put(Death_Fire, new int[] { FLARE_SHARD, 85 });
	}

	@Override
	public String onEvent(String event, QuestState st, L2NpcInstance npc)
	{
		if(event.equalsIgnoreCase("30376-03.htm") && st.getState() == CREATED)
		{
			st.setState(STARTED);
			st.set("cond", "1");
			st.playSound(SOUND_ACCEPT);
		}
		else if(event.equalsIgnoreCase("30376-08.htm") && st.getState() == STARTED)
		{
			st.playSound(SOUND_FINISH);
			st.exitCurrentQuest(true);
		}
		return event;
	}

	@Override
	public String onTalk(L2NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		if(npc.getNpcId() != NELL)
			return htmltext;
		int _state = st.getState();

		if(_state == CREATED)
		{
			if(st.getPlayer().getLevel() >= 25)
			{
				st.set("cond", "0");
				return "30376-02.htm";
			}
			st.exitCurrentQuest(true);
			return "30376-01.htm";
		}

		if(_state != STARTED)
			return htmltext;
		int cond = st.getInt("cond");
		if(cond == 1)
			htmltext = "30376-04.htm";
		else if(cond == 3)
			htmltext = "30376-09.htm";
		else if(cond == 2 || cond == 4)
		{
			int max_count = cond == 2 ? 50 : 200;
			long FLARE_SHARD_COUNT = st.getQuestItemsCount(FLARE_SHARD);
			long FREEZING_SHARD_COUNT = st.getQuestItemsCount(FREEZING_SHARD);
			if(FLARE_SHARD_COUNT != max_count || FREEZING_SHARD_COUNT != max_count)
			{
				st.set("cond", String.valueOf(cond - 1));
				return onTalk(npc, st);
			}

			st.takeItems(FLARE_SHARD, -1);
			st.takeItems(FREEZING_SHARD, -1);
			if(cond == 2)
			{
				htmltext = "30376-05.htm";
				st.giveItems(ADENA_ID, 12500);
				st.playSound(SOUND_MIDDLE);
				st.set("cond", "3");
			}
			else
			{
				htmltext = "30376-10.htm";
				st.giveItems(ADENA_ID, 63500);
				st.playSound(SOUND_FINISH);
				st.exitCurrentQuest(true);
			}
		}

		return htmltext;
	}

	@Override
	public String onKill(L2NpcInstance npc, QuestState qs)
	{
		int cond = qs.getCond();
		if(cond != 1 && cond != 3)
			return null;

		int[] drop = DROPLIST.get(npc.getNpcId());
		if(drop == null)
			return null;

		int max_count = cond == 1 ? 50 : 200;
		if(qs.getQuestItemsCount(drop[0]) < max_count && qs.rollAndGive(drop[0], 1, 1, max_count, drop[1]) && qs.getQuestItemsCount(FLARE_SHARD) >= max_count && qs.getQuestItemsCount(FREEZING_SHARD) >= max_count)
			qs.setCond(cond == 1 ? 2 : 4);

		return null;
	}

	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}
}