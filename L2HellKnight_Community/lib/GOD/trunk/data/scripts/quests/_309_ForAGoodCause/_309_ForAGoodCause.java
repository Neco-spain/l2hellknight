package quests._309_ForAGoodCause;

import java.io.File;

import l2rt.Config;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.model.L2Multisell;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.quest.Quest;
import l2rt.gameserver.model.quest.QuestState;
import quests._239_WontYouJoinUs._239_WontYouJoinUs;
import quests._308_ReedFieldMaintenance._308_ReedFieldMaintenance;

public class _309_ForAGoodCause extends Quest implements ScriptFile
{
	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}

	private static void loadMultiSell()
	{
		L2Multisell.getInstance().parseFile(new File(Config.DATAPACK_ROOT, "data/scripts/quests/_309_ForAGoodCause/32647001.xml"));
		L2Multisell.getInstance().parseFile(new File(Config.DATAPACK_ROOT, "data/scripts/quests/_309_ForAGoodCause/32647002.xml"));
		L2Multisell.getInstance().parseFile(new File(Config.DATAPACK_ROOT, "data/scripts/quests/_309_ForAGoodCause/32647011.xml"));
		L2Multisell.getInstance().parseFile(new File(Config.DATAPACK_ROOT, "data/scripts/quests/_309_ForAGoodCause/32647012.xml"));
	}

	public static void OnReloadMultiSell()
	{
		loadMultiSell();
	}

	static
	{
		loadMultiSell();
	}

	private static final int Atra = 32647;

	private static final int MucrokianHide = 14873;
	private static final int FallenMucrokianHide = 14874;

	private static final int MucrokianFanatic = 22650;
	private static final int MucrokianAscetic = 22651;
	private static final int MucrokianSavior = 22652;
	private static final int MucrokianPreacher = 22653;
	private static final int ContaminatedMucrokian = 22654;
	private static final int ChangedMucrokian = 22655;

	public _309_ForAGoodCause()
	{
		super(false);
		addStartNpc(Atra);
		addQuestItem(MucrokianHide, FallenMucrokianHide);
		addKillId(MucrokianFanatic, MucrokianAscetic, MucrokianSavior, MucrokianPreacher, ContaminatedMucrokian, ChangedMucrokian);
	}

	@Override
	public String onEvent(String event, QuestState st, L2NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("32647-05.htm"))
		{
			st.setCond(1);
			st.setState(STARTED);
		}
		if(event.equalsIgnoreCase("32646-14.htm"))
			st.exitCurrentQuest(true);
		return htmltext;
	}

	@Override
	public String onTalk(L2NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int id = st.getState();
		int cond = st.getCond();

		if(npcId == Atra)
			if(id == CREATED)
			{
				QuestState qs1 = st.getPlayer().getQuestState(_308_ReedFieldMaintenance.class);
				if(qs1 != null && qs1.isStarted())
					return "32647-17.htm"; // нельзя брать оба квеста сразу
				if(st.getPlayer().getLevel() < 82)
					return "32647-00.htm";
				return "32647-01.htm";
			}
			else if(cond == 1)
			{
				long fallen = st.takeAllItems(FallenMucrokianHide);
				if(fallen > 0)
					st.giveItems(MucrokianHide, fallen * 2);

				if(st.getQuestItemsCount(MucrokianHide) == 0)
					return "32647-06.htm"; // нечего менять
				return "32647-a1.htm";
			}

		return htmltext;
	}

	@Override
	public String onKill(L2NpcInstance npc, QuestState st)
	{
		st.rollAndGive(npc.getNpcId() == ContaminatedMucrokian ? FallenMucrokianHide : MucrokianHide, 1, 35);
		return null;
	}
}