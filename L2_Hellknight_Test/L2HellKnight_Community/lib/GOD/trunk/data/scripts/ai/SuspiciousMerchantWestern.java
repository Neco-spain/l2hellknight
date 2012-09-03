//
// Suspicious Merchant - Western Fortress (36211).
//
package ai;

import l2rt.gameserver.ai.DefaultAI;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.util.Location;
import l2rt.util.Rnd;

public class SuspiciousMerchantWestern extends DefaultAI
{
	static final Location[] points = { new Location(114221, -18762, -1768), new Location(115920, -19177, -2120),
			new Location(117105, -19759, -2400), new Location(118417, -20135, -2632), new Location(118881, -20011, -2712),
			new Location(117210, -18329, -1816), new Location(118881, -20011, -2712), new Location(118417, -20135, -2632),
			new Location(117105, -19759, -2400), new Location(115920, -19177, -2120), new Location(114221, -18762, -1768) };

	private int current_point = -1;
	private long wait_timeout = 0;
	private boolean wait = false;

	public SuspiciousMerchantWestern(L2Character actor)
	{
		super(actor);
	}

	@Override
	public boolean isGlobalAI()
	{
		return true;
	}

	@Override
	protected boolean thinkActive()
	{
		L2NpcInstance actor = getActor();
		if(actor == null || actor.isDead())
			return true;

		if(_def_think)
		{
			doTask();
			return true;
		}

		if(System.currentTimeMillis() > wait_timeout && (current_point > -1 || Rnd.chance(5)))
		{
			if(!wait)
				switch(current_point)
				{
					case 0:
						wait_timeout = System.currentTimeMillis() + 30000;
						wait = true;
						return true;
					case 3:
						wait_timeout = System.currentTimeMillis() + 30000;
						wait = true;
						return true;
					case 5:
						wait_timeout = System.currentTimeMillis() + 60000;
						wait = true;
						return true;
					case 7:
						wait_timeout = System.currentTimeMillis() + 30000;
						wait = true;
						return true;
					case 10:
						wait_timeout = System.currentTimeMillis() + 30000;
						wait = true;
						return true;
				}

			wait_timeout = 0;
			wait = false;
			current_point++;

			if(current_point >= points.length)
				current_point = 0;

			addTaskMove(points[current_point], false);
			doTask();
			return true;
		}

		if(randomAnimation())
			return true;

		return false;
	}

	@Override
	protected void onEvtAttacked(L2Character attacker, int damage)
	{}

	@Override
	protected void onEvtAggression(L2Character target, int aggro)
	{}
}