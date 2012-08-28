package net.sf.l2j.gameserver;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.datatables.MapRegionTable.TeleportWhereType;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.model.AutoChatHandler;
import net.sf.l2j.gameserver.model.AutoChatHandler.AutoChatInstance;
import net.sf.l2j.gameserver.model.AutoSpawnHandler;
import net.sf.l2j.gameserver.model.AutoSpawnHandler.AutoSpawnInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SignsSky;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.StatsSet;

public class SevenSigns
{
  protected static final Logger _log = Logger.getLogger(SevenSigns.class.getName());
  private static SevenSigns _instance;
  public static final String SEVEN_SIGNS_DATA_FILE = "config/signs.properties";
  public static final String SEVEN_SIGNS_HTML_PATH = "data/html/seven_signs/";
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
  private final Calendar _calendar = Calendar.getInstance();
  protected int _activePeriod;
  protected int _currentCycle;
  protected double _dawnStoneScore;
  protected double _duskStoneScore;
  protected int _dawnFestivalScore;
  protected int _duskFestivalScore;
  protected int _compWinner;
  protected int _previousWinner;
  private Map<Integer, StatsSet> _signsPlayerData;
  private Map<Integer, Integer> _signsSealOwners;
  private Map<Integer, Integer> _signsDuskSealTotals;
  private Map<Integer, Integer> _signsDawnSealTotals;
  private static AutoSpawnHandler.AutoSpawnInstance _merchantSpawn;
  private static AutoSpawnHandler.AutoSpawnInstance _blacksmithSpawn;
  private static AutoSpawnHandler.AutoSpawnInstance _spiritInSpawn;
  private static AutoSpawnHandler.AutoSpawnInstance _spiritOutSpawn;
  private static AutoSpawnHandler.AutoSpawnInstance _lilithSpawn;
  private static AutoSpawnHandler.AutoSpawnInstance _anakimSpawn;
  private static AutoSpawnHandler.AutoSpawnInstance _crestofdawnspawn;
  private static AutoSpawnHandler.AutoSpawnInstance _crestofduskspawn;
  private static Map<Integer, AutoSpawnHandler.AutoSpawnInstance> _oratorSpawns;
  private static Map<Integer, AutoSpawnHandler.AutoSpawnInstance> _preacherSpawns;
  private static Map<Integer, AutoSpawnHandler.AutoSpawnInstance> _marketeerSpawns;

  public SevenSigns()
  {
    _signsPlayerData = new FastMap();
    _signsSealOwners = new FastMap();
    _signsDuskSealTotals = new FastMap();
    _signsDawnSealTotals = new FastMap();
    try
    {
      restoreSevenSignsData();
    }
    catch (Exception e) {
      _log.severe("SevenSigns: Failed to load configuration: " + e);
    }

    _log.info("SevenSigns: Currently in the " + getCurrentPeriodName() + " period!");
    initializeSeals();

    if (isSealValidationPeriod()) {
      if (getCabalHighestScore() == 0)
        _log.info("SevenSigns: The competition ended with a tie last week.");
      else
        _log.info("SevenSigns: The " + getCabalName(getCabalHighestScore()) + " were victorious last week.");
    }
    else if (getCabalHighestScore() == 0)
      _log.info("SevenSigns: The competition, if the current trend continues, will end in a tie this week.");
    else {
      _log.info("SevenSigns: The " + getCabalName(getCabalHighestScore()) + " are in the lead this week.");
    }
    synchronized (this)
    {
      setCalendarForNextPeriodChange();
      long milliToChange = getMilliToPeriodChange();

      SevenSignsPeriodChange sspc = new SevenSignsPeriodChange();
      ThreadPoolManager.getInstance().scheduleGeneral(sspc, milliToChange);

      double numSecs = milliToChange / 1000L % 60L;
      double countDown = (milliToChange / 1000L - numSecs) / 60.0D;
      int numMins = (int)Math.floor(countDown % 60.0D);
      countDown = (countDown - numMins) / 60.0D;
      int numHours = (int)Math.floor(countDown % 24.0D);
      int numDays = (int)Math.floor((countDown - numHours) / 24.0D);

      _log.info("SevenSigns: Next period begins in " + numDays + " days, " + numHours + " hours and " + numMins + " mins.");
    }
  }

