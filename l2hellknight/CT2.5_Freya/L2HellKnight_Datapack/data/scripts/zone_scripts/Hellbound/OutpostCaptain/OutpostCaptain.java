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

/*
 * by SquiD
 */


package zone_scripts.Hellbound.OutpostCaptain;

import l2.hellknight.gameserver.datatables.DoorTable;
import l2.hellknight.gameserver.instancemanager.HellboundManager;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2DoorInstance;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;

public class OutpostCaptain extends Quest
{
	private static final int CAPTAIN = 18466;
	private static final int[] DEFENDERS = { 22357, 22358 };
	private static final int DOORKEEPER = 32351;

	@Override
	public String onAdvEvent (String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("level_up"))
		{
			npc.deleteMe();
			HellboundManager.getInstance().setLevel(9);
		}
		
		return null;
	}

	@Override
	public final String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if (HellboundManager.getInstance().getLevel() == 8)
			addSpawn(DOORKEEPER, npc.getSpawn().getLocx(), npc.getSpawn().getLocy(), npc.getSpawn().getLocz(), 0, false, 0, false); 

		return super.onKill(npc, killer, isPet);
	}

	@Override
	public final String onSpawn(L2Npc npc)
	{
		npc.setIsNoRndWalk(true);
		
		if (npc.getNpcId() == CAPTAIN)
		{
			L2DoorInstance door = DoorTable.getInstance().getDoor(20250001);
			if (door != null)
				door.closeMe();
		}
		
		else if (npc.getNpcId() == DOORKEEPER)
			startQuestTimer("level_up", 3000, npc, null);

		return super.onSpawn(npc);
	}

	public OutpostCaptain(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addKillId(CAPTAIN);
		addSpawnId(CAPTAIN);
		addSpawnId(DOORKEEPER);
		
		for (int i : DEFENDERS)
			addSpawnId(i);
	}

	public static void main(String[] args)
	{
		new OutpostCaptain(-1, OutpostCaptain.class.getSimpleName(), "ai");
	}
}