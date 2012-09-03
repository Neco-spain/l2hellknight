package ai;

import static l2rt.gameserver.ai.CtrlIntention.AI_INTENTION_ATTACK;
import l2rt.extensions.scripts.Functions;
import l2rt.gameserver.ai.Fighter;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.ai.CtrlIntention;
import l2rt.gameserver.model.L2World;
import l2rt.util.Location;
import l2rt.util.Rnd;

public class HackLstec2 extends Fighter
{
	public HackLstec2(L2Character actor)
	{
		super(actor);
	}

	@Override
	public boolean isGlobalAI()
	{
		return true;
	}
	
	@Override
	protected boolean randomWalk()
	{
		return false;
	}
	
	@Override
	protected void onIntentionAttack(L2Character target)
	{
		if (target != null && target instanceof L2Player)
			return;
		else 
			super.onIntentionAttack(target);
	}

	@Override
	protected void onEvtAggression(L2Character target, int aggro)
	{}

	@Override
	protected boolean thinkActive()
	{
		L2NpcInstance actor = getActor();
		if(actor == null)
			return false;
			
			if (getIntention() != AI_INTENTION_ATTACK) {
				for(L2NpcInstance target : L2World.getAroundNpc(actor, 100, 200))
				{
					if (target != null && target.getNpcId() == 22991) {
						target.addDamageHate(actor, 0, 100);
						setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
					}
				}
			}
	
		return true;
	}
}