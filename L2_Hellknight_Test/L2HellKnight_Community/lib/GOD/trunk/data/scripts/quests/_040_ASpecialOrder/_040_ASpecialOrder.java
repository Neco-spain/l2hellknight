package quests._040_ASpecialOrder;

import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.quest.Quest;
import l2rt.gameserver.model.quest.QuestState;
import l2rt.util.Rnd;

public class _040_ASpecialOrder extends Quest implements ScriptFile
{
	// NPC
	static final int HELVETIA = 30081;
	static final int OFULLE = 31572;
	static final int GESTO = 30511;

	// Items
	static final int OrangeNimbleFish = 6450;
	static final int OrangeUglyFish = 6451;
	static final int OrangeFatFish = 6452;
	static final int FishChest = 12764;
	static final int GoldenCobol = 5079;
	static final int ThornCobol = 5082;
	static final int GreatCobol = 5084;
	static final int SeedJar = 12765;
	static final int WondrousCubic = 10632;

	public CheckStatus LAST_CHECK_STATUS = CheckStatus.GRACIA_FINAL;

	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}

	public _040_ASpecialOrder()
	{
		super(false);
		addStartNpc(HELVETIA);

		addTalkId(HELVETIA);
		addTalkId(OFULLE);
		addTalkId(GESTO);

		addQuestItem(FishChest);
		addQuestItem(SeedJar);
	}

	@Override
	public String onEvent(String event, QuestState st, L2NpcInstance npc)
	{
		String htmltext = event;
		if(event.equals("30081-02.htm"))
		{
			st.set("cond", "1");
			int condition = Rnd.get(1, 2);
			if(condition == 1)
			{
				st.set("cond", "2");
				htmltext = "30081-02a.htm";
			}
			else
			{
				st.set("cond", "5");
				htmltext = "30081-02b.htm";
			}
			st.setState(STARTED);
			st.playSound(SOUND_ACCEPT);
		}
		else if(event.equals("30511-03.htm"))
		{
			st.set("cond", "6");
			st.playSound(SOUND_MIDDLE);
		}
		else if(event.equals("31572-03.htm"))
		{
			st.set("cond", "3");
			st.playSound(SOUND_MIDDLE);
		}
		else if(event.equals("30081-05a.htm"))
		{
			st.takeItems(FishChest, 1);
			st.giveItems(WondrousCubic, 1);
			st.playSound(SOUND_FINISH);
			st.exitCurrentQuest(false);
		}
		else if(event.equals("30081-05b.htm"))
		{
			st.takeItems(SeedJar, 1);
			st.giveItems(WondrousCubic, 1);
			st.playSound(SOUND_FINISH);
			st.exitCurrentQuest(false);
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getInt("cond");
		if(npcId == HELVETIA)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 40)
					htmltext = "30081-01.htm";
				else
				{
					htmltext = "30081-00.htm";
					st.exitCurrentQuest(true);
				}
			}
			else if(cond == 2 || cond == 3)
				htmltext = "30081-03a.htm";
			else if(cond == 4)
				htmltext = "30081-04a.htm";
			else if(cond == 5 || cond == 6)
				htmltext = "30081-03b.htm";
			else if(cond == 7)
				htmltext = "30081-04b.htm";
		}
		else if(npcId == OFULLE)
		{
			if(cond == 2)
				htmltext = "31572-01.htm";
			else if(cond == 3)
			{
				if(st.getQuestItemsCount(OrangeNimbleFish) >= 10 && st.getQuestItemsCount(OrangeUglyFish) >= 10 && st.getQuestItemsCount(OrangeFatFish) >= 10)
				{
					st.takeItems(OrangeNimbleFish, 10);
					st.takeItems(OrangeUglyFish, 10);
					st.takeItems(OrangeFatFish, 10);
					st.giveItems(FishChest, 1, false);
					st.set("cond", "4");
					st.playSound(SOUND_MIDDLE);
					htmltext = "31572-04.htm";
				}
				else
					htmltext = "31572-05.htm";
			}
			else if(cond == 4)
				htmltext = "31572-06.htm";
		}
		else if(npcId == GESTO)
		{
			if(cond == 5)
				htmltext = "30511-01.htm";
			else if(cond == 6)
			{
				if(st.getQuestItemsCount(GoldenCobol) >= 40 && st.getQuestItemsCount(ThornCobol) >= 40 && st.getQuestItemsCount(GreatCobol) >= 40)
				{
					st.set("cond", "7");
					st.takeItems(GoldenCobol, 40);
					st.takeItems(ThornCobol, 40);
					st.takeItems(GreatCobol, 40);
					st.playSound(SOUND_MIDDLE);
					st.giveItems(SeedJar, 1);
					htmltext = "30511-04.htm";
				}
				else
					htmltext = "30511-05.htm";
			}
			else if(cond == 7)
				htmltext = "30511-06.htm";
		}
		return htmltext;
	}
}