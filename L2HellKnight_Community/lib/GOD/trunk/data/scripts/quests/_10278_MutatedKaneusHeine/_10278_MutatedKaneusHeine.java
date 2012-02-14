package quests._10278_MutatedKaneusHeine;

import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.quest.Quest;
import l2rt.gameserver.model.quest.QuestState;

public class _10278_MutatedKaneusHeine extends Quest implements ScriptFile
{
	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}

	// NPCs
	private static final int Gosta = 30916;
	private static final int Minevia = 30907;

	// MOBs
	private static final int BladeOtis = 18562;
	private static final int WeirdBunei = 18564;

	// Items
	private static final int Tissue1 = 13834;
	private static final int Tissue2 = 13835;

	public CheckStatus LAST_CHECK_STATUS = CheckStatus.GRACIA_EPILOGUE;

	public _10278_MutatedKaneusHeine()
	{
		super(true);
		addStartNpc(Gosta);
		addTalkId(Minevia);
		addKillId(BladeOtis, WeirdBunei);
		addQuestItem(Tissue1, Tissue2);
	}

	@Override
	public String onEvent(String event, QuestState st, L2NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("30916-03.htm"))
		{
			st.setCond(1);
			st.setState(STARTED);
			st.playSound(SOUND_ACCEPT);
		}
		else if(event.equalsIgnoreCase("30907-02.htm"))
		{
			st.giveItems(57, 180000);
			st.exitCurrentQuest(false);
			st.playSound(SOUND_FINISH);
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int id = st.getState();
		int cond = st.getCond();
		int npcId = npc.getNpcId();
		if(id == COMPLETED)
		{
			if(npcId == Gosta)
				htmltext = "30916-0a.htm";
		}
		else if(id == CREATED && npcId == Gosta)
		{
			if(st.getPlayer().getLevel() >= 38)
				htmltext = "30916-01.htm";
			else
				htmltext = "30916-00.htm";
		}
		else
		{
			if(npcId == Gosta)
			{
				if(cond == 1)
					htmltext = "30916-04.htm";
				else if(cond == 2)
					htmltext = "30916-05.htm";
			}
			else if(npcId == Minevia)
			{
				if(cond == 1)
					htmltext = "30907-01a.htm";
				else if(cond == 2)
					htmltext = "30907-01.htm";
			}
		}
		return htmltext;
	}

	@Override
	public String onKill(L2NpcInstance npc, QuestState st)
	{
		if(st.getState() == STARTED && st.getCond() == 1)
		{
			st.giveItems(Tissue1, 1);
			st.giveItems(Tissue2, 1);
			st.setCond(2);
			st.playSound(SOUND_MIDDLE);
		}
		return null;
	}
}