  public void spawnSevenSignsNPC()
  {
    _merchantSpawn = AutoSpawnHandler.getInstance().getAutoSpawnInstance(31113, false);
    _blacksmithSpawn = AutoSpawnHandler.getInstance().getAutoSpawnInstance(31126, false);
    _marketeerSpawns = AutoSpawnHandler.getInstance().getAutoSpawnInstances(31092);
    _spiritInSpawn = AutoSpawnHandler.getInstance().getAutoSpawnInstance(31111, false);
    _spiritOutSpawn = AutoSpawnHandler.getInstance().getAutoSpawnInstance(31112, false);
    _lilithSpawn = AutoSpawnHandler.getInstance().getAutoSpawnInstance(25283, false);
    _anakimSpawn = AutoSpawnHandler.getInstance().getAutoSpawnInstance(25286, false);
    _crestofdawnspawn = AutoSpawnHandler.getInstance().getAutoSpawnInstance(31170, false);
    _crestofduskspawn = AutoSpawnHandler.getInstance().getAutoSpawnInstance(31171, false);
    _oratorSpawns = AutoSpawnHandler.getInstance().getAutoSpawnInstances(31094);
    _preacherSpawns = AutoSpawnHandler.getInstance().getAutoSpawnInstances(31093);

    if ((isSealValidationPeriod()) || (isCompResultsPeriod()))
    {
      for (AutoSpawnHandler.AutoSpawnInstance spawnInst : _marketeerSpawns.values()) {
        AutoSpawnHandler.getInstance().setSpawnActive(spawnInst, true);
      }
      if ((getSealOwner(2) == getCabalHighestScore()) && (getSealOwner(2) != 0))
      {
        if (!Config.ANNOUNCE_MAMMON_SPAWN) {
          _blacksmithSpawn.setBroadcast(false);
        }
        if (!AutoSpawnHandler.getInstance().getAutoSpawnInstance(_blacksmithSpawn.getObjectId(), true).isSpawnActive()) {
          AutoSpawnHandler.getInstance().setSpawnActive(_blacksmithSpawn, true);
        }
        for (AutoSpawnHandler.AutoSpawnInstance spawnInst : _oratorSpawns.values()) {
          if (!AutoSpawnHandler.getInstance().getAutoSpawnInstance(spawnInst.getObjectId(), true).isSpawnActive())
            AutoSpawnHandler.getInstance().setSpawnActive(spawnInst, true);
        }
        for (AutoSpawnHandler.AutoSpawnInstance spawnInst : _preacherSpawns.values()) {
          if (!AutoSpawnHandler.getInstance().getAutoSpawnInstance(spawnInst.getObjectId(), true).isSpawnActive())
            AutoSpawnHandler.getInstance().setSpawnActive(spawnInst, true);
        }
        if ((!AutoChatHandler.getInstance().getAutoChatInstance(31093, false).isActive()) && (!AutoChatHandler.getInstance().getAutoChatInstance(31094, false).isActive()))
          AutoChatHandler.getInstance().setAutoChatActive(true);
      }
      else
      {
        AutoSpawnHandler.getInstance().setSpawnActive(_blacksmithSpawn, false);

        for (AutoSpawnHandler.AutoSpawnInstance spawnInst : _oratorSpawns.values()) {
          AutoSpawnHandler.getInstance().setSpawnActive(spawnInst, false);
        }
        for (AutoSpawnHandler.AutoSpawnInstance spawnInst : _preacherSpawns.values()) {
          AutoSpawnHandler.getInstance().setSpawnActive(spawnInst, false);
        }
        AutoChatHandler.getInstance().setAutoChatActive(false);
      }

      if ((getSealOwner(1) == getCabalHighestScore()) && (getSealOwner(1) != 0))
      {
        if (!Config.ANNOUNCE_MAMMON_SPAWN) {
          _merchantSpawn.setBroadcast(false);
        }
        if (!AutoSpawnHandler.getInstance().getAutoSpawnInstance(_merchantSpawn.getObjectId(), true).isSpawnActive()) {
          AutoSpawnHandler.getInstance().setSpawnActive(_merchantSpawn, true);
        }
        if (!AutoSpawnHandler.getInstance().getAutoSpawnInstance(_spiritInSpawn.getObjectId(), true).isSpawnActive()) {
          AutoSpawnHandler.getInstance().setSpawnActive(_spiritInSpawn, true);
        }
        if (!AutoSpawnHandler.getInstance().getAutoSpawnInstance(_spiritOutSpawn.getObjectId(), true).isSpawnActive()) {
          AutoSpawnHandler.getInstance().setSpawnActive(_spiritOutSpawn, true);
        }
        switch (getCabalHighestScore())
        {
        case 2:
          if (!AutoSpawnHandler.getInstance().getAutoSpawnInstance(_lilithSpawn.getObjectId(), true).isSpawnActive()) {
            AutoSpawnHandler.getInstance().setSpawnActive(_lilithSpawn, true);
          }
          AutoSpawnHandler.getInstance().setSpawnActive(_anakimSpawn, false);
          if (!AutoSpawnHandler.getInstance().getAutoSpawnInstance(_crestofdawnspawn.getObjectId(), true).isSpawnActive()) {
            AutoSpawnHandler.getInstance().setSpawnActive(_crestofdawnspawn, true);
          }
          AutoSpawnHandler.getInstance().setSpawnActive(_crestofduskspawn, false);
          break;
        case 1:
          if (!AutoSpawnHandler.getInstance().getAutoSpawnInstance(_anakimSpawn.getObjectId(), true).isSpawnActive()) {
            AutoSpawnHandler.getInstance().setSpawnActive(_anakimSpawn, true);
          }
          AutoSpawnHandler.getInstance().setSpawnActive(_lilithSpawn, false);
          if (!AutoSpawnHandler.getInstance().getAutoSpawnInstance(_crestofduskspawn.getObjectId(), true).isSpawnActive()) {
            AutoSpawnHandler.getInstance().setSpawnActive(_crestofduskspawn, true);
          }
          AutoSpawnHandler.getInstance().setSpawnActive(_crestofdawnspawn, false);
        }

      }
      else
      {
        AutoSpawnHandler.getInstance().setSpawnActive(_merchantSpawn, false);
        AutoSpawnHandler.getInstance().setSpawnActive(_lilithSpawn, false);
        AutoSpawnHandler.getInstance().setSpawnActive(_anakimSpawn, false);
        AutoSpawnHandler.getInstance().setSpawnActive(_crestofdawnspawn, false);
        AutoSpawnHandler.getInstance().setSpawnActive(_crestofduskspawn, false);
        AutoSpawnHandler.getInstance().setSpawnActive(_spiritInSpawn, false);
        AutoSpawnHandler.getInstance().setSpawnActive(_spiritOutSpawn, false);
      }
    }
    else
    {
      AutoSpawnHandler.getInstance().setSpawnActive(_merchantSpawn, false);
      AutoSpawnHandler.getInstance().setSpawnActive(_blacksmithSpawn, false);
      AutoSpawnHandler.getInstance().setSpawnActive(_lilithSpawn, false);
      AutoSpawnHandler.getInstance().setSpawnActive(_anakimSpawn, false);
      AutoSpawnHandler.getInstance().setSpawnActive(_crestofdawnspawn, false);
      AutoSpawnHandler.getInstance().setSpawnActive(_crestofduskspawn, false);
      AutoSpawnHandler.getInstance().setSpawnActive(_spiritInSpawn, false);
      AutoSpawnHandler.getInstance().setSpawnActive(_spiritOutSpawn, false);

      for (AutoSpawnHandler.AutoSpawnInstance spawnInst : _oratorSpawns.values()) {
        AutoSpawnHandler.getInstance().setSpawnActive(spawnInst, false);
      }
      for (AutoSpawnHandler.AutoSpawnInstance spawnInst : _preacherSpawns.values()) {
        AutoSpawnHandler.getInstance().setSpawnActive(spawnInst, false);
      }
      for (AutoSpawnHandler.AutoSpawnInstance spawnInst : _marketeerSpawns.values()) {
        AutoSpawnHandler.getInstance().setSpawnActive(spawnInst, false);
      }
      AutoChatHandler.getInstance().setAutoChatActive(false);
    }
  }

