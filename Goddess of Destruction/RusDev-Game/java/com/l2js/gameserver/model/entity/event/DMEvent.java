/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2js.gameserver.model.entity.event;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastMap;

import com.l2js.Config;
import com.l2js.gameserver.cache.HtmCache;
import com.l2js.gameserver.datatables.*;
import com.l2js.gameserver.instancemanager.IPManager;
import com.l2js.gameserver.instancemanager.InstanceManager;
import com.l2js.gameserver.model.L2Skill;
import com.l2js.gameserver.model.L2Spawn;
import com.l2js.gameserver.model.L2World;
import com.l2js.gameserver.model.actor.L2Character;
import com.l2js.gameserver.model.actor.L2Npc;
import com.l2js.gameserver.model.actor.L2Summon;
import com.l2js.gameserver.model.actor.instance.L2DoorInstance;
import com.l2js.gameserver.model.actor.instance.L2PcInstance;
import com.l2js.gameserver.model.actor.instance.L2PetInstance;
import com.l2js.gameserver.model.actor.instance.L2SummonInstance;
import com.l2js.gameserver.model.itemcontainer.PcInventory;
import com.l2js.gameserver.model.olympiad.OlympiadManager;
import com.l2js.gameserver.network.SystemMessageId;
import com.l2js.gameserver.network.clientpackets.Say2;
import com.l2js.gameserver.network.serverpackets.*;
import com.l2js.gameserver.templates.chars.L2NpcTemplate;
import com.l2js.util.Rnd;
import com.l2js.util.StringUtil;

/**
 * @author L0ngh0rn
 */
public class DMEvent
{
	enum EventState
	{
		INACTIVE, INACTIVATING, PARTICIPATING, STARTING, STARTED, REWARDING
	}

	static
	{
		DMRestriction.getInstance().activate();
	}

	protected static final Logger _log = Logger.getLogger(DMEvent.class.getName());
	
	private static final String htmlPath = "data/html/mods/DMEvent/";
	
	private static EventState _state = EventState.INACTIVE;
	
	private static L2Spawn _npcSpawn = null;
	
	private static L2Npc _lastNpcSpawn = null;
	
	private static int _DMEventInstance = 0;

	private static Map<Integer, DMPlayer> _dmPlayer = new FastMap<Integer, DMPlayer>();

	public DMEvent()
	{
	}

	public static void init()
	{
	}

	private static void setState(EventState state)
	{
		synchronized (_state)
		{
			_state = state;
		}
	}

	public static boolean isInactive()
	{
		boolean isInactive;

		synchronized (_state)
		{
			isInactive = _state == EventState.INACTIVE;
		}

		return isInactive;
	}

	public static boolean isInactivating()
	{
		boolean isInactivating;

		synchronized (_state)
		{
			isInactivating = _state == EventState.INACTIVATING;
		}

		return isInactivating;
	}

	public static boolean isParticipating()
	{
		boolean isParticipating;

		synchronized (_state)
		{
			isParticipating = _state == EventState.PARTICIPATING;
		}

		return isParticipating;
	}

	public static boolean isStarting()
	{
		boolean isStarting;

		synchronized (_state)
		{
			isStarting = _state == EventState.STARTING;
		}

		return isStarting;
	}

	public static boolean isStarted()
	{
		boolean isStarted;

		synchronized (_state)
		{
			isStarted = _state == EventState.STARTED;
		}

		return isStarted;
	}

	public static boolean isRewarding()
	{
		boolean isRewarding;

		synchronized (_state)
		{
			isRewarding = _state == EventState.REWARDING;
		}

		return isRewarding;
	}

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

