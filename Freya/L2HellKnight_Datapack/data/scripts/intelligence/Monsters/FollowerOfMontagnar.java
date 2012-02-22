package intelligence.Monsters;

import l2.hellknight.Config;
import l2.hellknight.bflmpsvz.a.L2AttackableAIScript;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;

public class FollowerOfMontagnar extends L2AttackableAIScript
{
	private static final int FOFMONTAGNAR = 18569;

	public FollowerOfMontagnar(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addAggroRangeEnterId(FOFMONTAGNAR);
	}

	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		int npcId = npc.getNpcId();

		if (npcId == FOFMONTAGNAR)
			npc.setIsInvul(true);

		return "";
	}

	public static void main(String[] args)
	{
		new FollowerOfMontagnar(-1, "FollowerOfMontagnar", "ai");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Monster: Follower Of Montagnar");
	}
}