package teleports.ToiVortexGreen;

import l2.hellknight.Config;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;

public class ToiVortexGreen extends Quest
{
	private static final String qn = "ToiVortexGreen";

	private final static int DIMENSION_VORTEX_2 = 30953;
	private final static int DIMENSION_VORTEX_3 = 30954;

	private final static int GREEN_DIMENSION_STONE = 4401;

	public ToiVortexGreen(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(DIMENSION_VORTEX_2);
		addStartNpc(DIMENSION_VORTEX_3);
		addTalkId(DIMENSION_VORTEX_2);
		addTalkId(DIMENSION_VORTEX_3);
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(getName());
		int npcId = npc.getNpcId();
		if (npcId == DIMENSION_VORTEX_2 || npcId == DIMENSION_VORTEX_3)
		{
			if (st.getQuestItemsCount(GREEN_DIMENSION_STONE) >= 1)
			{
				st.takeItems(GREEN_DIMENSION_STONE, 1);
				player.teleToLocation(110930, 15963, -4378);
			}
			else
				htmltext = "1.htm";
		}

		st.exitQuest(true);
		return htmltext;
	}

	public static void main(String[] args)
	{
		new ToiVortexGreen(-1, qn, "teleports");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Teleport: ToiVortexGreen Teleport");
	}
}