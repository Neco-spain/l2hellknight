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
package instances.ToTheMonastery;

import javolution.util.FastList;
import javolution.util.FastMap;

import l2.hellknight.gameserver.ai.CtrlIntention;
import l2.hellknight.gameserver.instancemanager.InstanceManager;
import l2.hellknight.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.entity.Instance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.network.NpcStringId;
import l2.hellknight.gameserver.network.SystemMessageId;
import l2.hellknight.gameserver.network.clientpackets.Say2;
import l2.hellknight.gameserver.network.serverpackets.NpcSay;
import l2.hellknight.gameserver.network.serverpackets.SystemMessage;
import l2.hellknight.util.Rnd;

/**
 * @author SquiD
 * Elcadia Talk and buff with animations.
 *
 */

public class ToTheMonastery extends Quest
{
	private static final String qn = "ToTheMonastery";
	// Values
	private static final int INSTANCE_ID = 151;
	// NPC's
	private static final int Elcadia_Support = 32785;
	private static final int OddGlobe = 32815;
	// Teleports
	private static final int ENTER = 0;
	private static final int HOLYGRAL = 1;
	private static final int EXIT = 2;
	private static final int TW = 3;
	private static final int TN = 4;
	private static final int TE = 5;
	private static final int TS = 6;
	private static final int RTE = 7;
	private static final int RTG = 8;
	
	private static int WestTeleportControlDevice = 3816;
	private static int NorthTeleportControlDevice = 3817;
	private static int EastTeleportControlDevice = 3818;
	private static int SouthTeleportControlDevice = 3819;
	
	private static int RelicGuardian = 32803;
	
	private static int ErissEvilThoughts = 32792;
	
	private static int WestRelicWatcher = 32804;
	private static int NorthRelicWatcher = 32805;
	private static int EastRelicWatcher = 32806;
	private static int SouthRelicWatcher = 32807;
	
	private static final int[][] TELEPORTS = 
	{
		{ 120664, -86968, -3392 }, //Enter	
		{ 85937, -249618, -8320 }, //HolyBurailOpen
		{ 115512, -85000, -339  }, //ExitInstance
		{ 82434, -249546, -8320 }, //TeleWest
		{ 85691, -252426, -8320 }, //TeleNorth
		{ 88573, -249556, -8320 }, //TeleEast
		{ 85675, -246630, -8320 }, //TeleSouth
		{ 120727, -86868, -3392 }, //ReturnToEris
		{ 85937, -249618, -8320 }, //ReturnToGuardian

	};
	
	private static final NpcStringId[] spam = {
		NpcStringId.I_MUST_ASK_LIBRARIAN_SOPHIA_ABOUT_THE_BOOK,
		NpcStringId.THIS_LIBRARY_ITS_HUGE_BUT_THERE_ARENT_MANY_USEFUL_BOOKS_RIGHT,
		NpcStringId.AN_UNDERGROUND_LIBRARY_I_HATE_DAMP_AND_SMELLY_PLACES,
		NpcStringId.THE_BOOK_THAT_WE_SEEK_IS_CERTAINLY_HERE_SEARCH_INCH_BY_INCH
	};
	private final FastMap<Integer, InstanceHolder> instanceWorlds = new FastMap<Integer, InstanceHolder>();
	
	private static class InstanceHolder
	{
		FastList<L2Npc> mobs = new FastList<L2Npc>();
	}
	
	private class ToTheMonasteryWorld extends InstanceWorld
	{
		public ToTheMonasteryWorld()
		{
		}
	}
	
	private void teleportPlayer(L2Npc npc, L2PcInstance player, int[] coords, int instanceId)
	{
		InstanceHolder holder = instanceWorlds.get(instanceId);
		if (holder == null && instanceId > 0)
		{
			holder = new InstanceHolder();
			instanceWorlds.put(instanceId, holder);
		}
		player.stopAllEffectsExceptThoseThatLastThroughDeath();
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.setInstanceId(instanceId);
		player.teleToLocation(coords[0], coords[1], coords[2], false);
		cancelQuestTimer("check_follow", npc, player);
		if (holder != null)
		{
			for(L2Npc h : holder.mobs)
			{
				h.deleteMe();
			}
			holder.mobs.clear();
		}
		if (instanceId > 0)
		{
			L2Npc support = addSpawn(Elcadia_Support, player.getX(), player.getY(),player.getZ(), 0, false, 0, false, player.getInstanceId());
			holder.mobs.add(support);
			startQuestTimer("check_follow", 3000, support, player);
		}
	}
	
