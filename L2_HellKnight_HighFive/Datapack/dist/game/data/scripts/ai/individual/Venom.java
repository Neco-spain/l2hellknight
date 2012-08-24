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

import javolution.util.FastList;
import javolution.util.FastMap;

import ai.group_template.L2AttackableAIScript;

import l2.hellknight.gameserver.ai.CtrlIntention;
import l2.hellknight.gameserver.datatables.DoorTable;
import l2.hellknight.gameserver.datatables.SpawnTable;
import l2.hellknight.gameserver.instancemanager.CastleManager;
import l2.hellknight.gameserver.instancemanager.GlobalVariablesManager;
import l2.hellknight.gameserver.model.L2CharPosition;
import l2.hellknight.gameserver.model.L2Spawn;
import l2.hellknight.gameserver.model.Location;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.L2Attackable;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.network.serverpackets.NpcSay;
import l2.hellknight.gameserver.network.serverpackets.SocialAction;
import l2.hellknight.gameserver.network.serverpackets.SpecialCamera;
import l2.hellknight.util.Rnd;

/**
 * Venom Custom AI
 * 
 * @author L0ngh0rn
 * @since 30/03/2011
 */
public final class Venom extends L2AttackableAIScript
{
	public static final String    	QN				= "Venom";
	private static final int		CASTLE_ID		= 8; //Rune
	private static final int        VENOM           = 29054;
	private static final int        VENOM_TELEPORT  = 13101;
	private static final String[]   VENOM_SPEAK     = 
	{
		"You should have finished me when you had the chance!!!",
		"I will crush all of you!!!",
		"I am not finished here, come face me!!!",
		"You cowards!!! I will torture each and everyone of you!!!"
	};
	
	private static final FastMap<Integer, Location> VENOM_WALK_ROUTES = new FastMap<>();
	
	private static final int[] WALK_TIMES =
	{
		18000,17000,4500,16000,22000,14000,10500,14000,9500,12500,20500,14500,17000,20000,22000,11000,11000,20000,8000,5500,20000,18000,25000,28000,25000,25000,25000,25000,10000,24000,7000,12000,20000 
	};
	
	private L2Npc              _Venom;
	private static final byte  ALIVE               = 0;
	private static final byte  DEAD                = 1;
	private static byte        VenomIsSpawned      = 0;
	private static int         VenomWalkRouteStep  = 0;
	
	static
	{
		VENOM_WALK_ROUTES.put(0, new Location(12565, -49739, -547));
		VENOM_WALK_ROUTES.put(1, new Location(11242, -49689, -33));
		VENOM_WALK_ROUTES.put(2, new Location(10751, -49702, 83));
		VENOM_WALK_ROUTES.put(3, new Location(10824, -50808, 316));
		VENOM_WALK_ROUTES.put(4, new Location(9084, -50786, 972));
		VENOM_WALK_ROUTES.put(5, new Location(9095, -49787, 1252));
		VENOM_WALK_ROUTES.put(6, new Location(8371, -49711, 1252));
		VENOM_WALK_ROUTES.put(7, new Location(8423, -48545, 1252));
		VENOM_WALK_ROUTES.put(8, new Location(9105, -48474, 1252));
		VENOM_WALK_ROUTES.put(9, new Location(9085, -47488, 972));
		VENOM_WALK_ROUTES.put(10, new Location(10858, -47527, 316));
		VENOM_WALK_ROUTES.put(11, new Location(10842, -48626, 75));
		VENOM_WALK_ROUTES.put(12, new Location(12171, -48464, -547));
		VENOM_WALK_ROUTES.put(13, new Location(13565, -49145, -535));
		VENOM_WALK_ROUTES.put(14, new Location(15653, -49159, -1059));
		VENOM_WALK_ROUTES.put(15, new Location(15423, -48402, -839));
		VENOM_WALK_ROUTES.put(16, new Location(15066, -47438, -419));
		VENOM_WALK_ROUTES.put(17, new Location(13990, -46843, -292));
		VENOM_WALK_ROUTES.put(18, new Location(13685, -47371, -163));
		VENOM_WALK_ROUTES.put(19, new Location(13384, -47470, -163));
		VENOM_WALK_ROUTES.put(20, new Location(14609, -48608, 346));
		VENOM_WALK_ROUTES.put(21, new Location(13878, -47449, 747));
		VENOM_WALK_ROUTES.put(22, new Location(12894, -49109, 980));
		VENOM_WALK_ROUTES.put(23, new Location(10135, -49150, 996));
		VENOM_WALK_ROUTES.put(24, new Location(12894, -49109, 980));
		VENOM_WALK_ROUTES.put(25, new Location(13738, -50894, 747));
		VENOM_WALK_ROUTES.put(26, new Location(14579, -49698, 347));
		VENOM_WALK_ROUTES.put(27, new Location(12896, -51135, -166));
		VENOM_WALK_ROUTES.put(28, new Location(12971, -52046, -292));
		VENOM_WALK_ROUTES.put(29, new Location(15140, -50781, -442));
		VENOM_WALK_ROUTES.put(30, new Location(15328, -50406, -603));
		VENOM_WALK_ROUTES.put(31, new Location(15594, -49192, -1059));
		VENOM_WALK_ROUTES.put(32, new Location(13175, -49153, -537));
	}
	
