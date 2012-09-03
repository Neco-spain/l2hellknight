package ai;

import l2rt.gameserver.ai.DefaultAI;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.util.Location;
import l2rt.util.Rnd;

public class Pterosaur extends DefaultAI
{
	private static final Location[] points = { new Location(3964, -7496, -480), new Location(7093, -6207, -140),
			new Location(7838, -7407, -480), new Location(7155, -9208, -1467), new Location(7667, -10459, -1922),
			new Location(9431, -11590, -140), new Location(8241, -13708, -1922), new Location(8417, -15135, -1981),
			new Location(7604, -15878, -1922), new Location(7835, -18087, -1981), new Location(7880, -20446, -480),
			new Location(6889, -21556, -1023), new Location(5506, -21796, -1981), new Location(5350, -20690, -1981),
			new Location(3718, -19280, -1981), new Location(2819, -17029, -1922), new Location(2394, -14635, -480),
			new Location(3169, -13397, -88), new Location(2596, -11971, -1023), new Location(2040, -9636, -88),
			new Location(2910, -7033, -480), new Location(5099, -6510, -464), new Location(5895, -8563, -1023),
			new Location(3970, -9894, -1023), new Location(5994, -10320, -1981), new Location(6468, -11106, -424),
			new Location(7273, -18036, -424), new Location(5827, -20411, -1922), new Location(4708, -18472, -480),
			new Location(4104, -15834, -1023), new Location(5770, -15281, -480), new Location(7596, -19798, -480),
			new Location(10069, -22629, -480), new Location(10015, -23379, -140), new Location(8079, -22995, -1023),
			new Location(5846, -23514, -464), new Location(5683, -24093, -1981), new Location(4663, -24953, -140),
			new Location(7631, -25726, -1023), new Location(9875, -27738, -1922), new Location(11293, -27864, -1981),
			new Location(11058, -25030, -1981), new Location(11074, -23164, -140), new Location(10370, -22899, -424),
			new Location(9788, -24086, -424), new Location(11039, -24780, -1023), new Location(11341, -23669, -1023),
			new Location(8189, -20399, -140), new Location(6438, -20501, -1922), new Location(4972, -17586, -1922),
			new Location(6393, -13759, -1922), new Location(8841, -13530, -1981), new Location(9567, -12500, -480),
			new Location(9023, -11165, -1981), new Location(7626, -11191, -480), new Location(7341, -12035, -1981),
			new Location(11039, -24780, -140), new Location(8234, -13204, -1981), new Location(9316, -12869, -140),
			new Location(6935, -7852, -480) };

	private int current_point = -1;
	private long wait_timeout = 0;
	private boolean wait = false;

	public Pterosaur(L2Character actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtSpawn()
	{
		L2NpcInstance actor = getActor();
		if(actor == null)
			return;

		actor.setFlying(true);
		actor.hasChatWindow = false;
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
			if(doTask())
				clearTasks();
			return true;
		}

		long now = System.currentTimeMillis();
		if(now > wait_timeout && (current_point > -1 || Rnd.chance(5)))
		{
			if(!wait)
				switch(current_point)
				{
					case 0:
					case 8:
						wait_timeout = now + 10000;
						wait = false;
						return true;
				}

			wait_timeout = 0;
			wait = true;
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