  public static SevenSigns getInstance()
  {
    if (_instance == null) {
      _instance = new SevenSigns();
    }
    return _instance;
  }

  public static int calcContributionScore(int blueCount, int greenCount, int redCount)
  {
    int contrib = blueCount * 3;
    contrib += greenCount * 5;
    contrib += redCount * 10;

    return contrib;
  }

  public static int calcAncientAdenaReward(int blueCount, int greenCount, int redCount)
  {
    int reward = blueCount * 3;
    reward += greenCount * 5;
    reward += redCount * 10;

    return reward;
  }

  public static final String getCabalShortName(int cabal)
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

  public static final String getCabalName(int cabal)
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

  public static final String getSealName(int seal, boolean shortName)
  {
    String sealName = !shortName ? "Seal of " : "";

    switch (seal)
    {
    case 1:
      sealName = sealName + "Avarice";
      break;
    case 2:
      sealName = sealName + "Gnosis";
      break;
    case 3:
      sealName = sealName + "Strife";
    }

    return sealName;
  }

  public final int getCurrentCycle()
  {
    return _currentCycle;
  }

  public final int getCurrentPeriod()
  {
    return _activePeriod;
  }

  private final int getDaysToPeriodChange()
  {
    int numDays = _calendar.get(7) - 2;

    if (numDays < 0) {
      return 0 - numDays;
    }
    return 7 - numDays;
  }

