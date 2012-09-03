package ai;

import l2rt.common.ThreadPoolManager;
import l2rt.gameserver.ai.DefaultAI;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2World;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.util.Location;
import l2rt.util.GArray;
import l2rt.util.Rnd;

public class TeleportArkan extends DefaultAI
{
	public TeleportArkan(L2Character actor)
	{
		super(actor);
		this.AI_TASK_DELAY = 1000;
		this.AI_TASK_ACTIVE_DELAY = 1000;
	}

	@Override
	protected boolean thinkActive()
	{
		L2NpcInstance actor = getActor();
		if(actor == null)
			return true;
		
		for(L2Player player : L2World.getAroundPlayers(actor, 200, 200))
		{
			if (player != null) {
				player.teleToLocation(new Location(207559, 86429, -1000));
			}
		}
		return true;
	}

	@Override
	public boolean isGlobalAI()
	{
		return true;
	}
}