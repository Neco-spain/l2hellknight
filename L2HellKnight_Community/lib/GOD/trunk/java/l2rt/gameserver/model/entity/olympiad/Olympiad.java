package l2rt.gameserver.model.entity.olympiad;

import javolution.util.FastMap;
import l2rt.Config;
import l2rt.common.ThreadPoolManager;
import l2rt.extensions.multilang.CustomMessage;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.instancemanager.ServerVariables;
import l2rt.gameserver.instancemanager.ZoneManager;
import l2rt.gameserver.model.L2Party;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Zone;
import l2rt.gameserver.model.base.ClassId;
import l2rt.gameserver.model.entity.Hero;
import l2rt.gameserver.model.instances.L2DoorInstance;
import l2rt.gameserver.model.instances.L2OlympiadManagerInstance;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.tables.DoorTable;
import l2rt.gameserver.templates.StatsSet;
import l2rt.util.GArray;
import l2rt.util.GCSArray;
import l2rt.util.Location;
import l2rt.util.MultiValueIntegerMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Properties;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

public class Olympiad
{
	public static final Logger _log = Logger.getLogger(Olympiad.class.getName());

	public static FastMap<Integer, StatsSet> _nobles;
	public static FastMap<Integer, Integer> _noblesRank;
	public static GArray<StatsSet> _heroesToBe;
	public static GCSArray<Integer> _nonClassBasedRegisters = new GCSArray<Integer>();
	public static MultiValueIntegerMap _classBasedRegisters = new MultiValueIntegerMap();
	public static GCSArray<Integer> _teamRandomBasedRegisters = new GCSArray<Integer>();
	public static MultiValueIntegerMap _teamBasedRegisters = new MultiValueIntegerMap();

	public static final int DEFAULT_POINTS = 18;
	private static final int WEEKLY_POINTS = 3;

	private static final String OLYMPIAD_DATA_FILE = "config/olympiad.ini";
	public static final String OLYMPIAD_HTML_PATH = "data/html/olympiad/";
	public static final String OLYMPIAD_LOAD_NOBLES = "SELECT * FROM `olympiad_nobles`";
	public static final String OLYMPIAD_SAVE_NOBLES = "REPLACE INTO `olympiad_nobles` (`char_id`, `class_id`, `char_name`, `olympiad_points`, `olympiad_points_past`, `olympiad_points_past_static`, `competitions_done`, `competitions_win`, `competitions_loose`) VALUES (?,?,?,?,?,?,?,?,?)";
	public static final String OLYMPIAD_GET_HEROS = "SELECT `char_id`, `char_name` FROM `olympiad_nobles` WHERE `class_id` = ? AND `competitions_done` >= 9 AND `competitions_win` > 0 ORDER BY `olympiad_points` DESC, `competitions_win` DESC, `competitions_done` DESC";
	public static final String OLYMPIAD_GET_HEROS_SOULHOUND = "SELECT `char_id`, `char_name` FROM `olympiad_nobles` WHERE `class_id` IN (?, 133) AND `competitions_done` >= 9 AND `competitions_win` > 0 ORDER BY `olympiad_points` DESC, `competitions_win` DESC, `competitions_done` DESC";
	public static final String GET_ALL_CLASSIFIED_NOBLESS = "SELECT `char_id` FROM `olympiad_nobles` ORDER BY olympiad_points_past_static DESC";
	public static final String GET_EACH_CLASS_LEADER = "SELECT `char_name` FROM `olympiad_nobles` WHERE `class_id` = ? AND `olympiad_points_past_static` != 0 ORDER BY `olympiad_points_past_static` DESC LIMIT 10";
	public static final String GET_EACH_CLASS_LEADER_SOULHOUND = "SELECT `char_name` FROM `olympiad_nobles` WHERE `class_id` IN (?, 133) AND `olympiad_points_past_static` != 0 ORDER BY `olympiad_points_past_static` DESC LIMIT 10";
	public static final String OLYMPIAD_CALCULATE_LAST_PERIOD = "UPDATE `olympiad_nobles` SET `olympiad_points_past` = `olympiad_points`, `olympiad_points_past_static` = `olympiad_points` WHERE `competitions_done` >= 9";
	public static final String OLYMPIAD_CLEANUP_NOBLES = "UPDATE `olympiad_nobles` SET `olympiad_points` = " + DEFAULT_POINTS + ", `competitions_done` = 0, `competitions_win` = 0, `competitions_loose` = 0";