  public final long getMilliToPeriodChange()
  {
    long currTimeMillis = System.currentTimeMillis();
    long changeTimeMillis = _calendar.getTimeInMillis();

    return changeTimeMillis - currTimeMillis;
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
      if (daysToChange > 0) {
        _calendar.add(5, daysToChange);
      }
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

  public final int getCurrentScore(int cabal)
  {
    double totalStoneScore = _dawnStoneScore + _duskStoneScore;

    switch (cabal)
    {
    case 0:
      return 0;
    case 2:
      return Math.round((float)(_dawnStoneScore / ((float)totalStoneScore == 0.0F ? 1.0D : totalStoneScore)) * 500.0F) + _dawnFestivalScore;
    case 1:
      return Math.round((float)(_duskStoneScore / ((float)totalStoneScore == 0.0F ? 1.0D : totalStoneScore)) * 500.0F) + _duskFestivalScore;
    }

    return 0;
  }

  public final double getCurrentStoneScore(int cabal)
  {
    switch (cabal)
    {
    case 0:
      return 0.0D;
    case 2:
      return _dawnStoneScore;
    case 1:
      return _duskStoneScore;
    }

    return 0.0D;
  }

  public final int getCurrentFestivalScore(int cabal)
  {
    switch (cabal)
    {
    case 0:
      return 0;
    case 2:
      return _dawnFestivalScore;
    case 1:
      return _duskFestivalScore;
    }

    return 0;
  }

  public final int getCabalHighestScore()
  {
    if (getCurrentScore(1) == getCurrentScore(2))
      return 0;
    if (getCurrentScore(1) > getCurrentScore(2)) {
      return 1;
    }
    return 2;
  }

  public final int getSealOwner(int seal)
  {
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
    String cabalName = getCabalShortName(cabal);

    for (StatsSet sevenDat : _signsPlayerData.values()) {
      if (sevenDat.getString("cabal").equals(cabalName))
        cabalMembers++;
    }
    return cabalMembers;
  }

  public final StatsSet getPlayerData(L2PcInstance player)
  {
    if (!hasRegisteredBefore(player)) {
      return null;
    }
    return (StatsSet)_signsPlayerData.get(Integer.valueOf(player.getObjectId()));
  }

  public int getPlayerStoneContrib(L2PcInstance player)
  {
    if (!hasRegisteredBefore(player)) {
      return 0;
    }
    int stoneCount = 0;

    StatsSet currPlayer = getPlayerData(player);

    stoneCount += currPlayer.getInteger("red_stones");
    stoneCount += currPlayer.getInteger("green_stones");
    stoneCount += currPlayer.getInteger("blue_stones");

    return stoneCount;
  }

  public int getPlayerContribScore(L2PcInstance player)
  {
    if (!hasRegisteredBefore(player)) {
      return 0;
    }
    StatsSet currPlayer = getPlayerData(player);

    return currPlayer.getInteger("contribution_score");
  }

  public int getPlayerAdenaCollect(L2PcInstance player)
  {
    if (!hasRegisteredBefore(player)) {
      return 0;
    }
    return ((StatsSet)_signsPlayerData.get(Integer.valueOf(player.getObjectId()))).getInteger("ancient_adena_amount");
  }

  public int getPlayerSeal(L2PcInstance player)
  {
    if (!hasRegisteredBefore(player)) {
      return 0;
    }
    return getPlayerData(player).getInteger("seal");
  }

  public int getPlayerCabal(L2PcInstance player)
  {
    if (!hasRegisteredBefore(player)) {
      return 0;
    }
    String playerCabal = getPlayerData(player).getString("cabal");

    if (playerCabal.equalsIgnoreCase("dawn"))
      return 2;
    if (playerCabal.equalsIgnoreCase("dusk")) {
      return 1;
    }
    return 0;
  }

  protected void restoreSevenSignsData()
  {
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT char_obj_id, cabal, seal, red_stones, green_stones, blue_stones, ancient_adena_amount, contribution_score FROM seven_signs");

      rset = statement.executeQuery();

      while (rset.next())
      {
        int charObjId = rset.getInt("char_obj_id");

        StatsSet sevenDat = new StatsSet();
        sevenDat.set("char_obj_id", charObjId);
        sevenDat.set("cabal", rset.getString("cabal"));
        sevenDat.set("seal", rset.getInt("seal"));
        sevenDat.set("red_stones", rset.getInt("red_stones"));
        sevenDat.set("green_stones", rset.getInt("green_stones"));
        sevenDat.set("blue_stones", rset.getInt("blue_stones"));
        sevenDat.set("ancient_adena_amount", rset.getDouble("ancient_adena_amount"));
        sevenDat.set("contribution_score", rset.getDouble("contribution_score"));

        if (Config.DEBUG) {
          _log.info("SevenSigns: Loaded data from DB for char ID " + charObjId + " (" + sevenDat.getString("cabal") + ")");
        }
        _signsPlayerData.put(Integer.valueOf(charObjId), sevenDat);
      }

      rset.close();
      statement.close();

      statement = con.prepareStatement("SELECT * FROM seven_signs_status WHERE id=0");
      rset = statement.executeQuery();

      while (rset.next())
      {
        _currentCycle = rset.getInt("current_cycle");
        _activePeriod = rset.getInt("active_period");
        _previousWinner = rset.getInt("previous_winner");

        _dawnStoneScore = rset.getDouble("dawn_stone_score");
        _dawnFestivalScore = rset.getInt("dawn_festival_score");
        _duskStoneScore = rset.getDouble("dusk_stone_score");
        _duskFestivalScore = rset.getInt("dusk_festival_score");

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

      rset.close();
      statement.close();

      statement = con.prepareStatement("UPDATE seven_signs_status SET date=? WHERE id=0");
      statement.setInt(1, Calendar.getInstance().get(7));
      statement.execute();

      statement.close();
      con.close();
    }
    catch (SQLException e)
    {
      _log.severe("SevenSigns: Unable to load Seven Signs data from database: " + e);
    }
    finally
    {
      try
      {
        rset.close();
        statement.close();
        con.close();
      }
      catch (Exception e)
      {
      }
    }
  }

  public void saveSevenSignsData(L2PcInstance player, boolean updateSettings)
  {
    Connection con = null;
    PreparedStatement statement = null;

    if (Config.DEBUG) {
      System.out.println("SevenSigns: Saving data to disk.");
    }
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      for (StatsSet sevenDat : _signsPlayerData.values())
      {
        if ((player != null) && 
          (sevenDat.getInteger("char_obj_id") != player.getObjectId())) {
          continue;
        }
        statement = con.prepareStatement("UPDATE seven_signs SET cabal=?, seal=?, red_stones=?, green_stones=?, blue_stones=?, ancient_adena_amount=?, contribution_score=? WHERE char_obj_id=?");

        statement.setString(1, sevenDat.getString("cabal"));
        statement.setInt(2, sevenDat.getInteger("seal"));
        statement.setInt(3, sevenDat.getInteger("red_stones"));
        statement.setInt(4, sevenDat.getInteger("green_stones"));
        statement.setInt(5, sevenDat.getInteger("blue_stones"));
        statement.setDouble(6, sevenDat.getDouble("ancient_adena_amount"));
        statement.setDouble(7, sevenDat.getDouble("contribution_score"));
        statement.setInt(8, sevenDat.getInteger("char_obj_id"));
        statement.execute();

        statement.close();

        if (Config.DEBUG) {
          _log.info("SevenSigns: Updated data in database for char ID " + sevenDat.getInteger("char_obj_id") + " (" + sevenDat.getString("cabal") + ")");
        }
      }
      if (updateSettings)
      {
        String sqlQuery = "UPDATE seven_signs_status SET current_cycle=?, active_period=?, previous_winner=?, dawn_stone_score=?, dawn_festival_score=?, dusk_stone_score=?, dusk_festival_score=?, avarice_owner=?, gnosis_owner=?, strife_owner=?, avarice_dawn_score=?, gnosis_dawn_score=?, strife_dawn_score=?, avarice_dusk_score=?, gnosis_dusk_score=?, strife_dusk_score=?, festival_cycle=?, ";

        for (int i = 0; i < 5; i++) {
          sqlQuery = sqlQuery + "accumulated_bonus" + String.valueOf(i) + "=?, ";
        }
        sqlQuery = sqlQuery + "date=? WHERE id=0";

        statement = con.prepareStatement(sqlQuery);
        statement.setInt(1, _currentCycle);
        statement.setInt(2, _activePeriod);
        statement.setInt(3, _previousWinner);
        statement.setDouble(4, _dawnStoneScore);
        statement.setInt(5, _dawnFestivalScore);
        statement.setDouble(6, _duskStoneScore);
        statement.setInt(7, _duskFestivalScore);
        statement.setInt(8, ((Integer)_signsSealOwners.get(Integer.valueOf(1))).intValue());
        statement.setInt(9, ((Integer)_signsSealOwners.get(Integer.valueOf(2))).intValue());
        statement.setInt(10, ((Integer)_signsSealOwners.get(Integer.valueOf(3))).intValue());
        statement.setInt(11, ((Integer)_signsDawnSealTotals.get(Integer.valueOf(1))).intValue());
        statement.setInt(12, ((Integer)_signsDawnSealTotals.get(Integer.valueOf(2))).intValue());
        statement.setInt(13, ((Integer)_signsDawnSealTotals.get(Integer.valueOf(3))).intValue());
        statement.setInt(14, ((Integer)_signsDuskSealTotals.get(Integer.valueOf(1))).intValue());
        statement.setInt(15, ((Integer)_signsDuskSealTotals.get(Integer.valueOf(2))).intValue());
        statement.setInt(16, ((Integer)_signsDuskSealTotals.get(Integer.valueOf(3))).intValue());
        statement.setInt(17, SevenSignsFestival.getInstance().getCurrentFestivalCycle());

        for (int i = 0; i < 5; i++) {
          statement.setInt(18 + i, SevenSignsFestival.getInstance().getAccumulatedBonus(i));
        }
        statement.setInt(23, Calendar.getInstance().get(7));
        statement.execute();

        statement.close();
        con.close();

        if (Config.DEBUG) {
          _log.info("SevenSigns: Updated data in database.");
        }
      }
    }
    catch (SQLException e)
    {
      _log.severe("SevenSigns: Unable to save data to database: " + e);
    }
    finally
    {
      try {
        statement.close();
        con.close();
      }
      catch (Exception e)
      {
      }
    }
  }

