package events.DestructionOfFlag;

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
import l2rt.gameserver.ai.L2PlayableAI;
import l2rt.gameserver.instancemanager.CastleManager;
import l2rt.gameserver.instancemanager.ServerVariables;
import l2rt.gameserver.instancemanager.ZoneManager;
import l2rt.gameserver.model.*;
import l2rt.gameserver.model.L2Zone.ZoneType;
import l2rt.gameserver.model.entity.Hero;
import l2rt.gameserver.model.entity.olympiad.Olympiad;
import l2rt.gameserver.model.entity.residence.Castle;
import l2rt.gameserver.model.entity.siege.territory.TerritorySiege;
import l2rt.gameserver.model.instances.L2MonsterInstance;
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

public class DestructionOfFlag extends Functions implements ScriptFile
{
	private static Logger _log = Logger.getLogger(DestructionOfFlag.class.getName());
    private static final boolean REMOVE_BUFFS = false; //включен ли этот массив или нет
    private static final int[][] BUFFS_TO_REMOVE = { // какие бафы снимать при телепорте на эвент
            {1, 1}, {2}, {3, 1}//{id скила, лвл скила}, {id скила, лвл скила};(но если я не указываю лвл скила {id скила};, то снимаем любой лвл бафа, а если лвл скила указан, то снимает баф(скил) с определенным лвлом) пример:  {1, 1}, {2}, {3, 1}
    };

    private static final int[] REWARD = { //массив по выдачи бонуса {ID предмета, количество; ID предмета, количество поддержка}; возможно несколько бонусов
            57, 2000000000
    };
	
    private static final String[][] startTime = {

	{"2:35", "2:56"},
	{"5:35", "5:56"},
	{"8:35", "8:56"},
	{"11:35", "11:56"},
	{"14:30", "14:46"},
	{"17:30", "17:46"},
	{"19:45", "19:56"},
	{"20:45", "20:56"},
	{"23:30", "23:46"}



    };

