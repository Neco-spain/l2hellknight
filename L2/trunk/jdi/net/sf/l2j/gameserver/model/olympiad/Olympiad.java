package net.sf.l2j.gameserver.model.olympiad;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;
import javolution.text.TextBuilder;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.HeroSkillTable;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.instancemanager.OlympiadStadiaManager;
import net.sf.l2j.gameserver.model.L2Augmentation;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.appearance.PcAppearance;
import net.sf.l2j.gameserver.model.actor.instance.L2CubicInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.status.PcStatus;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.model.entity.Hero;
import net.sf.l2j.gameserver.model.entity.TvTEvent;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExAutoSoulShot;
import net.sf.l2j.gameserver.network.serverpackets.ExOlympiadMode;
import net.sf.l2j.gameserver.network.serverpackets.ExOlympiadUserInfoSpectator;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.StatsSet;
import net.sf.l2j.util.L2FastList;
import net.sf.l2j.util.Rnd;
import org.mmocore.network.ISocket;
import org.mmocore.network.MMOConnection;

public class Olympiad
{
  protected static final Logger _log = Logger.getLogger(Olympiad.class.getName());
  private static Olympiad _instance;
  protected static Map<Integer, StatsSet> _nobles;
  protected static L2FastList<StatsSet> _heroesToBe;
  protected static L2FastList<L2PcInstance> _nonClassBasedRegisters;
  protected static Map<Integer, L2FastList<L2PcInstance>> _classBasedRegisters;
  private static final String OLYMPIAD_DATA_FILE = "config/olympiad.txt";
  public static final String OLYMPIAD_HTML_FILE = "data/html/olympiad/";
  private static final String OLYMPIAD_LOAD_NOBLES = "SELECT * from olympiad_nobles";
  private static final String OLYMPIAD_SAVE_NOBLES = "INSERT INTO olympiad_nobles values (?,?,?,?,?)";
  private static final String OLYMPIAD_UPDATE_NOBLES = "UPDATE olympiad_nobles set olympiad_points = ?, competitions_done = ? where char_id = ?";
  private static final String OLYMPIAD_GET_HEROS = "SELECT char_id, char_name from olympiad_nobles where class_id = ? and competitions_done >= 9 order by olympiad_points desc, competitions_done desc";
  private static final String GET_EACH_CLASS_LEADER = "SELECT char_name from olympiad_nobles where class_id = ? order by olympiad_points desc, competitions_done desc";
  private static final String OLYMPIAD_DELETE_ALL = "DELETE from olympiad_nobles";
  private static final int[] HERO_IDS = { 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 131, 132, 133, 134 };

  private static final int COMP_START = Config.ALT_OLY_START_TIME;
  private static final int COMP_MIN = Config.ALT_OLY_MIN;
  private static final long COMP_PERIOD = Config.ALT_OLY_CPERIOD;
  protected static final long BATTLE_PERIOD = Config.ALT_OLY_BATTLE;
  protected static final long BATTLE_WAIT = Config.ALT_OLY_BWAIT;
  protected static final long INITIAL_WAIT = Config.ALT_OLY_IWAIT;
  protected static final long WEEKLY_PERIOD = Config.ALT_OLY_WPERIOD;
  protected static final long VALIDATION_PERIOD = Config.ALT_OLY_VPERIOD;
  private static final int DEFAULT_POINTS = 18;
  protected static final int WEEKLY_POINTS = 3;
  public static final String CHAR_ID = "char_id";
  public static final String CLASS_ID = "class_id";
  public static final String CHAR_NAME = "char_name";
  public static final String POINTS = "olympiad_points";
  public static final String COMP_DONE = "competitions_done";
  protected long _olympiadEnd;
  protected long _validationEnd;
  protected int _period;
  protected long _nextWeeklyChange;
  protected int _currentCycle;
  private long _compEnd;
  private Calendar _compStart;
  protected static boolean _inCompPeriod;
  protected static boolean _isOlympiadEnd;
  protected static boolean _compStarted = false;
  protected static boolean _battleStarted;
  protected static boolean _cycleTerminated;
  protected ScheduledFuture<?> _scheduledCompStart;
  protected ScheduledFuture<?> _scheduledCompEnd;
  protected ScheduledFuture<?> _scheduledOlympiadEnd;
  protected ScheduledFuture<?> _scheduledManagerTask;
  protected ScheduledFuture<?> _scheduledWeeklyTask;
  protected ScheduledFuture<?> _scheduledValdationTask;
  public static final Stadia[] STADIUMS = { new Stadia(-20814, -21189, -3030), new Stadia(-120324, -225077, -3331), new Stadia(-102495, -209023, -3331), new Stadia(-120156, -207378, -3331), new Stadia(-87628, -225021, -3331), new Stadia(-81705, -213209, -3331), new Stadia(-87593, -207339, -3331), new Stadia(-93709, -218304, -3331), new Stadia(-77157, -218608, -3331), new Stadia(-69682, -209027, -3331), new Stadia(-76887, -201256, -3331), new Stadia(-109985, -218701, -3331), new Stadia(-126367, -218228, -3331), new Stadia(-109629, -201292, -3331), new Stadia(-87523, -240169, -3331), new Stadia(-81748, -245950, -3331), new Stadia(-77123, -251473, -3331), new Stadia(-69778, -241801, -3331), new Stadia(-76754, -234014, -3331), new Stadia(-93742, -251032, -3331), new Stadia(-87466, -257752, -3331), new Stadia(-114413, -213241, -3331) };
  protected static OlympiadManager _manager;

  public static Olympiad getInstance()
  {
    if (_instance == null)
      _instance = new Olympiad();
    return _instance;
  }

  public Olympiad()
  {
    try
    {
      load();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    catch (SQLException s)
    {
      s.printStackTrace();
    }

    if (_period == 0) init(); 
  }

  private void load()
    throws IOException, SQLException
  {
    _nobles = new FastMap();

    Properties OlympiadProperties = new Properties();
    InputStream is = new FileInputStream(new File("./config/olympiad.txt"));
    OlympiadProperties.load(is);
    is.close();

    _currentCycle = Integer.parseInt(OlympiadProperties.getProperty("CurrentCycle", "1"));
    _period = Integer.parseInt(OlympiadProperties.getProperty("Period", "0"));
    _olympiadEnd = Long.parseLong(OlympiadProperties.getProperty("OlympiadEnd", "0"));
    _validationEnd = Long.parseLong(OlympiadProperties.getProperty("ValdationEnd", "0"));
    _nextWeeklyChange = Long.parseLong(OlympiadProperties.getProperty("NextWeeklyChange", "0"));

    switch (_period)
    {
    case 0:
      if ((_olympiadEnd == 0L) || (_olympiadEnd < Calendar.getInstance().getTimeInMillis()))
        setNewOlympiadEnd();
      else
        _isOlympiadEnd = false;
      break;
    case 1:
      if (_validationEnd > Calendar.getInstance().getTimeInMillis())
      {
        _isOlympiadEnd = true;

        _scheduledValdationTask = ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
        {
          public void run() {
            _period = 0;
            _currentCycle += 1;
            deleteNobles();
            setNewOlympiadEnd();
            init();
          }
        }
        , getMillisToValidationEnd());
      }
      else
      {
        _currentCycle += 1;
        _period = 0;
        deleteNobles();
        setNewOlympiadEnd();
      }
      break;
    default:
      _log.warning("Olympiad System: Omg something went wrong in loading!! Period = " + _period);
      return;
    }

    try
    {
      Connection con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("SELECT * from olympiad_nobles");
      ResultSet rset = statement.executeQuery();

      while (rset.next())
      {
        StatsSet statDat = new StatsSet();
        int charId = rset.getInt("char_id");
        statDat.set("class_id", rset.getInt("class_id"));
        statDat.set("char_name", rset.getString("char_name"));
        statDat.set("olympiad_points", rset.getInt("olympiad_points"));
        statDat.set("competitions_done", rset.getInt("competitions_done"));
        statDat.set("to_save", false);

        _nobles.put(Integer.valueOf(charId), statDat);
      }

      rset.close();
      statement.close();
      con.close();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    synchronized (this)
    {
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

        _log.info("Olympiad System: " + numDays2 + " days, " + numHours2 + " hours and " + numMins2 + " mins.");
      }

    }

    _log.info("Olympiad System: Loaded " + _nobles.size() + " Nobles");
  }

