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

import java.util.List;

import l2.brick.gameserver.ai.CtrlIntention;
import l2.brick.gameserver.instancemanager.HellboundManager;
import l2.brick.gameserver.model.L2CharPosition;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2MonsterInstance;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.network.NpcStringId;
import l2.brick.gameserver.network.clientpackets.Say2;
import l2.brick.gameserver.network.serverpackets.NpcSay;
import l2.brick.gameserver.taskmanager.DecayTaskManager;

/**
 * @author DS
 */
public class Slaves extends L2AttackableAIScript
{
	private static final int[] MASTERS =
	{
		22320, 22321
	};
	private static final L2CharPosition MOVE_TO = new L2CharPosition(-25451, 252291, -3252, 3500);
	private static final int TRUST_REWARD = 10;
	
	@Override
	public final String onSpawn(L2Npc npc)
	{
		((L2MonsterInstance) npc).enableMinions(HellboundManager.getInstance().getLevel() < 5);
		((L2MonsterInstance) npc).setOnKillDelay(1000);
		
		return super.onSpawn(npc);
	}
	
	// Let's count trust points for killing in Engine
	@Override
	public final String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if (((L2MonsterInstance) npc).getMinionList() != null)
		{
			final List<L2MonsterInstance> slaves = ((L2MonsterInstance) npc).getMinionList().getSpawnedMinions();
			if ((slaves != null) && !slaves.isEmpty())
			{
				for (L2MonsterInstance slave : slaves)
				{
					if ((slave == null) || slave.isDead())
					{
						continue;
					}
					
					slave.clearAggroList();
					slave.abortAttack();
					slave.abortCast();
					slave.broadcastPacket(new NpcSay(slave.getObjectId(), Say2.ALL, slave.getNpcId(), NpcStringId.THANK_YOU_FOR_SAVING_ME_FROM_THE_CLUTCHES_OF_EVIL));
					
					if ((HellboundManager.getInstance().getLevel() >= 1) && (HellboundManager.getInstance().getLevel() <= 2))
					{
						HellboundManager.getInstance().updateTrust(TRUST_REWARD, false);
					}
					
					slave.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO);
					DecayTaskManager.getInstance().addDecayTask(slave);
				}
			}
		}
		return super.onKill(npc, killer, isPet);
	}
	
	public Slaves(int questId, String name, String descr)
	{
		super(questId, name, descr);
		for (int npcId : MASTERS)
		{
			addSpawnId(npcId);
			addKillId(npcId);
		}
	}
	
	public static void main(String[] args)
	{
		new Slaves(-1, "Slaves", "ai");
	}
}
