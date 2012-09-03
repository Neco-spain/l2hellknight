package quests._631_DeliciousTopChoiceMeat;

import l2rt.config.ConfigSystem;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.quest.Quest;
import l2rt.gameserver.model.quest.QuestState;
import l2rt.util.Rnd;

public class _631_DeliciousTopChoiceMeat extends Quest implements ScriptFile
{
	//NPC
	public final int TUNATUN = 31537;
	//MOBS
	public final int MOB_LIST[] = { 21460, 21461, 21462, 21463, 21464, 21465, 21466, 21467, 21468, 21469, //Alpen Kookaburra
			21479, 21480, 21481, 21482, 21483, 21484, 21485, 21486, 21487, 21488, //Alpen Buffalo
			21498, 21499, 21500, 21501, 21502, 21503, 21504, 21505, 21506, 21507 };//Alpen Cougar
	//ITEMS
	public final int TOP_QUALITY_MEAT = 7546;
	//REWARDS
	public final int MOLD_GLUE = 4039;
	public final int MOLD_LUBRICANT = 4040;
	public final int MOLD_HARDENER = 4041;
	public final int ENRIA = 4042;
	public final int ASOFE = 4043;
	public final int THONS = 4044;
	public final int[][] REWARDS = { { 1, MOLD_GLUE, 15 }, { 2, ASOFE, 15 }, { 3, THONS, 15 }, { 4, MOLD_LUBRICANT, 10 },
			{ 5, ENRIA, 10 }, { 6, MOLD_HARDENER, 5 } };

	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}

	public _631_DeliciousTopChoiceMeat()
	{
		super(false);

		addStartNpc(TUNATUN);

		addTalkId(TUNATUN);

		for(int i : MOB_LIST)
			addKillId(i);

		addQuestItem(TOP_QUALITY_MEAT);
	}

	@Override
	public String onEvent(String event, QuestState st, L2NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("beast_herder_tunatun_q0631_0104.htm"))
		{
			st.set("cond", "1");
			st.setState(STARTED);
			st.playSound(SOUND_ACCEPT);
		}
		else if(event.equalsIgnoreCase("beast_herder_tunatun_q0631_0201.htm") && st.getQuestItemsCount(TOP_QUALITY_MEAT) >= 120)
			st.set("cond", "3");
		for(int[] element : REWARDS)
			if(event.equalsIgnoreCase(String.valueOf(element[0])))
				if(st.getInt("cond") == 3 && st.getQuestItemsCount(TOP_QUALITY_MEAT) >= 120)
				{
					htmltext = "beast_herder_tunatun_q0631_0202.htm";
					st.takeItems(TOP_QUALITY_MEAT, -1);
					st.giveItems(element[1], Math.round(element[2] * st.getRateQuestsReward()));
					st.playSound(SOUND_FINISH);
					st.exitCurrentQuest(true);
				}
				else
				{
					htmltext = "beast_herder_tunatun_q0631_0203.htm";
					st.set("cond", "1");
				}
		return htmltext;
	}

	@Override
	public String onTalk(L2NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int cond = st.getInt("cond");
		if(cond < 1)
		{
			if(st.getPlayer().getLevel() < 65)
			{
				htmltext = "beast_herder_tunatun_q0631_0103.htm";
				st.exitCurrentQuest(true);
			}
			else
				htmltext = "beast_herder_tunatun_q0631_0101.htm";
		}
		else if(cond == 1)
			htmltext = "beast_herder_tunatun_q0631_0106.htm";
		else if(cond == 2)
		{
			if(st.getQuestItemsCount(TOP_QUALITY_MEAT) < 120)
			{
				htmltext = "beast_herder_tunatun_q0631_0106.htm";
				st.set("cond", "1");
			}
			else
				htmltext = "beast_herder_tunatun_q0631_0105.htm";
		}
		else if(cond == 3)
			htmltext = "beast_herder_tunatun_q0631_0201.htm";
		return htmltext;
	}

	@Override
	public String onKill(L2NpcInstance npc, QuestState st)
	{
		if(st.getInt("cond") == 1 && Rnd.chance(80))
		{
			st.giveItems(TOP_QUALITY_MEAT, ConfigSystem.getInt("RateQuestsDrop"));
			if(st.getQuestItemsCount(TOP_QUALITY_MEAT) < 120)
				st.playSound(SOUND_ITEMGET);
			else
			{
				st.playSound(SOUND_MIDDLE);
				st.set("cond", "2");
			}
		}
		return null;
	}
}