package ai;

import l2rt.extensions.scripts.Functions;
import l2rt.gameserver.ai.DefaultAI;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.util.Rnd;

/**
 * @author Unkonown, Angy
 *         mobId 20270
 */
public class BrekaOrcOverlord extends DefaultAI
{
	private static boolean _firstTimeAttacked;

	public BrekaOrcOverlord(L2Character actor)
	{
		super(actor);
		_firstTimeAttacked = true;
	}

	@Override
	protected void onEvtAttacked(L2Character attacker, int damage)
	{
		L2NpcInstance actor = getActor();
		if(actor == null)
		{
			return;
		}
		if(_firstTimeAttacked)
		{
			if(Rnd.get(100) == 50)
			{
				Functions.npcSay(actor, "Extreme strength! ! ! !");
			}
			else if(Rnd.get(100) == 50)
			{
				Functions.npcSay(actor, "Humph, wanted to win me to be also in tender!");
			}
			else if(Rnd.get(100) == 50)
			{
				Functions.npcSay(actor, "Haven't thought to use this unique skill for this small thing!");
			}
			_firstTimeAttacked = false;
		}
		super.onEvtAttacked(attacker, damage);
	}

	@Override
	protected void onEvtDead(L2Character killer)
	{
		_firstTimeAttacked = true;
		super.onEvtDead(killer);
	}
}