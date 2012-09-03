package events.Carnage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

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
import l2rt.gameserver.model.*;
import l2rt.gameserver.model.L2Zone.ZoneType;
import l2rt.gameserver.model.entity.Hero;
import l2rt.gameserver.model.entity.olympiad.Olympiad;
import l2rt.gameserver.model.entity.residence.Castle;
import l2rt.gameserver.model.entity.siege.territory.TerritorySiege;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.ExShowScreenMessage;
import l2rt.gameserver.network.serverpackets.Revive;
import l2rt.gameserver.network.serverpackets.SkillList;
import l2rt.gameserver.skills.AbnormalEffect;
import l2rt.gameserver.skills.Env;
import l2rt.gameserver.skills.effects.EffectTemplate;
import l2rt.gameserver.tables.DoorTable;
import l2rt.gameserver.tables.NpcTable;
import l2rt.gameserver.tables.SkillTable;
import l2rt.gameserver.templates.L2NpcTemplate;
import l2rt.util.Files;
import l2rt.util.GArray;
import l2rt.util.GCSArray;
import l2rt.util.Location;
import l2rt.util.Rnd;
import l2rt.util.Util;

public class Carnage extends Functions implements ScriptFile
{	
	private static Logger _log = Logger.getLogger(Carnage.class.getName());
    private static final boolean REMOVE_BUFFS = true; //включен ли этот массив или нет
    private static final int[][] BUFFS_TO_REMOVE = { // какие бафы снимать при телепорте на эвент
            {2003, 1}//{id скила, лвл скила}, {id скила, лвл скила};(но если я не указываю лвл скила {id скила};, то снимаем любой лвл бафа, а если лвл скила указан, то снимает баф(скил) с определенным лвлом) пример:  {1, 1}, {2}, {3, 1}
    };

    private static final int[] REWARD = { //массив по выдачи бонуса {ID предмета, количество; ID предмета, количество поддержка}; возможно несколько бонусов
            6673, 10000
    };

    private static final String [][] eventTimes = {

	{"2:00", "2:15"},
	{"6:00", "6:15"},
	{"11:00", "11:15"},
	{"15:00", "15:15"},
	{"21:00", "21:15"}
    };

   /**
	* Это список NPC которые будут спавнится после телепорта.
	* Формат массива: ID Npc, locX, locY, locZ, locH
	**/
	private static int[][] npcs = {
									{31143, 10000, 10676, -3455, 0}, // Собственно ID NPC, locX, locY, locZ, locH
									{31143, 10000, 10676, -3455, 0},						
									{31143, 10000, 10676, -3455, 0}
								  };
	// Спавнить вообще этих НПЦ?
	private static boolean _spawnNpcs = false;

	private static boolean ALLOW_RESTRICT_SKILLS = true;
	private static int[][] RESTRICT_SKILLS = {
			{1487, 0}, {262, 0}, {1015, 0}, {1012, 0}, {1015, 0}, {1217, 0}, {1218, 0}, {1254, 0}, {1018, 0}, {1258, 0}, 
			{1020, 0}, {1401, 0}, {1418, 0}, {1409, 0}, {1459, 0}, {1426, 0}, {1505, 0}, {1506, 0}, {1428, 0}, 
			{1460, 0}, {1238, 0}, {1013, 0}, {1044, 0}, {1531, 0}, {1507, 0}, {1391, 0}, {1010, 0}, 
			{1005, 0}, {1229, 0}, {1003, 0}, {1306, 0}, {1427, 0}, {1305, 0}, {1249, 0}, {1538, 0}, 
			{1008, 0}, {1260, 0}, {1416, 0}, {1414, 0}, {1365, 0}, {1282, 0}, {1364, 0}, {1537, 0}, 
			{1250, 0}, {1004, 0}, {1415, 0}, {1005, 0}, {1536, 0}, {1261, 0}, 
			{1388, 0}, {1389, 0}, {1068, 0}, {1040, 0}, {1086, 0}, {1085, 0}, {1035, 0}, {1036, 0}, {1043, 0}, {1045, 0},
			{1048, 0}, {1059, 0}, {1062, 0}, {1077, 0}, {1078, 0}, {1087, 0}, {1182, 0}, {1189, 0}, 
			{1191, 0}, {1204, 0}, {1240, 0}, {1242, 0}, {1243, 0}, {1259, 0}, {1268, 0}, {1303, 0}, 
			{1304, 0}, {1352, 0}, {1353, 0}, {1354, 0}, {1392, 0}, {1393, 0}, {1397, 0}, {1542, 0}, 
			{1499, 0}, {1500, 0}, {1501, 0}, {1502, 0}, {1503, 0}, {1504, 0}, {1519, 0}, {1002, 0}, 
			{1006, 0}, {1007, 0}, {1009, 0}, {1010, 0}, {1251, 0}, {1252, 0}, {1253, 0}, {1284, 0}, 
			{1308, 0}, {1309, 0}, {1310, 0}, {1362, 0}, {1390, 0}, {1391, 0}, {1413, 0}, {1461, 0}, {1535, 0}, 
			{825, 0}, {826, 0}, {827, 0}, {828, 0}, {829, 0}, {830, 0}, {1331, 0}, {1331, 0}, {1332, 0}, 
			{1332, 0}, {1355, 0}, {1356, 0}, {1357, 0}, {1363, 0}, // использовать скил ТОЛЬКО НА СЕБЯ
			{213, 1} // использовать скилы только на ЧЛЕНЫ СВОЕЙ КОМАНДЫ
					 // если пусто, то используется на все на врагов и союзников...
	};

