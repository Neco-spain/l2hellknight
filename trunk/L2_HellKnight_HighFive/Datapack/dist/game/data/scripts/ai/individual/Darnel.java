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
package ai.individual;

import ai.group_template.L2AttackableAIScript;

import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;

public class Darnel extends L2AttackableAIScript
{
	private static final int DARNEL = 25531;

	public Darnel(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addKillId(DARNEL);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		int npcId = npc.getNpcId();

		if (npcId == DARNEL)
			addSpawn(32279, 152761, 145950, -12588, 0, false, 0, false, player.getInstanceId());

		return "";
	}

	public static void main(String[] args)
	{
		new Darnel(-1, "Darnel", "ai");
	}
}