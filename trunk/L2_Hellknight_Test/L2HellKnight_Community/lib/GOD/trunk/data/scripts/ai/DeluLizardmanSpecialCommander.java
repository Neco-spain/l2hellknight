package ai;

import l2rt.extensions.scripts.Functions;
import l2rt.gameserver.ai.Ranger;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.util.Rnd;

/**
 * AI для Delu Lizardman Commander Agent ID: 21107
 */
public class DeluLizardmanSpecialCommander extends Ranger
{
	private boolean _firstTimeAttacked = true;

	public DeluLizardmanSpecialCommander(L2Character actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtAttacked(L2Character attacker, int damage)
	{
		L2NpcInstance actor = getActor();
		if(actor == null)
			return;
		if(_firstTimeAttacked)
		{
			_firstTimeAttacked = false;
			if(Rnd.chance(40))
				Functions.npcSay(actor, "Come on, Ill take you on!");
		}
		else if(Rnd.chance(15))
			Functions.npcSay(actor, "How dare you interrupt a sacred duel! You must be taught a lesson!");
		super.onEvtAttacked(attacker, damage);
	}

	@Override
	protected void onEvtDead(L2Character killer)
	{
		_firstTimeAttacked = true;
		super.onEvtDead(killer);
	}
}