	protected void enterInstance(L2Npc npc, L2PcInstance player)
	{
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if (world != null)
		{
			if (!(world instanceof ToTheMonasteryWorld))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return;
			}
			Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
			if (inst != null)
			{
				teleportPlayer(npc, player, TELEPORTS[ENTER], world.instanceId);
			}
			return;
		}
		final int instanceId = InstanceManager.getInstance().createDynamicInstance("ToTheMonastery.xml");
		
		world = new ToTheMonasteryWorld();
		world.instanceId = instanceId;
		world.templateId = INSTANCE_ID;
		world.status = 0;
		InstanceManager.getInstance().addWorld(world);
		
		world.allowed.add(player.getObjectId());
		
		teleportPlayer(npc, player, TELEPORTS[ENTER], instanceId);
		return;
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{		
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState("Q10294_SevenSignToTheMonastery");
		int npcId = npc.getNpcId();
		if (st == null)
			st = newQuestState(player);		
		if ("check_follow".equals(event))
		{
			cancelQuestTimer("check_follow", npc, player);
			npc.getAI().stopFollow();
			npc.setIsRunning(true);
			npc.getAI().startFollow(player);
			npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.ALL, npc.getNpcId(), spam[Rnd.get(0, spam.length - 1)]));
			startQuestTimer("check_follow", 20000, npc, player);
			return "";
		}
		else if("buff".equals(event))
		{
		   // TODO BUFF PLAYER
		}
		else if("movie".equals(event))
		{
			player.showQuestMovie(23);
			return null;
		}
		else if (npcId == OddGlobe)
		{
			if ("enter".equals(event))
			{
				enterInstance(npc, player);			
				return null;
			}
		}
		else if (npcId == ErissEvilThoughts)
		{
			if ("Enter3".equals(event))
			{		
				startQuestTimer("movie", 4000, npc, player);
				teleportPlayer(npc, player, TELEPORTS[HOLYGRAL], player.getInstanceId());
				return null;
			}
			else if ("Exit".equals(event))
			{
				InstanceHolder holder = instanceWorlds.get(player.getInstanceId());
				if (holder != null)
				{
					for(L2Npc h : holder.mobs)
					{
						h.deleteMe();
					}
					holder.mobs.clear();
				}
				teleportPlayer(npc, player, TELEPORTS[EXIT], 0);
				return null;
			}			
		}	
		else if (npcId == WestTeleportControlDevice)
		{
			if ("TeleWest".equals(event))
			{
				teleportPlayer(npc, player, TELEPORTS[TW], player.getInstanceId());
				return null;
			}
		}
		else if (npcId == NorthTeleportControlDevice)
		{
			if ("TeleNorth".equals(event))
			{
				teleportPlayer(npc, player, TELEPORTS[TN], player.getInstanceId());
				return null;
			}
		}	
		else if (npcId == EastTeleportControlDevice)
		{
			if ("TeleEast".equals(event))
			{
				teleportPlayer(npc, player, TELEPORTS[TE], player.getInstanceId());
				return null;
			}
		}	
		else if (npcId == SouthTeleportControlDevice)
		{
			if ("TeleSouth".equals(event))
			{
				teleportPlayer(npc, player, TELEPORTS[TS], player.getInstanceId());
				return null;
			}
		}	
		else if (npcId == RelicGuardian)
		{
			if ("ReturnToEris".equals(event))
			{
				teleportPlayer(npc, player, TELEPORTS[RTE], player.getInstanceId());
				return null;
			}
		}
		else if (npcId == WestRelicWatcher || npcId == NorthRelicWatcher || npcId == EastRelicWatcher || npcId == SouthRelicWatcher)
		{
			if ("ReturnToGuardian".equals(event))
			{
				teleportPlayer(npc, player, TELEPORTS[RTG], player.getInstanceId());
				return null;
			}
		}
		return htmltext;	
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);

		if (st == null)
		{
			return htmltext;
		}
		
		int npcId = npc.getNpcId();
		int cond = st.getInt("cond");
		
		if(npcId == OddGlobe && cond == 1 || cond == 2)
			htmltext = "32815-00.htm";
		else
			htmltext = getNoQuestMsg(player);
		return htmltext;	
	}	
	
	public ToTheMonastery(int questId, String name, String descr)
	{
		super(questId, name, descr);	
		addStartNpc(OddGlobe);
		addTalkId(OddGlobe,Elcadia_Support, RelicGuardian, WestRelicWatcher, NorthRelicWatcher, EastRelicWatcher, SouthRelicWatcher, ErissEvilThoughts);
		addTalkId(WestTeleportControlDevice, NorthTeleportControlDevice, EastTeleportControlDevice, SouthTeleportControlDevice);
		addFirstTalkId(OddGlobe);
	}
	
	public static void main(String[] args)
	{
		new ToTheMonastery(-1, qn, "instances");
	}
}
