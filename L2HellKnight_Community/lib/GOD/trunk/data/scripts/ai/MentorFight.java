package ai;

import l2rt.extensions.scripts.Functions;
import l2rt.gameserver.ai.DefaultAI;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.network.serverpackets.SocialAction;
import l2rt.gameserver.model.L2World;
import l2rt.util.Location;
import l2rt.util.Rnd;

public class MentorFight extends DefaultAI
{
	private int[] lastAction = {5,8,9,10};
	private int action;
	private int lastactionId = 0;
	public MentorFight(L2Character actor)
	{
		super(actor);
		this.AI_TASK_DELAY = 1500;
		this.AI_TASK_ACTIVE_DELAY = 1500;
	}

	@Override
	public boolean isGlobalAI()
	{
		return true;
	}

	@Override
	protected boolean thinkActive()
	{
		L2NpcInstance actor = getActor();
		if(actor == null)
			return true;
		
		
		if (lastactionId == 0){
			action = lastAction[Rnd.get(0,3)];
			actor.broadcastPacket(new SocialAction(actor.getObjectId(), action));	
			lastactionId = 1;
		}
		else if (lastactionId == 1) {
			actor.broadcastPacket(new SocialAction(actor.getObjectId(), 6));
			lastactionId = 2;
		}
		
		else if (lastactionId == 2) {
		for(L2NpcInstance target : L2World.getAroundNpc(actor, 500, 200))
			if (target != null && target.getNpcId() == 33018)
					target.broadcastPacket(new SocialAction(target.getObjectId(), action));
		lastactionId = 3;			
		}
		else if (lastactionId == 3)
			lastactionId = 0;
			
		return true;
	}
	
	@Override
	protected boolean randomWalk()
	{
		return false;
	}
}