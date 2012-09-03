package events.TvT;

import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import l2rt.Config;
import l2rt.common.ThreadPoolManager;
import l2rt.extensions.listeners.L2ZoneEnterLeaveListener;
import l2rt.extensions.multilang.CustomMessage;
import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.Announcements;
import l2rt.gameserver.network.clientpackets.Say2C;
import l2rt.gameserver.instancemanager.CastleManager;
import l2rt.gameserver.instancemanager.ServerVariables;
import l2rt.gameserver.instancemanager.ZoneManager;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Object;
import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.L2Territory;
import l2rt.gameserver.model.L2Zone;
import l2rt.gameserver.model.L2Zone.ZoneType;
import l2rt.gameserver.model.entity.olympiad.Olympiad;
import l2rt.gameserver.model.entity.residence.Castle;
import l2rt.gameserver.model.entity.siege.territory.TerritorySiege;
import l2rt.gameserver.network.serverpackets.Revive;
import l2rt.gameserver.tables.DoorTable;
import l2rt.gameserver.tables.SkillTable;
import l2rt.util.Files;
import l2rt.util.GArray;
import l2rt.util.GCSArray;
import l2rt.util.Location;
import l2rt.util.Rnd;
import l2rt.util.Util;

public class TvT extends Functions implements ScriptFile
{
	private static Logger _log = Logger.getLogger(TvT.class.getName());

	public class StartTask implements Runnable
	{
		@Override
		public void run()
		{
			if(!_active)
				return;

			if(isPvPEventStarted())
			{
				_log.info("TvT not started: another event is already running");
				return;
			}

			if(!Rnd.chance(Config.EVENT_TvTChanceToStart))
			{
				_log.fine("TvT not started: chance");
				return;
			}

			if(TerritorySiege.isInProgress())
			{
				_log.fine("TvT not started: TerritorySiege in progress");
				return;
			}

			for(Castle c : CastleManager.getInstance().getCastles().values())
				if(c.getSiege() != null && c.getSiege().isInProgress())
				{
					_log.fine("TvT not started: CastleSiege in progress");
					return;
				}

			start(new String[] { "1", "1" });
		}
	}

	private static ScheduledFuture<?> _startTask;

	private static GCSArray<Long> players_list1 = new GCSArray<Long>();
	private static GCSArray<Long> players_list2 = new GCSArray<Long>();
	private static GCSArray<Long> live_list1 = new GCSArray<Long>();
	private static GCSArray<Long> live_list2 = new GCSArray<Long>();

	private static boolean _isRegistrationActive = false;
	private static int _status = 0;
	private static int _time_to_start;
	private static int _category;
	private static int _minLevel;
	private static int _maxLevel;
	private static int _autoContinue = 0;

	private static ScheduledFuture<?> _endTask;

	private static L2Zone _zone = ZoneManager.getInstance().getZoneByIndex(ZoneType.battle_zone, 4, true);
	ZoneListener _zoneListener = new ZoneListener();

	private static L2Territory team1loc = new L2Territory(11000001);
	private static L2Territory team2loc = new L2Territory(11000002);

	@Override
	public void onLoad()
	{
		_zone.getListenerEngine().addMethodInvokedListener(_zoneListener);

		team1loc.add(149878, 47505, -3408, -3308);
		team1loc.add(150262, 47513, -3408, -3308);
		team1loc.add(150502, 47233, -3408, -3308);
		team1loc.add(150507, 46300, -3408, -3308);
		team1loc.add(150256, 46002, -3408, -3308);
		team1loc.add(149903, 46005, -3408, -3308);

		team2loc.add(149027, 46005, -3408, -3308);
		team2loc.add(148686, 46003, -3408, -3308);
		team2loc.add(148448, 46302, -3408, -3308);
		team2loc.add(148449, 47231, -3408, -3308);
		team2loc.add(148712, 47516, -3408, -3308);
		team2loc.add(149014, 47527, -3408, -3308);

		_startTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new StartTask(), 3600000, 3600000);