	public static final String CHAR_ID = "char_id";
	public static final String CLASS_ID = "class_id";
	public static final String CHAR_NAME = "char_name";
	public static final String POINTS = "olympiad_points";
	public static final String POINTS_PAST = "olympiad_points_past";
	public static final String POINTS_PAST_STATIC = "olympiad_points_past_static";
	public static final String COMP_DONE = "competitions_done";
	public static final String COMP_WIN = "competitions_win";
	public static final String COMP_LOOSE = "competitions_loose";

	public static long _olympiadEnd;
	public static long _validationEnd;
	public static int _period;
	public static long _nextWeeklyChange;
	public static int _currentCycle;
	private static long _compEnd;
	private static Calendar _compStart;
	public static boolean _inCompPeriod;
	public static boolean _isOlympiadEnd;

	private static ScheduledFuture<?> _scheduledOlympiadEnd;
	public static ScheduledFuture<?> _scheduledManagerTask;
	public static ScheduledFuture<?> _scheduledWeeklyTask;
	public static ScheduledFuture<?> _scheduledValdationTask;

	public static final Stadia[] STADIUMS = new Stadia[88];

	public static OlympiadManager _manager;
	private static GArray<L2OlympiadManagerInstance> _npcs = new GArray<L2OlympiadManagerInstance>();