  protected void resetPlayerData()
  {
    if (Config.DEBUG) {
      _log.info("SevenSigns: Resetting player data for new event period.");
    }

    for (StatsSet sevenDat : _signsPlayerData.values())
    {
      int charObjId = sevenDat.getInteger("char_obj_id");

      sevenDat.set("cabal", "");
      sevenDat.set("seal", 0);
      sevenDat.set("contribution_score", 0);

      _signsPlayerData.put(Integer.valueOf(charObjId), sevenDat);
    }
  }

  private boolean hasRegisteredBefore(L2PcInstance player)
  {
    return _signsPlayerData.containsKey(Integer.valueOf(player.getObjectId()));
  }

  public int setPlayerInfo(L2PcInstance player, int chosenCabal, int chosenSeal)
  {
    int charObjId = player.getObjectId();
    Connection con = null;
    PreparedStatement statement = null;
    StatsSet currPlayerData = getPlayerData(player);

    if (currPlayerData != null)
    {
      currPlayerData.set("cabal", getCabalShortName(chosenCabal));
      currPlayerData.set("seal", chosenSeal);

      _signsPlayerData.put(Integer.valueOf(charObjId), currPlayerData);
    }
    else
    {
      currPlayerData = new StatsSet();
      currPlayerData.set("char_obj_id", charObjId);
      currPlayerData.set("cabal", getCabalShortName(chosenCabal));
      currPlayerData.set("seal", chosenSeal);
      currPlayerData.set("red_stones", 0);
      currPlayerData.set("green_stones", 0);
      currPlayerData.set("blue_stones", 0);
      currPlayerData.set("ancient_adena_amount", 0);
      currPlayerData.set("contribution_score", 0);

      _signsPlayerData.put(Integer.valueOf(charObjId), currPlayerData);
      try
      {
        con = L2DatabaseFactory.getInstance().getConnection();
        statement = con.prepareStatement("INSERT INTO seven_signs (char_obj_id, cabal, seal) VALUES (?,?,?)");

        statement.setInt(1, charObjId);
        statement.setString(2, getCabalShortName(chosenCabal));
        statement.setInt(3, chosenSeal);
        statement.execute();

        statement.close();
        con.close();

        if (Config.DEBUG)
          _log.info("SevenSigns: Inserted data in DB for char ID " + currPlayerData.getInteger("char_obj_id") + " (" + currPlayerData.getString("cabal") + ")");
      }
      catch (SQLException e)
      {
        _log.severe("SevenSigns: Failed to save data: " + e);
      }
      finally
      {
        try
        {
          statement.close();
          con.close();
        }
        catch (Exception e)
        {
        }
      }
    }
    if (currPlayerData.getString("cabal") == "dawn")
      _signsDawnSealTotals.put(Integer.valueOf(chosenSeal), Integer.valueOf(((Integer)_signsDawnSealTotals.get(Integer.valueOf(chosenSeal))).intValue() + 1));
    else {
      _signsDuskSealTotals.put(Integer.valueOf(chosenSeal), Integer.valueOf(((Integer)_signsDuskSealTotals.get(Integer.valueOf(chosenSeal))).intValue() + 1));
    }
    saveSevenSignsData(player, true);

    if (Config.DEBUG) {
      _log.info("SevenSigns: " + player.getName() + " has joined the " + getCabalName(chosenCabal) + " for the " + getSealName(chosenSeal, false) + "!");
    }
    return chosenCabal;
  }

