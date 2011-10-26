package intelligence.NPCs.Other;

import l2.hellknight.Config;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;

public class NoTalkingNpcs extends Quest
{
	private final static int[] NO_TALKING_LIST =
	{
		18684, 18685, 18686, 18687, 18688, 18689, 18690, 19691, 18692, 31557, 31606, 
		31671, 31672, 31673, 31674, 32026, 32030, 32031, 32032, 32619, 32620, 32621
	};

	public NoTalkingNpcs(int questId, String name, String descr)
	{
		super(questId, name, descr);
		for (int _npcIds : NO_TALKING_LIST)
		{
			addStartNpc(_npcIds);
			addFirstTalkId(_npcIds);
		}
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if (contains(NO_TALKING_LIST, npc.getNpcId()))
			return null;

		npc.showChatWindow(player);
		return null;
	}

	public static void main(String[] args)
	{
		new NoTalkingNpcs(-1, "NoTalkingNpcs", "ai");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded NPC: No Talking NPCs");
	}
}
