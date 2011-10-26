package intelligence.Monsters;

import l2.hellknight.Config;
import l2.hellknight.bflmpsvz.a.L2AttackableAIScript;
import l2.hellknight.gameserver.datatables.SkillTable;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;

public class FollowerOfAllosce extends L2AttackableAIScript
{
	private static final int FOFALLOSCE = 18568;

	public FollowerOfAllosce(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addAggroRangeEnterId(FOFALLOSCE);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("time_to_skill"))
		{
			npc.setTarget(player);
			npc.doCast(SkillTable.getInstance().getInfo(5624, 1));
			startQuestTimer("time_to_skill", 30000, npc, player);
		}

		return "";
	}

	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		int npcId = npc.getNpcId();

		if (npcId == FOFALLOSCE)
		{
			npc.setIsInvul(true);
			startQuestTimer("time_to_skill", 30000, npc, player);
			npc.setTarget(player);
			npc.doCast(SkillTable.getInstance().getInfo(5624, 1));
		}

		return "";
	}

	public static void main(String[] args)
	{
		new FollowerOfAllosce(-1, "FollowerOfAllosce", "ai");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Monster: Follower Of Allosce");
	}
}