package quests._308_ReedFieldMaintenance;

import java.io.File;

import l2rt.Config;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.model.L2Multisell;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.quest.Quest;
import l2rt.gameserver.model.quest.QuestState;
import quests._238_SuccessFailureOfBusiness._238_SuccessFailureOfBusiness;
import quests._309_ForAGoodCause._309_ForAGoodCause;

public class _308_ReedFieldMaintenance extends Quest implements ScriptFile
{
	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}

	private static void loadMultiSell()
	{
		L2Multisell.getInstance().parseFile(new File(Config.DATAPACK_ROOT, "data/scripts/quests/_308_ReedFieldMaintenance/32646001.xml"));
		L2Multisell.getInstance().parseFile(new File(Config.DATAPACK_ROOT, "data/scripts/quests/_308_ReedFieldMaintenance/32646002.xml"));
		L2Multisell.getInstance().parseFile(new File(Config.DATAPACK_ROOT, "data/scripts/quests/_308_ReedFieldMaintenance/32646011.xml"));
		L2Multisell.getInstance().parseFile(new File(Config.DATAPACK_ROOT, "data/scripts/quests/_308_ReedFieldMaintenance/32646012.xml"));
	}

	public static void OnReloadMultiSell()
	{
		loadMultiSell();
	}

	static
	{
		loadMultiSell();
	}

	private static final int Katensa = 32646;

	private static final int MucrokianHide = 14871;
	private static final int AwakenMucrokianHide = 14872; // TODO: WTF is this?

	private static final int MucrokianFanatic = 22650;
	private static final int MucrokianAscetic = 22651;
	private static final int MucrokianSavior = 22652;
	private static final int MucrokianPreacher = 22653;
	private static final int ContaminatedMucrokian = 22654;
	private static final int ChangedMucrokian = 22655;

	public _308_ReedFieldMaintenance()
	{
		super(false);
		addStartNpc(Katensa);
		addQuestItem(MucrokianHide, AwakenMucrokianHide);
		addKillId(MucrokianFanatic, MucrokianAscetic, MucrokianSavior, MucrokianPreacher, ContaminatedMucrokian, ChangedMucrokian);
	}

	@Override
	public String onEvent(String event, QuestState st, L2NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("32646-04.htm"))
		{
			st.setCond(1);
			st.setState(STARTED);
		}
		if(event.equalsIgnoreCase("32646-11.htm"))
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

		if(npcId == Katensa)
			if(id == CREATED)
			{
				QuestState qs1 = st.getPlayer().getQuestState(_309_ForAGoodCause.class);
				if(qs1 != null && qs1.isStarted())
					return "32646-15.htm"; // нельзя брать оба квеста сразу
				if(st.getPlayer().getLevel() < 82)
					return "32646-00.htm";
				return "32646-01.htm";
			}
			else if(cond == 1)
			{
				long awaken = st.takeAllItems(AwakenMucrokianHide);
				if(awaken > 0)
					st.giveItems(MucrokianHide, awaken * 2);

				if(st.getQuestItemsCount(MucrokianHide) == 0)
					return "32646-05.htm";
				return "32646-a1.htm";
			}

		return htmltext;
	}

	@Override
	public String onKill(L2NpcInstance npc, QuestState st)
	{
		st.rollAndGive(npc.getNpcId() == ChangedMucrokian ? AwakenMucrokianHide : MucrokianHide, 1, 35);
		return null;
	}
}