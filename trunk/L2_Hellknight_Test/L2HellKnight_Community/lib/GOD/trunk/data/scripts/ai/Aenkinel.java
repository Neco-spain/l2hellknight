package ai;

import l2rt.gameserver.ai.Fighter;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2World;
import l2rt.gameserver.model.instances.L2NpcInstance;

/**
 * AI рейд босов Aenkinel в Delusion Chamber
 * @author SYS
 */
public class Aenkinel extends Fighter
{

	public Aenkinel(L2Character actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtDead(L2Character killer)
	{
		L2NpcInstance actor = getActor();
		if(actor == null)
			return;

		// Устанавливаем реюз для Tower и Great Seal 
		if(actor.getNpcId() == 25694 || actor.getNpcId() == 25695)
		{
			String refName = actor.getReflection().getName();
			for(L2Player p : L2World.getAroundPlayers(actor))
				if(p != null)
					p.setVar(refName, String.valueOf(System.currentTimeMillis()));
		}

		// TODO: Добавить спаун сундуков
		// босс 25694 - сундук 18820, 4 шт
		// босс 25695 - сундук 18823, 4 шт

		super.onEvtDead(killer);
	}
}