  public int getAncientAdenaReward(L2PcInstance player, boolean removeReward)
  {
    StatsSet currPlayer = getPlayerData(player);
    int rewardAmount = currPlayer.getInteger("ancient_adena_amount");

    currPlayer.set("red_stones", 0);
    currPlayer.set("green_stones", 0);
    currPlayer.set("blue_stones", 0);
    currPlayer.set("ancient_adena_amount", 0);

    if (removeReward)
    {
      _signsPlayerData.put(Integer.valueOf(player.getObjectId()), currPlayer);
      saveSevenSignsData(player, true);
    }

    return rewardAmount;
  }

  public int addPlayerStoneContrib(L2PcInstance player, int blueCount, int greenCount, int redCount)
  {
    StatsSet currPlayer = getPlayerData(player);

    int contribScore = calcContributionScore(blueCount, greenCount, redCount);
    int totalAncientAdena = currPlayer.getInteger("ancient_adena_amount") + calcAncientAdenaReward(blueCount, greenCount, redCount);
    int totalContribScore = currPlayer.getInteger("contribution_score") + contribScore;

    if (totalContribScore > Config.ALT_MAXIMUM_PLAYER_CONTRIB) {
      return -1;
    }
    currPlayer.set("red_stones", currPlayer.getInteger("red_stones") + redCount);
    currPlayer.set("green_stones", currPlayer.getInteger("green_stones") + greenCount);
    currPlayer.set("blue_stones", currPlayer.getInteger("blue_stones") + blueCount);
    currPlayer.set("ancient_adena_amount", totalAncientAdena);
    currPlayer.set("contribution_score", totalContribScore);
    _signsPlayerData.put(Integer.valueOf(player.getObjectId()), currPlayer);

    switch (getPlayerCabal(player))
    {
    case 2:
      _dawnStoneScore += contribScore;
      break;
    case 1:
      _duskStoneScore += contribScore;
    }

    saveSevenSignsData(player, true);

    if (Config.DEBUG) {
      _log.info("SevenSigns: " + player.getName() + " contributed " + contribScore + " seal stone points to their cabal.");
    }
    return contribScore;
  }

