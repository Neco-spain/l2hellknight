package ai;

import static l2rt.gameserver.ai.CtrlIntention.AI_INTENTION_ACTIVE;
import l2rt.extensions.scripts.Functions;
import l2rt.gameserver.ai.Fighter;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.util.Rnd;

/**
 * AI монахов в Monastery of Silence<br>
 * - агрятся на чаров с оружием в руках
 * - перед тем как броситься в атаку кричат
 */
public class MoSMonk extends Fighter
{
	public MoSMonk(L2Character actor)
	{
		super(actor);
	}

	@Override
	protected void onIntentionAttack(L2Character target)
	{
		L2NpcInstance actor = getActor();
		if(actor == null)
			return;
			
		if(getIntention() == AI_INTENTION_ACTIVE && Rnd.chance(50))
			Functions.npcSay(actor, "Вы не сможете пронести оружие с собой без особого разрешения!");
			//"You cannot carry a weapon without authorization!"
				
		super.onIntentionAttack(target);
	}

	@Override
	public void checkAggression(L2Character target)
	{
		if(target.getActiveWeaponInstance() == null)
			return;
		super.checkAggression(target);
	}
}