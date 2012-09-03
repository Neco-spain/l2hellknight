package quests._377_GiantsExploration2;

import java.io.File;

import l2rt.Config;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.model.L2Multisell;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.quest.Quest;
import l2rt.gameserver.model.quest.QuestState;
import l2rt.util.Rnd;

public class _377_GiantsExploration2 extends Quest implements ScriptFile
{
	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}

	// Titan Ancient Books drop rate in %
	private static final int DROP_RATE = 20;

	// Quest items
	private static final int ANC_BOOK_OLD = 5955;
	private static final int ANC_BOOK_NEW = 14847;
	private static final int DICT2 = 5892;

	// Quest collections
	private static final int[][] EXCHANGE = { { 5945, 5946, 5947, 5948, 5949 }, //science basis
			{ 5950, 5951, 5952, 5953, 5954 } //culture
	};

	// NPCs
	private static final int HR_SOBLING = 31147;

	// Mobs
	private static final int[] MOBS = {
	// список мобов для квеста
			20654, // Lesser Giant Soldier L62 
			20655, // Lesser Giant Shooter L63
			20656, // Lesser Giant Scout L63
			20657, // Lesser Giant Mage L64
			20658, // Lesser Giant Elder L65
			20771, // Barif L64 
			20772, // Barif's Pet L61
			22661, // Lesser Giant Soldier L81 
			22662, // Lesser Giant Shooter L82
			22663, // Lesser Giant Scout L82
			22664, // Lesser Giant Mage L82
			22665, // Lesser Giant Elder L82
			22666, // Barif L82 
			22667, // Barif's Pet L81
			22668, // Gamlin L81 
			22669, // Leogul L82
	};

	public _377_GiantsExploration2()
	{
		super(true);
		addStartNpc(HR_SOBLING);
		addKillId(MOBS);
	}

	@Override
	public String onEvent(String event, QuestState st, L2NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("yes"))
		{
			htmltext = "Starting.htm";
			st.setState(STARTED);
			st.setCond(1);
			st.playSound(SOUND_ACCEPT);
		}
		else if(event.equalsIgnoreCase("0"))
		{
			htmltext = "ext_msg.htm";
			st.playSound(SOUND_FINISH);
			st.exitCurrentQuest(true);
		}
		else if(event.equalsIgnoreCase("show"))
		{
			htmltext = "no_items.htm";
			for(int[] i : EXCHANGE)
			{
				long count = Long.MAX_VALUE;
				for(int j : i)
					count = Math.min(count, st.getQuestItemsCount(j));
				if(count > 0)
				{
					htmltext = "tnx4items.htm";
					for(int j : i)
						st.takeItems(j, count);
					for(int n = 0; n < count; n++)
					{
						int luck = Rnd.get(100);
						int item = 0;
						if(luck > 75)
							item = 5420; // nightmare leather 60%
						else if(luck > 50)
							item = 5422; // majestic plate 60%
						else if(luck > 25)
							item = 5336; // nightmare armor 60%
						else
							item = 5338; // majestic leather 60%
						if(Config.ALT_100_RECIPES_A)
							item += 1;
						st.giveItems(item, 1);
					}
				}
			}
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int id = st.getState();
		if(st.getQuestItemsCount(DICT2) == 0)
			st.exitCurrentQuest(true);
		else if(id == CREATED)
		{
			htmltext = "start.htm";
			if(st.getPlayer().getLevel() < 57)
			{
				st.exitCurrentQuest(true);
				htmltext = "error_1.htm";
			}
		}
		else if(id == STARTED)
			if(st.getQuestItemsCount(ANC_BOOK_OLD) != 0 || st.getQuestItemsCount(ANC_BOOK_NEW) != 0)
				htmltext = "checkout.htm";
			else
				htmltext = "checkout2.htm";
		return htmltext;
	}

	@Override
	public String onKill(L2NpcInstance npc, QuestState st)
	{
		if(st.getCond() == 1)
			if(npc.getLevel() < 80)
				st.rollAndGive(ANC_BOOK_OLD, 1, DROP_RATE * npc.getLevel() / 60.);
			else
				st.rollAndGive(ANC_BOOK_NEW, 1, DROP_RATE);
		return null;
	}

	private static void loadMultiSell()
	{
		L2Multisell.getInstance().parseFile(new File(Config.DATAPACK_ROOT, "data/scripts/quests/_377_GiantsExploration2/311472.xml"));
	}

	public static void OnReloadMultiSell()
	{
		loadMultiSell();
	}

	static
	{
		loadMultiSell();
	}
}