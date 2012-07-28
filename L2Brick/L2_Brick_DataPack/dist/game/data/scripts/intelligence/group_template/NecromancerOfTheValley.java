package intelligence.group_template;

import l2.brick.gameserver.ai.CtrlIntention;
import l2.brick.gameserver.model.actor.L2Attackable;
import l2.brick.gameserver.model.actor.L2Character;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.util.Rnd;

/**
 * @author malyelfik
 */

public class NecromancerOfTheValley extends L2AttackableAIScript
{	
	public NecromancerOfTheValley(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addKillId(22858);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if (Rnd.get(100) < 30)
		{
			L2Character attacker = isPet ? killer.getPet() : killer;
			// Exploding Orc Ghost
			L2Attackable explodingOrc = (L2Attackable) addSpawn(22818, npc.getX(), npc.getY(), npc.getZ() + 10, npc.getHeading(), false, 0, true);
			explodingOrc.setRunning();
			explodingOrc.addDamageHate(attacker, 0, 500);
			explodingOrc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
			
			// Wrathful Orc Ghost
			L2Attackable  wrathfulOrc = (L2Attackable) addSpawn(22819, npc.getX(), npc.getY(), npc.getZ() + 10, npc.getHeading(), false, 0, false);
			wrathfulOrc.setRunning();
			wrathfulOrc.addDamageHate(attacker, 0, 500);
			wrathfulOrc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
			
		}
		return super.onKill(npc, killer, isPet);
	}
	
	public static void main(String[] args)
	{
		new NecromancerOfTheValley(-1, "NecromancerOfTheValley", "ai");
	}
}