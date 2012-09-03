package ai;

import l2rt.gameserver.ai.CtrlIntention;
import l2rt.gameserver.ai.Fighter;
import l2rt.gameserver.geodata.GeoEngine;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.Reflection;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.instances.L2MonsterInstance;
import l2rt.gameserver.instancemanager.HellboundManager;
import l2rt.gameserver.tables.NpcTable;
import l2rt.util.Location;
import l2rt.util.Rnd;

/**
 * AI Native for Hellbound<br>
 * @author Drizzy
 * @date 26.11.10
 */
 
public class HBNative extends Fighter
{
	private static final int[] Native = { 22322, 22323 };
	private static final int NATIVE_COUNT = 2;
	private L2MonsterInstance npc = (L2MonsterInstance)NpcTable.getTemplate(Native[Rnd.get(Native.length)]).getNewInstance();
	
	public HBNative(L2Character actor)
	{
		super(actor);
	}
	
	@Override
	public boolean isGlobalAI()
	{
		return true;
	}

	@Override
	public void startAITask()
	{
		int hLevel = HellboundManager.getInstance().getLevel();	
		L2NpcInstance actor = getActor();				
		if(_aiTask == null)
		{
			if(hLevel < 5)
			{		
				for(int i = 0; i < NATIVE_COUNT; i++)
				{
					try
					{		
						if(actor != null)
						{
							Reflection r = actor.getReflection();
							Location pos = GeoEngine.findPointToStay(actor.getX(), actor.getY(), actor.getZ(), 100, 120, actor.getReflection().getGeoIndex());
							npc.setSpawnedLoc(pos);
							npc.setReflection(r);
							npc.onSpawn();
							npc.spawnMe(npc.getSpawnedLoc());
							npc.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, actor, 500);
						}	
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		}
		super.startAITask();
	}
	
	@Override
	protected void onEvtDead(L2Character killer)
	{
		int id = getActor().getNpcId();
		int hLevel = HellboundManager.getInstance().getLevel();
		if(id == 22320)
			if (hLevel <= 1)
				HellboundManager.getInstance().addPoints(1);
		if(id == 22321)
			if (hLevel <= 1)
				HellboundManager.getInstance().addPoints(1);
		npc.decayMe();
		super.onEvtDead(killer);
	}		
}