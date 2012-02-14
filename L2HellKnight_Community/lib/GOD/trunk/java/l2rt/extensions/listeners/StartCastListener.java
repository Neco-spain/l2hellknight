package l2rt.extensions.listeners;

import l2rt.extensions.listeners.events.MethodEvent;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Skill;

/**
 * User: Death
 */
public abstract class StartCastListener implements MethodInvokeListener, MethodCollection
{
	@Override
	public final void methodInvoked(MethodEvent e)
	{
		Object[] args = e.getArgs();
		onCastStart((L2Skill) args[0], (L2Character) args[1], (Boolean) args[2]);
	}

	@Override
	public final boolean accept(MethodEvent event)
	{
		return event.getMethodName().equals(onStartCast);
	}

	public abstract void onCastStart(L2Skill skill, L2Character target, boolean forceUse);
}
