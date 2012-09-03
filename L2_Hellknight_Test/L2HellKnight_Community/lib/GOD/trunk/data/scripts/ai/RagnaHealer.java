package ai;

import l2rt.gameserver.ai.CtrlEvent;
import l2rt.gameserver.ai.Priest;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.util.GArray;

/**
 * @author Diamond
 */
public class RagnaHealer extends Priest
{
	private long lastFactionNotifyTime;

	public RagnaHealer(L2Character actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtAttacked(L2Character attacker, int damage)
	{
		L2NpcInstance actor = getActor();
		if(actor == null || attacker == null)
			return;

		if(System.currentTimeMillis() - lastFactionNotifyTime > 10000)
		{
			lastFactionNotifyTime = System.currentTimeMillis();
			GArray<L2NpcInstance> around = actor.getAroundNpc(500, 300);
			if(around != null && !around.isEmpty())
				for(L2NpcInstance npc : around)
					if(npc.isMonster() && npc.getNpcId() >= 22691 && npc.getNpcId() <= 22702)
						npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 5000);
		}

		super.onEvtAttacked(attacker, damage);
	}
}