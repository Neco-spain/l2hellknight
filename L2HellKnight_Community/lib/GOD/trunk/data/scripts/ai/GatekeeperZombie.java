package ai;

import l2rt.extensions.scripts.Functions;
import l2rt.gameserver.ai.CtrlIntention;
import l2rt.gameserver.ai.Mystic;
import l2rt.gameserver.geodata.GeoEngine;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Playable;
import l2rt.gameserver.model.instances.L2NpcInstance;

/**
 * AI охраны входа в Pagan Temple.<br>
 * <li>кидаются на всех игроков, у которых в кармане нету предмета 8064 или 8067
 * <li>не умеют ходить
 *
 * @author SYS
 */
public class GatekeeperZombie extends Mystic
{
	public GatekeeperZombie(L2Character actor)
	{
		super(actor);
		actor.setImobilised(true);
	}

	@Override
	public void checkAggression(L2Character target)
	{
		L2NpcInstance actor = getActor();
		if(actor == null || target == null || !target.isPlayable())
			return;
		if(getIntention() != CtrlIntention.AI_INTENTION_ACTIVE)
			return;
		if(_globalAggro < 0)
			return;
		if(!actor.isInRange(target, actor.getAggroRange()))
			return;
		if(Math.abs(target.getZ() - actor.getZ()) > 400)
			return;
		if(Functions.getItemCount((L2Playable) target, 8067) != 0 || Functions.getItemCount((L2Playable) target, 8064) != 0)
			return;
		if(!GeoEngine.canSeeTarget(actor, target, false))
			return;
		target.addDamageHate(actor, 0, 1);
		setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
	}

	@Override
	protected boolean randomWalk()
	{
		return false;
	}
}