package teleports.ToiVortexBlue;

import l2.brick.Config;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.model.quest.QuestState;

public class ToiVortexBlue extends Quest
{
	private static final String qn = "ToiVortexBlue";

	private final static int DIMENSION_VORTEX_1 = 30952;
	private final static int DIMENSION_VORTEX_3 = 30954;

	private final static int BLUE_DIMENSION_STONE = 4402;

	public ToiVortexBlue(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(DIMENSION_VORTEX_1);
		addStartNpc(DIMENSION_VORTEX_3);
		addTalkId(DIMENSION_VORTEX_1);
		addTalkId(DIMENSION_VORTEX_3);
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(getName());
		int npcId = npc.getNpcId();
		if (npcId == DIMENSION_VORTEX_1 || npcId == DIMENSION_VORTEX_3)
		{
			if (st.getQuestItemsCount(BLUE_DIMENSION_STONE) >= 1)
			{
				st.takeItems(BLUE_DIMENSION_STONE, 1);
				player.teleToLocation(114097, 19935, 935);
			}
			else
				htmltext = "1.htm";
		}

		st.exitQuest(true);
		return htmltext;
	}

	public static void main(String[] args)
	{
		new ToiVortexBlue(-1, qn, "teleports");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Teleport: ToiVortexBlue Teleport");
	}
}