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
package intelligence.group_template;

import l2.brick.gameserver.datatables.SpawnTable;
import l2.brick.gameserver.model.L2Spawn;
import l2.brick.gameserver.model.actor.L2Attackable;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.util.Util;

public class SeeThroughSilentMove extends L2AttackableAIScript
{
	private static final int[] MOBIDS =
	{
		18001,18002, // Blood Queen
		22199,22215,22216,22217, // Primeval Isle
		22746,22747,22748,22749,22750,22751,22752,22753,22754,22755,22756,22757,22758,22759,22760,22761,22762,22763,22764,22765,22794,22795,22796,22797,22798,22799,22800, // SOA
		29009,29010,29011,29012,29013, // Cruma Tower
		22719,22720,22721,22722,22723,22724,22725,22726,22727,22728,22730,22731,22732,22733,22734,22735,22736,22737,22738,22739,22740, // Delusion Chamber
		22820,22821,22831,22832,22833,22834,22843,22857,22859,25718,25719,25720,25721,25722,25723,25724,25725,25726,25727 //DV + LoA
	};
	
	public SeeThroughSilentMove(int questId, String name, String descr)
	{
		super(questId, name, descr);
		for (L2Spawn npc : SpawnTable.getInstance().getSpawnTable())
			if (Util.contains(MOBIDS,npc.getNpcid()) && npc.getLastSpawn() != null && npc.getLastSpawn() instanceof L2Attackable)
				((L2Attackable)npc.getLastSpawn()).setSeeThroughSilentMove(true);
		registerMobs(MOBIDS, QuestEventType.ON_SPAWN);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		if (npc instanceof L2Attackable)
			((L2Attackable)npc).setSeeThroughSilentMove(true);
		return super.onSpawn(npc);
	}
	
	public static void main(String[] args)
	{
		new SeeThroughSilentMove(-1, "SeeThroughSilentMove", "ai");
	}
}