package ai;

import l2rt.extensions.scripts.Functions;
import l2rt.gameserver.ai.CtrlEvent;
import l2rt.gameserver.ai.CtrlIntention;
import l2rt.gameserver.ai.Fighter;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.util.GArray;

/**
 * Увидев игрока направляет в атаку всех кого найдет рядом
 */
public class Leogul extends Fighter
{
	public Leogul(L2Character actor)
	{
		super(actor);
	}

	@Override
	public void checkAggression(L2Character target)
	{
		CtrlIntention curIntention = getIntention();
		super.checkAggression(target);

		if(curIntention != CtrlIntention.AI_INTENTION_ACTIVE || getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
			return;

		L2NpcInstance actor = getActor();
		if(actor == null || target == null || target.getPlayer() == null)
			return;

		Functions.npcSayCustomMessage(actor, "scripts.ai.Leogul");
		GArray<L2NpcInstance> around = actor.getAroundNpc(800, 128);
		if(around != null && !around.isEmpty())
			for(L2NpcInstance npc : around)
				if(npc.isMonster() && npc.getNpcId() >= 22660 && npc.getNpcId() <= 22677)
					npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, target, 5000);
	}
}