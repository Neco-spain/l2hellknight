package ai;

import l2rt.gameserver.ai.CtrlEvent;
import l2rt.gameserver.ai.Fighter;
import l2rt.gameserver.geodata.GeoEngine;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Spawn;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.tables.NpcTable;
import l2rt.util.Location;
import l2rt.util.Rnd;

public class MaleSpikedStakato extends Fighter
{
	public MaleSpikedStakato(L2Character actor)
	{
		super(actor);
	}
	
	@Override
	protected void onEvtDead(L2Character killer)
	{
		L2NpcInstance actor = getActor();
		if(actor != null)
			if(Rnd.chance(80))
			{
				for(int i = 0; i < 2; i++)
					try
					{
						Location pos = GeoEngine.findPointToStay(actor.getX(), actor.getY(), actor.getZ(), 100, 120, actor.getReflection().getGeoIndex());
						L2Spawn sp = new L2Spawn(NpcTable.getTemplate(22620));
						sp.setLoc(pos);
						L2NpcInstance npc = sp.doSpawn(true);
						npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, killer, Rnd.get(1, 100));
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
			}
		super.onEvtDead(killer);
	}
}
