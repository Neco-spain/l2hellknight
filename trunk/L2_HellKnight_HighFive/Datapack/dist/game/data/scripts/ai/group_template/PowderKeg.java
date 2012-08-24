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

import l2.hellknight.gameserver.datatables.SkillTable;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.skills.L2Skill;
import l2.hellknight.gameserver.util.Util;

/**
 * ¤õÃÄ±í
 */
public class PowderKeg extends L2AttackableAIScript
{
	private static final int[] NPC_IDS = { 18622 };

	public PowderKeg(int questId, String name, String descr)
	{
		super(questId, name, descr);
		this.registerMobs(NPC_IDS);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		int npcId = npc.getNpcId();

		if (Util.contains(NPC_IDS, npcId))
		{
			L2Skill _boom = SkillTable.getInstance().getInfo(5714, 1);
			npc.disableCoreAI(true);
			npc.doCast(_boom);
			npc.broadcastStatusUpdate();
			return "";
		}
		return super.onKill(npc, killer, isPet);
	}

	public static void main(String[] args)
	{
		new PowderKeg(-1,"powderkeg","ai");
	}
}