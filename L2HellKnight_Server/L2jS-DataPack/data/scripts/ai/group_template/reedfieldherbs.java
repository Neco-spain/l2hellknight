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
import com.l2js.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2js.gameserver.model.actor.instance.L2PcInstance;
import com.l2js.util.Rnd;

public class reedfieldherbs extends L2AttackableAIScript
{
	private static final int[] mobs =
	{
		22650,22651,22652,22653,22654,22655,22656,22657,22658,22659
	};

	public reedfieldherbs(int questId, String name, String descr)
	{
		super(questId, name, descr);
		for (int id : mobs)
			addKillId(id);
	}

	@Override
	public String onKill (L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		int r = Rnd.get(100);
		if (r <= 55)
			((L2MonsterInstance)npc).dropItem(killer, 8603, 1);
		else if (r < 85)
			((L2MonsterInstance)npc).dropItem(killer, 8604, 1);
		else if (r < 90)
			((L2MonsterInstance)npc).dropItem(killer, 8605, 1);
		return super.onKill(npc,killer,isPet);
	}

	public static void main(String[] args)
	{
		new reedfieldherbs(-1, "reedfieldherbs", "ai");
	}
}
