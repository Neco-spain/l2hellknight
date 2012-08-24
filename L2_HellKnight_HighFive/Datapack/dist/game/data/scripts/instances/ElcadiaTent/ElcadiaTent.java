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
package instances.ElcadiaTent;

import l2.hellknight.gameserver.ai.CtrlIntention;
import l2.hellknight.gameserver.instancemanager.InstanceManager;
import l2.hellknight.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.entity.Instance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;
import l2.hellknight.gameserver.network.SystemMessageId;
import l2.hellknight.gameserver.network.serverpackets.SystemMessage;

public class ElcadiaTent extends Quest
{
	private static final String qn = "ElcadiaTent";
	// Values
	private static final int INSTANCE_ID = 158;
	// NPC's
	private static final int Gruff_looking_Man = 32862;
	private static final int Elcadia = 32784;
	// Teleports
	private static final int ENTER = 0;
	private static final int EXIT = 1;
	private static final int[][] TELEPORTS = { { 89706, -238074, -9632 }, { 43316, -87986, -2832 } };
	
	private class ElcadiaTentWorld extends InstanceWorld
	{
		public ElcadiaTentWorld()
		{
		}
	}
	
	private void teleportPlayer(L2PcInstance player, int[] coords, int instanceId)
	{
		player.stopAllEffectsExceptThoseThatLastThroughDeath();
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.setInstanceId(instanceId);
		player.teleToLocation(coords[0], coords[1], coords[2], false);
	}
	
	protected void enterInstance(L2PcInstance player)
	{
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if (world != null)
		{
			if (!(world instanceof ElcadiaTentWorld))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return;
			}
			Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
			if (inst != null)
			{
				teleportPlayer(player, TELEPORTS[ENTER], world.instanceId);
			}
			return;
		}
		final int instanceId = InstanceManager.getInstance().createDynamicInstance("ElcadiaTent.xml");
		
		world = new ElcadiaTentWorld();
		world.instanceId = instanceId;
		world.templateId = INSTANCE_ID;
		world.status = 0;
		InstanceManager.getInstance().addWorld(world);
		
		world.allowed.add(player.getObjectId());
		teleportPlayer(player, TELEPORTS[ENTER], instanceId);
		
		return;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		if (st == null)
			st = newQuestState(player);
		
		if (npc.getNpcId() == Gruff_looking_Man)
		{
			if (player.getQuestState("Q10292_SevenSignsGirlofDoubt") != null && player.getQuestState("Q10292_SevenSignsGirlofDoubt").getState() == State.STARTED)
			{
				enterInstance(player);
				return null;
			}
			else if (player.getQuestState("Q10292_SevenSignsGirlofDoubt") != null && player.getQuestState("Q10292_SevenSignsGirlofDoubt").getState() == State.COMPLETED && player.getQuestState("Q10293_SevenSignsForbiddenBookOfTheElmoreAdenKingdom") == null)
			{
				enterInstance(player);
				return null;
			}
			else if (player.getQuestState("Q10293_SevenSignsForbiddenBookOfTheElmoreAdenKingdom") != null && player.getQuestState("Q10293_SevenSignsForbiddenBookOfTheElmoreAdenKingdom").getState() != State.COMPLETED)
			{
				enterInstance(player);
				return null;
			}
			else if (player.getQuestState("Q10293_SevenSignsForbiddenBookOfTheElmoreAdenKingdom") != null && player.getQuestState("Q10293_SevenSignsForbiddenBookOfTheElmoreAdenKingdom").getState() == State.COMPLETED && player.getQuestState("Q10294_SevenSignsToTheMonasteryOfSilence") == null)
			{
				enterInstance(player);
				return null;
			}
			else if (player.getQuestState("Q10296_SevenSignsOneWhoSeeksThePowerOfTheSeal") != null && player.getQuestState("Q10296_SevenSignsOneWhoSeeksThePowerOfTheSeal").getInt("cond") == 3)
			{
				enterInstance(player);
				return null;
			}
			else
			{
				htmltext = "32862.html";
			}
		}
		if (npc.getNpcId() == Elcadia)
		{
			teleportPlayer(player, TELEPORTS[EXIT], 0);
			return null;
		}
		return htmltext;
	}
	
	public ElcadiaTent(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(Gruff_looking_Man);
		addTalkId(Gruff_looking_Man);
		addTalkId(Elcadia);
	}
	
	public static void main(String[] args)
	{
		new ElcadiaTent(-1, qn, "instances");
	}
}
