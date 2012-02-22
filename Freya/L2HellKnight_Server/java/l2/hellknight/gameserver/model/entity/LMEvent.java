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

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastMap;

import l2.hellknight.Config;
import l2.hellknight.gameserver.cache.HtmCache;
import l2.hellknight.gameserver.datatables.DoorTable;
import l2.hellknight.gameserver.datatables.ItemTable;
import l2.hellknight.gameserver.datatables.NpcTable;
import l2.hellknight.gameserver.datatables.SkillTable;
import l2.hellknight.gameserver.datatables.SpawnTable;
import l2.hellknight.gameserver.instancemanager.InstanceManager;
import l2.hellknight.gameserver.model.L2Party;
import l2.hellknight.gameserver.model.L2Skill;
import l2.hellknight.gameserver.model.L2Spawn;
import l2.hellknight.gameserver.model.L2World;
import l2.hellknight.gameserver.model.actor.L2Character;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.L2Summon;
import l2.hellknight.gameserver.model.actor.instance.L2DoorInstance;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.actor.instance.L2PetInstance;
import l2.hellknight.gameserver.model.actor.instance.L2SummonInstance;
import l2.hellknight.gameserver.model.itemcontainer.PcInventory;
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

public class LMEvent
{
	enum EventState
	{
		INACTIVE,
		INACTIVATING,
		PARTICIPATING,
		STARTING,
		STARTED,
		REWARDING
	}
		
	protected static final Logger _log = Logger.getLogger(LMEvent.class.getName());
	/** html path **/
	private static final String htmlPath = "data/html/mods/LMEvent/";
	/** The state of the LMEvent<br> */
	private static EventState _state = EventState.INACTIVE;
	/** The spawn of the participation npc<br> */
	private static L2Spawn _npcSpawn = null;
	/** the npc instance of the participation npc<br> */
	private static L2Npc _lastNpcSpawn = null;
	/** Instance id<br> */
	private static int _LMEventInstance = 0;
	
	private static Map<Integer, LMPlayer> _lmPlayer = new FastMap<Integer, LMPlayer>();
	
	private static DecimalFormat f = new DecimalFormat(",##0,000");
	
	public LMEvent() {}
	
	/**
	 * LM initializing<br>
	 */
	public static void init() {}
	
	/**
	 * Sets the LMEvent state<br><br>
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
	 * Is LMEvent inactive?<br><br>
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
	 * Is LMEvent in inactivating?<br><br>
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
	 * Is LMEvent in participation?<br><br>
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
	 * Is LMEvent starting?<br><br>
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
	 * Is LMEvent started?<br><br>
	 *
	 * @return boolean: true if event is started, otherwise false<br>
	 */
	public static boolean isStarted()
	{
		boolean isStarted;
		
		synchronized (_state)
		{
			isStarted = _state == EventState.STARTED;
		}
		
		return isStarted;
	}
	