	private static int[] _listAllowSaveBuffs =  // Ниже список бафов, которые сохраняются, если ALLOW_BUFFS = true,
												// если конечно бафнуты на персонажа.
												{1388, 1389, 1068, 1040, 1086, 1085, 1242, 1059, 1240, 1078,
												1077, 1303, 1204, 1062, 1542, 1397, 1045, 1048, 1087, 1043,
												1268, 1259, 1243, 1035, 1304, 1036, 1191, 1182, 1189, 1352,
												1354, 1353, 1393, 1392, 1499, 1501, 1502, 1500, 1519, 1503,
												1504, 1251, 1252, 1253, 1002, 1284, 1308, 1309, 1391, 1007,
												1009, 1006, 1461, 1010, 1390, 1310, 1362, 1413, 1535, 275,
												276, 274, 273, 271, 365, 272, 277, 310, 307, 311, 309, 915,
												530, 269, 266, 264, 267, 268, 265, 349, 364, 764, 529, 304,
												270, 306, 305, 308, 363, 914, 4700, 4703, 4699, 4702, 825,
												828, 827, 829, 826, 830, 1356, 1355, 1357, 1363, 2003};
	private static int[][][] _listBuff = // Это собственно массив бафов, которые бафаются на игроков в случае,
										{// если их бафы не сохраняются, т.е. ALLOW_BUFFS = false
											{ // Бафы на Война:
												{1086, 2}, // Haste: 2 LvL
												{4342, 2}, // Wind Walk: 2 LvL
												{1068, 3}, // Might: 3 LvL
												{1240, 3}, // Guidance: 3 LvL
												{1077, 3}, // Focus: 3 LvL
												{1242, 3} // Death Whisper: 3 LvL
											},
											{ // Бафы на Мага:
												{4342, 2}, // Wind Walk: 2 LvL
												{1059, 3}, // Empower: 3 LvL
												{1085, 3}, // Acumen: 3 LvL
												{1078, 6}, // Concentration: 6 LvL
												{1062, 2} // Berserker Spirit: 2 LvL
											}

										};

	private static boolean ALLOW_RESTRICT_ITEMS = true;   // Включена ли проверка на использование запрещенных предметов?
	private static int[] RESTRICT_ITEMS = {5592, 5591, 1060, 1061, 14700, 1539, 
						736, 737, 1829, 1830, 3926, 3927, 3928, 3929, 3930, 3931, 
						3932, 3933, 3934, 3935, 4218, 7117, 7118, 7119, 7120, 7121, 
						7122, 7123, 7124, 7125, 7126, 7127, 7128, 7129, 7130, 7131, 
						7132, 7133, 7134, 7135, 10129, 20523, 20524, 20525, 20526,
						8623, 8624, 8625, 8626, 8627, 8628, 8629, 8630, 8631, 8632, 
						8633, 8634, 8635, 8636, 8637, 8638, 8639, 5234, 5235, 5236, 
						5237, 5242, 5243, 5244, 5245, 5246, 5247, 5248, 726}; // Сам список запрещенных предметов.
	private static boolean PROTECT_IP_ACTIVE = false;

	private static int TIME_FOR_RES = 1; // Через какое время после смерти персонажа восстанавливают.


	private static HashMap<Long, GArray<L2Effect>> _saveBuffList = new HashMap<Long,GArray<L2Effect>>();
	private static GArray<L2NpcInstance> _spawns = new GArray<L2NpcInstance>();
	private static HashMap<Long, Integer> _topKills = new HashMap<Long, Integer>();
	private static HashMap<Long, ScheduledFuture<?>> _resurrectionList = new HashMap<Long, ScheduledFuture<?>>(); //Хранилище задач на Ressuction.

