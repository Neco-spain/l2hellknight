package ai.residences.fortress.siege;

import l2p.gameserver.model.Creature;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.serverpackets.components.NpcString;
import l2p.gameserver.scripts.Functions;
import ai.residences.SiegeGuardFighter;

/**
 * @author VISTALL
 * @date 20:10/19.04.2011
 */
public class RebelCommander extends SiegeGuardFighter
{
	public RebelCommander(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	public void onEvtDead(Creature killer)
	{
		super.onEvtDead(killer);

		Functions.npcSay(getActor(), NpcString.DONT_THINK_THAT_ITS_GONNA_END_LIKE_THIS);
	}
}