package l2rt.gameserver.skills;

import l2rt.config.ConfigSystem;
import l2rt.debug.StatsLimitDebugger;
import l2rt.extensions.listeners.MaxHpMpCpListener;
import l2rt.extensions.listeners.StatsChangeListener;
import l2rt.extensions.listeners.StorageSizeListener;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.skills.funcs.Func;
import l2rt.gameserver.skills.funcs.FuncOwner;

/**
 * A calculator is created to manage and dynamically calculate the effect of a character property (ex : MAX_HP, REGENERATE_HP_RATE...).
 * In fact, each calculator is a table of Func object in which each Func represents a mathematic function : <BR><BR>
 *
 * FuncAtkAccuracy -> Math.sqrt(_player.getDEX())*6+_player.getLevel()<BR><BR>
 *
 * When the calc method of a calculator is launched, each mathematic function is called according to its priority <B>_order</B>.
 * Indeed, Func with lowest priority order is executed firsta and Funcs with the same order are executed in unspecified order.
 * The result of the calculation is stored in the value property of an Env class instance.<BR><BR>
 *
 * Method addFunc and removeFunc permit to add and remove a Func object from a Calculator.<BR><BR>
 *
 */
public final class Calculator
{
	/** Empty Func table definition */
	private static final Func[] emptyFuncs = new Func[0];

	/** Table of Func object */
	private Func[] _functions;

	private static final StatsChangeListener[] emptyListeners = new StatsChangeListener[0];
	private StatsChangeListener[] _listeners = emptyListeners;

	private Double _base = null;
	private Double _last = null;

	public final Stats _stat;
	public final L2Character _character;

	/**
	 * Constructor<?> of Calculator (Init value : emptyFuncs).<BR><BR>
	 */
	public Calculator(Stats stat, L2Character character)
	{
		_stat = stat;
		_character = character;
		_functions = emptyFuncs;
		if(character.isPlayable() || !stat.isLimitOnlyPlayable())
			if(ConfigSystem.getBoolean("DebugStatLimits") && character.isPlayer())
				addListener(new StatsLimitDebugger(stat));
		if(stat == Stats.MAX_MP || stat == Stats.MAX_HP || stat == Stats.MAX_CP)
			addListener(new MaxHpMpCpListener(stat));
		if(character.isPlayer() && (stat == Stats.INVENTORY_LIMIT || stat == Stats.STORAGE_LIMIT || stat == Stats.TRADE_LIMIT || stat == Stats.COMMON_RECIPE_LIMIT || stat == Stats.DWARVEN_RECIPE_LIMIT))
			addListener(new StorageSizeListener(stat));
	}

	/**
	 * Check if 2 calculators are equals.<BR><BR>
	 */
	public static boolean equalsCals(Calculator c1, Calculator c2)
	{
		if(c1 == c2)
			return true;

		if(c1 == null || c2 == null)
			return false;

		Func[] funcs1 = c1.getFunctions();
		Func[] funcs2 = c2.getFunctions();

		if(funcs1.length != funcs2.length)
			return false;

		if(funcs1 == funcs2)
			return true;

		if(funcs1.length == 0)
			return true;

		for(int i = 0; i < funcs1.length; i++)
			if(funcs1[i] != funcs2[i])
				return false;
		return true;

	}

	/**
	 * Return the number of Funcs in the Calculator.<BR><BR>
	 */
	public int size()
	{
		return _functions.length;
	}

	public synchronized void addListener(StatsChangeListener l)
	{
		StatsChangeListener[] tmp_listeners = new StatsChangeListener[_listeners.length + 1];
		if(_listeners.length > 0)
			System.arraycopy(_listeners, 0, tmp_listeners, 0, _listeners.length);
		tmp_listeners[_listeners.length] = l;
		l.setCalculator(this);
		_listeners = tmp_listeners;
	}

	/**
	 * Add a Func to the Calculator.<BR><BR>
	 */
	public synchronized void addFunc(Func f)
	{
		Func[] funcs = _functions;
		Func[] tmp = new Func[funcs.length + 1];

		final int order = f._order;
		int i;

		for(i = 0; i < funcs.length && order >= funcs[i]._order; i++)
			tmp[i] = funcs[i];

		tmp[i] = f;

		for(; i < funcs.length; i++)
			tmp[i + 1] = funcs[i];

		_functions = tmp;
	}

	/**
	 * Remove a Func from the Calculator.<BR><BR>
	 */
	public synchronized void removeFunc(Func f)
	{
		Func[] funcs = _functions;
		if(funcs.length == 0)
			return;

		if(funcs.length == 1)
		{
			if(funcs[0] == f)
				_functions = emptyFuncs;
			return;
		}

		int size = funcs.length;
		for(Func func : funcs)
			if(func == f)
				size--;

		if(size == funcs.length)
			return;
		if(size <= 0)
		{
			_functions = emptyFuncs;
			return;
		}

		Func[] tmp = new Func[size];

		int j = 0;

		for(int i = 0; i < funcs.length; i++)
			if(tmp.length > j && f != funcs[i])
				tmp[j++] = funcs[i];

		_functions = tmp;
	}

	/**
	 * Remove each Func with the specified owner of the Calculator.<BR><BR>
	 */
	public synchronized void removeOwner(Object owner)
	{
		Func[] funcs = _functions;
		for(Func element : funcs)
			if(element._funcOwner == owner)
				removeFunc(element);
	}

	/**
	 * Run each Func of the Calculator.<BR><BR>
	 */
	public void calc(Env env)
	{
		Func[] funcs = _functions;
		_base = env.value;
		boolean overrideLimits = false;
		for(Func func : funcs)
		{
			if(func._funcOwner instanceof FuncOwner)
			{
				if(!((FuncOwner) func._funcOwner).isFuncEnabled())
					continue;
				if(((FuncOwner) func._funcOwner).overrideLimits())
					overrideLimits = true;
			}
			if(func.getCondition() == null || func.getCondition().test(env))
				func.calc(env);
		}

		if(!overrideLimits)
		{
			if(_stat._min != null && env.value < _stat._min)
				env.value = _stat._min;
			if(_stat._max != null && env.value > _stat._max && (_character.isPlayer() || !_stat.isLimitOnlyPlayable()))
				env.value = _stat._max;
		}
		if(_last == null || _last != env.value)
		{
			Double last = _last;
			_last = env.value;
			for(StatsChangeListener _listener : _listeners)
				_listener.statChanged(last, env.value, _base, env);
		}
	}

	/**
	 * Для отладки
	 */
	public Func[] getFunctions()
	{
		return _functions;
	}

	public Double getBase()
	{
		return _base;
	}

	public Double getLast()
	{
		return _last;
	}
}