	public static void load()
	{
		_nobles = new FastMap<Integer, StatsSet>().setShared(true);
		_currentCycle = ServerVariables.getInt("Olympiad_CurrentCycle", -1);
		_period = ServerVariables.getInt("Olympiad_Period", -1);
		_olympiadEnd = ServerVariables.getLong("Olympiad_End", -1);
		_validationEnd = ServerVariables.getLong("Olympiad_ValdationEnd", -1);
		_nextWeeklyChange = ServerVariables.getLong("Olympiad_NextWeeklyChange", -1);

		Properties OlympiadProperties = new Properties();
		InputStream is;
		try
		{
			is = new FileInputStream(new File("./" + OLYMPIAD_DATA_FILE));
			OlympiadProperties.load(is);
			is.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		if(_currentCycle == -1)
			_currentCycle = Integer.parseInt(OlympiadProperties.getProperty("CurrentCycle", "1"));
		if(_period == -1)
			_period = Integer.parseInt(OlympiadProperties.getProperty("Period", "0"));
		if(_olympiadEnd == -1)
			_olympiadEnd = Long.parseLong(OlympiadProperties.getProperty("OlympiadEnd", "0"));
		if(_validationEnd == -1)
			_validationEnd = Long.parseLong(OlympiadProperties.getProperty("ValdationEnd", "0"));
		if(_nextWeeklyChange == -1)
			_nextWeeklyChange = Long.parseLong(OlympiadProperties.getProperty("NextWeeklyChange", "0"));

		initStadiums();

		OlympiadDatabase.loadNobles();
		OlympiadDatabase.loadNoblesRank();

		switch(_period)
		{
			case 0:
				if(_olympiadEnd == 0 || _olympiadEnd < Calendar.getInstance().getTimeInMillis())
					OlympiadDatabase.setNewOlympiadEnd();
				else
					_isOlympiadEnd = false;
				break;
			case 1:
				_isOlympiadEnd = true;
				_scheduledValdationTask = ThreadPoolManager.getInstance().scheduleGeneral(new ValidationTask(), getMillisToValidationEnd());
				break;
			default:
				_log.warning("Olympiad System: Omg something went wrong in loading!! Period = " + _period);
				return;
		}

		_log.info("Olympiad System: Loading Olympiad System....");
		if(_period == 0)
			_log.info("Olympiad System: Currently in Olympiad Period");
		else
			_log.info("Olympiad System: Currently in Validation Period");

		_log.info("Olympiad System: Period Ends....");

		long milliToEnd;
		if(_period == 0)
			milliToEnd = getMillisToOlympiadEnd();
		else
			milliToEnd = getMillisToValidationEnd();

		double numSecs = milliToEnd / 1000 % 60;
		double countDown = (milliToEnd / 1000 - numSecs) / 60;
		int numMins = (int) Math.floor(countDown % 60);
		countDown = (countDown - numMins) / 60;
		int numHours = (int) Math.floor(countDown % 24);
		int numDays = (int) Math.floor((countDown - numHours) / 24);

		_log.info("Olympiad System: In " + numDays + " days, " + numHours + " hours and " + numMins + " mins.");

		if(_period == 0)
		{
			_log.info("Olympiad System: Next Weekly Change is in....");

			milliToEnd = getMillisToWeekChange();

			double numSecs2 = milliToEnd / 1000 % 60;
			double countDown2 = (milliToEnd / 1000 - numSecs2) / 60;
			int numMins2 = (int) Math.floor(countDown2 % 60);
			countDown2 = (countDown2 - numMins2) / 60;
			int numHours2 = (int) Math.floor(countDown2 % 24);
			int numDays2 = (int) Math.floor((countDown2 - numHours2) / 24);

			_log.info("Olympiad System: In " + numDays2 + " days, " + numHours2 + " hours and " + numMins2 + " mins.");
		}

		_log.info("Olympiad System: Loaded " + _nobles.size() + " Noblesses");

		if(_period == 0)
			init();
	}

	private static void initStadiums()
	{
		for(L2DoorInstance door : DoorTable.getInstance().getDoors())
			if(door.getDoorName().startsWith("Door.OlympiadStadium"))
			{
				String[] res = door.getDoorName().split("_");
				int id = Integer.valueOf(res[1]);
				Stadia s = STADIUMS[id - 1];
				if(s == null)
					s = new Stadia();
				s.setDoor(door.getDoorId());
				STADIUMS[id - 1] = s;
			}
	}

	public static void init()
	{
		if(_period == 1)
			return;

		_compStart = Calendar.getInstance();
		_compStart.set(Calendar.HOUR_OF_DAY, Config.ALT_OLY_START_TIME);
		_compStart.set(Calendar.MINUTE, Config.ALT_OLY_MIN);
		_compEnd = _compStart.getTimeInMillis() + Config.ALT_OLY_CPERIOD;

		if(_scheduledOlympiadEnd != null)
			_scheduledOlympiadEnd.cancel(true);
		_scheduledOlympiadEnd = ThreadPoolManager.getInstance().scheduleGeneral(new OlympiadEndTask(), getMillisToOlympiadEnd());

		updateCompStatus();

		if(_scheduledWeeklyTask != null)
			_scheduledWeeklyTask.cancel(true);
		_scheduledWeeklyTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new WeeklyTask(), getMillisToWeekChange(), Config.ALT_OLY_WPERIOD);
	}

