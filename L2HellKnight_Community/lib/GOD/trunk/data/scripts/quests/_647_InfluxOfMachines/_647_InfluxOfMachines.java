package quests._647_InfluxOfMachines;

import l2rt.config.ConfigSystem;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.quest.Quest;
import l2rt.gameserver.model.quest.QuestState;
import l2rt.util.Rnd;

/**
 * Квест проверен и работает.
 * Рейты прописаны путем повышения шанса получения квестовых вещей.
 */
public class _647_InfluxOfMachines extends Quest implements ScriptFile
{
	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}

	// Settings: drop chance in %
	private static final int DROP_CHANCE = 60;

	// QUEST ITEMS
	private static final int DESTROYED_GOLEM_SHARD = 15521;

	// REWARDS
	private static final int[] RECIPES_60 = { 6881, 6883, 6885, 6887, 7580, 6891, 6893, 6895, 6897, 6899 };

	public _647_InfluxOfMachines()
	{
		super(true);
		addStartNpc(32069);
		for(int i = 22801; i < 22812; i++) 
			addKillId(i);
		addQuestItem(DESTROYED_GOLEM_SHARD);
	}

	@Override
	public String onEvent(String event, QuestState st, L2NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("quest_accept"))
		{
			htmltext = "collecter_gutenhagen_q0647_0103.htm";
			st.set("cond", "1");
			st.setState(STARTED);
			st.playSound(SOUND_ACCEPT);
		}
		else if(event.equalsIgnoreCase("647_3"))
			if(st.getQuestItemsCount(DESTROYED_GOLEM_SHARD) >= 500)
			{
				st.takeItems(DESTROYED_GOLEM_SHARD, 500);
				st.giveItems(RECIPES_60[Rnd.get(RECIPES_60.length)], 1);
				st.playSound(SOUND_FINISH);
				st.exitCurrentQuest(true);
				htmltext = "collecter_gutenhagen_q0647_0201.htm";
			}
			else
				htmltext = "collecter_gutenhagen_q0647_0106.htm";
		return htmltext;
	}

	@Override
	public String onTalk(L2NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int cond = st.getInt("cond");
		long count = st.getQuestItemsCount(DESTROYED_GOLEM_SHARD);
		if(cond == 0)
			if(st.getPlayer().getLevel() >= 70)
				htmltext = "collecter_gutenhagen_q0647_0101.htm";
			else
			{
				htmltext = "collecter_gutenhagen_q0647_0102.htm";
				st.exitCurrentQuest(true);
			}
		else if(cond == 1 && count < 500)
			htmltext = "collecter_gutenhagen_q0647_0106.htm";
		else if(cond == 2 && count >= 500)
			htmltext = "collecter_gutenhagen_q0647_0105.htm";
		return htmltext;
	}

	@Override
	public String onKill(L2NpcInstance npc, QuestState st)
	{
		long count = st.getQuestItemsCount(DESTROYED_GOLEM_SHARD);
		
		st.rollAndGive(DESTROYED_GOLEM_SHARD, ConfigSystem.getInt("RateQuestsDrop"), ConfigSystem.getInt("RateQuestsDrop"), 500, 80);
		st.playSound(SOUND_ITEMGET);
		if(count == 500)
			st.set("cond", "2");
			
		return null;
	}
}