  public void addFestivalScore(int cabal, int amount)
  {
    if (cabal == 1) {
      _duskFestivalScore += amount;

      if (_dawnFestivalScore >= amount)
        _dawnFestivalScore -= amount;
    }
    else {
      _dawnFestivalScore += amount;

      if (_duskFestivalScore >= amount)
        _duskFestivalScore -= amount;
    }
  }

  public void sendCurrentPeriodMsg(L2PcInstance player)
  {
    SystemMessage sm = null;

    switch (getCurrentPeriod())
    {
    case 0:
      sm = new SystemMessage(SystemMessageId.PREPARATIONS_PERIOD_BEGUN);
      break;
    case 1:
      sm = new SystemMessage(SystemMessageId.COMPETITION_PERIOD_BEGUN);
      break;
    case 2:
      sm = new SystemMessage(SystemMessageId.RESULTS_PERIOD_BEGUN);
      break;
    case 3:
      sm = new SystemMessage(SystemMessageId.VALIDATION_PERIOD_BEGUN);
    }

    player.sendPacket(sm);
  }

  public void sendMessageToAll(SystemMessageId sysMsgId)
  {
    SystemMessage sm = new SystemMessage(sysMsgId);

    for (L2PcInstance player : L2World.getInstance().getAllPlayers())
      player.sendPacket(sm);
  }

