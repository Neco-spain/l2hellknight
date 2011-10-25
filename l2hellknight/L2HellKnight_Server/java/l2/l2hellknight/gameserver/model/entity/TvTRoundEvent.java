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
package l2.hellknight.gameserver.model.entity;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastMap;

import l2.hellknight.Config;
import l2.hellknight.gameserver.ThreadPoolManager;
import l2.hellknight.gameserver.cache.HtmCache;
import l2.hellknight.gameserver.datatables.DoorTable;
import l2.hellknight.gameserver.datatables.ItemTable;
import l2.hellknight.gameserver.datatables.NpcTable;
import l2.hellknight.gameserver.datatables.SkillTable;
import l2.hellknight.gameserver.datatables.SpawnTable;
import l2.hellknight.gameserver.instancemanager.AntiFeedManager;
import l2.hellknight.gameserver.instancemanager.InstanceManager;
import l2.hellknight.gameserver.model.L2Skill;
import l2.hellknight.gameserver.model.L2Spawn;
import l2.hellknight.gameserver.model.actor.L2Character;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.L2Summon;
import l2.hellknight.gameserver.model.actor.instance.L2DoorInstance;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.actor.instance.L2PetInstance;
import l2.hellknight.gameserver.model.actor.instance.L2SummonInstance;
import l2.hellknight.gameserver.model.itemcontainer.PcInventory;
import l2.hellknight.gameserver.model.olympiad.OlympiadManager;
import l2.hellknight.gameserver.network.SystemMessageId;
import l2.hellknight.gameserver.network.clientpackets.Say2;
import l2.hellknight.gameserver.network.serverpackets.CreatureSay;
import l2.hellknight.gameserver.network.serverpackets.MagicSkillUse;
import l2.hellknight.gameserver.network.serverpackets.NpcHtmlMessage;
import l2.hellknight.gameserver.network.serverpackets.StatusUpdate;
import l2.hellknight.gameserver.network.serverpackets.SystemMessage;
import l2.hellknight.gameserver.templates.L2NpcTemplate;
import l2.hellknight.util.Rnd;
import l2.hellknight.util.StringUtil;

public class TvTRoundEvent
{
	enum EventState
	{
		INACTIVE,
		INACTIVATING,
		PARTICIPATING,
		STARTING,
		FIRSTROUND,
		FROUNDFINISHED,
		SECONDROUND,
		SROUNDFINISHED,
		THIRDROUND,
		TROUNDFINISHED,
		NOWINNERS,
		REWARDING
	}
	
	protected static final Logger _log = Logger.getLogger(TvTRoundEvent.class.getName());
	/** html path **/
	private static final String htmlPath = "data/html/mods/TvTRoundEvent/";
	/**	The teams of the TvTRoundEvent<br> */
	private static TvTRoundEventTeam[] _teams = new TvTRoundEventTeam[2];
	/** The state of the TvTRoundEvent<br> */
	private static EventState _state = EventState.INACTIVE;
	/** The spawn of the participation npc<br> */
	private static L2Spawn _npcSpawn = null;
	/** the npc instance of the participation npc<br> */
	private static L2Npc _lastNpcSpawn = null;
	/** Instance id<br> */
	private static int _TvTRoundEventInstance = 0;
	/** Round Tie points<br> */
	private static short _roundTie;
	
	/**
	 * No instance of this class!<br>
	 */
	private TvTRoundEvent()
	{
	}
	
	/**
	 * Teams initializing<br>
	 */
	public static void init()
	{
		AntiFeedManager.getInstance().registerEvent(AntiFeedManager.TVT_ROUND_ID);
		_teams[0] = new TvTRoundEventTeam(Config.TVT_ROUND_EVENT_TEAM_1_NAME, Config.TVT_ROUND_EVENT_TEAM_1_COORDINATES);
		_teams[1] = new TvTRoundEventTeam(Config.TVT_ROUND_EVENT_TEAM_2_NAME, Config.TVT_ROUND_EVENT_TEAM_2_COORDINATES);
	}
	
	/**
	 * Starts the participation of the TvTRoundEvent<br>
	 * 1. Get L2NpcTemplate by Config.TVT_ROUND_EVENT_PARTICIPATION_NPC_ID<br>
	 * 2. Try to spawn a new npc of it<br><br>
	 *
	 * @return boolean: true if success, otherwise false<br>
	 */
	public static boolean startParticipation()
	{
		L2NpcTemplate tmpl = NpcTable.getInstance().getTemplate(Config.TVT_ROUND_EVENT_PARTICIPATION_NPC_ID);
		
		if (tmpl == null)
		{
			_log.warning("TvTRoundEventEngine[TvTRoundEvent.startParticipation()]: L2NpcTemplate is a NullPointer -> Invalid npc id in configs?");
			return false;
		}
		
		try
		{
			_npcSpawn = new L2Spawn(tmpl);
			
			_npcSpawn.setLocx(Config.TVT_ROUND_EVENT_PARTICIPATION_NPC_COORDINATES[0]);
			_npcSpawn.setLocy(Config.TVT_ROUND_EVENT_PARTICIPATION_NPC_COORDINATES[1]);
			_npcSpawn.setLocz(Config.TVT_ROUND_EVENT_PARTICIPATION_NPC_COORDINATES[2]);
			_npcSpawn.setAmount(1);
			_npcSpawn.setHeading(Config.TVT_ROUND_EVENT_PARTICIPATION_NPC_COORDINATES[3]);
			_npcSpawn.setRespawnDelay(1);
			// later no need to delete spawn from db, we don't store it (false)
			SpawnTable.getInstance().addNewSpawn(_npcSpawn, false);
			_npcSpawn.init();
			_lastNpcSpawn = _npcSpawn.getLastSpawn();
			_lastNpcSpawn.setCurrentHp(_lastNpcSpawn.getMaxHp());
			_lastNpcSpawn.setTitle("TvT Round Event Participation");
			_lastNpcSpawn.isAggressive();
			_lastNpcSpawn.decayMe();
			_lastNpcSpawn.spawnMe(_npcSpawn.getLastSpawn().getX(), _npcSpawn.getLastSpawn().getY(), _npcSpawn.getLastSpawn().getZ());
			_lastNpcSpawn.broadcastPacket(new MagicSkillUse(_lastNpcSpawn, _lastNpcSpawn, 1034, 1, 1, 1));
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "TvTRoundEventEngine[TvTRoundEvent.startParticipation()]: exception: " + e.getMessage(), e);
			return false;
		}
		
