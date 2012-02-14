package quests._375_WhisperOfDreams2;

import l2rt.Config;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.quest.Quest;
import l2rt.gameserver.model.quest.QuestState;
import l2rt.util.Rnd;

public class _375_WhisperOfDreams2 extends Quest implements ScriptFile
{
	//NPCs
	private static final int MANAKIA = 30515;
	//Quest items
	private static final int MSTONE = 5887;
	private static final int K_HORN = 5888;
	private static final int CH_SKULL = 5889;
	// Items chance drop
	private static final int DROP_CHANCE_ITEMS = 20;
	//Quest collections
	private static final int[] REWARDS = { 5348, 5352, 5350 };
	private static final int[] REWARDS2 = { 5349, 5353, 5351 };

	//Mobs & Drop
	private static final int CAVEHOWLER = 20624;
	private static final int KARIK = 20629;

	//Messages
	private static final String _default = "noquest";

	public _375_WhisperOfDreams2()
	{
		super(true);

		addStartNpc(MANAKIA);
		addKillId(new int[] { CAVEHOWLER, KARIK });
		addQuestItem(new int[] { K_HORN, CH_SKULL });
	}

	@Override
	public String onEvent(String event, QuestState st, L2NpcInstance npc)
	{
		if(event.equalsIgnoreCase("30515-6.htm"))
		{
			st.setState(STARTED);
			st.set("cond", "1");
			st.playSound(SOUND_ACCEPT);
		}
		else if(event.equalsIgnoreCase("30515-7.htm"))
		{
			st.playSound(SOUND_FINISH);
			st.exitCurrentQuest(true);
		}
		else if(event.equalsIgnoreCase("30515-8.htm"))
		{}
		return event;
	}

	@Override
	public String onTalk(L2NpcInstance npc, QuestState st)
	{
		String htmltext = _default;
		int id = st.getState();
		if(id == CREATED)
		{
			htmltext = "30515-1.htm";
			if(st.getPlayer().getLevel() < 60)
			{
				htmltext = "30515-2.htm";
				st.exitCurrentQuest(true);
			}
			else if(st.getQuestItemsCount(MSTONE) < 1)
			{
				htmltext = "30515-3.htm";
				st.exitCurrentQuest(true);
			}
		}
		else if(id == STARTED)
			if(st.getQuestItemsCount(CH_SKULL) == 100 && st.getQuestItemsCount(K_HORN) == 100)
			{
				st.takeItems(CH_SKULL, -1);
				st.takeItems(K_HORN, -1);
				int item = Config.ALT_100_RECIPES_A ? REWARDS2[Rnd.get(REWARDS2.length)] : REWARDS[Rnd.get(REWARDS.length)];
				st.giveItems(item, 1);
				htmltext = "30515-4.htm";
			}
			else
				htmltext = "30515-5.htm";
		return htmltext;
	}

	@Override
	public String onKill(L2NpcInstance npc, QuestState st)
	{
		int npcid = npc.getNpcId();
		if(npcid == KARIK)
			st.rollAndGive(K_HORN, 1, 1, 100, DROP_CHANCE_ITEMS);
		else if(npcid == CAVEHOWLER)
			st.rollAndGive(CH_SKULL, 1, 1, 100, DROP_CHANCE_ITEMS);
		return null;
	}

	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}
}