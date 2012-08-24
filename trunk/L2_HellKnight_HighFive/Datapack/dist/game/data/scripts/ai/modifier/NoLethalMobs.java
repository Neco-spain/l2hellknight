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
package ai.modifier;

import java.util.Collection;

import ai.group_template.L2AttackableAIScript;

import l2.hellknight.gameserver.datatables.SpawnTable;
import l2.hellknight.gameserver.model.L2Spawn;
import l2.hellknight.gameserver.model.actor.L2Npc;

public class NoLethalMobs extends L2AttackableAIScript
{
	// Add here the IDs of the mobs that should have no lethals
	private final static int[] NO_LETHAL_LIST = 
	{
		18554, 18555, 18556, 18557, 18558, 18559, 18560, 18561, 18562,
		18563, 18564, 18565, 18566, 18567, 18568, 18569, 18570, 18571,
		18572, 18573, 18574, 18575, 18576, 18577, 18578, 18607, 18608,
		18609, 18610, 18620, 18628, 18629, 18630, 18631, 18632, 18633,
		18660, 22215, 22216, 22217, 29119,
		// Shadow of Halisha
		25339, 25342, 25346, 25349, 
		//HeadQuarters
		35062
	};

	public NoLethalMobs(int questId, String name, String descr)
	{
		super(questId, name, descr);

		final Collection<L2Spawn> spawns =  SpawnTable.getInstance().getSpawnTable();
		for (L2Spawn npc : spawns)
		{
			if (contains(NO_LETHAL_LIST, npc.getTemplate()._npcId))
			{
				if (npc.getLastSpawn() != null)
					npc.getLastSpawn().setIsNoLethal(true);
			}
		}

		for (int npcid : NO_LETHAL_LIST)
			addSpawnId(npcid);
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		if (contains(NO_LETHAL_LIST, npc.getNpcId()))
			npc.setIsNoLethal(true);

		return super.onSpawn(npc);
	}

	public static void main(String[] args)
	{
		new NoLethalMobs(-1, "NoLethalMobs", "ai");
	}
}
