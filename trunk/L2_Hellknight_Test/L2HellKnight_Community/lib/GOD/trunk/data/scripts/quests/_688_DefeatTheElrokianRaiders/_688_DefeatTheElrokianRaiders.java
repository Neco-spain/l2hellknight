package quests._688_DefeatTheElrokianRaiders;

import l2rt.Config;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.quest.Quest;
import l2rt.gameserver.model.quest.QuestState;
import l2rt.util.Rnd;

public class _688_DefeatTheElrokianRaiders extends Quest implements ScriptFile
{
	// NPC
	private static int DINN = 32105;
	// MOB
	private static int Elroki = 22214;
	// Settings: drop chance in %
	private static int DROP_CHANCE = 50;

	private static int ADENA = 57;
	private static int DINOSAUR_FANG_NECKLACE = 8785;

	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}

	public _688_DefeatTheElrokianRaiders()
	{
		super(false);

		addStartNpc(DINN);
		addTalkId(DINN);
		addKillId(Elroki);
		addQuestItem(DINOSAUR_FANG_NECKLACE);
	}

	@Override
	public String onEvent(String event, QuestState st, L2NpcInstance npc)
	{
		String htmltext = event;
		long count = st.getQuestItemsCount(DINOSAUR_FANG_NECKLACE);
		if(event.equalsIgnoreCase("32105-04.htm"))
		{
			st.set("cond", "1");
			st.setState(STARTED);
			st.playSound(SOUND_ACCEPT);
		}
		else if(event.equalsIgnoreCase("Reward1"))
		{
			if(count > 0)
				htmltext = "32105-07.htm";
			else
				htmltext = "32105-06.htm";
		}
		else if(event.equalsIgnoreCase("Reward2"))
		{
			if(count > 99)
				htmltext = "32105-12.htm";
			else
				htmltext = "32105-11.htm";
		}
		else if(event.equalsIgnoreCase("Quit"))
		{
			if(count == 100)
			{
				st.takeItems(DINOSAUR_FANG_NECKLACE, 100);
				st.giveItems(ADENA, 450000);
			}
			else if(count > 100)
			{
				st.takeItems(DINOSAUR_FANG_NECKLACE, -1);
				st.giveItems(ADENA, ((count - 100) * 3000) + 450000);
			}
			st.playSound(SOUND_FINISH);
			st.exitCurrentQuest(true);
			return null;
		}
		else if(event.equalsIgnoreCase("32105-08.htm"))
		{
			if(count > 0)
			{
				st.takeItems(DINOSAUR_FANG_NECKLACE, -1);
				st.giveItems(ADENA, count * 3000);
			}
			st.playSound(SOUND_FINISH);
			st.exitCurrentQuest(true);
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int cond = st.getInt("cond");
		long count = st.getQuestItemsCount(DINOSAUR_FANG_NECKLACE);
		if(cond == 0)
		{
			if(st.getPlayer().getLevel() >= 75)
				htmltext = "32105-01.htm";
			else
			{
				htmltext = "32105-02.htm";
				st.exitCurrentQuest(true);
			}
		}
		else if(cond == 1)
		{
			if(count == 0)
				htmltext = "32105-06.htm";
			else
				htmltext = "32105-05.htm";
		}
		return htmltext;
	}

	@Override
	public String onKill(L2NpcInstance npc, QuestState st)
	{
		long count = st.getQuestItemsCount(DINOSAUR_FANG_NECKLACE);
		if(st.getInt("cond") == 1 && count < 100 && Rnd.chance(DROP_CHANCE))
		{
			long numItems = (int) Config.RATE_QUESTS_REWARD;
			if(count + numItems > 100)
				numItems = 100 - count;
			if(count + numItems >= 100)
				st.playSound(SOUND_MIDDLE);
			else
				st.playSound(SOUND_ITEMGET);
			st.giveItems(DINOSAUR_FANG_NECKLACE, numItems);
		}
		return null;
	}
}