package ai;

import l2rt.gameserver.ai.CtrlEvent;
import l2rt.gameserver.ai.Fighter;
import l2rt.gameserver.geodata.GeoEngine;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Spawn;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.tables.NpcTable;
import l2rt.util.Location;

/**
 * @author Drizzy
 * @date 12.11.10
 * @AI Pavel Safety Device for Pavel Ruins.
 */
 
public class PavelSafetyDevice extends Fighter
{	
	public L2Character killer;
	public PavelSafetyDevice(L2Character actor)
	{
		super(actor);
	}
	
	@Override
	protected boolean randomWalk()
	{
		return false;
	}	

	@Override
	protected void onEvtDead(L2Character killer)
	{
		L2NpcInstance actor = getActor();	
		if(actor != null)
			try
			{
				Location pos = GeoEngine.findPointToStay(actor.getX(), actor.getY(), actor.getZ(), 100, 120, actor.getReflection().getGeoIndex());
				Location pos1 = GeoEngine.findPointToStay(actor.getX(), actor.getY(), actor.getZ(), 100, 120, actor.getReflection().getGeoIndex());
				L2Spawn sp = new L2Spawn(NpcTable.getTemplate(22802));
				L2Spawn sp1 = new L2Spawn(NpcTable.getTemplate(22805));
				sp.setLoc(pos);
				sp1.setLoc(pos1);
				L2NpcInstance npc = sp.doSpawn(true);
				L2NpcInstance npc1 = sp1.doSpawn(true);	
				npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, killer, 100);
				npc1.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, killer, 100);			
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		super.onEvtDead(killer);
	}	
}