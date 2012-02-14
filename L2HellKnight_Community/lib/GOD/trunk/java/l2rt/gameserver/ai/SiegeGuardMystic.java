package l2rt.gameserver.ai;

import l2rt.gameserver.model.L2Character;

public class SiegeGuardMystic extends SiegeGuard
{
	public SiegeGuardMystic(L2Character actor)
	{
		super(actor);
	}

	@Override
	public boolean getIsMobile()
	{
		return false;
	}

	@Override
	protected boolean createNewTask()
	{
		return defaultFightTask();
	}

	@Override
	public int getRatePHYS()
	{
		return _dam_skills.length == 0 ? 25 : 0;
	}

	@Override
	public int getRateDOT()
	{
		return 25;
	}

	@Override
	public int getRateDEBUFF()
	{
		return 25;
	}

	@Override
	public int getRateDAM()
	{
		return 100;
	}

	@Override
	public int getRateSTUN()
	{
		return 10;
	}

	@Override
	public int getRateBUFF()
	{
		return 10;
	}

	@Override
	public int getRateHEAL()
	{
		return 20;
	}
}