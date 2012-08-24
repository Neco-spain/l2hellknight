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

import l2.hellknight.gameserver.ai.CtrlIntention;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.network.NpcStringId;
import l2.hellknight.gameserver.network.serverpackets.NpcSay;
import l2.hellknight.util.Rnd;

public class TimakOrcOverlord extends L2AttackableAIScript
{
	private static final int TIMAK_ORC_OVERLORD = 20588;
	
	public TimakOrcOverlord(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addAttackId(TIMAK_ORC_OVERLORD);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet)
	{
		if (npc.getNpcId() == TIMAK_ORC_OVERLORD)
		{
			if (npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_ATTACK)
			{
				if (Rnd.get(100) < 50)
					npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), NpcStringId.DEAR_ULTIMATE_POWER));
			}
		}
		
		return super.onAttack(npc, player, damage, isPet);
	}
	
	public static void main(String[] args)
	{
		new TimakOrcOverlord(-1, "TimakOrcOverlord", "ai");
	}
}