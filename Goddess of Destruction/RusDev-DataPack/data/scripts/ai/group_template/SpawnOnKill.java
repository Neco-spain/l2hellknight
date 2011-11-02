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
package ai.group_template;

import com.l2js.gameserver.model.actor.L2Npc;
import com.l2js.gameserver.model.actor.instance.L2PcInstance;
import com.l2js.util.Rnd;

import gnu.trove.TIntObjectHashMap;

public class SpawnOnKill extends L2AttackableAIScript
{
	private static final TIntObjectHashMap<int[]> SPAWNS = new TIntObjectHashMap<int[]>();
	
	static
	{
		SPAWNS.put(22704, new int[] { 22706 }); //Turka Follower's Ghost
		SPAWNS.put(22705, new int[] { 22707 }); //Turka Commander's Ghost
	}
	
	public SpawnOnKill(int questId, String name, String descr)
	{
		super(questId, name, descr);
		int[] temp =
		{
			22704, 22705
		};
		this.registerMobs(temp);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		int npcId = npc.getNpcId();
		if (SPAWNS.containsKey(npcId))
		{
			if (Rnd.get(100) < 5) //mob that spawn only on certain chance
				for (int val : SPAWNS.get(npcId))
				{
					addSpawn(val, npc);
				}
		}
		return super.onKill(npc, killer, isPet);
	}
	
	public static void main(String[] args)
	{
		// now call the constructor (starts up the ai)
		new SpawnOnKill(-1, "SpawnOnKill", "ai");
	}
}
