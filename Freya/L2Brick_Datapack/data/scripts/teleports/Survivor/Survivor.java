package teleports.Survivor;

import l2.brick.Config;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.model.quest.QuestState;

public class Survivor extends Quest
{
	private static final String qn = "Survivor";

	private final static int survivor = 32632;

	public Survivor(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(survivor);
		addTalkId(survivor);
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = ""; 
		QuestState st = player.getQuestState(getName());
		if (player.getLevel() >= 75)
		{
			if (st.getQuestItemsCount(57) >= 150000)
			{
				player.teleToLocation(-149406, 255247, -80);
				st.takeItems(57, 150000);
			}
			else
				htmltext = "32632-2.htm";
		}
		else
			htmltext = "32632-3.htm";

		st.exitQuest(true);
		return htmltext;
	}

	public static void main(String[] args)
	{
		new Survivor(-1, qn, "teleports");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Teleport: Survivor Teleport");
	}
}