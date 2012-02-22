package zone_scripts.ForgeOfTheGods.ValakasLair;

import l2.hellknight.Config;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.bflmpsvz.a.L2AttackableAIScript;

public class FireDragonMinions extends L2AttackableAIScript
{
	private static final int FireDragon = 29028;
	private static final int PUSTBON = 29029;
	
	public int FireDragonStatus;
	
	public FireDragonMinions(int id, String name, String descr)
	{
		super(id, name, descr);
		addKillId(FireDragon);
		addAttackId(FireDragon);
		addSpawnId(FireDragon);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		if (npc.getNpcId() == FireDragon)
			FireDragonStatus = 0;
		
		return super.onSpawn(npc);
	}
	
	private void SpawnMobs(L2Npc npc)
	{
		addSpawn(PUSTBON, 211555, -113281, -1636, 0, false, 0, false, npc.getInstanceId());
		addSpawn(PUSTBON, 212558, -112708, -1639, 0, false, 0, false, npc.getInstanceId());
		addSpawn(PUSTBON, 214460, -113874, -1636, 0, false, 0, false, npc.getInstanceId());
		addSpawn(PUSTBON, 214498, -115229, -1636, 0, false, 0, false, npc.getInstanceId());
		addSpawn(PUSTBON, 214256, -116424, -1636, 0, false, 0, false, npc.getInstanceId());
		addSpawn(PUSTBON, 213214, -116647, -1636, 0, false, 0, false, npc.getInstanceId());
		addSpawn(PUSTBON, 212590, -116376, -1636, 0, false, 0, false, npc.getInstanceId());
		addSpawn(PUSTBON, 211801, -116142, -1636, 0, false, 0, false, npc.getInstanceId());
		addSpawn(PUSTBON, 210882, -114370, -1636, 0, false, 0, false, npc.getInstanceId());
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if (npc.getNpcId() == FireDragon)
		{
			final int maxHp = npc.getMaxHp();
			final double nowHp = npc.getStatus().getCurrentHp();
			
			switch (FireDragonStatus)
			{
				case 0:
					if (nowHp < maxHp * 0.9)
					{
						FireDragonStatus = 1;
						SpawnMobs(npc);
					}
					break;
			}
		}
		
		return super.onAttack(npc, attacker, damage, isPet);
	}

	public static void main(String[] args)
	{
		new FireDragonMinions(-1, "FireDragonMinions", "ai");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Forge Of The Gods: Valakas Lair - Fire Dragon Minions");
	}
}