		setState(EventState.PARTICIPATING);
		return true;
	}
	
	private static int highestLevelPcInstanceOf(Map< Integer, L2PcInstance > players)
	{
		int maxLevel = Integer.MIN_VALUE, maxLevelId = -1;
		for (L2PcInstance player : players.values())
		{
			if (player.getLevel() >= maxLevel)
			{
				maxLevel = player.getLevel();
				maxLevelId = player.getObjectId();
			}
		}
		return maxLevelId;
	}
	
	/**
	 * Starts the TvTRoundEvent first fight<br>
	 * 1. Set state EventState.STARTING<br>
	 * 2. Abort if not enough participants(return false)<br>
	 * 3. Close doors specified in configs<br>
	 * 4. Open the anteroom doors specified in configs<br>
	 * 5. Set state EventState.FIRSTROUND<br>
	 * 6. Teleport all participants to team spot<br><br>
	 *
	 * @return boolean: true if success, otherwise false<br>
	 */
	public static boolean startEvent()
	{
		// Set state to STARTING
		setState(EventState.STARTING);
		
		// Randomize and balance team distribution
		Map< Integer, L2PcInstance > allParticipants = new FastMap< Integer, L2PcInstance >();
		allParticipants.putAll(_teams[0].getParticipatedPlayers());
		allParticipants.putAll(_teams[1].getParticipatedPlayers());
		_teams[0].cleanMe();
		_teams[1].cleanMe();
		cleanRoundTie();

		L2PcInstance player;
		Iterator<L2PcInstance> iter;
		if (needParticipationFee())
		{
			iter = allParticipants.values().iterator();
			while (iter.hasNext())
			{
				player = iter.next();
				if (!hasParticipationFee(player))
					iter.remove();
			}
		}
		
		int balance[] = { 0, 0 }, priority = 0, highestLevelPlayerId;
		L2PcInstance highestLevelPlayer;
	// XXX: allParticipants should be sorted by level instead of using highestLevelPcInstanceOf for every fetch
		while (!allParticipants.isEmpty())
		{
			// Priority team gets one player
			highestLevelPlayerId = highestLevelPcInstanceOf(allParticipants);
			highestLevelPlayer = allParticipants.get(highestLevelPlayerId);
			allParticipants.remove(highestLevelPlayerId);
			_teams[priority].addPlayer(highestLevelPlayer);
			balance[priority] += highestLevelPlayer.getLevel();
			// Exiting if no more players
			if (allParticipants.isEmpty()) break;
			// The other team gets one player
			// XXX: Code not dry
			priority = 1-priority;
			highestLevelPlayerId = highestLevelPcInstanceOf(allParticipants);
			highestLevelPlayer = allParticipants.get(highestLevelPlayerId);
			allParticipants.remove(highestLevelPlayerId);
			_teams[priority].addPlayer(highestLevelPlayer);
			balance[priority] += highestLevelPlayer.getLevel();
			// Recalculating priority
			priority = balance[0] > balance[1] ? 1 : 0;
		}
		
		// Check for enought participants
		if (_teams[0].getParticipatedPlayerCount() < Config.TVT_ROUND_EVENT_MIN_PLAYERS_IN_TEAMS || _teams[1].getParticipatedPlayerCount() < Config.TVT_ROUND_EVENT_MIN_PLAYERS_IN_TEAMS)
		{
			// Set state INACTIVE
			setState(EventState.INACTIVE);
			// Cleanup of teams
			_teams[0].cleanMe();
			_teams[1].cleanMe();
			// Unspawn the event NPC
			unSpawnNpc();
			AntiFeedManager.getInstance().clear(AntiFeedManager.TVT_ROUND_ID);
			return false;
		}
		
		if (needParticipationFee())
		{
			iter = _teams[0].getParticipatedPlayers().values().iterator();
			while (iter.hasNext())
			{
				player = iter.next();
				if (!payParticipationFee(player))
					iter.remove();
			}
			iter = _teams[1].getParticipatedPlayers().values().iterator();
			while (iter.hasNext())
			{
				player = iter.next();
				if (!payParticipationFee(player))
					iter.remove();
			}
		}
		
		if (Config.TVT_ROUND_EVENT_IN_INSTANCE)
		{
			try
			{
				_TvTRoundEventInstance = InstanceManager.getInstance().createDynamicInstance(Config.TVT_ROUND_EVENT_INSTANCE_FILE);
				InstanceManager.getInstance().getInstance(_TvTRoundEventInstance).setAllowSummon(false);
				InstanceManager.getInstance().getInstance(_TvTRoundEventInstance).setPvPInstance(true);
				InstanceManager.getInstance().getInstance(_TvTRoundEventInstance).setEmptyDestroyTime(Config.TVT_ROUND_EVENT_START_RESPAWN_LEAVE_TELEPORT_DELAY * 1000 + 60000L);
			}
			catch (Exception e)
			{
				_TvTRoundEventInstance = 0;
				_log.log(Level.WARNING, "TvTRoundEventEngine[TvTRoundEvent.createDynamicInstance]: exception: " + e.getMessage(), e);
			}
		}
		
		// Opens all doors specified in configs for tvt round
		openDoors(Config.TVT_ROUND_DOORS_IDS_TO_OPEN);
		// Closes all doors specified in configs for tvt round
		closeDoors(Config.TVT_ROUND_DOORS_IDS_TO_CLOSE);
		// Open the doors after a configurable time
		openAnteroomDoors();
		
		// Set state STARTED
		setState(EventState.FIRSTROUND);
		
		// Iterate over all teams
		for (TvTRoundEventTeam team : _teams)
		{
			// Iterate over all participated player instances in this team
			for (L2PcInstance playerInstance : team.getParticipatedPlayers().values())
			{
				if (playerInstance != null)
				{
					// Teleporter implements Runnable and starts itself
					new TvTRoundEventTeleporter(playerInstance, team.getCoordinates(), false, false);
				}
			}
		}
		
		return true;
	}
	
	/**
	 * Starts the TvTRoundEvent fights<br>
	 * 1. Abort if not enough participants(return false)<br>
	 * 2. Open the anteroom doors specified in configs<br>
	 * 3. Teleport all participants to anterooms<br><br>
	 *
	 * @return boolean: true if success, otherwise false<br>
	 */
	public static boolean startFights()
	{
		// Re-check for enough participants
		if (_teams[0].getParticipatedPlayerCount() < Config.TVT_ROUND_EVENT_MIN_PLAYERS_IN_TEAMS || _teams[1].getParticipatedPlayerCount() < Config.TVT_ROUND_EVENT_MIN_PLAYERS_IN_TEAMS)
			return false;
		
		// Open the doors after a configurable time
		openAnteroomDoors();
		
		// Iterate over all teams
		for (TvTRoundEventTeam team : _teams)
		{
			// Iterate over all participated player instances in this team
			for (L2PcInstance playerInstance : team.getParticipatedPlayers().values())
			{
				if (playerInstance != null)
				{
					// Teleporter implements Runnable and starts itself
					new TvTRoundEventTeleporter(playerInstance, team.getCoordinates(), false, false);
				}
			}
		}
		return true;
	}
	
	public static void openAnteroomDoors()
	{
		int TvTRoundWaitOpenAnteroomDoors = (Config.TVT_ROUND_EVENT_WAIT_OPEN_ANTEROOM_DOORS + Config.TVT_ROUND_EVENT_START_RESPAWN_LEAVE_TELEPORT_DELAY);
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				// Open all doors specified in the config
				openDoors(Config.TVT_ROUND_ANTEROOM_DOORS_IDS_TO_OPEN_CLOSE);
				closeAnteroomDoors();
			}
		}, TvTRoundWaitOpenAnteroomDoors * 1000);
	}
	
	public static void closeAnteroomDoors()
	{
		int TvTRoundWaitCloseAnteroomDoors = (Config.TVT_ROUND_EVENT_WAIT_CLOSE_ANTEROOM_DOORS + Config.TVT_ROUND_EVENT_START_RESPAWN_LEAVE_TELEPORT_DELAY);
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				// Close all doors specified in the config
				closeDoors(Config.TVT_ROUND_ANTEROOM_DOORS_IDS_TO_OPEN_CLOSE);
			}
		}, TvTRoundWaitCloseAnteroomDoors * 1000);
	}
	
	/**
	 * Calculates the TvTRoundEvent points<br>
	 * 1. Check if both teams are at a tie (points equals)<br>
	 * 2. Set state EventState.REWARDING<br>
	 * 3. Give round point to the team with more points<br><br>
	 *
	 * @return String: winning team name<br>
	 */
	public static String calculatePoints()
	{
		if (_teams[0].getPoints() == _teams[1].getPoints())
		{
			// Check if one of the teams have no more players left
			if (_teams[0].getParticipatedPlayerCount() == 0 || _teams[1].getParticipatedPlayerCount() == 0)
			{
				setState(EventState.REWARDING);
				return "TvT Round Event: No team won due to inactivity!";
			}
			
			// Both teams have equals points
			sysMsgToAllParticipants("TvT Round Event: Round has ended, both teams have tied.");
			if (Config.TVT_ROUND_GIVE_POINT_TEAM_TIE)
			{
				_teams[0].increaseRoundPoints();
				_teams[1].increaseRoundPoints();
				if (Config.TVT_ROUND_EVENT_STOP_ON_TIE)
					addRoundTie();
				return "TvT Round Event: Round has ended with both teams tying.";
			}
			else
			{
				if (Config.TVT_ROUND_EVENT_STOP_ON_TIE)
					addRoundTie();
				return "TvT Round Event: Round has ended with both teams tying.";
			}
		}
		
		// Set state REWARDING so nobody can point anymore
		setState(EventState.REWARDING);
		
		// Get team which has more points
		TvTRoundEventTeam team = _teams[_teams[0].getPoints() > _teams[1].getPoints() ? 0 : 1];
		team.increaseRoundPoints();
		return "TvT Round Event: Round finished. Team " + team.getName() + " won with " + team.getPoints() + " kills.";
	}
	
	/**
	 * Calculates the TvTRoundEvent reward<br>
	 * 1. If both teams are at a tie (points equals), send it as system message to all participants, if one of the teams have 0 participants left online abort rewarding<br>
	 * 2. Wait till teams are not at a tie anymore<br>
	 * 3. Set state EventState.REWARDING<br>
	 * 4. Reward team with more points<br>
	 * 5. Show win html to winning team participants<br><br>
	 *
	 * @return String: winning team name<br>
	 */
	public static String calculateRewards()
	{
		if (_teams[0].getRoundPoints() == _teams[1].getRoundPoints())
		{
			// Check if one of the teams have no more players left
			if (_teams[0].getParticipatedPlayerCount() == 0 || _teams[1].getParticipatedPlayerCount() == 0)
			{
				// set state to rewarding
				setState(EventState.REWARDING);
				// return here, the fight can't be completed
				return "TvT Round Event: Event has ended. No team won due to inactivity!";
			}
			
			// Both teams have equals points
			sysMsgToAllParticipants("TvT Round Event: Event has ended, both teams have tied.");
			if (Config.TVT_ROUND_REWARD_TEAM_TIE)
			{
				rewardTeam(_teams[0]);
				rewardTeam(_teams[1]);
				return "TvT Round Event: Event has ended with both teams tying.";
			}
			else
				return "TvT Round Event: Event has ended with both teams tying.";
		}
		
		// Set state REWARDING so nobody can point anymore
		setState(EventState.REWARDING);
		
		// Get team which has more round points
		TvTRoundEventTeam team = _teams[_teams[0].getRoundPoints() > _teams[1].getRoundPoints() ? 0 : 1];
		rewardTeam(team);
		return "TvT Round Event: Event finished. Team " + team.getName() + " won with " + team.getRoundPoints() + " round(s).";
	}
	
	private static void rewardTeam(TvTRoundEventTeam team)
	{
		// Iterate over all participated player instances of the winning team
		for (L2PcInstance playerInstance : team.getParticipatedPlayers().values())
		{
			// Check for nullpointer
			if (playerInstance == null)
			{
				continue;
			}
			
			SystemMessage systemMessage = null;
			
			// Iterate over all tvt round event rewards
			for (int[] reward : Config.TVT_ROUND_EVENT_REWARDS)
			{
				PcInventory inv = playerInstance.getInventory();
				
				// Check for stackable item, non stackabe items need to be added one by one
				if (ItemTable.getInstance().createDummyItem(reward[0]).isStackable())
				{
					inv.addItem("TvT Round Event", reward[0], reward[1], playerInstance, playerInstance);
					
					if (reward[1] > 1)
					{
						systemMessage = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
						systemMessage.addItemName(reward[0]);
						systemMessage.addItemNumber(reward[1]);
					}
					else
					{
						systemMessage = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
						systemMessage.addItemName(reward[0]);
					}
					
					playerInstance.sendPacket(systemMessage);
				}
				else
				{
					for (int i = 0; i < reward[1]; ++i)
					{
						inv.addItem("TvT Round Event", reward[0], 1, playerInstance, playerInstance);
						systemMessage = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
						systemMessage.addItemName(reward[0]);
						playerInstance.sendPacket(systemMessage);
					}
				}
			}
			
			StatusUpdate statusUpdate = new StatusUpdate(playerInstance);
			NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);
			
			statusUpdate.addAttribute(StatusUpdate.CUR_LOAD, playerInstance.getCurrentLoad());
			npcHtmlMessage.setHtml(HtmCache.getInstance().getHtm(playerInstance.getHtmlPrefix(), htmlPath+"Reward.htm"));
			playerInstance.sendPacket(statusUpdate);
			playerInstance.sendPacket(npcHtmlMessage);
		}
	}
	
	/**
	 * Stops the TvTRoundEvent event<br>
	 * 1. Set state EventState.INACTIVATING<br>
	 * 2. Remove tvt round npc from world<br>
	 * 3. Open doors specified in configs<br>
	 * 4. Teleport all participants back to participation npc location<br>
	 * 5. Teams cleaning<br>
	 * 6. Set state EventState.INACTIVE<br>
	 */
	public static void stopEvent()
	{
		// Set state INACTIVATING
		setState(EventState.INACTIVATING);
		//Unspawn event npc
		unSpawnNpc();
		// Opens all doors specified in configs for tvt round
		openDoors(Config.TVT_ROUND_DOORS_IDS_TO_CLOSE);
		// Closes all doors specified in Configs for tvt round
		closeDoors(Config.TVT_ROUND_DOORS_IDS_TO_OPEN);
		
		// Iterate over all teams
		for (TvTRoundEventTeam team : _teams)
		{
			for (L2PcInstance playerInstance : team.getParticipatedPlayers().values())
			{
				// Check for nullpointer
				if (playerInstance != null)
				{
					new TvTRoundEventTeleporter(playerInstance, Config.TVT_ROUND_EVENT_PARTICIPATION_NPC_COORDINATES, false, false);
				}
			}
		}
		
		// Cleanup of teams
		_teams[0].cleanMe();
		_teams[1].cleanMe();
		// Set state INACTIVE
		setState(EventState.INACTIVE);
		AntiFeedManager.getInstance().clear(AntiFeedManager.TVT_ROUND_ID);
	}
	
	/**
	 * Adds a player to a TvTRoundEvent team<br>
	 * 1. Calculate the id of the team in which the player should be added<br>
	 * 2. Add the player to the calculated team<br><br>
	 *
	 * @param playerInstance as L2PcInstance<br>
	 * @return boolean: true if success, otherwise false<br>
	 */
	public static synchronized boolean addParticipant(L2PcInstance playerInstance)
	{
		// Check for nullpoitner
		if (playerInstance == null)
		{
			return false;
		}
		
		byte teamId = 0;
		
		// Check to which team the player should be added
		if (_teams[0].getParticipatedPlayerCount() == _teams[1].getParticipatedPlayerCount())
		{
			teamId = (byte) (Rnd.get(2));
		}
		else
		{
			teamId = (byte) (_teams[0].getParticipatedPlayerCount() > _teams[1].getParticipatedPlayerCount() ? 1 : 0);
		}
		
		return _teams[teamId].addPlayer(playerInstance);
	}
	
	/**
	 * Removes a TvTRoundEvent player from it's team<br>
	 * 1. Get team id of the player<br>
	 * 2. Remove player from it's team<br><br>
	 *
	 * @param playerName as String<br>
	 * @return boolean: true if success, otherwise false<br>
	 */
	public static boolean removeParticipant(int playerObjectId)
	{
		// Get the teamId of the player
		byte teamId = getParticipantTeamId(playerObjectId);
		
		// Check if the player is participant
		if (teamId != -1)
		{
			// Remove the player from team
			_teams[teamId].removePlayer(playerObjectId);
			return true;
		}
		
		return false;
	}
	
	public static boolean needParticipationFee()
	{
		return Config.TVT_ROUND_EVENT_PARTICIPATION_FEE[0] != 0 && Config.TVT_ROUND_EVENT_PARTICIPATION_FEE[1] != 0;
	}
	
	public static boolean hasParticipationFee(L2PcInstance playerInstance)
	{
		return playerInstance.getInventory().getInventoryItemCount(Config.TVT_ROUND_EVENT_PARTICIPATION_FEE[0], -1) >= Config.TVT_ROUND_EVENT_PARTICIPATION_FEE[1];
	}
	
	public static boolean payParticipationFee(L2PcInstance playerInstance)
	{
		return playerInstance.destroyItemByItemId("TvT Participation Fee", Config.TVT_ROUND_EVENT_PARTICIPATION_FEE[0], Config.TVT_ROUND_EVENT_PARTICIPATION_FEE[1], _lastNpcSpawn, true);
	}
	
	public static String getParticipationFee()
	{
		int itemId = Config.TVT_ROUND_EVENT_PARTICIPATION_FEE[0];
		int itemNum = Config.TVT_ROUND_EVENT_PARTICIPATION_FEE[1];
		
		if (itemId == 0 || itemNum == 0)
			return "-";
		
		return StringUtil.concat(String.valueOf(itemNum), " ", ItemTable.getInstance().getTemplate(itemId).getName());
	}
	
	/**
	 * Send a SystemMessage to all participated players<br>
	 * 1. Send the message to all players of team number one<br>
	 * 2. Send the message to all players of team number two<br><br>
	 *
	 * @param message as String<br>
	 */
	public static void sysMsgToAllParticipants(String message)
	{
		for (L2PcInstance playerInstance : _teams[0].getParticipatedPlayers().values())
		{
			if (playerInstance != null)
			{
				playerInstance.sendMessage(message);
			}
		}
		
		for (L2PcInstance playerInstance : _teams[1].getParticipatedPlayers().values())
		{
			if (playerInstance != null)
			{
				playerInstance.sendMessage(message);
			}
		}
	}
	
	/**
	 * Close doors specified in configs
	 */
	private static void closeDoors(List<Integer> doors)
	{
		for (int doorId : doors)
		{
			L2DoorInstance doorInstance = DoorTable.getInstance().getDoor(doorId);
			
			if (doorInstance != null)
			{
				doorInstance.closeMe();
			}
		}
	}
	
	/**
	 * Open doors specified in configs
	 */
	private static void openDoors(List<Integer> doors)
	{
		for (int doorId : doors)
		{
			L2DoorInstance doorInstance = DoorTable.getInstance().getDoor(doorId);
			
			if (doorInstance != null)
			{
				doorInstance.openMe();
			}
		}
	}
	
	/**
	 * UnSpawns the TvTRoundEvent npc
	 */
	private static void unSpawnNpc()
	{
		// Delete the npc
		_lastNpcSpawn.deleteMe();
		SpawnTable.getInstance().deleteSpawn(_lastNpcSpawn.getSpawn(), false);
		// Stop respawning of the npc
		_npcSpawn.stopRespawn();
		_npcSpawn = null;
		_lastNpcSpawn = null;
	}
	
	/**
	 * Called when a player logs in<br><br>
	 *
	 * @param playerInstance as L2PcInstance<br>
	 */
	public static void onLogin(L2PcInstance playerInstance)
	{
		if (playerInstance == null || (!isStarting() && !isStarted()))
		{
			return;
		}
		
		byte teamId = getParticipantTeamId(playerInstance.getObjectId());
		
		if (teamId == -1)
		{
			return;
		}
		
		_teams[teamId].addPlayer(playerInstance);
		new TvTRoundEventTeleporter(playerInstance, _teams[teamId].getCoordinates(), true, false);
	}
	
	/**
	 * Called when a player logs out<br><br>
	 *
	 * @param playerInstance as L2PcInstance<br>
	 */
	public static void onLogout(L2PcInstance playerInstance)
	{
		if (playerInstance != null && (isStarting() || isStarted() || isParticipating()))
		{
			if (removeParticipant(playerInstance.getObjectId()))
				playerInstance.setXYZInvisible(Config.TVT_ROUND_EVENT_PARTICIPATION_NPC_COORDINATES[0] + Rnd.get(101)-50,
						Config.TVT_ROUND_EVENT_PARTICIPATION_NPC_COORDINATES[1] + Rnd.get(101)-50,
						Config.TVT_ROUND_EVENT_PARTICIPATION_NPC_COORDINATES[2]);
		}
	}
	
	/**
	 * Called on every bypass by npc of type L2TvTRoundEventNpc<br>
	 * Needs synchronization cause of the max player check<br><br>
	 *
	 * @param command as String<br>
	 * @param playerInstance as L2PcInstance<br>
	 */
	public static synchronized void onBypass(String command, L2PcInstance playerInstance)
	{
		if (playerInstance == null || !isParticipating())
			return;
		
		final String htmContent;
		
		if (command.equals("tvt_round_event_participation"))
		{
			NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);
			int playerLevel = playerInstance.getLevel();
			
			if (playerInstance.isCursedWeaponEquipped())
			{
				htmContent = HtmCache.getInstance().getHtm(playerInstance.getHtmlPrefix(), htmlPath+"CursedWeaponEquipped.htm");
				if (htmContent != null)
					npcHtmlMessage.setHtml(htmContent);
			}
			else if (OlympiadManager.getInstance().isRegistered(playerInstance))
			{
				htmContent = HtmCache.getInstance().getHtm(playerInstance.getHtmlPrefix(), htmlPath+"Olympiad.htm");
				if (htmContent != null)
					npcHtmlMessage.setHtml(htmContent);
			}
			else if (playerInstance.getKarma() > 0)
			{
				htmContent = HtmCache.getInstance().getHtm(playerInstance.getHtmlPrefix(), htmlPath+"Karma.htm");
				if (htmContent != null)
					npcHtmlMessage.setHtml(htmContent);
			}
			else if (playerLevel < Config.TVT_ROUND_EVENT_MIN_LVL || playerLevel > Config.TVT_ROUND_EVENT_MAX_LVL)
			{
				htmContent = HtmCache.getInstance().getHtm(playerInstance.getHtmlPrefix(), htmlPath+"Level.htm");
				if (htmContent != null)
				{
					npcHtmlMessage.setHtml(htmContent);
					npcHtmlMessage.replace("%roundmin%", String.valueOf(Config.TVT_ROUND_EVENT_MIN_LVL));
					npcHtmlMessage.replace("%roundmax%", String.valueOf(Config.TVT_ROUND_EVENT_MAX_LVL));
				}
			}
			else if (_teams[0].getParticipatedPlayerCount() == Config.TVT_ROUND_EVENT_MAX_PLAYERS_IN_TEAMS && _teams[1].getParticipatedPlayerCount() == Config.TVT_ROUND_EVENT_MAX_PLAYERS_IN_TEAMS)
			{
				htmContent = HtmCache.getInstance().getHtm(playerInstance.getHtmlPrefix(), htmlPath+"TeamsFull.htm");
				if (htmContent != null)
				{
					npcHtmlMessage.setHtml(htmContent);
					npcHtmlMessage.replace("%roundmax%", String.valueOf(Config.TVT_ROUND_EVENT_MAX_PLAYERS_IN_TEAMS));
				}
			}
			else if (Config.TVT_ROUND_EVENT_MAX_PARTICIPANTS_PER_IP > 0
					&& !AntiFeedManager.getInstance().tryAddPlayer(AntiFeedManager.TVT_ROUND_ID, playerInstance, Config.TVT_ROUND_EVENT_MAX_PARTICIPANTS_PER_IP))
			{
				htmContent = HtmCache.getInstance().getHtm(playerInstance.getHtmlPrefix(), htmlPath+"IPRestriction.htm");
				if (htmContent != null)
				{
					npcHtmlMessage.setHtml(htmContent);
					npcHtmlMessage.replace("%roundmax%", String.valueOf(AntiFeedManager.getInstance().getLimit(playerInstance, Config.TVT_ROUND_EVENT_MAX_PARTICIPANTS_PER_IP)));
				}
			}
			else if (needParticipationFee() && !hasParticipationFee(playerInstance))
			{
				htmContent = HtmCache.getInstance().getHtm(playerInstance.getHtmlPrefix(), htmlPath+"ParticipationFee.htm");
				if (htmContent != null)
				{
					npcHtmlMessage.setHtml(htmContent);
					npcHtmlMessage.replace("%roundfee%", getParticipationFee());
				}
			}
			else if (addParticipant(playerInstance))
				npcHtmlMessage.setHtml(HtmCache.getInstance().getHtm(playerInstance.getHtmlPrefix(), htmlPath+"Registered.htm"));
			else
				return;
			
			playerInstance.sendPacket(npcHtmlMessage);
		}
		else if (command.equals("tvt_round_event_remove_participation"))
		{
			removeParticipant(playerInstance.getObjectId());
			if (Config.TVT_ROUND_EVENT_MAX_PARTICIPANTS_PER_IP > 0)
				AntiFeedManager.getInstance().removePlayer(AntiFeedManager.TVT_ROUND_ID, playerInstance);
			
			NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);
			
			npcHtmlMessage.setHtml(HtmCache.getInstance().getHtm(playerInstance.getHtmlPrefix(), htmlPath+"Unregistered.htm"));
			playerInstance.sendPacket(npcHtmlMessage);
		}
	}
	
	/**
	 * Called on every onAction in L2PcIstance<br><br>
	 *
	 * @param playerName as String<br>
	 * @param targetPlayerName as String<br>
	 * @return boolean: true if player is allowed to target, otherwise false<br>
	 */
	public static boolean onAction(L2PcInstance playerInstance, int targetedPlayerObjectId)
	{
		if (playerInstance == null || !isStarted())
		{
			return true;
		}
		
		if (playerInstance.isGM())
		{
			return true;
		}
		
		byte playerTeamId = getParticipantTeamId(playerInstance.getObjectId());
		byte targetedPlayerTeamId = getParticipantTeamId(targetedPlayerObjectId);
		
		if ((playerTeamId != -1 && targetedPlayerTeamId == -1) || (playerTeamId == -1 && targetedPlayerTeamId != -1))
		{
			return false;
		}
		
		if (playerTeamId != -1 && targetedPlayerTeamId != -1 && playerTeamId == targetedPlayerTeamId && playerInstance.getObjectId() != targetedPlayerObjectId && !Config.TVT_ROUND_EVENT_TARGET_TEAM_MEMBERS_ALLOWED)
		{
			return false;
		}
		
		return true;
	}
	
	/**
	 * Called on every scroll use<br><br>
	 *
	 * @param playerName as String<br>
	 * @return boolean: true if player is allowed to use scroll, otherwise false<br>
	 */
	public static boolean onScrollUse(int playerObjectId)
	{
		if (!isStarted())
			return true;
		
		if (isPlayerParticipant(playerObjectId) && !Config.TVT_ROUND_EVENT_SCROLL_ALLOWED)
			return false;
		
		return true;
	}
	
	/**
	 * Called on every potion use<br><br>
	 *
	 * @param playerName as String<br>
	 * @return boolean: true if player is allowed to use potions, otherwise false<br>
	 */
	public static boolean onPotionUse(int playerObjectId)
	{
		if (!isStarted())
			return true;
		
		if (isPlayerParticipant(playerObjectId) && !Config.TVT_ROUND_EVENT_POTIONS_ALLOWED)
			return false;
		
		return true;
	}
	
	/**
	 * Called on every escape use(thanks to nbd)<br><br>
	 *
	 * @param playerName as String<br>
	 * @return boolean: true if player is not in tvt round event, otherwise false<br>
	 */
	public static boolean onEscapeUse(int playerObjectId)
	{
		if (!isStarted())
		{
			return true;
		}
		
		if (isPlayerParticipant(playerObjectId))
		{
			return false;
		}
		
		return true;
	}
	
	/**
	 * Called on every summon item use<br><br>
	 *
	 * @param playerName as String<br>
	 * @return boolean: true if player is allowed to summon by item, otherwise false<br>
	 */
	public static boolean onItemSummon(int playerObjectId)
	{
		if (!isStarted())
		{
			return true;
		}
		
		if (isPlayerParticipant(playerObjectId) && !Config.TVT_ROUND_EVENT_SUMMON_BY_ITEM_ALLOWED)
		{
			return false;
		}
		
		return true;
	}
	
	/**
	 * Is called when a player is killed<br><br>
	 * 
	 * @param killerCharacter as L2Character<br>
	 * @param killedPlayerInstance as L2PcInstance<br>
	 */
	public static void onKill(L2Character killerCharacter, L2PcInstance killedPlayerInstance)
	{
		if (killedPlayerInstance == null || !isStarted())
		{
			return;
		}
		
		byte killedTeamId = getParticipantTeamId(killedPlayerInstance.getObjectId());
		
		if (killedTeamId == -1)
		{
			return;
		}
		
		if (Config.TVT_ROUND_EVENT_ON_DIE)
			new TvTRoundEventTeleporter(killedPlayerInstance, _teams[killedTeamId].getCoordinates(), false, false);
		else
			killedPlayerInstance.sendMessage("You're dead. Now you must wait until new round or event end.");
		
		if (killerCharacter == null)
		{
			return;
		}
		
		L2PcInstance killerPlayerInstance = null;
		
		if (killerCharacter instanceof L2PetInstance || killerCharacter instanceof L2SummonInstance)
		{
			killerPlayerInstance = ((L2Summon) killerCharacter).getOwner();
			
			if (killerPlayerInstance == null)
			{
				return;
			}
		}
		else if (killerCharacter instanceof L2PcInstance)
		{
			killerPlayerInstance = (L2PcInstance) killerCharacter;
		}
		else
		{
			return;
		}
		
		byte killerTeamId = getParticipantTeamId(killerPlayerInstance.getObjectId());
		
		if (killerTeamId != -1 && killedTeamId != -1 && killerTeamId != killedTeamId)
		{
			TvTRoundEventTeam killerTeam = _teams[killerTeamId];
			
			killerTeam.increasePoints();
			
			CreatureSay cs = new CreatureSay(killerPlayerInstance.getObjectId(), Say2.TELL, killerPlayerInstance.getName(), "I have killed " + killedPlayerInstance.getName() + "!");
			
			for (L2PcInstance playerInstance : _teams[killerTeamId].getParticipatedPlayers().values())
			{
				if (playerInstance != null)
				{
					playerInstance.sendPacket(cs);
				}
			}
		}
	}
	
	/**
	 * Called on Appearing packet received (player finished teleporting)<br><br>
	 * 
	 * @param L2PcInstance playerInstance
	 */
	public static void onTeleported(L2PcInstance playerInstance)
	{
		if (!isStarted() || playerInstance == null || !isPlayerParticipant(playerInstance.getObjectId()))
			return;
		
		if (playerInstance.isMageClass())
		{
			if (Config.TVT_ROUND_EVENT_MAGE_BUFFS != null && !Config.TVT_ROUND_EVENT_MAGE_BUFFS.isEmpty())
			{
				for (int i : Config.TVT_ROUND_EVENT_MAGE_BUFFS.keys())
				{
					L2Skill skill = SkillTable.getInstance().getInfo(i, Config.TVT_ROUND_EVENT_MAGE_BUFFS.get(i));
					if (skill != null)
						skill.getEffects(playerInstance, playerInstance);
				}
			}
		}
		else
		{
			if (Config.TVT_ROUND_EVENT_FIGHTER_BUFFS != null && !Config.TVT_ROUND_EVENT_FIGHTER_BUFFS.isEmpty())
			{
				for (int i : Config.TVT_ROUND_EVENT_FIGHTER_BUFFS.keys())
				{
					L2Skill skill = SkillTable.getInstance().getInfo(i, Config.TVT_ROUND_EVENT_FIGHTER_BUFFS.get(i));
					if (skill != null)
						skill.getEffects(playerInstance, playerInstance);
				}
			}
		}
	}
	
	/*
	 * Return true if player valid for skill
	 */
	public static final boolean checkForTvTRoundSkill(L2PcInstance source, L2PcInstance target, L2Skill skill)
	{
		if (!isStarted())
			return true;
		// TvT Round is started
		final int sourcePlayerId = source.getObjectId();
		final int targetPlayerId = target.getObjectId();
		final boolean isSourceParticipant = isPlayerParticipant(sourcePlayerId);
		final boolean isTargetParticipant = isPlayerParticipant(targetPlayerId);
		
		// both players not participating
		if (!isSourceParticipant && !isTargetParticipant)
			return true;
		// one player not participating
		if (!(isSourceParticipant && isTargetParticipant))
			return false;
		// players in the different teams ?
		if (getParticipantTeamId(sourcePlayerId) != getParticipantTeamId(targetPlayerId))
		{
			if (!skill.isOffensive())
				return false;
		}
		return true;
	}
	
	public static void addRoundTie()
	{
		++_roundTie;
	}
	
	public static short getRoundTie()
	{
		return _roundTie;
	}
	
	public static void cleanRoundTie()
	{
		_roundTie = 0;
	}
	
	/**
	 * Sets the TvTRoundEvent state<br><br>
	 *
	 * @param state as EventState<br>
	 */
	private static void setState(EventState state)
	{
		synchronized (_state)
		{
			_state = state;
		}
	}
	
	/**
	 * Is TvTRoundEvent inactive?<br><br>
	 *
	 * @return boolean: true if event is inactive(waiting for next event cycle), otherwise false<br>
	 */
	public static boolean isInactive()
	{
		boolean isInactive;
		
		synchronized (_state)
		{
			isInactive = _state == EventState.INACTIVE;
		}
		
		return isInactive;
	}
	
	/**
	 * Is TvTRoundEvent in inactivating?<br><br>
	 *
	 * @return boolean: true if event is in inactivating progress, otherwise false<br>
	 */
	public static boolean isInactivating()
	{
		boolean isInactivating;
		
		synchronized (_state)
		{
			isInactivating = _state == EventState.INACTIVATING;
		}
		
		return isInactivating;
	}
	
	/**
	 * Is TvTRoundEvent in participation?<br><br>
	 *
	 * @return boolean: true if event is in participation progress, otherwise false<br>
	 */
	public static boolean isParticipating()
	{
		boolean isParticipating;
		
		synchronized (_state)
		{
			isParticipating = _state == EventState.PARTICIPATING;
		}
		
		return isParticipating;
	}
	
	/**
	 * Is TvTRoundEvent starting?<br><br>
	 *
	 * @return boolean: true if event is starting up(setting up fighting spot, teleport players etc.), otherwise false<br>
	 */
	public static boolean isStarting()
	{
		boolean isStarting;
		
		synchronized (_state)
		{
			isStarting = _state == EventState.STARTING;
		}
		
		return isStarting;
	}
	
	/**
	 * Is TvTRoundEvent in the first round?<br><br>
	 */
	public static boolean isInFirstRound()
	{
		boolean isInFirstRound;
		
		synchronized (_state)
		{
			isInFirstRound = _state == EventState.FIRSTROUND;
		}
		
		return isInFirstRound;
	}
	
	/**
	 * Is TvTRoundEvent first round finished?<br><br>
	 */
	public static boolean isFRoundFinished()
	{
		boolean isFRoundFinished;
		
		synchronized (_state)
		{
			isFRoundFinished = _state == EventState.FROUNDFINISHED;
		}
		
		return isFRoundFinished;
	}
	
	/**
	 * Is TvTRoundEvent in the second round?<br><br>
	 */
	public static boolean isInSecondRound()
	{
		boolean isInSecondRound;
		
		synchronized (_state)
		{
			isInSecondRound = _state == EventState.SECONDROUND;
		}
		
		return isInSecondRound;
	}
	
	/**
	 * Is TvTRoundEvent second round finished?<br><br>
	 */
	public static boolean isSRoundFinished()
	{
		boolean isSRoundFinished;
		
		synchronized (_state)
		{
			isSRoundFinished = _state == EventState.SROUNDFINISHED;
		}
		
		return isSRoundFinished;
	}
	
	/**
	 * Is TvTRoundEvent in the third round?<br><br>
	 */
	public static boolean isInThirdRound()
	{
		boolean isInThirdRound;
		
		synchronized (_state)
		{
			isInThirdRound = _state == EventState.THIRDROUND;
		}
		
		return isInThirdRound;
	}
	
	/**
	 * Is TvTRoundEvent third round finished?<br><br>
	 */
	public static boolean isTRoundFinished()
	{
		boolean isTRoundFinished;
		
		synchronized (_state)
		{
			isTRoundFinished = _state == EventState.TROUNDFINISHED;
		}
		
		return isTRoundFinished;
	}
	
	/**
	 * Is TvTRoundEvent started?<br><br>
	 *
	 * @return boolean: true if event is started, otherwise false<br>
	 */
	public static boolean isStarted()
	{
		if (isInFirstRound() || isInSecondRound() || isInThirdRound())
		{
			return true;
		}
		return false;
	}
	
	/**
	 * Is TvTRoundEvent rewarding?<br><br>
	 *
	 * @return boolean: true if event is currently rewarding, otherwise false<br>
	 */
	public static boolean isRewarding()
	{
		boolean isRewarding;
		
		synchronized (_state)
		{
			isRewarding = _state == EventState.REWARDING;
		}
		
		return isRewarding;
	}
	
	/**
	 * Is TvTRoundEvent finished without winners?<br><br>
	 */
	public static boolean isWithoutWinners()
	{
		boolean isWithoutWinners;
		
		synchronized (_state)
		{
			isWithoutWinners = _state == EventState.NOWINNERS;
		}
		
		return isWithoutWinners;
	}
	
	public static void setFirstRoundFinished()
	{
		setState(EventState.FROUNDFINISHED);
	}
	
	public static void setSecondRoundFinished()
	{
		setState(EventState.SROUNDFINISHED);
	}
	
	public static void setThirdRoundFinished()
	{
		setState(EventState.TROUNDFINISHED);
	}
	
	public static void setInSecondRound()
	{
		setState(EventState.SECONDROUND);
	}
	
	public static void setInThirdRound()
	{
		setState(EventState.THIRDROUND);
	}
	
	public static void setIsWithoutWinners()
	{
		setState(EventState.NOWINNERS);
	}
	
	/**
	 * Returns the team id of a player, if player is not participant it returns -1<br><br>
	 *
	 * @param playerName as String<br>
	 * @return byte: team name of the given playerName, if not in event -1<br>
	 */
	public static byte getParticipantTeamId(int playerObjectId)
	{
		return (byte) (_teams[0].containsPlayer(playerObjectId) ? 0 : (_teams[1].containsPlayer(playerObjectId) ? 1 : -1));
	}
	
	/**
	 * Returns the team of a player, if player is not participant it returns null <br><br>
	 *
	 * @param player objectId as Integer<br>
	 * @return TvTRoundEventTeam: team of the given playerObjectId, if not in event null <br>
	 */
	public static TvTRoundEventTeam getParticipantTeam(int playerObjectId)
	{
		return (_teams[0].containsPlayer(playerObjectId) ? _teams[0] : (_teams[1].containsPlayer(playerObjectId) ? _teams[1] : null));
	}
	
	/**
	 * Returns the enemy team of a player, if player is not participant it returns null <br><br>
	 *
	 * @param player objectId as Integer<br>
	 * @return TvTRoundEventTeam: enemy team of the given playerObjectId, if not in event null <br>
	 */
	public static TvTRoundEventTeam getParticipantEnemyTeam(int playerObjectId)
	{
		return (_teams[0].containsPlayer(playerObjectId) ? _teams[1] : (_teams[1].containsPlayer(playerObjectId) ? _teams[0] : null));
	}
	
	/**
	 * Returns the team coordinates in which the player is in, if player is not in a team return null<br><br>
	 *
	 * @param playerName as String<br>
	 * @return int[]: coordinates of teams, 2 elements, index 0 for team 1 and index 1 for team 2<br>
	 */
	public static int[] getParticipantTeamCoordinates(int playerObjectId)
	{
		return _teams[0].containsPlayer(playerObjectId) ? _teams[0].getCoordinates() : (_teams[1].containsPlayer(playerObjectId) ? _teams[1].getCoordinates() : null);
	}
	
	/**
	 * Is given player participant of the event?<br><br>
	 *
	 * @param playerName as String<br>
	 * @return boolean: true if player is participant, ohterwise false<br>
	 */
	public static boolean isPlayerParticipant(int playerObjectId)
	{
		if (!isParticipating() && !isStarting() && !isStarted())
		{
			return false;
		}
		
		return _teams[0].containsPlayer(playerObjectId) || _teams[1].containsPlayer(playerObjectId);
	}
	
	/**
	 * Returns participated player count<br><br>
	 *
	 * @return int: amount of players registered in the event<br>
	 */
	public static int getParticipatedPlayersCount()
	{
		if (!isParticipating() && !isStarting() && !isStarted())
		{
			return 0;
		}
		
		return _teams[0].getParticipatedPlayerCount() + _teams[1].getParticipatedPlayerCount();
	}
	
	/**
	 * Returns teams names<br><br>
	 *
	 * @return String[]: names of teams, 2 elements, index 0 for team 1 and index 1 for team 2<br>
	 */
	public static String[] getTeamNames()
	{
		return new String[]
		                  {
				_teams[0].getName(), _teams[1].getName()
		                  };
	}
	
	/**
	 * Returns player count of both teams<br><br>
	 *
	 * @return int[]: player count of teams, 2 elements, index 0 for team 1 and index 1 for team 2<br>
	 */
	public static int[] getTeamsPlayerCounts()
	{
		return new int[]
		               {
				_teams[0].getParticipatedPlayerCount(), _teams[1].getParticipatedPlayerCount()
		               };
	}
	
	/**
	 * Returns points count of both teams
	 *
	 * @return int[]: points of teams, 2 elements, index 0 for team 1 and index 1 for team 2<br>
	 */
	public static int[] getTeamsPoints()
	{
		return new int[]
		               {
				_teams[0].getPoints(), _teams[1].getPoints()
		               };
	}
	
	public static void cleanTeamsPoints()
	{
		_teams[0].cleanPoints();
		_teams[1].cleanPoints();
	}
	
	public static boolean checkForPossibleWinner()
	{
		TvTRoundEventTeam team = _teams[_teams[0].getPoints() > _teams[1].getPoints() ? 0 : 1];
		if (team.getRoundPoints() == 2)
		{
			return true;
		}
		return false;
	}
	
	public static int getTvTRoundEventInstance()
	{
		return _TvTRoundEventInstance;
	}
}