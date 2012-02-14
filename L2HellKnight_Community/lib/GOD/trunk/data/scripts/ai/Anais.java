package ai;

import l2rt.gameserver.ai.Fighter;
import l2rt.gameserver.instancemanager.ZoneManager;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Playable;
import l2rt.gameserver.model.L2Zone;
import l2rt.gameserver.model.L2Zone.ZoneType;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.util.Rnd;

/**
 * AI для RB Anais в Monastery of Silence.
 * Агрится на игроков.
 * Видит через Silent Move.
 * При выходе из комнаты делает телепортацию на место.
 */
public class Anais extends Fighter
{
	private static L2Zone _zone;

	public Anais(L2Character actor)
	{
		super(actor);
		_zone = ZoneManager.getInstance().getZoneById(ZoneType.dummy, 702110, false);
	}

	@Override
	protected boolean maybeMoveToHome()
	{
		L2NpcInstance actor = getActor();
		if(actor != null && _zone.checkIfInZone(actor))
		{
			teleportHome(false);
		}
		return false;
	}

	public static L2Zone getZone()
	{
		return _zone;
	}

	@Override
	public boolean canSeeInSilentMove(L2Playable target)
	{
		// Может видеть игроков в режиме Silent Move с вероятностью 10%
		return !target.isSilentMoving() || Rnd.chance(10);
	}

	@Override
	protected void onEvtAggression(L2Character target, int aggro)
	{
	}
}