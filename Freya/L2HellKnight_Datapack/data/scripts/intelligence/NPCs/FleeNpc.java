package intelligence.NPCs;

import l2.hellknight.Config;
import l2.hellknight.bflmpsvz.a.L2AttackableAIScript;

import l2.hellknight.gameserver.ai.CtrlIntention;
import l2.hellknight.gameserver.model.L2CharPosition;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.util.Rnd;

public class FleeNpc extends L2AttackableAIScript
{
	private int[] _npcId = { 20432, 22228 ,18150,18151,18152,18153,18154,18155,18156,18157};
	
	public FleeNpc(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		for( int i = 0; i < _npcId.length; i++ )
		{
			this.addEventId(_npcId[i], Quest.QuestEventType.ON_ATTACK);
		}
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if (npc.getNpcId() >= 18150 && npc.getNpcId() <= 18157)
		{
			npc.getAI().setIntention( CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition((npc.getX() + Rnd.get(-40, 40)), (npc.getY()+ Rnd.get(-40, 40)), npc.getZ(), npc.getHeading()));
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
			return null;
		}
		else if (npc.getNpcId() == 20432 || npc.getNpcId() == 22228)
		{
			if (Rnd.get(3) == 2)
				npc.getAI().setIntention( CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition((npc.getX() + Rnd.get(-200, 200)), (npc.getY()+ Rnd.get(-200, 200)), npc.getZ(), npc.getHeading()));
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
			return null;
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	// Register the new Script at the Script System
	public static void main(String[] args)
	{
		new FleeNpc(-1, "FleeNpc", "Ai for Flee Npcs");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded NPC: Flee Npc");
	}
}
