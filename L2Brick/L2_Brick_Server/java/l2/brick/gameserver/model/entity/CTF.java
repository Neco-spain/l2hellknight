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
package l2.brick.gameserver.model.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Vector;
import java.util.Calendar;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import javolution.text.TextBuilder;
import javolution.util.FastMap;

import l2.brick.Config;
import l2.brick.L2DatabaseFactory;
import l2.brick.gameserver.Announcements;
import l2.brick.gameserver.GmListTable;
import l2.brick.gameserver.ThreadPoolManager;
import l2.brick.gameserver.datatables.ItemTable;
import l2.brick.gameserver.datatables.NpcTable;
import l2.brick.gameserver.datatables.SpawnTable;
import l2.brick.gameserver.model.L2Effect;
import l2.brick.gameserver.model.L2Party;
import l2.brick.gameserver.model.L2Radar;
import l2.brick.gameserver.model.L2Spawn;
import l2.brick.gameserver.model.L2Party.messageType;
import l2.brick.gameserver.model.actor.L2Summon;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.actor.instance.L2PetInstance;
import l2.brick.gameserver.model.item.instance.L2ItemInstance;
import l2.brick.gameserver.model.itemcontainer.Inventory;
import l2.brick.gameserver.network.SystemMessageId;
import l2.brick.gameserver.network.serverpackets.ActionFailed;
import l2.brick.gameserver.network.serverpackets.CreatureSay;
import l2.brick.gameserver.network.serverpackets.InventoryUpdate;
import l2.brick.gameserver.network.serverpackets.ItemList;
import l2.brick.gameserver.network.serverpackets.MagicSkillUse;
import l2.brick.gameserver.network.serverpackets.NpcHtmlMessage;
import l2.brick.gameserver.network.serverpackets.PlaySound;
import l2.brick.gameserver.network.serverpackets.RadarControl;
import l2.brick.gameserver.network.serverpackets.SocialAction;
import l2.brick.gameserver.network.serverpackets.SystemMessage;
import l2.brick.gameserver.templates.chars.L2NpcTemplate;
import l2.brick.gameserver.templates.skills.L2EffectType;
import l2.brick.util.Rnd;

public class CTF
{		
		/** Task for event cycles<br> */
		private CTFStartTask _task1;
		
		private CTF()
		{
			if (Config.CTF_EVENT_ENABLED)
			{
				loadData();				
				this.scheduleCTFEventStart();
				_log.warning("CTF Event Engine: Started.");
			}
			else
			{
				_log.warning("CTF Event Engine: Disabled by config.");
			}
		}
		
		/**
		 * Initialize new/Returns the one and only instance<br><br>
		 *
		 * @return CTF<br>
		 */
		public static CTF getInstance()
		{
			return SingletonHolder._instance;
		}
		