	public static boolean canSpawnPet(L2Player player)
	{
		if (players_list.contains(player.getObjectId()))
			if (!ALLOW_PETS) return false;
		return true;
	}
	
	public class StartTask implements Runnable
	{
        String endTime;

        public StartTask(String endTime) {
            this.endTime = endTime;
        }

		public void run()
		{

			if(!_active)
				return;

			if(isPvPEventStarted())
			{
				_log.info("Last Hero not started: another event is already running");
				return;
			}

			if(TerritorySiege.isInProgress())
			{
				_log.fine("Carnage not started: TerritorySiege in progress");
				return;
			}

			for(Castle c : CastleManager.getInstance().getCastles().values())
				if(c.getSiege() != null && c.getSiege().isInProgress())
				{
					_log.fine("Carnage not started: CastleSiege in progress");
					return;
				}

			start(new String[] { "-1", "-1", endTime });
		}
	}

	private static List<ScheduledFuture<?>> startTasks = new ArrayList<ScheduledFuture<?>>();

	private static GCSArray<Long> players_list = new GCSArray<Long>();

	private static boolean _isRegistrationActive = false;
	public static int _status = 0;
	private static int _time_to_start;
	private static int _category;
	private static int _minLevel;
	private static int _maxLevel;
	private static int _autoContinue = 0;

	private static boolean ALLOW_BUFFS = true;
	private static boolean ALLOW_CLAN_SKILL = true;
	private static boolean ALLOW_HERO_SKILL = true;
	private static boolean ALLOW_PETS = true; //Разрешить спавн пета?

	private static L2Zone _zone = ZoneManager.getInstance().getZoneByIndex(ZoneType.battle_zone, 4, true);
	ZoneListener _zoneListener = new ZoneListener();

