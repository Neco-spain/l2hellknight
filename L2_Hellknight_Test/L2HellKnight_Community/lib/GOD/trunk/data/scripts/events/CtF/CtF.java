package events.CtF;

import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import l2rt.Config;
import l2rt.common.ThreadPoolManager;
import l2rt.extensions.listeners.L2ZoneEnterLeaveListener;
import l2rt.extensions.multilang.CustomMessage;
import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.Announcements;
import l2rt.gameserver.cache.Msg;
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
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.Revive;
import l2rt.gameserver.skills.EffectType;
import l2rt.gameserver.tables.DoorTable;
import l2rt.gameserver.tables.SkillTable;
import l2rt.gameserver.xml.ItemTemplates;
import l2rt.util.Files;
import l2rt.util.GArray;
import l2rt.util.GCSArray;
import l2rt.util.Location;
import l2rt.util.Rnd;
import l2rt.util.Util;

public class CtF extends Functions implements ScriptFile
{
	private static Logger _log = Logger.getLogger(CtF.class.getName());

	public class StartTask implements Runnable
	{
		public void run()
		{
			if(!_active)
				return;

			if(isPvPEventStarted())
			{
				_log.info("CtF not started: another event is already running");
				return;
			}

			if(!Rnd.chance(Config.EVENT_CtFChanceToStart))
			{
				_log.fine("CtF not started: chance");
				return;
			}

			if(TerritorySiege.isInProgress())
			{
				_log.fine("CtF not started: TerritorySiege in progress");
				return;
			}

			for(Castle c : CastleManager.getInstance().getCastles().values())
				if(c.getSiege() != null && c.getSiege().isInProgress())
				{
					_log.fine("CtF not started: CastleSiege in progress");
					return;
				}

			start(new String[] { "1", "1" });
		}
	}

	private static ScheduledFuture<?> _startTask;

	private static GCSArray<Long> players_list1 = new GCSArray<Long>();
	private static GCSArray<Long> players_list2 = new GCSArray<Long>();

	private static L2NpcInstance redFlag = null;
	private static L2NpcInstance blueFlag = null;

	private static boolean _isRegistrationActive = false;
	private static int _status = 0;
	private static int _time_to_start;
	private static int _category;
	private static int _minLevel;
	private static int _maxLevel;
	private static int _autoContinue = 0;

	private static ScheduledFuture<?> _endTask;

	private static L2Zone _zone = ZoneManager.getInstance().getZoneByIndex(ZoneType.battle_zone, 4, true);
	private static L2Zone _blueBaseZone = ZoneManager.getInstance().getZoneByIndex(ZoneType.battle_zone, 5, true);
	private static L2Zone _redBaseZone = ZoneManager.getInstance().getZoneByIndex(ZoneType.battle_zone, 6, true);

	ZoneListener _zoneListener = new ZoneListener();
	RedBaseZoneListener _redBaseZoneListener = new RedBaseZoneListener();
	BlueBaseZoneListener _blueBaseZoneListener = new BlueBaseZoneListener();

	private static L2Territory team1loc = new L2Territory(11000003);
	private static L2Territory team2loc = new L2Territory(11000004);

	private static Location blueFlagLoc = new Location(150760, 45848, -3408);
	private static Location redFlagLoc = new Location(148232, 47688, -3408);

	public void onLoad()
	{
		_zone.getListenerEngine().addMethodInvokedListener(_zoneListener);
		_blueBaseZone.getListenerEngine().addMethodInvokedListener(_blueBaseZoneListener);
		_redBaseZone.getListenerEngine().addMethodInvokedListener(_redBaseZoneListener);

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

		_active = ServerVariables.getString("CtF", "off").equalsIgnoreCase("on");

		_log.fine("Loaded Event: CtF");
	}