	public static synchronized boolean registerNoble(L2Player noble, CompType type)
	{
		if(!_inCompPeriod || _isOlympiadEnd)
		{
			noble.sendPacket(Msg.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS);
			return false;
		}

		if(getMillisToOlympiadEnd() <= 600 * 1000)
		{
			noble.sendPacket(Msg.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS);
			return false;
		}

		if(getMillisToCompEnd() <= 600 * 1000)
		{
			noble.sendPacket(Msg.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS);
			return false;
		}
		
		if (noble.getLevel() < 85)
		{
			noble.sendMessage("Ваш уровень должен быть 85 или выше.");
			return false;
		}
		// TODO сделать через системМеседж
		if (!noble.isAwaking())
		{
			noble.sendMessage("Вы должны получить пробужденную силу.");
			return false;
		}

		if(noble.isCursedWeaponEquipped())
		{
			noble.sendMessage(new CustomMessage("l2rt.gameserver.model.entity.Olympiad.Cursed", noble));
			return false;
		}

		StatsSet nobleInfo = _nobles.get(noble.getObjectId());

		if(nobleInfo == null || !noble.isNoble())
		{
			noble.sendPacket(Msg.ONLY_NOBLESS_CAN_PARTICIPATE_IN_THE_OLYMPIAD);
			return false;
		}

		if(noble.getBaseClassId() != noble.getClassId().getId())
		{
			noble.sendPacket(Msg.YOU_CANT_JOIN_THE_OLYMPIAD_WITH_A_SUB_JOB_CHARACTER);
			return false;
		}

		if(getNoblePoints(noble.getObjectId()) < 3)
		{
			noble.sendMessage(new CustomMessage("l2rt.gameserver.model.entity.Olympiad.LessPoints", noble));
			return false;
		}

		if(noble.getOlympiadGameId() > 0)
		{
			noble.sendPacket(Msg.YOU_HAVE_ALREADY_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_AN_EVENT);
			return false;
		}

		int classId = nobleInfo.getInteger(CLASS_ID);

		switch(type)
		{
			case CLASSED:
			{
				if(_classBasedRegisters.containsValue(noble.getObjectId()))
				{
					noble.sendPacket(Msg.YOU_ARE_ALREADY_ON_THE_WAITING_LIST_TO_PARTICIPATE_IN_THE_GAME_FOR_YOUR_CLASS);
					return false;
				}

				_classBasedRegisters.put(classId, noble.getObjectId());
				noble.sendPacket(Msg.YOU_HAVE_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_CLASSIFIED_GAMES);
				break;
			}
			case NON_CLASSED:
			{
				if(_nonClassBasedRegisters.contains(noble.getObjectId()))
				{
					noble.sendPacket(Msg.YOU_ARE_ALREADY_ON_THE_WAITING_LIST_FOR_ALL_CLASSES_WAITING_TO_PARTICIPATE_IN_THE_GAME);
					return false;
				}

				_nonClassBasedRegisters.add(noble.getObjectId());
				noble.sendPacket(Msg.YOU_HAVE_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_NO_CLASS_GAMES);
				break;
			}
			case TEAM_RANDOM:
			{
				if(_teamRandomBasedRegisters.contains(noble.getObjectId()))
				{
					noble.sendPacket(new SystemMessage(SystemMessage.C1_IS_ALREADY_REGISTERED_ON_THE_WAITING_LIST_FOR_THE_NON_CLASS_LIMITED_MATCH_EVENT).addName(noble));
					return false;
				}

				_teamRandomBasedRegisters.add(noble.getObjectId());
				noble.sendPacket(Msg.YOU_HAVE_REGISTERED_ON_THE_WAITING_LIST_FOR_THE_NON_CLASS_LIMITED_TEAM_MATCH_EVENT);
				break;
			}
			case TEAM:
			{
				if(_teamBasedRegisters.containsValue(noble.getObjectId()))
				{
					noble.sendPacket(new SystemMessage(SystemMessage.C1_IS_ALREADY_REGISTERED_ON_THE_WAITING_LIST_FOR_THE_NON_CLASS_LIMITED_MATCH_EVENT).addName(noble));
					return false;
				}

				L2Party party = noble.getParty();
				if(party == null || party.getMemberCount() != 3)
					return false; // TODO message

				for(L2Player member : party.getPartyMembers())
					if(!member.isNoble())
					{
						noble.sendPacket(Msg.ONLY_NOBLESS_CAN_PARTICIPATE_IN_THE_OLYMPIAD);
						return false;
					}

				_teamBasedRegisters.putAll(noble.getObjectId(), party.getPartyMembersObjIds());
				noble.sendPacket(Msg.YOU_HAVE_REGISTERED_ON_THE_WAITING_LIST_FOR_THE_NON_CLASS_LIMITED_TEAM_MATCH_EVENT);
				break;
			}
		}

		return true;
	}

