package l2rt.extensions.listeners.events.L2Object;

import l2rt.extensions.listeners.events.DefaultMethodInvokeEvent;
import l2rt.gameserver.model.L2Object;

/**
 * @author Death
 */
public class MethodInvokeEvent extends DefaultMethodInvokeEvent
{
	public MethodInvokeEvent(String methodName, L2Object owner, Object[] args)
	{
		super(methodName, owner, args);
	}

	public L2Object getObject()
	{
		return (L2Object) getOwner();
	}
}