		/**
		 * Starts CTFStartTask
		 */
		public void scheduleCTFEventStart()
		{
			try
			{
				Calendar currentTime = Calendar.getInstance();
				Calendar nextStartTime = null;
				Calendar testStartTime = null;
				for (String timeOfDay : Config.CTF_EVENT_INTERVAL)
				{
					// Creating a Calendar object from the specified interval value
					testStartTime = Calendar.getInstance();
					testStartTime.setLenient(true);
					String[] splitTimeOfDay = timeOfDay.split(":");
					testStartTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(splitTimeOfDay[0]));
					testStartTime.set(Calendar.MINUTE, Integer.parseInt(splitTimeOfDay[1]));
					// If the date is in the past, make it the next day (Example: Checking for "1:00", when the time is 23:57.)
					if (testStartTime.getTimeInMillis() < currentTime.getTimeInMillis())
					{
						testStartTime.add(Calendar.DAY_OF_MONTH, 1);
					}
					// Check for the test date to be the minimum (smallest in the specified list)
					if (nextStartTime == null || testStartTime.getTimeInMillis() < nextStartTime.getTimeInMillis())
					{
						nextStartTime = testStartTime;
					}
				}
				_task1 = new CTFStartTask(nextStartTime.getTimeInMillis());
				ThreadPoolManager.getInstance().executeTask(_task1);
			}
			catch (Exception e)
			{
				_log.warning("CTFEventEngine: Error figuring out a start time. Check CTFEventInterval in config file.");
			}
		}
		
			
		public void skipDelay()
		{
			if (_task1.nextRun.cancel(false))
			{
				_task1.setStartTime(System.currentTimeMillis());
				ThreadPoolManager.getInstance().executeTask(_task1);
			}
		}
		
		/**
		 * Class forCTFT cycles
		 */
		class CTFStartTask implements Runnable
		{
			private long _startTime;
			public ScheduledFuture<?> nextRun;
			
			public CTFStartTask(long startTime)
			{
				_startTime = startTime;
			}
			
			public void setStartTime(long startTime)
			{
				_startTime = startTime;
			}
			
			/**
			 * @see java.lang.Runnable#run()
			 */
			@Override
			public void run()
			{
				int delay = (int) Math.round((_startTime - System.currentTimeMillis()) / 1000.0);
				
				
				int nextMsg = 0;
				if (delay > 3600)
				{
					nextMsg = delay - 3600;
				}
				else if (delay > 1800)
				{
					nextMsg = delay - 1800;
				}
				else if (delay > 900)
				{
					nextMsg = delay - 900;
				}
				else if (delay > 600)
				{
					nextMsg = delay - 600;
				}
				else if (delay > 300)
				{
					nextMsg = delay - 300;
				}
				else if (delay > 60)
				{
					nextMsg = delay - 60;
				}
				else if (delay > 5)
				{
					nextMsg = delay - 5;
				}
				else if (delay > 0)
				{
					nextMsg = delay;
				}
				else
				{
					// start

					autoEvent();
					_task1.setStartTime(System.currentTimeMillis() + 60000L * 225);
					ThreadPoolManager.getInstance().executeTask(_task1);
				}
				
				if (delay > 0)
				{
					nextRun = ThreadPoolManager.getInstance().scheduleGeneral(this, nextMsg * 1000);
				}
			}
		}
			
	private final static Logger _log = Logger.getLogger(CTF.class.getName());
	private static int _FlagNPC = 35062, _FLAG_IN_HAND_ITEM_ID = 6718;
	public static String _eventName = new String(), _eventDesc = new String(), _topTeam = new String(), _joiningLocationName = new String();
	public static Vector<String> _teams = new Vector<String>(), _savePlayers = new Vector<String>(), _savePlayerTeams = new Vector<String>();
	public static Vector<L2PcInstance> _players = new Vector<L2PcInstance>(), _playersShuffle = new Vector<L2PcInstance>();
	public static Vector<Integer> _teamPlayersCount = new Vector<Integer>(), _teamColors = new Vector<Integer>(), _teamsX = new Vector<Integer>(), _teamsY = new Vector<Integer>(), _teamsZ = new Vector<Integer>(), _teamsBaseX = new Vector<Integer>(), _teamsBaseY = new Vector<Integer>(),
			_teamsBaseZ = new Vector<Integer>();
	public static boolean _joining = false, _teleport = false, _started = false, _sitForced = false;
	public static L2Spawn _npcSpawn;
	public static int _npcId = 0, _npcX = 0, _npcY = 0, _npcZ = 0, _npcHeading = 0, _rewardId = 0, _rewardAmount = 0, _minlvl = 0, _maxlvl = 0, _joinTime = 0, _eventTime = 0, _minPlayers = 0, _maxPlayers = 0;
	public static long _flagHoldTime = 0;
	public static Vector<Integer> _teamPointsCount = new Vector<Integer>();
	public static Vector<Integer> _flagIds = new Vector<Integer>(), _flagsX = new Vector<Integer>(), _flagsY = new Vector<Integer>(), _flagsZ = new Vector<Integer>();
	public static Vector<L2Spawn> _flagSpawns = new Vector<L2Spawn>(), _throneSpawns = new Vector<L2Spawn>();
	public static Vector<Boolean> _flagsTaken = new Vector<Boolean>(), _flagsNotRemoved = new Vector<Boolean>();
	public static int _topScore = 0, eventCenterX = 0, eventCenterY = 0, eventCenterZ = 0, eventOffset = 0;
	public static Map<String, Integer> _playerScores = new FastMap<String, Integer>();
	
	public static void showFlagHtml(L2PcInstance eventPlayer, String objectId, String teamName)
	{
		if (eventPlayer == null)
			return;
		
		try
		{
			NpcHtmlMessage adminReply = new NpcHtmlMessage(0);
			
			TextBuilder replyMSG = new TextBuilder();
			
			replyMSG.append("<html><body><center>");
			replyMSG.append("CTF Flag<br><br>");
			replyMSG.append("<font color=\"00FF00\">" + teamName + "'s Flag</font><br>");
			if (eventPlayer._teamNameCTF != null && eventPlayer._teamNameCTF.equals(teamName))
				replyMSG.append("<font color=\"LEVEL\">This is your Flag</font><br>");
			else
				replyMSG.append("<font color=\"LEVEL\">Enemy Flag!</font><br>");
			if (_started)
			{
				processInFlagRange(eventPlayer);
			}
			else
				replyMSG.append("CTF match is not in progress yet.<br>Wait for a GM to start the event<br>");
			replyMSG.append("</center></body></html>");
			adminReply.setHtml(replyMSG.toString());
			eventPlayer.sendPacket(adminReply);
		}
		catch (Exception e)
		{
			_log.warning("" + "CTF Engine[showEventHtlm(" + eventPlayer.getName() + ", " + objectId + ")]: exception: " + e.getStackTrace());
		}
	}
	
	public static void CheckRestoreFlags()
	{
		Vector<Integer> teamsTakenFlag = new Vector<Integer>();
		try
		{
			for (L2PcInstance player : _players)
			{ //if there's a player with a flag
				//add the index of the team who's FLAG WAS TAKEN to the list
				if (player != null)
				{
					if (!player.isOnline() && player._haveFlagCTF)// logged off with a flag in his hands
					{
						AnnounceToPlayers(false, "CTF Event: " + player.getName() + " logged off with a CTF flag!");
						player._haveFlagCTF = false;
						if (_teams.indexOf(player._teamNameHaveFlagCTF) >= 0)
						{
							if (_flagsTaken.get(_teams.indexOf(player._teamNameHaveFlagCTF)))
							{
								_flagsTaken.set(_teams.indexOf(player._teamNameHaveFlagCTF), false);
								spawnFlag(player._teamNameHaveFlagCTF);
								AnnounceToPlayers(false, "CTF Event: " + player._teamNameHaveFlagCTF + " flag now returned to place.");
							}
						}
						removeFlagFromPlayer(player);
						player._teamNameHaveFlagCTF = null;
						return;
					}
					else if (player._haveFlagCTF)
						teamsTakenFlag.add(_teams.indexOf(player._teamNameHaveFlagCTF));
				}
			}
			//Go over the list of ALL teams
			for (String team : _teams)
			{
				if (team == null)
					continue;
				int index = _teams.indexOf(team);
				if (!teamsTakenFlag.contains(index))
				{
					if (_flagsTaken.get(index))
					{
						_flagsTaken.set(index, false);
						spawnFlag(team);
						AnnounceToPlayers(false, "CTF Event: " + team + " flag returned due to player error.");
					}
				}
			}
			//Check if a player ran away from the event holding a flag:
			for (L2PcInstance player : _players)
			{
				if (player != null && player._haveFlagCTF)
				{
					if (isOutsideCTFArea(player))
					{
						AnnounceToPlayers(false, "CTF Event: " + player.getName() + " escaped from the event holding a flag!");
						player._haveFlagCTF = false;
						if (_teams.indexOf(player._teamNameHaveFlagCTF) >= 0)
						{
							if (_flagsTaken.get(_teams.indexOf(player._teamNameHaveFlagCTF)))
							{
								_flagsTaken.set(_teams.indexOf(player._teamNameHaveFlagCTF), false);
								spawnFlag(player._teamNameHaveFlagCTF);
								AnnounceToPlayers(false, "CTF Event: " + player._teamNameHaveFlagCTF + " flag now returned to place.");
							}
						}
						removeFlagFromPlayer(player);
						player._teamNameHaveFlagCTF = null;
						if (Config.CTF_BASE_TELEPORT_FIRST)
						{
							player.teleToLocation(_teamsBaseX.get(_teams.indexOf(player._teamNameCTF)), _teamsBaseY.get(_teams.indexOf(player._teamNameCTF)), _teamsBaseZ.get(_teams.indexOf(player._teamNameCTF)));
							
							ThreadPoolManager.getInstance().scheduleGeneral(new BaseTeleportTask(player, false), 10000);
							
							player.sendMessage("You have been returned to your base. You will be sent into battle in 10 seconds.");
						}
						else
						{
							player.teleToLocation(_teamsX.get(_teams.indexOf(player._teamNameCTF)), _teamsY.get(_teams.indexOf(player._teamNameCTF)), _teamsZ.get(_teams.indexOf(player._teamNameCTF)));
							player.sendMessage("You have been returned to your team spawn.");
						}
						return;
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.warning("CTF.restoreFlags() Error:" + e.toString());
		}
	}
	
	public static void kickPlayerFromCTf(L2PcInstance playerToKick)
	{
		if (playerToKick == null)
			return;
		
		if (_joining)
		{
			_playersShuffle.remove(playerToKick);
			_players.remove(playerToKick);
			playerToKick._inEventCTF = false;
			playerToKick._teamNameCTF = new String();
		}
		if (_started || _teleport)
		{
			_playersShuffle.remove(playerToKick);
			playerToKick._inEventCTF = false;
			removePlayer(playerToKick);
			if (playerToKick.isOnline())
			{
				playerToKick.getAppearance().setNameColor(playerToKick._originalNameColorCTF);
				playerToKick.setKarma(playerToKick._originalKarmaCTF);
				playerToKick.setTitle(playerToKick._originalTitleCTF);
				playerToKick.broadcastUserInfo();
				playerToKick.sendMessage("You have been kicked from the CTF.");
				playerToKick.teleToLocation(_npcX, _npcY, _npcZ, false);
			}
		}
	}
	
	public static void AnnounceToPlayers(Boolean toall, String announce)
	{
		if (toall)
			Announcements.getInstance().announceToAll(announce);
		else
		{
			CreatureSay cs = new CreatureSay(0, 2, "", "Announcements : " + announce);
			if (_players != null && !_players.isEmpty())
			{
				for (L2PcInstance player : _players)
				{
					if (player != null && player.isOnline())
						player.sendPacket(cs);
				}
			}
		}
	}
	
	public static void Started(L2PcInstance player)
	{
		player._teamNameHaveFlagCTF = null;
		player._haveFlagCTF = false;
	}
	
	public static void StartEvent()
	{
		for (L2PcInstance player : _players)
		{
			if (player != null)
			{
				player._teamNameHaveFlagCTF = null;
				player._haveFlagCTF = false;
			}
		}
	}
	
	public static void addFlagToPlayer(L2PcInstance _player)
	{
		//remove items from the player hands (right, left, both)
		// This is NOT a BUG, I don't want them to see the icon they have 8D
		L2ItemInstance wpn = _player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
		L2ItemInstance wpn2 = _player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		if (wpn != null)
		{
			_player.getInventory().unEquipItemInBodySlotAndRecord(wpn.getItem().getBodyPart());
			if (wpn2 != null)
				_player.getInventory().unEquipItemInBodySlotAndRecord(wpn2.getItem().getBodyPart());
		}
		//add the flag in his hands
		_player.getInventory().equipItem(ItemTable.getInstance().createItem("", CTF._FLAG_IN_HAND_ITEM_ID, 1, _player, null));
		_player.broadcastPacket(new SocialAction(_player.getObjectId(), 16)); //amazing glow
		_player._haveFlagCTF = true;
		_player.broadcastUserInfo();
		CreatureSay cs = new CreatureSay(_player.getObjectId(), 15, ":", "You got it! Run back! ::"); // 8D
		_player.sendPacket(cs);
		
		// Start the flag holding timer 
		_flagsNotRemoved.set(_teams.indexOf(_player._teamNameCTF), true);
		flagHoldTimer(_player, _flagHoldTime);
		
		// If player is invisible, make them visible
		if (_player.getAppearance().getInvisible())
		{
			@SuppressWarnings("unused")
			L2Effect eInvisible = _player.getFirstEffect(L2EffectType.HIDE);
		}
	}
	
	public static void removeFlagFromPlayer(L2PcInstance player)
	{
		L2ItemInstance wpn = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
		player._haveFlagCTF = false;
		
		// Reset boolean of whether the holder's flag has not been removed yet to false and kill the flagHoldTimer thread
		_flagsNotRemoved.set(_teams.indexOf(player._teamNameCTF), false);
		
		if (wpn != null)
		{
			L2ItemInstance[] unequiped = player.getInventory().unEquipItemInBodySlotAndRecord(wpn.getItem().getBodyPart());
			player.getInventory().destroyItemByItemId("", CTF._FLAG_IN_HAND_ITEM_ID, 1, player, null);
			InventoryUpdate iu = new InventoryUpdate();
			for (L2ItemInstance element : unequiped)
				iu.addModifiedItem(element);
			player.sendPacket(iu);
			player.sendPacket(new ItemList(player, true)); // get your weapon back now ...
			player.abortAttack();
			player.broadcastUserInfo();
		}
		else
		{
			player.getInventory().destroyItemByItemId("", CTF._FLAG_IN_HAND_ITEM_ID, 1, player, null);
			player.sendPacket(new ItemList(player, true)); // get your weapon back now ...
			player.abortAttack();
			player.broadcastUserInfo();
		}
	}
	
	public static void setTeamFlag(String teamName, L2PcInstance activeChar)
	{
		int index = _teams.indexOf(teamName);
		
		if (index == -1)
			return;
		addOrSet(_teams.indexOf(teamName), null, false, _FlagNPC, activeChar.getX(), activeChar.getY(), activeChar.getZ());
	}
	
	public static void setTeamFlag(String teamName, int x, int y, int z)
	{
		int index = _teams.indexOf(teamName);
		
		if (index == -1)
			return;
		addOrSet(_teams.indexOf(teamName), null, false, _FlagNPC, x, y, z);
	}
	
	public static void spawnAllFlags()
	{
		while (_flagSpawns.size() < _teams.size())
			_flagSpawns.add(null);
		while (_throneSpawns.size() < _teams.size())
			_throneSpawns.add(null);
		for (String team : _teams)
		{
			int index = _teams.indexOf(team);
			L2NpcTemplate tmpl = NpcTable.getInstance().getTemplate(_flagIds.get(index));
			L2NpcTemplate throne = NpcTable.getInstance().getTemplate(32027);
			try
			{
				//spawn throne
				_throneSpawns.set(index, new L2Spawn(throne));
				_throneSpawns.get(index).setLocx(_flagsX.get(index));
				_throneSpawns.get(index).setLocy(_flagsY.get(index));
				_throneSpawns.get(index).setLocz(_flagsZ.get(index) - 10);
				_throneSpawns.get(index).setAmount(1);
				_throneSpawns.get(index).setHeading(0);
				_throneSpawns.get(index).setRespawnDelay(1);
				SpawnTable.getInstance().addNewSpawn(_throneSpawns.get(index), false);
				_throneSpawns.get(index).init();
				_throneSpawns.get(index).getLastSpawn().getStatus().setCurrentHp(999999999);
				_throneSpawns.get(index).getLastSpawn().decayMe();
				_throneSpawns.get(index).getLastSpawn().spawnMe(_throneSpawns.get(index).getLastSpawn().getX(), _throneSpawns.get(index).getLastSpawn().getY(), _throneSpawns.get(index).getLastSpawn().getZ());
				_throneSpawns.get(index).getLastSpawn().setTitle(team + " Throne");
				_throneSpawns.get(index).getLastSpawn().broadcastPacket(new MagicSkillUse(_throneSpawns.get(index).getLastSpawn(), _throneSpawns.get(index).getLastSpawn(), 1036, 1, 5500, 1));
				_throneSpawns.get(index).getLastSpawn()._isCTF_throneSpawn = true;
				
				//spawn flag
				_flagSpawns.set(index, new L2Spawn(tmpl));
				_flagSpawns.get(index).setLocx(_flagsX.get(index));
				_flagSpawns.get(index).setLocy(_flagsY.get(index));
				_flagSpawns.get(index).setLocz(_flagsZ.get(index));
				_flagSpawns.get(index).setAmount(1);
				_flagSpawns.get(index).setHeading(0);
				_flagSpawns.get(index).setRespawnDelay(1);
				SpawnTable.getInstance().addNewSpawn(_flagSpawns.get(index), false);
				_flagSpawns.get(index).init();
				_flagSpawns.get(index).getLastSpawn().getStatus().setCurrentHp(999999999);
				_flagSpawns.get(index).getLastSpawn().setTitle(team + "'s Flag");
				_flagSpawns.get(index).getLastSpawn()._CTF_FlagTeamName = team;
				_flagSpawns.get(index).getLastSpawn().decayMe();
				_flagSpawns.get(index).getLastSpawn().spawnMe(_flagSpawns.get(index).getLastSpawn().getX(), _flagSpawns.get(index).getLastSpawn().getY(), _flagSpawns.get(index).getLastSpawn().getZ());
				_flagSpawns.get(index).getLastSpawn()._isCTF_Flag = true;
				if (index == (_teams.size() - 1))
					calculateOutSideOfCTF(); // sets event boundaries so players don't run with the flag.
			}
			catch (Exception e)
			{
				_log.warning("CTF Engine[spawnAllFlags()]: exception: " + e.getStackTrace());
			}
		}
	}
	
	public static void processTopTeam()
	{
		
		_topTeam = null;
		for (String team : _teams)
		{
			if (teamPointsCount(team) == _topScore && _topScore > 0)
				_topTeam = null;
			if (teamPointsCount(team) > _topScore)
			{
				_topTeam = team;
				_topScore = teamPointsCount(team);
			}
		}
		if (_topScore <= 0)
		{
			AnnounceToPlayers(true, "CTF Event: No flags taken.");
		}
		else
		{
			if (_topTeam == null)
				AnnounceToPlayers(true, "CTF Event: Maximum flags taken : " + _topScore + " flags! No one won.");
			else
			{
				AnnounceToPlayers(true, "CTF Event: Team " + _topTeam + " wins the match, with " + _topScore + " flags taken!");
				rewardTeam(_topTeam);
			}
		}
	}
	
	public static void unspawnAllFlags()
	{
		try
		{
			if (_throneSpawns == null || _flagSpawns == null || _teams == null)
				return;
			for (String team : _teams)
			{
				int index = _teams.indexOf(team);
				if (_throneSpawns.get(index) != null)
				{
					_throneSpawns.get(index).getLastSpawn().deleteMe();
					_throneSpawns.get(index).stopRespawn();
					SpawnTable.getInstance().deleteSpawn(_throneSpawns.get(index), true);
				}
				if (_flagSpawns.get(index) != null)
				{
					_flagSpawns.get(index).getLastSpawn().deleteMe();
					_flagSpawns.get(index).stopRespawn();
					SpawnTable.getInstance().deleteSpawn(_flagSpawns.get(index), true);
				}
			}
			_throneSpawns.removeAllElements();
		}
		catch (Throwable t)
		{
			_log.warning("CTF Engine[unspawnAllFlags()]: exception: " + t.getStackTrace());
		}
	}
	
	private static void unspawnFlag(String teamName)
	{
		int index = _teams.indexOf(teamName);
		
		_flagSpawns.get(index).getLastSpawn().deleteMe();
		_flagSpawns.get(index).stopRespawn();
		SpawnTable.getInstance().deleteSpawn(_flagSpawns.get(index), true);
	}
	
	public static void spawnFlag(String teamName)
	{
		int index = _teams.indexOf(teamName);
		L2NpcTemplate tmpl = NpcTable.getInstance().getTemplate(_flagIds.get(index));
		
		try
		{
			_flagSpawns.set(index, new L2Spawn(tmpl));
			
			_flagSpawns.get(index).setLocx(_flagsX.get(index));
			_flagSpawns.get(index).setLocy(_flagsY.get(index));
			_flagSpawns.get(index).setLocz(_flagsZ.get(index));
			_flagSpawns.get(index).setAmount(1);
			_flagSpawns.get(index).setHeading(0);
			_flagSpawns.get(index).setRespawnDelay(1);
			
			SpawnTable.getInstance().addNewSpawn(_flagSpawns.get(index), false);
			
			_flagSpawns.get(index).init();
			_flagSpawns.get(index).getLastSpawn().getStatus().setCurrentHp(999999999);
			_flagSpawns.get(index).getLastSpawn().setTitle(teamName + "'s Flag");
			_flagSpawns.get(index).getLastSpawn()._CTF_FlagTeamName = teamName;
			_flagSpawns.get(index).getLastSpawn()._isCTF_Flag = true;
			_flagSpawns.get(index).getLastSpawn().decayMe();
			_flagSpawns.get(index).getLastSpawn().spawnMe(_flagSpawns.get(index).getLastSpawn().getX(), _flagSpawns.get(index).getLastSpawn().getY(), _flagSpawns.get(index).getLastSpawn().getZ());
		}
		catch (Exception e)
		{
			_log.warning("CTF Engine[spawnFlag(" + teamName + ")]: exception: " + e.getStackTrace());
		}
	}
	
	public static boolean InRangeOfFlag(L2PcInstance _player, int flagIndex, int offset)
	{
		if (_player.getX() > CTF._flagsX.get(flagIndex) - offset && _player.getX() < CTF._flagsX.get(flagIndex) + offset && _player.getY() > CTF._flagsY.get(flagIndex) - offset && _player.getY() < CTF._flagsY.get(flagIndex) + offset && _player.getZ() > CTF._flagsZ.get(flagIndex) - offset && _player.getZ() < CTF._flagsZ.get(flagIndex) + offset)
			return true;
		return false;
	}
	
	public static void processInFlagRange(L2PcInstance _player)
	{
		try
		{
			CheckRestoreFlags();
			for (String team : _teams)
			{
				if (team.equals(_player._teamNameCTF))
				{
					int indexOwn = _teams.indexOf(_player._teamNameCTF);
					
					//if player is near his team flag holding the enemy flag
					if (InRangeOfFlag(_player, indexOwn, 100) && !_flagsTaken.get(indexOwn) && _player._haveFlagCTF)
					{
						int indexEnemy = _teams.indexOf(_player._teamNameHaveFlagCTF);
						//return enemy flag to place
						_flagsTaken.set(indexEnemy, false);
						spawnFlag(_player._teamNameHaveFlagCTF);
						//remove the flag from this player
						_player.broadcastPacket(new SocialAction(_player.getObjectId(), 16)); // amazing glow
						_player.broadcastUserInfo();
						_player.broadcastPacket(new SocialAction(_player.getObjectId(), 3)); // Victory
						_player.broadcastUserInfo();
						removeFlagFromPlayer(_player);
						_teamPointsCount.set(indexOwn, teamPointsCount(team) + 1);
						_player.broadcastPacket(new PlaySound(0, "ItemSound.quest_finish", 1, _player.getObjectId(), _player.getX(), _player.getY(), _player.getZ()));
						_player.broadcastUserInfo();
						_playerScores.put(_player.getName(), playerScoresCount(_player.getName()) + 1);
						AnnounceToPlayers(false, "CTF Event: " + _player.getName() + " scores for " + _player._teamNameCTF + " team.");
						AnnounceToPlayers(false, "CTF Event: Scores - " + _teams.get(0) + ": " + teamPointsCount(_teams.get(0)) + " " + _teams.get(1) + ": " + teamPointsCount(_teams.get(1)));
					}
				}
				else
				{
					int indexEnemy = _teams.indexOf(team);
					//if the player is near a enemy flag
					if (InRangeOfFlag(_player, indexEnemy, 100) && !_flagsTaken.get(indexEnemy) && !_player._haveFlagCTF && !_player.isDead())
					{
						if (_player.isRidingStrider() || _player.isFlying())
						{
							_player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_A_PET_OR_A_SERVITOR_IS_SUMMONED));
							break;
						}
						
						_flagsTaken.set(indexEnemy, true);
						unspawnFlag(team);
						_player._teamNameHaveFlagCTF = team;
						addFlagToPlayer(_player);
						_player.broadcastUserInfo();
						_player._haveFlagCTF = true;
						AnnounceToPlayers(false, "CTF Event: " + team + " flag taken by " + _player.getName() + "...");
						pointTeamTo(_player, team);
						break;
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void pointTeamTo(L2PcInstance hasFlag, String ourFlag)
	{
		try
		{
			for (L2PcInstance player : _players)
			{
				if (player != null && player.isOnline())
				{
					if (player._teamNameCTF.equals(ourFlag))
					{
						player.sendMessage(hasFlag.getName() + " took your flag!");
						if (player._haveFlagCTF)
						{
							player.sendMessage("You can not return the flag to headquarters, until your flag is returned to it's place.");
							player.sendPacket(new RadarControl(1, 1, player.getX(), player.getY(), player.getZ()));
						}
						else
						{
							player.sendPacket(new RadarControl(0, 1, hasFlag.getX(), hasFlag.getY(), hasFlag.getZ()));
							L2Radar rdr = new L2Radar(player);
							L2Radar.RadarOnPlayer radar = rdr.new RadarOnPlayer(hasFlag, player);
							ThreadPoolManager.getInstance().scheduleGeneral(radar, 10000 + Rnd.get(30000));
						}
					}
				}
			}
		}
		catch (Throwable t)
		{
		}
	}
	
	private static int playerScoresCount(String player)
	{
		if (_playerScores.containsKey(player))
			return _playerScores.get(player);
		else if (player != null)
		{
			_playerScores.put(player, 0);
			return _playerScores.get(player);
		}
		else
			return 0;
	}
	
	public static int teamPointsCount(String teamName)
	{
		int index = _teams.indexOf(teamName);
		
		if (index == -1)
			return -1;
		
		return _teamPointsCount.get(index);
	}
	
	public static void setTeamPointsCount(String teamName, int teamPointCount)
	{
		int index = _teams.indexOf(teamName);
		
		if (index == -1)
			return;
		
		_teamPointsCount.set(index, teamPointCount);
	}
	
	public static int teamPlayersCount(String teamName)
	{
		int index = _teams.indexOf(teamName);
		
		if (index == -1)
			return -1;
		
		return _teamPlayersCount.get(index);
	}
	
	public static void setTeamPlayersCount(String teamName, int teamPlayersCount)
	{
		int index = _teams.indexOf(teamName);
		
		if (index == -1)
			return;
		
		_teamPlayersCount.set(index, teamPlayersCount);
	}
	
	public static void setNpcPos(L2PcInstance activeChar)
	{
		_npcX = activeChar.getX();
		_npcY = activeChar.getY();
		_npcZ = activeChar.getZ();
		_npcHeading = activeChar.getHeading();
	}
	
	public static void setNpcPos(int x, int y, int z)
	{
		_npcX = x;
		_npcY = y;
		_npcZ = z;
	}
	
	public static void addTeam(String teamName)
	{
		if (!checkTeamOk())
		{
			if (Config.DEBUG)
				_log.fine("CTF Engine[addTeam(" + teamName + ")]: checkTeamOk() = false");
			return;
		}
		
		if (teamName.equals(" "))
			return;
		
		_teams.add(teamName);
		_teamPlayersCount.add(0);
		_teamColors.add(0);
		_teamsX.add(0);
		_teamsY.add(0);
		_teamsZ.add(0);
		_teamsBaseX.add(0);
		_teamsBaseY.add(0);
		_teamsBaseZ.add(0);
		_teamPointsCount.add(0);
		addOrSet(_teams.indexOf(teamName), null, false, _FlagNPC, 0, 0, 0);
	}
	
	private static void addOrSet(int listSize, L2Spawn flagSpawn, boolean flagsTaken, int flagId, int flagX, int flagY, int flagZ)
	{
		while (_flagsX.size() <= listSize)
		{
			_flagSpawns.add(null);
			_flagsTaken.add(false);
			_flagIds.add(_FlagNPC);
			_flagsX.add(0);
			_flagsY.add(0);
			_flagsZ.add(0);
		}
		_flagSpawns.set(listSize, flagSpawn);
		_flagsTaken.set(listSize, flagsTaken);
		_flagIds.set(listSize, flagId);
		_flagsX.set(listSize, flagX);
		_flagsY.set(listSize, flagY);
		_flagsZ.set(listSize, flagZ);
	}
	
	public static boolean checkMaxLevel(int maxlvl)
	{
		if (_minlvl >= maxlvl)
			return false;
		
		return true;
	}
	
	public static boolean checkMinLevel(int minlvl)
	{
		if (_maxlvl <= minlvl)
			return false;
		
		return true;
	}
	
	/** returns true if participated players is higher or equal then minimum needed players 
	 * @param players 
	 * @return */
	public static boolean checkMinPlayers(int players)
	{
		if (_minPlayers <= players)
			return true;
		
		return false;
	}
	
	/** returns true if max players is higher or equal then participated players 
	 * @param players 
	 * @return */
	public static boolean checkMaxPlayers(int players)
	{
		if (_maxPlayers > players)
			return true;
		
		return false;
	}
	
	public static void removeTeam(String teamName)
	{
		if (!checkTeamOk() || _teams.isEmpty())
		{
			if (Config.DEBUG)
				_log.fine("CTF Engine[removeTeam(" + teamName + ")]: checkTeamOk() = false");
			return;
		}
		
		if (teamPlayersCount(teamName) > 0)
		{
			if (Config.DEBUG)
				_log.fine("CTF Engine[removeTeam(" + teamName + ")]: teamPlayersCount(teamName) > 0");
			return;
		}
		
		int index = _teams.indexOf(teamName);
		
		if (index == -1)
			return;
		
		_teamsZ.remove(index);
		_teamsY.remove(index);
		_teamsX.remove(index);
		_teamsBaseZ.remove(index);
		_teamsBaseY.remove(index);
		_teamsBaseX.remove(index);
		_teamColors.remove(index);
		_teamPointsCount.remove(index);
		_teamPlayersCount.remove(index);
		_teams.remove(index);
		_flagSpawns.remove(index);
		_flagsTaken.remove(index);
		_flagIds.remove(index);
		_flagsX.remove(index);
		_flagsY.remove(index);
		_flagsZ.remove(index);
	}
	
	public static void setTeamPos(String teamName, L2PcInstance activeChar)
	{
		int index = _teams.indexOf(teamName);
		
		if (index == -1)
			return;
		
		_teamsX.set(index, activeChar.getX());
		_teamsY.set(index, activeChar.getY());
		_teamsZ.set(index, activeChar.getZ());
	}
	
	public static void setTeamPos(String teamName, int x, int y, int z)
	{
		int index = _teams.indexOf(teamName);
		
		if (index == -1)
			return;
		
		_teamsX.set(index, x);
		_teamsY.set(index, y);
		_teamsZ.set(index, z);
	}
	
	public static void setTeamBasePos(String teamName, L2PcInstance activeChar)
	{
		int index = _teams.indexOf(teamName);
		
		if (index == -1)
			return;
		
		_teamsBaseX.set(index, activeChar.getX());
		_teamsBaseY.set(index, activeChar.getY());
		_teamsBaseZ.set(index, activeChar.getZ());
	}
	
	public static void setTeamBasePos(String teamName, int x, int y, int z)
	{
		int index = _teams.indexOf(teamName);
		
		if (index == -1)
			return;
		
		_teamsBaseX.set(index, x);
		_teamsBaseY.set(index, y);
		_teamsBaseZ.set(index, z);
	}
	
	public static void setTeamColor(String teamName, int color)
	{
		if (!checkTeamOk())
			return;
		
		int index = _teams.indexOf(teamName);
		
		if (index == -1)
			return;
		
		_teamColors.set(index, color);
	}
	
	public static boolean checkTeamOk()
	{
		if (_started || _teleport || _joining)
			return false;
		
		return true;
	}
	
	public static void startJoin(L2PcInstance activeChar)
	{
		if (!startJoinOk())
		{
			activeChar.sendMessage("Event not setted propertly.");
			if (Config.DEBUG)
				_log.fine("CTF Engine[startJoin(" + activeChar.getName() + ")]: startJoinOk() = false");
			return;
		}
		
		_joining = true;
		spawnEventNpc(activeChar);
		AnnounceToPlayers(true, "CTF Event: Registration opened for 15 minute(s)! Use .joinctf command to register.");
	}
	
	public static void startJoin()
	{
		if (!startJoinOk())
		{
			_log.warning("Event not setted propertly.");
			if (Config.DEBUG)
				_log.fine("CTF Engine[startJoin(startJoinOk() = false");
			return;
		}
		
		_joining = true;
		spawnEventNpc();
	}
	
	public static boolean startAutoJoin()
	{
		if (!startJoinOk())
		{
			if (Config.DEBUG)
				_log.fine("CTF Engine[startJoin]: startJoinOk() = false");
			return false;
		}
		
		_joining = true;
		spawnEventNpc();
		AnnounceToPlayers(true, "CTF Event: Registration opened for 15 minute(s)! Use .joinctf command to register.");
		return true;
	}
	
	public static boolean startJoinOk()
	{
		if (Config.CTF_BASE_TELEPORT_FIRST && (_teamsBaseX.contains(0) || _teamsBaseY.contains(0) || _teamsBaseZ.contains(0)))
			return false;
		else if (_started || _teleport || _joining || _teams.size() < 2 || _eventName.equals("") || _joiningLocationName.equals("") || _eventDesc.equals("") || _npcId == 0 || _npcX == 0 || _npcY == 0 || _npcZ == 0 || _rewardId == 0 || _rewardAmount == 0 || _teamsX.contains(0) || _teamsY.contains(0) || _teamsZ.contains(0) || _flagHoldTime == 0)
			return false;
		try
		{
			if (_flagsX.contains(0) || _flagsY.contains(0) || _flagsZ.contains(0) || _flagIds.contains(0))
				return false;
			if (_flagsX.size() < _teams.size() || _flagsY.size() < _teams.size() || _flagsZ.size() < _teams.size() || _flagIds.size() < _teams.size())
				return false;
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			return false;
		}
		return true;
	}
	
	private static void spawnEventNpc(L2PcInstance activeChar)
	{
		L2NpcTemplate tmpl = NpcTable.getInstance().getTemplate(_npcId);
		
		try
		{
			_npcSpawn = new L2Spawn(tmpl);
			
			_npcSpawn.setLocx(_npcX);
			_npcSpawn.setLocy(_npcY);
			_npcSpawn.setLocz(_npcZ);
			_npcSpawn.setAmount(1);
			_npcSpawn.setHeading(_npcHeading);
			_npcSpawn.setRespawnDelay(1);
			
			SpawnTable.getInstance().addNewSpawn(_npcSpawn, false);
			
			_npcSpawn.init();
			_npcSpawn.getLastSpawn().getStatus().setCurrentHp(999999999);
			_npcSpawn.getLastSpawn().setTitle(_eventName);
			_npcSpawn.getLastSpawn()._isEventMobCTF = true;
			_npcSpawn.getLastSpawn().isAggressive();
			_npcSpawn.getLastSpawn().decayMe();
			_npcSpawn.getLastSpawn().spawnMe(_npcSpawn.getLastSpawn().getX(), _npcSpawn.getLastSpawn().getY(), _npcSpawn.getLastSpawn().getZ());
			
			_npcSpawn.getLastSpawn().broadcastPacket(new MagicSkillUse(_npcSpawn.getLastSpawn(), _npcSpawn.getLastSpawn(), 1034, 1, 1, 1));
		}
		catch (Exception e)
		{
			_log.warning("CTF Engine[spawnEventNpc(" + activeChar.getName() + ")]: exception: " + e.getMessage());
		}
	}
	
	public static class BaseTeleportTask implements Runnable
	{
		L2PcInstance player;
		boolean onBegin;
		
		public BaseTeleportTask(L2PcInstance _player, boolean _onBegin)
		{
			player = _player;
			onBegin = _onBegin;
		}
		
		public BaseTeleportTask(boolean _onBegin)
		{
			onBegin = _onBegin;
		}
		
		@Override
		public void run()
		{
			if (CTF._teleport || CTF._started)
			{
				if (onBegin)
				{
					spawnAllFlags();
					
					for (L2PcInstance players : _players)
					{
						new BaseTeleportTask(players, false).run();
					}
				}
				else
				{
					if (player != null)
					{
						player.teleToLocation(CTF._teamsX.get(CTF._teams.indexOf(player._teamNameCTF)), CTF._teamsY.get(CTF._teams.indexOf(player._teamNameCTF)), CTF._teamsZ.get(CTF._teams.indexOf(player._teamNameCTF)), false);
					}
				}
			}
		}
	}
	
	private static void spawnEventNpc()
	{
		L2NpcTemplate tmpl = NpcTable.getInstance().getTemplate(_npcId);
		
		try
		{
			_npcSpawn = new L2Spawn(tmpl);
			
			_npcSpawn.setLocx(_npcX);
			_npcSpawn.setLocy(_npcY);
			_npcSpawn.setLocz(_npcZ);
			_npcSpawn.setAmount(1);
			_npcSpawn.setHeading(_npcHeading);
			_npcSpawn.setRespawnDelay(1);
			
			SpawnTable.getInstance().addNewSpawn(_npcSpawn, false);
			
			_npcSpawn.init();
			_npcSpawn.getLastSpawn().getStatus().setCurrentHp(999999999);
			_npcSpawn.getLastSpawn().setTitle(_eventName);
			_npcSpawn.getLastSpawn()._isEventMobCTF = true;
			_npcSpawn.getLastSpawn().isAggressive();
			_npcSpawn.getLastSpawn().decayMe();
			_npcSpawn.getLastSpawn().spawnMe(_npcSpawn.getLastSpawn().getX(), _npcSpawn.getLastSpawn().getY(), _npcSpawn.getLastSpawn().getZ());
			
			_npcSpawn.getLastSpawn().broadcastPacket(new MagicSkillUse(_npcSpawn.getLastSpawn(), _npcSpawn.getLastSpawn(), 1034, 1, 1, 1));
		}
		catch (Exception e)
		{
			_log.warning("CTF Engine[spawnEventNpc(exception: " + e.getMessage());
		}
	}
	
	public static void teleportStart()
	{
		if (!_joining || _started || _teleport)
			return;
		
		if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE") && checkMinPlayers(_playersShuffle.size()))
		{
			removeOfflinePlayers();
			shuffleTeams();
		}
		else if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE") && !checkMinPlayers(_playersShuffle.size()))
		{
			return;
		}
		
		_joining = false;
		setUserData();
		
		if (Config.CTF_BASE_TELEPORT_FIRST)
		{
			AnnounceToPlayers(true, "CTF Event: Teleporting to team base. The fight will being in 20 seconds!");
			
			for (L2PcInstance player : _players)
			{
				if (player != null)
				{
					if (Config.CTF_ON_START_UNSUMMON_PET)
					{
						//Remove Summon's buffs
						if (player.getPet() != null)
						{
							L2Summon summon = player.getPet();
							for (L2Effect e : summon.getAllEffects())
								if (e != null)
									e.exit();
							
							if (summon instanceof L2PetInstance)
								summon.unSummon(player);
						}
					}
					
					if (Config.CTF_ON_START_REMOVE_ALL_EFFECTS)
					{
						for (L2Effect e : player.getAllEffects())
						{
							if (e != null)
								e.exit();
						}
					}
					
					//Remove player from his party
					if (player.getParty() != null)
					{
						L2Party party = player.getParty();
						party.removePartyMember(player, messageType.Expelled);
					}
					
					player.teleToLocation(_teamsBaseX.get(_teams.indexOf(player._teamNameCTF)), _teamsBaseY.get(_teams.indexOf(player._teamNameCTF)), _teamsBaseZ.get(_teams.indexOf(player._teamNameCTF)));
				}
			}
			ThreadPoolManager.getInstance().scheduleGeneral(new BaseTeleportTask(true), 20000);
		}
		else
		{
			AnnounceToPlayers(true, "CTF Event: Teleporting to team spot in 15 seconds!");
			
			ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
			{
				@Override
				public void run()
				{
					spawnAllFlags();
					
					for (L2PcInstance player : _players)
					{
						if (player != null)
						{
							if (Config.CTF_ON_START_UNSUMMON_PET)
							{
								//Remove Summon's buffs
								if (player.getPet() != null)
								{
									L2Summon summon = player.getPet();
									for (L2Effect e : summon.getAllEffects())
										if (e != null)
											e.exit();
									
									if (summon instanceof L2PetInstance)
										summon.unSummon(player);
								}
							}
							
							if (Config.CTF_ON_START_REMOVE_ALL_EFFECTS)
							{
								for (L2Effect e : player.getAllEffects())
								{
									if (e != null)
										e.exit();
								}
							}
							
							//Remove player from his party
							if (player.getParty() != null)
							{
								L2Party party = player.getParty();
								party.removePartyMember(player, messageType.Expelled);
							}
							
							player.teleToLocation(_teamsX.get(_teams.indexOf(player._teamNameCTF)), _teamsY.get(_teams.indexOf(player._teamNameCTF)), _teamsZ.get(_teams.indexOf(player._teamNameCTF)));
						}
					}
				}
			}, 15000);
		}
		_teleport = true;
	}
	
	public static boolean teleportAutoStart()
	{
		if (!_joining || _started || _teleport)
			return false;
		
		if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE") && checkMinPlayers(_playersShuffle.size()))
		{
			removeOfflinePlayers();
			shuffleTeams();
		}
		else if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE") && !checkMinPlayers(_playersShuffle.size()))
		{
			AnnounceToPlayers(true, "CTF Event: Not enough players registered.");
			return false;
		}
		
		_joining = false;
		setUserData();
		
		if (Config.CTF_BASE_TELEPORT_FIRST)
		{
			AnnounceToPlayers(true, "CTF Event: Teleporting to team base. The fight will being in 20 seconds!");
			
			for (L2PcInstance player : _players)
			{
				if (player != null)
				{
					if (Config.CTF_ON_START_UNSUMMON_PET)
					{
						//Remove Summon's buffs
						if (player.getPet() != null)
						{
							L2Summon summon = player.getPet();
							for (L2Effect e : summon.getAllEffects())
								if (e != null)
									e.exit();
							
							if (summon instanceof L2PetInstance)
								summon.unSummon(player);
						}
					}
					
					if (Config.CTF_ON_START_REMOVE_ALL_EFFECTS)
					{
						for (L2Effect e : player.getAllEffects())
						{
							if (e != null)
								e.exit();
						}
					}
					
					//Remove player from his party
					if (player.getParty() != null)
					{
						L2Party party = player.getParty();
						party.removePartyMember(player, messageType.Expelled);
					}
					
					player.teleToLocation(_teamsBaseX.get(_teams.indexOf(player._teamNameCTF)), _teamsBaseY.get(_teams.indexOf(player._teamNameCTF)), _teamsBaseZ.get(_teams.indexOf(player._teamNameCTF)));
				}
			}
			ThreadPoolManager.getInstance().scheduleGeneral(new BaseTeleportTask(true), 30000);
		}
		else
		{
			AnnounceToPlayers(false, "CTF Event: Teleporting to team spot in 15 seconds!");
			
			ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
			{
				@Override
				public void run()
				{
					spawnAllFlags();
					
					for (L2PcInstance player : _players)
					{
						if (player != null)
						{
							if (Config.CTF_ON_START_UNSUMMON_PET)
							{
								//Remove Summon's buffs
								if (player.getPet() != null)
								{
									L2Summon summon = player.getPet();
									for (L2Effect e : summon.getAllEffects())
										if (e != null)
											e.exit();
									
									if (summon instanceof L2PetInstance)
										summon.unSummon(player);
								}
							}
							
							if (Config.CTF_ON_START_REMOVE_ALL_EFFECTS)
							{
								for (L2Effect e : player.getAllEffects())
								{
									if (e != null)
										e.exit();
								}
							}
							
							//Remove player from his party
							if (player.getParty() != null)
							{
								L2Party party = player.getParty();
								party.removePartyMember(player, messageType.Expelled);
							}
							
							player.teleToLocation(_teamsX.get(_teams.indexOf(player._teamNameCTF)), _teamsY.get(_teams.indexOf(player._teamNameCTF)), _teamsZ.get(_teams.indexOf(player._teamNameCTF)));
						}
					}
				}
			}, 15000);
		}
		_teleport = true;
		return true;
	}
	
	public static void startEvent(L2PcInstance activeChar)
	{
		if (!startEventOk())
		{
			if (Config.DEBUG)
				_log.fine("CTF Engine[startEvent(" + activeChar.getName() + ")]: startEventOk() = false");
			return;
		}
		
		_teleport = false;
		
		_started = true;
		StartEvent();
	}
	
	public static void setJoinTime(int time)
	{
		_joinTime = time;
	}
	
	public static void setEventTime(int time)
	{
		_eventTime = time;
	}
	
	public static boolean startAutoEvent()
	{
		if (!startEventOk())
		{
			if (Config.DEBUG)
				_log.fine("CTF Engine[startEvent]: startEventOk() = false");
			return false;
		}
		
		_teleport = false;
		
		_started = true;
		return true;
	}
	
	public static synchronized void autoEvent()
	{
		if (startAutoJoin())
		{
			if (_joinTime > 0)
				waiter(_joinTime * 60 * 1000); // minutes for join event
			else if (_joinTime <= 0)
			{
				abortEvent();
				return;
			}
			if (teleportAutoStart())
			{
				waiter(1 * 1 * 1000); // 1 seconds wait time until start fight after teleported

				if (startAutoEvent())
				{
					AnnounceToPlayers(true, "CTF Event: Started. Go Capture the Flags!");
					waiter(_eventTime * 60 * 1000); // minutes for event time
					finishEvent();
				}
			}
			else if (!teleportAutoStart())
			{
				abortEvent();
			}
		}
	}
	
	// a scheduled time to remove the flag after a user set time _flagHoldTime
	private static void flagHoldTimer(final L2PcInstance _player, final long interval)
	{
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			@SuppressWarnings("null")
			public void run()
			{
				if (_started)
				{
					try
					// just to be sure 
					{
						long countDown = System.currentTimeMillis();
						int seconds = (int) interval;
						
						while (countDown + (interval * 1000) > System.currentTimeMillis() && _flagsNotRemoved.get(_teams.indexOf(_player._teamNameCTF)) != false)
						{
							seconds--;
							
							switch (seconds)
							{
								case 600: //  10 minutes left 
									if (_player != null && _player._haveFlagCTF)
										_player.sendMessage("You have 10 minutes to capture the flag or it will be returned.");
									break;
								case 300: // 5 minutes left
									if (_player != null && _player._haveFlagCTF)
										_player.sendMessage("You have 5 minutes to capture the flag or it will be returned.");
									break;
								case 240: // 4 minutes left
									if (_player != null && _player._haveFlagCTF)
										_player.sendMessage("You have 5 minutes to capture the flag or it will be returned.");
									break;
								case 180: // 3 minutes left
									if (_player != null && _player._haveFlagCTF)
										_player.sendMessage("You have 3 minutes to capture the flag or it will be returned.");
									break;
								case 120: // 2 minutes left
									if (_player != null && _player._haveFlagCTF)
										_player.sendMessage("You have 2 minutes to capture the flag or it will be returned.");
									break;
								case 60: // 1 minute left
									if (_player != null && _player._haveFlagCTF)
										_player.sendMessage("You have 1 minute to capture the flag or it will be returned.");
									break;
								case 30: // 30 seconds left
									if (_player != null && _player._haveFlagCTF)
										_player.sendMessage("You have 30 seconds to capture the flag or it will be returned.");
									break;
								case 10: // 10 seconds left
									if (_player != null && _player._haveFlagCTF)
										_player.sendMessage("You have 10 seconds to capture the flag or it will be returned.");
									break;
								case 1: // 1 seconds left		
									if (_player != null && _player._haveFlagCTF)
									{
										removeFlagFromPlayer(_player);
										_flagsTaken.set(_teams.indexOf(_player._teamNameHaveFlagCTF), false);
										spawnFlag(_player._teamNameHaveFlagCTF);
										_player.sendMessage("You've held the flag for too long. The enemy flag has been returned.");
										AnnounceToPlayers(false, "CTF Event: " + _player.getName() + " held the flag for too long. " + _player._teamNameCTF + " flag has been returned.");
									}
									break;
							}
							long startOneSecondWaiterStartTime = System.currentTimeMillis();
							
							// only the try catch with Thread.sleep(1000) give bad countdown on high wait times
							while (startOneSecondWaiterStartTime + 1000 > System.currentTimeMillis())
							{
								try
								{
									Thread.sleep(1);
								}
								catch (InterruptedException ie)
								{
								}
							}
						}
					}
					catch (Exception e)
					{
						_log.warning("Exception: CTF.flagHoldTimer(): " + e.getMessage());
					}
				}
			}
		}, 1);
	}
	
	private static synchronized void waiter(long interval)
	{
		long startWaiterTime = System.currentTimeMillis();
		int seconds = (int) (interval / 1000);
		
		while (startWaiterTime + interval > System.currentTimeMillis())
		{
			seconds--; // here because we don't want to see two time announce at the same time
			
			if (_joining || _started || _teleport)
			{
				switch (seconds)
				{
					case 3600: // 1 hour left
						if (_joining)
						{
							AnnounceToPlayers(true, "CTF Event: " + seconds / 60 / 60 + " hour(s) until registration ends! Use .joinctf command to register.");
						}
						else if (_started)
							AnnounceToPlayers(false, "CTF Event: " + seconds / 60 / 60 + " hour(s) until event ends!");
						
						break;
					case 600: //  10 minutes left 
					case 300: // 5 minutes left
					case 60: // 1 minute left
						if (_joining)
						{
							removeOfflinePlayers();
							AnnounceToPlayers(true, "CTF Event: " + seconds / 60 + " minute(s) until registration ends! Use .joinctf command to register.");
						}
						else if (_started)
							AnnounceToPlayers(false, "CTF Event: " + seconds / 60 + " minute(s) until event ends!");
						
						break;

					case 5: // 5 seconds left

						if (_joining)
							AnnounceToPlayers(true, "CTF Event: " + seconds + " second(s) until registration ends! Use .joinctf command to register.");
						else if (_teleport)
							AnnounceToPlayers(false, "CTF Event: " + seconds + " seconds(s) until ends starts!");
						else if (_started)
							AnnounceToPlayers(false, "CTF Event: " + seconds + " second(s) until event ends!");
						
						break;
				}
			}
			
			long startOneSecondWaiterStartTime = System.currentTimeMillis();
			
			// only the try catch with Thread.sleep(1000) give bad countdown on high wait times
			while (startOneSecondWaiterStartTime + 1000 > System.currentTimeMillis())
			{
				try
				{
					Thread.sleep(1);
				}
				catch (InterruptedException ie)
				{
				}
			}
		}
	}
	
	private static boolean startEventOk()
	{
		if (_joining || !_teleport || _started)
			return false;
		
		if (Config.CTF_EVEN_TEAMS.equals("NO") || Config.CTF_EVEN_TEAMS.equals("BALANCE"))
		{
			if (_teamPlayersCount.contains(0))
				return false;
		}
		else if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE"))
		{
			Vector<L2PcInstance> playersShuffleTemp = new Vector<L2PcInstance>();
			int loopCount = 0;
			
			loopCount = _playersShuffle.size();
			
			for (int i = 0; i < loopCount; i++)
			{
				if (_playersShuffle != null)
					playersShuffleTemp.add(_playersShuffle.get(i));
			}
			
			_playersShuffle = playersShuffleTemp;
			playersShuffleTemp.clear();
			
			//  if (_playersShuffle.size() < (_teams.size()*2)){
			//	  return false;
			//  }
		}
		
		return true;
	}
	
	public static void shuffleTeams()
	{
		int teamCount = 0, playersCount = 0;
		
		for (;;)
		{
			if (_playersShuffle.isEmpty())
				break;
			
			int playerToAddIndex = Rnd.nextInt(_playersShuffle.size());
			L2PcInstance player = null;
			player = _playersShuffle.get(playerToAddIndex);
			player._originalNameColorCTF = player.getAppearance().getNameColor();
			player._originalKarmaCTF = player.getKarma();
			
			_players.add(player);
			_players.get(playersCount)._teamNameCTF = _teams.get(teamCount);
			_savePlayers.add(_players.get(playersCount).getName());
			_savePlayerTeams.add(_teams.get(teamCount));
			playersCount++;
			
			if (teamCount == _teams.size() - 1)
				teamCount = 0;
			else
				teamCount++;
			
			_playersShuffle.remove(playerToAddIndex);
		}
	}
	
	public static void setUserData()
	{
		for (L2PcInstance player : _players)
		{
			if (_teams.indexOf(player._teamNameCTF) == 0)
			{
				player.setTeam(2);
				player.setKarma(0);
				player.broadcastUserInfo();				
			}
			else
				player.setTeam(_teams.indexOf(player._teamNameCTF));
				player.setKarma(0);
				player.broadcastUserInfo();				
		}
	}
	
	public static void finishEvent()
	{
		if (!finishEventOk())
		{
			if (Config.DEBUG)
				_log.fine("CTF Engine[finishEvent]: finishEventOk() = false");
			return;
		}
		
		_started = false;
		unspawnEventNpc();
		unspawnAllFlags();
		processTopTeam();
		
		if (_topScore != 0)
			playKneelAnimation(_topTeam);
		
		if (Config.CTF_ANNOUNCE_TEAM_STATS)
		{
			AnnounceToPlayers(true, _eventName + " Team Statistics:");
			for (String team : _teams)
			{
				int _flags_ = teamFlagCount(team);
				AnnounceToPlayers(true, "Team: " + team + " - Flags taken: " + _flags_);
			}
		}
		
		teleportFinish();
	}
	
	//show loosers and winners animations
	public static void playKneelAnimation(String teamName)
	{
		for (L2PcInstance player : _players)
		{
			if (player != null && player.isOnline() && player._inEventCTF == true)
			{
				if (!player._teamNameCTF.equals(teamName))
				{
					player.broadcastPacket(new SocialAction(player.getObjectId(), 7));
				}
				else if (player._teamNameCTF.equals(teamName))
				{
					player.broadcastPacket(new SocialAction(player.getObjectId(), 3));
				}
			}
		}
	}
	
	private static boolean finishEventOk()
	{
		if (!_started)
			return false;
		
		return true;
	}
	
	/* Erro nas mensagens
	public static void rewardTeam(String teamName)
	{	        		
		for (L2PcInstance player : _players)
		{
			if (player != null)
			{
				if (player._teamNameCTF.equals(teamName))
				{
					player.addItem("CTF Event: " + _eventName, _rewardId, _rewardAmount, player, true);

					NpcHtmlMessage nhm = new NpcHtmlMessage(0);
					TextBuilder replyMSG = new TextBuilder();
					int count = 0;
					
					replyMSG.append("<html><body><center><font color=\"FF66OO\">PG-L][ Rare CTF Event</font><br>Your team won!<br></center>");
					replyMSG.append("<center>-= <font color=\"99CC00\">Best Flag Runners</font> =-</center><br>");
					replyMSG.append("<table width=\"200\" align=\"center\"><tr align=\"center\"><td>");
					
					for (String team : _teams)
					{
						replyMSG.append("<font color=\"LEVEL\">" + team + "</font>");
						
						for (L2PcInstance p : _players)
						{
								if (_playerScores.containsKey(p.getName()))
								{
									if (p._teamNameCTF.equals(team))
									{
										replyMSG.append("<br>" + ++count + ". " + p.getName() + " - " + _playerScores.get(p.getName()));
									}
								}
						}
						if (((_teams.indexOf(team) + 1) % 2) != 0)
						{
							if (team == _teams.lastElement())
								replyMSG.append("</td></tr></table></body></html>");
							else 
								replyMSG.append("</td><td>");
						}
						else
						{
							if (team == _teams.lastElement())
								replyMSG.append("</td></tr></table></body></html>");
							else
								replyMSG.append("</td></tr><tr><td>");
						}
						count = 0;
					}

					nhm.setHtml(replyMSG.toString());
					player.sendPacket(nhm);

					// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
				else
				{
					NpcHtmlMessage nhm = new NpcHtmlMessage(0);
					TextBuilder replyMSG = new TextBuilder();
					int count = 0;

					replyMSG.append("<html><body><center><font color=\"FF66OO\">PG-L][ Rare CTF Event</font><br>Better luck next time!<br></center>");
					replyMSG.append("<center>-= <font color=\"99CC00\">Best Flag Runners</font> =-</center><br>");
					replyMSG.append("<table width=\"200\" align=\"center\"><center><tr align=\"center\"><td>");
					
					for (String team : _teams)
					{
						replyMSG.append("<font color=\"LEVEL\">" + team + "</font>");
						
						for (L2PcInstance p : _players)
						{
								if (_playerScores.containsKey(p.getName()))
								{
									if (p._teamNameCTF.equals(team))
									{
										replyMSG.append("<br>" + ++count + ". " + p.getName() + " - " + _playerScores.get(p.getName()));
									}
								}
						}
						if (((_teams.indexOf(team) + 1) % 2) != 0)
						{
							if (team == _teams.lastElement())
								replyMSG.append("</td></tr></table></body></html>");
							else 
								replyMSG.append("</td><td>");
						}
						else
						{
							if (team == _teams.lastElement())
								replyMSG.append("</td></tr></table></body></html>");
							else
								replyMSG.append("</td></tr><tr><td>");
						}
						count = 0;
					}

					nhm.setHtml(replyMSG.toString());
					player.sendPacket(nhm);

					// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
			}
		}
	}
	*/

	public static void rewardTeam(String teamName)
	{
		for (L2PcInstance player : _players)
		{
			if (player != null)
			{
				if (player._teamNameCTF.equals(teamName))
				{
					player.addItem("CTF Event: " + _eventName, _rewardId, _rewardAmount, player, true);
					
					NpcHtmlMessage nhm = new NpcHtmlMessage(5);
					TextBuilder replyMSG = new TextBuilder();
					
					replyMSG.append("<html><body>Your team wins the event. Look in your inventory for the reward.</body></html>");
					
					nhm.setHtml(replyMSG.toString());
					player.sendPacket(nhm);
					
					// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
			}
		}
	}
	
	public static void abortEvent()
	{
		if (!_joining && !_teleport && !_started)
		{
			GmListTable.broadcastMessageToGMs("Failed aborting CTF: No CTF instance has been started.");
			return;
		}
		
		if (_joining && !_teleport && !_started)
		{
			unspawnEventNpc();
			cleanCTF();
			_joining = false;
			AnnounceToPlayers(true, "CTF Event: Match aborted!");
			return;
		}
		_joining = false;
		_teleport = false;
		_started = false;
		unspawnEventNpc();
		unspawnAllFlags();
		teleportFinish();
	}
	
	
	public static void dumpData()
	{
		_log.warning("");
		_log.warning("");
		
		if (!_joining && !_teleport && !_started)
		{
			_log.warning("<<---------------------------------->>");
			_log.warning(">> CTF Engine infos dump (INACTIVE) <<");
			_log.warning("<<--^----^^-----^----^^------^^----->>");
		}
		else if (_joining && !_teleport && !_started)
		{
			_log.warning("<<--------------------------------->>");
			_log.warning(">> CTF Engine infos dump (JOINING) <<");
			_log.warning("<<--^----^^-----^----^^------^----->>");
		}
		else if (!_joining && _teleport && !_started)
		{
			_log.warning("<<---------------------------------->>");
			_log.warning(">> CTF Engine infos dump (TELEPORT) <<");
			_log.warning("<<--^----^^-----^----^^------^^----->>");
		}
		else if (!_joining && !_teleport && _started)
		{
			_log.warning("<<--------------------------------->>");
			_log.warning(">> CTF Engine infos dump (STARTED) <<");
			_log.warning("<<--^----^^-----^----^^------^----->>");
		}
		
		_log.warning("Name: " + _eventName);
		_log.warning("Desc: " + _eventDesc);
		_log.warning("Join location: " + _joiningLocationName);
		_log.warning("Min lvl: " + _minlvl);
		_log.warning("Max lvl: " + _maxlvl);
		_log.warning("");
		_log.warning("##########################");
		_log.warning("# _teams(Vector<String>) #");
		_log.warning("##########################");
		
		for (String team : _teams)
			_log.warning(team + " Flags Taken :" + _teamPointsCount.get(_teams.indexOf(team)));
		
		if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE"))
		{
			_log.warning("");
			_log.warning("#########################################");
			_log.warning("# _playersShuffle(Vector<L2PcInstance>) #");
			_log.warning("#########################################");
			
			for (L2PcInstance player : _playersShuffle)
			{
				if (player != null)
					_log.warning("Name: " + player.getName());
			}
		}
		
		_log.warning("");
		_log.warning("##################################");
		_log.warning("# _players(Vector<L2PcInstance>) #");
		_log.warning("##################################");
		
		for (L2PcInstance player : _players)
		{
			if (player != null)
				_log.warning("Name: " + player.getName() + "   Team: " + player._teamNameCTF + "  Flags :" + player._countCTFflags);
		}
		
		_log.warning("");
		_log.warning("#####################################################################");
		_log.warning("# _savePlayers(Vector<String>) and _savePlayerTeams(Vector<String>) #");
		_log.warning("#####################################################################");
		
		for (String player : _savePlayers)
			_log.warning("Name: " + player + "	Team: " + _savePlayerTeams.get(_savePlayers.indexOf(player)));
		
		_log.warning("");
		_log.warning("");
		_log.warning("**********==CTF==************");
		_log.warning("CTF._teamPointsCount:" + _teamPointsCount.toString());
		_log.warning("CTF._flagIds:" + _flagIds.toString());
		_log.warning("CTF._flagSpawns:" + _flagSpawns.toString());
		_log.warning("CTF._throneSpawns:" + _throneSpawns.toString());
		_log.warning("CTF._flagsTaken:" + _flagsTaken.toString());
		_log.warning("CTF._flagsX:" + _flagsX.toString());
		_log.warning("CTF._flagsY:" + _flagsY.toString());
		_log.warning("CTF._flagsZ:" + _flagsZ.toString());
		_log.warning("************EOF**************");
		_log.warning("");
	}
	
	public static void loadData()
	{
		_eventName = new String();
		_eventDesc = new String();
		_topTeam = new String();
		_joiningLocationName = new String();
		_teams = new Vector<String>();
		_savePlayers = new Vector<String>();
		_savePlayerTeams = new Vector<String>();
		_players = new Vector<L2PcInstance>();
		_playersShuffle = new Vector<L2PcInstance>();
		_teamPlayersCount = new Vector<Integer>();
		_teamPointsCount = new Vector<Integer>();
		_teamColors = new Vector<Integer>();
		_teamsX = new Vector<Integer>();
		_teamsY = new Vector<Integer>();
		_teamsZ = new Vector<Integer>();
		_teamsBaseX = new Vector<Integer>();
		_teamsBaseY = new Vector<Integer>();
		_teamsBaseZ = new Vector<Integer>();
		_playerScores = new FastMap<String, Integer>();
		
		_throneSpawns = new Vector<L2Spawn>();
		_flagSpawns = new Vector<L2Spawn>();
		_flagsTaken = new Vector<Boolean>();
		_flagIds = new Vector<Integer>();
		_flagsX = new Vector<Integer>();
		_flagsY = new Vector<Integer>();
		_flagsZ = new Vector<Integer>();
		_flagsNotRemoved = new Vector<Boolean>();
		
		_joining = false;
		_teleport = false;
		_started = false;
		_sitForced = false;
		_npcId = 0;
		_npcX = 0;
		_npcY = 0;
		_npcZ = 0;
		_npcHeading = 0;
		_rewardId = 0;
		_rewardAmount = 0;
		_topScore = 0;
		_minlvl = 0;
		_maxlvl = 0;
		_joinTime = 0;
		_eventTime = 0;
		_minPlayers = 0;
		_maxPlayers = 0;
		_flagHoldTime = 0;
		
		Connection con = null;
		try
		{
			PreparedStatement statement;
			ResultSet rs;
			
			con = L2DatabaseFactory.getInstance().getConnection();
			
			statement = con.prepareStatement("Select * from ctf");
			rs = statement.executeQuery();
			
			int teams = 0;
			
			while (rs.next())
			{
				_eventName = rs.getString("eventName");
				_eventDesc = rs.getString("eventDesc");
				_joiningLocationName = rs.getString("joiningLocation");
				_minlvl = rs.getInt("minlvl");
				_maxlvl = rs.getInt("maxlvl");
				_npcId = rs.getInt("npcId");
				_npcX = rs.getInt("npcX");
				_npcY = rs.getInt("npcY");
				_npcZ = rs.getInt("npcZ");
				_npcHeading = rs.getInt("npcHeading");
				_rewardId = rs.getInt("rewardId");
				_rewardAmount = rs.getInt("rewardAmount");
				teams = rs.getInt("teamsCount");
				_joinTime = rs.getInt("joinTime");
				_eventTime = rs.getInt("eventTime");
				_minPlayers = rs.getInt("minPlayers");
				_maxPlayers = rs.getInt("maxPlayers");
				_flagHoldTime = rs.getInt("flagHoldTime");
			}
			statement.close();
			
			int index = -1;
			if (teams > 0)
				index = 0;
			while (index < teams && index > -1)
			{
				statement = con.prepareStatement("Select * from ctf_teams where teamId = ?");
				statement.setInt(1, index);
				rs = statement.executeQuery();
				while (rs.next())
				{
					_teams.add(rs.getString("teamName"));
					_teamPlayersCount.add(0);
					_teamPointsCount.add(0);
					_teamColors.add(0);
					_teamsX.add(0);
					_teamsY.add(0);
					_teamsZ.add(0);
					_teamsX.set(index, rs.getInt("teamX"));
					_teamsY.set(index, rs.getInt("teamY"));
					_teamsZ.set(index, rs.getInt("teamZ"));
					_teamColors.set(index, rs.getInt("teamColor"));
					_flagsX.add(0);
					_flagsY.add(0);
					_flagsZ.add(0);
					_flagsX.set(index, rs.getInt("flagX"));
					_flagsY.set(index, rs.getInt("flagY"));
					_flagsZ.set(index, rs.getInt("flagZ"));
					if (Config.CTF_BASE_TELEPORT_FIRST)
					{
						_teamsBaseX.add(0);
						_teamsBaseY.add(0);
						_teamsBaseZ.add(0);
						_teamsBaseX.set(index, rs.getInt("teamBaseX"));
						_teamsBaseY.set(index, rs.getInt("teamBaseY"));
						_teamsBaseZ.set(index, rs.getInt("teamBaseZ"));
					}
					_flagSpawns.add(null);
					_flagIds.add(_FlagNPC);
					_flagsTaken.add(false);
					_flagsNotRemoved.add(false);
				}
				index++;
				statement.close();
			}
		}
		catch (Exception e)
		{
			_log.warning("Exception: CTF.loadData(): " + e.getMessage());
		}
		finally
		{
			try
			{
				if (con != null)
					con.close();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public static void saveData()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			
			statement = con.prepareStatement("Delete from ctf");
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("INSERT INTO ctf (eventName, eventDesc, joiningLocation, minlvl, maxlvl, npcId, npcX, npcY, npcZ, npcHeading, rewardId, rewardAmount, teamsCount, joinTime, eventTime, minPlayers, maxPlayers, flagHoldTime) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			statement.setString(1, _eventName);
			statement.setString(2, _eventDesc);
			statement.setString(3, _joiningLocationName);
			statement.setInt(4, _minlvl);
			statement.setInt(5, _maxlvl);
			statement.setInt(6, _npcId);
			statement.setInt(7, _npcX);
			statement.setInt(8, _npcY);
			statement.setInt(9, _npcZ);
			statement.setInt(10, _npcHeading);
			statement.setInt(11, _rewardId);
			statement.setInt(12, _rewardAmount);
			statement.setInt(13, _teams.size());
			statement.setInt(14, _joinTime);
			statement.setInt(15, _eventTime);
			statement.setInt(16, _minPlayers);
			statement.setInt(17, _maxPlayers);
			statement.setLong(18, _flagHoldTime);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("Delete from ctf_teams");
			statement.execute();
			statement.close();
			
			for (String teamName : _teams)
			{
				int index = _teams.indexOf(teamName);
				
				if (index == -1)
					return;
				if (Config.CTF_BASE_TELEPORT_FIRST)
				{
					statement = con.prepareStatement("INSERT INTO ctf_teams (teamId ,teamName, teamX, teamY, teamZ, teamColor, flagX, flagY, flagZ, teamBaseX, teamBaseY, teamBaseZ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
					statement.setInt(1, index);
					statement.setString(2, teamName);
					statement.setInt(3, _teamsX.get(index));
					statement.setInt(4, _teamsY.get(index));
					statement.setInt(5, _teamsZ.get(index));
					statement.setInt(6, _teamColors.get(index));
					statement.setInt(7, _flagsX.get(index));
					statement.setInt(8, _flagsY.get(index));
					statement.setInt(9, _flagsZ.get(index));
					statement.setInt(10, _teamsBaseX.get(index));
					statement.setInt(11, _teamsBaseY.get(index));
					statement.setInt(12, _teamsBaseZ.get(index));
					statement.execute();
					statement.close();
				}
				else
				{
					statement = con.prepareStatement("INSERT INTO ctf_teams (teamId ,teamName, teamX, teamY, teamZ, teamColor, flagX, flagY, flagZ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
					statement.setInt(1, index);
					statement.setString(2, teamName);
					statement.setInt(3, _teamsX.get(index));
					statement.setInt(4, _teamsY.get(index));
					statement.setInt(5, _teamsZ.get(index));
					statement.setInt(6, _teamColors.get(index));
					statement.setInt(7, _flagsX.get(index));
					statement.setInt(8, _flagsY.get(index));
					statement.setInt(9, _flagsZ.get(index));
					statement.execute();
					statement.close();
				}
			}
		}
		catch (Exception e)
		{
			_log.warning("Exception: CTF.saveData(): " + e.getMessage());
		}
		finally
		{
			try
			{
				if (con != null)
					con.close();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public static void showEventHtml(L2PcInstance eventPlayer, String objectId)
	{
		try
		{
			NpcHtmlMessage adminReply = new NpcHtmlMessage(0);
			TextBuilder replyMSG = new TextBuilder();
			
			replyMSG.append("<html><body>");
			replyMSG.append("CTF Match<br><br><br>");
			replyMSG.append("Current event...<br>");
			replyMSG.append("   ... description:&nbsp;<font color=\"00FF00\">" + _eventDesc + "</font><br>");
			if (Config.CTF_ANNOUNCE_REWARD)
				replyMSG.append("   ... reward: (" + _rewardAmount + ") " + ItemTable.getInstance().getTemplate(_rewardId).getName() + "<br>");
			
			if (!_started && !_joining)
				replyMSG.append("<center>Wait till the admin/gm start the participation.</center>");
			else if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE") && !checkMaxPlayers(_playersShuffle.size()))
			{
				if (!CTF._started)
				{
					replyMSG.append("<font color=\"FFFF00\">The event has reached its maximum capacity.</font><br>Keep checking, someone may crit and you can steal their spot.");
				}
			}
			else if (eventPlayer.isCursedWeaponEquipped() && !Config.CTF_JOIN_CURSED)
			{
				replyMSG.append("<font color=\"FFFF00\">You can't participate in this event with a cursed Weapon.</font><br>");
			}
			else if (!_started && _joining && eventPlayer.getLevel() >= _minlvl && eventPlayer.getLevel() <= _maxlvl)
			{
				if (_players.contains(eventPlayer) || checkShufflePlayers(eventPlayer))
				{
					if (Config.CTF_EVEN_TEAMS.equals("NO") || Config.CTF_EVEN_TEAMS.equals("BALANCE"))
						replyMSG.append("You are already participating in team <font color=\"LEVEL\">" + eventPlayer._teamNameCTF + "</font><br><br>");
					else if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE"))
						replyMSG.append("You are already participating!<br><br>");
					
					replyMSG.append("<table border=\"0\"><tr>");
					replyMSG.append("<td width=\"200\">Wait till event start or</td>");
					replyMSG.append("<td width=\"60\"><center><button value=\"remove\" action=\"bypass -h npc_" + objectId + "_ctf_player_leave\" width=50 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center></td>");
					replyMSG.append("<td width=\"100\">your participation!</td>");
					replyMSG.append("</tr></table>");
				}
				else
				{
					replyMSG.append("<td width=\"200\">Your level : <font color=\"00FF00\">" + eventPlayer.getLevel() + "</font></td><br>");
					replyMSG.append("<td width=\"200\">Min level : <font color=\"00FF00\">" + _minlvl + "</font></td><br>");
					replyMSG.append("<td width=\"200\">Max level : <font color=\"00FF00\">" + _maxlvl + "</font></td><br><br>");
					
					if (Config.CTF_EVEN_TEAMS.equals("NO") || Config.CTF_EVEN_TEAMS.equals("BALANCE"))
					{
						replyMSG.append("<center><table border=\"0\">");
						
						for (String team : _teams)
						{
							replyMSG.append("<tr><td width=\"100\"><font color=\"LEVEL\">" + team + "</font>&nbsp;(" + teamPlayersCount(team) + " joined)</td>");
							replyMSG.append("<td width=\"60\"><button value=\"Join\" action=\"bypass -h npc_" + objectId + "_ctf_player_join " + team + "\" width=50 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
						}
						
						replyMSG.append("</table></center>");
					}
					else if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE"))
					{
						replyMSG.append("<center><table border=\"0\">");
						
						for (String team : _teams)
							replyMSG.append("<tr><td width=\"100\"><font color=\"LEVEL\">" + team + "</font></td>");
						
						replyMSG.append("</table></center><br>");
						
						replyMSG.append("<button value=\"Join\" action=\"bypass -h npc_" + objectId + "_ctf_player_join eventShuffle\" width=50 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
						replyMSG.append("Teams will be randomly generated!");
					}
				}
			}
			else if (_started && !_joining)
				replyMSG.append("<center>CTF match is in progress.</center>");
			else if (eventPlayer.getLevel() < _minlvl || eventPlayer.getLevel() > _maxlvl)
			{
				replyMSG.append("Your lvl : <font color=\"00FF00\">" + eventPlayer.getLevel() + "</font><br>");
				replyMSG.append("Min level : <font color=\"00FF00\">" + _minlvl + "</font><br>");
				replyMSG.append("Max level : <font color=\"00FF00\">" + _maxlvl + "</font><br><br>");
				replyMSG.append("<font color=\"FFFF00\">You can't participatein this event.</font><br>");
			}
			// Show how many players joined & how many are still needed to join
			replyMSG.append("<br>There are " + _playersShuffle.size() + " player(s) participating in this event.<br>");
			if (_joining)
			{
				if (_playersShuffle.size() < _minPlayers)
				{
					int playersNeeded = _minPlayers - _playersShuffle.size();
					replyMSG.append("The event will not start unless " + playersNeeded + " more player(s) joins!");
				}
			}
			
			replyMSG.append("</body></html>");
			adminReply.setHtml(replyMSG.toString());
			eventPlayer.sendPacket(adminReply);
			
			// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
			eventPlayer.sendPacket(ActionFailed.STATIC_PACKET);
		}
		catch (Exception e)
		{
			_log.warning("CTF Engine[showEventHtlm(" + eventPlayer.getName() + ", " + objectId + ")]: exception" + e.getMessage());
		}
	}
	
	public static void addPlayer(L2PcInstance player, String teamName)
	{
		if (!addPlayerOk(teamName, player))
			return;
		
		if (Config.CTF_EVEN_TEAMS.equals("NO") || Config.CTF_EVEN_TEAMS.equals("BALANCE"))
		{
			player._teamNameCTF = teamName;
			_players.add(player);
			setTeamPlayersCount(teamName, teamPlayersCount(teamName) + 1);
		}
		else if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE"))
			_playersShuffle.add(player);
		
		player._inEventCTF = true;
		player._countCTFflags = 0;
	}
	
	public static synchronized void removeOfflinePlayers()
	{
		try
		{
			if (_playersShuffle == null)
				return;
			else if (_playersShuffle.isEmpty())
				return;
			else if (_playersShuffle.size() > 0)
			{
				for (L2PcInstance player : _playersShuffle)
				{
					if (player == null)
						_playersShuffle.remove(player);
					else if (!player.isOnline() || player.isInJail())
						removePlayer(player);
					if (_playersShuffle.size() == 0 || _playersShuffle.isEmpty())
						break;
				}
			}
		}
		catch (Exception e)
		{
			_log.warning("CTF Engine exception: " + e.getMessage());
			return;
		}
	}
	
	public static boolean checkShufflePlayers(L2PcInstance eventPlayer)
	{
		try
		{
			for (L2PcInstance player : _playersShuffle)
			{
				if (player == null || !player.isOnline())
				{
					_playersShuffle.remove(player);
					eventPlayer._inEventCTF = false;
					continue;
				}
				else if (player.getObjectId() == eventPlayer.getObjectId())
				{
					eventPlayer._inEventCTF = true;
					eventPlayer._countCTFflags = 0;
					return true;
				}
				//this 1 is incase player got new objectid after DC or reconnect
				else if (player.getName().equals(eventPlayer.getName()))
				{
					_playersShuffle.remove(player);
					_playersShuffle.add(eventPlayer);
					eventPlayer._inEventCTF = true;
					eventPlayer._countCTFflags = 0;
					return true;
				}
			}
		}
		catch (Exception e)
		{
		}
		return false;
	}
	
	public static boolean addPlayerOk(String teamName, L2PcInstance eventPlayer)
	{
		try
		{
			if (checkShufflePlayers(eventPlayer) || eventPlayer._inEventCTF)
			{
				eventPlayer.sendMessage("You are already participating in the event!");
				return false;
			}
			
			for (L2PcInstance player : _players)
			{
				if (player.getObjectId() == eventPlayer.getObjectId())
				{
					eventPlayer.sendMessage("You are already participating in the event!");
					return false;
				}
				else if (player.getName() == eventPlayer.getName())
				{
					eventPlayer.sendMessage("You are already participating in the event!");
					return false;
				}
			}
			if (_players.contains(eventPlayer))
			{
				eventPlayer.sendMessage("You are already participating in the event!");
				return false;
			}
		}
		catch (Exception e)
		{
			_log.warning("CTF Siege Engine exception: " + e.getMessage());
		}
		
		if (Config.CTF_EVEN_TEAMS.equals("NO"))
			return true;
		else if (Config.CTF_EVEN_TEAMS.equals("BALANCE"))
		{
			boolean allTeamsEqual = true;
			int countBefore = -1;
			
			for (int playersCount : _teamPlayersCount)
			{
				if (countBefore == -1)
					countBefore = playersCount;
				
				if (countBefore != playersCount)
				{
					allTeamsEqual = false;
					break;
				}
				
				countBefore = playersCount;
			}
			
			if (allTeamsEqual)
				return true;
			
			countBefore = Integer.MAX_VALUE;
			
			for (int teamPlayerCount : _teamPlayersCount)
			{
				if (teamPlayerCount < countBefore)
					countBefore = teamPlayerCount;
			}
			
			Vector<String> joinableTeams = new Vector<String>();
			
			for (String team : _teams)
			{
				if (teamPlayersCount(team) == countBefore)
					joinableTeams.add(team);
			}
			
			if (joinableTeams.contains(teamName))
				return true;
		}
		else if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE"))
			return true;
		
		eventPlayer.sendMessage("Too many players in team \"" + teamName + "\"");
		return false;
	}
	
	public static synchronized void addDisconnectedPlayer(L2PcInstance player)
	{
		/*
		 * !!! CAUTION !!!
		 * Do NOT fix multiple object Ids on this event or you will ruin the flag reposition check!!!
		 * All Multiple object Ids will be collected by the Garbage Collector, after the event ends, memory sweep is made!!!
		 */

		if ((Config.CTF_EVEN_TEAMS.equals("SHUFFLE") && (_teleport || _started)) || (Config.CTF_EVEN_TEAMS.equals("NO") || Config.CTF_EVEN_TEAMS.equals("BALANCE") && (_teleport || _started)))
		{
			if (Config.CTF_ON_START_REMOVE_ALL_EFFECTS)
			{
				for (L2Effect e : player.getAllEffects())
				{
					if (e != null)
						e.exit();
				}
			}
			
			player._teamNameCTF = _savePlayerTeams.get(_savePlayers.indexOf(player.getName()));
			for (L2PcInstance p : _players)
			{
				if (p == null)
				{
					continue;
				}
				//check by name incase player got new objectId
				else if (p.getName().equals(player.getName()))
				{
					player._originalNameColorCTF = player.getAppearance().getNameColor();
					player._originalKarmaCTF = player.getKarma();
					player._inEventCTF = true;
					player._countCTFflags = p._countCTFflags;
					_players.remove(p); //removing old object id from vector
					_players.add(player); //adding new objectId to vector
					break;
				}
			}
			player.setTeam(_teams.indexOf(player._teamNameCTF));
			player.setKarma(0);
			player.broadcastUserInfo();
			if (Config.CTF_BASE_TELEPORT_FIRST)
			{
				player.sendMessage("We missed you! You will be sent back into battle in 10 seconds!");
				
				player.teleToLocation(_teamsBaseX.get(_teams.indexOf(player._teamNameCTF)), _teamsBaseY.get(_teams.indexOf(player._teamNameCTF)), _teamsBaseZ.get(_teams.indexOf(player._teamNameCTF)));
				
				ThreadPoolManager.getInstance().scheduleGeneral(new BaseTeleportTask(player, false), 10000);
			}
			else
			{
				player.teleToLocation(_teamsX.get(_teams.indexOf(player._teamNameCTF)), _teamsY.get(_teams.indexOf(player._teamNameCTF)), _teamsZ.get(_teams.indexOf(player._teamNameCTF)));
			}
			Started(player);
			CheckRestoreFlags();
		}
	}
	
	public static void removePlayer(L2PcInstance player)
	{
		if (player._inEventCTF)
		{
			if (!_joining)
			{
				player.getAppearance().setNameColor(player._originalNameColorCTF);
				player.setKarma(player._originalKarmaCTF);
				player.broadcastUserInfo();
			}
			player._teamNameCTF = new String();
			player._countCTFflags = 0;
			player._inEventCTF = false;
			
			if ((Config.CTF_EVEN_TEAMS.equals("NO") || Config.CTF_EVEN_TEAMS.equals("BALANCE")) && _players.contains(player))
			{
				setTeamPlayersCount(player._teamNameCTF, teamPlayersCount(player._teamNameCTF) - 1);
				_players.remove(player);
			}
			else if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE") && (!_playersShuffle.isEmpty() && _playersShuffle.contains(player)))
				_playersShuffle.remove(player);
		}
	}
	
	public static void cleanCTF()
	{
		_log.warning("CTF : Cleaning players.");
		for (L2PcInstance player : _players)
		{
			if (player != null)
			{
				if (player._haveFlagCTF)
					removeFlagFromPlayer(player);
				else
					player.getInventory().destroyItemByItemId("", CTF._FLAG_IN_HAND_ITEM_ID, 1, player, null);
				player._haveFlagCTF = false;
				removePlayer(player);
				if (_savePlayers.contains(player.getName()))
					_savePlayers.remove(player.getName());
				player._inEventCTF = false;
			}
		}
		if (_playersShuffle != null && !_playersShuffle.isEmpty())
		{
			for (L2PcInstance player : _playersShuffle)
			{
				if (player != null)
					player._inEventCTF = false;
			}
		}
		_log.warning("CTF : Cleaning teams and flags.");
		for (String team : _teams)
		{
			int index = _teams.indexOf(team);
			_teamPointsCount.set(index, 0);
			_flagSpawns.set(index, null);
			_flagsTaken.set(index, false);
			_teamPlayersCount.set(index, 0);
			_teamPointsCount.set(index, 0);
			_flagsNotRemoved.set(index, false);
		}
		_topScore = 0;
		_topTeam = new String();
		_players = new Vector<L2PcInstance>();
		_playersShuffle = new Vector<L2PcInstance>();
		_savePlayers = new Vector<String>();
		_savePlayerTeams = new Vector<String>();
		_teamPointsCount = new Vector<Integer>();
		_flagSpawns = new Vector<L2Spawn>();
		_flagsTaken = new Vector<Boolean>();
		_teamPlayersCount = new Vector<Integer>();
		_flagsNotRemoved = new Vector<Boolean>();
		_playerScores = new FastMap<String, Integer>();
		_log.warning("Cleaning CTF done.");
		_log.warning("Loading new data from MySql");
		loadData();
	}
	
	public static void unspawnEventNpc()
	{
		if (_npcSpawn == null)
			return;
		
		_npcSpawn.getLastSpawn().deleteMe();
		_npcSpawn.stopRespawn();
		SpawnTable.getInstance().deleteSpawn(_npcSpawn, true);
	}
	
	public static void teleportFinish()
	{
		AnnounceToPlayers(false, "CTF Event: Teleport back to participation NPC in 15 seconds!");
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				for (L2PcInstance player : _players)
				{
					if (player != null)
					{
						if (player.isOnline())
						{	
							player.setTeam(0);
							player.broadcastUserInfo();
							player.teleToLocation(_npcX, _npcY, _npcZ, false);
						}
							
						else
						{
							Connection con = null;
							try
							{
								con = L2DatabaseFactory.getInstance().getConnection();
								
								PreparedStatement statement = con.prepareStatement("UPDATE characters SET x=?, y=?, z=? WHERE char_name=?");
								statement.setInt(1, _npcX);
								statement.setInt(2, _npcY);
								statement.setInt(3, _npcZ);
								statement.setString(4, player.getName());
								statement.execute();
								statement.close();
							}
							catch (SQLException se)
							{
								_log.warning("CTF Engine exception: " + se.getMessage());
							}
							finally
							{
								try
								{
									if (con != null)
										con.close();
								}
								catch (SQLException e)
								{
									e.printStackTrace();
								}
							}
						}
					}
				}
				cleanCTF();
			}
		}, 15000);
	}
	
	public static int teamFlagCount(String teamName)
	{
		int index = _teams.indexOf(teamName);
		
		if (index == -1)
			return -1;
		
		return _teamPointsCount.get(index);
	}
	
	public static void setTeamFlagCount(String teamName, int teamFlagCount)
	{
		int index = _teams.indexOf(teamName);
		
		if (index == -1)
			return;
		
		_teamPointsCount.set(index, teamFlagCount);
	}
	
	/**
	 * Used to calculate the event CTF area, so that players don't run off with the flag.
	 * Essential, since a player may take the flag just so other teams can't score points.
	 * This function is Only called upon ONE time on BEGINING OF EACH EVENT right after we spawn the flags.
	 */
	private static void calculateOutSideOfCTF()
	{
		if (_teams == null || _flagSpawns == null || _teamsX == null || _teamsY == null || _teamsZ == null)
			return;
		
		int division = _teams.size() * 2, pos = 0;
		int[] locX = new int[division], locY = new int[division], locZ = new int[division];
		//Get all coordinates inorder to create a polygon:
		for (L2Spawn flag : _flagSpawns)
		{
			locX[pos] = flag.getLocx();
			locY[pos] = flag.getLocy();
			locZ[pos] = flag.getLocz();
			pos++;
			if (pos > division / 2)
				break;
		}
		
		for (int x = 0; x < _teams.size(); x++)
		{
			locX[pos] = _teamsX.get(x);
			locY[pos] = _teamsY.get(x);
			locZ[pos] = _teamsZ.get(x);
			pos++;
			if (pos > division)
				break;
		}
		
		//find the polygon center, note that it's not the mathematical center of the polygon, 
		//rather than a point which centers all coordinates:
		int centerX = 0, centerY = 0, centerZ = 0;
		for (int x = 0; x < pos; x++)
		{
			centerX += (locX[x] / division);
			centerY += (locY[x] / division);
			centerZ += (locZ[x] / division);
		}
		
		//now let's find the furthest distance from the "center" to the egg shaped sphere 
		//surrounding the polygon, size x1.5 (for maximum logical area to wander...):
		int maxX = 0, maxY = 0, maxZ = 0;
		for (int x = 0; x < pos; x++)
		{
			if (maxX < 2 * Math.abs(centerX - locX[x]))
				maxX = (2 * Math.abs(centerX - locX[x]));
			if (maxY < 2 * Math.abs(centerY - locY[x]))
				maxY = (2 * Math.abs(centerY - locY[x]));
			if (maxZ < 2 * Math.abs(centerZ - locZ[x]))
				maxZ = (2 * Math.abs(centerZ - locZ[x]));
		}
		
		//centerX,centerY,centerZ are the coordinates of the "event center".
		//so let's save those coordinates to check on the players:
		eventCenterX = centerX;
		eventCenterY = centerY;
		eventCenterZ = centerZ;
		eventOffset = maxX;
		if (eventOffset < maxY)
			eventOffset = maxY;
		if (eventOffset < maxZ)
			eventOffset = maxZ;
	}
	
	/**
	 * Called on every potion use
	 * @param playerObjectId
	 * @param player 
	 * @return boolean: true if player is allowed to use potions, otherwise false
	 */
	public static boolean onPotionUse(int playerObjectId, L2PcInstance player)
	{
		if (!_started)
			return true;
		
		//if (CTF.startEventOk() && !Config.CTF_ALLOW_POTIONS)
		if ((player != null && player.isOnline() && player._inEventCTF == true) && !Config.CTF_ALLOW_POTIONS)
			return false;
		return true;
	}
	
	public static boolean isOutsideCTFArea(L2PcInstance _player)
	{
		if (_player == null || !_player.isOnline())
			return true;
		if (!(_player.getX() > eventCenterX - eventOffset && _player.getX() < eventCenterX + eventOffset && _player.getY() > eventCenterY - eventOffset && _player.getY() < eventCenterY + eventOffset && _player.getZ() > eventCenterZ - eventOffset && _player.getZ() < eventCenterZ + eventOffset))
			return true;
		return false;
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final CTF _instance = new CTF();
	}	
}