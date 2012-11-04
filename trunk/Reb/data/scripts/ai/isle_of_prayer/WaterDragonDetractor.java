package ai.isle_of_prayer;

import l2r.commons.util.Rnd;
import l2r.gameserver.ai.Fighter;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.instances.NpcInstance;

public class WaterDragonDetractor extends Fighter
{
	private static final int SPIRIT_OF_LAKE = 9689;
	private static final int BLUE_CRYSTAL = 9595;

	public WaterDragonDetractor(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		if (killer != null)
		{
			final Player player = killer.getPlayer();
			if (player != null)
			{
				final NpcInstance actor = getActor();
				actor.dropItem(player, SPIRIT_OF_LAKE, 1);
				if (Rnd.chance(10))
					actor.dropItem(player, BLUE_CRYSTAL, 1);
			}
		}
		super.onEvtDead(killer);
	}
}