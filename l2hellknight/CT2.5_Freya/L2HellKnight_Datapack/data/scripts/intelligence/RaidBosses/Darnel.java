package intelligence.RaidBosses;

import l2.hellknight.Config;
import l2.hellknight.bflmpsvz.a.L2AttackableAIScript;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;

public class Darnel extends L2AttackableAIScript
{
	private static final int DARNEL = 25531;

	public Darnel(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addKillId(DARNEL);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		int npcId = npc.getNpcId();

		if (npcId == DARNEL)
			addSpawn(32279, 152761, 145950, -12588, 0, false, 0, false, player.getInstanceId());

		return "";
	}

	public static void main(String[] args)
	{
		new Darnel(-1, "Darnel", "ai");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded RaidBoss: Darnel");
	}
}