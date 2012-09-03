package l2rt.gameserver.model.instances;

import l2rt.gameserver.templates.L2NpcTemplate;

import java.util.logging.Logger;

public class L2GeneralInstance extends L2RaidBossInstance
{
	protected static Logger _log = Logger.getLogger(L2GeneralInstance.class.getName());
	private boolean _teleportedToNest;

	private static final int BOSS_MAINTENANCE_INTERVAL = 10000;


	public L2GeneralInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	protected int getMaintenanceInterval()
	{
		return BOSS_MAINTENANCE_INTERVAL;
	}

	public void setTeleported(boolean flag)
	{
		_teleportedToNest = flag;
	}

	public boolean isTeleported()
	{
		return _teleportedToNest;
	}

	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}
}
