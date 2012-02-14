package quests._126_IntheNameofEvilPart2;

import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.quest.Quest;
import l2rt.gameserver.model.quest.QuestState;
import quests._125_InTheNameOfEvilPart1._125_InTheNameOfEvilPart1;

public class _126_IntheNameofEvilPart2 extends Quest implements ScriptFile
{
	private int Mushika = 32114;
	private int Asamah = 32115;
	private int UluKaimu = 32119;
	private int BaluKaimu = 32120;
	private int ChutaKaimu = 32121;
	private int WarriorGrave = 32122;
	private int ShilenStoneStatue = 32109;

	private int BONEPOWDER = 8783;
	private int EPITAPH = 8781;
	private int EWA = 729;

	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}

	public _126_IntheNameofEvilPart2()
	{
		super(false);

		addStartNpc(Asamah);
		addTalkId(Mushika);
		addTalkId(UluKaimu);
		addTalkId(BaluKaimu);
		addTalkId(ChutaKaimu);
		addTalkId(WarriorGrave);
		addTalkId(ShilenStoneStatue);
	}

	@Override
	public String onEvent(String event, QuestState st, L2NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("proklatie.htm"))
		{
			st.setState(STARTED);
			st.playSound(SOUND_ACCEPT);
			st.set("cond", "1");
		}
		else if(event.equalsIgnoreCase("statui.htm"))
			st.set("cound", "2");
		else if(event.equalsIgnoreCase("statuesvedenia.htm"))
			st.set("cound", "3");
		else if(event.equalsIgnoreCase("statuemelodia.htm"))
			st.set("cound", "4");
		else if(event.equalsIgnoreCase("statuepoka.htm"))
			st.set("cound", "5");
		else if(event.equalsIgnoreCase("statueevoprosss.htm"))
			st.set("cound", "6");
		else if(event.equalsIgnoreCase("statueemelodia.htm"))
			st.set("cound", "7");
		else if(event.equalsIgnoreCase("statueepoka.htm"))
			st.set("cound", "8");
		else if(event.equalsIgnoreCase("statueeevopross.htm"))
			st.set("cound", "9");
		else if(event.equalsIgnoreCase("statueeemelodia.htm"))
			st.set("cound", "10");
		else if(event.equalsIgnoreCase("statueeepoka.htm"))
			st.set("cound", "11");
		else if(event.equalsIgnoreCase("wargravevopross.htm"))
			st.set("cound", "13");
		else if(event.equalsIgnoreCase("wargravevoprosss.htm"))
			st.set("cound", "14");
		else if(event.equalsIgnoreCase("wargravertodinok.htm"))
			st.set("cound", "15");
		else if(event.equalsIgnoreCase("wargravertdwaok.htm"))
			st.set("cound", "16");
		else if(event.equalsIgnoreCase("wargraverttriok.htm"))
			st.set("cound", "17");
		else if(event.equalsIgnoreCase("wargraveprah.htm"))
			st.set("cound", "18");
		else if(event.equalsIgnoreCase("wargraveritualok.htm"))
			st.giveItems(BONEPOWDER, 1);
		else if(event.equalsIgnoreCase("shstatuee.htm"))
			st.set("cound", "19");
		else if(event.equalsIgnoreCase("shstatueritual.htm"))
		{
			st.set("cound", "20");
			st.takeItems(BONEPOWDER, 1);
		}
		else if(event.equalsIgnoreCase("vernulsaaa.htm"))
			st.set("cound", "21");
		else if(event.equalsIgnoreCase("gazkh.htm"))
			st.set("cound", "22");
		else if(event.equalsIgnoreCase("theend.htm"))
		{
			st.playSound(SOUND_FINISH);
			st.giveItems(EWA, 1);
			st.giveItems(ADENA_ID, 460483);
			st.addExpAndSp(1015973, 102802);
			st.unset("cond");
			st.setState(COMPLETED);
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int id = st.getState();
		int npcId = npc.getNpcId();
		int cond = st.getInt("cond");
		if(npcId == Asamah && st.getQuestItemsCount(EPITAPH) >= 1)
			if(id == CREATED)
			{
				QuestState IntheNameofEvilPart1 = st.getPlayer().getQuestState(_125_InTheNameOfEvilPart1.class);
				if(IntheNameofEvilPart1 != null)
				{
					if(IntheNameofEvilPart1.isCompleted())
					{
						if(st.getPlayer().getLevel() >= 77)
							htmltext = "privetstvie.htm";
						else
						{
							htmltext = "lvl.htm";
							st.exitCurrentQuest(true);
						}
					}
					else
					{
						htmltext = "no.htm";
						st.exitCurrentQuest(true);
					}
				}
				else if(cond == 2)
					htmltext = "statui.htm";
				else if(cond == 20)
					htmltext = "vernulsa.htm";
				else if(cond == 21)
					htmltext = "gazkh.htm";
			}
		if(npcId == UluKaimu)
			if(cond == 2)
				htmltext = "statue.htm";
			else if(cond == 3)
				htmltext = "statuesvedenia.htm";
			else if(cond == 4)
				htmltext = "statuemelodia.htm";
			else if(cond == 5)
				htmltext = "statuepoka.htm";
		if(npcId == BaluKaimu)
			if(cond == 5)
				htmltext = "statuee.htm";
			else if(cond == 6)
				htmltext = "statueevoprosss.htm";
			else if(cond == 7)
				htmltext = "statueemelodia.htm";
			else if(cond == 8)
				htmltext = "statueepoka.htm";
		if(npcId == ChutaKaimu)
			if(cond == 8)
				htmltext = "statueee.htm";
			else if(cond == 9)
				htmltext = "statueeevoprosss.htm";
			else if(cond == 10)
				htmltext = "statueeemelodia.htm";
			else if(cond == 11)
				htmltext = "statueeepoka.htm";
		if(npcId == WarriorGrave)
			if(cond == 11)
			{
				htmltext = "wargrave.htm";
				st.set("cond", "12");
			}
			else if(cond == 12)
				htmltext = "wargravemolitva.htm";
			else if(cond == 13)
				htmltext = "wargravevopross.htm";
			else if(cond == 14)
				htmltext = "wargraveritual.htm";
			else if(cond == 15)
				htmltext = "wargravertdwa.htm";
			else if(cond == 16)
				htmltext = "wargraverttri.htm";
			else if(cond == 17)
				htmltext = "wargraveritualok.htm";
			else if(cond == 18)
				htmltext = "wargraveprah.htm";
		if(npcId == ShilenStoneStatue)
			if(cond == 18)
				htmltext = "shstatue.htm";
			else if(cond == 19)
				htmltext = "shstatuee.htm";
			else if(cond == 20)
				htmltext = "shstatueritual.htm";
		if(npcId == Mushika)
			if(cond == 22)
				htmltext = "mushika.htm";
			else if(cond == 23)
				htmltext = "theend.htm";
		return htmltext;
	}
}