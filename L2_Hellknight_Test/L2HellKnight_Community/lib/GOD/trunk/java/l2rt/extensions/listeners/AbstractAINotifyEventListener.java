package l2rt.extensions.listeners;

import l2rt.extensions.listeners.events.AbstractAI.AbstractAINotifyEvent;
import l2rt.extensions.listeners.events.MethodEvent;
import l2rt.gameserver.ai.AbstractAI;
import l2rt.gameserver.ai.CtrlEvent;

/**
 * @Author: Diamond
 * @Date: 08/11/2007
 * @Time: 7:17:24
 */
public abstract class AbstractAINotifyEventListener implements MethodInvokeListener, MethodCollection
{
	@Override
	public final void methodInvoked(MethodEvent e)
	{
		AbstractAINotifyEvent event = (AbstractAINotifyEvent) e;
		AbstractAI ai = event.getOwner();
		CtrlEvent evt = (CtrlEvent) event.getArgs()[0];
		NotifyEvent(ai, evt, (Object[]) event.getArgs()[1]);
	}

	@Override
	public final boolean accept(MethodEvent event)
	{
		String method = event.getMethodName();
		return event instanceof AbstractAINotifyEvent && method.equals(AbstractAInotifyEvent);
	}

	public abstract void NotifyEvent(AbstractAI ai, CtrlEvent evt, Object[] args);
}
