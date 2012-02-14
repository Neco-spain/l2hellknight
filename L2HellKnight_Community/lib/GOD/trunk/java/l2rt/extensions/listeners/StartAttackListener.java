package l2rt.extensions.listeners;

import l2rt.extensions.listeners.events.MethodEvent;
import l2rt.gameserver.model.L2Character;

public abstract class StartAttackListener implements MethodInvokeListener, MethodCollection
{
	@Override
	public final void methodInvoked(MethodEvent e)
	{
		onAttackStart((L2Character) e.getArgs()[0], (L2Character) e.getArgs()[1]);
	}

	@Override
	public final boolean accept(MethodEvent event)
	{
		return event.getMethodName().equals(onStartAttack);
	}

	public abstract void onAttackStart(L2Character attacker, L2Character target);
}
