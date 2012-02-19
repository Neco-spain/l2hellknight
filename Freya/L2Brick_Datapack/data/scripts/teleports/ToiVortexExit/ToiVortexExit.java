package teleports.ToiVortexExit;

import l2.brick.Config;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.model.quest.QuestState;

public class ToiVortexExit extends Quest
{
	private static final String qn = "ToiVortexExit";

	private final static int NPC = 29055;

	public ToiVortexExit(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(NPC);
		addTalkId(NPC);
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		int chance = st.getRandom(3);
		if (chance == 0)
		{
			int x = 108784 + st.getRandom(100);
			int y = 16000 + st.getRandom(100);
			int z = -4928;
			player.teleToLocation(x, y, z);
		}
		else if (chance == 1)
		{
			int x = 113824 + st.getRandom(100);
			int y = 10448 + st.getRandom(100);
			int z = -5164;
			player.teleToLocation(x, y, z);
		}
		else
		{
			int x = 115488 + st.getRandom(100);
			int y = 22096 + st.getRandom(100);
			int z = -5168;
			player.teleToLocation(x, y, z);
		}

		st.exitQuest(true);
		return null;
	}

	public static void main(String[] args)
	{
		new ToiVortexExit(-1, qn, "teleports");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Teleport: ToiVortexExit Teleport");
	}
}