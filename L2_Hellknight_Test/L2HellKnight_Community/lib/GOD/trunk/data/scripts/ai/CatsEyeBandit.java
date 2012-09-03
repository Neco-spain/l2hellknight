package ai;

import l2rt.extensions.scripts.Functions;
import l2rt.gameserver.ai.Fighter;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.util.Rnd;

public class CatsEyeBandit extends Fighter
{
	private boolean FirstAttacked = false;

	public CatsEyeBandit(L2Character actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtAttacked(L2Character attacker, int damage)
	{
		L2NpcInstance actor = getActor();
		if(actor == null)
			return;
		if(FirstAttacked)
		{
			if(Rnd.get(100) < 40)
				Functions.npcShout(actor, "You're fool, you think you can catch me?");
		}
		else
			FirstAttacked = true;
	}

	@Override
	protected void onEvtDead(L2Character killer)
	{
		L2NpcInstance actor = getActor();
		if(Rnd.get(100) < 80)
			Functions.npcShout(actor, "I have to do something after this shameful incident...");
		FirstAttacked = false;
		super.onEvtDead(killer);
	}
}