	public Venom(int questId, String name, String descr, String folder)
	{
		super(questId, name, descr);
		
		addStartNpc(VENOM_TELEPORT);
		addTalkId(VENOM_TELEPORT);
		addAggroRangeEnterId(VENOM);
		addKillId(VENOM);
		
		final int castleOwner = CastleManager.getInstance().getCastleById(CASTLE_ID).getOwnerId();
		final long siegeDate = CastleManager.getInstance().getCastleById(CASTLE_ID).getSiegeDate().getTimeInMillis();
		final long currentTime = System.currentTimeMillis();
		long venomTeleporterSpawn = (siegeDate - currentTime) - 86400000;
		final long venomRaidRoomSpawn = (siegeDate - currentTime) - 86400000;
		long venomRaidSiegeSpawn = (siegeDate - currentTime);
		
		
		if (venomTeleporterSpawn < 0)
			venomTeleporterSpawn = 1;
		if (venomRaidSiegeSpawn < 0)
			venomRaidSiegeSpawn = 1;
		
		if (castleOwner > 0)
		{
			if (venomTeleporterSpawn >= 1)
				startQuestTimer("VenomTeleSpawn", venomTeleporterSpawn, null, null);
			
			if ((siegeDate - currentTime) > 0)
				startQuestTimer("VenomRaidRoomSpawn", venomRaidRoomSpawn, null, null);
			
			startQuestTimer("VenomRaidSiegeSpawn", venomRaidSiegeSpawn, null, null);
		}
	}
	
