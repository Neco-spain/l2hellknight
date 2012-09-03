package ai;

import l2rt.gameserver.ai.DefaultAI;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.util.Location;
import l2rt.util.Rnd;

public class GhostOfVonHellmannsPage extends DefaultAI
{
	static final Location[] points = { new Location(51462, -54539, -3176), new Location(51870, -54398, -3176),
			new Location(52164, -53964, -3176), new Location(52390, -53282, -3176), new Location(52058, -52071, -3104),
			new Location(52237, -51483, -3112), new Location(52024, -51262, -3096) };

	static final String[] NPCtext = new String[] { //FIXME unused?
	"Follow me...", "This where that here...", "I want to speak to you..." };

	private int current_point = -1;
	private long wait_timeout = 0;
	private boolean wait = false;

	public GhostOfVonHellmannsPage(L2Character actor)
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
					case 6:
						wait_timeout = System.currentTimeMillis() + 60000;
						wait = true;
						return true;
				}

			wait_timeout = 0;
			wait = false;
			current_point++;

			if(current_point >= points.length)
			{
				actor.deleteMe();
				return false;
			}

			addTaskMove(points[current_point], true);
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