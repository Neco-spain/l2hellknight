package l2rt.extensions.listeners;

import l2rt.gameserver.skills.Calculator;
import l2rt.gameserver.skills.Env;
import l2rt.gameserver.skills.Stats;

public abstract class StatsChangeListener
{
	public final Stats _stat;
	protected Calculator _calculator;

	public StatsChangeListener(Stats stat)
	{
		_stat = stat;
	}

	public void setCalculator(Calculator calculator)
	{
		_calculator = calculator;
	}

	public abstract void statChanged(Double oldValue, double newValue, double baseValue, Env env);
}