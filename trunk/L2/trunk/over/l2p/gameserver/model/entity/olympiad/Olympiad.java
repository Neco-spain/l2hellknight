package l2p.gameserver.model.entity.olympiad;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import l2p.commons.configuration.ExProperties;
import l2p.gameserver.Config;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.dao.OlympiadNobleDAO;
import l2p.gameserver.instancemanager.OlympiadHistoryManager;
import l2p.gameserver.instancemanager.ServerVariables;
import l2p.gameserver.model.GameObjectsStorage;
import l2p.gameserver.model.Party;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Summon;
import l2p.gameserver.model.base.ClassId;
import l2p.gameserver.model.entity.Hero;
import l2p.gameserver.model.entity.Reflection;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.serverpackets.SystemMessage2;
import l2p.gameserver.serverpackets.components.CustomMessage;
import l2p.gameserver.serverpackets.components.SystemMsg;
import l2p.gameserver.templates.InstantZone;
import l2p.gameserver.templates.StatsSet;
import l2p.gameserver.utils.Location;
import l2p.gameserver.utils.MultiValueIntegerMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Olympiad
{
  private static final Logger _log = LoggerFactory.getLogger(Olympiad.class);
  public static Map<Integer, StatsSet> _nobles;
  public static Map<Integer, Integer> _noblesRank;
  public static List<StatsSet> _heroesToBe;
  public static List<Integer> _nonClassBasedRegisters = new CopyOnWriteArrayList();
  public static MultiValueIntegerMap _classBasedRegisters = new MultiValueIntegerMap();
  public static MultiValueIntegerMap _teamBasedRegisters = new MultiValueIntegerMap();
  public static final int TEAM_PARTY_SIZE = 3;
  public static final String OLYMPIAD_HTML_PATH = "olympiad/";
  public static final String CHAR_ID = "char_id";
  public static final String CLASS_ID = "class_id";
  public static final String CHAR_NAME = "char_name";
  public static final String POINTS = "olympiad_points";
  public static final String POINTS_PAST = "olympiad_points_past";
  public static final String POINTS_PAST_STATIC = "olympiad_points_past_static";
  public static final String COMP_DONE = "competitions_done";
  public static final String COMP_WIN = "competitions_win";
  public static final String COMP_LOOSE = "competitions_loose";
  public static final String GAME_CLASSES_COUNT = "game_classes_count";
  public static final String GAME_NOCLASSES_COUNT = "game_noclasses_count";
  public static final String GAME_TEAM_COUNT = "game_team_count";
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
  public static final Stadia[] STADIUMS = new Stadia[Config.OLYMPIAD_STADIAS_COUNT];
  public static OlympiadManager _manager;
  private static List<NpcInstance> _npcs = new ArrayList();

  public static void load()
  {
    _nobles = new ConcurrentHashMap();
    _currentCycle = ServerVariables.getInt("Olympiad_CurrentCycle", -1);
    _period = ServerVariables.getInt("Olympiad_Period", -1);
    _olympiadEnd = ServerVariables.getLong("Olympiad_End", -1L);
    _validationEnd = ServerVariables.getLong("Olympiad_ValdationEnd", -1L);
    _nextWeeklyChange = ServerVariables.getLong("Olympiad_NextWeeklyChange", -1L);

    ExProperties olympiadProperties = Config.load("config/olympiad.properties");

    if (_currentCycle == -1)
      _currentCycle = olympiadProperties.getProperty("CurrentCycle", 1);
    if (_period == -1)
      _period = olympiadProperties.getProperty("Period", 0);
    if (_olympiadEnd == -1L)
      _olympiadEnd = olympiadProperties.getProperty("OlympiadEnd", 0L);
    if (_validationEnd == -1L)
      _validationEnd = olympiadProperties.getProperty("ValdationEnd", 0L);
    if (_nextWeeklyChange == -1L) {
      _nextWeeklyChange = olympiadProperties.getProperty("NextWeeklyChange", 0L);
    }
    initStadiums();

    OlympiadHistoryManager.getInstance();
    OlympiadNobleDAO.getInstance().select();
    OlympiadDatabase.loadNoblesRank();

    switch (_period)
    {
    case 0:
      if ((_olympiadEnd == 0L) || (_olympiadEnd < Calendar.getInstance().getTimeInMillis()))
        OlympiadDatabase.setNewOlympiadEnd();
      else
        _isOlympiadEnd = false;
      break;
    case 1:
      _isOlympiadEnd = true;
      _scheduledValdationTask = ThreadPoolManager.getInstance().schedule(new ValidationTask(), getMillisToValidationEnd());
      break;
    default:
      _log.warn("Olympiad System: Omg something went wrong in loading!! Period = " + _period);
      return;
    }

    _log.info("Olympiad System: Loading Olympiad System....");
    if (_period == 0)
      _log.info("Olympiad System: Currently in Olympiad Period");
    else {
      _log.info("Olympiad System: Currently in Validation Period");
    }
    _log.info("Olympiad System: Period Ends....");
    long milliToEnd;
    long milliToEnd;
    if (_period == 0)
      milliToEnd = getMillisToOlympiadEnd();
    else {
      milliToEnd = getMillisToValidationEnd();
    }
    double numSecs = milliToEnd / 1000L % 60L;
    double countDown = (milliToEnd / 1000L - numSecs) / 60.0D;
    int numMins = (int)Math.floor(countDown % 60.0D);
    countDown = (countDown - numMins) / 60.0D;
    int numHours = (int)Math.floor(countDown % 24.0D);
    int numDays = (int)Math.floor((countDown - numHours) / 24.0D);

    _log.info("Olympiad System: In " + numDays + " days, " + numHours + " hours and " + numMins + " mins.");

    if (_period == 0)
    {
      _log.info("Olympiad System: Next Weekly Change is in....");

      milliToEnd = getMillisToWeekChange();

      double numSecs2 = milliToEnd / 1000L % 60L;
      double countDown2 = (milliToEnd / 1000L - numSecs2) / 60.0D;
      int numMins2 = (int)Math.floor(countDown2 % 60.0D);
      countDown2 = (countDown2 - numMins2) / 60.0D;
      int numHours2 = (int)Math.floor(countDown2 % 24.0D);
      int numDays2 = (int)Math.floor((countDown2 - numHours2) / 24.0D);

      _log.info("Olympiad System: In " + numDays2 + " days, " + numHours2 + " hours and " + numMins2 + " mins.");
    }

    _log.info("Olympiad System: Loaded " + _nobles.size() + " Noblesses");

    if (_period == 0)
      init();
  }

  private static void initStadiums()
  {
    for (int i = 0; i < STADIUMS.length; i++)
      if (STADIUMS[i] == null)
        STADIUMS[i] = new Stadia();
  }

  public static void init()
  {
    if (_period == 1) {
      return;
    }
    _compStart = Calendar.getInstance();
    _compStart.set(11, Config.ALT_OLY_START_TIME);
    _compStart.set(12, Config.ALT_OLY_MIN);
    _compEnd = _compStart.getTimeInMillis() + Config.ALT_OLY_CPERIOD;

    if (_scheduledOlympiadEnd != null)
      _scheduledOlympiadEnd.cancel(false);
    _scheduledOlympiadEnd = ThreadPoolManager.getInstance().schedule(new OlympiadEndTask(), getMillisToOlympiadEnd());

    updateCompStatus();

    if (_scheduledWeeklyTask != null)
      _scheduledWeeklyTask.cancel(false);
    _scheduledWeeklyTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new WeeklyTask(), getMillisToWeekChange(), Config.ALT_OLY_WPERIOD);
  }

  public static synchronized boolean registerNoble(Player noble, CompType type)
  {
    if (noble.getClassId().getLevel() != 4)
    {
      return false;
    }

    if ((!_inCompPeriod) || (_isOlympiadEnd))
    {
      noble.sendPacket(SystemMsg.THE_GRAND_OLYMPIAD_GAMES_ARE_NOT_CURRENTLY_IN_PROGRESS);
      return false;
    }

    if (getMillisToOlympiadEnd() <= 600000L)
    {
      noble.sendPacket(SystemMsg.THE_GRAND_OLYMPIAD_GAMES_ARE_NOT_CURRENTLY_IN_PROGRESS);
      return false;
    }

    if (getMillisToCompEnd() <= 600000L)
    {
      noble.sendPacket(SystemMsg.THE_GRAND_OLYMPIAD_GAMES_ARE_NOT_CURRENTLY_IN_PROGRESS);
      return false;
    }

    if (noble.isCursedWeaponEquipped())
    {
      noble.sendPacket(SystemMsg.YOU_CANNOT_REGISTER_WHILE_IN_POSSESSION_OF_A_CURSED_WEAPON);
      return false;
    }

    StatsSet nobleInfo = (StatsSet)_nobles.get(Integer.valueOf(noble.getObjectId()));

    if (!validPlayer(noble, noble, type))
    {
      return false;
    }

    if (getNoblePoints(noble.getObjectId()) < 3)
    {
      noble.sendMessage(new CustomMessage("l2p.gameserver.model.entity.Olympiad.LessPoints", noble, new Object[0]));
      return false;
    }

    if (noble.getOlympiadGame() != null)
    {
      return false;
    }

    int classId = nobleInfo.getInteger("class_id");

    switch (1.$SwitchMap$l2p$gameserver$model$entity$olympiad$CompType[type.ordinal()])
    {
    case 1:
      _classBasedRegisters.put(Integer.valueOf(classId), Integer.valueOf(noble.getObjectId()));
      noble.sendPacket(SystemMsg.YOU_HAVE_BEEN_REGISTERED_FOR_THE_GRAND_OLYMPIAD_WAITING_LIST_FOR_A_CLASS_SPECIFIC_MATCH);
      break;
    case 2:
      _nonClassBasedRegisters.add(Integer.valueOf(noble.getObjectId()));
      noble.sendPacket(SystemMsg.YOU_ARE_CURRENTLY_REGISTERED_FOR_A_1V1_CLASS_IRRELEVANT_MATCH);
      break;
    case 3:
      Party party = noble.getParty();
      if (party == null)
      {
        noble.sendPacket(SystemMsg.ONLY_A_PARTY_LEADER_CAN_REQUEST_A_TEAM_MATCH);
        return false;
      }

      if (party.getMemberCount() != 3)
      {
        noble.sendPacket(SystemMsg.THE_REQUEST_CANNOT_BE_MADE_BECAUSE_THE_REQUIREMENTS_HAVE_NOT_BEEN_MET);
        return false;
      }

      for (Player member : party.getPartyMembers())
      {
        if (!validPlayer(noble, member, type))
        {
          return false;
        }
      }

      _teamBasedRegisters.putAll(Integer.valueOf(noble.getObjectId()), party.getPartyMembersObjIds());
      noble.sendPacket(SystemMsg.YOU_ARE_CURRENTLY_REGISTERED_FOR_A_3_VS_3_CLASS_IRRELEVANT_TEAM_MATCH);
      break;
    }

    return true;
  }

  private static boolean validPlayer(Player sendPlayer, Player validPlayer, CompType type)
  {
    if (!validPlayer.isNoble())
    {
      sendPlayer.sendPacket(new SystemMessage2(SystemMsg.C1_DOES_NOT_MEET_THE_PARTICIPATION_REQUIREMENTS_ONLY_NOBLESSE_CHARACTERS_CAN_PARTICIPATE_IN_THE_OLYMPIAD).addName(validPlayer));
      return false;
    }

    if (validPlayer.getBaseClassId() != validPlayer.getClassId().getId())
    {
      sendPlayer.sendPacket(new SystemMessage2(SystemMsg.C1_DOES_NOT_MEET_THE_PARTICIPATION_REQUIREMENTS_SUBCLASS_CHARACTER_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD).addName(validPlayer));
      return false;
    }

    int[] ar = getWeekGameCounts(validPlayer.getObjectId());

    switch (1.$SwitchMap$l2p$gameserver$model$entity$olympiad$CompType[type.ordinal()])
    {
    case 1:
      if (_classBasedRegisters.containsValue(Integer.valueOf(validPlayer.getObjectId())))
      {
        sendPlayer.sendPacket(new SystemMessage2(SystemMsg.C1_IS_ALREADY_REGISTERED_ON_THE_CLASS_MATCH_WAITING_LIST).addName(validPlayer));
        return false;
      }

      if (ar[1] != 0)
        break;
      validPlayer.sendPacket(SystemMsg.THE_TOTAL_NUMBER_OF_MATCHES_THAT_CAN_BE_ENTERED_IN_1_WEEK_IS_60_CLASS_IRRELEVANT_INDIVIDUAL_MATCHES_30_SPECIFIC_MATCHES_AND_10_TEAM_MATCHES);
      return false;
    case 2:
      if (_nonClassBasedRegisters.contains(Integer.valueOf(validPlayer.getObjectId())))
      {
        sendPlayer.sendPacket(new SystemMessage2(SystemMsg.C1_IS_ALREADY_REGISTERED_ON_THE_WAITING_LIST_FOR_THE_CLASS_IRRELEVANT_INDIVIDUAL_MATCH).addName(validPlayer));
        return false;
      }
      if (ar[2] != 0)
        break;
      validPlayer.sendPacket(SystemMsg.THE_TOTAL_NUMBER_OF_MATCHES_THAT_CAN_BE_ENTERED_IN_1_WEEK_IS_60_CLASS_IRRELEVANT_INDIVIDUAL_MATCHES_30_SPECIFIC_MATCHES_AND_10_TEAM_MATCHES);
      return false;
    case 3:
      if (_teamBasedRegisters.containsValue(Integer.valueOf(validPlayer.getObjectId())))
      {
        sendPlayer.sendPacket(new SystemMessage2(SystemMsg.C1_IS_ALREADY_REGISTERED_ON_THE_WAITING_LIST_FOR_THE_3_VS_3_CLASS_IRRELEVANT_TEAM_MATCH).addName(validPlayer));
        return false;
      }
      if (ar[3] != 0)
        break;
      validPlayer.sendPacket(SystemMsg.THE_TOTAL_NUMBER_OF_MATCHES_THAT_CAN_BE_ENTERED_IN_1_WEEK_IS_60_CLASS_IRRELEVANT_INDIVIDUAL_MATCHES_30_SPECIFIC_MATCHES_AND_10_TEAM_MATCHES);
      return false;
    }

    if (ar[0] == 0)
    {
      validPlayer.sendPacket(SystemMsg.THE_MAXIMUM_MATCHES_YOU_CAN_PARTICIPATE_IN_1_WEEK_IS_70);
      return false;
    }

    if (isRegisteredInComp(validPlayer))
    {
      sendPlayer.sendPacket(new SystemMessage2(SystemMsg.C1_IS_ALREADY_REGISTERED_ON_THE_MATCH_WAITING_LIST).addName(validPlayer));
      return false;
    }

    return true;
  }

  public static synchronized void logoutPlayer(Player player)
  {
    _classBasedRegisters.removeValue(Integer.valueOf(player.getObjectId()));
    _nonClassBasedRegisters.remove(new Integer(player.getObjectId()));
    _teamBasedRegisters.removeValue(Integer.valueOf(player.getObjectId()));

    OlympiadGame game = player.getOlympiadGame();
    if (game != null)
      try
      {
        if ((!game.logoutPlayer(player)) && (!game.validated))
          game.endGame(20000L, true);
      }
      catch (Exception e)
      {
        _log.error("", e);
      }
  }

  public static synchronized boolean unRegisterNoble(Player noble)
  {
    if ((!_inCompPeriod) || (_isOlympiadEnd))
    {
      noble.sendPacket(SystemMsg.THE_GRAND_OLYMPIAD_GAMES_ARE_NOT_CURRENTLY_IN_PROGRESS);
      return false;
    }

    if (!noble.isNoble())
    {
      noble.sendPacket(SystemMsg.THE_GRAND_OLYMPIAD_GAMES_ARE_NOT_CURRENTLY_IN_PROGRESS);
      return false;
    }

    if (!isRegistered(noble))
    {
      noble.sendPacket(SystemMsg.YOU_ARE_NOT_CURRENTLY_REGISTERED_FOR_THE_GRAND_OLYMPIAD);
      return false;
    }

    OlympiadGame game = noble.getOlympiadGame();
    if (game != null)
    {
      if (game.getStatus() == BattleStatus.Begin_Countdown)
      {
        noble.sendMessage("Now you can't cancel participation in the Grand Olympiad.");
        return false;
      }

      try
      {
        if ((!game.logoutPlayer(noble)) && (!game.validated))
          game.endGame(20000L, true);
      }
      catch (Exception e)
      {
        _log.error("", e);
      }
    }
    _classBasedRegisters.removeValue(Integer.valueOf(noble.getObjectId()));
    _nonClassBasedRegisters.remove(new Integer(noble.getObjectId()));
    _teamBasedRegisters.removeValue(Integer.valueOf(noble.getObjectId()));

    noble.sendPacket(SystemMsg.YOU_HAVE_BEEN_REMOVED_FROM_THE_GRAND_OLYMPIAD_WAITING_LIST);

    return true;
  }

  private static synchronized void updateCompStatus()
  {
    long milliToStart = getMillisToCompBegin();
    double numSecs = milliToStart / 1000L % 60L;
    double countDown = (milliToStart / 1000L - numSecs) / 60.0D;
    int numMins = (int)Math.floor(countDown % 60.0D);
    countDown = (countDown - numMins) / 60.0D;
    int numHours = (int)Math.floor(countDown % 24.0D);
    int numDays = (int)Math.floor((countDown - numHours) / 24.0D);

    _log.info("Olympiad System: Competition Period Starts in " + numDays + " days, " + numHours + " hours and " + numMins + " mins.");
    _log.info("Olympiad System: Event starts/started: " + _compStart.getTime());

    ThreadPoolManager.getInstance().schedule(new CompStartTask(), getMillisToCompBegin());
  }

  private static long getMillisToOlympiadEnd()
  {
    return _olympiadEnd - System.currentTimeMillis();
  }

  static long getMillisToValidationEnd()
  {
    if (_validationEnd > System.currentTimeMillis())
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
    if ((_compStart.getTimeInMillis() < Calendar.getInstance().getTimeInMillis()) && (_compEnd > Calendar.getInstance().getTimeInMillis()))
      return 10L;
    if (_compStart.getTimeInMillis() > Calendar.getInstance().getTimeInMillis())
      return _compStart.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
    return setNewCompBegin();
  }

  private static long setNewCompBegin()
  {
    _compStart = Calendar.getInstance();
    _compStart.set(11, Config.ALT_OLY_START_TIME);
    _compStart.set(12, Config.ALT_OLY_MIN);
    _compStart.add(11, 24);
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
    if (_nextWeeklyChange > Calendar.getInstance().getTimeInMillis())
      return _nextWeeklyChange - Calendar.getInstance().getTimeInMillis();
    return 10L;
  }

  public static synchronized void doWeekTasks()
  {
    if (_period == 1)
      return;
    for (Map.Entry entry : _nobles.entrySet())
    {
      StatsSet set = (StatsSet)entry.getValue();
      Player player = GameObjectsStorage.getPlayer(((Integer)entry.getKey()).intValue());

      if (_period != 1)
        set.set("olympiad_points", set.getInteger("olympiad_points") + Config.OLYMPIAD_POINTS_WEEKLY);
      set.set("game_classes_count", 0);
      set.set("game_noclasses_count", 0);
      set.set("game_team_count", 0);

      if (player != null)
        player.sendPacket(((SystemMessage2)new SystemMessage2(SystemMsg.C1_HAS_EARNED_S2_POINTS_IN_THE_GRAND_OLYMPIAD_GAMES).addName(player)).addInteger(Config.OLYMPIAD_POINTS_WEEKLY));
    }
  }

  public static int getCurrentCycle()
  {
    return _currentCycle;
  }

  public static synchronized void addSpectator(int id, Player spectator)
  {
    if ((spectator.getOlympiadGame() != null) || (isRegistered(spectator)) || (isRegisteredInComp(spectator)))
    {
      spectator.sendPacket(SystemMsg.YOU_MAY_NOT_OBSERVE_A_GRAND_OLYMPIAD_GAMES_MATCH_WHILE_YOU_ARE_ON_THE_WAITING_LIST);
      return;
    }

    if ((_manager == null) || (_manager.getOlympiadInstance(id) == null) || (_manager.getOlympiadInstance(id).getStatus() == BattleStatus.Begining) || (_manager.getOlympiadInstance(id).getStatus() == BattleStatus.Begin_Countdown))
    {
      spectator.sendPacket(SystemMsg.THE_GRAND_OLYMPIAD_GAMES_ARE_NOT_CURRENTLY_IN_PROGRESS);
      return;
    }

    if (spectator.getPet() != null) {
      spectator.getPet().unSummon();
    }
    OlympiadGame game = getOlympiadGame(id);
    List spawns = game.getReflection().getInstancedZone().getTeleportCoords();
    if (spawns.size() < 3)
    {
      Location c1 = (Location)spawns.get(0);
      Location c2 = (Location)spawns.get(1);
      spectator.enterOlympiadObserverMode(new Location((c1.x + c2.x) / 2, (c1.y + c2.y) / 2, (c1.z + c2.z) / 2), game, game.getReflection());
    }
    else {
      spectator.enterOlympiadObserverMode((Location)spawns.get(2), game, game.getReflection());
    }
  }

  public static synchronized void removeSpectator(int id, Player spectator) {
    if ((_manager == null) || (_manager.getOlympiadInstance(id) == null)) {
      return;
    }
    _manager.getOlympiadInstance(id).removeSpectator(spectator);
  }

  public static List<Player> getSpectators(int id)
  {
    if ((_manager == null) || (_manager.getOlympiadInstance(id) == null))
      return null;
    return _manager.getOlympiadInstance(id).getSpectators();
  }

  public static OlympiadGame getOlympiadGame(int gameId)
  {
    if ((_manager == null) || (gameId < 0))
      return null;
    return (OlympiadGame)_manager.getOlympiadGames().get(Integer.valueOf(gameId));
  }

  public static synchronized int[] getWaitingList()
  {
    if (!inCompPeriod()) {
      return null;
    }
    int[] array = new int[3];
    array[0] = _classBasedRegisters.totalSize();
    array[1] = _nonClassBasedRegisters.size();
    array[2] = _teamBasedRegisters.totalSize();

    return array;
  }

  public static synchronized int getNoblessePasses(Player player)
  {
    int objId = player.getObjectId();

    StatsSet noble = (StatsSet)_nobles.get(Integer.valueOf(objId));
    if (noble == null) {
      return 0;
    }
    int points = noble.getInteger("olympiad_points_past");
    if (points == 0) {
      return 0;
    }
    int rank = ((Integer)_noblesRank.get(Integer.valueOf(objId))).intValue();
    switch (rank)
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

    if ((player.isHero()) || (Hero.getInstance().isInactiveHero(player.getObjectId()))) {
      points += Config.ALT_OLY_HERO_POINTS;
    }
    noble.set("olympiad_points_past", 0);
    OlympiadDatabase.saveNobleData(objId);

    return points * Config.ALT_OLY_GP_PER_POINT;
  }

  public static synchronized boolean isRegistered(Player noble)
  {
    if (_classBasedRegisters.containsValue(Integer.valueOf(noble.getObjectId())))
      return true;
    if (_nonClassBasedRegisters.contains(Integer.valueOf(noble.getObjectId()))) {
      return true;
    }
    return _teamBasedRegisters.containsValue(Integer.valueOf(noble.getObjectId()));
  }

  public static synchronized boolean isRegisteredInComp(Player player)
  {
    if (isRegistered(player))
      return true;
    if ((_manager == null) || (_manager.getOlympiadGames() == null))
      return false;
    for (OlympiadGame g : _manager.getOlympiadGames().values())
      if ((g != null) && (g.isRegistered(player.getObjectId())))
        return true;
    return false;
  }

  public static synchronized int getNoblePoints(int objId)
  {
    StatsSet noble = (StatsSet)_nobles.get(Integer.valueOf(objId));
    if (noble == null)
      return 0;
    return noble.getInteger("olympiad_points");
  }

  public static synchronized int getNoblePointsPast(int objId)
  {
    StatsSet noble = (StatsSet)_nobles.get(Integer.valueOf(objId));
    if (noble == null)
      return 0;
    return noble.getInteger("olympiad_points_past");
  }

  public static synchronized int getCompetitionDone(int objId)
  {
    StatsSet noble = (StatsSet)_nobles.get(Integer.valueOf(objId));
    if (noble == null)
      return 0;
    return noble.getInteger("competitions_done");
  }

  public static synchronized int getCompetitionWin(int objId)
  {
    StatsSet noble = (StatsSet)_nobles.get(Integer.valueOf(objId));
    if (noble == null)
      return 0;
    return noble.getInteger("competitions_win");
  }

  public static synchronized int getCompetitionLoose(int objId)
  {
    StatsSet noble = (StatsSet)_nobles.get(Integer.valueOf(objId));
    if (noble == null)
      return 0;
    return noble.getInteger("competitions_loose");
  }

  public static synchronized int[] getWeekGameCounts(int objId)
  {
    int[] ar = new int[4];

    StatsSet noble = (StatsSet)_nobles.get(Integer.valueOf(objId));
    if (noble == null) {
      return ar;
    }
    ar[0] = (Config.GAME_MAX_LIMIT - noble.getInteger("game_classes_count") - noble.getInteger("game_noclasses_count") - noble.getInteger("game_team_count"));
    ar[1] = (Config.GAME_CLASSES_COUNT_LIMIT - noble.getInteger("game_classes_count"));
    ar[2] = (Config.GAME_NOCLASSES_COUNT_LIMIT - noble.getInteger("game_noclasses_count"));
    ar[3] = (Config.GAME_TEAM_COUNT_LIMIT - noble.getInteger("game_team_count"));

    return ar;
  }

  public static Stadia[] getStadiums()
  {
    return STADIUMS;
  }

  public static List<NpcInstance> getNpcs()
  {
    return _npcs;
  }

  public static void addOlympiadNpc(NpcInstance npc)
  {
    _npcs.add(npc);
  }

  public static void changeNobleName(int objId, String newName)
  {
    StatsSet noble = (StatsSet)_nobles.get(Integer.valueOf(objId));
    if (noble == null)
      return;
    noble.set("char_name", newName);
    OlympiadDatabase.saveNobleData(objId);
  }

  public static String getNobleName(int objId)
  {
    StatsSet noble = (StatsSet)_nobles.get(Integer.valueOf(objId));
    if (noble == null)
      return null;
    return noble.getString("char_name");
  }

  public static int getNobleClass(int objId)
  {
    StatsSet noble = (StatsSet)_nobles.get(Integer.valueOf(objId));
    if (noble == null)
      return 0;
    return noble.getInteger("class_id");
  }

  public static void manualSetNoblePoints(int objId, int points)
  {
    StatsSet noble = (StatsSet)_nobles.get(Integer.valueOf(objId));
    if (noble == null)
      return;
    noble.set("olympiad_points", points);
    OlympiadDatabase.saveNobleData(objId);
  }

  public static synchronized boolean isNoble(int objId)
  {
    return _nobles.get(Integer.valueOf(objId)) != null;
  }

  public static synchronized void addNoble(Player noble)
  {
    if (!_nobles.containsKey(Integer.valueOf(noble.getObjectId())))
    {
      int classId = noble.getBaseClassId();
      if (classId < 88) {
        for (ClassId id : ClassId.VALUES) {
          if ((id.level() != 3) || (id.getParent(0).getId() != classId))
            continue;
          classId = id.getId();
          break;
        }
      }
      StatsSet statDat = new StatsSet();
      statDat.set("class_id", classId);
      statDat.set("char_name", noble.getName());
      statDat.set("olympiad_points", Config.OLYMPIAD_POINTS_DEFAULT);
      statDat.set("olympiad_points_past", 0);
      statDat.set("olympiad_points_past_static", 0);
      statDat.set("competitions_done", 0);
      statDat.set("competitions_win", 0);
      statDat.set("competitions_loose", 0);
      statDat.set("game_classes_count", 0);
      statDat.set("game_noclasses_count", 0);
      statDat.set("game_team_count", 0);

      _nobles.put(Integer.valueOf(noble.getObjectId()), statDat);
      OlympiadDatabase.saveNobleData();
    }
  }

  public static synchronized void removeNoble(Player noble)
  {
    _nobles.remove(Integer.valueOf(noble.getObjectId()));
    OlympiadDatabase.saveNobleData();
  }
}