	public static synchronized void logoutPlayer(L2Player player)
	{
		_classBasedRegisters.removeValue(player.getObjectId());
		_nonClassBasedRegisters.remove(new Integer(player.getObjectId()));
		_teamRandomBasedRegisters.remove(new Integer(player.getObjectId()));
		_teamBasedRegisters.removeValue(player.getObjectId());

		OlympiadGame game = getOlympiadGame(player.getOlympiadGameId());
		if(game != null)
			try
			{
				if(!game.logoutPlayer(player) && !game.validated)
					game.endGame(20000, true);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
	}

	public static synchronized boolean unRegisterNoble(L2Player noble)
	{
		if(!_inCompPeriod || _isOlympiadEnd)
		{
			noble.sendPacket(Msg.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS);
			return false;
		}

		if(!noble.isNoble())
		{
			noble.sendPacket(Msg.ONLY_NOBLESS_CAN_PARTICIPATE_IN_THE_OLYMPIAD);
			return false;
		}

		OlympiadGame game = getOlympiadGame(noble.getOlympiadGameId());
		if(game != null)
			try
			{
				if(!game.logoutPlayer(noble) && !game.validated)
					game.endGame(20000, true);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}

		if(!isRegistered(noble))
		{
			noble.sendPacket(Msg.YOU_HAVE_NOT_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_A_GAME);
			return false;
		}

		_classBasedRegisters.removeValue(noble.getObjectId());
		_nonClassBasedRegisters.remove(new Integer(noble.getObjectId()));
		_teamRandomBasedRegisters.remove(new Integer(noble.getObjectId()));
		_teamBasedRegisters.removeValue(noble.getObjectId());

		noble.sendPacket(Msg.YOU_HAVE_BEEN_DELETED_FROM_THE_WAITING_LIST_OF_A_GAME);

		return true;
	}

	private static synchronized void updateCompStatus()
	{
		long milliToStart = getMillisToCompBegin();
		double numSecs = milliToStart / 1000 % 60;
		double countDown = (milliToStart / 1000 - numSecs) / 60;
		int numMins = (int) Math.floor(countDown % 60);
		countDown = (countDown - numMins) / 60;
		int numHours = (int) Math.floor(countDown % 24);
		int numDays = (int) Math.floor((countDown - numHours) / 24);

		_log.info("Olympiad System: Competition Period Starts in " + numDays + " days, " + numHours + " hours and " + numMins + " mins.");
		_log.info("Olympiad System: Event starts/started: " + _compStart.getTime());

		ThreadPoolManager.getInstance().scheduleGeneral(new CompStartTask(), getMillisToCompBegin());
	}

	private static long getMillisToOlympiadEnd()
	{
		return _olympiadEnd - System.currentTimeMillis();
	}

	static long getMillisToValidationEnd()
	{
		if(_validationEnd > System.currentTimeMillis())
			return _validationEnd - System.currentTimeMillis();
		return 10L;
	}

	public static boolean isOlympiadEnd()
	{
		return _isOlympiadEnd;
	}

	public static boolean inCompPeriod()
	{
		return _inCompPeriod;
	}

	private static long getMillisToCompBegin()
	{
		if(_compStart.getTimeInMillis() < Calendar.getInstance().getTimeInMillis() && _compEnd > Calendar.getInstance().getTimeInMillis())
			return 10L;
		if(_compStart.getTimeInMillis() > Calendar.getInstance().getTimeInMillis())
			return _compStart.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
		return setNewCompBegin();
	}

	private static long setNewCompBegin()
	{
		_compStart = Calendar.getInstance();
		_compStart.set(Calendar.HOUR_OF_DAY, Config.ALT_OLY_START_TIME);
		_compStart.set(Calendar.MINUTE, Config.ALT_OLY_MIN);
		_compStart.add(Calendar.HOUR_OF_DAY, 24);
		_compEnd = _compStart.getTimeInMillis() + Config.ALT_OLY_CPERIOD;

		_log.info("Olympiad System: New Schedule @ " + _compStart.getTime());

		return _compStart.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
	}

	public static long getMillisToCompEnd()
	{
		return _compEnd - Calendar.getInstance().getTimeInMillis();
	}

	private static long getMillisToWeekChange()
	{
		if(_nextWeeklyChange > Calendar.getInstance().getTimeInMillis())
			return _nextWeeklyChange - Calendar.getInstance().getTimeInMillis();
		return 10L;
	}

	public static synchronized void addWeeklyPoints()
	{
		if(_period == 1)
			return;
		for(Integer nobleId : _nobles.keySet())
		{
			StatsSet nobleInfo = _nobles.get(nobleId);
			if(nobleInfo != null)
				nobleInfo.set(POINTS, nobleInfo.getInteger(POINTS) + WEEKLY_POINTS);
		}
	}

	public static int getCurrentCycle()
	{
		return _currentCycle;
	}

	public static synchronized void addSpectator(int id, L2Player spectator)
	{
		if(spectator.getOlympiadGameId() != -1 || Olympiad.isRegisteredInComp(spectator))
		{
			spectator.sendPacket(Msg.WHILE_YOU_ARE_ON_THE_WAITING_LIST_YOU_ARE_NOT_ALLOWED_TO_WATCH_THE_GAME);
			return;
		}

		if(_manager == null || _manager.getOlympiadInstance(id) == null || _manager.getOlympiadInstance(id).getStatus() == BattleStatus.Begining || _manager.getOlympiadInstance(id).getStatus() == BattleStatus.Begin_Countdown)
		{
			spectator.sendPacket(Msg.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS);
			return;
		}

		int[] c = ZoneManager.getInstance().getZoneById(L2Zone.ZoneType.OlympiadStadia, 3001 + id, false).getSpawns().get(0);
		int[] c2 = ZoneManager.getInstance().getZoneById(L2Zone.ZoneType.OlympiadStadia, 3001 + id, false).getSpawns().get(1);

		spectator.enterOlympiadObserverMode(new Location(c[0], c[1], c[2]), id);

		_manager.getOlympiadInstance(id).addSpectator(spectator);
	}

	public static synchronized void removeSpectator(int id, L2Player spectator)
	{
		if(_manager == null || _manager.getOlympiadInstance(id) == null)
			return;

		_manager.getOlympiadInstance(id).removeSpectator(spectator);
	}

	public static GCSArray<L2Player> getSpectators(int id)
	{
		if(_manager == null || _manager.getOlympiadInstance(id) == null)
			return null;
		return _manager.getOlympiadInstance(id).getSpectators();
	}

	public static OlympiadGame getOlympiadGame(int gameId)
	{
		if(_manager == null || gameId < 0)
			return null;
		return _manager.getOlympiadGames().get(gameId);
	}

	public static synchronized int[] getWaitingList()
	{
		if(!inCompPeriod())
			return null;

		int[] array = new int[4];
		array[0] = _classBasedRegisters.totalSize();
		array[1] = _nonClassBasedRegisters.size();
		array[2] = _teamRandomBasedRegisters.size();
		array[3] = _teamBasedRegisters.totalSize();

		return array;
	}

	public static synchronized int getNoblessePasses(L2Player player)
	{
		int objId = player.getObjectId();

		StatsSet noble = _nobles.get(objId);
		if(noble == null)
			return 0;

		int points = noble.getInteger(POINTS_PAST);
		if(points == 0) // Уже получил бонус
			return 0;

		int rank = _noblesRank.get(objId);
		switch(rank)
		{
			case 1:
				points = Config.ALT_OLY_RANK1_POINTS;
				break;
			case 2:
				points = Config.ALT_OLY_RANK2_POINTS;
				break;
			case 3:
				points = Config.ALT_OLY_RANK3_POINTS;
				break;
			case 4:
				points = Config.ALT_OLY_RANK4_POINTS;
				break;
			default:
				points = Config.ALT_OLY_RANK5_POINTS;
		}

		if(player.isHero() || Hero.getInstance().isInactiveHero(player.getObjectId()))
			points += Config.ALT_OLY_HERO_POINTS;

		noble.set(POINTS_PAST, 0);
		OlympiadDatabase.saveNobleData(objId);

		return points * Config.ALT_OLY_GP_PER_POINT;
	}

	public static synchronized boolean isRegistered(L2Player noble)
	{
		if(_classBasedRegisters.containsValue(noble.getObjectId()))
			return true;
		if(_nonClassBasedRegisters.contains(noble.getObjectId()))
			return true;
		if(_teamRandomBasedRegisters.contains(noble.getObjectId()))
			return true;
		if(_teamBasedRegisters.containsValue(noble.getObjectId()))
			return true;
		return false;
	}

	public static synchronized boolean isRegisteredInComp(L2Player player)
	{
		if(isRegistered(player))
			return true;
		if(_manager == null || _manager.getOlympiadGames() == null)
			return false;
		for(OlympiadGame g : _manager.getOlympiadGames().values())
			if(g != null && g.isRegistered(player.getObjectId()))
				return true;
		return false;
	}

	/**
	 * Возвращает олимпийские очки за текущий период
	 * @param objId
	 * @return
	 */
	public static synchronized int getNoblePoints(int objId)
	{
		StatsSet noble = _nobles.get(objId);
		if(noble == null)
			return 0;
		return noble.getInteger(POINTS);
	}

	/**
	 * Возвращает олимпийские очки за прошлый период
	 * @param objId
	 * @return
	 */
	public static synchronized int getNoblePointsPast(int objId)
	{
		StatsSet noble = _nobles.get(objId);
		if(noble == null)
			return 0;
		return noble.getInteger(POINTS_PAST);
	}

	public static synchronized int getCompetitionDone(int objId)
	{
		StatsSet noble = _nobles.get(objId);
		if(noble == null)
			return 0;
		return noble.getInteger(COMP_DONE);
	}

	public static synchronized int getCompetitionWin(int objId)
	{
		StatsSet noble = _nobles.get(objId);
		if(noble == null)
			return 0;
		return noble.getInteger(COMP_WIN);
	}

	public static synchronized int getCompetitionLoose(int objId)
	{
		StatsSet noble = _nobles.get(objId);
		if(noble == null)
			return 0;
		return noble.getInteger(COMP_LOOSE);
	}

	public static Stadia[] getStadiums()
	{
		return STADIUMS;
	}

	public static GArray<L2OlympiadManagerInstance> getNpcs()
	{
		return _npcs;
	}

	public static void addOlympiadNpc(L2OlympiadManagerInstance npc)
	{
		_npcs.add(npc);
	}

	public static void changeNobleName(int objId, String newName)
	{
		StatsSet noble = _nobles.get(objId);
		if(noble == null)
			return;
		noble.set(CHAR_NAME, newName);
		OlympiadDatabase.saveNobleData(objId);
	}

	public static String getNobleName(int objId)
	{
		StatsSet noble = _nobles.get(objId);
		if(noble == null)
			return null;
		return noble.getString(CHAR_NAME);
	}

	public static int getNobleClass(int objId)
	{
		StatsSet noble = _nobles.get(objId);
		if(noble == null)
			return 0;
		return noble.getInteger(CLASS_ID);
	}

	public static void manualSetNoblePoints(int objId, int points)
	{
		StatsSet noble = _nobles.get(objId);
		if(noble == null)
			return;
		noble.set(POINTS, points);
		OlympiadDatabase.saveNobleData(objId);
	}

	public static synchronized boolean isNoble(int objId)
	{
		return _nobles.get(objId) != null;
	}

	public static synchronized void addNoble(L2Player noble)
	{
		if(!_nobles.containsKey(noble.getObjectId()))
		{
			int classId = noble.getBaseClassId();
			if(classId < 88) // Если это не 3-я профа, то исправляем со 2-й на 3-ю.
				for(ClassId id : ClassId.values())
					if(id.level() == 3 && id.getParent((byte) 0).getId() == classId)
					{
						classId = id.getId();
						break;
					}

			StatsSet statDat = new StatsSet();
			statDat.set(CLASS_ID, classId);
			statDat.set(CHAR_NAME, noble.getName());
			statDat.set(POINTS, DEFAULT_POINTS);
			statDat.set(POINTS_PAST, 0);
			statDat.set(POINTS_PAST_STATIC, 0);
			statDat.set(COMP_DONE, 0);
			statDat.set(COMP_WIN, 0);
			statDat.set(COMP_LOOSE, 0);
			_nobles.put(noble.getObjectId(), statDat);
			OlympiadDatabase.saveNobleData();
		}
	}

	public static synchronized void removeNoble(L2Player noble)
	{
		_nobles.remove(noble.getObjectId());
		OlympiadDatabase.saveNobleData();
	}
}