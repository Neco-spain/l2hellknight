//
// Suspicious Merchant - Hive Fortress (35728).
//
package ai;

import l2rt.gameserver.ai.DefaultAI;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.util.Location;
import l2rt.util.Rnd;

public class SuspiciousMerchantHive extends DefaultAI
{
	static final Location[] points = { new Location(19408, 189422, -3136), new Location(20039, 187700, -3416),
			new Location(19016, 185813, -3552), new Location(17959, 181955, -3680), new Location(16440, 181635, -3616),
			new Location(15679, 182540, -3608), new Location(15310, 182791, -3568), new Location(15242, 184507, -3112),
			new Location(15310, 182791, -3568), new Location(15679, 182540, -3608), new Location(16440, 181635, -3616),
			new Location(17959, 181955, -3680), new Location(19016, 185813, -3552), new Location(20039, 187700, -3416),
			new Location(19408, 189422, -3136) };

	private int current_point = -1;
	private long wait_timeout = 0;
	private boolean wait = false;

	public SuspiciousMerchantHive(L2Character actor)
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
					case 2:
						wait_timeout = System.currentTimeMillis() + 30000;
						wait = true;
						return true;
					case 4:
						wait_timeout = System.currentTimeMillis() + 30000;
						wait = true;
						return true;
					case 7:
						wait_timeout = System.currentTimeMillis() + 60000;
						wait = true;
						return true;
					case 10:
						wait_timeout = System.currentTimeMillis() + 30000;
						wait = true;
						return true;
					case 12:
						wait_timeout = System.currentTimeMillis() + 30000;
						wait = true;
						return true;
					case 14:
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