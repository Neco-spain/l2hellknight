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
package instances.DawnHideout;

import l2.hellknight.gameserver.ai.CtrlIntention;
import l2.hellknight.gameserver.instancemanager.InstanceManager;
import l2.hellknight.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.entity.Instance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.network.SystemMessageId;
import l2.hellknight.gameserver.network.serverpackets.SystemMessage;

public class DawnHideout extends Quest
{
	private static final String qn = "DawnHideout";
	// Values
	private static final int INSTANCE_ID = 113;
	// NPC's
	private static final int _wood = 32593;
	private static final int _jaina = 32617;
	// Teleports
	private static final int ENTER = 0;
	private static final int EXIT = 1;
	private static final int[][] TELEPORTS = {
		{ -23758, -8959, -5384 },
		{ 147062, 23762, -1984 }
	};

	private class DawnHideoutWorld extends InstanceWorld
	{
		public DawnHideoutWorld()
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
			if (!(world instanceof DawnHideoutWorld))
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
		final int instanceId = InstanceManager.getInstance().createDynamicInstance("DawnHideout.xml");

		world = new DawnHideoutWorld();
		world.instanceId = instanceId;
		world.templateId = INSTANCE_ID;
		world.status = 0;
		InstanceManager.getInstance().addWorld(world);

		world.allowed.add(player.getObjectId());
		teleportPlayer(player, TELEPORTS[ENTER], instanceId);

		//_log.info("DisciplesNecropolis instance started: " + instanceId + " created by player: " + player.getName());
		return;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		if (st == null)
			st = newQuestState(player);

		if (npc.getNpcId() == _wood)
		{
			enterInstance(player);
			return "enter.htm";			
		}
		if (npc.getNpcId() == _jaina)
		{
			teleportPlayer(player,TELEPORTS[EXIT],0);
			return "exit.htm";
		}
		return htmltext;
	}
	public DawnHideout(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(_wood);
		addTalkId(_wood);
		addTalkId(_jaina);
	}

	public static void main(String[] args)
	{
		new DawnHideout(-1, qn, "instances");
	}
}