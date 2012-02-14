package ai;

import l2rt.gameserver.ai.DefaultAI;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.util.Rnd;

/**
 * @author Kazumi, Angy
 */
public class MutationDrake extends DefaultAI
{
	public MutationDrake(L2Character actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtAttacked(L2Character attacker, int damage)
	{
		L2NpcInstance actor = getActor();
		if(actor == null || attacker == null)
		{
			return;
		}
		double hpPercent = actor.getCurrentHp() / actor.getMaxHp() * 100;
		int chance = Rnd.get(100);
		if(hpPercent > 75)
		{
			if(chance < 75)
			{
				actor.setAggressionTarget(actor.getRandomHated());
			}
		}
		else if(hpPercent > 50)
		{
			if(chance < 50)
			{
				actor.setAggressionTarget(actor.getRandomHated());
			}
		}
		else if(hpPercent > 25)
		{
			if(chance < 25)
			{
				actor.setAggressionTarget(actor.getRandomHated());
			}
		}
		super.onEvtAttacked(attacker, damage);
	}
}