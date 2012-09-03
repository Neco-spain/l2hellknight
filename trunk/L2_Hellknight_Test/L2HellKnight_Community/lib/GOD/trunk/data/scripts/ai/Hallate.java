package ai;

import l2rt.gameserver.ai.Fighter;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.instances.L2NpcInstance;

	public class Hallate extends Fighter
	{
		private static final int z1 = -2150;
		private static final int z2 = -1650;

		public Hallate(L2Character actor)
		{
			super(actor);
			this.AI_TASK_DELAY = 1000;
			this.AI_TASK_ACTIVE_DELAY = 1000;
		}

		protected void onEvtAttacked(L2Character attacker, int damage)
		{
			L2NpcInstance actor = getActor();
			int z = actor.getZ();
			if ((z > -1650) || (z < -2150))
			{
				actor.teleToLocation(113548, 17061, -2125);
				actor.setCurrentHp(actor.getMaxHp(), false);
			}
			super.onEvtAttacked(attacker, damage);
		}
	}