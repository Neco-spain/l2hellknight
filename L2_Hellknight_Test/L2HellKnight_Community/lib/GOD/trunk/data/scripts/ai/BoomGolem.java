package ai;

import l2rt.common.ThreadPoolManager;
import l2rt.gameserver.ai.Mystic;
import l2rt.gameserver.ai.CtrlIntention;
import l2rt.gameserver.geodata.GeoEngine;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2World;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.network.serverpackets.MagicSkillUse;
import l2rt.gameserver.tables.SkillTable;
import l2rt.util.Location;

/**
 * @author Drizzy
 * @date 12.11.10
 * @AI BoomGolem for Pavel Ruins.
 */
 
public class BoomGolem extends Mystic
{	
	public boolean skill1 = false;
	
	public BoomGolem(L2Character actor)
	{
		super(actor);
	}

	@Override
	protected boolean randomWalk()
	{
		return false;
	}
	
	@Override
	protected boolean thinkActive()
	{
		L2NpcInstance actor = getActor();
		
		for(L2Player player : L2World.getAroundPlayers(actor, 450, 450))
		{
			if (actor.getDistance(player) >= 210 && actor.getDistance(player) <= 450)
			{
				Location loc = GeoEngine.findPointToStay(player.getX(), player.getY(), player.getZ(), 0, 0, player.getReflection().getGeoIndex());
				actor.broadcastPacketToOthers(new MagicSkillUse(actor, actor, 4671, 1, 500, 0));
				ThreadPoolManager.getInstance().scheduleAi(new Teleport(loc), 500, false);
				actor.doCast(SkillTable.getInstance().getInfo(6264, 1), player, true); // Golem Boom
			}
		}
		return super.thinkActive();
	}
	
	@Override
	protected void thinkAttack()
	{
		L2NpcInstance actor = getActor();	
		
		for(L2Player c : L2World.getAroundPlayers(actor, 1500, 1500))
		{
			if (actor.getDistance(c) <= 199)
			{
				if (skill1 == false)
				{
					actor.doCast(SkillTable.getInstance().getInfo(6264, 1), c, true); // Golem Boom	
					skill1 = true;
				}
			}
			if (actor.getDistance(c) >= 200)
			{
				setIntention(CtrlIntention.AI_INTENTION_ACTIVE);			
			}
		}
		super.thinkAttack();
	}
	
	@Override
	protected void onEvtDead(L2Character killer)
	{
		skill1 = false;
		super.onEvtDead(killer);
	}	
}