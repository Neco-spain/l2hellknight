package l2m.gameserver.model.entity.olympiad;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import l2p.commons.dbutils.DbUtils;
import l2m.gameserver.Announcements;
import l2m.gameserver.Config;
import l2m.gameserver.data.dao.OlympiadNobleDAO;
import l2m.gameserver.database.DatabaseFactory;
import l2m.gameserver.instancemanager.ServerVariables;
import l2m.gameserver.model.base.ClassId;
import l2m.gameserver.network.serverpackets.SystemMessage;
import l2m.gameserver.templates.StatsSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OlympiadDatabase
{
  private static final Logger _log = LoggerFactory.getLogger(OlympiadDatabase.class);

  public static synchronized void loadNoblesRank()
  {
    Olympiad._noblesRank = new ConcurrentHashMap();
    Map tmpPlace = new HashMap();

    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT `char_id` FROM `olympiad_nobles` ORDER BY olympiad_points_past_static DESC");
      rset = statement.executeQuery();
      int place = 1;
      while (rset.next()) {
        tmpPlace.put(Integer.valueOf(rset.getInt("char_id")), Integer.valueOf(place++));
      }
    }
    catch (Exception e)
    {
      _log.error("Olympiad System: Error!", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement, rset);
    }

    int rank1 = (int)Math.round(tmpPlace.size() * 0.01D);
    int rank2 = (int)Math.round(tmpPlace.size() * 0.1D);
    int rank3 = (int)Math.round(tmpPlace.size() * 0.25D);
    int rank4 = (int)Math.round(tmpPlace.size() * 0.5D);

    if (rank1 == 0)
    {
      rank1 = 1;
      rank2++;
      rank3++;
      rank4++;
    }

    for (Iterator i$ = tmpPlace.keySet().iterator(); i$.hasNext(); ) { int charId = ((Integer)i$.next()).intValue();
      if (((Integer)tmpPlace.get(Integer.valueOf(charId))).intValue() <= rank1)
        Olympiad._noblesRank.put(Integer.valueOf(charId), Integer.valueOf(1));
      else if (((Integer)tmpPlace.get(Integer.valueOf(charId))).intValue() <= rank2)
        Olympiad._noblesRank.put(Integer.valueOf(charId), Integer.valueOf(2));
      else if (((Integer)tmpPlace.get(Integer.valueOf(charId))).intValue() <= rank3)
        Olympiad._noblesRank.put(Integer.valueOf(charId), Integer.valueOf(3));
      else if (((Integer)tmpPlace.get(Integer.valueOf(charId))).intValue() <= rank4)
        Olympiad._noblesRank.put(Integer.valueOf(charId), Integer.valueOf(4));
      else
        Olympiad._noblesRank.put(Integer.valueOf(charId), Integer.valueOf(5));
    }
  }

  public static synchronized void cleanupNobles()
  {
    _log.info("Olympiad: Calculating last period...");
    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("UPDATE `olympiad_nobles` SET `olympiad_points_past` = `olympiad_points`, `olympiad_points_past_static` = `olympiad_points` WHERE `competitions_done` >= ?");
      statement.setInt(1, Config.OLYMPIAD_BATTLES_FOR_REWARD);
      statement.execute();
      DbUtils.close(statement);

      statement = con.prepareStatement("UPDATE `olympiad_nobles` SET `olympiad_points` = ?, `competitions_done` = 0, `competitions_win` = 0, `competitions_loose` = 0, game_classes_count=0, game_noclasses_count=0, game_team_count=0");
      statement.setInt(1, Config.OLYMPIAD_POINTS_DEFAULT);
      statement.execute();
    }
    catch (Exception e)
    {
      _log.error("Olympiad System: Couldn't calculate last period!", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }

    for (Integer nobleId : Olympiad._nobles.keySet())
    {
      StatsSet nobleInfo = (StatsSet)Olympiad._nobles.get(nobleId);
      int points = nobleInfo.getInteger("olympiad_points");
      int compDone = nobleInfo.getInteger("competitions_done");
      nobleInfo.set("olympiad_points", Config.OLYMPIAD_POINTS_DEFAULT);
      if (compDone >= Config.OLYMPIAD_BATTLES_FOR_REWARD)
      {
        nobleInfo.set("olympiad_points_past", points);
        nobleInfo.set("olympiad_points_past_static", points);
      }
      else
      {
        nobleInfo.set("olympiad_points_past", 0);
        nobleInfo.set("olympiad_points_past_static", 0);
      }
      nobleInfo.set("competitions_done", 0);
      nobleInfo.set("competitions_win", 0);
      nobleInfo.set("competitions_loose", 0);
      nobleInfo.set("game_classes_count", 0);
      nobleInfo.set("game_noclasses_count", 0);
      nobleInfo.set("game_team_count", 0);
    }
  }

  public static List<String> getClassLeaderBoard(int classId)
  {
    List names = new ArrayList();

    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement(classId == 132 ? "SELECT characters.char_name AS char_name FROM `olympiad_nobles` LEFT JOIN characters ON characters.obj_Id=olympiad_nobles.char_id WHERE `class_id` IN (?, 133) AND `olympiad_points_past_static` != 0 ORDER BY `olympiad_points_past_static` DESC LIMIT 10" : "SELECT characters.char_name AS char_name FROM `olympiad_nobles` LEFT JOIN characters ON char_id=characters.obj_Id WHERE `class_id` = ? AND `olympiad_points_past_static` != 0 ORDER BY `olympiad_points_past_static` DESC LIMIT 10");
      statement.setInt(1, classId);
      rset = statement.executeQuery();
      while (rset.next())
        names.add(rset.getString("char_name"));
    }
    catch (Exception e)
    {
      _log.error("Olympiad System: Couldnt get heros from db!", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement, rset);
    }

    return names;
  }

  public static synchronized void sortHerosToBe()
  {
    if (Olympiad._period != 1) {
      return;
    }
    Olympiad._heroesToBe = new ArrayList();

    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();

      for (ClassId id : ClassId.VALUES)
      {
        if (id.getId() == 133)
          continue;
        if (id.level() != 3)
          continue;
        statement = con.prepareStatement(id.getId() == 132 ? "SELECT `char_id`, characters.char_name AS char_name FROM `olympiad_nobles` LEFT JOIN characters ON char_id=characters.obj_Id WHERE `class_id` IN (?, 133) AND `competitions_done` >= ? AND `competitions_win` > 0 ORDER BY `olympiad_points` DESC, `competitions_win` DESC, `competitions_done` DESC" : "SELECT `char_id`, characters.char_name AS char_name FROM `olympiad_nobles` LEFT JOIN characters ON char_id=characters.obj_Id WHERE `class_id` = ? AND `competitions_done` >= ? AND `competitions_win` > 0 ORDER BY `olympiad_points` DESC, `competitions_win` DESC, `competitions_done` DESC");
        statement.setInt(1, id.getId());
        statement.setInt(2, Config.OLYMPIAD_BATTLES_FOR_REWARD);
        rset = statement.executeQuery();

        if (rset.next())
        {
          StatsSet hero = new StatsSet();
          hero.set("class_id", id.getId());
          hero.set("char_id", rset.getInt("char_id"));
          hero.set("char_name", rset.getString("char_name"));

          Olympiad._heroesToBe.add(hero);
        }
        DbUtils.close(statement, rset);
      }

    }
    catch (Exception e)
    {
      _log.error("Olympiad System: Couldnt heros from db!", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement, rset);
    }
  }

  public static synchronized void saveNobleData(int nobleId)
  {
    OlympiadNobleDAO.getInstance().replace(nobleId);
  }

  public static synchronized void saveNobleData()
  {
    if (Olympiad._nobles == null)
      return;
    for (Integer nobleId : Olympiad._nobles.keySet())
      saveNobleData(nobleId.intValue());
  }

  public static synchronized void setNewOlympiadEnd()
  {
    Announcements.getInstance().announceToAll(new SystemMessage(1639).addNumber(Olympiad._currentCycle));

    Calendar currentTime = Calendar.getInstance();
    currentTime.set(5, 1);
    currentTime.add(2, 1);
    currentTime.set(11, 0);
    currentTime.set(12, 0);
    Olympiad._olympiadEnd = currentTime.getTimeInMillis();

    Calendar nextChange = Calendar.getInstance();
    Olympiad._nextWeeklyChange = nextChange.getTimeInMillis() + Config.ALT_OLY_WPERIOD;

    Olympiad._isOlympiadEnd = false;
  }

  public static void save()
  {
    saveNobleData();
    ServerVariables.set("Olympiad_CurrentCycle", Olympiad._currentCycle);
    ServerVariables.set("Olympiad_Period", Olympiad._period);
    ServerVariables.set("Olympiad_End", Olympiad._olympiadEnd);
    ServerVariables.set("Olympiad_ValdationEnd", Olympiad._validationEnd);
    ServerVariables.set("Olympiad_NextWeeklyChange", Olympiad._nextWeeklyChange);
  }
}