  protected void initializeSeals()
  {
    for (Integer currSeal : _signsSealOwners.keySet())
    {
      int sealOwner = ((Integer)_signsSealOwners.get(currSeal)).intValue();

      if (sealOwner != 0) {
        if (isSealValidationPeriod())
          _log.info("SevenSigns: The " + getCabalName(sealOwner) + " have won the " + getSealName(currSeal.intValue(), false) + ".");
        else
          _log.info("SevenSigns: The " + getSealName(currSeal.intValue(), false) + " is currently owned by " + getCabalName(sealOwner) + ".");
      }
      else _log.info("SevenSigns: The " + getSealName(currSeal.intValue(), false) + " remains unclaimed.");
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
    if (Config.DEBUG)
    {
      _log.info("SevenSigns: (Avarice) Dawn = " + _signsDawnSealTotals.get(Integer.valueOf(1)) + ", Dusk = " + _signsDuskSealTotals.get(Integer.valueOf(1)));
      _log.info("SevenSigns: (Gnosis) Dawn = " + _signsDawnSealTotals.get(Integer.valueOf(2)) + ", Dusk = " + _signsDuskSealTotals.get(Integer.valueOf(2)));
      _log.info("SevenSigns: (Strife) Dawn = " + _signsDawnSealTotals.get(Integer.valueOf(3)) + ", Dusk = " + _signsDuskSealTotals.get(Integer.valueOf(3)));
    }

    for (Integer currSeal : _signsDawnSealTotals.keySet())
    {
      int prevSealOwner = ((Integer)_signsSealOwners.get(currSeal)).intValue();
      int newSealOwner = 0;
      int dawnProportion = getSealProportion(currSeal.intValue(), 2);
      int totalDawnMembers = getTotalMembers(2) == 0 ? 1 : getTotalMembers(2);
      int dawnPercent = Math.round(dawnProportion / totalDawnMembers * 100.0F);
      int duskProportion = getSealProportion(currSeal.intValue(), 1);
      int totalDuskMembers = getTotalMembers(1) == 0 ? 1 : getTotalMembers(1);
      int duskPercent = Math.round(duskProportion / totalDuskMembers * 100.0F);

      switch (prevSealOwner)
      {
      case 0:
        switch (getCabalHighestScore())
        {
        case 0:
          newSealOwner = 0;
          break;
        case 2:
          if (dawnPercent >= 35)
            newSealOwner = 2;
          else
            newSealOwner = 0;
          break;
        case 1:
          if (duskPercent >= 35)
            newSealOwner = 1;
          else {
            newSealOwner = 0;
          }
        }
        break;
      case 2:
        switch (getCabalHighestScore())
        {
        case 0:
          if (dawnPercent >= 10)
            newSealOwner = 2;
          else
            newSealOwner = 0;
          break;
        case 2:
          if (dawnPercent >= 10)
            newSealOwner = 2;
          else
            newSealOwner = 0;
          break;
        case 1:
          if (duskPercent >= 35)
            newSealOwner = 1;
          else if (dawnPercent >= 10)
            newSealOwner = 2;
          else {
            newSealOwner = 0;
          }
        }
        break;
      case 1:
        switch (getCabalHighestScore())
        {
        case 0:
          if (duskPercent >= 10)
            newSealOwner = 1;
          else
            newSealOwner = 0;
          break;
        case 2:
          if (dawnPercent >= 35)
            newSealOwner = 2;
          else if (duskPercent >= 10)
            newSealOwner = 1;
          else
            newSealOwner = 0;
          break;
        case 1:
          if (duskPercent >= 10)
            newSealOwner = 1;
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
          sendMessageToAll(SystemMessageId.DAWN_OBTAINED_AVARICE); } else {
          if (newSealOwner != 1) break;
          sendMessageToAll(SystemMessageId.DUSK_OBTAINED_AVARICE); } break;
      case 2:
        if (newSealOwner == 2) {
          sendMessageToAll(SystemMessageId.DAWN_OBTAINED_GNOSIS); } else {
          if (newSealOwner != 1) break;
          sendMessageToAll(SystemMessageId.DUSK_OBTAINED_GNOSIS); } break;
      case 3:
        if (newSealOwner == 2)
          sendMessageToAll(SystemMessageId.DAWN_OBTAINED_STRIFE);
        else if (newSealOwner == 1) {
          sendMessageToAll(SystemMessageId.DUSK_OBTAINED_STRIFE);
        }
        CastleManager.getInstance().validateTaxes(newSealOwner);
      }
    }
  }

  protected void teleLosingCabalFromDungeons(String compWinner)
  {
    for (L2PcInstance onlinePlayer : L2World.getInstance().getAllPlayers())
    {
      StatsSet currPlayer = getPlayerData(onlinePlayer);

      if ((isSealValidationPeriod()) || (isCompResultsPeriod()))
      {
        if ((!onlinePlayer.isGM()) && (onlinePlayer.isIn7sDungeon()) && (!currPlayer.getString("cabal").equals(compWinner)))
        {
          onlinePlayer.teleToLocation(MapRegionTable.TeleportWhereType.Town);
          onlinePlayer.setIsIn7sDungeon(false);
          onlinePlayer.sendMessage("You have been teleported to the nearest town due to the beginning of the Seal Validation period.");
        }

      }
      else if ((!onlinePlayer.isGM()) && (onlinePlayer.isIn7sDungeon()) && (!currPlayer.getString("cabal").equals("")))
      {
        onlinePlayer.teleToLocation(MapRegionTable.TeleportWhereType.Town);
        onlinePlayer.setIsIn7sDungeon(false);
        onlinePlayer.sendMessage("You have been teleported to the nearest town because you have not signed for any cabal.");
      }
    }
  }

  protected class SevenSignsPeriodChange
    implements Runnable
  {
    protected SevenSignsPeriodChange()
    {
    }

    public void run()
    {
      int periodEnded = getCurrentPeriod();
      _activePeriod += 1;

      switch (periodEnded)
      {
      case 0:
        SevenSignsFestival.getInstance().startFestivalManager();

        sendMessageToAll(SystemMessageId.QUEST_EVENT_PERIOD_BEGUN);
        break;
      case 1:
        sendMessageToAll(SystemMessageId.QUEST_EVENT_PERIOD_ENDED);

        int compWinner = getCabalHighestScore();

        SevenSignsFestival.getInstance().getFestivalManagerSchedule().cancel(false);

        calcNewSealOwners();

        switch (compWinner)
        {
        case 2:
          sendMessageToAll(SystemMessageId.DAWN_WON);
          break;
        case 1:
          sendMessageToAll(SystemMessageId.DUSK_WON);
        }

        _previousWinner = compWinner;
        break;
      case 2:
        initializeSeals();

        sendMessageToAll(SystemMessageId.SEAL_VALIDATION_PERIOD_BEGUN);

        SevenSigns._log.info("SevenSigns: The " + SevenSigns.getCabalName(_previousWinner) + " have won the competition with " + getCurrentScore(_previousWinner) + " points!");
        break;
      case 3:
        SevenSignsFestival.getInstance().rewardHighestRanked();

        _activePeriod = 0;

        sendMessageToAll(SystemMessageId.SEAL_VALIDATION_PERIOD_ENDED);

        resetPlayerData();
        resetSeals();

        SevenSignsFestival.getInstance().resetFestivalData(false);

        _dawnStoneScore = 0.0D;
        _duskStoneScore = 0.0D;

        _dawnFestivalScore = 0;
        _duskFestivalScore = 0;

        _currentCycle += 1;
      }

      saveSevenSignsData(null, true);

      teleLosingCabalFromDungeons(SevenSigns.getCabalShortName(getCabalHighestScore()));

      SignsSky ss = new SignsSky();

      for (L2PcInstance player : L2World.getInstance().getAllPlayers()) {
        player.sendPacket(ss);
      }
      spawnSevenSignsNPC();

      SevenSigns._log.info("SevenSigns: The " + getCurrentPeriodName() + " period has begun!");

      setCalendarForNextPeriodChange();

      SevenSignsPeriodChange sspc = new SevenSignsPeriodChange(SevenSigns.this);
      ThreadPoolManager.getInstance().scheduleGeneral(sspc, getMilliToPeriodChange());
    }
  }
}