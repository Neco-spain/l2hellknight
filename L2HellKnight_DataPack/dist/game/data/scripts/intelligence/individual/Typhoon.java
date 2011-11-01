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

import l2.hellknight.gameserver.instancemanager.RaidBossSpawnManager;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.actor.instance.L2RaidBossInstance;
import l2.hellknight.gameserver.skills.SkillHolder;

/**
 * @author GKR
 */
public class Typhoon extends L2AttackableAIScript
{
	private static final int TYPHOON = 25539;
	
	private static SkillHolder STORM = new SkillHolder(5434, 1);
	
	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("cast") && (npc != null) && !npc.isDead())
		{
			npc.doSimultaneousCast(STORM.getSkill());
			startQuestTimer("cast", 5000, npc, null);
		}
		return null;
	}
	
	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		npc.doSimultaneousCast(STORM.getSkill());
		return super.onAggroRangeEnter(npc, player, isPet);
	}
	
	@Override
	public final String onSpawn(L2Npc npc)
	{
		if (!npc.isTeleporting())
		{
			startQuestTimer("cast", 5000, npc, null);
		}
		
		return super.onSpawn(npc);
	}
	
	public Typhoon(int id, String name, String descr)
	{
		super(id, name, descr);
		
		addAggroRangeEnterId(TYPHOON);
		addSpawnId(TYPHOON);
		
		final L2RaidBossInstance boss = RaidBossSpawnManager.getInstance().getBosses().get(TYPHOON);
		if (boss != null)
		{
			onSpawn(boss);
		}
	}
	
	public static void main(String[] args)
	{
		new Typhoon(-1, "typhoon", "ai");
	}
}