  protected void init()
  {
    if (_period == 1)
      return;
    _nonClassBasedRegisters = new L2FastList();
    _classBasedRegisters = new FastMap();

    _compStart = Calendar.getInstance();
    _compStart.set(11, COMP_START);
    _compStart.set(12, COMP_MIN);
    _compEnd = (_compStart.getTimeInMillis() + COMP_PERIOD);

    _scheduledOlympiadEnd = ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
    {
      public void run() {
        SystemMessage sm = new SystemMessage(SystemMessageId.OLYMPIAD_PERIOD_S1_HAS_ENDED);
        sm.addNumber(_currentCycle);

        Announcements.getInstance().announceToAll(sm);
        Announcements.getInstance().announceToAll("Olympiad Validation Period has began");

        Olympiad._isOlympiadEnd = true;
        if (_scheduledManagerTask != null)
          _scheduledManagerTask.cancel(true);
        if (_scheduledWeeklyTask != null) {
          _scheduledWeeklyTask.cancel(true);
        }
        Calendar validationEnd = Calendar.getInstance();
        _validationEnd = (validationEnd.getTimeInMillis() + Olympiad.VALIDATION_PERIOD);

        saveNobleData();

        _period = 1;

        sortHerosToBe();

        giveHeroBonus();

        Hero.getInstance().computeNewHeroes(Olympiad._heroesToBe);
        try
        {
          save();
        }
        catch (Exception e) {
          Olympiad._log.warning("Olympiad System: Failed to save Olympiad configuration: " + e);
        }

        _scheduledValdationTask = ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
        {
          public void run() {
            Announcements.getInstance().announceToAll("Olympiad Validation Period has ended");
            _period = 0;
            _currentCycle += 1;
            deleteNobles();
            setNewOlympiadEnd();
            init();
          }
        }
        , getMillisToValidationEnd());
      }
    }
    , getMillisToOlympiadEnd());

    updateCompStatus();
    scheduleWeeklyChange();
  }

  public boolean registerNoble(L2PcInstance noble, boolean classBased)
  {
    if (TvTEvent.isParticipating())
    {
      if (TvTEvent.getParticipantTeamId(noble.getObjectId()) != -1)
      {
        noble.sendMessage("You can't participate to Olympiad with TvT Event mode.");
        return false;
      }
    }

    if (noble._inEventCTF)
    {
      noble.sendMessage("You can't participate to Olympiad with CTF Event mode.");
      return false;
    }

    if (noble.getKarma() > 0)
    {
      noble.sendMessage("You can't participate to Olympiad with karma.");
      return false;
    }

    if (!_inCompPeriod)
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS);
      noble.sendPacket(sm);
      return false;
    }

    if (noble.isCursedWeaponEquiped())
    {
      noble.sendMessage("You can't participate to Olympiad while holding a cursed weapon.");
      return false;
    }

    if (!noble.isNoble())
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.ONLY_NOBLESS_CAN_PARTICIPATE_IN_THE_OLYMPIAD);
      noble.sendPacket(sm);
      return false;
    }

    if (TvTEvent.isPlayerParticipant(noble.getObjectId()))
    {
      noble.sendMessage("You can't join olympiad while participating on TvT Event.");
      return false;
    }

    if (noble.getBaseClass() != noble.getClassId().getId())
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.YOU_CANT_JOIN_THE_OLYMPIAD_WITH_A_SUB_JOB_CHARACTER);
      noble.sendPacket(sm);
      return false;
    }

    if (!_nobles.containsKey(Integer.valueOf(noble.getObjectId())))
    {
      StatsSet statDat = new StatsSet();
      statDat.set("class_id", noble.getClassId().getId());
      statDat.set("char_name", noble.getName());
      statDat.set("olympiad_points", 18);
      statDat.set("competitions_done", 0);
      statDat.set("to_save", true);

      _nobles.put(Integer.valueOf(noble.getObjectId()), statDat);
    }

    if (_classBasedRegisters.containsKey(Integer.valueOf(noble.getClassId().getId())))
    {
      L2FastList classed = (L2FastList)_classBasedRegisters.get(Integer.valueOf(noble.getClassId().getId()));
      for (L2PcInstance partecipant : classed)
      {
        if (partecipant.getObjectId() == noble.getObjectId())
        {
          SystemMessage sm = new SystemMessage(SystemMessageId.YOU_ARE_ALREADY_ON_THE_WAITING_LIST_TO_PARTICIPATE_IN_THE_GAME_FOR_YOUR_CLASS);
          noble.sendPacket(sm);
          return false;
        }
      }
    }

    for (L2PcInstance partecipant : _nonClassBasedRegisters)
    {
      if (partecipant.getObjectId() == noble.getObjectId())
      {
        SystemMessage sm = new SystemMessage(SystemMessageId.YOU_ARE_ALREADY_ON_THE_WAITING_LIST_FOR_ALL_CLASSES_WAITING_TO_PARTICIPATE_IN_THE_GAME);
        noble.sendPacket(sm);
        return false;
      }
    }

    for (L2OlympiadGame g : _manager.getOlympiadGames().values())
    {
      for (L2PcInstance player : g.getPlayers())
      {
        if (player.getObjectId() != noble.getObjectId())
          continue;
        SystemMessage sm = new SystemMessage(SystemMessageId.YOU_ARE_ALREADY_ON_THE_WAITING_LIST_FOR_ALL_CLASSES_WAITING_TO_PARTICIPATE_IN_THE_GAME);
        noble.sendPacket(sm);
        return false;
      }

    }

    if ((classBased) && (getNoblePoints(noble.getObjectId()) < 3))
    {
      noble.sendMessage("Cant register when you have less than 3 points");
      return false;
    }
    if ((!classBased) && (getNoblePoints(noble.getObjectId()) < 5))
    {
      noble.sendMessage("Cant register when you have less than 5 points");
      return false;
    }

    if (classBased)
    {
      if (_classBasedRegisters.containsKey(Integer.valueOf(noble.getClassId().getId())))
      {
        L2FastList classed = (L2FastList)_classBasedRegisters.get(Integer.valueOf(noble.getClassId().getId()));
        classed.add(noble);

        _classBasedRegisters.remove(Integer.valueOf(noble.getClassId().getId()));
        _classBasedRegisters.put(Integer.valueOf(noble.getClassId().getId()), classed);

        SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_CLASSIFIED_GAMES);
        noble.sendPacket(sm);
      }
      else
      {
        L2FastList classed = new L2FastList();
        classed.add(noble);

        _classBasedRegisters.put(Integer.valueOf(noble.getClassId().getId()), classed);

        SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_CLASSIFIED_GAMES);
        noble.sendPacket(sm);
      }

    }
    else
    {
      _nonClassBasedRegisters.add(noble);
      SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_NO_CLASS_GAMES);
      noble.sendPacket(sm);
    }

    return true;
  }

  public boolean isRegistered(L2PcInstance noble)
  {
    if (_nonClassBasedRegisters == null) return false;
    if (_classBasedRegisters == null) return false;
    if (!_nonClassBasedRegisters.contains(noble))
    {
      if (!_classBasedRegisters.containsKey(Integer.valueOf(noble.getClassId().getId())))
      {
        return false;
      }

      L2FastList classed = (L2FastList)_classBasedRegisters.get(Integer.valueOf(noble.getClassId().getId()));
      if (!classed.contains(noble))
      {
        return false;
      }
    }

    return true;
  }

  public boolean unRegisterNoble(L2PcInstance noble)
  {
    if (!_inCompPeriod)
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS);
      noble.sendPacket(sm);
      return false;
    }

    if (!noble.isNoble())
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.ONLY_NOBLESS_CAN_PARTICIPATE_IN_THE_OLYMPIAD);
      noble.sendPacket(sm);
      return false;
    }

    if (!isRegistered(noble))
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_NOT_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_A_GAME);
      noble.sendPacket(sm);
      return false;
    }

    if (_nonClassBasedRegisters.contains(noble)) {
      _nonClassBasedRegisters.remove(noble);
    }
    else {
      L2FastList classed = (L2FastList)_classBasedRegisters.get(Integer.valueOf(noble.getClassId().getId()));
      classed.remove(noble);

      _classBasedRegisters.remove(Integer.valueOf(noble.getClassId().getId()));
      _classBasedRegisters.put(Integer.valueOf(noble.getClassId().getId()), classed);
    }

    for (L2OlympiadGame game : _manager.getOlympiadGames().values())
    {
      if ((game._playerOne.getObjectId() == noble.getObjectId()) || (game._playerTwo.getObjectId() == noble.getObjectId()))
      {
        noble.sendMessage("Cant Unregister whilst you are already selected for a game");
        return false;
      }
    }

    SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_BEEN_DELETED_FROM_THE_WAITING_LIST_OF_A_GAME);
    noble.sendPacket(sm);

    return true;
  }

  public void removeDisconnectedCompetitor(L2PcInstance player)
  {
    if ((_manager == null) || (_manager.getOlympiadInstance(player.getOlympiadGameId()) == null)) return;

    _manager.getOlympiadInstance(player.getOlympiadGameId()).handleDisconnect(player);
  }

  private void updateCompStatus()
  {
    synchronized (this)
    {
      long milliToStart = getMillisToCompBegin();

      double numSecs = milliToStart / 1000L % 60L;
      double countDown = (milliToStart / 1000L - numSecs) / 60.0D;
      int numMins = (int)Math.floor(countDown % 60.0D);
      countDown = (countDown - numMins) / 60.0D;
      int numHours = (int)Math.floor(countDown % 24.0D);
      int numDays = (int)Math.floor((countDown - numHours) / 24.0D);

      _log.info("Olympiad System: Competition Period Starts in " + numDays + " days, " + numHours + " hours and " + numMins + " mins.");

      _log.info("Olympiad System: Event starts/started : " + _compStart.getTime());
    }

    _scheduledCompStart = ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
    {
      public void run() {
        if (isOlympiadEnd()) {
          return;
        }
        Olympiad._inCompPeriod = true;
        Olympiad.OlympiadManager om = new Olympiad.OlympiadManager(Olympiad.this);

        Announcements.getInstance().announceToAll(new SystemMessage(SystemMessageId.THE_OLYMPIAD_GAME_HAS_STARTED));
        Olympiad._log.info("Olympiad System: Olympiad Game Started");

        Thread olyCycle = new Thread(om);
        olyCycle.start();

        _scheduledCompEnd = ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
        {
          public void run() {
            if (isOlympiadEnd()) {
              return;
            }
            Olympiad._inCompPeriod = false;
            Announcements.getInstance().announceToAll(new SystemMessage(SystemMessageId.THE_OLYMPIAD_GAME_HAS_ENDED));
            Olympiad._log.info("Olympiad System: Olympiad Game Ended");
            try
            {
              while (Olympiad._battleStarted)
                try
                {
                  Thread.sleep(60000L);
                } catch (InterruptedException e) {
                }
              save();
            }
            catch (Exception e) {
              Olympiad._log.warning("Olympiad System: Failed to save Olympiad configuration: " + e);
            }

            init();
          }
        }
        , getMillisToCompEnd());
      }
    }
    , getMillisToCompBegin());
  }

  private long getMillisToOlympiadEnd()
  {
    return _olympiadEnd - Calendar.getInstance().getTimeInMillis();
  }

  public void manualSelectHeroes()
  {
    SystemMessage sm = new SystemMessage(SystemMessageId.OLYMPIAD_PERIOD_S1_HAS_ENDED);
    sm.addNumber(_currentCycle);

    Announcements.getInstance().announceToAll(sm);
    Announcements.getInstance().announceToAll("Olympiad Validation Period has began");

    _isOlympiadEnd = true;
    if (_scheduledManagerTask != null)
      _scheduledManagerTask.cancel(true);
    if (_scheduledWeeklyTask != null)
      _scheduledWeeklyTask.cancel(true);
    if (_scheduledOlympiadEnd != null) {
      _scheduledOlympiadEnd.cancel(true);
    }
    Calendar validationEnd = Calendar.getInstance();
    _validationEnd = (validationEnd.getTimeInMillis() + VALIDATION_PERIOD);

    saveNobleData();

    _period = 1;

    sortHerosToBe();

    giveHeroBonus();

    Hero.getInstance().computeNewHeroes(_heroesToBe);
    try
    {
      save();
    }
    catch (Exception e) {
      _log.warning("Olympiad System: Failed to save Olympiad configuration: " + e);
    }

    _scheduledValdationTask = ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
    {
      public void run() {
        Announcements.getInstance().announceToAll("Olympiad Validation Period has ended");
        _period = 0;
        _currentCycle += 1;
        deleteNobles();
        setNewOlympiadEnd();
        init();
      }
    }
    , getMillisToValidationEnd());
  }

  protected long getMillisToValidationEnd()
  {
    if (_validationEnd > Calendar.getInstance().getTimeInMillis())
      return _validationEnd - Calendar.getInstance().getTimeInMillis();
    return 10L;
  }

  public boolean isOlympiadEnd()
  {
    return _isOlympiadEnd;
  }

  protected void setNewOlympiadEnd()
  {
    SystemMessage sm = new SystemMessage(SystemMessageId.OLYMPIAD_PERIOD_S1_HAS_STARTED);
    sm.addNumber(_currentCycle);

    Announcements.getInstance().announceToAll(sm);

    Calendar currentTime = Calendar.getInstance();

    if (Config.ALT_OLY_WEEK)
    {
      currentTime.add(4, 1);
      currentTime.set(7, 2);
    }
    else
    {
      currentTime.add(2, 1);
      currentTime.set(5, 1);
    }
    currentTime.set(9, 0);
    currentTime.set(10, 12);
    currentTime.set(12, 0);
    currentTime.set(13, 0);
    _olympiadEnd = currentTime.getTimeInMillis();

    Calendar nextChange = Calendar.getInstance();
    _nextWeeklyChange = (nextChange.getTimeInMillis() + WEEKLY_PERIOD);

    _isOlympiadEnd = false;
  }

  public boolean inCompPeriod()
  {
    return _inCompPeriod;
  }

  private long getMillisToCompBegin()
  {
    if ((_compStart.getTimeInMillis() < Calendar.getInstance().getTimeInMillis()) && (_compEnd > Calendar.getInstance().getTimeInMillis()))
    {
      return 10L;
    }
    if (_compStart.getTimeInMillis() > Calendar.getInstance().getTimeInMillis()) {
      return _compStart.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
    }
    return setNewCompBegin();
  }

  private long setNewCompBegin()
  {
    _compStart = Calendar.getInstance();
    _compStart.set(11, COMP_START);
    _compStart.set(12, COMP_MIN);
    _compStart.add(11, 24);
    _compEnd = (_compStart.getTimeInMillis() + COMP_PERIOD);

    _log.info("Olympiad System: New Schedule @ " + _compStart.getTime());

    return _compStart.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
  }

  protected long getMillisToCompEnd()
  {
    return _compEnd - Calendar.getInstance().getTimeInMillis();
  }

  private long getMillisToWeekChange()
  {
    if (_nextWeeklyChange > Calendar.getInstance().getTimeInMillis())
      return _nextWeeklyChange - Calendar.getInstance().getTimeInMillis();
    return 10L;
  }

  private void scheduleWeeklyChange()
  {
    _scheduledWeeklyTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new Runnable()
    {
      public void run() {
        addWeeklyPoints();
        Olympiad._log.info("Olympiad System: Added weekly points to nobles");

        Calendar nextChange = Calendar.getInstance();
        _nextWeeklyChange = (nextChange.getTimeInMillis() + Olympiad.WEEKLY_PERIOD);
      }
    }
    , getMillisToWeekChange(), WEEKLY_PERIOD);
  }

  protected synchronized void addWeeklyPoints()
  {
    if (_period == 1) {
      return;
    }
    for (Integer nobleId : _nobles.keySet())
    {
      StatsSet nobleInfo = (StatsSet)_nobles.get(nobleId);
      int currentPoints = nobleInfo.getInteger("olympiad_points");
      currentPoints += 3;
      nobleInfo.set("olympiad_points", currentPoints);

      _nobles.remove(nobleId);
      _nobles.put(nobleId, nobleInfo);
    }
  }

  public String[] getMatchList()
  {
    return _manager == null ? null : _manager.getAllTitles();
  }

  public L2PcInstance[] getPlayers(int Id)
  {
    if ((_manager == null) || (_manager.getOlympiadInstance(Id) == null))
    {
      return null;
    }
    L2PcInstance[] players = _manager.getOlympiadInstance(Id).getPlayers();
    return players;
  }

  public int getCurrentCycle()
  {
    return _currentCycle;
  }

  public static void addSpectator(int id, L2PcInstance spectator)
  {
    for (L2PcInstance player : _nonClassBasedRegisters)
    {
      if (spectator.getObjectId() == player.getObjectId())
      {
        spectator.sendMessage("You are already registered for a competition");
        return;
      }
    }
    for (L2FastList list : _classBasedRegisters.values())
    {
      for (L2PcInstance player : list)
      {
        if (spectator.getObjectId() == player.getObjectId())
        {
          spectator.sendMessage("You are already registered for a competition");
          return;
        }
      }
    }
    if (spectator.getOlympiadGameId() != -1)
    {
      spectator.sendMessage("You are already registered for a competition");
      return;
    }
    if ((_manager == null) || (_manager.getOlympiadInstance(id) == null))
    {
      spectator.sendPacket(new SystemMessage(SystemMessageId.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS));
      return;
    }

    L2PcInstance[] players = _manager.getOlympiadInstance(id).getPlayers();

    if (players == null) return;

    STADIUMS[id].addSpectator(id, spectator);
  }

  public static void removeSpectator(int id, L2PcInstance spectator)
  {
    STADIUMS[id].removeSpectator(spectator);
  }

  public L2FastList<L2PcInstance> getSpectators(int id)
  {
    if ((_manager == null) || (_manager.getOlympiadInstance(id) == null))
      return null;
    return _manager.getOlympiadInstance(id).getSpectators();
  }

  public static void bypassChangeArena(String command, L2PcInstance player)
  {
    String[] commands = command.split(" ");
    int id = Integer.parseInt(commands[1]);
    int arena = getSpectatorArena(player);
    if (arena >= 0)
    {
      removeSpectator(arena, player);
    }
    addSpectator(id, player);
  }

  public static int getSpectatorArena(L2PcInstance player)
  {
    for (int i = 0; i < STADIUMS.length; i++)
    {
      if (STADIUMS[i].getSpectators().contains(player))
        return i;
    }
    return -1;
  }

  public Map<Integer, L2OlympiadGame> getOlympiadGames()
  {
    return _manager.getOlympiadGames();
  }

  public boolean playerInStadia(L2PcInstance player)
  {
    return OlympiadStadiaManager.getInstance().getStadium(player) != null;
  }

  public int[] getWaitingList()
  {
    int[] array = new int[2];

    if (!inCompPeriod()) {
      return null;
    }
    int classCount = 0;

    if (_classBasedRegisters.size() != 0) {
      for (L2FastList classed : _classBasedRegisters.values())
      {
        classCount += classed.size();
      }
    }
    array[0] = classCount;
    array[1] = _nonClassBasedRegisters.size();

    return array;
  }

  protected synchronized void saveNobleData()
  {
    Connection con = null;

    if (_nobles == null) {
      return;
    }
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      for (Integer nobleId : _nobles.keySet())
      {
        StatsSet nobleInfo = (StatsSet)_nobles.get(nobleId);

        int charId = nobleId.intValue();
        int classId = nobleInfo.getInteger("class_id");
        String charName = nobleInfo.getString("char_name");
        int points = nobleInfo.getInteger("olympiad_points");
        int compDone = nobleInfo.getInteger("competitions_done");
        boolean toSave = nobleInfo.getBool("to_save");

        if (toSave)
        {
          PreparedStatement statement = con.prepareStatement("INSERT INTO olympiad_nobles values (?,?,?,?,?)");
          statement.setInt(1, charId);
          statement.setInt(2, classId);
          statement.setString(3, charName);
          statement.setInt(4, points);
          statement.setInt(5, compDone);
          statement.execute();

          statement.close();

          nobleInfo.set("to_save", false);

          _nobles.remove(nobleId);
          _nobles.put(nobleId, nobleInfo);
        }
        else
        {
          PreparedStatement statement = con.prepareStatement("UPDATE olympiad_nobles set olympiad_points = ?, competitions_done = ? where char_id = ?");
          statement.setInt(1, points);
          statement.setInt(2, compDone);
          statement.setInt(3, charId);
          statement.execute();
          statement.close();
        }
      }
    } catch (SQLException e) {
      _log.warning("Olympiad System: Couldnt save nobles info in db");
    } finally {
      try {
        con.close(); } catch (Exception e) { e.printStackTrace(); }
    }
  }

  protected void sortHerosToBe()
  {
    if (_period != 1) return;

    _heroesToBe = new L2FastList();

    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      for (int i = 0; i < HERO_IDS.length; i++)
      {
        PreparedStatement statement = con.prepareStatement("SELECT char_id, char_name from olympiad_nobles where class_id = ? and competitions_done >= 9 order by olympiad_points desc, competitions_done desc");
        statement.setInt(1, HERO_IDS[i]);
        ResultSet rset = statement.executeQuery();

        if (rset.next())
        {
          StatsSet hero = new StatsSet();
          hero.set("class_id", HERO_IDS[i]);
          hero.set("char_id", rset.getInt("char_id"));
          hero.set("char_name", rset.getString("char_name"));

          _heroesToBe.add(hero);
        }

        statement.close();
        rset.close();
      }
    } catch (SQLException e) {
      _log.warning("Olympiad System: Couldnt heros from db");
    } finally {
      try {
        con.close(); } catch (Exception e) { e.printStackTrace();
      }
    }
  }

  public L2FastList<String> getClassLeaderBoard(int classId)
  {
    L2FastList names = new L2FastList();

    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("SELECT char_name from olympiad_nobles where class_id = ? order by olympiad_points desc, competitions_done desc");
      statement.setInt(1, classId);
      ResultSet rset = statement.executeQuery();

      while (rset.next())
      {
        names.add(rset.getString("char_name"));
      }

      statement.close();
      rset.close();

      L2FastList localL2FastList1 = names;
      return localL2FastList1;
    }
    catch (SQLException e)
    {
      _log.warning("Olympiad System: Couldnt heros from db");
    } finally {
      try {
        con.close(); } catch (Exception e) { e.printStackTrace();
      }
    }
    return names;
  }

  protected void giveHeroBonus()
  {
    if (_heroesToBe.size() == 0) {
      return;
    }
    for (StatsSet hero : _heroesToBe)
    {
      int charId = hero.getInteger("char_id");

      StatsSet noble = (StatsSet)_nobles.get(Integer.valueOf(charId));
      int currentPoints = noble.getInteger("olympiad_points");
      currentPoints += Config.ALT_OLY_HERO_POINTS;
      noble.set("olympiad_points", currentPoints);

      _nobles.remove(Integer.valueOf(charId));
      _nobles.put(Integer.valueOf(charId), noble);
    }
  }

  public int getNoblessePasses(int objId)
  {
    if ((_period != 1) || (_nobles.size() == 0)) {
      return 0;
    }
    StatsSet noble = (StatsSet)_nobles.get(Integer.valueOf(objId));
    if (noble == null)
      return 0;
    int points = noble.getInteger("olympiad_points");
    if (points <= Config.ALT_OLY_MIN_POINT_FOR_EXCH) {
      return 0;
    }
    noble.set("olympiad_points", 0);
    _nobles.remove(Integer.valueOf(objId));
    _nobles.put(Integer.valueOf(objId), noble);

    points *= Config.ALT_OLY_GP_PER_POINT;

    return points;
  }

  public boolean isRegisteredInComp(L2PcInstance player)
  {
    boolean result = false;

    if ((_nonClassBasedRegisters != null) && (_nonClassBasedRegisters.contains(player))) {
      result = true;
    }
    else if ((_classBasedRegisters != null) && (_classBasedRegisters.containsKey(Integer.valueOf(player.getClassId().getId()))))
    {
      L2FastList classed = (L2FastList)_classBasedRegisters.get(Integer.valueOf(player.getClassId().getId()));
      if (classed.contains(player))
        result = true;
    }
    if (_inCompPeriod)
    {
      for (L2OlympiadGame game : _manager.getOlympiadGames().values())
      {
        if ((game._playerOne.getObjectId() == player.getObjectId()) || (game._playerTwo.getObjectId() == player.getObjectId()))
        {
          result = true;
          break;
        }
      }
    }

    return result;
  }

  public int getNoblePoints(int objId)
  {
    if (_nobles.size() == 0) {
      return 0;
    }
    StatsSet noble = (StatsSet)_nobles.get(Integer.valueOf(objId));
    if (noble == null)
      return 0;
    int points = noble.getInteger("olympiad_points");

    return points;
  }

  public int getCompetitionDone(int objId)
  {
    if (_nobles.size() == 0) {
      return 0;
    }
    StatsSet noble = (StatsSet)_nobles.get(Integer.valueOf(objId));
    if (noble == null)
      return 0;
    int points = noble.getInteger("competitions_done");

    return points;
  }

  protected void deleteNobles()
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("DELETE from olympiad_nobles");
      statement.execute();
      statement.close();
    } catch (SQLException e) {
      _log.warning("Olympiad System: Couldnt delete nobles from db");
    } finally {
      try {
        con.close(); } catch (Exception e) { e.printStackTrace();
      }
    }
    _nobles.clear();
  }

  public void save() throws IOException
  {
    saveNobleData();

    Properties OlympiadProperties = new Properties();
    FileOutputStream fos = new FileOutputStream(new File(Config.DATAPACK_ROOT, "config/olympiad.txt"));

    OlympiadProperties.setProperty("CurrentCycle", String.valueOf(_currentCycle));
    OlympiadProperties.setProperty("Period", String.valueOf(_period));
    OlympiadProperties.setProperty("OlympiadEnd", String.valueOf(_olympiadEnd));
    OlympiadProperties.setProperty("ValdationEnd", String.valueOf(_validationEnd));
    OlympiadProperties.setProperty("NextWeeklyChange", String.valueOf(_nextWeeklyChange));

    OlympiadProperties.store(fos, "Olympiad Properties");
    fos.close();
  }

  public void logoutPlayer(L2PcInstance player)
  {
    _classBasedRegisters.remove(Integer.valueOf(player.getClassId().getId()));
    _nonClassBasedRegisters.remove(player);
  }

  public static void sendMatchList(L2PcInstance player)
  {
    NpcHtmlMessage message = new NpcHtmlMessage(0);
    TextBuilder replyMSG = new TextBuilder("<html><body>");
    replyMSG.append("<center><br>Grand Olympiad Game View<table width=270 border=0 bgcolor=\"000000\">");
    replyMSG.append("<tr><td fixwidth=30>NO.</td><td fixwidth=60>Status</td><td>Player1 / Player2</td></tr>");

    FastMap matches = getInstance().getMatchListView();
    for (int i = 0; i < 22; i++)
    {
      int arenaID = i + 1;
      String players = "&nbsp;";
      String state = "Initial State";
      if (matches.containsKey(Integer.valueOf(i)))
      {
        state = "In Progress";
        players = (String)matches.get(Integer.valueOf(i));
      }
      replyMSG.append("<tr><td fixwidth=30><a action=\"bypass -h arenawtt " + i + "\">" + arenaID + "</a></td><td fixwidth=60>" + state + "</td><td>" + players + "</td></tr>");
    }

    replyMSG.append("</table></center></body></html>");

    message.setHtml(replyMSG.toString());
    player.sendPacket(message);
  }

  public FastMap<Integer, String> getMatchListView()
  {
    return _manager == null ? null : _manager.getAllTitles4View();
  }

  private class L2OlympiadGame
  {
    protected Olympiad.COMP_TYPE _type;
    public boolean _aborted;
    public boolean _gamestarted;
    public boolean _playerOneDisconnected;
    public boolean _playerTwoDisconnected;
    public String _playerOneName;
    public String _playerTwoName;
    public int _playerOneID = 0;
    public int _playerTwoID = 0;
    public L2PcInstance _playerOne;
    public L2PcInstance _playerTwo;
    public L2Spawn _spawnOne;
    public L2Spawn _spawnTwo;
    private L2FastList<L2PcInstance> _players;
    private int[] _stadiumPort;
    private int x1;
    private int y1;
    private int z1;
    private int x2;
    private int y2;
    private int z2;
    public int _stadiumID;
    public L2FastList<L2PcInstance> _spectators;
    private SystemMessage _sm;
    private SystemMessage _sm2;
    private SystemMessage _sm3;

    protected L2OlympiadGame(Olympiad.COMP_TYPE id, L2FastList<L2PcInstance> type, int[] list)
    {
      _aborted = false;
      _gamestarted = false;
      _stadiumID = id;
      _playerOneDisconnected = false;
      _playerTwoDisconnected = false;
      _type = type;
      _stadiumPort = stadiumPort;
      _spectators = new L2FastList();

      if (list != null)
      {
        _players = list;
        _playerOne = ((L2PcInstance)list.get(0));
        _playerTwo = ((L2PcInstance)list.get(1));
        try
        {
          _playerOneName = _playerOne.getName();
          _playerTwoName = _playerTwo.getName();
          _playerOne.setOlympiadGameId(id);
          _playerTwo.setOlympiadGameId(id);
          _playerOneID = _playerOne.getObjectId();
          _playerTwoID = _playerTwo.getObjectId();
        }
        catch (Exception e) {
          _aborted = true;
          clearPlayers();
        }
        Olympiad._log.info("Olympiad System: Game - " + id + ": " + _playerOne.getName() + " Vs " + _playerTwo.getName());
      }
      else
      {
        _aborted = true;
        clearPlayers();
        return;
      }
    }

    public boolean isAborted()
    {
      return _aborted;
    }

    protected void clearPlayers()
    {
      _playerOne = null;
      _playerTwo = null;
      _players = null;
      _playerOneName = "";
      _playerTwoName = "";
      _playerOneID = 0;
      _playerTwoID = 0;
    }

    protected void handleDisconnect(L2PcInstance player)
    {
      if (player == _playerOne)
        _playerOneDisconnected = true;
      else if (player == _playerTwo)
        _playerTwoDisconnected = true;
    }

    protected void removals()
    {
      if (_aborted) return;

      if ((_playerOne == null) || (_playerTwo == null)) return;
      if ((_playerOneDisconnected) || (_playerTwoDisconnected)) return;

      if (_playerOne.isDead())
        _playerOne.doRevive(true);
      if (_playerTwo.isDead()) {
        _playerTwo.doRevive(true);
      }
      for (L2PcInstance player : _players)
      {
        try
        {
          if (player.getClan() != null)
          {
            for (L2Skill skill : player.getClan().getAllSkills()) {
              player.removeSkill(skill, false);
            }
          }
          if (player.isCastingNow())
          {
            player.abortCast();
          }

          if (player.isHero())
          {
            for (L2Skill skill : HeroSkillTable.getHeroSkills()) {
              player.removeSkill(skill, false);
            }
          }

          player.setCurrentCp(player.getMaxCp());
          player.setCurrentHp(player.getMaxHp());
          player.setCurrentMp(player.getMaxMp());

          player.stopAllEffects();

          player.stopSkillEffects(176);
          player.stopSkillEffects(139);
          player.stopSkillEffects(406);

          if (player.getPet() != null)
          {
            L2Summon summon = player.getPet();
            summon.stopAllEffects();

            if ((summon instanceof L2PetInstance)) {
              summon.unSummon(player);
            }

          }

          if (player.getCubics() != null)
          {
            for (L2CubicInstance cubic : player.getCubics().values())
            {
              if (cubic.getOwner() != player)
              {
                cubic.stopAction();
                player.delCubic(cubic.getId());
                player.broadcastUserInfo();
              }
            }

          }

          if (player.getParty() != null)
          {
            L2Party party = player.getParty();
            party.removePartyMember(player);
          }

          if (player.getInventory().getPaperdollItem(7) != null)
          {
            L2ItemInstance wpn = player.getInventory().getPaperdollItem(7);
            checkWeaponArmor(player, wpn);
          }
          if (player.getInventory().getPaperdollItem(14) != null)
          {
            L2ItemInstance wpn = player.getInventory().getPaperdollItem(14);
            checkWeaponArmor(player, wpn);
          }
          if (player.getInventory().getPaperdollItem(8) != null)
          {
            L2ItemInstance wpn = player.getInventory().getPaperdollItem(8);
            checkWeaponArmor(player, wpn);
          }
          if (player.getInventory().getPaperdollItem(0) != null)
          {
            L2ItemInstance wpn = player.getInventory().getPaperdollItem(0);
            checkWeaponArmor(player, wpn);
          }
          if (player.getInventory().getPaperdollItem(1) != null)
          {
            L2ItemInstance wpn = player.getInventory().getPaperdollItem(1);
            checkWeaponArmor(player, wpn);
          }
          if (player.getInventory().getPaperdollItem(2) != null)
          {
            L2ItemInstance wpn = player.getInventory().getPaperdollItem(2);
            checkWeaponArmor(player, wpn);
          }
          if (player.getInventory().getPaperdollItem(3) != null)
          {
            L2ItemInstance wpn = player.getInventory().getPaperdollItem(3);
            checkWeaponArmor(player, wpn);
          }
          if (player.getInventory().getPaperdollItem(4) != null)
          {
            L2ItemInstance wpn = player.getInventory().getPaperdollItem(4);
            checkWeaponArmor(player, wpn);
          }
          if (player.getInventory().getPaperdollItem(5) != null)
          {
            L2ItemInstance wpn = player.getInventory().getPaperdollItem(5);
            checkWeaponArmor(player, wpn);
          }
          if (player.getInventory().getPaperdollItem(6) != null)
          {
            L2ItemInstance wpn = player.getInventory().getPaperdollItem(6);
            checkWeaponArmor(player, wpn);
          }
          if (player.getInventory().getPaperdollItem(9) != null)
          {
            L2ItemInstance wpn = player.getInventory().getPaperdollItem(9);
            checkWeaponArmor(player, wpn);
          }
          if (player.getInventory().getPaperdollItem(10) != null)
          {
            L2ItemInstance wpn = player.getInventory().getPaperdollItem(10);
            checkWeaponArmor(player, wpn);
          }
          if (player.getInventory().getPaperdollItem(11) != null)
          {
            L2ItemInstance wpn = player.getInventory().getPaperdollItem(11);
            checkWeaponArmor(player, wpn);
          }
          if (player.getInventory().getPaperdollItem(12) != null)
          {
            L2ItemInstance wpn = player.getInventory().getPaperdollItem(12);
            checkWeaponArmor(player, wpn);
          }
          if (player.getInventory().getPaperdollItem(13) != null)
          {
            L2ItemInstance wpn = player.getInventory().getPaperdollItem(13);
            checkWeaponArmor(player, wpn);
          }
          if (player.getInventory().getPaperdollItem(15) != null)
          {
            L2ItemInstance wpn = player.getInventory().getPaperdollItem(15);
            checkWeaponArmor(player, wpn);
          }
          if (player.getInventory().getPaperdollItem(16) != null)
          {
            L2ItemInstance wpn = player.getInventory().getPaperdollItem(16);
            checkWeaponArmor(player, wpn);
          }
          if (player.getInventory().getPaperdollItem(17) != null)
          {
            L2ItemInstance wpn = player.getInventory().getPaperdollItem(17);
            checkWeaponArmor(player, wpn);
          }

          Map activeSoulShots = player.getAutoSoulShot();
          for (Iterator i$ = activeSoulShots.values().iterator(); i$.hasNext(); ) { int itemId = ((Integer)i$.next()).intValue();

            player.removeAutoSoulShot(itemId);
            ExAutoSoulShot atk = new ExAutoSoulShot(itemId, 0);
            player.sendPacket(atk);
          }
          if (Config.ALT_OLY_REUSE_SKILL)
          {
            for (L2Skill skill : player.getAllSkills()) {
              if ((skill.getId() == 1324) || (!player.isSkillDisabled(skill)))
                continue;
              player.enableSkill(skill.getId());
              MagicSkillUser MSU = new MagicSkillUser(player, skill.getDisplayId(), skill.getLevel(), 300, 1000);
              player.sendPacket(MSU);
            }
          }
          player.sendMessage("\u041E\u0442\u043A\u0430\u0442 \u0441\u043A\u0438\u043B\u043E\u0432 ...");
          player.sendSkillList();
        }
        catch (Exception e) {
        }
      }
    }

    protected boolean portPlayersToArena() {
      boolean _playerOneCrash = (_playerOne == null) || (_playerOneDisconnected);
      boolean _playerTwoCrash = (_playerTwo == null) || (_playerTwoDisconnected);

      if ((_playerOneCrash) || (_playerTwoCrash) || (_aborted))
      {
        _playerOne = null;
        _playerTwo = null;
        _aborted = true;
        return false;
      }
      try
      {
        x1 = _playerOne.getX();
        y1 = _playerOne.getY();
        z1 = _playerOne.getZ();

        x2 = _playerTwo.getX();
        y2 = _playerTwo.getY();
        z2 = _playerTwo.getZ();

        if (_playerOne.isSitting()) {
          _playerOne.standUp();
        }
        if (_playerTwo.isSitting()) {
          _playerTwo.standUp();
        }
        _playerOne.setTarget(null);
        _playerTwo.setTarget(null);

        if (_playerOne != null) _playerOne.teleToLocation(_stadiumPort[0] + 900, _stadiumPort[1], _stadiumPort[2], true);
        if (_playerTwo != null) _playerTwo.teleToLocation(_stadiumPort[0] - 900, _stadiumPort[1], _stadiumPort[2], true);

        _playerOne.sendPacket(new ExOlympiadMode(2));
        _playerTwo.sendPacket(new ExOlympiadMode(2));

        _playerOne.setIsInOlympiadMode(true);
        _playerOne.setIsOlympiadStart(false);
        _playerOne.setOlympiadSide(1);

        _playerTwo.setIsInOlympiadMode(true);
        _playerTwo.setIsOlympiadStart(false);
        _playerTwo.setOlympiadSide(2);

        _gamestarted = true;
      }
      catch (NullPointerException e) {
        return false;
      }
      return true;
    }

    protected void sendMessageToPlayers(boolean toBattleBegin, int nsecond)
    {
      if (!toBattleBegin)
        _sm = new SystemMessage(SystemMessageId.YOU_WILL_ENTER_THE_OLYMPIAD_STADIUM_IN_S1_SECOND_S);
      else {
        _sm = new SystemMessage(SystemMessageId.THE_GAME_WILL_START_IN_S1_SECOND_S);
      }
      _sm.addNumber(nsecond);
      try
      {
        for (L2PcInstance player : _players)
          player.sendPacket(_sm);
      }
      catch (Exception e)
      {
      }
    }

    protected void portPlayersBack() {
      if (_playerOne != null) {
        _playerOne.teleToLocation(x1, y1, z1, true);
      }
      if (_playerTwo != null)
        _playerTwo.teleToLocation(x2, y2, z2, true);
    }

    protected void PlayersStatusBack()
    {
      for (L2PcInstance player : _players)
      {
        try
        {
          player.getStatus().startHpMpRegeneration();
          player.setCurrentCp(player.getMaxCp());
          player.setCurrentHp(player.getMaxHp());
          player.setCurrentMp(player.getMaxMp());
          player.setIsInOlympiadMode(false);
          player.setIsOlympiadStart(false);
          player.setOlympiadSide(-1);
          player.setOlympiadGameId(-1);
          player.sendPacket(new ExOlympiadMode(0));

          if (player.getClan() != null)
          {
            for (L2Skill skill : player.getClan().getAllSkills())
            {
              if (skill.getMinPledgeClass() <= player.getPledgeClass()) {
                player.addSkill(skill, false);
              }
            }
          }

          if (player.isHero())
          {
            for (L2Skill skill : HeroSkillTable.getHeroSkills())
              player.addSkill(skill, false);
          }
          player.sendSkillList();
        }
        catch (Exception e) {
        }
      }
    }

    protected boolean haveWinner() {
      boolean retval = false;
      if ((_aborted) || (_playerOne == null) || (_playerTwo == null) || (_playerOneDisconnected) || (_playerTwoDisconnected))
      {
        return true;
      }

      double playerOneHp = 0.0D;
      try
      {
        if ((_playerOne != null) && (_playerOne.getOlympiadGameId() != -1))
        {
          playerOneHp = _playerOne.getCurrentHp();
        }
      } catch (Exception e) {
        playerOneHp = 0.0D;
      }

      double playerTwoHp = 0.0D;
      try {
        if ((_playerTwo != null) && (_playerTwo.getOlympiadGameId() != -1))
        {
          playerTwoHp = _playerTwo.getCurrentHp();
        }
      } catch (Exception e) {
        playerTwoHp = 0.0D;
      }

      if ((playerTwoHp <= 0.0D) || (playerOneHp <= 0.0D))
      {
        if (_playerOne.getPet() != null)
        {
          L2Summon summon = _playerOne.getPet();
          summon.stopAllEffects();
          summon.unSummon(_playerOne);
        }
        if (_playerTwo.getPet() != null)
        {
          L2Summon summon = _playerTwo.getPet();
          summon.stopAllEffects();
          summon.unSummon(_playerTwo);
        }
        return true;
      }

      return retval;
    }

    protected void validateWinner()
    {
      if ((_aborted) || (_playerOne == null) || (_playerTwo == null) || (_playerOneDisconnected) || (_playerTwoDisconnected))
      {
        return;
      }

      StatsSet playerOneStat = (StatsSet)Olympiad._nobles.get(Integer.valueOf(_playerOneID));
      StatsSet playerTwoStat = (StatsSet)Olympiad._nobles.get(Integer.valueOf(_playerTwoID));

      int playerOnePlayed = playerOneStat.getInteger("competitions_done");
      int playerTwoPlayed = playerTwoStat.getInteger("competitions_done");

      int playerOnePoints = playerOneStat.getInteger("olympiad_points");
      int playerTwoPoints = playerTwoStat.getInteger("olympiad_points");

      double playerOneHp = 0.0D;
      try
      {
        if ((_playerOne != null) && (!_playerOneDisconnected))
        {
          if (!_playerOne.isDead())
          {
            playerOneHp = _playerOne.getCurrentHp() + _playerOne.getCurrentCp();
          }
        }
      }
      catch (Exception e)
      {
        playerOneHp = 0.0D;
      }

      double playerTwoHp = 0.0D;
      try
      {
        if ((_playerTwo != null) && (!_playerTwoDisconnected))
        {
          if (!_playerTwo.isDead())
          {
            playerTwoHp = _playerTwo.getCurrentHp() + _playerTwo.getCurrentCp();
          }
        }
      }
      catch (Exception e)
      {
        playerTwoHp = 0.0D;
      }

      _sm = new SystemMessage(SystemMessageId.S1_HAS_WON_THE_GAME);
      _sm2 = new SystemMessage(SystemMessageId.S1_HAS_GAINED_S2_OLYMPIAD_POINTS);
      _sm3 = new SystemMessage(SystemMessageId.S1_HAS_LOST_S2_OLYMPIAD_POINTS);

      String result = "";

      String ip1 = "";
      String ip2 = "";
      try
      {
        if ((_playerOne != null) && (_playerOne.isOnline() != 0) && (!_playerOne.isOffline()))
          ip1 = _playerOne.getClient().getConnection().getSocket().getInetAddress().getHostAddress();
        if ((_playerTwo != null) && (_playerTwo.isOnline() != 0) && (!_playerTwo.isOffline())) {
          ip2 = _playerTwo.getClient().getConnection().getSocket().getInetAddress().getHostAddress();
        }
      }
      catch (Exception e)
      {
      }
      if ((ip1.equals(ip2)) && (!Config.OLY_SAME_IP))
      {
        Olympiad._log.info("Match from same ip " + _playerOneName + " vs " + _playerTwoName);
        result = " tie";
        _sm = new SystemMessage(SystemMessageId.THE_GAME_ENDED_IN_A_TIE);
        broadcastMessage(_sm, true);
        try
        {
          _playerOne.sendMessage("Match suspected of Illegal Violation: GM informed");
          _playerTwo.sendMessage("Match suspected of Illegal Violation: GM informed");
        }
        catch (Exception e)
        {
        }

      }

      _playerOne = L2World.getInstance().getPlayer(_playerOneName);
      _players.set(0, _playerOne);
      _playerTwo = L2World.getInstance().getPlayer(_playerTwoName);
      _players.set(1, _playerTwo);
      int _div;
      int _gpreward;
      switch (Olympiad.6.$SwitchMap$net$sf$l2j$gameserver$model$olympiad$Olympiad$COMP_TYPE[_type.ordinal()]) { case 1:
        _div = 5; _gpreward = Config.ALT_OLY_NONCLASSED_RITEM_C; break;
      default:
        _div = 3; _gpreward = Config.ALT_OLY_CLASSED_RITEM_C;
      }

      int pointDiff = Math.min(playerOnePoints, playerTwoPoints) / _div;

      if ((_playerTwo.isOnline() == 0) || ((playerTwoHp <= 0.0D) && (playerOneHp > 0.0D)) || ((_playerOne.dmgDealt > _playerTwo.dmgDealt) && (playerTwoHp != 0.0D) && (playerOneHp != 0.0D)))
      {
        playerOneStat.set("olympiad_points", playerOnePoints + pointDiff);
        playerTwoStat.set("olympiad_points", playerTwoPoints - pointDiff);

        _sm.addString(_playerOneName);
        broadcastMessage(_sm, true);
        _sm2.addString(_playerOneName);
        _sm2.addNumber(pointDiff);
        broadcastMessage(_sm2, true);
        _sm3.addString(_playerTwoName);
        _sm3.addNumber(pointDiff);
        broadcastMessage(_sm3, true);
        try
        {
          result = " (" + playerOneHp + "hp vs " + playerTwoHp + "hp - " + _playerOne.dmgDealt + "dmg vs " + _playerTwo.dmgDealt + "dmg) " + _playerOneName + " win " + pointDiff + " points";
          L2ItemInstance item = _playerOne.getInventory().addItem("Olympiad", Config.ALT_OLY_BATTLE_REWARD_ITEM, _gpreward, _playerOne, null);
          InventoryUpdate iu = new InventoryUpdate();
          iu.addModifiedItem(item);
          _playerOne.sendPacket(iu);

          SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
          sm.addItemName(item.getItemId());
          sm.addNumber(_gpreward);
          _playerOne.sendPacket(sm);
        } catch (Exception e) {
        }
      } else if ((_playerOne.isOnline() == 0) || ((playerOneHp <= 0.0D) && (playerTwoHp >= 0.0D)) || ((_playerTwo.dmgDealt > _playerOne.dmgDealt) && (playerOneHp != 0.0D) && (playerTwoHp != 0.0D)))
      {
        playerTwoStat.set("olympiad_points", playerTwoPoints + pointDiff);
        playerOneStat.set("olympiad_points", playerOnePoints - pointDiff);

        _sm.addString(_playerTwoName);
        broadcastMessage(_sm, true);
        _sm2.addString(_playerTwoName);
        _sm2.addNumber(pointDiff);
        broadcastMessage(_sm2, true);
        _sm3.addString(_playerOneName);
        _sm3.addNumber(pointDiff);
        broadcastMessage(_sm3, true);
        try
        {
          result = " (" + playerOneHp + "hp vs " + playerTwoHp + "hp - " + _playerOne.dmgDealt + "dmg vs " + _playerTwo.dmgDealt + "dmg) " + _playerTwoName + " win " + pointDiff + " points";
          L2ItemInstance item = _playerTwo.getInventory().addItem("Olympiad", Config.ALT_OLY_BATTLE_REWARD_ITEM, _gpreward, _playerTwo, null);
          InventoryUpdate iu = new InventoryUpdate();
          iu.addModifiedItem(item);
          _playerTwo.sendPacket(iu);

          SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
          sm.addItemName(item.getItemId());
          sm.addNumber(_gpreward);
          _playerTwo.sendPacket(sm);
        } catch (Exception e) {
        }
      }
      else {
        result = " tie";
        _sm = new SystemMessage(SystemMessageId.THE_GAME_ENDED_IN_A_TIE);
        broadcastMessage(_sm, true);
      }
      Olympiad._log.info("Olympia Result: " + _playerOneName + " vs " + _playerTwoName + " ... " + result);

      playerOneStat.set("competitions_done", playerOnePlayed + 1);
      playerTwoStat.set("competitions_done", playerTwoPlayed + 1);

      Olympiad._nobles.remove(Integer.valueOf(_playerOneID));
      Olympiad._nobles.remove(Integer.valueOf(_playerTwoID));

      Olympiad._nobles.put(Integer.valueOf(_playerOneID), playerOneStat);
      Olympiad._nobles.put(Integer.valueOf(_playerTwoID), playerTwoStat);

      for (int i = 20; i > 10; i -= 10)
      {
        _sm = new SystemMessage(SystemMessageId.YOU_WILL_GO_BACK_TO_THE_VILLAGE_IN_S1_SECOND_S);
        _sm.addNumber(10);
        broadcastMessage(_sm, false);
        try
        {
          Thread.sleep(5000L);
        } catch (InterruptedException e) {
        }
      }
      for (int i = 5; i > 0; i--)
      {
        _sm = new SystemMessage(SystemMessageId.YOU_WILL_GO_BACK_TO_THE_VILLAGE_IN_S1_SECOND_S);
        _sm.addNumber(i);
        broadcastMessage(_sm, false);
        try { Thread.sleep(1000L); } catch (InterruptedException e) {
        }
      }
    }

    protected void additions() {
      for (L2PcInstance player : _players)
      {
        try
        {
          player.setCurrentCp(player.getMaxCp());
          player.setCurrentHp(player.getMaxHp());
          player.setCurrentMp(player.getMaxMp());
          player.dmgDealt = 0;

          L2Skill skill = SkillTable.getInstance().getInfo(1204, 2);
          skill.getEffects(player, player);
          SystemMessage sm = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
          sm.addSkillName(1204);
          player.sendPacket(sm);

          if (!player.isMageClass())
          {
            skill = SkillTable.getInstance().getInfo(1086, 1);
            skill.getEffects(player, player);
            sm = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
            sm.addSkillName(1086);
            player.sendPacket(sm);
          }
          else
          {
            skill = SkillTable.getInstance().getInfo(1085, 1);
            skill.getEffects(player, player);
            sm = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
            sm.addSkillName(1085);
            player.sendPacket(sm);
          }
        }
        catch (Exception e) {
        }
      }
    }

    protected boolean makePlayersVisible() {
      _sm = new SystemMessage(SystemMessageId.STARTS_THE_GAME);
      try
      {
        for (L2PcInstance player : _players)
        {
          player.getAppearance().setVisible();
          player.broadcastUserInfo();
          player.sendPacket(_sm);
          if (player.getPet() != null)
            player.getPet().updateAbnormalEffect();
        }
      } catch (NullPointerException e) {
        _aborted = true; return false;
      }return true;
    }

    protected boolean makeCompetitionStart()
    {
      if (_aborted) return false;

      _sm = new SystemMessage(SystemMessageId.STARTS_THE_GAME);
      broadcastMessage(_sm, true);
      try
      {
        for (L2PcInstance player : _players)
        {
          player.setIsOlympiadStart(true);
        }
      } catch (Exception e) {
        _aborted = true; return false;
      }return true;
    }

    protected String getTitle()
    {
      String msg = "";
      msg = msg + _playerOneName + " : " + _playerTwoName;
      return msg;
    }

    protected L2PcInstance[] getPlayers()
    {
      L2PcInstance[] players = new L2PcInstance[2];

      if ((_playerOne == null) || (_playerTwo == null)) return null;

      players[0] = _playerOne;
      players[1] = _playerTwo;

      return players;
    }

    protected L2FastList<L2PcInstance> getSpectators()
    {
      return _spectators;
    }

    protected void addSpectator(L2PcInstance spec)
    {
      _spectators.add(spec);
    }

    protected void removeSpectator(L2PcInstance spec)
    {
      if ((_spectators != null) && (_spectators.contains(spec)))
        _spectators.remove(spec);
    }

    protected void clearSpectators()
    {
      if (_spectators != null)
      {
        for (L2PcInstance pc : _spectators)
          try
          {
            if (pc.inObserverMode())
              pc.leaveOlympiadObserverMode();
          } catch (NullPointerException e) {
          }
        _spectators.clear();
      }
    }

    private void broadcastMessage(SystemMessage sm, boolean toAll)
    {
      try {
        _playerOne.sendPacket(sm);
        _playerTwo.sendPacket(sm);
      } catch (Exception e) {
      }
      if ((toAll) && (_spectators != null))
      {
        for (L2PcInstance spec : _spectators)
          try {
            spec.sendPacket(sm);
          } catch (NullPointerException e) {
          }
      }
    }

    private void checkWeaponArmor(L2PcInstance player, L2ItemInstance wpn) {
      if ((wpn != null) && ((wpn.isHeroItem()) || (wpn.isOlyRestrictedItem())))
      {
        if (wpn.isAugmented())
          wpn.getAugmentation().removeBoni(player);
        L2ItemInstance[] unequiped = player.getInventory().unEquipItemInBodySlotAndRecord(wpn.getItem().getBodyPart());
        InventoryUpdate iu = new InventoryUpdate();
        for (L2ItemInstance item : unequiped)
        {
          iu.addModifiedItem(item);
        }
        player.sendPacket(iu);
        player.abortAttack();
        player.broadcastUserInfo();

        if (unequiped.length > 0)
        {
          if (unequiped[0].isWear())
            return;
          SystemMessage sm = null;
          if (unequiped[0].getEnchantLevel() > 0)
          {
            sm = new SystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
            sm.addNumber(unequiped[0].getEnchantLevel());
            sm.addItemName(unequiped[0].getItemId());
          }
          else
          {
            sm = new SystemMessage(SystemMessageId.S1_DISARMED);
            sm.addItemName(unequiped[0].getItemId());
          }
          player.sendPacket(sm);
        }
      }
    }
  }

  private class OlympiadManager
    implements Runnable
  {
    private Map<Integer, Olympiad.L2OlympiadGame> _olympiadInstances;

    public OlympiadManager()
    {
      _olympiadInstances = new FastMap();
      Olympiad._manager = this;
    }

    public synchronized void run()
    {
      Olympiad._cycleTerminated = false;
      if (isOlympiadEnd())
      {
        _scheduledManagerTask.cancel(true);
        Olympiad._cycleTerminated = true;
        return;
      }
      Map _gamesQueue = new FastMap();
      while (inCompPeriod())
      {
        if (Olympiad._nobles.size() == 0)
        {
          try {
            wait(60000L); } catch (InterruptedException ex) {
          }
          continue;
        }

        int classBasedPgCount = 0;
        for (L2FastList classList : Olympiad._classBasedRegisters.values()) {
          classBasedPgCount += classList.size();
        }
        while (((_gamesQueue.size() > 0) || (classBasedPgCount >= Config.ALT_OLY_CLASSED) || (Olympiad._nonClassBasedRegisters.size() >= Config.ALT_OLY_NONCLASSED)) && (inCompPeriod()))
        {
          int _gamesQueueSize = 0;
          _gamesQueueSize = _gamesQueue.size();
          for (int i = 0; i < _gamesQueueSize; i++)
          {
            if ((_gamesQueue.get(Integer.valueOf(i)) == null) || (((Olympiad.OlympiadGameTask)_gamesQueue.get(Integer.valueOf(i))).isTerminated()) || (((Olympiad.OlympiadGameTask)_gamesQueue.get(Integer.valueOf(i)))._game == null))
            {
              if (_gamesQueue.containsKey(Integer.valueOf(i)))
              {
                try
                {
                  _olympiadInstances.remove(Integer.valueOf(i));
                  _gamesQueue.remove(Integer.valueOf(i));
                  Olympiad.STADIUMS[i].setStadiaFree();
                } catch (Exception e) {
                  e.printStackTrace();
                }
              }
              else
                _gamesQueueSize += 1;
            }
            else {
              if ((_gamesQueue.get(Integer.valueOf(i)) == null) || (((Olympiad.OlympiadGameTask)_gamesQueue.get(Integer.valueOf(i))).isStarted())) {
                continue;
              }
              Thread T = new Thread((Runnable)_gamesQueue.get(Integer.valueOf(i)));
              T.start();
            }
          }

          for (int i = 0; i < Olympiad.STADIUMS.length; i++)
          {
            if ((!existNextOpponents(Olympiad._nonClassBasedRegisters)) && (!existNextOpponents(getRandomClassList(Olympiad._classBasedRegisters))))
            {
              break;
            }

            if (!Olympiad.STADIUMS[i].isFreeToUse())
              continue;
            if (existNextOpponents(Olympiad._nonClassBasedRegisters))
            {
              try
              {
                _olympiadInstances.put(Integer.valueOf(i), new Olympiad.L2OlympiadGame(Olympiad.this, i, Olympiad.COMP_TYPE.NON_CLASSED, nextOpponents(Olympiad._nonClassBasedRegisters), Olympiad.STADIUMS[i].getCoordinates()));
                _gamesQueue.put(Integer.valueOf(i), new Olympiad.OlympiadGameTask(Olympiad.this, (Olympiad.L2OlympiadGame)_olympiadInstances.get(Integer.valueOf(i))));
                Olympiad.STADIUMS[i].setStadiaBusy();
              }
              catch (Exception ex)
              {
                if (_olympiadInstances.get(Integer.valueOf(i)) != null)
                {
                  for (L2PcInstance player : ((Olympiad.L2OlympiadGame)_olympiadInstances.get(Integer.valueOf(i))).getPlayers())
                  {
                    player.sendMessage("Your olympiad registration was canceled due to an error");
                    player.setIsInOlympiadMode(false);
                    player.setIsOlympiadStart(false);
                    player.setOlympiadSide(-1);
                    player.setOlympiadGameId(-1);
                  }
                  _olympiadInstances.remove(Integer.valueOf(i));
                }
                if (_gamesQueue.get(Integer.valueOf(i)) != null)
                  _gamesQueue.remove(Integer.valueOf(i));
                Olympiad.STADIUMS[i].setStadiaFree();

                i--;
              }
            } else {
              if (!existNextOpponents(getRandomClassList(Olympiad._classBasedRegisters)))
                continue;
              try
              {
                _olympiadInstances.put(Integer.valueOf(i), new Olympiad.L2OlympiadGame(Olympiad.this, i, Olympiad.COMP_TYPE.CLASSED, nextOpponents(getRandomClassList(Olympiad._classBasedRegisters)), Olympiad.STADIUMS[i].getCoordinates()));
                _gamesQueue.put(Integer.valueOf(i), new Olympiad.OlympiadGameTask(Olympiad.this, (Olympiad.L2OlympiadGame)_olympiadInstances.get(Integer.valueOf(i))));
                Olympiad.STADIUMS[i].setStadiaBusy();
              }
              catch (Exception ex)
              {
                if (_olympiadInstances.get(Integer.valueOf(i)) != null) {
                  for (L2PcInstance player : ((Olympiad.L2OlympiadGame)_olympiadInstances.get(Integer.valueOf(i))).getPlayers())
                  {
                    player.sendMessage("Your olympiad registration was canceled due to an error");
                    player.setIsInOlympiadMode(false);
                    player.setIsOlympiadStart(false);
                    player.setOlympiadSide(-1);
                    player.setOlympiadGameId(-1);
                  }
                  _olympiadInstances.remove(Integer.valueOf(i));
                }
                if (_gamesQueue.get(Integer.valueOf(i)) != null)
                  _gamesQueue.remove(Integer.valueOf(i));
                Olympiad.STADIUMS[i].setStadiaFree();

                i--;
              }
            }
          }

          try
          {
            wait(30000L);
          } catch (InterruptedException e) {
          }
        }
        try {
          wait(30000L);
        }
        catch (InterruptedException e) {
        }
      }
      boolean allGamesTerminated = false;

      while (!allGamesTerminated)
      {
        try {
          wait(30000L);
        } catch (InterruptedException e) {
        }
        if (_gamesQueue.size() == 0)
        {
          allGamesTerminated = true; continue;
        }
        for (Olympiad.OlympiadGameTask game : _gamesQueue.values())
        {
          allGamesTerminated = (allGamesTerminated) || (game.isTerminated());
        }
      }

      Olympiad._cycleTerminated = true;

      _gamesQueue.clear();

      _olympiadInstances.clear();
      Olympiad._classBasedRegisters.clear();
      Olympiad._nonClassBasedRegisters.clear();

      Olympiad._battleStarted = false;
    }

    protected Olympiad.L2OlympiadGame getOlympiadInstance(int index)
    {
      if ((_olympiadInstances != null) && (_olympiadInstances.size() > 0))
      {
        return (Olympiad.L2OlympiadGame)_olympiadInstances.get(Integer.valueOf(index));
      }
      return null;
    }

    protected Map<Integer, Olympiad.L2OlympiadGame> getOlympiadGames()
    {
      return _olympiadInstances == null ? null : _olympiadInstances;
    }

    private L2FastList<L2PcInstance> getRandomClassList(Map<Integer, L2FastList<L2PcInstance>> list)
    {
      if (list.size() == 0) {
        return null;
      }
      Map tmp = new FastMap();
      int tmpIndex = 0;
      for (L2FastList l : list.values())
      {
        tmp.put(Integer.valueOf(tmpIndex), l);
        tmpIndex++;
      }

      L2FastList rndList = new L2FastList();
      int classIndex = 0;
      if (tmp.size() == 1)
        classIndex = 0;
      else
        classIndex = Rnd.nextInt(tmp.size());
      rndList = (L2FastList)tmp.get(Integer.valueOf(classIndex));
      return rndList;
    }

    private L2FastList<L2PcInstance> nextOpponents(L2FastList<L2PcInstance> list) {
      L2FastList opponents = new L2FastList();
      if (list.size() == 0)
        return opponents;
      int loopCount = list.size() / 2;

      if (loopCount < 1) {
        return opponents;
      }
      int first = Rnd.nextInt(list.size());
      opponents.add(list.get(first));
      list.remove(first);

      int second = Rnd.nextInt(list.size());
      opponents.add(list.get(second));
      list.remove(second);

      return opponents;
    }

    private boolean existNextOpponents(L2FastList<L2PcInstance> list)
    {
      if (list == null)
        return false;
      if (list.size() == 0)
        return false;
      int loopCount = list.size() >> 1;

      return loopCount >= 1;
    }

    protected FastMap<Integer, String> getAllTitles4View()
    {
      FastMap titles = new FastMap();

      for (Olympiad.L2OlympiadGame instance : _olympiadInstances.values())
      {
        if (instance._gamestarted != true) {
          continue;
        }
        titles.put(Integer.valueOf(instance._stadiumID), instance.getTitle());
      }

      return titles;
    }

    protected String[] getAllTitles()
    {
      String[] msg = new String[_olympiadInstances.size()];
      int count = 0;
      int match = 1;
      int showbattle = 0;

      for (Olympiad.L2OlympiadGame instance : _olympiadInstances.values())
      {
        if (instance._gamestarted == true) showbattle = 1; else showbattle = 0;
        msg[count] = ("<" + showbattle + "><" + instance._stadiumID + "> In Progress " + instance.getTitle());
        count++;
        match++;
      }

      return msg;
    }
  }

  private static enum COMP_TYPE
  {
    CLASSED, 
    NON_CLASSED;
  }

  public static class Stadia
  {
    private boolean _freeToUse = true;
    private static L2FastList<L2PcInstance> _spectators;
    private int[] _coords = new int[3];

    public boolean isFreeToUse()
    {
      return _freeToUse;
    }

    public void setStadiaBusy() {
      _freeToUse = false;
    }

    public void setStadiaFree() {
      _freeToUse = true;
    }

    public int[] getCoordinates()
    {
      return _coords;
    }

    public Stadia(int[] coords) {
      _coords = coords;
    }

    public Stadia(int x, int y, int z) {
      _coords[0] = x;
      _coords[1] = y;
      _coords[2] = z;
      _spectators = new L2FastList();
    }

    private void addSpectator(int id, L2PcInstance spec)
    {
      spec.enterOlympiadObserverMode(getCoordinates()[0], getCoordinates()[1], getCoordinates()[2], id);
      _spectators.add(spec);
    }

    protected L2FastList<L2PcInstance> getSpectators()
    {
      return _spectators;
    }

    private void removeSpectator(L2PcInstance spec)
    {
      if ((_spectators != null) && (_spectators.contains(spec)))
        _spectators.remove(spec);
    }
  }

  private class OlympiadGameTask
    implements Runnable
  {
    public Olympiad.L2OlympiadGame _game = null;
    private SystemMessage _sm;
    private SystemMessage _sm2;
    private boolean _terminated = false;

    private boolean _started = false;

    public boolean isTerminated()
    {
      return _terminated;
    }

    public boolean isStarted()
    {
      return _started;
    }

    public OlympiadGameTask(Olympiad.L2OlympiadGame game)
    {
      _game = game;
    }

    protected boolean checkBattleStatus()
    {
      boolean _pOneCrash = (_game._playerOne == null) || (_game._playerOneDisconnected);
      boolean _pTwoCrash = (_game._playerTwo == null) || (_game._playerTwoDisconnected);

      return (!_pOneCrash) && (!_pTwoCrash) && (!_game._aborted);
    }

    protected boolean proverkaDoTeleporta()
    {
      boolean _pOneCrash = (_game._playerOne == null) || (_game._playerOneDisconnected);
      boolean _pTwoCrash = (_game._playerTwo == null) || (_game._playerTwoDisconnected);

      StatsSet playerOneStat = (StatsSet)Olympiad._nobles.get(Integer.valueOf(_game._playerOneID));
      StatsSet playerTwoStat = (StatsSet)Olympiad._nobles.get(Integer.valueOf(_game._playerTwoID));

      if ((_pOneCrash) || (_pTwoCrash) || (_game._aborted))
      {
        if ((_pOneCrash) && (!_pTwoCrash)) {
          try {
            int playerOnePoints = playerOneStat.getInteger("olympiad_points");
            int transferPoints = playerOnePoints / 5;
            playerOneStat.set("olympiad_points", playerOnePoints - transferPoints);
            Olympiad._log.info("Olympia Result: " + _game._playerOneName + " vs " + _game._playerTwoName + " ... " + _game._playerOneName + " lost " + transferPoints + " points for crash before teleport");
            int playerTwoPoints = playerTwoStat.getInteger("olympiad_points");
            playerTwoStat.set("olympiad_points", playerTwoPoints + transferPoints);
            Olympiad._log.info("Olympia Result: " + _game._playerOneName + " vs " + _game._playerTwoName + " ... " + _game._playerTwoName + " Win " + transferPoints + " points");

            _sm = new SystemMessage(SystemMessageId.S1_HAS_WON_THE_GAME);
            _sm2 = new SystemMessage(SystemMessageId.S1_HAS_GAINED_S2_OLYMPIAD_POINTS);
            _sm.addString(_game._playerTwoName);
            broadcastMessage(_sm, true);
            _sm2.addString(_game._playerTwoName);
            _sm2.addNumber(transferPoints);
            broadcastMessage(_sm2, true); } catch (Exception e) {
            e.printStackTrace();
          }
        }
        if ((_pTwoCrash) && (!_pOneCrash))
          try {
            int playerTwoPoints = playerTwoStat.getInteger("olympiad_points");
            int transferPoints = playerTwoPoints / 5;
            playerTwoStat.set("olympiad_points", playerTwoPoints - transferPoints);
            Olympiad._log.info("Olympia Result: " + _game._playerTwoName + " vs " + _game._playerOneName + " ... " + _game._playerTwoName + " lost " + transferPoints + " points for crash before teleport");
            int playerOnePoints = playerOneStat.getInteger("olympiad_points");
            playerOneStat.set("olympiad_points", playerOnePoints + transferPoints);
            Olympiad._log.info("Olympia Result: " + _game._playerTwoName + " vs " + _game._playerOneName + " ... " + _game._playerOneName + " Win " + transferPoints + " points");

            _sm = new SystemMessage(SystemMessageId.S1_HAS_WON_THE_GAME);
            _sm2 = new SystemMessage(SystemMessageId.S1_HAS_GAINED_S2_OLYMPIAD_POINTS);
            _sm.addString(_game._playerOneName);
            broadcastMessage(_sm, true);
            _sm2.addString(_game._playerOneName);
            _sm2.addNumber(transferPoints);
            broadcastMessage(_sm2, true); } catch (Exception e) {
            e.printStackTrace();
          }
        _terminated = true;
        _game._gamestarted = false;
        _game = null;
        return false;
      }
      return true;
    }

    protected boolean checkStatus()
    {
      boolean _pOneCrash = (_game._playerOne == null) || (_game._playerOneDisconnected);
      boolean _pTwoCrash = (_game._playerTwo == null) || (_game._playerTwoDisconnected);

      StatsSet playerOneStat = (StatsSet)Olympiad._nobles.get(Integer.valueOf(_game._playerOneID));
      StatsSet playerTwoStat = (StatsSet)Olympiad._nobles.get(Integer.valueOf(_game._playerTwoID));

      int playerOnePlayed = playerOneStat.getInteger("competitions_done");
      int playerTwoPlayed = playerTwoStat.getInteger("competitions_done");

      if ((_pOneCrash) || (_pTwoCrash) || (_game._aborted)) {
        if ((_pOneCrash) && (!_pTwoCrash)) {
          try {
            int playerOnePoints = playerOneStat.getInteger("olympiad_points");
            int transferPoints = playerOnePoints / 5;
            playerOneStat.set("olympiad_points", playerOnePoints - transferPoints);
            Olympiad._log.info("Olympia Result: " + _game._playerOneName + " vs " + _game._playerTwoName + " ... " + _game._playerOneName + " lost " + transferPoints + " points for crash");
            int playerTwoPoints = playerTwoStat.getInteger("olympiad_points");
            playerTwoStat.set("olympiad_points", playerTwoPoints + transferPoints);
            Olympiad._log.info("Olympia Result: " + _game._playerOneName + " vs " + _game._playerTwoName + " ... " + _game._playerTwoName + " Win " + transferPoints + " points");

            _sm = new SystemMessage(SystemMessageId.S1_HAS_WON_THE_GAME);
            _sm2 = new SystemMessage(SystemMessageId.S1_HAS_GAINED_S2_OLYMPIAD_POINTS);
            _sm.addString(_game._playerTwoName);
            broadcastMessage(_sm, true);
            _sm2.addString(_game._playerTwoName);
            _sm2.addNumber(transferPoints);
            broadcastMessage(_sm2, true); } catch (Exception e) {
            e.printStackTrace();
          }
        }
        if ((_pTwoCrash) && (!_pOneCrash))
          try {
            int playerTwoPoints = playerTwoStat.getInteger("olympiad_points");
            int transferPoints = playerTwoPoints / 5;
            playerTwoStat.set("olympiad_points", playerTwoPoints - transferPoints);
            Olympiad._log.info("Olympia Result: " + _game._playerTwoName + " vs " + _game._playerOneName + " ... " + _game._playerTwoName + " lost " + transferPoints + " points for crash");
            int playerOnePoints = playerOneStat.getInteger("olympiad_points");
            playerOneStat.set("olympiad_points", playerOnePoints + transferPoints);
            Olympiad._log.info("Olympia Result: " + _game._playerTwoName + " vs " + _game._playerOneName + " ... " + _game._playerOneName + " Win " + transferPoints + " points");

            _sm = new SystemMessage(SystemMessageId.S1_HAS_WON_THE_GAME);
            _sm2 = new SystemMessage(SystemMessageId.S1_HAS_GAINED_S2_OLYMPIAD_POINTS);
            _sm.addString(_game._playerOneName);
            broadcastMessage(_sm, true);
            _sm2.addString(_game._playerOneName);
            _sm2.addNumber(transferPoints);
            broadcastMessage(_sm2, true); } catch (Exception e) {
            e.printStackTrace();
          }
        playerOneStat.set("competitions_done", playerOnePlayed + 1);
        playerTwoStat.set("competitions_done", playerTwoPlayed + 1);

        _terminated = true;
        _game._gamestarted = false;
        _game.PlayersStatusBack();
        try {
          _game.portPlayersBack(); } catch (Exception e) {
          e.printStackTrace();
        }_game = null;
        return false;
      }
      return true;
    }

    public void run()
    {
      _started = true;
      if (_game != null)
      {
        if ((_game._playerOne != null) && (_game._playerTwo != null))
        {
          for (int i = 45; i > 10; i -= 5)
          {
            switch (i) {
            case 15:
            case 30:
            case 45:
              _game.sendMessageToPlayers(false, i);
            }
            try {
              Thread.sleep(5000L); } catch (InterruptedException e) {
            }if (!proverkaDoTeleporta()) return;
          }
          for (int i = 5; i > 0; i--)
          {
            _game.sendMessageToPlayers(false, i);
            try { Thread.sleep(1000L);
            } catch (InterruptedException e) {
            }
          }
          if (!checkStatus()) return;

          _game.removals();

          _game.portPlayersToArena();
          try {
            Thread.sleep(5000L); } catch (InterruptedException e) {
          }
          synchronized (this)
          {
            if (!Olympiad._battleStarted)
              Olympiad._battleStarted = true;
          }
          for (int i = 60; i > 10; i -= 10)
          {
            _game.sendMessageToPlayers(true, i);
            try {
              Thread.sleep(10000L); } catch (InterruptedException e) {
            }
            if (i != 20)
              continue;
            _game.sendMessageToPlayers(true, 10);
            try { Thread.sleep(5000L); } catch (InterruptedException e) {
            }
          }
          _game.additions();
          for (int i = 5; i > 0; i--)
          {
            _game.sendMessageToPlayers(true, i);
            try { Thread.sleep(1000L); } catch (InterruptedException e) {
            }
          }
          if (!checkStatus()) return;

          _game._playerOne.sendPacket(new ExOlympiadUserInfoSpectator(_game._playerTwo, 1));
          _game._playerTwo.sendPacket(new ExOlympiadUserInfoSpectator(_game._playerOne, 1));
          if (_game._spectators != null)
          {
            for (L2PcInstance spec : _game.getSpectators())
            {
              try
              {
                spec.sendPacket(new ExOlympiadUserInfoSpectator(_game._playerTwo, 2));
                spec.sendPacket(new ExOlympiadUserInfoSpectator(_game._playerOne, 1)); } catch (NullPointerException e) {
              }
            }
          }
          _game.makeCompetitionStart();

          for (int i = 0; i < Olympiad.BATTLE_PERIOD; i += 5000)
            try
            {
              Thread.sleep(5000L);

              if ((_game.haveWinner()) || (!checkBattleStatus()))
                break;
            } catch (InterruptedException e) {
            }
          if (!checkStatus()) return;
          _terminated = true;
          _game._gamestarted = false;
          try {
            _game.validateWinner();
            _game.removals();
            _game.PlayersStatusBack();
            _game.portPlayersBack(); } catch (Exception e) {
            e.printStackTrace();
          }_game = null;
        }
      }
    }

    private void broadcastMessage(SystemMessage sm, boolean toAll) {
      try {
        _game._playerOne.sendPacket(sm);
        _game._playerTwo.sendPacket(sm);
      } catch (Exception e) {
      }
      if ((toAll) && (_game._spectators != null))
      {
        for (L2PcInstance spec : _game._spectators)
          try {
            spec.sendPacket(sm);
          }
          catch (NullPointerException e)
          {
          }
      }
    }
  }
}