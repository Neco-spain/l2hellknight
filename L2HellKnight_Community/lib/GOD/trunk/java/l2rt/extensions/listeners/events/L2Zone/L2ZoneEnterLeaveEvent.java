package l2rt.extensions.listeners.events.L2Zone;

import l2rt.extensions.listeners.events.DefaultMethodInvokeEvent;
import l2rt.gameserver.model.L2Object;
import l2rt.gameserver.model.L2Zone;

/**
 * @Author: Death
 * @Date: 18/9/2007
 * @Time: 9:30:13
 */
public class L2ZoneEnterLeaveEvent extends DefaultMethodInvokeEvent
{
	public L2ZoneEnterLeaveEvent(String methodName, L2Zone owner, L2Object[] args)
	{
		super(methodName, owner, args);
	}

	@Override
	public L2Zone getOwner()
	{
		return (L2Zone) super.getOwner();
	}

	@Override
	public L2Object[] getArgs()
	{
		return (L2Object[]) super.getArgs();
	}
}
