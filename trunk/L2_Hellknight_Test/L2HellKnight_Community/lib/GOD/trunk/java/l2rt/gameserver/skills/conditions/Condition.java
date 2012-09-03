package l2rt.gameserver.skills.conditions;

import l2rt.gameserver.skills.Env;

import java.util.logging.Logger;

public abstract class Condition implements ConditionListener
{
	static final Logger _log = Logger.getLogger(Condition.class.getName());

	private ConditionListener _listener;

	private String _msg;

	private boolean _result;

	public final void setMessage(String msg)
	{
		_msg = msg;
	}

	public final String getMessage()
	{
		return _msg;
	}

	public void setListener(ConditionListener listener)
	{
		_listener = listener;
		notifyChanged();
	}

	public final ConditionListener getListener()
	{
		return _listener;
	}

	public final boolean test(Env env)
	{
		boolean res = testImpl(env);
		if(_listener != null && res != _result)
		{
			_result = res;
			notifyChanged();
		}
		return res;
	}

	protected abstract boolean testImpl(Env env);

	public void notifyChanged()
	{
		if(_listener != null)
			_listener.notifyChanged();
	}
}
