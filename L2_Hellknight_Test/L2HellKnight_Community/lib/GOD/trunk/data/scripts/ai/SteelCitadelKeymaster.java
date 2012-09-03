package ai;

import l2rt.extensions.scripts.Functions;
import l2rt.gameserver.ai.Fighter;
import l2rt.gameserver.geodata.GeoEngine;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2World;
import l2rt.gameserver.model.instances.L2NpcInstance;

/**
 * AI Steel Citadel Keymaster в городе-инстанте на Hellbound<br>
 * - кричит когда его атакуют первый раз
 * - портает к себе Amaskari, если был атакован
 * - не использует random walk
 * 
 * @author SYS
 */
public class SteelCitadelKeymaster extends Fighter
{
	private boolean _firstTimeAttacked = true;
	private static final int AMASKARI_ID = 22449;

	public SteelCitadelKeymaster(L2Character actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtAttacked(L2Character attacker, int damage)
	{
		L2NpcInstance actor = getActor();
		if(actor == null || actor.isDead())
			return;

		if(_firstTimeAttacked)
		{
			_firstTimeAttacked = false;
			Functions.npcSayCustomMessage(actor, "scripts.ai.SteelCitadelKeymaster.1800078");
			for(L2NpcInstance npc : L2World.getAroundNpc(actor))
				if(npc.getNpcId() == AMASKARI_ID && npc.getReflection().getId() == actor.getReflection().getId() && !npc.isDead())
				{
					npc.teleToLocation(GeoEngine.findPointToStay(actor.getX(), actor.getY(), actor.getZ(), 150, 180, actor.getReflection().getGeoIndex()));
					break;
				}
		}

		super.onEvtAttacked(attacker, damage);
	}

	@Override
	protected void onEvtDead(L2Character killer)
	{
		_firstTimeAttacked = true;
		super.onEvtDead(killer);
	}

	@Override
	protected boolean randomWalk()
	{
		return false;
	}
}