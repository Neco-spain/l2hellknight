package intelligence.NPCs;

import l2.brick.Config;
import l2.brick.gameserver.ai.CtrlIntention;
import l2.brick.gameserver.model.actor.L2Attackable;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;

import l2.brick.bflmpsvz.a.L2AttackableAIScript;

public class SearchingMaster extends L2AttackableAIScript
{
	private static final int[] mobs =
	{
		20965,20966,20967,20968,20969,20970,20971,20972,20973
	};
	
	public SearchingMaster(int questId, String name, String descr)
	{
		super(questId, name, descr);
		for (int id : mobs)
			addAttackId(id);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet)
	{
		if (player == null)
			return null;
		
		npc.setIsRunning(true);
		((L2Attackable) npc).addDamageHate(player, 0, 999);
		npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
		
		return super.onAttack(npc, player, damage, isPet);
	}
	
	public static void main(String[] args)
	{
		new SearchingMaster(-1, "SearchingMaster", "ai");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded NPC: Searching Master");
	}
}