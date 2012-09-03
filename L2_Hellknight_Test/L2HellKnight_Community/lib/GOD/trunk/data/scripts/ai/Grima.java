package ai;

import l2rt.gameserver.ai.CtrlIntention;
import l2rt.gameserver.ai.Fighter;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2World;
import l2rt.gameserver.model.instances.L2NpcInstance;

public class Grima extends Fighter
{
	private L2NpcInstance target;
	private long _lastTarget;
	
	public Grima(L2Character actor)
	{
		super(actor);
		AI_TASK_DELAY = 1000;
		AI_TASK_ACTIVE_DELAY = 1000;
	}
	
	@Override
	protected boolean thinkActive()
	{
		L2NpcInstance actor = getActor();
		if(actor == null)
			return true;
			
	    if(System.currentTimeMillis() - _lastTarget > 1000)
		{
			_lastTarget = System.currentTimeMillis();
		    if(target == null)
			    for(L2NpcInstance npc : L2World.getAroundNpc(actor, 1000, 200))
			    	if(npc.getNpcId() == 18846)
					{
					    npc.addDamageHate(actor, 0, 1);
						target = npc;
					}
			if(target != null)
			{
		    	setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
				target = null;
			}
		}
		return true;
	}
}