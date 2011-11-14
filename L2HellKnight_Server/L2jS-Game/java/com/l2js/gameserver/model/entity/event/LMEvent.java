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

import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.ArrayUtils;

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
public class LMEvent
{
	enum EventState
	{
		INACTIVE, INACTIVATING, PARTICIPATING, STARTING, STARTED, REWARDING
	}

	static
	{
		LMRestriction.getInstance().activate();
	}

	protected static final Logger _log = Logger.getLogger(LMEvent.class.getName());

	private static final String htmlPath = "data/html/mods/LMEvent/";

	private static EventState _state = EventState.INACTIVE;

	private static L2Spawn _npcSpawn = null;

	private static L2Npc _lastNpcSpawn = null;

	private static int _LMEventInstance = 0;

	private static Map<Integer, LMPlayer> _lmPlayer = new FastMap<Integer, LMPlayer>();

	private static DecimalFormat f = new DecimalFormat(",##0,000");

	public LMEvent()
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
			_lastNpcSpawn.spawnMe(_npcSpawn.getLastSpawn().getX(), _npcSpawn.getLastSpawn().getY(), _npcSpawn
					.getLastSpawn().getZ());
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

		if (Config.LM_EVENT_IN_INSTANCE)
		{
			try
			{
				_LMEventInstance = InstanceManager.getInstance().createDynamicInstance(Config.LM_EVENT_INSTANCE_FILE);
				InstanceManager.getInstance().getInstance(_LMEventInstance).setAllowSummon(false);
				InstanceManager.getInstance().getInstance(_LMEventInstance).setPvPInstance(true);
				InstanceManager.getInstance().getInstance(_LMEventInstance)
						.setEmptyDestroyTime(Config.LM_EVENT_START_LEAVE_TELEPORT_DELAY * 1000 + 60000L);
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

		for (LMPlayer p : _lmPlayer.values())
		{
			if (p != null)
			{
				// Teleporter implements Runnable and starts itself
				new LMEventTeleporter(p.getPlayer(), false, false);
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

			// Check for stackable item, non stackabe items need to be added one
			// by one
			if (ItemTable.getInstance().createDummyItem(reward[0]).isStackable())
			{
				inv.addItem("LM Event", reward[0], reward[1], activeChar, activeChar);

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
					inv.addItem("LM Event", reward[0], 1, activeChar, activeChar);
					systemMessage = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
					systemMessage.addItemName(reward[0]);
					activeChar.sendPacket(systemMessage);
				}
			}
			htmltext += " - " + (reward[1] > 999 ? f.format(reward[1]) : reward[1]) + " "
					+ ItemTable.getInstance().getTemplate(reward[0]).getName() + "<br1>";
		}

		StatusUpdate statusUpdate = new StatusUpdate(activeChar);
		NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);

		statusUpdate.addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad());
		npcHtmlMessage.setHtml(HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), htmlPath + "Reward.htm"));
		activeChar.sendPacket(statusUpdate);
		npcHtmlMessage.replace("%palyer%", activeChar.getName());
		npcHtmlMessage.replace("%killed%", String.valueOf(p.getPoints()));
		npcHtmlMessage.replace("%died%", String.valueOf(Config.LM_EVENT_PLAYER_CREDITS - p.getCredits()));
		npcHtmlMessage.replace("%reward%", htmltext);
		activeChar.sendPacket(npcHtmlMessage);
	}

	public static void stopFight()
	{
		// Set state INACTIVATING
		setState(EventState.INACTIVATING);
		// Unspawn event npc
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

	public static synchronized boolean addParticipant(L2PcInstance activeChar)
	{
		// Check for nullpoitner
		if (activeChar == null)
			return false;

		if (isPlayerParticipant(activeChar))
			return false;

		String hexCode = EventConfig.hexToString(EventConfig.generateHex(16));
		_lmPlayer.put(activeChar.getObjectId(), new LMPlayer(activeChar, hexCode));
		return true;
	}

	public static boolean isPlayerParticipant(L2PcInstance activeChar)
	{
		if (activeChar == null)
			return false;
		if (_lmPlayer.containsKey(activeChar.getObjectId()))
			return true;
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

		_lmPlayer.remove(activeChar.getObjectId());

		return true;
	}

	public static boolean needParticipationFee()
	{
		return Config.LM_EVENT_PARTICIPATION_FEE[0] != 0 && Config.LM_EVENT_PARTICIPATION_FEE[1] != 0;
	}

	public static boolean hasParticipationFee(L2PcInstance activeChar)
	{
		return activeChar.getInventory().getInventoryItemCount(Config.LM_EVENT_PARTICIPATION_FEE[0], -1) >= Config.LM_EVENT_PARTICIPATION_FEE[1];
	}

	public static boolean payParticipationFee(L2PcInstance activeChar)
	{
		return activeChar.destroyItemByItemId("LM Participation Fee", Config.LM_EVENT_PARTICIPATION_FEE[0],
				Config.LM_EVENT_PARTICIPATION_FEE[1], _lastNpcSpawn, true);
	}

	public static String getParticipationFee()
	{
		int itemId = Config.LM_EVENT_PARTICIPATION_FEE[0];
		int itemNum = Config.LM_EVENT_PARTICIPATION_FEE[1];

		if (itemId == 0 || itemNum == 0)
			return "-";

		return StringUtil.concat(String.valueOf(itemNum), " ", ItemTable.getInstance().getTemplate(itemId).getName());
	}

	public static void sysMsgToAllParticipants(String message)
	{
		for (LMPlayer player : _lmPlayer.values())
			if (player != null)
				player.getPlayer().sendMessage(message);
	}

	public static void onLogin(L2PcInstance activeChar)
	{
		if (activeChar == null || (!isStarting() && !isStarted()))
			return;
		if (!isPlayerParticipant(activeChar))
			return;

		new LMEventTeleporter(activeChar, false, false);
	}

	public static void onLogout(L2PcInstance activeChar)
	{
		if (activeChar != null && (isStarting() || isStarted() || isParticipating()))
		{
			if (removeParticipant(activeChar))
				activeChar.setXYZInvisible(Config.LM_EVENT_PARTICIPATION_NPC_COORDINATES[0] + Rnd.get(101) - 50,
						Config.LM_EVENT_PARTICIPATION_NPC_COORDINATES[1] + Rnd.get(101) - 50,
						Config.LM_EVENT_PARTICIPATION_NPC_COORDINATES[2]);
		}
	}

	public static synchronized void onBypass(String command, L2PcInstance activeChar)
	{
		if (activeChar == null || !isParticipating())
			return;

		final String htmContent;

		if (command.equals("lm_event_participation"))
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
			else if (playerLevel < Config.LM_EVENT_MIN_LVL || playerLevel > Config.LM_EVENT_MAX_LVL)
			{
				htmContent = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), htmlPath + "Level.htm");
				if (htmContent != null)
				{
					npcHtmlMessage.setHtml(htmContent);
					npcHtmlMessage.replace("%min%", String.valueOf(Config.LM_EVENT_MIN_LVL));
					npcHtmlMessage.replace("%max%", String.valueOf(Config.LM_EVENT_MAX_LVL));
				}
			}
			else if (getPlayerCounts() == Config.LM_EVENT_MAX_PLAYERS)
			{
				htmContent = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), htmlPath + "Full.htm");
				if (htmContent != null)
				{
					npcHtmlMessage.setHtml(htmContent);
					npcHtmlMessage.replace("%max%", String.valueOf(Config.LM_EVENT_MAX_PLAYERS));
				}
			}
			else if (Config.LM_EVENT_MULTIBOX_PROTECTION_ENABLE && onMultiBoxRestriction(activeChar))
			{
				htmContent = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), htmlPath + "MultiBox.htm");
				if (htmContent != null)
				{
					npcHtmlMessage.setHtml(htmContent);
					npcHtmlMessage.replace("%maxbox%", String.valueOf(Config.LM_EVENT_NUMBER_BOX_REGISTER));
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
		else if (command.equals("lm_event_remove_participation"))
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
		if (isPlayerParticipant(objectId) && !Config.LM_EVENT_SCROLL_ALLOWED)
			return false;
		return true;
	}

	public static boolean onPotionUse(int objectId)
	{
		if (!isStarted())
			return true;
		if (isPlayerParticipant(objectId) && !Config.LM_EVENT_POTIONS_ALLOWED)
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
		if (isPlayerParticipant(objectId) && !Config.LM_EVENT_SUMMON_BY_ITEM_ALLOWED)
			return false;
		return true;
	}

	public static void onKill(L2Character killerCharacter, L2PcInstance killedPlayerInstance)
	{
		if (killedPlayerInstance == null || !isStarted())
			return;

		if (!isPlayerParticipant(killedPlayerInstance.getObjectId()))
			return;

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
			_lmPlayer.get(killerPlayerInstance.getObjectId()).increasePoints();
			String msg = "";

			CreatureSay cs = new CreatureSay(killerPlayerInstance.getObjectId(), Say2.TELL, "LM Event", "You killed "
					+ _lmPlayer.get(killerPlayerInstance.getObjectId()).getPoints() + " player(s)!");
			killerPlayerInstance.sendPacket(cs);
			if (killedCredits <= 1)
				msg = "You do not have credits, leaving the event!";
			else
				msg = "Now you have " + String.valueOf(killedCredits - 1) + " credit(s)!";
			cs = new CreatureSay(killedPlayerInstance.getObjectId(), Say2.TELL, "LM Event", msg);
			killedPlayerInstance.sendPacket(cs);
		}

		if (getPlayerCounts() == 1)
			LMManager.getInstance().skipDelay();
	}

	public static void onTeleported(L2PcInstance activeChar)
	{
		if (!isStarted() || activeChar == null || !isPlayerParticipant(activeChar.getObjectId()))
			return;

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
		EventConfig.removeParty(activeChar);
	}

	public static final boolean checkForLMSkill(L2PcInstance source, L2PcInstance target, L2Skill skill)
	{
		if (!isStarted())
			return true;

		// LM is started
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
		return _lmPlayer.size();
	}

	public static int getLMEventInstance()
	{
		return _LMEventInstance;
	}

	public static Map<Integer, L2PcInstance> allParticipants()
	{
		Map<Integer, L2PcInstance> all = new FastMap<Integer, L2PcInstance>();
		if (getPlayerCounts() > 0)
		{
			for (LMPlayer lp : _lmPlayer.values())
				all.put(lp.getPlayer().getObjectId(), lp.getPlayer());
			return all;
		}
		return all;
	}
	
	public static L2PcInstance[] getAllParticipantsArray()
	{
		L2PcInstance[] all = new L2PcInstance[0];
		for (L2PcInstance p : allParticipants().values())
			all = (L2PcInstance[]) ArrayUtils.add(all, p);
		return all;
	}
	
	public static boolean onMultiBoxRestriction(L2PcInstance activeChar)
	{
		return IPManager.getInstance().validBox(activeChar, Config.LM_EVENT_NUMBER_BOX_REGISTER, getAllParticipantsArray(), false);
	}
}
