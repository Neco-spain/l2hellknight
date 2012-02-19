package teleports.TeleportWithCharm;

import l2.brick.Config;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.model.quest.QuestState;

public class TeleportWithCharm extends Quest
{
	private static final String qn = "TeleportWithCharm";

	private final static int WHIRPY = 30540;
	private final static int TAMIL = 30576;

	private final static int ORC_GATEKEEPER_CHARM = 1658;
	private final static int DWARF_GATEKEEPER_TOKEN = 1659;

	public TeleportWithCharm(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(WHIRPY);
		addStartNpc(TAMIL);
		addTalkId(WHIRPY);
		addTalkId(TAMIL);
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = ""; 
		QuestState st = player.getQuestState(getName());
		int npcId = npc.getNpcId();
		if (npcId == WHIRPY)
		{
			if (st.getQuestItemsCount(DWARF_GATEKEEPER_TOKEN) >= 1)
			{
				st.takeItems(DWARF_GATEKEEPER_TOKEN, 1);
				player.teleToLocation(-80826, 149775, -3043);
			}
			else
				htmltext = "30540-01.htm";
		}
		else if (npcId == TAMIL)
		{
			if (st.getQuestItemsCount(ORC_GATEKEEPER_CHARM) >= 1)
			{
				st.takeItems(ORC_GATEKEEPER_CHARM, 1);
				player.teleToLocation(-80826, 149775, -3043);
			}
			else
				htmltext = "30576-01.htm";
		}

		st.exitQuest(true);
		return htmltext;
	}

	public static void main(String[] args)
	{
		new TeleportWithCharm(-1, qn, "teleports");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Teleport: Teleport With Charm");
	}
}