	/**
	 * Is LMEvent rewadrding?<br><br>
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
	 * UnSpawns the LMEvent npc
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
	 * Starts the participation of the LMEvent<br>
	 * 1. Get L2NpcTemplate by Config.LM_EVENT_PARTICIPATION_NPC_ID<br>
	 * 2. Try to spawn a new npc of it<br><br>
	 *
	 * @return boolean: true if success, otherwise false<br>
	 */
	public static boolean startParticipation()
	{
		L2NpcTemplate tmpl = NpcTable.getInstance().getTemplate(Config.LM_EVENT_PARTICIPATION_NPC_ID);
		
		if (tmpl == null)
		{
			_log.warning("LMEventEngine[LMEvent.startParticipation()]: L2NpcTemplate is a NullPointer -> Invalid npc id in configs?");
			return false;
		}
		
		try
		{
			_npcSpawn = new L2Spawn(tmpl);
			
			_npcSpawn.setLocx(Config.LM_EVENT_PARTICIPATION_NPC_COORDINATES[0]);
			_npcSpawn.setLocy(Config.LM_EVENT_PARTICIPATION_NPC_COORDINATES[1]);
			_npcSpawn.setLocz(Config.LM_EVENT_PARTICIPATION_NPC_COORDINATES[2]);
			_npcSpawn.setAmount(1);
			_npcSpawn.setHeading(Config.LM_EVENT_PARTICIPATION_NPC_COORDINATES[3]);
			_npcSpawn.setRespawnDelay(1);
			// later no need to delete spawn from db, we don't store it (false)
			SpawnTable.getInstance().addNewSpawn(_npcSpawn, false);
			_npcSpawn.init();
			_lastNpcSpawn = _npcSpawn.getLastSpawn();
			_lastNpcSpawn.setCurrentHp(_lastNpcSpawn.getMaxHp());
			_lastNpcSpawn.setTitle("LM Event Participation");
			_lastNpcSpawn.isAggressive();
			_lastNpcSpawn.decayMe();
			_lastNpcSpawn.spawnMe(_npcSpawn.getLastSpawn().getX(), _npcSpawn.getLastSpawn().getY(), _npcSpawn.getLastSpawn().getZ());
			_lastNpcSpawn.broadcastPacket(new MagicSkillUse(_lastNpcSpawn, _lastNpcSpawn, 1034, 1, 1, 1));
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "LMEventEngine[LMEvent.startParticipation()]: exception: " + e.getMessage(), e);
			return false;
		}
		
