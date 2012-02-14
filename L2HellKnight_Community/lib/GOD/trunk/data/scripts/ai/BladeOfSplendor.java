package ai;
 
import l2rt.extensions.scripts.Functions;
import l2rt.gameserver.ai.CtrlEvent;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.instances.L2MonsterInstance;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.tables.NpcTable;
import l2rt.util.Rnd;
 
public class BladeOfSplendor extends RndTeleportFighter
{
	private static final int[] CLONES = { 21525 };

	private boolean _firstTimeAttacked = true;

	public BladeOfSplendor(L2Character actor)
	{
		super(actor);
		this.AI_TASK_DELAY = 1000;
		this.AI_TASK_ACTIVE_DELAY = 100000;
	}

	protected void onEvtAttacked(L2Character attacker, int damage)
	{
		L2NpcInstance actor = getActor();
		if (actor == null)
			return;
		if ((!actor.isDead()) && (this._firstTimeAttacked))
		{
			this._firstTimeAttacked = false;
			Functions.npcSay(actor, "Now I Know Why You Wanna Hate Me");
			for (int bro : CLONES)
				try
			{
					L2NpcInstance npc = NpcTable.getTemplate(bro).getNewInstance();
					npc.setSpawnedLoc(((L2MonsterInstance)actor).getMinionPosition());
					npc.setReflection(actor.getReflection());
					npc.onSpawn();
					npc.spawnMe(npc.getSpawnedLoc());
					npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, Integer.valueOf(Rnd.get(1, 1000)));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		super.onEvtAttacked(attacker, damage);
	}

	protected void onEvtDead(L2Character killer)
	{
		this._firstTimeAttacked = true;
		super.onEvtDead(killer);
	}
}

/* Location:           C:\Users\Baltasar\Desktop\scripts.jar
 * Qualified Name:     ai.BladeOfSplendor
 * JD-Core Version:    0.6.0
 */