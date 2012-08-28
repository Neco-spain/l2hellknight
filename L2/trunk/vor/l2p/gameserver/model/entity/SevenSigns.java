package l2p.gameserver.model.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import l2p.commons.dbutils.DbUtils;
import l2p.commons.listener.Listener;
import l2p.commons.listener.ListenerList;
import l2p.commons.threading.RunnableImpl;
import l2p.gameserver.Config;
import l2p.gameserver.GameServer;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.database.DatabaseFactory;
import l2p.gameserver.instancemanager.AutoSpawnManager;
import l2p.gameserver.instancemanager.AutoSpawnManager.AutoSpawnInstance;
import l2p.gameserver.instancemanager.RaidBossSpawnManager;
import l2p.gameserver.listener.GameListener;
import l2p.gameserver.listener.game.OnSSPeriodListener;
import l2p.gameserver.listener.game.OnStartListener;
import l2p.gameserver.model.GameObjectsStorage;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.entity.SevenSignsFestival.SevenSignsFestival;
import l2p.gameserver.serverpackets.SSQInfo;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.templates.StatsSet;
import l2p.gameserver.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SevenSigns
{
  private static final Logger _log = LoggerFactory.getLogger(SevenSigns.class);
  private static SevenSigns _instance;
  private ScheduledFuture<?> _periodChange;
  public static final String SEVEN_SIGNS_HTML_PATH = "seven_signs/";
  public static final int CABAL_NULL = 0;
  public static final int CABAL_DUSK = 1;
  public static final int CABAL_DAWN = 2;
  public static final int SEAL_NULL = 0;
  public static final int SEAL_AVARICE = 1;
  public static final int SEAL_GNOSIS = 2;
  public static final int SEAL_STRIFE = 3;
  public static final int PERIOD_COMP_RECRUITING = 0;
  public static final int PERIOD_COMPETITION = 1;
  public static final int PERIOD_COMP_RESULTS = 2;
  public static final int PERIOD_SEAL_VALIDATION = 3;
  public static final int PERIOD_START_HOUR = 18;
  public static final int PERIOD_START_MINS = 0;
  public static final int PERIOD_START_DAY = 2;
  public static final int PERIOD_MINOR_LENGTH = 900000;
  public static final int PERIOD_MAJOR_LENGTH = 603900000;
  public static final int ANCIENT_ADENA_ID = 5575;
  public static final int RECORD_SEVEN_SIGNS_ID = 5707;
  public static final int CERTIFICATE_OF_APPROVAL_ID = 6388;
  public static final int RECORD_SEVEN_SIGNS_COST = 500;
  public static final int ADENA_JOIN_DAWN_COST = 50000;
  public static final int ORATOR_NPC_ID = 31094;
  public static final int PREACHER_NPC_ID = 31093;
  public static final int MAMMON_MERCHANT_ID = 31113;
  public static final int MAMMON_BLACKSMITH_ID = 31126;
  public static final int MAMMON_MARKETEER_ID = 31092;
  public static final int SPIRIT_IN_ID = 31111;
  public static final int SPIRIT_OUT_ID = 31112;
  public static final int LILITH_NPC_ID = 25283;
  public static final int ANAKIM_NPC_ID = 25286;
  public static final int CREST_OF_DAWN_ID = 31170;
  public static final int CREST_OF_DUSK_ID = 31171;
  public static final int SEAL_STONE_BLUE_ID = 6360;
  public static final int SEAL_STONE_GREEN_ID = 6361;
  public static final int SEAL_STONE_RED_ID = 6362;
  public static final int SEAL_STONE_BLUE_VALUE = 3;
  public static final int SEAL_STONE_GREEN_VALUE = 5;
  public static final int SEAL_STONE_RED_VALUE = 10;
  public static final int BLUE_CONTRIB_POINTS = 3;
  public static final int GREEN_CONTRIB_POINTS = 5;
  public static final int RED_CONTRIB_POINTS = 10;
  public static final long MAXIMUM_PLAYER_CONTRIB = Math.round(1000000.0D * Config.RATE_DROP_ITEMS);

  private final Calendar _calendar = Calendar.getInstance();
  protected int _activePeriod;
  protected int _currentCycle;
  protected long _dawnStoneScore;
  protected long _duskStoneScore;
  protected long _dawnFestivalScore;
  protected long _duskFestivalScore;
  protected int _compWinner;
  protected int _previousWinner;
  private Map<Integer, StatsSet> _signsPlayerData;
  private Map<Integer, Integer> _signsSealOwners;
  private Map<Integer, Integer> _signsDuskSealTotals;
  private Map<Integer, Integer> _signsDawnSealTotals;
  private SSListenerList _listenerList = new SSListenerList();

  public SevenSigns()
  {
    GameServer.getInstance().addListener(new OnStartListenerImpl(null));

    _signsPlayerData = new ConcurrentHashMap();
    _signsSealOwners = new ConcurrentHashMap();
    _signsDuskSealTotals = new ConcurrentHashMap();
    _signsDawnSealTotals = new ConcurrentHashMap();
    try
    {
      restoreSevenSignsData();
    }
    catch (Exception e)
    {
      _log.error(new StringBuilder().append("SevenSigns: Failed to load configuration: ").append(e).toString());
      _log.error("", e);
    }

    _log.info(new StringBuilder().append("SevenSigns: Currently in the ").append(getCurrentPeriodName()).append(" period!").toString());
    initializeSeals();

    if (isSealValidationPeriod())
    {
      if (getCabalHighestScore() == 0)
        _log.info("SevenSigns: The Competition last week ended with a tie.");
      else
        _log.info(new StringBuilder().append("SevenSigns: The ").append(getCabalName(getCabalHighestScore())).append(" were victorious last week.").toString());
    }
    else if (getCabalHighestScore() == 0)
      _log.info("SevenSigns: The Competition this week, if the trend continue, will end with a tie.");
    else {
      _log.info(new StringBuilder().append("SevenSigns: The ").append(getCabalName(getCabalHighestScore())).append(" are in the lead this week.").toString());
    }
    int numMins = 0;
    int numHours = 0;
    int numDays = 0;

    setCalendarForNextPeriodChange();
    long milliToChange = getMilliToPeriodChange();
    if (milliToChange < 10L) {
      milliToChange = 10L;
    }
    _periodChange = ThreadPoolManager.getInstance().schedule(new SevenSignsPeriodChange(), milliToChange);

    double numSecs = milliToChange / 1000L % 60L;
    double countDown = (milliToChange / 1000L - numSecs) / 60.0D;
    numMins = (int)Math.floor(countDown % 60.0D);
    countDown = (countDown - numMins) / 60.0D;
    numHours = (int)Math.floor(countDown % 24.0D);
    numDays = (int)Math.floor((countDown - numHours) / 24.0D);

    _log.info(new StringBuilder().append("SevenSigns: Next period begins in ").append(numDays).append(" days, ").append(numHours).append(" hours and ").append(numMins).append(" mins.").toString());

    if (Config.SS_ANNOUNCE_PERIOD > 0)
      ThreadPoolManager.getInstance().schedule(new SevenSignsAnnounce(), Config.SS_ANNOUNCE_PERIOD * 1000L * 60L);
  }

  public void spawnSevenSignsNPC()
  {
    AutoSpawnManager.AutoSpawnInstance _merchantSpawn = AutoSpawnManager.getInstance().getAutoSpawnInstance(31113, false);
    AutoSpawnManager.AutoSpawnInstance _blacksmithSpawn = AutoSpawnManager.getInstance().getAutoSpawnInstance(31126, false);
    Map _marketeerSpawns = AutoSpawnManager.getInstance().getAllAutoSpawnInstance(31092);
    AutoSpawnManager.AutoSpawnInstance _spiritInSpawn = AutoSpawnManager.getInstance().getAutoSpawnInstance(31111, false);
    AutoSpawnManager.AutoSpawnInstance _spiritOutSpawn = AutoSpawnManager.getInstance().getAutoSpawnInstance(31112, false);
    AutoSpawnManager.AutoSpawnInstance _lilithSpawn = AutoSpawnManager.getInstance().getAutoSpawnInstance(25283, false);
    AutoSpawnManager.AutoSpawnInstance _anakimSpawn = AutoSpawnManager.getInstance().getAutoSpawnInstance(25286, false);
    AutoSpawnManager.AutoSpawnInstance _crestOfDawnSpawn = AutoSpawnManager.getInstance().getAutoSpawnInstance(31170, false);
    AutoSpawnManager.AutoSpawnInstance _crestOfDuskSpawn = AutoSpawnManager.getInstance().getAutoSpawnInstance(31171, false);
    Map _oratorSpawns = AutoSpawnManager.getInstance().getAllAutoSpawnInstance(31094);
    Map _preacherSpawns = AutoSpawnManager.getInstance().getAllAutoSpawnInstance(31093);

    if ((isSealValidationPeriod()) || (isCompResultsPeriod()))
    {
      for (AutoSpawnManager.AutoSpawnInstance spawnInst : _marketeerSpawns.values()) {
        AutoSpawnManager.getInstance().setSpawnActive(spawnInst, true);
      }
      if ((getSealOwner(2) == getCabalHighestScore()) && (getSealOwner(2) != 0))
      {
        if (!Config.ANNOUNCE_MAMMON_SPAWN) {
          _blacksmithSpawn.setBroadcast(false);
        }
        if (!AutoSpawnManager.getInstance().getAutoSpawnInstance(_blacksmithSpawn.getObjectId(), true).isSpawnActive())
          AutoSpawnManager.getInstance().setSpawnActive(_blacksmithSpawn, true);
        for (AutoSpawnManager.AutoSpawnInstance spawnInst : _oratorSpawns.values())
          if (!AutoSpawnManager.getInstance().getAutoSpawnInstance(spawnInst.getObjectId(), true).isSpawnActive())
            AutoSpawnManager.getInstance().setSpawnActive(spawnInst, true);
        for (AutoSpawnManager.AutoSpawnInstance spawnInst : _preacherSpawns.values())
          if (!AutoSpawnManager.getInstance().getAutoSpawnInstance(spawnInst.getObjectId(), true).isSpawnActive())
            AutoSpawnManager.getInstance().setSpawnActive(spawnInst, true);
      }
      else
      {
        AutoSpawnManager.getInstance().setSpawnActive(_blacksmithSpawn, false);
        for (AutoSpawnManager.AutoSpawnInstance spawnInst : _oratorSpawns.values())
          AutoSpawnManager.getInstance().setSpawnActive(spawnInst, false);
        for (AutoSpawnManager.AutoSpawnInstance spawnInst : _preacherSpawns.values()) {
          AutoSpawnManager.getInstance().setSpawnActive(spawnInst, false);
        }
      }
      if ((getSealOwner(1) == getCabalHighestScore()) && (getSealOwner(1) != 0))
      {
        if (!Config.ANNOUNCE_MAMMON_SPAWN) {
          _merchantSpawn.setBroadcast(false);
        }
        if (!AutoSpawnManager.getInstance().getAutoSpawnInstance(_merchantSpawn.getObjectId(), true).isSpawnActive()) {
          AutoSpawnManager.getInstance().setSpawnActive(_merchantSpawn, true);
        }
        if (!AutoSpawnManager.getInstance().getAutoSpawnInstance(_spiritInSpawn.getObjectId(), true).isSpawnActive())
          AutoSpawnManager.getInstance().setSpawnActive(_spiritInSpawn, true);
        if (!AutoSpawnManager.getInstance().getAutoSpawnInstance(_spiritOutSpawn.getObjectId(), true).isSpawnActive()) {
          AutoSpawnManager.getInstance().setSpawnActive(_spiritOutSpawn, true);
        }
        switch (getCabalHighestScore())
        {
        case 2:
          if (!AutoSpawnManager.getInstance().getAutoSpawnInstance(_lilithSpawn.getObjectId(), true).isSpawnActive())
            AutoSpawnManager.getInstance().setSpawnActive(_lilithSpawn, true);
          AutoSpawnManager.getInstance().setSpawnActive(_anakimSpawn, false);
          if (!AutoSpawnManager.getInstance().getAutoSpawnInstance(_crestOfDawnSpawn.getObjectId(), true).isSpawnActive())
            AutoSpawnManager.getInstance().setSpawnActive(_crestOfDawnSpawn, true);
          AutoSpawnManager.getInstance().setSpawnActive(_crestOfDuskSpawn, false);
          break;
        case 1:
          if (!AutoSpawnManager.getInstance().getAutoSpawnInstance(_anakimSpawn.getObjectId(), true).isSpawnActive())
            AutoSpawnManager.getInstance().setSpawnActive(_anakimSpawn, true);
          AutoSpawnManager.getInstance().setSpawnActive(_lilithSpawn, false);
          if (!AutoSpawnManager.getInstance().getAutoSpawnInstance(_crestOfDuskSpawn.getObjectId(), true).isSpawnActive())
            AutoSpawnManager.getInstance().setSpawnActive(_crestOfDuskSpawn, true);
          AutoSpawnManager.getInstance().setSpawnActive(_crestOfDawnSpawn, false);
        }

      }
      else
      {
        AutoSpawnManager.getInstance().setSpawnActive(_merchantSpawn, false);
        AutoSpawnManager.getInstance().setSpawnActive(_lilithSpawn, false);
        AutoSpawnManager.getInstance().setSpawnActive(_anakimSpawn, false);
        AutoSpawnManager.getInstance().setSpawnActive(_crestOfDawnSpawn, false);
        AutoSpawnManager.getInstance().setSpawnActive(_crestOfDuskSpawn, false);
        AutoSpawnManager.getInstance().setSpawnActive(_spiritInSpawn, false);
        AutoSpawnManager.getInstance().setSpawnActive(_spiritOutSpawn, false);
      }
    }
    else
    {
      AutoSpawnManager.getInstance().setSpawnActive(_merchantSpawn, false);
      AutoSpawnManager.getInstance().setSpawnActive(_blacksmithSpawn, false);
      AutoSpawnManager.getInstance().setSpawnActive(_lilithSpawn, false);
      AutoSpawnManager.getInstance().setSpawnActive(_anakimSpawn, false);
      AutoSpawnManager.getInstance().setSpawnActive(_crestOfDawnSpawn, false);
      AutoSpawnManager.getInstance().setSpawnActive(_crestOfDuskSpawn, false);
      AutoSpawnManager.getInstance().setSpawnActive(_spiritInSpawn, false);
      AutoSpawnManager.getInstance().setSpawnActive(_spiritOutSpawn, false);
      for (AutoSpawnManager.AutoSpawnInstance spawnInst : _oratorSpawns.values())
        AutoSpawnManager.getInstance().setSpawnActive(spawnInst, false);
      for (AutoSpawnManager.AutoSpawnInstance spawnInst : _preacherSpawns.values())
        AutoSpawnManager.getInstance().setSpawnActive(spawnInst, false);
      for (AutoSpawnManager.AutoSpawnInstance spawnInst : _marketeerSpawns.values())
        AutoSpawnManager.getInstance().setSpawnActive(spawnInst, false);
    }
  }

  public static SevenSigns getInstance()
  {
    if (_instance == null)
      _instance = new SevenSigns();
    return _instance;
  }

  public static long calcContributionScore(long blueCount, long greenCount, long redCount)
  {
    long contrib = blueCount * 3L;
    contrib += greenCount * 5L;
    contrib += redCount * 10L;

    return contrib;
  }

  public static long calcAncientAdenaReward(long blueCount, long greenCount, long redCount)
  {
    long reward = blueCount * 3L;
    reward += greenCount * 5L;
    reward += redCount * 10L;

    return reward;
  }

  public static int getCabalNumber(String cabal)
  {
    if (cabal.equalsIgnoreCase("dawn"))
      return 2;
    if (cabal.equalsIgnoreCase("dusk")) {
      return 1;
    }
    return 0;
  }

  public static String getCabalShortName(int cabal)
  {
    switch (cabal)
    {
    case 2:
      return "dawn";
    case 1:
      return "dusk";
    }
    return "No Cabal";
  }

  public static String getCabalName(int cabal)
  {
    switch (cabal)
    {
    case 2:
      return "Lords of Dawn";
    case 1:
      return "Revolutionaries of Dusk";
    }
    return "No Cabal";
  }

  public static String getSealName(int seal, boolean shortName)
  {
    String sealName = !shortName ? "Seal of " : "";

    switch (seal)
    {
    case 1:
      sealName = new StringBuilder().append(sealName).append("Avarice").toString();
      break;
    case 2:
      sealName = new StringBuilder().append(sealName).append("Gnosis").toString();
      break;
    case 3:
      sealName = new StringBuilder().append(sealName).append("Strife").toString();
    }

    return sealName;
  }

  public static String capitalizeWords(String str)
  {
    char[] charArray = str.toCharArray();
    StringBuilder buf = new StringBuilder();

    charArray[0] = Character.toUpperCase(charArray[0]);
    for (int i = 0; i < charArray.length; i++)
    {
      if ((Character.isWhitespace(charArray[i])) && (i != charArray.length - 1)) {
        charArray[(i + 1)] = Character.toUpperCase(charArray[(i + 1)]);
      }
      buf.append(Character.toString(charArray[i]));
    }

    return buf.toString();
  }

  public final int getCurrentCycle()
  {
    return _currentCycle;
  }

  public final int getCurrentPeriod()
  {
    return _activePeriod;
  }

  private int getDaysToPeriodChange()
  {
    int numDays = _calendar.get(7) - 2;

    if (numDays < 0) {
      return 0 - numDays;
    }
    return 7 - numDays;
  }

  public final long getMilliToPeriodChange()
  {
    return _calendar.getTimeInMillis() - System.currentTimeMillis();
  }

  protected void setCalendarForNextPeriodChange()
  {
    switch (getCurrentPeriod())
    {
    case 1:
    case 3:
      int daysToChange = getDaysToPeriodChange();

      if (daysToChange == 7) {
        if (_calendar.get(11) < 18)
          daysToChange = 0;
        else if ((_calendar.get(11) == 18) && (_calendar.get(12) < 0)) {
          daysToChange = 0;
        }
      }
      if (daysToChange > 0)
        _calendar.add(5, daysToChange);
      _calendar.set(11, 18);
      _calendar.set(12, 0);
      break;
    case 0:
    case 2:
      _calendar.add(14, 900000);
    }
  }

  public final String getCurrentPeriodName()
  {
    String periodName = null;

    switch (_activePeriod)
    {
    case 0:
      periodName = "Quest Event Initialization";
      break;
    case 1:
      periodName = "Competition (Quest Event)";
      break;
    case 2:
      periodName = "Quest Event Results";
      break;
    case 3:
      periodName = "Seal Validation";
    }

    return periodName;
  }

  public final boolean isSealValidationPeriod()
  {
    return _activePeriod == 3;
  }

  public final boolean isCompResultsPeriod()
  {
    return _activePeriod == 2;
  }

  public final long getCurrentScore(int cabal)
  {
    double totalStoneScore = _dawnStoneScore + _duskStoneScore;

    switch (cabal)
    {
    case 0:
      return 0L;
    case 2:
      return Math.round(_dawnStoneScore / (totalStoneScore == 0.0D ? 1.0D : totalStoneScore) * 500.0D) + _dawnFestivalScore;
    case 1:
      return Math.round(_duskStoneScore / (totalStoneScore == 0.0D ? 1.0D : totalStoneScore) * 500.0D) + _duskFestivalScore;
    }
    return 0L;
  }

  public final long getCurrentStoneScore(int cabal)
  {
    switch (cabal)
    {
    case 0:
      return 0L;
    case 2:
      return _dawnStoneScore;
    case 1:
      return _duskStoneScore;
    }
    return 0L;
  }

  public final long getCurrentFestivalScore(int cabal)
  {
    switch (cabal)
    {
    case 0:
      return 0L;
    case 2:
      return _dawnFestivalScore;
    case 1:
      return _duskFestivalScore;
    }
    return 0L;
  }

  public final int getCabalHighestScore()
  {
    long diff = getCurrentScore(1) - getCurrentScore(2);
    if (diff == 0L)
      return 0;
    if (diff > 0L) {
      return 1;
    }
    return 2;
  }

  public final int getSealOwner(int seal)
  {
    if ((_signsSealOwners == null) || (!_signsSealOwners.containsKey(Integer.valueOf(seal))))
      return 0;
    return ((Integer)_signsSealOwners.get(Integer.valueOf(seal))).intValue();
  }

  public final int getSealProportion(int seal, int cabal)
  {
    if (cabal == 0)
      return 0;
    if (cabal == 1) {
      return ((Integer)_signsDuskSealTotals.get(Integer.valueOf(seal))).intValue();
    }
    return ((Integer)_signsDawnSealTotals.get(Integer.valueOf(seal))).intValue();
  }

  public final int getTotalMembers(int cabal)
  {
    int cabalMembers = 0;

    for (StatsSet sevenDat : _signsPlayerData.values()) {
      if (sevenDat.getInteger("cabal") == cabal)
        cabalMembers++;
    }
    return cabalMembers;
  }

  public final StatsSet getPlayerStatsSet(Player player)
  {
    if (!hasRegisteredBefore(player.getObjectId())) {
      return null;
    }
    return (StatsSet)_signsPlayerData.get(Integer.valueOf(player.getObjectId()));
  }

  public long getPlayerStoneContrib(Player player)
  {
    if (!hasRegisteredBefore(player.getObjectId())) {
      return 0L;
    }
    long stoneCount = 0L;

    StatsSet currPlayer = (StatsSet)_signsPlayerData.get(Integer.valueOf(player.getObjectId()));

    if (getPlayerCabal(player) == 2)
    {
      stoneCount += currPlayer.getLong("dawn_red_stones");
      stoneCount += currPlayer.getLong("dawn_green_stones");
      stoneCount += currPlayer.getLong("dawn_blue_stones");
    }
    else
    {
      stoneCount += currPlayer.getLong("dusk_red_stones");
      stoneCount += currPlayer.getLong("dusk_green_stones");
      stoneCount += currPlayer.getLong("dusk_blue_stones");
    }

    return stoneCount;
  }

  public long getPlayerContribScore(Player player)
  {
    if (!hasRegisteredBefore(player.getObjectId())) {
      return 0L;
    }
    StatsSet currPlayer = (StatsSet)_signsPlayerData.get(Integer.valueOf(player.getObjectId()));
    if (getPlayerCabal(player) == 2)
      return currPlayer.getInteger("dawn_contribution_score");
    return currPlayer.getInteger("dusk_contribution_score");
  }

  public long getPlayerAdenaCollect(Player player)
  {
    if (!hasRegisteredBefore(player.getObjectId()))
      return 0L;
    return ((StatsSet)_signsPlayerData.get(Integer.valueOf(player.getObjectId()))).getLong(getPlayerCabal(player) == 2 ? "dawn_ancient_adena_amount" : "dusk_ancient_adena_amount");
  }

  public int getPlayerSeal(Player player)
  {
    if (!hasRegisteredBefore(player.getObjectId())) {
      return 0;
    }
    return ((StatsSet)_signsPlayerData.get(Integer.valueOf(player.getObjectId()))).getInteger("seal");
  }

  public int getPlayerCabal(Player player)
  {
    if (!hasRegisteredBefore(player.getObjectId())) {
      return 0;
    }
    return ((StatsSet)_signsPlayerData.get(Integer.valueOf(player.getObjectId()))).getInteger("cabal");
  }

  protected void restoreSevenSignsData()
  {
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT char_obj_id, cabal, seal, dawn_red_stones, dawn_green_stones, dawn_blue_stones, dawn_ancient_adena_amount, dawn_contribution_score, dusk_red_stones, dusk_green_stones, dusk_blue_stones, dusk_ancient_adena_amount, dusk_contribution_score FROM seven_signs");
      rset = statement.executeQuery();
      while (rset.next())
      {
        int charObjId = rset.getInt("char_obj_id");

        StatsSet sevenDat = new StatsSet();
        sevenDat.set("char_obj_id", charObjId);
        sevenDat.set("cabal", getCabalNumber(rset.getString("cabal")));
        sevenDat.set("seal", rset.getInt("seal"));
        sevenDat.set("dawn_red_stones", rset.getInt("dawn_red_stones"));
        sevenDat.set("dawn_green_stones", rset.getInt("dawn_green_stones"));
        sevenDat.set("dawn_blue_stones", rset.getInt("dawn_blue_stones"));
        sevenDat.set("dawn_ancient_adena_amount", rset.getInt("dawn_ancient_adena_amount"));
        sevenDat.set("dawn_contribution_score", rset.getInt("dawn_contribution_score"));
        sevenDat.set("dusk_red_stones", rset.getInt("dusk_red_stones"));
        sevenDat.set("dusk_green_stones", rset.getInt("dusk_green_stones"));
        sevenDat.set("dusk_blue_stones", rset.getInt("dusk_blue_stones"));
        sevenDat.set("dusk_ancient_adena_amount", rset.getInt("dusk_ancient_adena_amount"));
        sevenDat.set("dusk_contribution_score", rset.getInt("dusk_contribution_score"));

        _signsPlayerData.put(Integer.valueOf(charObjId), sevenDat);
      }
      DbUtils.close(statement, rset);

      statement = con.prepareStatement("SELECT * FROM seven_signs_status");
      rset = statement.executeQuery();

      while (rset.next())
      {
        _currentCycle = rset.getInt("current_cycle");
        _activePeriod = rset.getInt("active_period");
        _previousWinner = rset.getInt("previous_winner");

        _dawnStoneScore = rset.getLong("dawn_stone_score");
        _dawnFestivalScore = rset.getLong("dawn_festival_score");
        _duskStoneScore = rset.getLong("dusk_stone_score");
        _duskFestivalScore = rset.getLong("dusk_festival_score");

        _signsSealOwners.put(Integer.valueOf(1), Integer.valueOf(rset.getInt("avarice_owner")));
        _signsSealOwners.put(Integer.valueOf(2), Integer.valueOf(rset.getInt("gnosis_owner")));
        _signsSealOwners.put(Integer.valueOf(3), Integer.valueOf(rset.getInt("strife_owner")));

        _signsDawnSealTotals.put(Integer.valueOf(1), Integer.valueOf(rset.getInt("avarice_dawn_score")));
        _signsDawnSealTotals.put(Integer.valueOf(2), Integer.valueOf(rset.getInt("gnosis_dawn_score")));
        _signsDawnSealTotals.put(Integer.valueOf(3), Integer.valueOf(rset.getInt("strife_dawn_score")));
        _signsDuskSealTotals.put(Integer.valueOf(1), Integer.valueOf(rset.getInt("avarice_dusk_score")));
        _signsDuskSealTotals.put(Integer.valueOf(2), Integer.valueOf(rset.getInt("gnosis_dusk_score")));
        _signsDuskSealTotals.put(Integer.valueOf(3), Integer.valueOf(rset.getInt("strife_dusk_score")));
      }
      DbUtils.close(statement, rset);

      statement = con.prepareStatement("UPDATE seven_signs_status SET date=?");
      statement.setInt(1, Calendar.getInstance().get(7));
      statement.execute();
    }
    catch (SQLException e)
    {
      _log.error(new StringBuilder().append("Unable to load Seven Signs Data: ").append(e).toString());
    }
    finally
    {
      DbUtils.closeQuietly(con, statement, rset);
    }
  }

  public synchronized void saveSevenSignsData(int playerId, boolean updateSettings)
  {
    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("UPDATE seven_signs SET cabal=?, seal=?, dawn_red_stones=?, dawn_green_stones=?, dawn_blue_stones=?, dawn_ancient_adena_amount=?, dawn_contribution_score=?, dusk_red_stones=?, dusk_green_stones=?, dusk_blue_stones=?, dusk_ancient_adena_amount=?, dusk_contribution_score=? WHERE char_obj_id=?");

      if (playerId > 0)
        processStatement(statement, (StatsSet)_signsPlayerData.get(Integer.valueOf(playerId)));
      else {
        for (StatsSet sevenDat : _signsPlayerData.values())
          processStatement(statement, sevenDat);
      }
      DbUtils.close(statement);

      if (updateSettings)
      {
        StringBuilder buf = new StringBuilder();

        buf.append("UPDATE seven_signs_status SET current_cycle=?, active_period=?, previous_winner=?, dawn_stone_score=?, dawn_festival_score=?, dusk_stone_score=?, dusk_festival_score=?, avarice_owner=?, gnosis_owner=?, strife_owner=?, avarice_dawn_score=?, gnosis_dawn_score=?, strife_dawn_score=?, avarice_dusk_score=?, gnosis_dusk_score=?, strife_dusk_score=?, festival_cycle=?, ");

        for (int i = 0; i < 5; i++)
          buf.append(new StringBuilder().append("accumulated_bonus").append(String.valueOf(i)).append("=?, ").toString());
        buf.append("date=?");

        statement = con.prepareStatement(buf.toString());
        statement.setInt(1, _currentCycle);
        statement.setInt(2, _activePeriod);
        statement.setInt(3, _previousWinner);
        statement.setLong(4, _dawnStoneScore);
        statement.setLong(5, _dawnFestivalScore);
        statement.setLong(6, _duskStoneScore);
        statement.setLong(7, _duskFestivalScore);
        statement.setInt(8, ((Integer)_signsSealOwners.get(Integer.valueOf(1))).intValue());
        statement.setInt(9, ((Integer)_signsSealOwners.get(Integer.valueOf(2))).intValue());
        statement.setInt(10, ((Integer)_signsSealOwners.get(Integer.valueOf(3))).intValue());
        statement.setInt(11, ((Integer)_signsDawnSealTotals.get(Integer.valueOf(1))).intValue());
        statement.setInt(12, ((Integer)_signsDawnSealTotals.get(Integer.valueOf(2))).intValue());
        statement.setInt(13, ((Integer)_signsDawnSealTotals.get(Integer.valueOf(3))).intValue());
        statement.setInt(14, ((Integer)_signsDuskSealTotals.get(Integer.valueOf(1))).intValue());
        statement.setInt(15, ((Integer)_signsDuskSealTotals.get(Integer.valueOf(2))).intValue());
        statement.setInt(16, ((Integer)_signsDuskSealTotals.get(Integer.valueOf(3))).intValue());

        statement.setInt(17, getCurrentCycle());
        for (int i = 0; i < 5; i++)
          statement.setLong(18 + i, SevenSignsFestival.getInstance().getAccumulatedBonus(i));
        statement.setInt(23, Calendar.getInstance().get(7));
        statement.executeUpdate();
      }
    }
    catch (SQLException e)
    {
      _log.error(new StringBuilder().append("Unable to save Seven Signs data: ").append(e).toString());
      _log.error("", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }
  }

  private static void processStatement(PreparedStatement statement, StatsSet sevenDat) throws SQLException
  {
    statement.setString(1, getCabalShortName(sevenDat.getInteger("cabal")));
    statement.setInt(2, sevenDat.getInteger("seal"));
    statement.setInt(3, sevenDat.getInteger("dawn_red_stones"));
    statement.setInt(4, sevenDat.getInteger("dawn_green_stones"));
    statement.setInt(5, sevenDat.getInteger("dawn_blue_stones"));
    statement.setInt(6, sevenDat.getInteger("dawn_ancient_adena_amount"));
    statement.setInt(7, sevenDat.getInteger("dawn_contribution_score"));
    statement.setInt(8, sevenDat.getInteger("dusk_red_stones"));
    statement.setInt(9, sevenDat.getInteger("dusk_green_stones"));
    statement.setInt(10, sevenDat.getInteger("dusk_blue_stones"));
    statement.setInt(11, sevenDat.getInteger("dusk_ancient_adena_amount"));
    statement.setInt(12, sevenDat.getInteger("dusk_contribution_score"));
    statement.setInt(13, sevenDat.getInteger("char_obj_id"));
    statement.executeUpdate();
  }

  protected void resetPlayerData()
  {
    for (StatsSet sevenDat : _signsPlayerData.values())
    {
      int charObjId = sevenDat.getInteger("char_obj_id");

      if (sevenDat.getInteger("cabal") == getCabalHighestScore()) {
        switch (getCabalHighestScore())
        {
        case 2:
          sevenDat.set("dawn_red_stones", 0);
          sevenDat.set("dawn_green_stones", 0);
          sevenDat.set("dawn_blue_stones", 0);
          sevenDat.set("dawn_contribution_score", 0);
          break;
        case 1:
          sevenDat.set("dusk_red_stones", 0);
          sevenDat.set("dusk_green_stones", 0);
          sevenDat.set("dusk_blue_stones", 0);
          sevenDat.set("dusk_contribution_score", 0);
        }
      }
      else if ((sevenDat.getInteger("cabal") == 2) || (sevenDat.getInteger("cabal") == 0))
      {
        sevenDat.set("dusk_red_stones", 0);
        sevenDat.set("dusk_green_stones", 0);
        sevenDat.set("dusk_blue_stones", 0);
        sevenDat.set("dusk_contribution_score", 0);
      }
      else if ((sevenDat.getInteger("cabal") == 1) || (sevenDat.getInteger("cabal") == 0))
      {
        sevenDat.set("dawn_red_stones", 0);
        sevenDat.set("dawn_green_stones", 0);
        sevenDat.set("dawn_blue_stones", 0);
        sevenDat.set("dawn_contribution_score", 0);
      }

      sevenDat.set("cabal", 0);
      sevenDat.set("seal", 0);
      _signsPlayerData.put(Integer.valueOf(charObjId), sevenDat);
    }
  }

  private boolean hasRegisteredBefore(int charObjId)
  {
    return _signsPlayerData.containsKey(Integer.valueOf(charObjId));
  }

  public int setPlayerInfo(int charObjId, int chosenCabal, int chosenSeal)
  {
    StatsSet currPlayer = null;
    if (hasRegisteredBefore(charObjId))
    {
      currPlayer = (StatsSet)_signsPlayerData.get(Integer.valueOf(charObjId));
      currPlayer.set("cabal", chosenCabal);
      currPlayer.set("seal", chosenSeal);

      _signsPlayerData.put(Integer.valueOf(charObjId), currPlayer);
    }
    else
    {
      currPlayer = new StatsSet();
      currPlayer.set("char_obj_id", charObjId);
      currPlayer.set("cabal", chosenCabal);
      currPlayer.set("seal", chosenSeal);
      currPlayer.set("dawn_red_stones", 0);
      currPlayer.set("dawn_green_stones", 0);
      currPlayer.set("dawn_blue_stones", 0);
      currPlayer.set("dawn_ancient_adena_amount", 0);
      currPlayer.set("dawn_contribution_score", 0);
      currPlayer.set("dusk_red_stones", 0);
      currPlayer.set("dusk_green_stones", 0);
      currPlayer.set("dusk_blue_stones", 0);
      currPlayer.set("dusk_ancient_adena_amount", 0);
      currPlayer.set("dusk_contribution_score", 0);

      _signsPlayerData.put(Integer.valueOf(charObjId), currPlayer);

      Connection con = null;
      PreparedStatement statement = null;
      try
      {
        con = DatabaseFactory.getInstance().getConnection();
        statement = con.prepareStatement("INSERT INTO seven_signs (char_obj_id, cabal, seal) VALUES (?,?,?)");
        statement.setInt(1, charObjId);
        statement.setString(2, getCabalShortName(chosenCabal));
        statement.setInt(3, chosenSeal);
        statement.execute();
      }
      catch (SQLException e)
      {
        _log.error(new StringBuilder().append("SevenSigns: Failed to save data: ").append(e).toString());
      }
      finally
      {
        DbUtils.closeQuietly(con, statement);
      }
    }
    long contribScore = 0L;

    switch (chosenCabal)
    {
    case 2:
      contribScore = calcContributionScore(currPlayer.getInteger("dawn_blue_stones"), currPlayer.getInteger("dawn_green_stones"), currPlayer.getInteger("dawn_red_stones"));
      _dawnStoneScore += contribScore;
      break;
    case 1:
      contribScore = calcContributionScore(currPlayer.getInteger("dusk_blue_stones"), currPlayer.getInteger("dusk_green_stones"), currPlayer.getInteger("dusk_red_stones"));
      _duskStoneScore += contribScore;
    }

    if (currPlayer.getInteger("cabal") == 2)
      _signsDawnSealTotals.put(Integer.valueOf(chosenSeal), Integer.valueOf(((Integer)_signsDawnSealTotals.get(Integer.valueOf(chosenSeal))).intValue() + 1));
    else {
      _signsDuskSealTotals.put(Integer.valueOf(chosenSeal), Integer.valueOf(((Integer)_signsDuskSealTotals.get(Integer.valueOf(chosenSeal))).intValue() + 1));
    }
    saveSevenSignsData(charObjId, true);

    return chosenCabal;
  }

  public int getAncientAdenaReward(Player player, boolean removeReward)
  {
    int charObjId = player.getObjectId();
    StatsSet currPlayer = (StatsSet)_signsPlayerData.get(Integer.valueOf(charObjId));

    int rewardAmount = 0;
    if (currPlayer.getInteger("cabal") == 2)
    {
      rewardAmount = currPlayer.getInteger("dawn_ancient_adena_amount");
      currPlayer.set("dawn_ancient_adena_amount", 0);
    }
    else
    {
      rewardAmount = currPlayer.getInteger("dusk_ancient_adena_amount");
      currPlayer.set("dusk_ancient_adena_amount", 0);
    }

    if (removeReward)
    {
      _signsPlayerData.put(Integer.valueOf(charObjId), currPlayer);
      saveSevenSignsData(charObjId, false);
    }

    return rewardAmount;
  }

  public long addPlayerStoneContrib(Player player, long blueCount, long greenCount, long redCount)
  {
    return addPlayerStoneContrib(player.getObjectId(), blueCount, greenCount, redCount);
  }

  public long addPlayerStoneContrib(int charObjId, long blueCount, long greenCount, long redCount)
  {
    StatsSet currPlayer = (StatsSet)_signsPlayerData.get(Integer.valueOf(charObjId));

    long contribScore = calcContributionScore(blueCount, greenCount, redCount);
    long totalAncientAdena = 0L;
    long totalContribScore = 0L;

    if (currPlayer.getInteger("cabal") == 2)
    {
      totalAncientAdena = currPlayer.getInteger("dawn_ancient_adena_amount") + calcAncientAdenaReward(blueCount, greenCount, redCount);
      totalContribScore = currPlayer.getInteger("dawn_contribution_score") + contribScore;

      if (totalContribScore > MAXIMUM_PLAYER_CONTRIB) {
        return -1L;
      }
      currPlayer.set("dawn_red_stones", currPlayer.getInteger("dawn_red_stones") + redCount);
      currPlayer.set("dawn_green_stones", currPlayer.getInteger("dawn_green_stones") + greenCount);
      currPlayer.set("dawn_blue_stones", currPlayer.getInteger("dawn_blue_stones") + blueCount);
      currPlayer.set("dawn_ancient_adena_amount", totalAncientAdena);
      currPlayer.set("dawn_contribution_score", totalContribScore);
      _signsPlayerData.put(Integer.valueOf(charObjId), currPlayer);
      _dawnStoneScore += contribScore;
    }
    else
    {
      totalAncientAdena = currPlayer.getInteger("dusk_ancient_adena_amount") + calcAncientAdenaReward(blueCount, greenCount, redCount);
      totalContribScore = currPlayer.getInteger("dusk_contribution_score") + contribScore;

      if (totalContribScore > MAXIMUM_PLAYER_CONTRIB) {
        return -1L;
      }
      currPlayer.set("dusk_red_stones", currPlayer.getInteger("dusk_red_stones") + redCount);
      currPlayer.set("dusk_green_stones", currPlayer.getInteger("dusk_green_stones") + greenCount);
      currPlayer.set("dusk_blue_stones", currPlayer.getInteger("dusk_blue_stones") + blueCount);
      currPlayer.set("dusk_ancient_adena_amount", totalAncientAdena);
      currPlayer.set("dusk_contribution_score", totalContribScore);
      _signsPlayerData.put(Integer.valueOf(charObjId), currPlayer);
      _duskStoneScore += contribScore;
    }

    saveSevenSignsData(charObjId, true);

    return contribScore;
  }

  public synchronized void updateFestivalScore()
  {
    _duskFestivalScore = 0L;
    _dawnFestivalScore = 0L;
    for (int i = 0; i < 5; i++)
    {
      long dusk = SevenSignsFestival.getInstance().getHighestScore(1, i);
      long dawn = SevenSignsFestival.getInstance().getHighestScore(2, i);
      if (dusk > dawn)
        _duskFestivalScore += SevenSignsFestival.FESTIVAL_LEVEL_SCORES[i];
      else if (dusk < dawn)
        _dawnFestivalScore += SevenSignsFestival.FESTIVAL_LEVEL_SCORES[i];
    }
  }

  public void sendCurrentPeriodMsg(Player player)
  {
    switch (_activePeriod)
    {
    case 0:
      player.sendPacket(Msg.SEVEN_SIGNS_PREPARATIONS_HAVE_BEGUN_FOR_THE_NEXT_QUEST_EVENT);
      return;
    case 1:
      player.sendPacket(Msg.SEVEN_SIGNS_THE_QUEST_EVENT_PERIOD_HAS_BEGUN_SPEAK_WITH_A_PRIEST_OF_DAWN_OR_DUSK_PRIESTESS_IF_YOU_WISH_TO_PARTICIPATE_IN_THE_EVENT);
      return;
    case 2:
      player.sendPacket(Msg.SEVEN_SIGNS_QUEST_EVENT_HAS_ENDED_RESULTS_ARE_BEING_TALLIED);
      return;
    case 3:
      player.sendPacket(Msg.SEVEN_SIGNS_THIS_IS_THE_SEAL_VALIDATION_PERIOD_A_NEW_QUEST_EVENT_PERIOD_BEGINS_NEXT_MONDAY);
      return;
    }
  }

  public void sendMessageToAll(int sysMsgId)
  {
    SystemMessage sm = new SystemMessage(sysMsgId);
    for (Player player : GameObjectsStorage.getAllPlayersForIterate())
      player.sendPacket(sm);
  }

  protected void initializeSeals()
  {
    for (Integer currSeal : _signsSealOwners.keySet())
    {
      int sealOwner = ((Integer)_signsSealOwners.get(currSeal)).intValue();

      if (sealOwner != 0) {
        if (isSealValidationPeriod())
          _log.info(new StringBuilder().append("SevenSigns: The ").append(getCabalName(sealOwner)).append(" have won the ").append(getSealName(currSeal.intValue(), false)).append(".").toString());
        else
          _log.info(new StringBuilder().append("SevenSigns: The ").append(getSealName(currSeal.intValue(), false)).append(" is currently owned by ").append(getCabalName(sealOwner)).append(".").toString());
      }
      else _log.info(new StringBuilder().append("SevenSigns: The ").append(getSealName(currSeal.intValue(), false)).append(" remains unclaimed.").toString());
    }
  }

  protected void resetSeals()
  {
    _signsDawnSealTotals.put(Integer.valueOf(1), Integer.valueOf(0));
    _signsDawnSealTotals.put(Integer.valueOf(2), Integer.valueOf(0));
    _signsDawnSealTotals.put(Integer.valueOf(3), Integer.valueOf(0));
    _signsDuskSealTotals.put(Integer.valueOf(1), Integer.valueOf(0));
    _signsDuskSealTotals.put(Integer.valueOf(2), Integer.valueOf(0));
    _signsDuskSealTotals.put(Integer.valueOf(3), Integer.valueOf(0));
  }

  protected void calcNewSealOwners()
  {
    for (Integer currSeal : _signsDawnSealTotals.keySet())
    {
      int prevSealOwner = ((Integer)_signsSealOwners.get(currSeal)).intValue();
      int newSealOwner = 0;
      int dawnProportion = getSealProportion(currSeal.intValue(), 2);
      int totalDawnMembers = getTotalMembers(2) == 0 ? 1 : getTotalMembers(2);
      int duskProportion = getSealProportion(currSeal.intValue(), 1);
      int totalDuskMembers = getTotalMembers(1) == 0 ? 1 : getTotalMembers(1);

      switch (prevSealOwner)
      {
      case 0:
        switch (getCabalHighestScore())
        {
        case 0:
          if ((dawnProportion >= Math.round(0.35D * totalDawnMembers)) && (dawnProportion > duskProportion))
            newSealOwner = 2;
          else if ((duskProportion >= Math.round(0.35D * totalDuskMembers)) && (duskProportion > dawnProportion))
            newSealOwner = 1;
          else
            newSealOwner = prevSealOwner;
          break;
        case 2:
          if (dawnProportion >= Math.round(0.35D * totalDawnMembers))
            newSealOwner = 2;
          else if (duskProportion >= Math.round(0.35D * totalDuskMembers))
            newSealOwner = 1;
          else
            newSealOwner = prevSealOwner;
          break;
        case 1:
          if (duskProportion >= Math.round(0.35D * totalDuskMembers))
            newSealOwner = 1;
          else if (dawnProportion >= Math.round(0.35D * totalDawnMembers))
            newSealOwner = 2;
          else {
            newSealOwner = prevSealOwner;
          }
        }
        break;
      case 2:
        switch (getCabalHighestScore())
        {
        case 0:
          if (dawnProportion >= Math.round(0.1D * totalDawnMembers))
            newSealOwner = prevSealOwner;
          else if (duskProportion >= Math.round(0.35D * totalDuskMembers))
            newSealOwner = 1;
          else
            newSealOwner = 0;
          break;
        case 2:
          if (dawnProportion >= Math.round(0.1D * totalDawnMembers))
            newSealOwner = prevSealOwner;
          else if (duskProportion >= Math.round(0.35D * totalDuskMembers))
            newSealOwner = 1;
          else
            newSealOwner = 0;
          break;
        case 1:
          if (duskProportion >= Math.round(0.1D * totalDuskMembers))
            newSealOwner = 1;
          else if (dawnProportion >= Math.round(0.35D * totalDawnMembers))
            newSealOwner = prevSealOwner;
          else {
            newSealOwner = 0;
          }
        }
        break;
      case 1:
        switch (getCabalHighestScore())
        {
        case 0:
          if (duskProportion >= Math.round(0.1D * totalDuskMembers))
            newSealOwner = prevSealOwner;
          else if (dawnProportion >= Math.round(0.35D * totalDawnMembers))
            newSealOwner = 2;
          else
            newSealOwner = 0;
          break;
        case 2:
          if (dawnProportion >= Math.round(0.35D * totalDawnMembers))
            newSealOwner = 2;
          else if (duskProportion >= Math.round(0.1D * totalDuskMembers))
            newSealOwner = prevSealOwner;
          else
            newSealOwner = 0;
          break;
        case 1:
          if (duskProportion >= Math.round(0.1D * totalDuskMembers))
            newSealOwner = prevSealOwner;
          else if (dawnProportion >= Math.round(0.35D * totalDawnMembers))
            newSealOwner = 2;
          else {
            newSealOwner = 0;
          }
        }

      }

      _signsSealOwners.put(currSeal, Integer.valueOf(newSealOwner));

      switch (currSeal.intValue())
      {
      case 1:
        if (newSealOwner == 2) {
          sendMessageToAll(1212); } else {
          if (newSealOwner != 1) break;
          sendMessageToAll(1215); } break;
      case 2:
        if (newSealOwner == 2) {
          sendMessageToAll(1213); } else {
          if (newSealOwner != 1) break;
          sendMessageToAll(1216); } break;
      case 3:
        if (newSealOwner == 2) {
          sendMessageToAll(1214); } else {
          if (newSealOwner != 1) break;
          sendMessageToAll(1217);
        }
      }
    }
  }

  public int getPriestCabal(int id)
  {
    switch (id)
    {
    case 31078:
    case 31079:
    case 31080:
    case 31081:
    case 31082:
    case 31083:
    case 31084:
    case 31168:
    case 31692:
    case 31694:
    case 31997:
      return 2;
    case 31085:
    case 31086:
    case 31087:
    case 31088:
    case 31089:
    case 31090:
    case 31091:
    case 31169:
    case 31693:
    case 31695:
    case 31998:
      return 1;
    }
    return 0;
  }

  public void changePeriod()
  {
    _periodChange = ThreadPoolManager.getInstance().schedule(new SevenSignsPeriodChange(), 10L);
  }

  public void changePeriod(int period)
  {
    changePeriod(period, 1);
  }

  public void changePeriod(int period, int seconds)
  {
    _activePeriod = (period - 1);
    if (_activePeriod < 0)
      _activePeriod += 4;
    _periodChange = ThreadPoolManager.getInstance().schedule(new SevenSignsPeriodChange(), seconds * 1000L);
  }

  public void setTimeToNextPeriodChange(int time)
  {
    _calendar.setTimeInMillis(System.currentTimeMillis() + time * 1000L * 60L);
    if (_periodChange != null)
      _periodChange.cancel(false);
    _periodChange = ThreadPoolManager.getInstance().schedule(new SevenSignsPeriodChange(), getMilliToPeriodChange());
  }

  public SSListenerList getListenerEngine()
  {
    return _listenerList;
  }

  public <T extends GameListener> boolean addListener(T listener)
  {
    return _listenerList.add(listener);
  }

  public <T extends GameListener> boolean removeListener(T listener)
  {
    return _listenerList.remove(listener);
  }

  public class SevenSignsPeriodChange extends RunnableImpl
  {
    public SevenSignsPeriodChange()
    {
    }

    public void runImpl()
      throws Exception
    {
      _log.info("SevenSignsPeriodChange: old=" + _activePeriod);
      int periodEnded = _activePeriod;
      _activePeriod += 1;
      switch (periodEnded)
      {
      case 0:
        sendMessageToAll(1210);

        RaidBossSpawnManager.getInstance().distributeRewards();
        break;
      case 1:
        sendMessageToAll(1211);
        int compWinner = getCabalHighestScore();
        calcNewSealOwners();
        if (compWinner == 1)
          sendMessageToAll(1240);
        else
          sendMessageToAll(1241);
        _previousWinner = compWinner;
        break;
      case 2:
        SevenSignsFestival.getInstance().distribAccumulatedBonus();

        SevenSignsFestival.getInstance().rewardHighestRanked();

        initializeSeals();

        RaidBossSpawnManager.getInstance().distributeRewards();

        sendMessageToAll(1218);
        _log.info("SevenSigns: The " + SevenSigns.getCabalName(_previousWinner) + " have won the competition with " + getCurrentScore(_previousWinner) + " points!");
        break;
      case 3:
        _activePeriod = 0;

        sendMessageToAll(1219);

        resetPlayerData();
        resetSeals();
        _dawnStoneScore = 0L;
        _duskStoneScore = 0L;
        _dawnFestivalScore = 0L;
        _duskFestivalScore = 0L;
        _currentCycle += 1;

        SevenSignsFestival.getInstance().resetFestivalData(false);
      }

      saveSevenSignsData(0, true);
      _log.info("SevenSignsPeriodChange: new=" + _activePeriod);
      try
      {
        _log.info("SevenSigns: Change Catacomb spawn...");
        getListenerEngine().onPeriodChange();
        SSQInfo ss = new SSQInfo();
        for (Player player : GameObjectsStorage.getAllPlayersForIterate())
          player.sendPacket(ss);
        _log.info("SevenSigns: Spawning NPCs...");
        spawnSevenSignsNPC();
        _log.info("SevenSigns: The " + getCurrentPeriodName() + " period has begun!");
        _log.info("SevenSigns: Calculating next period change time...");
        setCalendarForNextPeriodChange();
        _log.info("SevenSignsPeriodChange: time to next change=" + Util.formatTime((int)(getMilliToPeriodChange() / 1000L)));
        SevenSignsPeriodChange sspc = new SevenSignsPeriodChange(SevenSigns.this);
        SevenSigns.access$102(SevenSigns.this, ThreadPoolManager.getInstance().schedule(sspc, getMilliToPeriodChange()));
      }
      catch (Exception e)
      {
        _log.error("", e);
      }
    }
  }

  public class SevenSignsAnnounce extends RunnableImpl
  {
    public SevenSignsAnnounce()
    {
    }

    public void runImpl()
      throws Exception
    {
      for (Player player : GameObjectsStorage.getAllPlayersForIterate())
        sendCurrentPeriodMsg(player);
      ThreadPoolManager.getInstance().schedule(new SevenSignsAnnounce(SevenSigns.this), Config.SS_ANNOUNCE_PERIOD * 1000L * 60L);
    }
  }

  protected class SSListenerList extends ListenerList<GameServer>
  {
    protected SSListenerList()
    {
    }

    public void onPeriodChange()
    {
      int mode = 0;
      if (SevenSigns.getInstance().getCurrentPeriod() == 3) {
        mode = SevenSigns.getInstance().getCabalHighestScore();
      }
      for (Listener listener : getListeners())
        if ((listener instanceof OnSSPeriodListener))
          ((OnSSPeriodListener)listener).onPeriodChange(mode);
    }
  }

  private class OnStartListenerImpl
    implements OnStartListener
  {
    private OnStartListenerImpl()
    {
    }

    public void onStart()
    {
      getListenerEngine().onPeriodChange();
    }
  }
}