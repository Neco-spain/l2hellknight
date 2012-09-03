package ai;

import l2rt.gameserver.ai.CtrlEvent;
import l2rt.gameserver.ai.Fighter;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Spawn;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.tables.NpcTable;
import l2rt.util.Rnd;
 
	public class ForgeOfGods extends Fighter
	{
		private static final int[] MOBS = { 18799, 18800, 18801, 18802, 18803 };
			
		public ForgeOfGods(L2Character actor)
		{
			super(actor);
		}

		protected void onEvtDead(L2Character killer)
		{
			L2NpcInstance actor = getActor();
			if (actor == null) {
				return;
			}
			if (actor.isDead())
			{
				if (Rnd.chance(40))
					for (int i = 0; i < 1; i++)
				try
			{
				L2Spawn spawn = new L2Spawn(NpcTable.getTemplate(MOBS[Rnd.get(MOBS.length)]));
				spawn.setLoc(actor.getLoc());
				L2NpcInstance npc = spawn.doSpawn(true);
				npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, killer, Integer.valueOf(Rnd.get(1, 100)));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		super.onEvtDead(killer);
	}
}