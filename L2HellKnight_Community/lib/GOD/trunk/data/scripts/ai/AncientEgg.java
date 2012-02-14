package ai;


import l2rt.gameserver.ai.Fighter;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Spawn;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.tables.NpcTable;
 
public class AncientEgg extends Fighter
{
	private boolean _firstTimeAttacked = true;
	private static final int[] BROTHERS = { 22196, 22199, 22200, 22203 };

	public AncientEgg(L2Character actor)
	{
		super(actor);
	}
	
	public void onEvtAttacked(L2Character attacker, int damage)
	{
		L2NpcInstance actor = getActor();
		if (this._firstTimeAttacked)
	{
		this._firstTimeAttacked = false;
	
		try
		{
			L2Spawn spawn1 = new L2Spawn(NpcTable.getTemplate(22196));
			spawn1.setLoc(attacker.getLoc());
			spawn1.doSpawn(true);
			spawn1.stopRespawn();

			L2Spawn spawn2 = new L2Spawn(NpcTable.getTemplate(22199));
			spawn2.setLoc(attacker.getLoc());
			spawn2.doSpawn(true);
			spawn2.stopRespawn();
			
			L2Spawn spawn3 = new L2Spawn(NpcTable.getTemplate(22200));
			spawn3.setLoc(attacker.getLoc());
			spawn3.doSpawn(true);
			spawn3.stopRespawn();

			L2Spawn spawn4 = new L2Spawn(NpcTable.getTemplate(22203));
			spawn4.setLoc(attacker.getLoc());
			spawn4.doSpawn(true);
			spawn4.stopRespawn();

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	super.onEvtAttacked(attacker, damage);
	}
	
	protected boolean randomWalk()
	{
		return false;
	}
	
	protected void onEvtDead(L2Character killer)
	{
		this._firstTimeAttacked = true;
	
		super.onEvtDead(killer);
	}
}
