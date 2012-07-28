/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package intelligence.individual;

import intelligence.group_template.L2AttackableAIScript;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;

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
		addSpawn(PUSTBON, 214209, -116394, -1636, 0, false, 0, false, npc.getInstanceId());
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
			
			// When 80%, 60%, 40%, 30%, 20%, 10% and 5% spawn minions
			switch (FireDragonStatus)
			{
				case 0:
					if (nowHp < maxHp * 0.8)
					{
						FireDragonStatus = 1;
						SpawnMobs(npc);
					}
					break;
				case 1:
					if (nowHp < maxHp * 0.6)
					{
						FireDragonStatus = 2;
						SpawnMobs(npc);
					}
					break;
				case 2:
					if (nowHp < maxHp * 0.4)
					{
						FireDragonStatus = 3;
						SpawnMobs(npc);
					}
					break;
				case 3:
					if (nowHp < maxHp * 0.3)
					{
						FireDragonStatus = 4;
						SpawnMobs(npc);
					}
					break;
				case 4:
					if (nowHp < maxHp * 0.2)
					{
						FireDragonStatus = 5;
						SpawnMobs(npc);
					}
					break;
				case 5:
					if (nowHp < maxHp * 0.1)
					{
						FireDragonStatus = 6;
						SpawnMobs(npc);
					}
					break;
				case 6:
					if (nowHp < maxHp * 0.05)
					{
						FireDragonStatus = 7;
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
	}
}
