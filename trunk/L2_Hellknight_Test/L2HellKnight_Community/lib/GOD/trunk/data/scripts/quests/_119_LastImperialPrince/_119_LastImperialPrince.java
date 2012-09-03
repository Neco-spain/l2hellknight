package quests._119_LastImperialPrince;

import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.quest.Quest;
import l2rt.gameserver.model.quest.QuestState;

public class _119_LastImperialPrince extends Quest implements ScriptFile
{
	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}

	// NPC
	private static final int SPIRIT = 31453; // Nameless Spirit
	private static final int DEVORIN = 32009; // Devorin

	// ITEM
	private static final int BROOCH = 7262; // Antique Brooch

	// REWARD
	private static final int AMOUNT = 150292; // Amount

	public _119_LastImperialPrince()
	{
		super(false);
		addStartNpc(SPIRIT);
		addTalkId(DEVORIN);
	}

	@Override
	public String onEvent(String event, QuestState st, L2NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("31453-4.htm"))
		{
			st.set("cond", "1");
			st.setState(STARTED);
			st.playSound(SOUND_ACCEPT);
		}
		else if(event.equalsIgnoreCase("32009-2.htm"))
		{
			if(st.getQuestItemsCount(BROOCH) < 1)
			{
				htmltext = "frintessa_nurse_q0119_05.htm";
				st.exitCurrentQuest(true);
			}
		}
		else if(event.equalsIgnoreCase("32009-3.htm"))
		{
			st.set("cond", "2");
			st.playSound(SOUND_MIDDLE);
		}
		else if(event.equalsIgnoreCase("31453-7.htm"))
		{
			st.giveItems(ADENA_ID, AMOUNT, true);
			st.addExpAndSp(902439, 90067);
			st.playSound(SOUND_FINISH);
			st.setState(COMPLETED);
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

		if(st.getPlayer().getLevel() < 74)
		{
			htmltext = "printessa_spirit_q0119_02.htm";
			st.exitCurrentQuest(true);
			return htmltext;
		}
		else if(st.getQuestItemsCount(BROOCH) < 1)
		{
			htmltext = "printessa_spirit_q0119_07a.htm";
			st.exitCurrentQuest(true);
			return htmltext;
		}
		else if(st.getState() == COMPLETED)
		{
			htmltext = "printessa_spirit_q0119_03.htm";
			st.exitCurrentQuest(true);
			return htmltext;
		}

		if(npcId == SPIRIT)
		{
			if(cond == 0)
				return "31453-1.htm";
			else if(cond == 2)
				return "31453-5.htm";
			else
				return "noquest";
		}
		else if(npcId == DEVORIN && cond == 1)
			htmltext = "32009-1.htm";
		return htmltext;
	}
}