		_active = ServerVariables.getString("TvT", "off").equalsIgnoreCase("on");

		_log.fine("Loaded Event: TvT");
	}

	@Override
	public void onReload()
	{
		_zone.getListenerEngine().removeMethodInvokedListener(_zoneListener);
		_startTask.cancel(true);
	}

	@Override
	public void onShutdown()
	{
		onReload();
	}

	private static boolean _active = false;

	private static boolean isActive()
	{
		return _active;
	}

	public void activateEvent()
	{
		L2Player player = (L2Player) getSelf();
		if(!player.getPlayerAccess().IsEventGm)
			return;

		if(!isActive())
		{
			if(_startTask == null)
				_startTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new StartTask(), 3600000, 3600000);
			ServerVariables.set("TvT", "on");
			_log.info("Event 'TvT' activated.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.TvT.AnnounceEventStarted", null);
		}
		else
			player.sendMessage("Event 'TvT' already active.");

		_active = true;

		show(Files.read("data/html/admin/events.htm", player), player);
	}

	public void deactivateEvent()
	{
		L2Player player = (L2Player) getSelf();
		if(!player.getPlayerAccess().IsEventGm)
			return;

		if(isActive())
		{
			if(_startTask != null)
			{
				_startTask.cancel(true);
				_startTask = null;
			}
			ServerVariables.unset("TvT");
			_log.info("Event 'TvT' deactivated.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.TvT.AnnounceEventStoped", null);
		}
		else
			player.sendMessage("Event 'TvT' not active.");

		_active = false;

		show(Files.read("data/html/admin/events.htm", player), player);
	}

	public static boolean isRunned()
	{
		return _isRegistrationActive || _status > 0;
	}

	public String DialogAppend_31225(Integer val)
	{
		if(val == 0)
		{
			L2Player player = (L2Player) getSelf();
			return Files.read("data/scripts/events/TvT/31225.html", player);
		}
		return "";
	}

	public static int getMinLevelForCategory(int category)
	{
		switch(category)
		{
			case 1:
				return 20;
			case 2:
				return 30;
			case 3:
				return 40;
			case 4:
				return 52;
			case 5:
				return 62;
			case 6:
				return 76;
			case 7:
				return 85;
		}
		return 0;
	}

	public static int getMaxLevelForCategory(int category)
	{
		switch(category)
		{
			case 1:
				return 29;
			case 2:
				return 39;
			case 3:
				return 51;
			case 4:
				return 61;
			case 5:
				return 75;
			case 6:
				return 85;
			case 7:
				return 99;
		}
		return 0;
	}

	public static int getCategory(int level)
	{
		if(level >= 20 && level <= 29)
			return 1;
		else if(level >= 30 && level <= 39)
			return 2;
		else if(level >= 40 && level <= 51)
			return 3;
		else if(level >= 52 && level <= 61)
			return 4;
		else if(level >= 62 && level <= 75)
			return 5;
		else if(level >= 76  && level <= 85)
			return 6;
		else if(level >= 86)
			return 7;
		return 0;
	}

	public void start(String[] var)
	{
		L2Player player = (L2Player) getSelf();
		if(var.length != 2)
		{
			show(new CustomMessage("common.Error", player), player);
			return;
		}

		Integer category;
		Integer autoContinue;
		try
		{
			category = Integer.valueOf(var[0]);
			autoContinue = Integer.valueOf(var[1]);
		}
		catch(Exception e)
		{
			show(new CustomMessage("common.Error", player), player);
			return;
		}

		_category = category;
		_autoContinue = autoContinue;

		if(_category == -1)
		{
			_minLevel = 1;
			_maxLevel = 99;
		}
		else
		{
			_minLevel = getMinLevelForCategory(_category);
			_maxLevel = getMaxLevelForCategory(_category);
		}

		if(_endTask != null)
		{
			show(new CustomMessage("common.TryLater", player), player);
			return;
		}

		_status = 0;
		_isRegistrationActive = true;
		_time_to_start = Config.EVENT_TvTTime;

		players_list1 = new GCSArray<Long>();
		players_list2 = new GCSArray<Long>();
		live_list1 = new GCSArray<Long>();
		live_list2 = new GCSArray<Long>();

		String[] param = { String.valueOf(_time_to_start), String.valueOf(_minLevel), String.valueOf(_maxLevel) };
		sayToAll("scripts.events.TvT.AnnouncePreStart", param);

		executeTask("events.TvT.TvT", "question", new Object[0], 10000);
		executeTask("events.TvT.TvT", "announce", new Object[0], 60000);
	}

	public static void sayToAll(String address, String[] replacements)
	{
		Announcements.getInstance().announceByCustomMessage(address, replacements, Say2C.CRITICAL_ANNOUNCEMENT);
	}

	public static void question()
	{
		for(L2Player player : L2ObjectsStorage.getAllPlayersForIterate())
			if(player != null && player.getLevel() >= _minLevel && player.getLevel() <= _maxLevel && player.getReflection().getId() <= 0 && !player.isInOlympiadMode())
				player.scriptRequest(new CustomMessage("scripts.events.TvT.AskPlayer", player).toString(), "events.TvT.TvT:addPlayer", new Object[0]);
	}

	public static void announce()
	{
		if(players_list1.isEmpty() || players_list2.isEmpty())
		{
			sayToAll("scripts.events.TvT.AnnounceEventCancelled", null);
			_isRegistrationActive = false;
			_status = 0;
			executeTask("events.TvT.TvT", "autoContinue", new Object[0], 10000);
			return;
		}

		if(_time_to_start > 1)
		{
			_time_to_start--;
			String[] param = { String.valueOf(_time_to_start), String.valueOf(_minLevel), String.valueOf(_maxLevel) };
			sayToAll("scripts.events.TvT.AnnouncePreStart", param);
			executeTask("events.TvT.TvT", "announce", new Object[0], 60000);
		}
		else
		{
			_status = 1;
			_isRegistrationActive = false;
			sayToAll("scripts.events.TvT.AnnounceEventStarting", null);
			executeTask("events.TvT.TvT", "prepare", new Object[0], 5000);
		}
	}

	public void addPlayer()
	{
		L2Player player = (L2Player) getSelf();
		if(player == null || !checkPlayer(player, true))
			return;

		int team = 0, size1 = players_list1.size(), size2 = players_list2.size();

		if(size1 > size2)
			team = 2;
		else if(size1 < size2)
			team = 1;
		else
			team = Rnd.get(1, 2);

		if(team == 1)
		{
			players_list1.add(player.getStoredId());
			live_list1.add(player.getStoredId());
			show(new CustomMessage("scripts.events.TvT.Registered", player), player);
		}
		else if(team == 2)
		{
			players_list2.add(player.getStoredId());
			live_list2.add(player.getStoredId());
			show(new CustomMessage("scripts.events.TvT.Registered", player), player);
		}
		else
			_log.info("WTF??? Command id 0 in TvT...");
	}

	public static boolean checkPlayer(L2Player player, boolean first)
	{
		if(first && !_isRegistrationActive)
		{
			show(new CustomMessage("scripts.events.Late", player), player);
			return false;
		}

		if(first && (players_list1.contains(player.getStoredId()) || players_list2.contains(player.getStoredId())))
		{
			show(new CustomMessage("scripts.events.TvT.Cancelled", player), player);
			return false;
		}

		if(player.getLevel() < _minLevel || player.getLevel() > _maxLevel)
		{
			show(new CustomMessage("scripts.events.TvT.CancelledLevel", player), player);
			return false;
		}

		if(player.isMounted())
		{
			show(new CustomMessage("scripts.events.TvT.Cancelled", player), player);
			return false;
		}

		if(player.getDuel() != null)
		{
			show(new CustomMessage("scripts.events.TvT.CancelledDuel", player), player);
			return false;
		}

		if(player.getTeam() != 0)
		{
			show(new CustomMessage("scripts.events.TvT.CancelledOtherEvent", player), player);
			return false;
		}

		if(player.getOlympiadGameId() > 0 || player.isInZoneOlympiad() || first && Olympiad.isRegistered(player))
		{
			show(new CustomMessage("scripts.events.TvT.CancelledOlympiad", player), player);
			return false;
		}

		if(player.isTeleporting())
		{
			show(new CustomMessage("scripts.events.TvT.CancelledTeleport", player), player);
			return false;
		}

		return true;
	}

	public static void prepare()
	{
		DoorTable.getInstance().getDoor(24190002).closeMe();
		DoorTable.getInstance().getDoor(24190003).closeMe();

		cleanPlayers();
		clearArena();

		executeTask("events.TvT.TvT", "ressurectPlayers", new Object[0], 1000);
		executeTask("events.TvT.TvT", "healPlayers", new Object[0], 2000);
		executeTask("events.TvT.TvT", "saveBackCoords", new Object[0], 3000);
		executeTask("events.TvT.TvT", "paralyzePlayers", new Object[0], 4000);
		executeTask("events.TvT.TvT", "teleportPlayersToColiseum", new Object[0], 5000);
		executeTask("events.TvT.TvT", "go", new Object[0], 60000);

		sayToAll("scripts.events.TvT.AnnounceFinalCountdown", null);
	}

	public static void go()
	{
		_status = 2;
		upParalyzePlayers();
		checkLive();
		clearArena();
		sayToAll("scripts.events.TvT.AnnounceFight", null);
		_endTask = executeTask("events.TvT.TvT", "endBattle", new Object[0], 300000);
	}

	public static void endBattle()
	{
		DoorTable.getInstance().getDoor(24190002).openMe();
		DoorTable.getInstance().getDoor(24190003).openMe();

		_status = 0;
		removeAura();
		if(live_list1.isEmpty())
		{
			sayToAll("scripts.events.TvT.AnnounceFinishedBlueWins", null);
			giveItemsToWinner(false, true, 1);
		}
		else if(live_list2.isEmpty())
		{
			sayToAll("scripts.events.TvT.AnnounceFinishedRedWins", null);
			giveItemsToWinner(true, false, 1);
		}
		else if(live_list1.size() < live_list2.size())
		{
			sayToAll("scripts.events.TvT.AnnounceFinishedBlueWins", null);
			giveItemsToWinner(false, true, 1);
		}
		else if(live_list1.size() > live_list2.size())
		{
			sayToAll("scripts.events.TvT.AnnounceFinishedRedWins", null);
			giveItemsToWinner(true, false, 1);
		}
		else if(live_list1.size() == live_list2.size())
		{
			sayToAll("scripts.events.TvT.AnnounceFinishedDraw", null);
			giveItemsToWinner(true, true, 0.5);
		}

		sayToAll("scripts.events.TvT.AnnounceEnd", null);
		executeTask("events.TvT.TvT", "end", new Object[0], 30000);
		_isRegistrationActive = false;
		if(_endTask != null)
		{
			_endTask.cancel(false);
			_endTask = null;
		}
	}

	public static void end()
	{
		executeTask("events.TvT.TvT", "ressurectPlayers", new Object[0], 1000);
		executeTask("events.TvT.TvT", "healPlayers", new Object[0], 2000);
		executeTask("events.TvT.TvT", "teleportPlayersToSavedCoords", new Object[0], 3000);
		executeTask("events.TvT.TvT", "autoContinue", new Object[0], 10000);
	}

	public void autoContinue()
	{
		live_list1.clear();
		live_list2.clear();
		players_list1.clear();
		players_list2.clear();

		if(_autoContinue > 0)
		{
			if(_autoContinue >= 7)
			{
				_autoContinue = 0;
				return;
			}
			start(new String[] { "" + (_autoContinue + 1), "" + (_autoContinue + 1) });
		}
	}

	public static void giveItemsToWinner(boolean team1, boolean team2, double rate)
	{
		if(team1)
			for(L2Player player : getPlayers(players_list1))
				addItem(player, Config.EVENT_TvTItemID, Math.round((Config.EVENT_TvT_rate ? player.getLevel() : 1) * Config.EVENT_TvTItemCOUNT * rate));
		if(team2)
			for(L2Player player : getPlayers(players_list2))
				addItem(player, Config.EVENT_TvTItemID, Math.round((Config.EVENT_TvT_rate ? player.getLevel() : 1) * Config.EVENT_TvTItemCOUNT * rate));
	}

	public static void saveBackCoords()
	{
		for(L2Player player : getPlayers(players_list1))
			player.setVar("TvT_backCoords", player.getX() + " " + player.getY() + " " + player.getZ() + " " + player.getReflection().getId());
		for(L2Player player : getPlayers(players_list2))
			player.setVar("TvT_backCoords", player.getX() + " " + player.getY() + " " + player.getZ() + " " + player.getReflection().getId());
	}

	public static void teleportPlayersToColiseum()
	{
		for(L2Player player : getPlayers(players_list1))
		{
			unRide(player);
			unSummonPet(player, true);
			int[] pos = team1loc.getRandomPoint();
			player.teleToLocation(pos[0], pos[1], pos[2], 0);
		}
		for(L2Player player : getPlayers(players_list2))
		{
			unRide(player);
			unSummonPet(player, true);
			int[] pos = team2loc.getRandomPoint();
			player.teleToLocation(pos[0], pos[1], pos[2], 0);
		}
	}

	public static void teleportPlayersToSavedCoords()
	{
		for(L2Player player : getPlayers(players_list1))
			try
			{
				String var = player.getVar("TvT_backCoords");
				if(var == null || var.equals(""))
					continue;
				String[] coords = var.split(" ");
				if(coords.length != 4)
					continue;
				player.teleToLocation(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]), Long.parseLong(coords[3]));
				player.unsetVar("TvT_backCoords");
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		for(L2Player player : getPlayers(players_list2))
			try
			{
				String var = player.getVar("TvT_backCoords");
				if(var == null || var.equals(""))
					continue;
				String[] coords = var.split(" ");
				if(coords.length != 4)
					continue;
				player.teleToLocation(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]), Integer.parseInt(coords[3]));
				player.unsetVar("TvT_backCoords");
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
	}

	public static void paralyzePlayers()
	{
		L2Skill revengeSkill = SkillTable.getInstance().getInfo(L2Skill.SKILL_RAID_CURSE, 1);
		for(L2Player player : getPlayers(players_list1))
		{
			player.getEffectList().stopEffect(L2Skill.SKILL_MYSTIC_IMMUNITY);
			revengeSkill.getEffects(player, player, false, false);
			if(player.getPet() != null)
				revengeSkill.getEffects(player, player.getPet(), false, false);
		}
		for(L2Player player : getPlayers(players_list2))
		{
			player.getEffectList().stopEffect(L2Skill.SKILL_MYSTIC_IMMUNITY);
			revengeSkill.getEffects(player, player, false, false);
			if(player.getPet() != null)
				revengeSkill.getEffects(player, player.getPet(), false, false);
		}
	}

	public static void upParalyzePlayers()
	{
		for(L2Player player : getPlayers(players_list1))
		{
			player.getEffectList().stopEffect(L2Skill.SKILL_RAID_CURSE);
			if(player.getPet() != null)
				player.getPet().getEffectList().stopEffect(L2Skill.SKILL_RAID_CURSE);

			if(player.getParty() != null)
				player.getParty().oustPartyMember(player);
		}
		for(L2Player player : getPlayers(players_list2))
		{
			player.getEffectList().stopEffect(L2Skill.SKILL_RAID_CURSE);
			if(player.getPet() != null)
				player.getPet().getEffectList().stopEffect(L2Skill.SKILL_RAID_CURSE);

			if(player.getParty() != null)
				player.getParty().oustPartyMember(player);
		}
	}

	public static void ressurectPlayers()
	{
		for(L2Player player : getPlayers(players_list1))
			if(player.isDead())
			{
				player.restoreExp();
				player.setCurrentCp(player.getMaxCp());
				player.setCurrentHp(player.getMaxHp(), true);
				player.setCurrentMp(player.getMaxMp());
				player.broadcastPacket(new Revive(player));
			}
		for(L2Player player : getPlayers(players_list2))
			if(player.isDead())
			{
				player.restoreExp();
				player.setCurrentCp(player.getMaxCp());
				player.setCurrentHp(player.getMaxHp(), true);
				player.setCurrentMp(player.getMaxMp());
				player.broadcastPacket(new Revive(player));
			}
	}

	public static void healPlayers()
	{
		for(L2Player player : getPlayers(players_list1))
		{
			player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
			player.setCurrentCp(player.getMaxCp());
		}
		for(L2Player player : getPlayers(players_list2))
		{
			player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
			player.setCurrentCp(player.getMaxCp());
		}
	}

	public static void cleanPlayers()
	{
		for(L2Player player : getPlayers(players_list1))
			if(!checkPlayer(player, false))
				removePlayer(player);
		for(L2Player player : getPlayers(players_list2))
			if(!checkPlayer(player, false))
				removePlayer(player);
	}

	public static void checkLive()
	{
		GCSArray<Long> new_live_list1 = new GCSArray<Long>();
		GCSArray<Long> new_live_list2 = new GCSArray<Long>();

		for(Long storeId : live_list1)
		{
			L2Player player = L2ObjectsStorage.getAsPlayer(storeId);
			if(player != null)
				new_live_list1.add(storeId);
		}

		for(Long storeId : live_list2)
		{
			L2Player player = L2ObjectsStorage.getAsPlayer(storeId);
			if(player != null)
				new_live_list2.add(storeId);
		}

		live_list1 = new_live_list1;
		live_list2 = new_live_list2;

		for(L2Player player : getPlayers(live_list1))
			if(player.isInZone(_zone) && !player.isDead() && player.isConnected() && !player.isLogoutStarted())
				player.setTeam(2, true);
			else
				loosePlayer(player);

		for(L2Player player : getPlayers(live_list2))
			if(player.isInZone(_zone) && !player.isDead() && player.isConnected() && !player.isLogoutStarted())
				player.setTeam(1, true);
			else
				loosePlayer(player);

		if(live_list1.size() < 1 || live_list2.size() < 1)
			endBattle();
	}

	public static void removeAura()
	{
		for(L2Player player : getPlayers(live_list1))
			player.setTeam(0, true);
		for(L2Player player : getPlayers(live_list2))
			player.setTeam(0, true);
	}

	public static void clearArena()
	{
		for(L2Object obj : _zone.getObjects())
			if(obj != null)
			{
				L2Player player = obj.getPlayer();
				if(player != null && !live_list1.contains(player.getStoredId()) && !live_list2.contains(player.getStoredId()))
					player.teleToLocation(147451, 46728, -3410);
			}
	}

	public static void OnDie(L2Character self, L2Character killer)
	{
		if(_status > 1 && self != null && self.isPlayer() && self.getTeam() > 0 && (live_list1.contains(self.getStoredId()) || live_list2.contains(self.getStoredId())))
		{
			loosePlayer((L2Player) self);
			checkLive();
		}
	}

	public static Location OnEscape(L2Player player)
	{
		if(_status > 1 && player != null && player.getTeam() > 0 && (live_list1.contains(player.getStoredId()) || live_list2.contains(player.getStoredId())))
		{
			removePlayer(player);
			checkLive();
		}
		return null;
	}

	public static void OnPlayerExit(L2Player player)
	{
		if(player == null || player.getTeam() < 1)
			return;

		// Вышел или вылетел во время регистрации
		if(_status == 0 && _isRegistrationActive && player.getTeam() > 0 && (live_list1.contains(player.getStoredId()) || live_list2.contains(player.getStoredId())))
		{
			removePlayer(player);
			return;
		}

		// Вышел или вылетел во время телепортации
		if(_status == 1 && (live_list1.contains(player.getStoredId()) || live_list2.contains(player.getStoredId())))
		{
			removePlayer(player);

			try
			{
				String var = player.getVar("TvT_backCoords");
				if(var == null || var.equals(""))
					return;
				String[] coords = var.split(" ");
				if(coords.length != 4)
					return;
				player.teleToLocation(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]), Integer.parseInt(coords[3]));
				player.unsetVar("TvT_backCoords");
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}

			return;
		}

		// Вышел или вылетел во время эвента
		OnEscape(player);
	}

	private class ZoneListener extends L2ZoneEnterLeaveListener
	{
		@Override
		public void objectEntered(L2Zone zone, L2Object object)
		{
			if(object == null)
				return;
			L2Player player = object.getPlayer();
			if(_status > 0 && player != null && !live_list1.contains(player.getStoredId()) && !live_list2.contains(player.getStoredId()))
				ThreadPoolManager.getInstance().scheduleGeneral(new TeleportTask((L2Character) object, new Location(147451, 46728, -3410)), 3000);
		}

		@Override
		public void objectLeaved(L2Zone zone, L2Object object)
		{
			if(object == null)
				return;
			L2Player player = object.getPlayer();
			if(_status > 1 && player != null && player.getTeam() > 0 && (live_list1.contains(player.getStoredId()) || live_list2.contains(player.getStoredId())))
			{
				double angle = Util.convertHeadingToDegree(object.getHeading()); // угол в градусах
				double radian = Math.toRadians(angle - 90); // угол в радианах
				int x = (int) (object.getX() + 50 * Math.sin(radian));
				int y = (int) (object.getY() - 50 * Math.cos(radian));
				int z = object.getZ();
				ThreadPoolManager.getInstance().scheduleGeneral(new TeleportTask((L2Character) object, new Location(x, y, z)), 3000);
			}
		}
	}

	public class TeleportTask implements Runnable
	{
		Location loc;
		L2Character target;

		public TeleportTask(L2Character target, Location loc)
		{
			this.target = target;
			this.loc = loc;
			target.startStunning();
		}

		@Override
		public void run()
		{
			target.stopStunning();
			target.teleToLocation(loc);
		}
	}

	private static void loosePlayer(L2Player player)
	{
		if(player != null)
		{
			live_list1.remove(player.getStoredId());
			live_list2.remove(player.getStoredId());
			player.setTeam(0, true);
			show(new CustomMessage("scripts.events.TvT.YouLose", player), player);
		}
	}

	private static void removePlayer(L2Player player)
	{
		if(player != null)
		{
			live_list1.remove(player.getStoredId());
			live_list2.remove(player.getStoredId());
			players_list1.remove(player.getStoredId());
			players_list2.remove(player.getStoredId());
			player.setTeam(0, true);
		}
	}

	private static GArray<L2Player> getPlayers(GCSArray<Long> list)
	{
		GArray<L2Player> result = new GArray<L2Player>();
		for(Long storeId : list)
		{
			L2Player player = L2ObjectsStorage.getAsPlayer(storeId);
			if(player != null)
				result.add(player);
		}
		return result;
	}
}