package teleports.CrumaTower;

import l2.brick.Config;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.model.quest.QuestState;

public class CrumaTower extends Quest
{
	private static final String qn = "CrumaTower";

	private final static int NPC = 30483;

	public CrumaTower(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(NPC);
		addTalkId(NPC);
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(getName());
		if (player.getLevel() > 55)
			htmltext = "30483.htm";
		else
			player.teleToLocation(17724, 114004, -11672);

		st.exitQuest(true);
		return htmltext;
	}

	public static void main(String[] args)
	{
		new CrumaTower(-1, qn, "teleports");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Teleport: CrumaTower Teleport");
	}
}