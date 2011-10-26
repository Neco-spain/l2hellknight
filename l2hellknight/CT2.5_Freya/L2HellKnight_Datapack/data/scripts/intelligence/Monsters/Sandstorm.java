package intelligence.Monsters;

import l2.hellknight.Config;
import l2.hellknight.bflmpsvz.a.L2AttackableAIScript;
import l2.hellknight.gameserver.datatables.SkillTable;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;

public class Sandstorm extends L2AttackableAIScript
{
	private static final int Sandstorm = 32350;
	 
	public Sandstorm (int questId, String name, String descr)
	{
		super(questId, name, descr);
		super.addAttackId(Sandstorm);
	}

	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
	int npcId = npc.getNpcId();

		if (npcId == Sandstorm)
		{
			npc.setTarget(player);
			npc.doCast(SkillTable.getInstance().getInfo(5435, 1));
		}

	return super.onAggroRangeEnter(npc, player, isPet);
	}

	public static void main(String[] args)
	{
		new Sandstorm (-1, "Sandstorm ", "ai");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Monster: Sandstorm");
	}
}