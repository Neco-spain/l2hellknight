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


public class NoAttackingMobs extends L2AttackableAIScript
{
	// Add here the IDs of the mobs that should never be champion
	private final static int[] NO_ATTACKING_LIST =
	{
		// Eye of Kasha
		18812, 18813, 18814,
		// Queen Ant Nurses
		29003
	};

	public NoAttackingMobs(int questId, String name, String descr)
	{
		super(questId, name, descr);

		final Collection<L2Spawn> spawns =  SpawnTable.getInstance().getSpawnTable();
		for (L2Spawn npc : spawns)
		{
			if (contains(NO_ATTACKING_LIST, npc.getTemplate()._npcId))
			{
				if (npc.getLastSpawn() != null)
					npc.getLastSpawn().setIsAttackDisabled(true);
			}
		}

		for (int npcid : NO_ATTACKING_LIST)
			addSpawnId(npcid);
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		if (contains(NO_ATTACKING_LIST, npc.getNpcId()))
			npc.setIsAttackDisabled(true);

		return super.onSpawn(npc);
	}

	public static void main(String[] args)
	{
		new NoAttackingMobs(-1, "NoAttackingMobs", "ai");
	}
}