	@Override
	public final String onTalk(L2Npc npc,  L2PcInstance player)
	{
		String htmltext = "";
		if (checkConditions(player))
		{
			player.teleToLocation(12432, -49206, -3011);
			return htmltext;
		}
		htmltext = "<html><body>Venom's Avatar:<br>Conditions are not right to meet Venom.</body></html>";

		return htmltext;
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final int statusBoss = checkStatusBoss();
		if (event.equalsIgnoreCase("VenomTeleSpawn"))
			addSpawn(VENOM_TELEPORT, 11013, -49629, -547, 13400, false, 0);
		else if (event.equalsIgnoreCase("VenomRaidRoomSpawn"))
		{
			if (VenomIsSpawned == 0 && statusBoss == ALIVE)
				_Venom = addSpawn(VENOM, 12047, -49211, -3009, 0, false, 0);
			VenomIsSpawned = 1;
		}
		else if (event.equalsIgnoreCase("VenomRaidSiegeSpawn"))
		{
			if (statusBoss == ALIVE)
			{
				
				switch (VenomIsSpawned)
				{
					case 0:
						_Venom = addSpawn(VENOM, 11025, -49152, -537, 0, false, 0);
						VenomIsSpawned = 1;
						break;
					case 1:
						_Venom.teleToLocation(11025, -49152, -537);
						break;
				}
				
				startQuestTimer("VenomSpawnEffect", 100, _Venom, null);
				startQuestTimer("VenomBossDespawn", 5400000, _Venom, null);
				cancelQuestTimer("VenomSpawn", _Venom, null);
				unSpawnNpc(VENOM_TELEPORT);
			}
		}
		else if (event.equalsIgnoreCase("VenomSpawnEffect"))
		{
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 200, 0, 150, 0, 5000));
			npc.broadcastPacket(new SocialAction(npc.getObjectId(), 3));
			startQuestTimer("VenomWalk", 5000, npc, null);
			VenomWalkRouteStep = 0;
		}
		else if (event.equalsIgnoreCase("Attacking"))
		{
			FastList<L2PcInstance> NumPlayers = new FastList<>();
			for (L2PcInstance plr : npc.getKnownList().getKnownPlayers().values())
				NumPlayers.add(plr);
				
				if (NumPlayers.size() > 0)
				{
					L2PcInstance target = NumPlayers.get(Rnd.get(NumPlayers.size()));
					((L2Attackable)npc).addDamageHate(target, 0, 999);
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
					startQuestTimer("Attacking", 2000, npc, player);
				}
				else if (NumPlayers.size() == 0)
					startQuestTimer("VenomWalkFinish", 2000, npc, null);
		}
		else if (event.equalsIgnoreCase("VenomWalkFinish"))
		{
			if (npc.getCastle().getSiege().getIsInProgress())
				cancelQuestTimer("Attacking", npc, player);
			npc.teleToLocation(
				VENOM_WALK_ROUTES.get(VenomWalkRouteStep).getZ(),
				VENOM_WALK_ROUTES.get(VenomWalkRouteStep).getY(),
				VENOM_WALK_ROUTES.get(VenomWalkRouteStep).getZ(),
				0);
			npc.setWalking();
			VenomWalkRouteStep = 0;
			startQuestTimer("VenomWalk", 2200, npc, null);
		}
		else if (event.equalsIgnoreCase("VenomWalk"))
		{
			if (VenomWalkRouteStep == 33)
			{
				VenomWalkRouteStep = 0;
				startQuestTimer("VenomWalk", 100, npc, null);
			}
			else
			{
				startQuestTimer("Talk", 100, npc, null);
				switch (VenomWalkRouteStep)
				{
					case 14:
						startQuestTimer("DoorOpen", 15000, null, null);
						startQuestTimer("DoorClose", 23000, null, null);
						break;
					case 32:
						startQuestTimer("DoorOpen", 500, null, null);
						startQuestTimer("DoorClose", 4000, null, null);
						break;
				}
				
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(
					VENOM_WALK_ROUTES.get(VenomWalkRouteStep).getX(),
					VENOM_WALK_ROUTES.get(VenomWalkRouteStep).getY(),
					VENOM_WALK_ROUTES.get(VenomWalkRouteStep).getZ(),
					0)
					);
				VenomWalkRouteStep++;
				startQuestTimer("VenomWalk", WALK_TIMES[VenomWalkRouteStep], npc, null);
			}
		}
		else if (event.equalsIgnoreCase("DoorOpen"))
		{
			DoorTable.getInstance().getDoor(20160005).openMe();
		}
		else if (event.equalsIgnoreCase("DoorClose"))
		{
			DoorTable.getInstance().getDoor(20160005).closeMe();
		}
		else if (event.equalsIgnoreCase("Talk"))
		{
			if (Rnd.get(100) < 40)
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), VENOM_SPEAK[Rnd.get(VENOM_SPEAK.length)]));
		}
		else if (event.equalsIgnoreCase("VenomBossDespawn"))
		{
			VenomIsSpawned = 0;
			unSpawnNpc(VENOM);
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		cancelQuestTimer("VenomWalk", npc, null);
		cancelQuestTimer("VenomWalkFinish", npc, null);
		startQuestTimer("Attacking", 100, npc, player);
		return super.onAggroRangeEnter(npc, player, isPet);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		updateStatusBoss(DEAD);
		cancelQuestTimer("VenomWalk", npc, null);
		cancelQuestTimer("VenomWalkFinish", npc, null);
		cancelQuestTimer("VenomBossDespawn", npc, null);
		cancelQuestTimer("Talk", npc, null);
		cancelQuestTimer("Attacking", npc, null);
		return super.onKill(npc, killer, isPet);
	}
	
	private void unSpawnNpc(int npcId)
	{
		for (L2Spawn spawn : SpawnTable.getInstance().getSpawnTable())
			if (spawn.getNpcid() == npcId)
			{
				SpawnTable.getInstance().deleteSpawn(spawn, false);
				L2Npc npc = spawn.getLastSpawn();
				npc.deleteMe();
			}
	}
	
	private boolean checkConditions(L2PcInstance player)
	{		

		if (checkStatusBoss()==DEAD){
			player.sendMessage("Venom is dead, so you cannot enter.");
			return false;
		}

		return true;
	}
	
	private int checkStatusBoss()
	{
		int checkStatus = ALIVE;
		if(GlobalVariablesManager.getInstance().isVariableStored("VenomStatus"))
		{
			checkStatus = Integer.parseInt(GlobalVariablesManager.getInstance().getStoredVariable("VenomStatus"));
		}
		else
		{
			GlobalVariablesManager.getInstance().storeVariable("VenomStatus", "0");
		}
		return checkStatus;
	}
	
	private void updateStatusBoss(int status)
	{
		GlobalVariablesManager.getInstance().storeVariable("VenomStatus",String.valueOf(status));
	}
	
	public static void main(String[] args)
	{
		new Venom(29054, QN, "Venom", "ai");
	}
}