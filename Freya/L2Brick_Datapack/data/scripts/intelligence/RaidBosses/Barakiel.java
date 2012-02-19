package intelligence.RaidBosses;

import l2.brick.Config;
import l2.brick.gameserver.model.actor.instance.L2NpcInstance;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.bflmpsvz.a.L2AttackableAIScript;

public class Barakiel extends L2AttackableAIScript
{
	// Barakiel NpcID
	private static final int BARAKIEL = 25325;
	// Barakiel Z coords
	private static final int x1 = 91008;
	private static final int x2 = -85904;
	private static final int y1 = -2736;

	public Barakiel (int questId, String name, String descr)
	{
		super(questId,name,descr);
		int[] mobs = {BARAKIEL};
		registerMobs(mobs);
	}

	public String onAttack (L2NpcInstance npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		int npcId = npc.getNpcId();
		if (npcId == BARAKIEL)
		{
			int x = npc.getX();
			int y = npc.getY();
			if (x < x1 || x > x2 || y < y1)
			{
				npc.teleToLocation(91008,-85904,-2736);
				npc.getStatus().setCurrentHp(npc.getMaxHp());
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	public static void main(String[] args)
	{
		new Barakiel(-1, "Barakiel", "ai");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded RaidBoss: Barakiel");
	}
}