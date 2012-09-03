package ai;

import l2rt.extensions.scripts.Functions;
import l2rt.gameserver.ai.Ranger;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.util.Rnd;

/**
 * AI для Karul Bugbear ID: 20600
 *
 * @author Diamond
 */
public class KarulBugbear extends Ranger
{
	private boolean _firstTimeAttacked = true;

	public KarulBugbear(L2Character actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtAttacked(L2Character attacker, int damage)
	{
		L2NpcInstance actor = getActor();
		if(actor == null)
			return;
		if(_firstTimeAttacked)
		{
			_firstTimeAttacked = false;
			if(Rnd.chance(25))
				Functions.npcSay(actor, "Your rear is practically unguarded!");
		}
		else if(Rnd.chance(10))
			Functions.npcSay(actor, "Watch your back!");
		super.onEvtAttacked(attacker, damage);
	}

	@Override
	protected void onEvtDead(L2Character killer)
	{
		_firstTimeAttacked = true;
		super.onEvtDead(killer);
	}
}