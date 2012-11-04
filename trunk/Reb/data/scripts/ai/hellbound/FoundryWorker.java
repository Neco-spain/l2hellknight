package ai.hellbound;

import l2r.gameserver.ai.Fighter;
import l2r.gameserver.geodata.GeoEngine;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.utils.Location;

public class FoundryWorker extends Fighter
{
	public FoundryWorker(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();
		if(attacker != null)
		{
			Location pos = Location.findPointToStay(actor, 150, 250);
			if(GeoEngine.canMoveToCoord(attacker.getX(), attacker.getY(), attacker.getZ(), pos.x, pos.y, pos.z, actor.getGeoIndex()))
			{
				actor.setRunning();
				addTaskMove(pos, false);
			}
		}
	}

	@Override
	public boolean checkAggression(Creature target)
	{
		return false;
	}

	@Override
	protected void onEvtAggression(Creature target, int aggro)
	{}
}