	public static boolean startParticipation()
	{
		L2NpcTemplate tmpl = NpcTable.getInstance().getTemplate(Config.DM_EVENT_PARTICIPATION_NPC_ID);

		if (tmpl == null)
		{
			_log.warning("DMEventEngine[DMEvent.startParticipation()]: L2NpcTemplate is a NullPointer -> Invalid npc id in configs?");
			return false;
		}

		try
		{
			_npcSpawn = new L2Spawn(tmpl);

			_npcSpawn.setLocx(Config.DM_EVENT_PARTICIPATION_NPC_COORDINATES[0]);
			_npcSpawn.setLocy(Config.DM_EVENT_PARTICIPATION_NPC_COORDINATES[1]);
			_npcSpawn.setLocz(Config.DM_EVENT_PARTICIPATION_NPC_COORDINATES[2]);
			_npcSpawn.setAmount(1);
			_npcSpawn.setHeading(Config.DM_EVENT_PARTICIPATION_NPC_COORDINATES[3]);
			_npcSpawn.setRespawnDelay(1);
			// later no need to delete spawn from db, we don't store it (false)
			SpawnTable.getInstance().addNewSpawn(_npcSpawn, false);
			_npcSpawn.init();
			_lastNpcSpawn = _npcSpawn.getLastSpawn();
			_lastNpcSpawn.setCurrentHp(_lastNpcSpawn.getMaxHp());
			_lastNpcSpawn.setTitle("DM Event Participation");
			_lastNpcSpawn.isAggressive();
			_lastNpcSpawn.decayMe();
			_lastNpcSpawn.spawnMe(_npcSpawn.getLastSpawn().getX(), _npcSpawn.getLastSpawn().getY(), _npcSpawn
					.getLastSpawn().getZ());
			_lastNpcSpawn.broadcastPacket(new MagicSkillUse(_lastNpcSpawn, _lastNpcSpawn, 1034, 1, 1, 1));
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "DMEventEngine[DMEvent.startParticipation()]: exception: " + e.getMessage(), e);
			return false;
		}

