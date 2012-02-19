package teleports.FantasyIsle;

import l2.brick.Config;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.State;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.model.quest.QuestState;
import l2.brick.gameserver.network.serverpackets.NpcSay;

public class FantasyIsle extends Quest
{
	private static final String qn = "FantasyIsle";

	private final static int PADDIES = 32378;
	private final static int[] TELEPORT_NPCs =
	{
		30320,30256,30059,30080,30899,30177,
		30848,30233,31320,31275,31964
	};

	private final static int[][] RETURN_LOCS =
	{
		{-80884, 149770, -3040},
		{-12682, 122862, -3112},
		{15744, 142928, -2696},
		{83475, 147966, -3400},
		{111409, 219364, -3545},
		{82971, 53207, -1488},
		{146705, 25840, -2008},
		{116819, 76994, -2714},
		{43835, -47749, -792},
		{147930, -55281, -2728},
		{87386, -143246, -1293}
	};

	private final static int[][] ISLE_LOCS =
	{
		{-58752, -56898, -2032},
		{-59716, -57868, -2032},
		{-60691, -56893, -2032},
		{-59720, -55921, -2032}
	};

	public FantasyIsle(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(PADDIES);
		addTalkId(PADDIES);
		for (int id : TELEPORT_NPCs)
		{
			addStartNpc(id);
			addTalkId(id);
		}
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(getName());

		int npcId = npc.getNpcId();
		int i = containsN(TELEPORT_NPCs, npcId);
		if (i > -1)
		{
			int random_id = st.getRandom(ISLE_LOCS.length);
			int x = ISLE_LOCS[random_id][0];
			int y = ISLE_LOCS[random_id][1];
			int z = ISLE_LOCS[random_id][2];
			player.teleToLocation(x, y, z);
			st.setState(State.STARTED);
			st.set("id", Integer.toString(i));
		}
		else if (npcId == PADDIES)
		{
			if (st.getState() == State.STARTED && st.getInt("id") >= 0)
			{
				int return_id = st.getInt("id");
				if (return_id >= TELEPORT_NPCs.length) return_id = TELEPORT_NPCs.length-1;
					player.teleToLocation(RETURN_LOCS[return_id][0], RETURN_LOCS[return_id][1], RETURN_LOCS[return_id][2]);
			}
			else
			{
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), "You've arrived here from a different way. I'll send you to Rune Township which is the nearest town."));
				player.teleToLocation(43835, -47749, -792);
			}

			st.exitQuest(true);
		}

		return htmltext;
	}

	private static int containsN(int[] array, int obj)
	{
		for (int i = 0; i < array.length; i++)
		{
			if (array[i] == obj)
				return i;
		}
		return -1;
	}

	public static void main(String[] args)
	{
		new FantasyIsle(-1, qn, "teleports");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Teleport: FantasyIsle Teleport");
	}
}