	public void onLoad()
	{
		_zone.getListenerEngine().addMethodInvokedListener(_zoneListener);
        for(String time[] : eventTimes) {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, Integer.valueOf(time[0].split(":")[0]));
            cal.set(Calendar.MINUTE, Integer.valueOf(time[0].split(":")[1]));
            cal.set(Calendar.SECOND, 0);
            while(cal.getTimeInMillis() < System.currentTimeMillis())
                cal.add(Calendar.DAY_OF_YEAR, 1);
            ScheduledFuture<?> startTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new StartTask(time[1]), cal.getTimeInMillis() - System.currentTimeMillis(), 86400000);
            startTasks.add(startTask);
        }
		_active = ServerVariables.getString("Carnage", "off").equalsIgnoreCase("on");

		_log.fine("Loaded Event: Carnage");
	}
	
	public void onReload()
	{
		_zone.getListenerEngine().removeMethodInvokedListener(_zoneListener);
        for(ScheduledFuture<?> sf : startTasks)
            if(sf != null)
                sf.cancel(true);

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
            for(String time[] : eventTimes) {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, Integer.valueOf(time[0].split(":")[0]));
                cal.set(Calendar.MINUTE, Integer.valueOf(time[0].split(":")[1]));
                cal.set(Calendar.SECOND, 0);
                while(cal.getTimeInMillis() < System.currentTimeMillis())
                    cal.add(Calendar.DAY_OF_YEAR, 1);
                ScheduledFuture<?> startTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new StartTask(time[1]), cal.getTimeInMillis() - System.currentTimeMillis(), 86400000);
                startTasks.add(startTask);
            }
			ServerVariables.set("Carnage", "on");
			_log.fine("Event 'Carnage' activated.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.Carnage.AnnounceEventStarted", null);
		}
		else
			player.sendMessage("Event 'Carnage' already active.");

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
            for(ScheduledFuture<?> sf : startTasks)
                if(sf != null)
                    sf.cancel(true);
            startTasks.clear();
			ServerVariables.unset("Carnage");
			_log.fine("Event 'Carnage' deactivated.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.Carnage.AnnounceEventStoped", null);
		}
		else
			player.sendMessage("Event 'Carnage' not active.");

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
			return Files.read("data/scripts/events/Carnage/31225.html", player);
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
		else if(level >= 76)
			return 6;
		return 0;
	}

	public void start(String[] var)
	{
		L2Player player = (L2Player) getSelf();
		if(var.length != 3)
		{
			show(new CustomMessage("common.Error", player), player);
			return;
		}

		Integer category;
		Integer autoContinue;
        String time;
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
			_maxLevel = 85;
		}
		else
		{
			_minLevel = getMinLevelForCategory(_category);
			_maxLevel = getMaxLevelForCategory(_category);
		}

		_status = 0;
		_isRegistrationActive = true;
		_time_to_start = 3;

		players_list = new GCSArray<Long>();

		String[] param = { String.valueOf(_time_to_start), String.valueOf(_minLevel), String.valueOf(_maxLevel) };
		sayToAll("scripts.events.Carnage.AnnouncePreStart", param);

		executeTask("events.Carnage.Carnage", "question", new Object[0], 10000);
		executeTask("events.Carnage.Carnage", "announce", new Object[]{var[2]}, 60000);
	}

	public static void sayToAll(String address, String[] replacements)
	{
		Announcements.getInstance().announceByCustomMessage(address, replacements, Say2C.CRITICAL_ANNOUNCEMENT);
	}

	public static void question()
	{
		for(L2Player player : L2ObjectsStorage.getAllPlayersForIterate())
			if(player != null && player.getLevel() >= _minLevel && player.getLevel() <= _maxLevel && player.getReflection().getId() <= 0 && !player.isInOlympiadMode())
				player.scriptRequest(new CustomMessage("scripts.events.Carnage.AskPlayer", player).toString(), "events.Carnage.Carnage:addPlayer", new Object[0]);
	}

	public static void announce(String time)
	{
		if(players_list.size() < 2)
		{
			sayToAll("scripts.events.Carnage.AnnounceEventCancelled", null);
			_isRegistrationActive = false;
			_status = 0;
			executeTask("events.Carnage.Carnage", "autoContinue", new Object[0], 10000);
			return;
		}

		if(_time_to_start > 1)
		{
			_time_to_start--;
			String[] param = { String.valueOf(_time_to_start), String.valueOf(_minLevel), String.valueOf(_maxLevel) };
			sayToAll("scripts.events.Carnage.AnnouncePreStart", param);
			executeTask("events.Carnage.Carnage", "announce", new Object[]{time}, 60000);
		}
		else
		{
			_status = 1;
			_isRegistrationActive = false;
			sayToAll("scripts.events.Carnage.AnnounceEventStarting", null);
			executeTask("events.Carnage.Carnage", "prepare", new Object[]{time}, 5000);
		}
	}

	public void addPlayer()
	{
		L2Player player = (L2Player) getSelf();
		if(player == null || !checkPlayer(player, true))
			return;

		players_list.add(player.getStoredId());

		show(new CustomMessage("scripts.events.Carnage.Registered", player), player);
	}

	public static boolean checkPlayer(L2Player player, boolean first)
	{
		if(first && !_isRegistrationActive)
		{
			show(new CustomMessage("scripts.events.Late", player), player);
			return false;
		}

		if(first && players_list.contains(player.getStoredId()))
		{
			show(new CustomMessage("scripts.events.Carnage.Cancelled", player), player);
			return false;
		}

		if(player.getLevel() < _minLevel || player.getLevel() > _maxLevel)
		{
			show(new CustomMessage("scripts.events.Carnage.CancelledLevel", player), player);
			return false;
		}

		if(player.isMounted())
		{
			show(new CustomMessage("scripts.events.Carnage.Cancelled", player), player);
			return false;
		}

		if(player.getDuel() != null)
		{
			show(new CustomMessage("scripts.events.Carnage.CancelledDuel", player), player);
			return false;
		}

		if(player.getTeam() != 0)
		{
			show(new CustomMessage("scripts.events.Carnage.CancelledOtherEvent", player), player);
			return false;
		}

		if(player.getOlympiadGameId() > 0 || player.isInZoneOlympiad() || first && Olympiad.isRegistered(player))
		{
			show(new CustomMessage("scripts.events.Carnage.CancelledOlympiad", player), player);
			return false;
		}

		if(player.isInParty())
		{
			show(new CustomMessage("scripts.events.Carnage.CancelledOtherEvent", player), player);
			return false;
		}

		if(player.isTeleporting())
		{
			show(new CustomMessage("scripts.events.Carnage.CancelledTeleport", player), player);
			return false;
		}

		if (first && PROTECT_IP_ACTIVE && sameIp(player))
		{
			show("Вы не можете учавствовать на эвенте, с вашим IP уже кто-то зарегестрирован.", player, null);
			return false;
		}
        if(player.getObserverMode() != 0){
            return false;
        }

		return true;
	}

	public static void prepare(String time)
	{
		DoorTable.getInstance().getDoor(24190002).closeMe();
		DoorTable.getInstance().getDoor(24190003).closeMe();

		cleanPlayers();
		clearArena();

		executeTask("events.Carnage.Carnage", "ressurectPlayers", new Object[0], 1000);
		executeTask("events.Carnage.Carnage", "healPlayers", new Object[0], 2000);
		executeTask("events.Carnage.Carnage", "saveBackCoords", new Object[0], 3000);
		executeTask("events.Carnage.Carnage", "paralyzePlayers", new Object[0], 4000);
		executeTask("events.Carnage.Carnage", "teleportPlayersToColiseum", new Object[0], 5000);
		executeTask("events.Carnage.Carnage", "go", new Object[]{time}, 60000);

		sayToAll("scripts.events.Carnage.AnnounceFinalCountdown", null);
	}

	public static void go(String time)
	{
		_status = 2;
		upParalyzePlayers();
		checkLive();
		clearArena();
		sayToAll("scripts.events.Carnage.AnnounceFight", null);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, Integer.valueOf(time.split(":")[0]));
        cal.set(Calendar.MINUTE, Integer.valueOf(time.split(":")[1]));
        cal.set(Calendar.SECOND, 0);
        while(cal.getTimeInMillis() < System.currentTimeMillis())
            cal.add(Calendar.DAY_OF_YEAR, 1);
		ThreadPoolManager.getInstance().scheduleGeneral(new timer((int) (cal.getTimeInMillis() - System.currentTimeMillis()) / 1000), 0);
	}

	public static void endBattle()
	{
		if (_status==0) return;
		DoorTable.getInstance().getDoor(24190002).openMe();
		DoorTable.getInstance().getDoor(24190003).openMe();

		_status = 0;
		removeAura();

		int max = 0;
		for(int frags : _topKills.values())
			max = Math.max(max, frags);

		if (max>0)
		{
			L2Player player;
			for(long objId : _topKills.keySet())
			{
				if (_topKills.get(objId)<max)
					continue;

				player = L2ObjectsStorage.getAsPlayer(objId);

				if (player == null)
					continue;

				String[] repl = { player.getName() };
				Announcements.getInstance().announceToAll("Carnage: победил " + player.getName() + ", со счетом: " + max);
                for(int i=0; i < REWARD.length; i+=2) {
                    addItem(player, REWARD[i], REWARD[i+1] / _topKills.size());
                }
			}
		}
		sayToAll("scripts.events.Carnage.AnnounceEnd", null);
		end();
		_isRegistrationActive = false;
	}

	public static void end()
	{
		despawnNpcs();
		executeTask("events.Carnage.Carnage", "ressurectPlayers", new Object[0], 1000);
		executeTask("events.Carnage.Carnage", "healPlayers", new Object[0], 2000);
		executeTask("events.Carnage.Carnage", "teleportPlayersToSavedCoords", new Object[0], 3000);
		executeTask("events.Carnage.Carnage", "backBuff", new Object[0], 4000);
		executeTask("events.Carnage.Carnage", "autoContinue", new Object[0], 10000);
	}

	public void autoContinue()
	{
		_saveBuffList.clear();
		players_list.clear();
		_topKills.clear();


		if(_autoContinue > 0)
		{
			if(_autoContinue >= 6)
			{
				_autoContinue = 0;
				return;
			}
			start(new String[] { "" + (_autoContinue + 1), "" + (_autoContinue + 1) });
		}
	}

	public static void saveBackCoords()
	{
		for(L2Player player : getPlayers(players_list))
			player.setVar("Carnage_backCoords", player.getX() + " " + player.getY() + " " + player.getZ() + " " + player.getReflection().getId());
	}

	public static void teleportPlayersToColiseum()
	{
		for(L2Player player : getPlayers(players_list))
		{
			unRide(player);
			unSummonPet(player, true);
            if(REMOVE_BUFFS) {
                for(int buff[] : BUFFS_TO_REMOVE) {
                    GArray<L2Effect> effects;
                    if((effects = player.getEffectList().getEffectsBySkillId(buff[0])) != null) {
                        if(buff.length == 2) {
                            for(L2Effect effect : effects) {
                                if(effect.getSkill().getLevel() == buff[1]) {
                                    player.getEffectList().stopEffect(buff[0]);
                                }
                            }
                        } else if (buff.length == 1) {
                            for(L2Effect effect : effects) {
                                player.getEffectList().stopEffect(buff[0]);
                            }
                        }
                    }
                }
            }
            Location pos = Rnd.coordsRandomize(149505, 46719, -3417, 0, 0, 500);
			player.teleToLocation(pos, 0);
		}
	}

	public static void teleportPlayersToSavedCoords()
	{
		for(L2Player player : getPlayers(players_list))
			try
			{
				String var = player.getVar("Carnage_backCoords");
				if(var == null || var.equals(""))
					continue;
				String[] coords = var.split(" ");
				if(coords.length != 4)
					continue;
				player.teleToLocation(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]), Long.parseLong(coords[3]));
				player.unsetVar("Carnage_backCoords");
				_topKills.remove(player.getStoredId());
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
	}

	public static void paralyzePlayers()
	{
		spawnNpcs();
		removeBuff();
		for(L2Player player : getPlayers(players_list))
		{
			player.startAbnormalEffect(AbnormalEffect.HOLD_2);
			player.setParalyzed(true);
			if(player.getPet() != null)
			{
				player.startAbnormalEffect(AbnormalEffect.HOLD_2);
				player.setParalyzed(true);
			}

			if(player.getParty() != null)
				player.getParty().oustPartyMember(player);
		}
	}

	public static void upParalyzePlayers()
	{
		for(L2Player player : getPlayers(players_list))
		{
			player.stopAbnormalEffect(AbnormalEffect.HOLD_2);
			player.setParalyzed(false);
			if(player.getPet() != null)
			{
				player.getPet().stopAbnormalEffect(AbnormalEffect.HOLD_2);
				player.getPet().setParalyzed(false);
			}

			if(player.getParty() != null)
				player.getParty().oustPartyMember(player);
		}
	}
	
	public static void removeBuff()
	{
		saveBuffList();
		for(L2Player player : getPlayers(players_list))
			if(player != null)
				try
				{
					if(player.isCastingNow())
						player.abortCast(true);

					if(!ALLOW_CLAN_SKILL)
						if(player.getClan() != null)
							for(L2Skill skill : player.getClan().getAllSkills())
								player.removeSkill(skill, false);

					if(!ALLOW_HERO_SKILL)
						if(player.isHero())
							Hero.removeSkills(player);

					if(!ALLOW_BUFFS)
					{
						player.getEffectList().stopAllEffects();

						if(player.getPet() != null)
						{
							L2Summon summon = player.getPet();
							summon.getEffectList().stopAllEffects();
							if(summon.isPet())
								summon.unSummon();
						}

						if(player.getAgathion() != null)
							player.setAgathion(0);
						ThreadPoolManager.getInstance().scheduleGeneral(new buffPlayer(player), 0);
					}

					player.sendPacket(new SkillList(player));
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
	}

	public static void backBuff()
	{
		for(L2Player player : getPlayers(players_list))
		{
			if(player == null)
				continue;
			try
			{
				player.getEffectList().stopAllEffects();

				if(!ALLOW_CLAN_SKILL)
					if(player.getClan() != null)
						for(L2Skill skill : player.getClan().getAllSkills())
							if(skill.getMinPledgeClass() <= player.getPledgeClass())
								player.addSkill(skill, false);

				if(!ALLOW_HERO_SKILL)
					if(player.isHero())
						Hero.addSkills(player);

				player.sendPacket(new SkillList(player));
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		restoreBuffList();
	}

	public static void ressurectPlayers()
	{
		for(L2Player player : getPlayers(players_list))
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
		for(L2Player player : getPlayers(players_list))
		{
			player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
			player.setCurrentCp(player.getMaxCp());
		}
	}

	public static void cleanPlayers()
	{
		for(L2Player player : getPlayers(players_list))
			if(!checkPlayer(player, false))
				removePlayer(player, false);
	}

	public static void checkLive()
	{
		for(L2Player player : getPlayers(players_list))
			if(player.isInZone(_zone) && player.isConnected() && !player.isLogoutStarted())
				player.setTeam(2, false);
			else
				removePlayer(player, true);

		if(players_list.size() == 0)
			endBattle();
	}

	public static void removeAura()
	{
		for(L2Player player : getPlayers(players_list))
			player.setTeam(0, false);
	}

	public static void clearArena()
	{
		for(L2Object obj : _zone.getObjects())
			if(obj != null)
			{
				L2Player player = obj.getPlayer();
				if(player != null && !players_list.contains(player.getStoredId()))
					player.teleToLocation(147451, 46728, -3410);
			}
	}

	public static void doDie(L2Character self, L2Character killer)
	{
		if(_status > 1 && self != null && self.isPlayer() && self.getTeam() > 0 && players_list.contains(self.getStoredId()))
		{
			self.sendMessage("Через " + TIME_FOR_RES + " секунд вы будите восстановлены.");
			_resurrectionList.put(self.getStoredId(), executeTask("events.Carnage.Carnage", "ressurectPlayerInRound", new Object[]{(L2Player) self}, TIME_FOR_RES * 1000));

			_topKills.put(killer.getStoredId(), (_topKills.get(killer.getStoredId())!=null ? _topKills.get(killer.getStoredId()) + 1 : 1));
			checkLive();
		}
	}

	public static void ressurectPlayerInRound(L2Player player)
	{
			ressurectPlayer(player);
			Location pos = Rnd.coordsRandomize(149505, 46719, -3417, 0, 0, 1300);
			player.teleToLocation(pos, 0);
			if (!ALLOW_BUFFS) ThreadPoolManager.getInstance().scheduleGeneral(new buffPlayer(player), 0);
			else ThreadPoolManager.getInstance().scheduleGeneral(new restoreBuffListForPlayer(player), 0);
	}

	public static void ressurectPlayer(L2Player player)
	{
		if(player.isDead())
		{
			player.restoreExp();
			player.setCurrentCp(player.getMaxCp());
			player.setCurrentHp(player.getMaxHp(), true);
			player.setCurrentMp(player.getMaxMp());
			player.broadcastPacket(new Revive(player));
		}
	}

	public static Location OnEscape(L2Player player)
	{
		if(_status > 1 && player != null && player.getTeam() > 0 && players_list.contains(player.getStoredId()))
		{
			removePlayer(player, true);
			checkLive();
		}
		return null;
	}

	public static void OnPlayerExit(L2Player player)
	{
		if(player == null)
			return;

		// Вышел или вылетел во время регистрации
		if(_status == 0 && _isRegistrationActive && players_list.contains(player.getStoredId()))
		{
			removePlayer(player, false);
			return;
		}

		// Вышел или вылетел во время телепортации
		if(_status == 1 && players_list.contains(player.getStoredId()))
		{
			removePlayer(player, true);

			try
			{
				String var = player.getVar("Carnage_backCoords");
				if(var == null || var.equals(""))
					return;
				String[] coords = var.split(" ");
				if(coords.length != 4)
					return;
				player.teleToLocation(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]), Integer.parseInt(coords[3]));
				player.unsetVar("Carnage_backCoords");
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
			if(_status > 0 && player != null && !players_list.contains(player.getStoredId()))
				ThreadPoolManager.getInstance().scheduleGeneral(new TeleportTask((L2Character) object, new Location(147451, 46728, -3410)), 2000);
		}

		@Override
		public void objectLeaved(L2Zone zone, L2Object object)
		{
			if(object == null)
				return;
			L2Player player = object.getPlayer();
			if(_status > 1 && player != null && player.getTeam() > 0 && players_list.contains(player.getStoredId()))
			{
				double angle = Util.convertHeadingToDegree(object.getHeading()); // угол в градусах
				double radian = Math.toRadians(angle - 90); // угол в радианах
				int x = (int) (object.getX() + 50 * Math.sin(radian));
				int y = (int) (object.getY() - 50 * Math.cos(radian));
				int z = object.getZ();
				ThreadPoolManager.getInstance().scheduleGeneral(new TeleportTask((L2Character) object, new Location(x, y, z)), 3000);
			} else if (player != null && (player.getTeam() == 0 || !players_list.contains(player.getStoredId()))) {
                removePlayer(player, true);
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

		public void run()
		{
			target.stopStunning();
			target.teleToLocation(loc);
		}
	}

	private static void removePlayer(L2Player player, boolean teleToTown)
	{
		if(player != null)
		{
			players_list.remove(player.getStoredId());
			_topKills.remove(player.getStoredId());
			player.setTeam(0, false);
            if(teleToTown) {
                player.teleToClosestTown();
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
	
	private static void spawnNpcs()
	{
		L2NpcTemplate template;
		L2Spawn spawn;
		if(_spawnNpcs)
		{
			for(int[] npc : npcs)
			{
				template = NpcTable.getTemplate(npc[0]);
				if (template==null)
					continue;
				try {
					spawn = new L2Spawn(template);
				spawn.setLocx(npc[1]);
				spawn.setLocy(npc[2]);
				spawn.setLocz(npc[3]);
				spawn.setHeading(npc[4]);
				_spawns.add(spawn.doSpawn(true));
				} catch (ClassNotFoundException e) {
					e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
				}
			}
		}
	}

	private static void despawnNpcs()
	{
		for(L2NpcInstance npc : _spawns)
			npc.deleteMe();
		_spawns.clear();
	}

	public static void saveBuffList()
	{
		L2Effect skill[];
		for(L2Player player : getPlayers(players_list))
			if(player != null)
				{
					skill = player.getEffectList().getAllFirstEffects();
					if (skill.length==0)
						continue;
					for(L2Effect effect : skill)
					{
						if (!_saveBuffList.containsKey(player.getStoredId()))
							_saveBuffList.put(player.getStoredId(), new GArray<L2Effect>());

						for(int id : _listAllowSaveBuffs)
							if (effect.getSkill().getId()==id)
								_saveBuffList.get(player.getStoredId()).add(effect);
					}
				}
	}

	public static void restoreBuffList()
	{
		L2Player player;
		for(long objId : _saveBuffList.keySet())
		{
			player = L2ObjectsStorage.getAsPlayer(objId);
			ThreadPoolManager.getInstance().scheduleGeneral(new restoreBuffListForPlayer(player), 100);
		}
	}

	public static class restoreBuffListForPlayer implements Runnable
	{
		L2Player player;

		restoreBuffListForPlayer(L2Player player)
		{
			this.player = player;
		}

		public void run()
		{
			if (player == null)
				return;

			GArray<L2Effect> effects = _saveBuffList.get(player.getStoredId());

			if (effects!=null && effects.size()>0)
			{
				for(L2Effect effect : effects)
				{

                	for (EffectTemplate et : effect.getSkill().getEffectTemplates())
					{
                		Env env = new Env(player, player, effect.getSkill());
                		env.value = Integer.MAX_VALUE;
                		L2Effect e = et.getEffect(env);
                		e.setPeriod(effect.getPeriod());// 3 часа
                		e.getEffected().getEffectList().addEffect(e);
					}

					try{Thread.sleep(120);}
					catch(Exception e) {}
				}
			}
			player.setCurrentCp(player.getMaxCp());
			player.setCurrentHp(player.getMaxHp(), true);
			player.setCurrentMp(player.getMaxMp());
		}
	}

	public static class buffPlayer implements Runnable
	{
		L2Player player;

		buffPlayer(L2Player player)
		{
			this.player = player;
		}

		public void run()
		{
			if (player == null)
				return;
			L2Skill skill;
			for (int[] buff : _listBuff[player.isMageClass() ? 1 : 0])
			{
				skill = SkillTable.getInstance().getInfo(buff[0], buff[1]);
                for (EffectTemplate et : skill.getEffectTemplates())
				{
                	Env env = new Env(player, player, skill);
                	env.value = Integer.MAX_VALUE;
                	L2Effect e = et.getEffect(env);
                	e.setPeriod(600000);// 3 часа
                	e.getEffected().getEffectList().addEffect(e);
                }
				try{Thread.sleep(120);}
				catch(Exception e) {}
			}
		}
	}


	public static boolean canUseItem(L2Player player, L2ItemInstance item)
	{
		if (ALLOW_RESTRICT_ITEMS && (players_list.contains(player.getStoredId())))
		{
			for (int restrict_id : RESTRICT_ITEMS)
				if (item.getItemId()==restrict_id) return false;
		}
		return true;
	}

	public static boolean useSkill(L2Character player, L2Character target, L2Skill skill)
	{
		if (player instanceof L2Player && (target instanceof L2Player || target==null) && players_list.contains(player.getStoredId()))
			if (ALLOW_RESTRICT_SKILLS)
			{
				for (int[] restrict : RESTRICT_SKILLS)
					if (skill.getId() == restrict[0])
					{
						if (restrict[1]==0 && (target == null || target.equals(player)))
							return true;
						else if (restrict[1]==1 && (target.getTeam() == player.getTeam()))
							return true;

						return false;
					}
			}
		return true;
	}

	public static boolean canJoinParty(L2Player player, L2Player target)
	{
		return !players_list.contains(player.getStoredId());
	}

	public static class timer implements Runnable
	{
		int time;

		timer(int time)
		{
			this.time = time;
		}

		public void run()
		{
			int sec = 0;
			String message = "";
			_log.info("Time: " + time);
			while (time>0 && _status==2)
			{
				sec = time - ((int) time/60) * 60;
				for (L2Player player : getPlayers(players_list))
				{
					if (sec<10)
						message = " Осталось минут: " + Integer.toString(time/60) + ":0" + Integer.toString(sec) + " ";
					else
						message = " Осталось минут: " + Integer.toString(time/60) + ":" + Integer.toString(sec) + " ";
					message+= "\n Вы убили: " + (_topKills.get(player.getStoredId())==null ? 0 : _topKills.get(player.getStoredId())) + " ";
					player.sendPacket(new ExShowScreenMessage(message, 2000, ExShowScreenMessage.ScreenMessageAlign.BOTTOM_RIGHT, false));
				}
				try
				{Thread.sleep(1000);}
				catch(Exception e)
				{e.printStackTrace();}


				time--;
			}
			endBattle();
		}

	}

	public static boolean sameIp(L2Player player)
	{
		L2Player part;
		for (long objId : players_list)
		{
			part = L2ObjectsStorage.getAsPlayer(objId);
			if (part == null) continue;

			if (player.getNetConnection().getIpAddr().startsWith(part.getNetConnection().getIpAddr()))
				return true;
		}
		return false;
	}

}