	public void onReload()
	{
		_zone.getListenerEngine().removeMethodInvokedListener(_zoneListener);
		_startTask.cancel(true);
	}

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
			ServerVariables.set("CtF", "on");
			_log.info("Event 'CtF' activated.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.CtF.AnnounceEventStarted", null);
		}
		else
			player.sendMessage("Event 'CtF' already active.");

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
			ServerVariables.unset("CtF");
			_log.info("Event 'CtF' deactivated.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.CtF.AnnounceEventStoped", null);
		}
		else
			player.sendMessage("Event 'CtF' not active.");

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
			return Files.read("data/scripts/events/CtF/31225.html", player);
		}
		return "";
	}

	// Red flag
	public String DialogAppend_35423(Integer val)
	{
		L2Player player = (L2Player) getSelf();
		if(player.getTeam() != 1)
			return "";
		if(val == 0)
			return Files.read("data/scripts/events/CtF/35423.html", player).replaceAll("n1", "" + Rnd.get(100, 999)).replaceAll("n2", "" + Rnd.get(100, 999));
		return "";
	}

	// Blue flag
	public String DialogAppend_35426(Integer val)
	{
		L2Player player = (L2Player) getSelf();
		if(player.getTeam() != 2)
			return "";
		if(val == 0)
			return Files.read("data/scripts/events/CtF/35426.html", player).replaceAll("n1", "" + Rnd.get(100, 999)).replaceAll("n2", "" + Rnd.get(100, 999));
		return "";
	}

	public void capture(String[] var)
	{
		L2Player player = (L2Player) getSelf();
		if(var.length != 4)
		{
			show(new CustomMessage("common.Error", player), player);
			return;
		}
		
		L2NpcInstance npc = getNpc();

		if(player.isDead() || npc == null || !player.isInRange(npc, 200) || npc.getNpcId() != (player.getTeam() == 1 ? 35423 : 35426))
		{
			show(new CustomMessage("common.Error", player), player);
			return;
		}

		Integer base;
		Integer add1;
		Integer add2;
		Integer summ;
		try
		{
			base = Integer.valueOf(var[0]);
			add1 = Integer.valueOf(var[1]);
			add2 = Integer.valueOf(var[2]);
			summ = Integer.valueOf(var[3]);
		}
		catch(Exception e)
		{
			show(new CustomMessage("common.Error", player), player);
			return;
		}

		if(add1 + add2 != summ)
		{
			show(new CustomMessage("common.Error", player), player);
			return;
		}

		if(base == 1 && blueFlag.isVisible()) // Синяя база
		{
			blueFlag.decayMe();
			addFlag(player, 13561);
		}

		if(base == 2 && redFlag.isVisible()) // Красная база
		{
			redFlag.decayMe();
			addFlag(player, 13560);
		}

		if(player.isInvisible() && player.getEffectList().getEffectByType(EffectType.Invisible) != null)
			player.getEffectList().stopEffects(EffectType.Invisible);
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
		else if(level >= 76 && level <= 85)
			return 6;
		else if(level >= 86 )
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
		_time_to_start = Config.EVENT_CtFTime;

		players_list1 = new GCSArray<Long>();
		players_list2 = new GCSArray<Long>();

		if(redFlag != null)
			redFlag.deleteMe();
		if(blueFlag != null)
			blueFlag.deleteMe();

		redFlag = spawn(redFlagLoc, 35423);
		blueFlag = spawn(blueFlagLoc, 35426);

		redFlag.decayMe();
		blueFlag.decayMe();

		String[] param = { String.valueOf(_time_to_start), String.valueOf(_minLevel), String.valueOf(_maxLevel) };
		sayToAll("scripts.events.CtF.AnnouncePreStart", param);

		executeTask("events.CtF.CtF", "question", new Object[0], 10000);
		executeTask("events.CtF.CtF", "announce", new Object[0], 60000);
	}

	public static void sayToAll(String address, String[] replacements)
	{
		Announcements.getInstance().announceByCustomMessage(address, replacements, Say2C.CRITICAL_ANNOUNCEMENT);
	}

	public static void question()
	{
		for(L2Player player : L2ObjectsStorage.getAllPlayersForIterate())
			if(player != null && player.getLevel() >= _minLevel && player.getLevel() <= _maxLevel && player.getReflection().getId() <= 0 && !player.isInOlympiadMode())
				player.scriptRequest(new CustomMessage("scripts.events.CtF.AskPlayer", player).toString(), "events.CtF.CtF:addPlayer", new Object[0]);
	}

	public static void announce()
	{
		if(players_list1.isEmpty() || players_list2.isEmpty())
		{
			sayToAll("scripts.events.CtF.AnnounceEventCancelled", null);
			_isRegistrationActive = false;
			_status = 0;
			executeTask("events.CtF.CtF", "autoContinue", new Object[0], 10000);
			return;
		}

		if(_time_to_start > 1)
		{
			_time_to_start--;
			String[] param = { String.valueOf(_time_to_start), String.valueOf(_minLevel), String.valueOf(_maxLevel) };
			sayToAll("scripts.events.CtF.AnnouncePreStart", param);
			executeTask("events.CtF.CtF", "announce", new Object[0], 60000);
		}
		else
		{
			_status = 1;
			_isRegistrationActive = false;
			sayToAll("scripts.events.CtF.AnnounceEventStarting", null);
			executeTask("events.CtF.CtF", "prepare", new Object[0], 5000);
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
			show(new CustomMessage("scripts.events.CtF.Registered", player), player);
		}
		else if(team == 2)
		{
			players_list2.add(player.getStoredId());
			show(new CustomMessage("scripts.events.CtF.Registered", player), player);
		}
		else
			_log.info("WTF??? Command id 0 in CtF...");
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
			show(new CustomMessage("scripts.events.CtF.Cancelled", player), player);
			return false;
		}

		if(player.getLevel() < _minLevel || player.getLevel() > _maxLevel)
		{
			show(new CustomMessage("scripts.events.CtF.CancelledLevel", player), player);
			return false;
		}

		if(player.isMounted())
		{
			show(new CustomMessage("scripts.events.CtF.Cancelled", player), player);
			return false;
		}

		if(player.getDuel() != null)
		{
			show(new CustomMessage("scripts.events.CtF.CancelledDuel", player), player);
			return false;
		}

		if(player.getTeam() != 0)
		{
			show(new CustomMessage("scripts.events.CtF.CancelledOtherEvent", player), player);
			return false;
		}

		if(player.getOlympiadGameId() > 0 || player.isInZoneOlympiad() || first && Olympiad.isRegistered(player))
		{
			show(new CustomMessage("scripts.events.CtF.CancelledOlympiad", player), player);
			return false;
		}

		if(player.isTeleporting())
		{
			show(new CustomMessage("scripts.events.CtF.CancelledTeleport", player), player);
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

		redFlag.spawnMe();
		blueFlag.spawnMe();

		executeTask("events.CtF.CtF", "ressurectPlayers", new Object[0], 1000);
		executeTask("events.CtF.CtF", "healPlayers", new Object[0], 2000);
		executeTask("events.CtF.CtF", "saveBackCoords", new Object[0], 3000);
		executeTask("events.CtF.CtF", "paralyzePlayers", new Object[0], 4000);
		executeTask("events.CtF.CtF", "teleportPlayersToColiseum", new Object[0], 5000);
		executeTask("events.CtF.CtF", "go", new Object[0], 60000);

		sayToAll("scripts.events.CtF.AnnounceFinalCountdown", null);
	}

	public static void go()
	{
		_status = 2;
		upParalyzePlayers();
		clearArena();
		sayToAll("scripts.events.CtF.AnnounceFight", null);
		_endTask = executeTask("events.CtF.CtF", "endOfTime", new Object[0], 300000);
	}

	public static void endOfTime()
	{
		endBattle(3); // ничья
	}

	public static void endBattle(int win)
	{
		if(_endTask != null)
		{
			_endTask.cancel(false);
			_endTask = null;
		}

		removeFlags();

		if(redFlag != null)
		{
			redFlag.deleteMe();
			redFlag = null;
		}

		if(blueFlag != null)
		{
			blueFlag.deleteMe();
			blueFlag = null;
		}

		_status = 0;
		removeAura();

		DoorTable.getInstance().getDoor(24190002).openMe();
		DoorTable.getInstance().getDoor(24190003).openMe();

		switch(win)
		{
			case 1:
				sayToAll("scripts.events.CtF.AnnounceFinishedRedWins", null);
				giveItemsToWinner(false, true, 1);
				break;
			case 2:
				sayToAll("scripts.events.CtF.AnnounceFinishedBlueWins", null);
				giveItemsToWinner(true, false, 1);
				break;
			case 3:
				sayToAll("scripts.events.CtF.AnnounceFinishedDraw", null);
				giveItemsToWinner(true, true, 0.5);
				break;
		}

		sayToAll("scripts.events.CtF.AnnounceEnd", null);
		executeTask("events.CtF.CtF", "end", new Object[0], 30000);
		_isRegistrationActive = false;
	}

	public static void end()
	{
		executeTask("events.CtF.CtF", "ressurectPlayers", new Object[0], 1000);
		executeTask("events.CtF.CtF", "healPlayers", new Object[0], 2000);
		executeTask("events.CtF.CtF", "teleportPlayersToSavedCoords", new Object[0], 3000);
		executeTask("events.CtF.CtF", "autoContinue", new Object[0], 10000);
	}

	public void autoContinue()
	{
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
				addItem(player, Config.EVENT_CtFItemID, Math.round((Config.EVENT_CtF_rate ? player.getLevel() : 1) * Config.EVENT_CtFItemCOUNT * rate));
		if(team2)
			for(L2Player player : getPlayers(players_list2))
				addItem(player, Config.EVENT_CtFItemID, Math.round((Config.EVENT_CtF_rate ? player.getLevel() : 1) * Config.EVENT_CtFItemCOUNT * rate));
	}

	public static void saveBackCoords()
	{
		for(L2Player player : getPlayers(players_list1))
			player.setVar("CtF_backCoords", player.getX() + " " + player.getY() + " " + player.getZ() + " " + player.getReflection().getId());
		for(L2Player player : getPlayers(players_list2))
			player.setVar("CtF_backCoords", player.getX() + " " + player.getY() + " " + player.getZ() + " " + player.getReflection().getId());
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
				String var = player.getVar("CtF_backCoords");
				if(var == null || var.equals(""))
					continue;
				String[] coords = var.split(" ");
				if(coords.length != 4)
					continue;
				player.teleToLocation(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]), Long.parseLong(coords[3]));
				player.unsetVar("CtF_backCoords");
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		for(L2Player player : getPlayers(players_list2))
			try
			{
				String var = player.getVar("CtF_backCoords");
				if(var == null || var.equals(""))
					continue;
				String[] coords = var.split(" ");
				if(coords.length != 4)
					continue;
				player.teleToLocation(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]), Integer.parseInt(coords[3]));
				player.unsetVar("CtF_backCoords");
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
			else
				player.setTeam(1, true);
		for(L2Player player : getPlayers(players_list2))
			if(!checkPlayer(player, false))
				removePlayer(player);
			else
				player.setTeam(2, true);
	}

	public static void removeAura()
	{
		for(L2Player player : getPlayers(players_list1))
			player.setTeam(0, true);
		for(L2Player player : getPlayers(players_list2))
			player.setTeam(0, true);
	}

	public static void clearArena()
	{
		for(L2Object obj : _zone.getObjects())
			if(obj != null)
			{
				L2Player player = obj.getPlayer();
				if(player != null && !players_list1.contains(player.getStoredId()) && !players_list2.contains(player.getStoredId()))
					player.teleToLocation(147451, 46728, -3410);
			}
	}

	public static void OnDie(L2Character self, L2Character killer)
	{
		if(_status > 1 && self != null && self.isPlayer() && self.getTeam() > 0 && (players_list1.contains(self.getStoredId()) || players_list2.contains(self.getStoredId())))
		{
			dropFlag((L2Player) self);
			executeTask("events.CtF.CtF", "resurrectAtBase", new Object[] { (L2Player) self }, 10000);
		}
	}

	public static void resurrectAtBase(L2Player player)
	{
		if(player.getTeam() <= 0)
			return;
		if(player.isDead())
		{
			player.setCurrentCp(player.getMaxCp());
			player.setCurrentHp(player.getMaxHp(), true);
			player.setCurrentMp(player.getMaxMp());
			player.broadcastPacket(new Revive(player));
		}
		int[] pos;
		if(player.getTeam() == 1)
			pos = team1loc.getRandomPoint();
		else
			pos = team2loc.getRandomPoint();
		player.teleToLocation(pos[0], pos[1], pos[2], 0);
	}

	public static Location OnEscape(L2Player player)
	{
		if(_status > 1 && player != null && player.getTeam() > 0 && (players_list1.contains(player.getStoredId()) || players_list2.contains(player.getStoredId())))
			removePlayer(player);
		return null;
	}

	public static void OnPlayerExit(L2Player player)
	{
		if(player == null || player.getTeam() < 1)
			return;

		// Вышел или вылетел во время регистрации
		if(_status == 0 && _isRegistrationActive && player.getTeam() > 0 && (players_list1.contains(player.getStoredId()) || players_list2.contains(player.getStoredId())))
		{
			removePlayer(player);
			return;
		}

		// Вышел или вылетел во время телепортации
		if(_status == 1 && (players_list1.contains(player.getStoredId()) || players_list2.contains(player.getStoredId())))
		{
			removePlayer(player);

			try
			{
				String var = player.getVar("CtF_backCoords");
				if(var == null || var.equals(""))
					return;
				String[] coords = var.split(" ");
				if(coords.length != 4)
					return;
				player.teleToLocation(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]), Integer.parseInt(coords[3]));
				player.unsetVar("CtF_backCoords");
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
			if(_status > 0 && player != null && !players_list1.contains(player.getStoredId()) && !players_list2.contains(player.getStoredId()))
				ThreadPoolManager.getInstance().scheduleGeneral(new TeleportTask((L2Character) object, new Location(147451, 46728, -3410)), 3000);
		}

		@Override
		public void objectLeaved(L2Zone zone, L2Object object)
		{
			if(object == null)
				return;
			L2Player player = object.getPlayer();
			if(_status > 1 && player != null && player.getTeam() > 0 && (players_list1.contains(player.getStoredId()) || players_list2.contains(player.getStoredId())))
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

	private class RedBaseZoneListener extends L2ZoneEnterLeaveListener
	{
		@Override
		public void objectEntered(L2Zone zone, L2Object object)
		{
			if(object == null)
				return;
			L2Player player = object.getPlayer();
			if(_status > 0 && player != null && players_list1.contains(player.getStoredId()) && player.isTerritoryFlagEquipped())
				endBattle(2);
		}

		@Override
		public void objectLeaved(L2Zone zone, L2Object object)
		{}
	}

	private class BlueBaseZoneListener extends L2ZoneEnterLeaveListener
	{
		@Override
		public void objectEntered(L2Zone zone, L2Object object)
		{
			if(object == null)
				return;
			L2Player player = object.getPlayer();
			if(_status > 0 && player != null && players_list2.contains(player.getStoredId()) && player.isTerritoryFlagEquipped())
				endBattle(1);
		}

		@Override
		public void objectLeaved(L2Zone zone, L2Object object)
		{}
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

		public void run()
		{
			target.stopStunning();
			target.teleToLocation(loc);
		}
	}

	private static void removePlayer(L2Player player)
	{
		if(player != null)
		{
			players_list1.remove(player.getStoredId());
			players_list2.remove(player.getStoredId());
			player.setTeam(0, true);
			dropFlag(player);
		}
	}

	private static void addFlag(L2Player player, int flagId)
	{
		L2ItemInstance item = ItemTemplates.getInstance().createItem(flagId);
		item.setCustomType1(77);
		item.setCustomFlags(L2ItemInstance.FLAG_EQUIP_ON_PICKUP | L2ItemInstance.FLAG_NO_DESTROY | L2ItemInstance.FLAG_NO_TRADE | L2ItemInstance.FLAG_NO_UNEQUIP, false);
		player.getInventory().addItem(item);
		player.getInventory().equipItem(item, false);
		player.sendChanges();
		player.sendPacket(Msg.YOU_VE_ACQUIRED_THE_WARD_MOVE_QUICKLY_TO_YOUR_FORCES__OUTPOST);
	}

	private static void removeFlags()
	{
		for(L2Player player : getPlayers(players_list1))
			removeFlag(player);
		for(L2Player player : getPlayers(players_list2))
			removeFlag(player);
	}

	private static void removeFlag(L2Player player)
	{
		if(player != null && player.isTerritoryFlagEquipped())
		{
			L2ItemInstance flag = player.getActiveWeaponInstance();
			if(flag != null && flag.getCustomType1() == 77) // 77 это эвентовый флаг
			{
				flag.setCustomFlags(0, false);
				player.getInventory().destroyItem(flag, 1, false);
				player.broadcastUserInfo(true);
			}
		}
	}

	private static void dropFlag(L2Player player)
	{
		if(player != null && player.isTerritoryFlagEquipped())
		{
			L2ItemInstance flag = player.getActiveWeaponInstance();
			if(flag != null && flag.getCustomType1() == 77) // 77 это эвентовый флаг
			{
				flag.setCustomFlags(0, false);
				player.getInventory().destroyItem(flag, 1, false);
				player.broadcastUserInfo(true);
				if(flag.getItemId() == 13560)
				{
					redFlag.setXYZInvisible(player.getLoc());
					redFlag.spawnMe();
				}
				else if(flag.getItemId() == 13561)
				{
					blueFlag.setXYZInvisible(player.getLoc());
					blueFlag.spawnMe();
				}
			}
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