		setState(EventState.PARTICIPATING);
		return true;
	}

	public static boolean startFight()
	{
		// Set state to STARTING
		setState(EventState.STARTING);

		L2PcInstance player;
		Iterator<L2PcInstance> iter;
		if (needParticipationFee())
		{
			iter = allParticipants().values().iterator();
			while (iter.hasNext())
			{
				player = iter.next();
				if (!hasParticipationFee(player))
					iter.remove();
			}
		}

		// Check the number of participants
		if (_dmPlayer.size() < Config.DM_EVENT_MIN_PLAYERS)
		{
			// Set state INACTIVE
			setState(EventState.INACTIVE);

			// Cleanup of participants
			_dmPlayer.clear();

			// Unspawn the event NPC
			unSpawnNpc();
			return false;
		}

		if (needParticipationFee())
		{
			iter = allParticipants().values().iterator();
			while (iter.hasNext())
			{
				player = iter.next();
				if (!payParticipationFee(player))
					iter.remove();
			}
		}

		if (Config.DM_EVENT_IN_INSTANCE)
		{
			try
			{
				_DMEventInstance = InstanceManager.getInstance().createDynamicInstance(Config.DM_EVENT_INSTANCE_FILE);
				InstanceManager.getInstance().getInstance(_DMEventInstance).setAllowSummon(false);
				InstanceManager.getInstance().getInstance(_DMEventInstance).setPvPInstance(true);
				InstanceManager.getInstance().getInstance(_DMEventInstance)
						.setEmptyDestroyTime(Config.DM_EVENT_START_LEAVE_TELEPORT_DELAY * 1000 + 60000L);
			}
			catch (Exception e)
			{
				_DMEventInstance = 0;
				_log.log(Level.WARNING, "DMEventEngine[DMEvent.createDynamicInstance]: exception: " + e.getMessage(), e);
			}
		}

		// Opens all doors specified in configs for dm
		openDoors(Config.DM_DOORS_IDS_TO_OPEN);
		// Closes all doors specified in configs for dm
		closeDoors(Config.DM_DOORS_IDS_TO_CLOSE);
		// Set state STARTED
		setState(EventState.STARTED);

		for (DMPlayer p : _dmPlayer.values())
		{
			if (p != null)
			{
				// Teleporter implements Runnable and starts itself
				new DMEventTeleporter(p.getPlayer(), false, false);
			}

		}

		return true;
	}

	public static TreeSet<DMPlayer> orderPosition(Collection<DMPlayer> listPlayer)
	{
		TreeSet<DMPlayer> players = new TreeSet<DMPlayer>(new Comparator<DMPlayer>()
		{
			@Override
			public int compare(DMPlayer p1, DMPlayer p2)
			{
				Integer c1 = Integer.valueOf(p2.getPoints() - p1.getPoints());
				Integer c2 = Integer.valueOf(p1.getDeath() - p2.getDeath());
				Integer c3 = p1.getHexCode().compareTo(p2.getHexCode());

				if (c1 == 0)
				{
					if (c2 == 0)
						return c3;
					return c2;
				}
				return c1;
			}
		});
		players.addAll(listPlayer);
		return players;
	}

	public static String calculateRewards()
	{
		TreeSet<DMPlayer> players = orderPosition(_dmPlayer.values());

		for (int j = 0; j < Config.DM_REWARD_FIRST_PLAYERS; j++)
		{
			if (players.isEmpty())
				break;

			DMPlayer player = players.first();

			if (player.getPoints() == 0)
				break;

			rewardPlayer(player, j + 1);
			players.remove(player);
			int playerPointPrev = player.getPoints();

			if (!Config.DM_REWARD_PLAYERS_TIE)
				continue;

			while (!players.isEmpty())
			{
				player = players.first();
				if (player.getPoints() != playerPointPrev)
					break;
				rewardPlayer(player, j + 1);
				players.remove(player);
			}
		}

		// Set state REWARDING so nobody can point anymore
		setState(EventState.REWARDING);

		return "DM Event ended, thanks to everyone who participated!";
	}

	private static void rewardPlayer(DMPlayer p, int pos)
	{
		L2PcInstance activeChar = p.getPlayer();

		// Check for nullpointer
		if (activeChar == null)
			return;

		SystemMessage systemMessage = null;

		List<int[]> rewards = Config.DM_EVENT_REWARDS.get(pos);

		for (int[] reward : rewards)
		{
			PcInventory inv = activeChar.getInventory();

			// Check for stackable item, non stackabe items need to be added one
			// by one
			if (ItemTable.getInstance().createDummyItem(reward[0]).isStackable())
			{
				inv.addItem("DM Event", reward[0], reward[1], activeChar, activeChar);

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

				activeChar.sendPacket(systemMessage);
			}
			else
			{
				for (int i = 0; i < reward[1]; ++i)
				{
					inv.addItem("DM Event", reward[0], 1, activeChar, activeChar);
					systemMessage = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
					systemMessage.addItemName(reward[0]);
					activeChar.sendPacket(systemMessage);
				}
			}
		}

		StatusUpdate statusUpdate = new StatusUpdate(activeChar);
		NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);

		statusUpdate.addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad());
		npcHtmlMessage.setHtml(HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), htmlPath + "Reward.htm"));
		activeChar.sendPacket(statusUpdate);
		activeChar.sendPacket(npcHtmlMessage);
	}

	public static void stopFight()
	{
		// Set state INACTIVATING
		setState(EventState.INACTIVATING);
		// Unspawn event npc
		unSpawnNpc();
		// Opens all doors specified in configs for DM
		openDoors(Config.DM_DOORS_IDS_TO_CLOSE);
		// Closes all doors specified in Configs for DM
		closeDoors(Config.DM_DOORS_IDS_TO_OPEN);

		String[] topPositions;
		String htmltext = "";
		if (Config.DM_SHOW_TOP_RANK)
		{
			topPositions = getFirstPosition(Config.DM_TOP_RANK);
			Boolean c = true;
			String c1 = "D9CC46";
			String c2 = "FFFFFF";
			if (topPositions != null)
				for (int i = 0; i < topPositions.length; i++)
				{
					String color = (c ? c1 : c2);
					String[] row = topPositions[i].split("\\,");
					htmltext += "<tr>";
					htmltext += "<td width=\"35\" align=\"center\"><font color=\"" + color + "\">"
							+ String.valueOf(i + 1) + "</font></td>";
					htmltext += "<td width=\"100\" align=\"left\"><font color=\"" + color + "\">" + row[0]
							+ "</font></td>";
					htmltext += "<td width=\"125\" align=\"right\"><font color=\"" + color + "\">" + row[1]
							+ "</font></td>";
					htmltext += "</tr>";
					c = !c;
				}
		}

		for (DMPlayer player : _dmPlayer.values())
		{
			if (player != null)
			{
				// Top Rank
				if (Config.DM_SHOW_TOP_RANK)
				{
					NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);
					npcHtmlMessage.setHtml(HtmCache.getInstance().getHtm(player.getPlayer().getHtmlPrefix(),
							htmlPath + "TopRank.htm"));
					npcHtmlMessage.replace("%toprank%", htmltext);
					player.getPlayer().sendPacket(npcHtmlMessage);
				}
				new DMEventTeleporter(player.getPlayer(), Config.DM_EVENT_PARTICIPATION_NPC_COORDINATES, false, false);
			}
		}

		// Cleanup list
		_dmPlayer = new FastMap<Integer, DMPlayer>();
		// Set state INACTIVE
		setState(EventState.INACTIVE);
	}

	public static synchronized boolean addParticipant(L2PcInstance activeChar)
	{
		// Check for nullpoitner
		if (activeChar == null)
			return false;

		if (isPlayerParticipant(activeChar))
			return false;

		String hexCode = EventConfig.hexToString(EventConfig.generateHex(16));
		_dmPlayer.put(activeChar.getObjectId(), new DMPlayer(activeChar, hexCode));
		return true;
	}

	public static boolean isPlayerParticipant(L2PcInstance activeChar)
	{
		if (activeChar == null)
			return false;
		try
		{
			if (_dmPlayer.containsKey(activeChar.getObjectId()))
				return true;
		}
		catch (Exception e)
		{
			return false;
		}
		return false;
	}

	public static boolean isPlayerParticipant(int objectId)
	{
		L2PcInstance activeChar = L2World.getInstance().getPlayer(objectId);
		if (activeChar == null)
			return false;
		return isPlayerParticipant(activeChar);
	}

	public static boolean removeParticipant(L2PcInstance activeChar)
	{
		if (activeChar == null)
			return false;

		if (!isPlayerParticipant(activeChar))
			return false;

		try
		{
			_dmPlayer.remove(activeChar.getObjectId());
		}
		catch (Exception e)
		{
			return false;
		}

		return true;
	}

	public static boolean needParticipationFee()
	{
		return Config.DM_EVENT_PARTICIPATION_FEE[0] != 0 && Config.DM_EVENT_PARTICIPATION_FEE[1] != 0;
	}

	public static boolean hasParticipationFee(L2PcInstance activeChar)
	{
		return activeChar.getInventory().getInventoryItemCount(Config.DM_EVENT_PARTICIPATION_FEE[0], -1) >= Config.DM_EVENT_PARTICIPATION_FEE[1];
	}

	public static boolean payParticipationFee(L2PcInstance activeChar)
	{
		return activeChar.destroyItemByItemId("DM Participation Fee", Config.DM_EVENT_PARTICIPATION_FEE[0],
				Config.DM_EVENT_PARTICIPATION_FEE[1], _lastNpcSpawn, true);
	}

	public static String getParticipationFee()
	{
		int itemId = Config.DM_EVENT_PARTICIPATION_FEE[0];
		int itemNum = Config.DM_EVENT_PARTICIPATION_FEE[1];

		if (itemId == 0 || itemNum == 0)
			return "-";

		return StringUtil.concat(String.valueOf(itemNum), " ", ItemTable.getInstance().getTemplate(itemId).getName());
	}

	public static void sysMsgToAllParticipants(String message)
	{
		for (DMPlayer player : _dmPlayer.values())
			if (player != null)
				player.getPlayer().sendMessage(message);
	}

	public static void onLogin(L2PcInstance activeChar)
	{
		if (activeChar == null || (!isStarting() && !isStarted()))
		{
			return;
		}

		if (!isPlayerParticipant(activeChar))
			return;

		new DMEventTeleporter(activeChar, false, false);
	}

	public static void onLogout(L2PcInstance activeChar)
	{
		if (activeChar != null && (isStarting() || isStarted() || isParticipating()))
		{
			if (removeParticipant(activeChar))
				activeChar.setXYZInvisible(Config.DM_EVENT_PARTICIPATION_NPC_COORDINATES[0] + Rnd.get(101) - 50,
						Config.DM_EVENT_PARTICIPATION_NPC_COORDINATES[1] + Rnd.get(101) - 50,
						Config.DM_EVENT_PARTICIPATION_NPC_COORDINATES[2]);
		}
	}

	public static synchronized void onBypass(String command, L2PcInstance activeChar)
	{
		if (activeChar == null || !isParticipating())
			return;

		final String htmContent;

		if (command.equals("dm_event_participation"))
		{
			NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);
			int playerLevel = activeChar.getLevel();

			if (activeChar.isCursedWeaponEquipped())
			{
				htmContent = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(),
						htmlPath + "CursedWeaponEquipped.htm");
				if (htmContent != null)
					npcHtmlMessage.setHtml(htmContent);
			}
			else if (OlympiadManager.getInstance().isRegistered(activeChar))
			{
				htmContent = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), htmlPath + "Olympiad.htm");
				if (htmContent != null)
					npcHtmlMessage.setHtml(htmContent);
			}
			else if (activeChar.getKarma() > 0)
			{
				htmContent = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), htmlPath + "Karma.htm");
				if (htmContent != null)
					npcHtmlMessage.setHtml(htmContent);
			}
			else if (playerLevel < Config.DM_EVENT_MIN_LVL || playerLevel > Config.DM_EVENT_MAX_LVL)
			{
				htmContent = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), htmlPath + "Level.htm");
				if (htmContent != null)
				{
					npcHtmlMessage.setHtml(htmContent);
					npcHtmlMessage.replace("%min%", String.valueOf(Config.DM_EVENT_MIN_LVL));
					npcHtmlMessage.replace("%max%", String.valueOf(Config.DM_EVENT_MAX_LVL));
				}
			}
			else if (_dmPlayer.size() == Config.DM_EVENT_MAX_PLAYERS)
			{
				htmContent = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), htmlPath + "Full.htm");
				if (htmContent != null)
				{
					npcHtmlMessage.setHtml(htmContent);
					npcHtmlMessage.replace("%max%", String.valueOf(Config.DM_EVENT_MAX_PLAYERS));
				}
			}
			else if (Config.DM_EVENT_MULTIBOX_PROTECTION_ENABLE && onMultiBoxRestriction(activeChar))
			{
				htmContent = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), htmlPath + "MultiBox.htm");
				if (htmContent != null)
				{
					npcHtmlMessage.setHtml(htmContent);
					npcHtmlMessage.replace("%maxbox%", String.valueOf(Config.DM_EVENT_NUMBER_BOX_REGISTER));
				}
			}
			else if (needParticipationFee() && !hasParticipationFee(activeChar))
			{
				htmContent = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(),
						htmlPath + "ParticipationFee.htm");
				if (htmContent != null)
				{
					npcHtmlMessage.setHtml(htmContent);
					npcHtmlMessage.replace("%fee%", getParticipationFee());
				}
			}
			else if (isPlayerParticipant(activeChar))
				npcHtmlMessage.setHtml(HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(),
						htmlPath + "Registered.htm"));
			else if (addParticipant(activeChar))
				npcHtmlMessage.setHtml(HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(),
						htmlPath + "Registered.htm"));
			else
				return;

			activeChar.sendPacket(npcHtmlMessage);
		}
		else if (command.equals("dm_event_remove_participation"))
		{
			if (isPlayerParticipant(activeChar))
			{
				removeParticipant(activeChar);

				NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);

				npcHtmlMessage.setHtml(HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(),
						htmlPath + "Unregistered.htm"));
				activeChar.sendPacket(npcHtmlMessage);
			}
		}
	}

	public static boolean onAction(L2PcInstance activeChar, int targetedPlayerObjectId)
	{
		if (activeChar == null || !isStarted())
			return true;
		if (activeChar.isGM())
			return true;
		if (!isPlayerParticipant(activeChar) && isPlayerParticipant(targetedPlayerObjectId))
			return false;
		if (isPlayerParticipant(activeChar) && !isPlayerParticipant(targetedPlayerObjectId))
			return false;

		return true;
	}

	public static boolean onScrollUse(int objectId)
	{
		if (!isStarted())
			return true;

		if (isPlayerParticipant(objectId) && !Config.DM_EVENT_SCROLL_ALLOWED)
			return false;

		return true;
	}

	public static boolean onPotionUse(int objectId)
	{
		if (!isStarted())
			return true;

		if (isPlayerParticipant(objectId) && !Config.DM_EVENT_POTIONS_ALLOWED)
			return false;

		return true;
	}

	public static boolean onEscapeUse(int objectId)
	{
		if (!isStarted())
			return true;

		if (isPlayerParticipant(objectId))
			return false;

		return true;
	}

	public static boolean onItemSummon(int objectId)
	{
		if (!isStarted())
			return true;

		if (isPlayerParticipant(objectId) && !Config.DM_EVENT_SUMMON_BY_ITEM_ALLOWED)
			return false;

		return true;
	}

	public static void onKill(L2Character killerCharacter, L2PcInstance killedPlayerInstance)
	{
		if (killedPlayerInstance == null || !isStarted())
			return;

		if (!isPlayerParticipant(killedPlayerInstance.getObjectId()))
			return;

		new DMEventTeleporter(killedPlayerInstance, false, false);

		if (killerCharacter == null)
			return;

		L2PcInstance killerPlayerInstance = null;

		if (killerCharacter instanceof L2PetInstance || killerCharacter instanceof L2SummonInstance)
		{
			killerPlayerInstance = ((L2Summon) killerCharacter).getOwner();
			if (killerPlayerInstance == null)
				return;
		}
		else if (killerCharacter instanceof L2PcInstance)
			killerPlayerInstance = (L2PcInstance) killerCharacter;
		else
			return;

		if (isPlayerParticipant(killerPlayerInstance))
		{
			_dmPlayer.get(killerPlayerInstance.getObjectId()).increasePoints();
			killerPlayerInstance.sendPacket(new CreatureSay(killerPlayerInstance.getObjectId(), Say2.TELL,
					killerPlayerInstance.getName(), "I have killed " + killedPlayerInstance.getName() + "!"));

			_dmPlayer.get(killedPlayerInstance.getObjectId()).increaseDeath();
			killedPlayerInstance.sendPacket(new CreatureSay(killerPlayerInstance.getObjectId(), Say2.TELL,
					killerPlayerInstance.getName(), "I killed you!"));
		}
	}

	public static void onTeleported(L2PcInstance activeChar)
	{
		if (!isStarted() || activeChar == null || !isPlayerParticipant(activeChar.getObjectId()))
			return;

		if (activeChar.isMageClass())
		{
			if (Config.DM_EVENT_MAGE_BUFFS != null && !Config.DM_EVENT_MAGE_BUFFS.isEmpty())
			{
				for (int i : Config.DM_EVENT_MAGE_BUFFS.keys())
				{
					L2Skill skill = SkillTable.getInstance().getInfo(i, Config.DM_EVENT_MAGE_BUFFS.get(i));
					if (skill != null)
						skill.getEffects(activeChar, activeChar);
				}
			}
		}
		else
		{
			if (Config.DM_EVENT_FIGHTER_BUFFS != null && !Config.DM_EVENT_FIGHTER_BUFFS.isEmpty())
			{
				for (int i : Config.DM_EVENT_FIGHTER_BUFFS.keys())
				{
					L2Skill skill = SkillTable.getInstance().getInfo(i, Config.DM_EVENT_FIGHTER_BUFFS.get(i));
					if (skill != null)
						skill.getEffects(activeChar, activeChar);
				}
			}
		}

		EventConfig.removeParty(activeChar);
	}

	public static final boolean checkForDMSkill(L2PcInstance source, L2PcInstance target, L2Skill skill)
	{
		if (!isStarted())
			return true;

		// DM is started
		final boolean isSourceParticipant = isPlayerParticipant(source);
		final boolean isTargetParticipant = isPlayerParticipant(target);

		// both players not participating
		if (!isSourceParticipant && !isTargetParticipant)
			return true;
		// one player not participating
		if (!(isSourceParticipant && isTargetParticipant))
			return false;

		return true;
	}

	public static int getPlayerCounts()
	{
		return _dmPlayer.size();
	}

	public static String[] getFirstPosition(int countPos)
	{
		TreeSet<DMPlayer> players = orderPosition(_dmPlayer.values());
		String text = "";
		for (int j = 0; j < countPos; j++)
		{
			if (players.isEmpty())
				break;

			DMPlayer player = players.first();

			if (player.getPoints() == 0)
				break;

			text += player.getPlayer().getName() + "," + String.valueOf(player.getPoints()) + ";";
			players.remove(player);

			int playerPointPrev = player.getPoints();

			if (!Config.DM_REWARD_PLAYERS_TIE)
				continue;

			while (!players.isEmpty())
			{
				player = players.first();
				if (player.getPoints() != playerPointPrev)
					break;
				text += player.getPlayer().getName() + "," + String.valueOf(player.getPoints()) + ";";
				players.remove(player);
			}
		}

		if (text != "")
			return text.split("\\;");

		return null;
	}

	public static int getDMEventInstance()
	{
		return _DMEventInstance;
	}

	public static Map<Integer, L2PcInstance> allParticipants()
	{
		Map<Integer, L2PcInstance> all = new FastMap<Integer, L2PcInstance>();
		if (getPlayerCounts() > 0)
		{
			for (DMPlayer dp : _dmPlayer.values())
				all.put(dp.getPlayer().getObjectId(), dp.getPlayer());
			return all;
		}
		return all;
	}
	
	public static boolean onMultiBoxRestriction(L2PcInstance activeChar)
	{
		return IPManager.getInstance().validBox(activeChar, Config.DM_EVENT_NUMBER_BOX_REGISTER, allParticipants().values(), false);
	}
}
