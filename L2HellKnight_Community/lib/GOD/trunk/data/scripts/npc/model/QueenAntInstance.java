package npc.model;

import l2rt.extensions.scripts.Functions;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Spawn;
import l2rt.gameserver.model.instances.L2BossInstance;
import l2rt.gameserver.model.instances.L2MinionInstance;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.network.serverpackets.PlaySound;
import l2rt.gameserver.tables.NpcTable;
import l2rt.gameserver.templates.L2NpcTemplate;
import l2rt.util.GArray;
import l2rt.util.Location;
import l2rt.util.Rnd;

public class QueenAntInstance extends L2BossInstance
{
	private static final int Queen_Ant_Larva = 29002;

	private GArray<L2Spawn> _spawns = new GArray<L2Spawn>();
	private L2NpcInstance Larva = null;

	public QueenAntInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	public L2NpcInstance getLarva()
	{
		if(Larva == null)
			Larva = SpawnNPC(Queen_Ant_Larva, new Location(-21600, 179482, -5846, Rnd.get(0, 0xFFFF)));
		return Larva;
	}

	@Override
	protected int getKilledInterval(L2MinionInstance minion)
	{
		return minion.getNpcId() == 29003 ? 10000 : 280000 + Rnd.get(40000);
	}

	@Override
	public void doDie(L2Character killer)
	{
		broadcastPacketToOthers(new PlaySound(1, "BS02_D", 1, 0, getLoc()));
		Functions.deSpawnNPCs(_spawns);
		Larva = null;
		super.doDie(killer);
	}

	@Override
	public void spawnMe()
	{
		super.spawnMe();
		getLarva();
		broadcastPacketToOthers(new PlaySound(1, "BS01_A", 1, 0, getLoc()));
	}

	private L2NpcInstance SpawnNPC(int npcId, Location loc)
	{
		L2NpcTemplate template = NpcTable.getTemplate(npcId);
		if(template == null)
		{
			System.out.println("WARNING! template is null for npc: " + npcId);
			Thread.dumpStack();
			return null;
		}
		try
		{
			L2Spawn sp = new L2Spawn(template);
			sp.setLoc(loc);
			sp.setAmount(1);
			sp.setRespawnDelay(0);
			_spawns.add(sp);
			return sp.spawnOne();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
}