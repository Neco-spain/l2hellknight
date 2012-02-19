package intelligence.Monsters;

import l2.brick.bflmpsvz.a.L2AttackableAIScript;

import l2.brick.Config;
import l2.brick.gameserver.datatables.SpawnTable;
import l2.brick.gameserver.model.L2Spawn;
import l2.brick.gameserver.model.actor.L2Attackable;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.util.Util;

public class SeeThroughSilentMove extends L2AttackableAIScript
{
	private static final int[] MOBIDS = {18001,18002,22199,22215,22216,22217,22327,27351,29009,29010,29011,29012,29013};

	public SeeThroughSilentMove(int questId, String name, String descr)
	{
		super(questId, name, descr);
		for (L2Spawn npc : SpawnTable.getInstance().getSpawnTable())
			if (Util.contains(MOBIDS,npc.getNpcid()) && npc.getLastSpawn() != null && npc.getLastSpawn() instanceof L2Attackable)
				((L2Attackable)npc.getLastSpawn()).setSeeThroughSilentMove(true);
		for (int npcId : MOBIDS)
			this.addSpawnId(npcId);
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		if (npc instanceof L2Attackable)
			((L2Attackable)npc).setSeeThroughSilentMove(true);
		return super.onSpawn(npc);
	}

	public static void main(String[] args)
	{
		new SeeThroughSilentMove(-1, "SeeThroughSilentMove", "ai");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Monster: See Through Silent Move");
	}
}