		setState(EventState.PARTICIPATING);
		return true;
	}

	/**
	 * Starts the LMEvent fight<br>
	 * 1. Set state EventState.STARTING<br>
	 * 2. Close doors specified in configs<br>
	 * 3. Abort if not enought participants(return false)<br>
	 * 4. Set state EventState.STARTED<br>
	 * 5. Teleport all participants to team spot<br><br>
	 *
	 * @return boolean: true if success, otherwise false<br>
	 */
	 public static boolean startFight()
	{
		// Set state to STARTING
		setState(EventState.STARTING);
		
		// Check the number of participants
		if (getPlayerCounts() < Config.LM_EVENT_MIN_PLAYERS)
		{
			// Set state INACTIVE
			setState(EventState.INACTIVE);
			
			// Cleanup of participants
			_lmPlayer.clear();
			
			// Unspawn the event NPC
			unSpawnNpc();
			return false;
		}
		
		if (Config.LM_EVENT_IN_INSTANCE)
		{
			try
			{
				_LMEventInstance = InstanceManager.getInstance().createDynamicInstance(Config.LM_EVENT_INSTANCE_FILE);
				InstanceManager.getInstance().getInstance(_LMEventInstance).setAllowSummon(false);
				InstanceManager.getInstance().getInstance(_LMEventInstance).setPvPInstance(true);
				InstanceManager.getInstance().getInstance(_LMEventInstance).setEmptyDestroyTime(Config.LM_EVENT_START_LEAVE_TELEPORT_DELAY * 1000 + 60000L);
			}
			catch (Exception e)
			{
				_LMEventInstance = 0;
				_log.log(Level.WARNING, "LMEventEngine[LMEvent.createDynamicInstance]: exception: " + e.getMessage(), e);
			}
		}
		
		// Opens all doors specified in configs for lm
		openDoors(Config.LM_DOORS_IDS_TO_OPEN);
		// Closes all doors specified in configs for lm
		closeDoors(Config.LM_DOORS_IDS_TO_CLOSE);
		// Set state STARTED
		setState(EventState.STARTED);
		
		for (LMPlayer player: _lmPlayer.values())
		{
			if (player != null)
			{
				// Teleporter implements Runnable and starts itself
				new LMEventTeleporter(player.getPlayer(), false, false);
			}
				
		} 
		
		return true;
	}
	
	 public static TreeSet<LMPlayer> orderPosition(Collection<LMPlayer> listPlayer)
	 {
		TreeSet<LMPlayer> players = new TreeSet<LMPlayer>(new Comparator<LMPlayer>()
		{
			@Override
			public int compare(LMPlayer p1, LMPlayer p2)
			{
				Integer c1 = Integer.valueOf(p2.getCredits() - p1.getCredits());
				Integer c2 = Integer.valueOf(p2.getPoints() - p1.getPoints());
				Integer c3 = p1.getHexCode().compareTo(p2.getHexCode());

				if (c1 == 0)
				{
					if (c2 == 0) return c3;
					return c2;
				}		
				return c1;
			}
		});
		
		players.addAll(listPlayer);
		
		return players;
	 }
	 
	/**
	 * Calculates the LMEvent reward<br>
	 * 1. If both teams are at a tie(points equals), send it as system message to all participants, if one of the teams have 0 participants left online abort rewarding<br>
	 * 2. Wait till teams are not at a tie anymore<br>
	 * 3. Set state EvcentState.REWARDING<br>
	 * 4. Reward team with more points<br>
	 * 5. Show win html to wining team participants<br><br>
	 *
	 * @return String: winning team name<br>
	 */
	public static String calculateRewards()
	{
		TreeSet<LMPlayer> players = orderPosition(_lmPlayer.values());
		String msg = "";
		if (!Config.LM_REWARD_PLAYERS_TIE && getPlayerCounts() > 1)
			return "LM Event ended, thanks to everyone who participated!\nHe did not have winners!";

		for (int i = 0; i < players.size(); i++)
		{
			if (players.isEmpty())
				break;
			
			LMPlayer player = players.first();
			
			if (player.getCredits() == 0 || player.getPoints() == 0)
				break;
			
			rewardPlayer(player);
			players.remove(player);
			msg += " Player: " + player.getPlayer().getName();
			msg += " Killed: " + player.getPoints();
			msg += " Died: " + String.valueOf(Config.LM_EVENT_PLAYER_CREDITS - player.getCredits());
			msg += "\n";
			if (!Config.LM_REWARD_PLAYERS_TIE)
				break;
		}

		// Set state REWARDING so nobody can point anymore
		setState(EventState.REWARDING);
		
		return "LM Event ended, thanks to everyone who participated!\nWinner(s):\n" + msg;
	}
	
	private static void rewardPlayer(LMPlayer p)
	{
		L2PcInstance activeChar = p.getPlayer();
		
		// Check for nullpointer
		if (activeChar == null)
			return;
		
		SystemMessage systemMessage = null;
		String htmltext = "";
		for (int[] reward : Config.LM_EVENT_REWARDS)
		{
			PcInventory inv = activeChar.getInventory();
			
			// Check for stackable item, non stackabe items need to be added one by one
			if (ItemTable.getInstance().createDummyItem(reward[0]).isStackable())
			{
				inv.addItem("LM Event", reward[0], reward[1], activeChar, activeChar);
				
				if (reward[1] > 1)
				{
					systemMessage = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
					systemMessage.addItemName(reward[0]);
					systemMessage.addItemNumber(reward[1]);
				}
				else
				{
					systemMessage = new SystemMessage(SystemMessageId.EARNED_ITEM);
					systemMessage.addItemName(reward[0]);
				}
				
				activeChar.sendPacket(systemMessage);
			}
			else
			{
				for (int i = 0; i < reward[1]; ++i)
				{
					inv.addItem("LM Event", reward[0], 1, activeChar, activeChar);
					systemMessage = new SystemMessage(SystemMessageId.EARNED_ITEM);
					systemMessage.addItemName(reward[0]);
					activeChar.sendPacket(systemMessage);
				}
			}
			htmltext += " - " + (reward[1] > 999 ? f.format(reward[1]) : reward[1]) + " " + ItemTable.getInstance().getTemplate(reward[0]).getName() + "<br1>";
		}
		
		StatusUpdate statusUpdate = new StatusUpdate(activeChar);
		NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);
		
		statusUpdate.addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad());
		npcHtmlMessage.setHtml(HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), htmlPath+"Reward.htm"));
		activeChar.sendPacket(statusUpdate);
		npcHtmlMessage.replace("%palyer%", activeChar.getName());
		npcHtmlMessage.replace("%killed%", String.valueOf(p.getPoints()));
		npcHtmlMessage.replace("%died%", String.valueOf(Config.LM_EVENT_PLAYER_CREDITS - p.getCredits()));
		npcHtmlMessage.replace("%reward%", htmltext);
		activeChar.sendPacket(npcHtmlMessage);
	}
	
	/**
	 * Stops the LMEvent fight<br>
	 * 1. Set state EventState.INACTIVATING<br>
	 * 2. Remove LM npc from world<br>
	 * 3. Open doors specified in configs<br>
	 * 4. Send Top Rank<br>
	 * 5. Teleport all participants back to participation npc location<br>
	 * 6. List players cleaning<br>
	 * 7. Set state EventState.INACTIVE<br>
	 */
	public static void stopFight()
	{
		// Set state INACTIVATING
		setState(EventState.INACTIVATING);
		//Unspawn event npc
		unSpawnNpc();
		// Opens all doors specified in configs for LM
		openDoors(Config.LM_DOORS_IDS_TO_CLOSE);
		// Closes all doors specified in Configs for LM
		closeDoors(Config.LM_DOORS_IDS_TO_OPEN);
		
		for (LMPlayer player : _lmPlayer.values())
		{
			if (player != null)
			{
				new LMEventTeleporter(player.getPlayer(), Config.LM_EVENT_PARTICIPATION_NPC_COORDINATES, false, false);
			}
		}
		
		// Cleanup list
		_lmPlayer = new FastMap<Integer, LMPlayer>();
		// Set state INACTIVE
		setState(EventState.INACTIVE);
	}
	
	/**
	 * Adds a player to a LMEvent<br>
	 *
	 * @param activeChar as L2PcInstance<br>
	 * @return boolean: true if success, otherwise false<br>
	 */
	public static synchronized boolean addParticipant(L2PcInstance activeChar)
	{
		// Check for nullpoitner
		if (activeChar == null) return false;
		
		if (isPlayerParticipant(activeChar)) return false;
		
		String hexCode = hexToString(generateHex(16));
		_lmPlayer.put(activeChar.getObjectId(), new LMPlayer(activeChar, hexCode));
		return true;
	}
	
	public static boolean isPlayerParticipant(L2PcInstance activeChar)
	{
		if (activeChar == null) return false;
		if (_lmPlayer.containsKey(activeChar.getObjectId())) return true;
		return false;
	}
	
	public static boolean isPlayerParticipant(int objectId)
	{
		L2PcInstance activeChar = L2World.getInstance().getPlayer(objectId);
		if (activeChar == null) return false; 
		return isPlayerParticipant(activeChar);
	}
	
	/**
	 * Removes a LMEvent player<br>
	 *
	 * @param activeChar as L2PcInstance<br>
	 * @return boolean: true if success, otherwise false<br>
	 */
	public static boolean removeParticipant(L2PcInstance activeChar)
	{
		if (activeChar == null) return false;		
		if (!isPlayerParticipant(activeChar)) return false;
		
		_lmPlayer.remove(activeChar.getObjectId());
		
		return true;
	}
	
	public static boolean payParticipationFee(L2PcInstance activeChar)
	{
		int itemId = Config.LM_EVENT_PARTICIPATION_FEE[0];
		int itemNum = Config.LM_EVENT_PARTICIPATION_FEE[1];
		if (itemId == 0 || itemNum == 0) return true;
		
		if (activeChar.getInventory().getInventoryItemCount(itemId, -1) < itemNum) return false;
		
		return activeChar.destroyItemByItemId("LM Participation Fee", itemId, itemNum, _lastNpcSpawn, true);
	}
	
	public static String getParticipationFee()
	{
		int itemId = Config.LM_EVENT_PARTICIPATION_FEE[0];
		int itemNum = Config.LM_EVENT_PARTICIPATION_FEE[1];

		if (itemId == 0 || itemNum == 0) return "-";
		
		return StringUtil.concat(String.valueOf(itemNum), " ", ItemTable.getInstance().getTemplate(itemId).getName());
	}
	
	/**
	 * Send a SystemMessage to all participated players<br>
	 *
	 * @param message as String<br>
	 */
	public static void sysMsgToAllParticipants(String message)
	{
		for (LMPlayer player : _lmPlayer.values())
			if (player != null)
				player.getPlayer().sendMessage(message);
	}
	
	/**
	 * Called when a player logs in<br><br>
	 *
	 * @param activeChar as L2PcInstance<br>
	 */
	public static void onLogin(L2PcInstance activeChar)
	{
		if (activeChar == null || (!isStarting() && !isStarted())) return;
		if (!isPlayerParticipant(activeChar)) return;
		
		new LMEventTeleporter(activeChar, false, false);
	}
	
	/**
	 * Called when a player logs out<br><br>
	 *
	 * @param activeChar as L2PcInstance<br>
	 */
	public static void onLogout(L2PcInstance activeChar)
	{
		if (activeChar != null && (isStarting() || isStarted() || isParticipating()))
		{
			if (removeParticipant(activeChar))
				activeChar.setXYZInvisible(Config.LM_EVENT_PARTICIPATION_NPC_COORDINATES[0] + Rnd.get(101)-50,
						Config.LM_EVENT_PARTICIPATION_NPC_COORDINATES[1] + Rnd.get(101)-50,
						Config.LM_EVENT_PARTICIPATION_NPC_COORDINATES[2]);
		}
	}
	
	/**
	 * Called on every bypass by npc of type L2LMEventNpc<br>
	 * Needs synchronization cause of the max player check<br><br>
	 *
	 * @param command as String<br>
	 * @param activeChar as L2PcInstance<br>
	 */
	public static synchronized void onBypass(String command, L2PcInstance activeChar)
	{
		if (activeChar == null || !isParticipating()) return;
		
		final String htmContent;

		if (command.equals("lm_event_participation"))
		{
			NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);
			int playerLevel = activeChar.getLevel();
			
			if (activeChar.isCursedWeaponEquipped())
			{
				htmContent = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), htmlPath+"CursedWeaponEquipped.htm");
				if (htmContent != null)
					npcHtmlMessage.setHtml(htmContent);
			}
			else if (activeChar.isInOlympiadMode())
			{
				htmContent = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), htmlPath+"Olympiad.htm");
				if (htmContent != null)
					npcHtmlMessage.setHtml(htmContent);
			}
			else if (activeChar.getKarma() > 0)
			{
				htmContent = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), htmlPath+"Karma.htm");
				if (htmContent != null)
					npcHtmlMessage.setHtml(htmContent);
			}
			else if (playerLevel < Config.LM_EVENT_MIN_LVL || playerLevel > Config.LM_EVENT_MAX_LVL)
			{
				htmContent = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), htmlPath+"Level.htm");
				if (htmContent != null)
				{
					npcHtmlMessage.setHtml(htmContent);
					npcHtmlMessage.replace("%min%", String.valueOf(Config.LM_EVENT_MIN_LVL));
					npcHtmlMessage.replace("%max%", String.valueOf(Config.LM_EVENT_MAX_LVL));
				}
			}
			else if (getPlayerCounts() == Config.LM_EVENT_MAX_PLAYERS)
			{
				htmContent = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), htmlPath+"Full.htm");
				if (htmContent != null)
				{
					npcHtmlMessage.setHtml(htmContent);
					npcHtmlMessage.replace("%max%", String.valueOf(Config.LM_EVENT_MAX_PLAYERS));
				}
			}
			else if (!payParticipationFee(activeChar))
			{
				htmContent = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), htmlPath+"ParticipationFee.htm");
				if (htmContent != null)
				{
					npcHtmlMessage.setHtml(htmContent);
					npcHtmlMessage.replace("%fee%", getParticipationFee());
				}
			}
			else if (isPlayerParticipant(activeChar))
				npcHtmlMessage.setHtml(HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), htmlPath+"Registered.htm"));
			else if (addParticipant(activeChar))
				npcHtmlMessage.setHtml(HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), htmlPath+"Registered.htm"));
			else
				return;
			
			activeChar.sendPacket(npcHtmlMessage);
		}
		else if (command.equals("lm_event_remove_participation"))
		{
			if (isPlayerParticipant(activeChar))
			{
				removeParticipant(activeChar);
				
				NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);
				
				npcHtmlMessage.setHtml(HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), htmlPath+"Unregistered.htm"));
				activeChar.sendPacket(npcHtmlMessage);
			}
		}
	}
	
	/**
	 * Called on every onAction in L2PcIstance<br><br>
	 *
	 * @param activeChar as L2PcInstance<br>
	 * @param targetPlayerName as Integer<br>
	 * @return boolean: true if player is allowed to target, otherwise false<br>
	 */
	public static boolean onAction(L2PcInstance activeChar, int targetedPlayerObjectId)
	{
		if (activeChar == null || !isStarted()) return true;		
		if (activeChar.isGM()) return true;
		if (!isPlayerParticipant(activeChar) && isPlayerParticipant(targetedPlayerObjectId)) return false;		
		if (isPlayerParticipant(activeChar) && !isPlayerParticipant(targetedPlayerObjectId)) return false;
		
		return true;
	}
	
	/**
	 * Called on every scroll use<br><br>
	 *
	 * @param objectId as Integer<br>
	 * @return boolean: true if player is allowed to use scroll, otherwise false<br>
	 */
	public static boolean onScrollUse(int objectId)
	{
		if (!isStarted()) return true;
		if (isPlayerParticipant(objectId) && !Config.LM_EVENT_SCROLL_ALLOWED) return false;
		return true;
	}
	
	/**
	 * Called on every potion use<br><br>
	 *
	 * @param objectId as Integer<br>
	 * @return boolean: true if player is allowed to use potions, otherwise false<br>
	 */
	public static boolean onPotionUse(int objectId)
	{
		if (!isStarted()) return true;
		if (isPlayerParticipant(objectId) && !Config.LM_EVENT_POTIONS_ALLOWED) return false;
		return true;
	}
	
	/**
	 * Called on every escape use<br><br>
	 *
	 * @param objectId as Integer<br>
	 * @return boolean: true if player is not in LM Event, otherwise false<br>
	 */
	public static boolean onEscapeUse(int objectId)
	{
		if (!isStarted()) return true;
		if (isPlayerParticipant(objectId)) return false;
		return true;
	}
	
	/**
	 * Called on every summon item use<br><br>
	 *
	 * @param objectId as Integer<br>
	 * @return boolean: true if player is allowed to summon by item, otherwise false<br>
	 */
	public static boolean onItemSummon(int objectId)
	{
		if (!isStarted()) return true;
		if (isPlayerParticipant(objectId) && !Config.LM_EVENT_SUMMON_BY_ITEM_ALLOWED) return false;
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
		if (killedPlayerInstance == null || !isStarted()) return;
		
		if (!isPlayerParticipant(killedPlayerInstance.getObjectId())) return;
		
		short killedCredits = _lmPlayer.get(killedPlayerInstance.getObjectId()).getCredits();
		if (killedCredits <= 1)
		{
			removeParticipant(killedPlayerInstance);
			new LMEventTeleporter(killedPlayerInstance, Config.LM_EVENT_PARTICIPATION_NPC_COORDINATES, true, true);			
		}
		else
		{
			_lmPlayer.get(killedPlayerInstance.getObjectId()).decreaseCredits();
			new LMEventTeleporter(killedPlayerInstance, false, false);
		}
		
		if (killerCharacter == null) return;
		
		L2PcInstance killerPlayerInstance = null;
		
		if (killerCharacter instanceof L2PetInstance || killerCharacter instanceof L2SummonInstance)
		{
			killerPlayerInstance = ((L2Summon) killerCharacter).getOwner();
			if (killerPlayerInstance == null) return;
		}
		else if (killerCharacter instanceof L2PcInstance)
			killerPlayerInstance = (L2PcInstance) killerCharacter;
		else
			return;
		
		if (isPlayerParticipant(killerPlayerInstance))
		{			
			_lmPlayer.get(killerPlayerInstance.getObjectId()).increasePoints();
			String msg = "";
			
			CreatureSay cs = new CreatureSay(killerPlayerInstance.getObjectId(), Say2.TELL, "LM Event", "You killed " + _lmPlayer.get(killerPlayerInstance.getObjectId()).getPoints() + " player(s)!");
			killerPlayerInstance.sendPacket(cs);
			if (killedCredits <= 1)
				msg = "You do not have credits, leaving the event!";
			else
				msg = "Now you have " + String.valueOf(killedCredits - 1) + " credit(s)!";
			cs = new CreatureSay(killedPlayerInstance.getObjectId(), Say2.TELL, "LM Event", msg);
			killedPlayerInstance.sendPacket(cs);
		}
		
		if (getPlayerCounts() == 1) LMManager.getInstance().skipDelay();
	}
	
	/**
	 * Called on Appearing packet received (player finished teleporting)<br><br>
	 * 
	 * @param L2PcInstance activeChar
	 */
	public static void onTeleported(L2PcInstance activeChar)
	{
		if (!isStarted() || activeChar == null || !isPlayerParticipant(activeChar.getObjectId())) return;
		
		if (activeChar.isMageClass())
		{
			if (Config.LM_EVENT_MAGE_BUFFS != null && !Config.LM_EVENT_MAGE_BUFFS.isEmpty())
			{
				for (int i : Config.LM_EVENT_MAGE_BUFFS.keys())
				{
					L2Skill skill = SkillTable.getInstance().getInfo(i, Config.LM_EVENT_MAGE_BUFFS.get(i));
					if (skill != null)
						skill.getEffects(activeChar, activeChar);
				}
			}
		}
		else
		{
			if (Config.LM_EVENT_FIGHTER_BUFFS != null && !Config.LM_EVENT_FIGHTER_BUFFS.isEmpty())
			{
				for (int i : Config.LM_EVENT_FIGHTER_BUFFS.keys())
				{
					L2Skill skill = SkillTable.getInstance().getInfo(i, Config.LM_EVENT_FIGHTER_BUFFS.get(i));
					if (skill != null)
						skill.getEffects(activeChar, activeChar);
				}
			}
		}		
		removeParty(activeChar);
	}
	
	/*
     * Return true if player valid for skill
     */
    public static final boolean checkForLMSkill(L2PcInstance source, L2PcInstance target, L2Skill skill)
    {
    	if (!isStarted()) return true;

    	// LM is started
    	final boolean isSourceParticipant = isPlayerParticipant(source);
    	final boolean isTargetParticipant = isPlayerParticipant(target);

    	// both players not participating
    	if (!isSourceParticipant && !isTargetParticipant) return true;
    	// one player not participating
    	if (!(isSourceParticipant && isTargetParticipant)) return false;

    	return true;
    }
    
    public static int getPlayerCounts()
    {
    	return _lmPlayer.size();
    }
	
	public static int getLMEventInstance()
	{
		return _LMEventInstance;
	}

	public static void removeParty(L2PcInstance activeChar)
	{
		if (activeChar.getParty() != null)
		{
			L2Party party = activeChar.getParty();
			party.removePartyMember(activeChar);
		}
	}
	
	public static byte[] generateHex(int size)
	{
		byte[] array = new byte[size];
		Rnd.nextBytes(array);
		return array;
	}
	
	public static String hexToString(byte[] hex)
	{
		return new BigInteger(hex).toString(16);
	}
}