	private static int MIN_PLAYERS = 0; // Минимальное кол-во игроков в каждой команде, для начала эвента.

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
												828, 827, 829, 826, 830, 1356, 1355, 1357, 1363};
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

	private static boolean ALLOW_RESTRICT_SKILLS = false;
	private static int[][] RESTRICT_SKILLS = {
			{1218, 0}, // ТОЛЬКО НА СЕБЯ
			{1234, 1}, // ЧЛЕНЫ СВОЕЙ КОМАНДЫ
			{1410, 2} // ЗАПРЕТ НА АТАКУ СВОЕГО ФЛАГА
					 // остальные на всех...
	};

	public static String[] colors= {"00ff00", "ffffff", "00ffff"}; // Цвета для ников Команд (синий, красный, желтый).

	private static boolean ALLOW_RESTRICT_ITEMS = false;   // Включена ли проверка на использование запрещенных предметов?
	private static int[] RESTRICT_ITEMS = {725, 727}; // Сам список запрещенных предметов.
	private static boolean PROTECT_IP_ACTIVE = false;

	private static HashMap<Long, GArray<L2Effect>> _saveBuffList = new HashMap<Long,GArray<L2Effect>>();
	private static GArray<L2NpcInstance> _spawns = new GArray<L2NpcInstance>();

	public class StartTask implements Runnable
	{
        private String endTime;

        public StartTask(String endTime) {
            this.endTime = endTime;
        }
		public void run()
		{
			if(!_active)
			{
				_log.info("DestructionOfFlag: is not Active");
				return;
			}

			if(isPvPEventStarted())
			{
				_log.info("DestructionOfFlag not started: another event is already running");
				return;

			}
			if(TerritorySiege.isInProgress())
			{
				_log.fine("DestructionOfFlag not started: TerritorySiege in progress");
				return;
			}

			for(Castle c : CastleManager.getInstance().getCastles().values())
				if(c.getSiege() != null && c.getSiege().isInProgress())
				{
					_log.fine("DestructionOfFlag not started: CastleSiege in progress");
					return;
				}
			_log.info("DestructionOfFlag: started, end Time: " + endTime);
			start(new String[] { "-1", "-1", endTime});
		}
	}

	private static List<ScheduledFuture<?>> startTasks = new ArrayList<ScheduledFuture<?>>();

	private static GCSArray<Long> players_list1 = new GCSArray<Long>();
	private static GCSArray<Long> players_list2 = new GCSArray<Long>();
	private static GCSArray<Long> players_list3 = new GCSArray<Long>();
	private static GCSArray<Long> players_list4 = new GCSArray<Long>();

	private static L2MonsterInstance whiteFlag = null;
	private static L2MonsterInstance greenFlag = null;
	private static L2MonsterInstance yellowFlag = null;
	private static L2MonsterInstance blackFlag = null;

	private static boolean _isRegistrationActive = false;
	public static int _status = 0;
	private static int _time_to_start;
	private static int _category;
	private static int _minLevel;
	private static int _maxLevel;
	private static int _autoContinue = 0;

	/**
	  * Настройки эвента, то, что в конфиге - не используется.
	  **/
	private static boolean ALLOW_BUFFS = true;
	private static boolean ALLOW_CLAN_SKILL = true;
	private static boolean ALLOW_HERO_SKILL = true;
	private static int EVENT_DestructionOfFlagItemID = 6673;
	private static float EVENT_DestructionOfFlagItemCOUNT = 3000;
	private static boolean EVENT_DestructionOfFlag_rate = false;
	private static boolean ALLOW_PETS = true; //Разрешить спавн пета?

	private static int TIME_FOR_RES = 5; // Через какое время после смерти персонажа восстанавливают.

	private static L2Zone _zone = ZoneManager.getInstance().getZoneByIndex(ZoneType.battle_zone, 9903, true);
	ZoneListener _zoneListener = new ZoneListener();

	private static Location team1loc = new Location(-82952,-44344,-11496,-11396);
	private static Location team2loc = new Location(-82536,-47016,-11504,-11404);
	private static Location team3loc = new Location(-80680,-44296,-11496,-11396);
	private static Location team4loc = new Location(-78680,-41296,-11496,-11204);

	private static HashMap<Long, ScheduledFuture<?>> _resurrectionList = new HashMap<Long, ScheduledFuture<?>>(); //Хранилище задач на Ressuction.

	public static boolean canSpawnPet(L2Player player)
	{
		if (players_list1.contains(player.getObjectId()) || players_list2.contains(player.getObjectId()))
			if (!ALLOW_PETS) return false;
		return true;
	}
	
	public void onLoad()
	{
		if(_zone != null)
		{
			_zone.getListenerEngine().addMethodInvokedListener(_zoneListener);
		}

        for(String[] s : startTime) {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, Integer.valueOf(s[0].split(":")[0]));
            cal.set(Calendar.MINUTE, Integer.valueOf(s[0].split(":")[1]));
            cal.set(Calendar.SECOND, 0);
            while(cal.getTimeInMillis() < System.currentTimeMillis())
                cal.add(Calendar.DAY_OF_YEAR, 1);
            ScheduledFuture<?> startTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new StartTask(s[1]), cal.getTimeInMillis() - System.currentTimeMillis(), 86400000);
            startTasks.add(startTask);
        }

		_active = ServerVariables.getString("DestructionOfFlag", "off").equalsIgnoreCase("on");

		_log.fine("Loaded Event: DestructionOfFlag");
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
			for(String[] s : startTime) {
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.HOUR_OF_DAY, Integer.valueOf(s[0].split(":")[0]));
				cal.set(Calendar.MINUTE, Integer.valueOf(s[0].split(":")[1]));
				cal.set(Calendar.SECOND, 0);
				while(cal.getTimeInMillis() < System.currentTimeMillis())
					cal.add(Calendar.DAY_OF_YEAR, 1);
				ScheduledFuture<?> startTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new StartTask(s[1]), cal.getTimeInMillis() - System.currentTimeMillis(), 86400000);
				startTasks.add(startTask);
			}
			ServerVariables.set("DestructionOfFlag", "on");
			_log.info("Event 'DestructionOfFlag' activated.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.DestructionOfFlag.AnnounceEventStarted", null);
		}
		else
			player.sendMessage("Event 'DestructionOfFlag' already active.");

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
            startTasks.clear();
			ServerVariables.unset("DestructionOfFlag");
			_log.info("Event 'DestructionOfFlag' deactivated.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.DestructionOfFlag.AnnounceEventStoped", null);
		}
		else
			player.sendMessage("Event 'DestructionOfFlag' not active.");

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
			return Files.read("data/scripts/events/DestructionOfFlag/31225.html", player);
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
			_log.info("Destruction of Flag: Error start, var length: " + var.length);
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
			e.printStackTrace();
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

		players_list1 = new GCSArray<Long>();
		players_list2 = new GCSArray<Long>();

		if(whiteFlag != null)
			whiteFlag.deleteMe();
		if(greenFlag != null)
			greenFlag.deleteMe();


		L2NpcInstance temp;
		try{   

			greenFlag = (L2MonsterInstance) spawn(team1loc, 40000);
			greenFlag.setName("White Flag");
			greenFlag.setLevel((byte) 85);
			greenFlag.setParalyzed(true);
			greenFlag.setCurrentHp(greenFlag.getMaxHp(), true);
			
			whiteFlag = (L2MonsterInstance) spawn(team2loc, 40000);
			whiteFlag.setName("Green Flag");
			whiteFlag.setLevel((byte) 85);
			whiteFlag.setParalyzed(true);
			whiteFlag.setCurrentHp(whiteFlag.getMaxHp(), true);

			yellowFlag = (L2MonsterInstance) spawn(team3loc, 40000);
			yellowFlag.setName("Yellow Flag");
			yellowFlag.setLevel((byte) 85);
			yellowFlag.setParalyzed(true);
			yellowFlag.setCurrentHp(yellowFlag.getMaxHp(), true);

			blackFlag = (L2MonsterInstance) spawn(team4loc, 40000);
			blackFlag.setName("Black Flag");
			blackFlag.setLevel((byte) 85);
			blackFlag.setParalyzed(true);
			blackFlag.setCurrentHp(blackFlag.getMaxHp(), true);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		whiteFlag.decayMe();
		greenFlag.decayMe();
		yellowFlag.decayMe();
		blackFlag.decayMe();

		String[] param = { String.valueOf(_time_to_start), String.valueOf(_minLevel), String.valueOf(_maxLevel) };
		sayToAll("scripts.events.DestructionOfFlag.AnnouncePreStart", param);

		executeTask("events.DestructionOfFlag.DestructionOfFlag", "question", new Object[0], 10000);
		executeTask("events.DestructionOfFlag.DestructionOfFlag", "announce", new Object[]{var[2]}, 60000);
	}

	public static void sayToAll(String address, String[] replacements)
	{
		Announcements.getInstance().announceByCustomMessage(address, replacements, Say2C.CRITICAL_ANNOUNCEMENT);
	}

	public static void question()
	{
		for(L2Player player : L2ObjectsStorage.getAllPlayersForIterate())
			if(player != null && player.getLevel() >= _minLevel && player.getLevel() <= _maxLevel && player.getReflection().getId() <= 0 && !player.isInOlympiadMode())
				player.scriptRequest(new CustomMessage("scripts.events.DestructionOfFlag.AskPlayer", player).toString(), "events.DestructionOfFlag.DestructionOfFlag:addPlayer", new Object[0]);
	}

	public static void announce(String s)
	{
		if(players_list1.isEmpty() || players_list2.isEmpty())
		{
			sayToAll("scripts.events.DestructionOfFlag.AnnounceEventCancelled", null);
			_isRegistrationActive = false;
			_status = 0;
			executeTask("events.DestructionOfFlag.DestructionOfFlag", "autoContinue", new Object[0], 10000);
			return;
		}

		if(_time_to_start > 1)
		{
			_time_to_start--;
			String[] param = { String.valueOf(_time_to_start), String.valueOf(_minLevel), String.valueOf(_maxLevel) };
			sayToAll("scripts.events.DestructionOfFlag.AnnouncePreStart", param);
			executeTask("events.DestructionOfFlag.DestructionOfFlag", "announce", new Object[]{s}, 60000);
		}
		else
		{
			_status = 1;
			_isRegistrationActive = false;
			sayToAll("scripts.events.DestructionOfFlag.AnnounceEventStarting", null);
			executeTask("events.DestructionOfFlag.DestructionOfFlag", "prepare", new Object[]{s}, 5000);
		}
	}

	public void addPlayer()
	{
		L2Player player = (L2Player) getSelf();
		if(player == null || !checkPlayer(player, true))
			return;

		int min = Math.min(Math.min(players_list1.size(), players_list2.size()),  players_list3.size());

		if (min == players_list1.size())
		{
			players_list1.add(player.getStoredId());
		}
		else if (min == players_list2.size())
		{
			players_list2.add(player.getStoredId());
		}
		else 
		{
			players_list3.add(player.getStoredId());
		}
		show(new CustomMessage("scripts.events.DestructionOfFlag.Registered", player), player);
	}

	public static boolean checkPlayer(L2Player player, boolean first)
	{
		if(first && !_isRegistrationActive)
		{
			show(new CustomMessage("scripts.events.Late", player), player);
			return false;
		}

		if(first && playerInCommand(player.getStoredId())>0)
		{
			show(new CustomMessage("scripts.events.DestructionOfFlag.Cancelled", player), player);
			return false;
		}

		if(player.getLevel() < _minLevel || player.getLevel() > _maxLevel)
		{
			show(new CustomMessage("scripts.events.DestructionOfFlag.CancelledLevel", player), player);
			return false;
		}

		if(player.isMounted())
		{
			show(new CustomMessage("scripts.events.DestructionOfFlag.Cancelled", player), player);
			return false;
		}

		if(player.getDuel() != null)
		{
			show(new CustomMessage("scripts.events.DestructionOfFlag.CancelledDuel", player), player);
			return false;
		}

		if(first && playerInCommand(player.getStoredId()) != 0)
		{
			show(new CustomMessage("scripts.events.DestructionOfFlag.CancelledOtherEvent", player), player);
			return false;
		}

		if(player.getOlympiadGameId() > 0 || player.isInZoneOlympiad() || first && Olympiad.isRegistered(player))
		{
			show(new CustomMessage("scripts.events.DestructionOfFlag.CancelledOlympiad", player), player);
			return false;
		}

		if(player.isInParty())
		{
			show(new CustomMessage("scripts.events.DestructionOfFlag.CancelledOtherEvent", player), player);
			return false;
		}

		if(player.isTeleporting())
		{
			show(new CustomMessage("scripts.events.DestructionOfFlag.CancelledTeleport", player), player);
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

	public static void prepare(String s)
	{
		DoorTable.getInstance().getDoor(17160024).openMe();
		DoorTable.getInstance().getDoor(17160023).openMe();
		DoorTable.getInstance().getDoor(17160020).openMe();
		DoorTable.getInstance().getDoor(17160019).openMe();
		DoorTable.getInstance().getDoor(17160022).openMe();
		DoorTable.getInstance().getDoor(17160021).openMe();

		whiteFlag.spawnMe();
		greenFlag.spawnMe();
		yellowFlag.spawnMe();
		blackFlag.spawnMe();

		executeTask("events.DestructionOfFlag.DestructionOfFlag", "ressurectPlayers", new Object[0], 1000);
		executeTask("events.DestructionOfFlag.DestructionOfFlag", "healPlayers", new Object[0], 2000);
		executeTask("events.DestructionOfFlag.DestructionOfFlag", "saveBackCoords", new Object[0], 3000);
		executeTask("events.DestructionOfFlag.DestructionOfFlag", "paralyzePlayers", new Object[0], 4000);
		executeTask("events.DestructionOfFlag.DestructionOfFlag", "teleportPlayersToColiseum", new Object[0], 5000);
		executeTask("events.DestructionOfFlag.DestructionOfFlag", "go", new Object[]{s}, 60000);

		sayToAll("scripts.events.DestructionOfFlag.AnnounceFinalCountdown", null);
	}

	public static void go(String s)
	{
		if (players_list1.size()<MIN_PLAYERS || players_list2.size()<MIN_PLAYERS || players_list3.size()<MIN_PLAYERS)
		{
			Announcements.getInstance().announceToAll("DestructionOfFlag: эвент завершен, не было набрано минимальное кол-во участников.");
			executeTask("events.DestructionOfFlag.DestructionOfFlag", "autoContinue", new Object[0], 1000);
			return;
		}

		_status = 2;
		upParalyzePlayers();
		clearArena();
		sayToAll("scripts.events.DestructionOfFlag.AnnounceFight", null);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, Integer.valueOf(s.split(":")[0]));
        cal.set(Calendar.MINUTE, Integer.valueOf(s.split(":")[1]));
        cal.set(Calendar.SECOND, 0);
        while(cal.getTimeInMillis() < System.currentTimeMillis())
            cal.add(Calendar.DAY_OF_YEAR, 1);
		ThreadPoolManager.getInstance().scheduleGeneral(new timer((int) (cal.getTimeInMillis() - System.currentTimeMillis()) / 1000), 0);
	}

	public static void endBattle(int win)
	{
		if (_status==0) return;
		_status = 0;

		if(whiteFlag != null)
		{
			whiteFlag.deleteMe();
			whiteFlag = null;
		}

		if(greenFlag != null)
		{
			greenFlag.deleteMe();
			greenFlag = null;
		}

		if(yellowFlag != null)
		{
			yellowFlag.deleteMe();
			yellowFlag = null;
		}

		if(blackFlag != null)
		{
			blackFlag.deleteMe();
			blackFlag = null;
		}

		DoorTable.getInstance().getDoor(17160024).closeMe();
		DoorTable.getInstance().getDoor(17160023).closeMe();
		DoorTable.getInstance().getDoor(17160020).closeMe();
		DoorTable.getInstance().getDoor(17160019).closeMe();
		DoorTable.getInstance().getDoor(17160022).closeMe();
		DoorTable.getInstance().getDoor(17160021).closeMe();

		if(win!=0)
		{
			if (win==1)
			{
				Announcements.getInstance().announceToAll("Победила команда Белых!");
				giveItemsToWinner(win,1);
			}
			else if (win==2)
			{
				Announcements.getInstance().announceToAll("Победила команда Зеленых!");
				giveItemsToWinner(win,1);
			}
			else if (win==3)
			{
				Announcements.getInstance().announceToAll("Победила команда Желтых!");
				giveItemsToWinner(win,1);
			}
			else if (win==4)
			{
				Announcements.getInstance().announceToAll("Победила команда Черных!");
				giveItemsToWinner(win,1);
			}
		}
		else Announcements.getInstance().announceToAll("Победивших нет.");


		sayToAll("scripts.events.DestructionOfFlag.AnnounceEnd", null);
		end();
		_isRegistrationActive = false;
	}

	public static void end()
	{
		executeTask("events.DestructionOfFlag.DestructionOfFlag", "removeAura", new Object[0], 1000);
		executeTask("events.DestructionOfFlag.DestructionOfFlag", "ressurectPlayers", new Object[0], 2000);
		executeTask("events.DestructionOfFlag.DestructionOfFlag", "healPlayers", new Object[0], 3000);
		executeTask("events.DestructionOfFlag.DestructionOfFlag", "teleportPlayersToSavedCoordsAll", new Object[0], 4000);
		executeTask("events.DestructionOfFlag.DestructionOfFlag", "autoContinue", new Object[0], 10000);
		backBuff();
		despawnNpcs();
	}

	public void autoContinue()
	{
		players_list1.clear();
		players_list2.clear();
		players_list3.clear();
		players_list4.clear();
		_saveBuffList.clear();

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

	public static void giveItemsToWinner(int win, double rate)
	{
		if(win==1) {
			for(L2Player player : getPlayers(players_list1)) {
                for(int i=0;i < REWARD.length; i+=2) {
                    addItem(player, REWARD[i], Math.round((EVENT_DestructionOfFlag_rate ? player.getLevel() : 1) * REWARD[i+1] * rate));
                }
            }
        }
		if(win==2) {
			for(L2Player player : getPlayers(players_list2)) {
                               for(int i=0;i < REWARD.length; i+=2) {
                    addItem(player, REWARD[i], Math.round((EVENT_DestructionOfFlag_rate ? player.getLevel() : 1) * REWARD[i+1] * rate));
                }
            }
        }
        if(win==3) {
			for(L2Player player : getPlayers(players_list3)) {
                for(int i=0;i < REWARD.length; i+=2) {
                    addItem(player, REWARD[i], Math.round((EVENT_DestructionOfFlag_rate ? player.getLevel() : 1) * REWARD[i+1] * rate));
                }
            }
        }
    }

	public static void saveBackCoords()
	{
		for(L2Player player : getPlayers(players_list1))
		{
			player.setVar("DestructionOfFlag_backCoords", player.getX() + " " + player.getY() + " " + player.getZ() + " " + player.getReflection().getId());
			player.setVar("DestructionOfFlag_nameColor", Integer.toHexString(player.getNameColor()));
		}
		for(L2Player player : getPlayers(players_list2))
		{
			player.setVar("DestructionOfFlag_backCoords", player.getX() + " " + player.getY() + " " + player.getZ() + " " + player.getReflection().getId());
			player.setVar("DestructionOfFlag_nameColor", Integer.toHexString(player.getNameColor()));
		}
		for(L2Player player : getPlayers(players_list3))
		{
			player.setVar("DestructionOfFlag_backCoords", player.getX() + " " + player.getY() + " " + player.getZ() + " " + player.getReflection().getId());
			player.setVar("DestructionOfFlag_nameColor", Integer.toHexString(player.getNameColor()));
		}
		for(L2Player player : getPlayers(players_list4))
		{
			player.setVar("DestructionOfFlag_backCoords", player.getX() + " " + player.getY() + " " + player.getZ() + " " + player.getReflection().getId());
			player.setVar("DestructionOfFlag_nameColor", Integer.toHexString(player.getNameColor()));
		}

		cleanPlayers();
		clearArena();
	}

	public static void teleportPlayersToColiseum()
	{
		for(L2Player player : getPlayers(players_list1))
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
			Location pos = getLocForPlayer(player.getStoredId());
			if (pos!=null) player.teleToLocation(pos);
				else removePlayer(player);
		}
		for(L2Player player : getPlayers(players_list2))
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
			Location pos = getLocForPlayer(player.getStoredId());
			if (pos!=null) player.teleToLocation(pos);
				else removePlayer(player);
		}
		for(L2Player player : getPlayers(players_list3))
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
			Location pos = getLocForPlayer(player.getStoredId());
			if (pos!=null) player.teleToLocation(pos);
				else removePlayer(player);
		}
	}

	public static void teleportPlayersToSavedCoords(int command)
	{
		switch (command)
		{
			case 1:
				for(L2Player player : getPlayers(players_list1))
					teleportPlayerToSavedCoords(player);
			break;
			case 2:
				for(L2Player player : getPlayers(players_list2))
					teleportPlayerToSavedCoords(player);
			break;
			case 3:
				for(L2Player player : getPlayers(players_list3))
					teleportPlayerToSavedCoords(player);
			break;
			case 4:
				for(L2Player player : getPlayers(players_list4))
					teleportPlayerToSavedCoords(player);
			break;
		}
	}
	
	public static void teleportPlayersToSavedCoordsAll()
	{
		for(L2Player player : getPlayers(players_list1))
			teleportPlayerToSavedCoords(player);
		for(L2Player player : getPlayers(players_list2))
			teleportPlayerToSavedCoords(player);
		for(L2Player player : getPlayers(players_list3))
			teleportPlayerToSavedCoords(player);
		for(L2Player player : getPlayers(players_list4))
			teleportPlayerToSavedCoords(player);
	}

	public static void teleportPlayerToSavedCoords(L2Player player)
	{
		try
		{
			String var = player.getVar("DestructionOfFlag_backCoords");
			String color = player.getVar("DestructionOfFlag_nameColor");
			if (!color.isEmpty())
				player.setNameColor(Integer.decode("0x" + color));
			if(var == null || var.equals(""))
				return;
			String[] coords = var.split(" ");
			if(coords.length != 4)
				return;
			player.teleToLocation(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]), Integer.parseInt(coords[3]));
			player.unsetVar("DestructionOfFlag_backCoords");
			player.unsetVar("DestructionOfFlag_nameColor");
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
		for(L2Player player : getPlayers(players_list1))
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
		for(L2Player player : getPlayers(players_list2))
		{
			player.startAbnormalEffect(AbnormalEffect.HOLD_2);
			player.setParalyzed(true);
			if(player.getPet() != null)
			{
				player.getPet().stopAbnormalEffect(AbnormalEffect.HOLD_2);
				player.getPet().setParalyzed(false);
			}

			if(player.getParty() != null)
				player.getParty().oustPartyMember(player);
		}
		for(L2Player player : getPlayers(players_list3))
		{
			player.startAbnormalEffect(AbnormalEffect.HOLD_2);
			player.setParalyzed(true);
			if(player.getPet() != null)
			{
				player.getPet().stopAbnormalEffect(AbnormalEffect.HOLD_2);
				player.getPet().setParalyzed(false);
			}

			if(player.getParty() != null)
				player.getParty().oustPartyMember(player);
		}
	}

	public static void upParalyzePlayers()
	{
		for(L2Player player : getPlayers(players_list1))
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

		for(L2Player player : getPlayers(players_list2))
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
		for(L2Player player : getPlayers(players_list3))
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
		for(L2Player player : getPlayers(players_list1))
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

		for(L2Player player : getPlayers(players_list2))
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
		for(L2Player player : getPlayers(players_list3))
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
		for(L2Player player : getPlayers(players_list1))
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

		for(L2Player player : getPlayers(players_list2))
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

		for(L2Player player : getPlayers(players_list3))
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
		for(L2Player player : getPlayers(players_list1))
			ressurectPlayer(player);
		for(L2Player player : getPlayers(players_list2))
			ressurectPlayer(player);
		for(L2Player player : getPlayers(players_list3))
			ressurectPlayer(player);
		for(L2Player player : getPlayers(players_list4))
			ressurectPlayer(player);
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
		for(L2Player player : getPlayers(players_list3))
		{
			player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
			player.setCurrentCp(player.getMaxCp());
		}
		for(L2Player player : getPlayers(players_list4))
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
				setTeam(player);
		for(L2Player player : getPlayers(players_list2))
			if(!checkPlayer(player, false))
				removePlayer(player);
			else
				setTeam(player);
		for(L2Player player : getPlayers(players_list3))
			if(!checkPlayer(player, false))
				removePlayer(player);
			else
				setTeam(player);
		for(L2Player player : getPlayers(players_list4))
			if(!checkPlayer(player, false))
				removePlayer(player);
			else
				setTeam(player);
	}

	public static void clearArena()
	{
		for(L2Object obj : _zone.getObjects())
			if(obj != null)
			{
				L2Player player = obj.getPlayer();
				if(player != null && playerInCommand(player.getStoredId())==0)
					player.teleToLocation(147451, 46728, -3410);
			}
	}

	public static void doDie(L2Character self, L2Character killer)
	{
		if (_status<=1 || self==null) return;

		if(self.isPlayer() && playerInCommand(self.getStoredId())>0)
		{
			self.sendMessage("Через " + TIME_FOR_RES + " секунд вы будите восстановлены.");
			_resurrectionList.put(self.getStoredId(), executeTask("events.DestructionOfFlag.DestructionOfFlag", "resurrectAtBase", new Object[]{(L2Player) self}, TIME_FOR_RES * 100));
		}

		if (self instanceof L2MonsterInstance && (self == greenFlag || self == whiteFlag || self == yellowFlag || self == blackFlag))
		{
			lossTeam((L2MonsterInstance) self);
		}
	}

	public static void resurrectAtBase(L2Player player)
	{
		if(playerInCommand(player.getStoredId()) <= 0)
			return;
		if(player.isDead())
			ressurectPlayer(player);
		
		Location pos = getLocForPlayer(player.getStoredId());
		if (pos!=null) player.teleToLocation(pos);
			else removePlayer(player);

		if (!ALLOW_BUFFS) ThreadPoolManager.getInstance().scheduleGeneral(new buffPlayer(player), 0);
			else ThreadPoolManager.getInstance().scheduleGeneral(new restoreBuffListForPlayer(player), 0);
	}

	public static Location OnEscape(L2Player player)
	{
		if(_status > 1 && player != null && playerInCommand(player.getStoredId()) > 0)
			removePlayer(player);
		return null;
	}

	public static void OnPlayerExit(L2Player player)
	{
		if(player == null || playerInCommand(player.getStoredId()) < 1)
			return;

		// Вышел или вылетел во время регистрации
		if(_status == 0 && _isRegistrationActive && playerInCommand(player.getStoredId()) > 0)
		{
			removePlayer(player);
			return;
		}

		// Вышел или вылетел во время телепортации
		if(_status == 1 && playerInCommand(player.getStoredId()) > 0)
		{
			removePlayer(player);
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
			if(_status > 0 && player != null && playerInCommand(player.getStoredId())==0)
				ThreadPoolManager.getInstance().scheduleGeneral(new TeleportTask((L2Character) object, new Location(147451, 46728, -3410)), 3000);
		}

		@Override
		public void objectLeaved(L2Zone zone, L2Object object)
		{
			if(object == null)
				return;
			L2Player player = object.getPlayer();
			if(_status > 1 && player != null && playerInCommand(player.getStoredId()) > 0 && playerInCommand(player.getStoredId())>0)
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

		public void run()
		{
			target.stopStunning();
			target.teleToLocation(loc);
		}
	}

	private static void removePlayer(L2Player player)
	{
		players_list1.remove(player.getStoredId());
		players_list2.remove(player.getStoredId());
		players_list3.remove(player.getStoredId());
		players_list4.remove(player.getStoredId());
		teleportPlayerToSavedCoords(player);
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
		for(L2Player player : getPlayers(players_list1))
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


		for(L2Player player : getPlayers(players_list2))
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

			player.getEffectList().stopAllEffects();

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

					try{Thread.sleep(150);}
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
				try{Thread.sleep(150);}
				catch(Exception e) {}
			}
			player.setCurrentCp(player.getMaxCp());
			player.setCurrentHp(player.getMaxHp(), true);
			player.setCurrentMp(player.getMaxMp());
		}
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
			int sec;
			String message;
			while (time>0 && _status==2)
			{
				sec = time - (time /60) * 60;
				for (L2Player player : getPlayers(players_list1))
				{
					if (sec<10)
						message = " Осталось минут: " + Integer.toString(time/60) + ":0" + Integer.toString(sec) + " ";
					else
						message = " Осталось минут: " + Integer.toString(time/60) + ":" + Integer.toString(sec) + " ";
					if (greenFlag!=null) message+= "\n Green Flag: " + greenFlag.getCurrentHp() + " Hp ";
					if (whiteFlag!=null) message+= "\n White Flag: " + whiteFlag.getCurrentHp() + " Hp ";
					if (yellowFlag!=null) message+= "\n Yellow Flag: " + yellowFlag.getCurrentHp() + " Hp ";
					if (blackFlag!=null) message+= "\n Black Flag: " + blackFlag.getCurrentHp() + " Hp ";
					player.sendPacket(new ExShowScreenMessage(message, 2000, ExShowScreenMessage.ScreenMessageAlign.BOTTOM_RIGHT, false));
				}

				for (L2Player player : getPlayers(players_list2))
				{
					if (sec<10)
						message = " Осталось минут: " + Integer.toString(time/60) + ":0" + Integer.toString(sec) + " ";
					else
						message = " Осталось минут: " + Integer.toString(time/60) + ":" + Integer.toString(sec) + " ";
					if (whiteFlag!=null) message+= "\n White Flag: " + whiteFlag.getCurrentHp() + " Hp ";
					if (greenFlag!=null) message+= "\n Green Flag: " + greenFlag.getCurrentHp() + " Hp ";
					if (yellowFlag!=null) message+= "\n Yellow Flag: " + yellowFlag.getCurrentHp() + " Hp ";
					if (blackFlag!=null) message+= "\n Black Flag: " + blackFlag.getCurrentHp() + " Hp ";
					player.sendPacket(new ExShowScreenMessage(message, 2000, ExShowScreenMessage.ScreenMessageAlign.BOTTOM_RIGHT, false));
				}

				for (L2Player player : getPlayers(players_list3))
				{
					if (sec<10)
						message = " Осталось минут: " + Integer.toString(time/60) + ":0" + Integer.toString(sec) + " ";
					else
						message = " Осталось минут: " + Integer.toString(time/60) + ":" + Integer.toString(sec) + " ";
					if (blackFlag!=null) message+= "\n Black Flag: " + blackFlag.getCurrentHp() + " Hp ";
					if (yellowFlag!=null) message+= "\n Yellow Flag: " + yellowFlag.getCurrentHp() + " Hp ";
					if (greenFlag!=null) message+= "\n Green Flag: " + greenFlag.getCurrentHp() + " Hp ";
					if (whiteFlag!=null) message+= "\n White Flag: " + whiteFlag.getCurrentHp() + " Hp ";
					player.sendPacket(new ExShowScreenMessage(message, 2000, ExShowScreenMessage.ScreenMessageAlign.BOTTOM_RIGHT, false));
				}
				try
				{Thread.sleep(1000);}
				catch(Exception e)
				{e.printStackTrace();}


				time--;
			}
			endBattle(0);
		}

	}

	public static int playerInCommand(long objectId)
	{
		return players_list1.contains(objectId) ? 1 : players_list2.contains(objectId) ? 2 : players_list3.contains(objectId) ? 3 : 0;
	}

	public static Location getLocForPlayer(long objectId)
	{
		switch (playerInCommand(objectId))
		{
			case 1: return (Rnd.coordsRandomize(team1loc, 50, 200));
			case 2: return (Rnd.coordsRandomize(team2loc, 50, 200));
			case 3: return (Rnd.coordsRandomize(team3loc, 50, 200));
			case 4: return (Rnd.coordsRandomize(team4loc, 50, 200));
			default: return null;
		}
	}

	public static void setTeam(L2Player player)
	{
		int command = playerInCommand(player.getStoredId());
		if (command<1 || command>3)
		{
			removePlayer(player);
			return;
		}
		player.setNameColor(Integer.decode("0x" + colors[playerInCommand(player.getStoredId()) - 1]));
	}

	  public static void lossTeam(L2MonsterInstance flag) {
	        if (flag == greenFlag) {
	            lossTeam(players_list1);
	            greenFlag.deleteMe();
	            if (players_list2.isEmpty()) endBattle(2);
	            else if (players_list2.isEmpty()) endBattle(3);
	            else if (players_list2.isEmpty()) endBattle(4);
	            else if (players_list3.isEmpty()) endBattle(2);
	            else if (players_list3.isEmpty()) endBattle(3);
	            else if (players_list3.isEmpty()) endBattle(4);
	            else if (players_list4.isEmpty()) endBattle(2);
	            else if (players_list4.isEmpty()) endBattle(3);
	            else if (players_list4.isEmpty()) endBattle(4);
	        }
	        if (flag == whiteFlag) {
	            lossTeam(players_list2);
	            whiteFlag.deleteMe();
	            if (players_list1.isEmpty()) endBattle(1);
	            else if (players_list1.isEmpty()) endBattle(3);
	            else if (players_list1.isEmpty()) endBattle(4);
	            else if (players_list3.isEmpty()) endBattle(1);
	            else if (players_list3.isEmpty()) endBattle(3);
	            else if (players_list3.isEmpty()) endBattle(4);
	            else if (players_list4.isEmpty()) endBattle(1);
	            else if (players_list4.isEmpty()) endBattle(3);
	            else if (players_list4.isEmpty()) endBattle(4);
	        }
	        if (flag == yellowFlag) {
	            lossTeam(players_list3);
	            yellowFlag.deleteMe();
	            if (players_list1.isEmpty()) endBattle(1);
	            else if (players_list1.isEmpty()) endBattle(2);
	            else if (players_list1.isEmpty()) endBattle(4);
	            else if (players_list2.isEmpty()) endBattle(1);
	            else if (players_list2.isEmpty()) endBattle(2);
	            else if (players_list2.isEmpty()) endBattle(4);
	            else if (players_list4.isEmpty()) endBattle(1);
	            else if (players_list4.isEmpty()) endBattle(2);
	            else if (players_list4.isEmpty()) endBattle(4);
	        }
	        if (flag == blackFlag) {
	            lossTeam(players_list4);
	            blackFlag.deleteMe();
	            if (players_list1.isEmpty()) endBattle(1);
	            else if (players_list1.isEmpty()) endBattle(2);
	            else if (players_list1.isEmpty()) endBattle(3);
	            else if (players_list2.isEmpty()) endBattle(1);
	            else if (players_list2.isEmpty()) endBattle(2);
	            else if (players_list2.isEmpty()) endBattle(3);
	            else if (players_list3.isEmpty()) endBattle(1);
	            else if (players_list3.isEmpty()) endBattle(2);
	            else if (players_list3.isEmpty()) endBattle(3);
	        }
		flag.deleteMe();
	}

	public static void lossTeam(GCSArray<Long> team)
	{
		L2Player player;
		for(long objId : team)
		{
			player = L2ObjectsStorage.getAsPlayer(objId);
			if (player!=null)
			{
				removePlayer(player);
				player.sendMessage("Ваш флаг - уничтожен. Вы проиграли.");
			}
		}
		team.clear();
	}

	public static boolean canJoinParty(L2Player player, L2Player target)
     {

		 return !(playerInCommand(player.getStoredId()) > 0 || playerInCommand(target.getStoredId()) > 0) || playerInCommand(player.getStoredId()) == playerInCommand(target.getStoredId());
     }



	public static boolean canUseItem(L2Player player, L2ItemInstance item)
	{
		if (ALLOW_RESTRICT_ITEMS && playerInCommand(player.getStoredId())>0)
		{
			for (int restrict_id : RESTRICT_ITEMS)
				if (item.getItemId()==restrict_id) return false;
		}
		return true;
	}

	public static boolean useSkill(L2Character player, L2Character target, L2Skill skill)
	{
		return checkTarget(player,target, skill);
	}

	public static boolean checkTarget(L2Player player, L2Character target)
	{
		return checkTarget(player,target,null);
	}

	public static boolean checkTarget(L2Character character, L2Character target, L2Skill skill)
	{
		if (_status<2)
			return true;

		if (character instanceof L2Player && target!=null && target!=character)
		{
			if (playerInCommand(character.getStoredId())>0)
			{
				if (target instanceof L2MonsterInstance)
				{
					if(getMonsterTeam(target)==playerInCommand(character.getObjectId()))
					{
						_log.info("Monster Team: " + getMonsterTeam(target) + " | Player Team: " + playerInCommand(character.getObjectId()));
						return false;
					}

					return true;
				}

				if (skill!=null)
				{

					if (ALLOW_RESTRICT_SKILLS)
					{
						for (int[] restrict : RESTRICT_SKILLS)
							if (skill.getId() == restrict[0])
							{
								if (restrict[1]==0)
									return character.getStoredId().equals(target.getStoredId());

								if (restrict[1]==1)
									return playerInCommand(character.getStoredId())==playerInCommand(target.getStoredId());
							}
					}

					if (playerInCommand(target.getStoredId())>0)
					{
						switch (skill.getSkillType())
						{
							case BUFF:
							case HEAL:
							case HEAL_PERCENT:
							case BALANCE:
							case COMBATPOINTHEAL:
							case MANAHEAL:
							case MANAHEAL_PERCENT:
								return playerInCommand(character.getStoredId()) == playerInCommand(target.getStoredId());
							default:
								for(L2Character targ : skill.getTargets(character, target, true))
								{
									if (targ instanceof L2Player)
									{
										if (playerInCommand(character.getStoredId()) == playerInCommand(targ.getStoredId()))
											return false;
									}
									else if (getMonsterTeam(targ)==playerInCommand(character.getObjectId()))
										return false;
								}
						}
					}
						
						if (playerInCommand(target.getStoredId()) == 0)
						{
							switch (skill.getSkillType())
							{
								case MDAM:
								case PDAM:
								case DISCORD:
								case AGGRESSION:
								case BLEED:
								case STUN:
								case DEBUFF:
								case CANCEL:
									return playerInCommand(character.getStoredId()) != playerInCommand(target.getStoredId());
								default:
									for(L2Character targ : skill.getTargets(character, target, true))
									{
										if (targ instanceof L2Player)
										{
											if (playerInCommand(character.getStoredId()) != playerInCommand(targ.getStoredId()))
												return false;
										}
										else if (getMonsterTeam(targ)==playerInCommand(character.getObjectId()))
											return false;

									}
							}
						}


					for(L2Character targ : skill.getTargets(character, target, true))
					{
						if (targ instanceof L2Player)
						{
							if (playerInCommand(character.getStoredId()) == playerInCommand(targ.getStoredId()))
								return false;
						}
						else if (getMonsterTeam(targ)==playerInCommand(character.getObjectId()))
							return false;
				    }
				}

				return playerInCommand(character.getStoredId()) != playerInCommand(target.getStoredId());
			}
			else
			{
				if (playerInCommand(target.getStoredId())>0 || getMonsterTeam(target)>0)
					return false;
			}
		}
		return true;
	}

	private static int getMonsterTeam(L2Character monster)
	{
		if (monster.getStoredId().equals(greenFlag.getStoredId())) return 1;
		else if (monster.getStoredId().equals(whiteFlag.getStoredId())) return 2;
		else if (monster.getStoredId().equals(yellowFlag.getStoredId())) return 3;
		else if (monster.getStoredId().equals(blackFlag.getStoredId())) return 4;
		else return 0;
	}

	public static boolean sameIp(L2Player player)
	{
		L2Player part;
		for (long objId : players_list1)
		{
			part = L2ObjectsStorage.getAsPlayer(objId);
			if (part == null) continue;

			if (player.getNetConnection().getIpAddr().equals(part.getNetConnection().getIpAddr()))
				return true;
		}
		for (long objId : players_list2)
		{
			part = L2ObjectsStorage.getAsPlayer(objId);
			if (part == null) continue;

			if (player.getNetConnection().getIpAddr().startsWith(part.getNetConnection().getIpAddr()))
				return true;
		}
		for (long objId : players_list3)
		{
			part = L2ObjectsStorage.getAsPlayer(objId);
			if (part == null) continue;

			if (player.getNetConnection().getIpAddr().startsWith(part.getNetConnection().getIpAddr()))
				return true;
		}
		for (long objId : players_list4)
		{
			part = L2ObjectsStorage.getAsPlayer(objId);
			if (part == null) continue;

			if (player.getNetConnection().getIpAddr().startsWith(part.getNetConnection().getIpAddr()))
				return true;
		}
		return false;
	}
}