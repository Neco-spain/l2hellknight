package quests._125_InTheNameOfEvilPart1;

import l2rt.Config;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.quest.Quest;
import l2rt.gameserver.model.quest.QuestState;
import l2rt.util.Rnd;

public class _125_InTheNameOfEvilPart1 extends Quest implements ScriptFile
{
	// NPC
	private final int Mushika = 32114;
	private final int Karakawei = 32117;
	private final int UluKaimu = 32119;
	private final int BaluKaimu = 32120;
	private final int ChutaKaimu = 32121;
	
	// QUEST ITEMS
	private final int GAZKHFRAG = 8782;
	private final int EPITAPH = 8781;
	private final int OrClaw = 8779;
	private final int DienBone = 8780;

	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}

	public _125_InTheNameOfEvilPart1()
	{
		super(false);

		addStartNpc(Mushika);
		addTalkId(Karakawei);
		addTalkId(UluKaimu);
		addTalkId(BaluKaimu);
		addTalkId(ChutaKaimu);
		addKillId(22200);
		addKillId(22201);
		addKillId(22202);
		addKillId(22203);
		addKillId(22204);
		addKillId(22205);
		addKillId(22219);
		addKillId(22220);
		addKillId(22224);
		addKillId(22224);
		addQuestItem(OrClaw);
		addQuestItem(DienBone);
	}

	private String getWordText32119(QuestState st)
	{
		String htmltext = "32119-04.htm";
		if(st.getInt("T32119") > 0 && st.getInt("E32119") > 0 && st.getInt("P32119") > 0 && st.getInt("U32119") > 0)
			htmltext = "32119-09.htm";
		return htmltext;
	}

	private String getWordText32120(QuestState st)
	{
		String htmltext = "32120-04.htm";
		if(st.getInt("T32120") > 0 && st.getInt("O32120") > 0 && st.getInt("O32120_2") > 0 && st.getInt("N32120") > 0)
			htmltext = "32120-09.htm";
		return htmltext;
	}

	private String getWordText32121(QuestState st)
	{
		String htmltext = "32121-04.htm";
		if(st.getInt("W32121") > 0 && st.getInt("A32121") > 0 && st.getInt("G32121") > 0 && st.getInt("U32121") > 0)
			htmltext = "32121-09.htm";
		return htmltext;
	}

	@Override
	public String onEvent(String event, QuestState st, L2NpcInstance npc)
	{
		String htmltext = event;

		if(event.equalsIgnoreCase("32114-05.htm"))
		{
			st.set("cond", "1");
			st.setState(STARTED);
			st.playSound(SOUND_ACCEPT);
		}
		if(event.equalsIgnoreCase("32114-12.htm"))
		{
			st.giveItems(GAZKHFRAG, 1);
			st.set("cond", "2");
			st.playSound(SOUND_MIDDLE);
		}
		if(event.equalsIgnoreCase("32114-13.htm"))
		{
			st.set("cond", "2");
			st.playSound(SOUND_MIDDLE);
		}
		if(event.equalsIgnoreCase("32117-08.htm"))
		{
			st.set("cond", "3");
			st.playSound(SOUND_MIDDLE);
		}
		if(event.equalsIgnoreCase("32117-16.htm"))
		{
			st.set("cond", "5");
			st.playSound(SOUND_MIDDLE);
		}
		if(event.equalsIgnoreCase("32119-20.htm"))
		{
			st.set("cond", "6");
			st.playSound(SOUND_MIDDLE);
		}
		if(event.equalsIgnoreCase("32120-19.htm"))
		{
			st.set("cond", "7");
			st.playSound(SOUND_MIDDLE);
		}
		if(event.equalsIgnoreCase("32121-23.htm"))
		{
			st.giveItems(EPITAPH, 1);
			st.set("cond", "8");
			st.playSound(SOUND_MIDDLE);
		}

		if(event.equalsIgnoreCase("T32119"))
		{
			htmltext = "32119-05.htm";
			if(st.getInt("T32119") < 1)
				st.set("T32119", "1");
		}
		else if(event.equalsIgnoreCase("E32119"))
		{
			htmltext = "32119-06.htm";
			if(st.getInt("E32119") < 1)
				st.set("E32119", "1");
		}
		else if(event.equalsIgnoreCase("P32119"))
		{
			htmltext = "32119-07.htm";
			if(st.getInt("P32119") < 1)
				st.set("P32119", "1");
		}
		else if(event.equalsIgnoreCase("U32119"))
		{
			if(st.getInt("U32119") < 1)
				st.set("U32119", "1");
			htmltext = getWordText32119(st);
		}
		if(event.equalsIgnoreCase("T32120"))
		{
			htmltext = "32120-05.htm";
			if(st.getInt("T32120") < 1)
				st.set("T32120", "1");
		}
		else if(event.equalsIgnoreCase("O32120"))
		{
			htmltext = "32120-06.htm";
			if(st.getInt("O32120") < 1)
				st.set("O32120", "1");
		}
		else if(event.equalsIgnoreCase("O32120_2"))
		{
			htmltext = "32120-07.htm";
			if(st.getInt("O32120_2") < 1)
				st.set("O32120_2", "1");
		}
		else if(event.equalsIgnoreCase("N32120"))
		{
			if(st.getInt("N32120") < 1)
				st.set("N32120", "1");
			htmltext = getWordText32120(st);
		}
		if(event.equalsIgnoreCase("W32121"))
		{
			htmltext = "32121-05.htm";
			if(st.getInt("W32121") < 1)
				st.set("W32121", "1");
		}
		else if(event.equalsIgnoreCase("A32121"))
		{
			htmltext = "32121-06.htm";
			if(st.getInt("A32121") < 1)
				st.set("A32121", "1");
		}
		else if(event.equalsIgnoreCase("G32121"))
		{
			htmltext = "32121-07.htm";
			if(st.getInt("G32121") < 1)
				st.set("G32121", "1");
		}
		else if(event.equalsIgnoreCase("U32121"))
		{
			if(st.getInt("U32121") < 1)
				st.set("U32121", "1");
			htmltext = getWordText32121(st);
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getInt("cond");
		if(npcId == Mushika)
		{
			QuestState qs124 = st.getPlayer().getQuestState("_124_MeetingTheElroki");
			if(cond == 0)
			{
				if(qs124 != null && qs124.isCompleted())
				{
					htmltext = "32114-01.htm";
					st.exitCurrentQuest(true);
				}
				else if(st.getPlayer().getLevel() < 76)
				{
					htmltext = "32114-02.htm";
					st.exitCurrentQuest(true);
				}
				else
				{
					htmltext = "32114-04.htm";
					st.exitCurrentQuest(true);
				}
			}
			else if(cond == 1)
				htmltext = "32114-10.htm";
			else if(cond > 1 && cond < 8)
				htmltext = "32114-14.htm";
			else if(cond == 8)
			{
				st.unset("T32119");
				st.unset("E32119");
				st.unset("P32119");
				st.unset("U32119");
				st.unset("T32120");
				st.unset("O32120");
				st.unset("O32120_2");
				st.unset("N32120");
				st.unset("W32121");
				st.unset("A32121");
				st.unset("G32121");
				st.unset("U32121");
				st.unset("cond");
				htmltext = "32114-15.htm";
				st.addExpAndSp(859195, 86603, true);
				st.playSound(SOUND_FINISH);
				st.exitCurrentQuest(false);
			}
		}
		else if(npcId == Karakawei)
		{
			if(cond == 1)
				htmltext = "32117-02.htm";
			else if(cond == 2)
				htmltext = "32117-01.htm";
			else if(cond == 3 && (st.getQuestItemsCount(OrClaw) < 2 || st.getQuestItemsCount(DienBone) < 2))
				htmltext = "32117-12.htm";
			else if(cond == 3 && (st.getQuestItemsCount(OrClaw) == 2 && st.getQuestItemsCount(DienBone) == 2))
			{
				htmltext = "32117-11.htm";
				st.takeItems(OrClaw, 2);
				st.takeItems(DienBone, 2);
				st.set("cond", "4");
				st.playSound(SOUND_MIDDLE);
			}
			else if(cond > 4 && cond < 8)
				htmltext = "32117-19.htm";
			else if(cond == 8)
				htmltext = "32117-20.htm";
		}
		else if(npcId == UluKaimu)
		{
			if(cond == 5)
				htmltext = "32119-01.htm";
			else if(cond < 5)
				htmltext = "32119-02.htm";
			else if(cond > 5)
				htmltext = "32119-03.htm";
		}
		else if(npcId == BaluKaimu)
		{
			if(cond == 6)
				htmltext = "32120-01.htm";
			else if(cond < 6)
				htmltext = "32120-02.htm";
			else if(cond > 6)
				htmltext = "32120-03.htm";
		}
		else if(npcId == ChutaKaimu)
		{
			if(cond == 7)
				htmltext = "32121-01.htm";
			else if(cond < 7)
				htmltext = "32121-02.htm";
			else if(cond > 7)
				htmltext = "32121-03.htm";
			else if(cond == 8)
				htmltext = "32121-24.htm";
		}
		else
			htmltext = "completed";
		return htmltext;
	}

	@Override
	public String onKill(L2NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();

		if(((npcId >= 22200 && npcId <= 22202) || npcId == 22219 || npcId == 22224) && st.getQuestItemsCount(OrClaw) < 2 && Rnd.chance(10 * Config.RATE_QUESTS_DROP))
		{
			st.giveItems(OrClaw, 1);
			st.playSound(SOUND_MIDDLE);
		}
		if(((npcId >= 22203 && npcId <= 22205) || npcId == 22220 || npcId == 22225) && st.getQuestItemsCount(DienBone) < 2 && Rnd.chance(10 * Config.RATE_QUESTS_DROP))
		{
			st.giveItems(DienBone, 1);
			st.playSound(SOUND_MIDDLE);
		}

		return null;
	}
}