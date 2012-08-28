package net.sf.l2j.gameserver.model.entity.olympiad;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.instancemanager.ServerVariables;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.StatsSet;
import net.sf.l2j.mysql.Close;
import net.sf.l2j.mysql.Connect;

public class OlympiadDatabase
{
  public static synchronized void loadNobles()
  {
    Connect con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT * FROM `olympiad_nobles`");
      rset = statement.executeQuery();

      while (rset.next())
      {
        int classId = rset.getInt("class_id");

        StatsSet statDat = new StatsSet();
        int charId = rset.getInt("char_id");
        statDat.set("class_id", classId);
        statDat.set("char_name", rset.getString("char_name"));
        statDat.set("olympiad_points", rset.getInt("olympiad_points"));
        statDat.set("olympiad_points_past", rset.getInt("olympiad_points_past"));
        statDat.set("olympiad_points_past_static", rset.getInt("olympiad_points_past_static"));
        statDat.set("competitions_done", rset.getInt("competitions_done"));
        statDat.set("competitions_win", rset.getInt("competitions_win"));
        statDat.set("competitions_loose", rset.getInt("competitions_loose"));

        Olympiad._nobles.put(Integer.valueOf(charId), statDat);
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    finally
    {
      Close.CSR(con, statement, rset);
    }
  }

  public static synchronized void loadNoblesRank()
  {
    Olympiad._noblesRank = new FastMap().setShared(true);
    Map tmpPlace = new FastMap();

    Connect con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT `char_id` FROM `olympiad_nobles` ORDER BY olympiad_points_past_static DESC");
      rset = statement.executeQuery();

      int place = 1;
      while (rset.next())
        tmpPlace.put(Integer.valueOf(rset.getInt("char_id")), Integer.valueOf(place++));
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    finally
    {
      Close.CSR(con, statement, rset);
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
    Olympiad._log.info("Olympiad: Calculating last period...");
    Connect con = null;
    PreparedStatement statement = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("UPDATE `olympiad_nobles` SET `olympiad_points_past` = `olympiad_points`, `olympiad_points_past_static` = `olympiad_points` WHERE `competitions_done` >= 5");
      statement.execute();
    }
    catch (Exception e)
    {
      Olympiad._log.warning("Olympiad System: Couldn't calculate last period!");
      e.printStackTrace();
    }
    finally
    {
      Close.CS(con, statement);
    }

    Olympiad._log.info("Olympiad: Clearing nobles table...");
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("UPDATE `olympiad_nobles` SET `olympiad_points` = 18, `competitions_done` = 0, `competitions_win` = 0, `competitions_loose` = 0");
      statement.execute();
    }
    catch (Exception e)
    {
      Olympiad._log.warning("Olympiad System: Couldn't cleanup nobles table!");
      e.printStackTrace();
    }
    finally
    {
      Close.CS(con, statement);
    }

    for (Integer nobleId : Olympiad._nobles.keySet())
    {
      StatsSet nobleInfo = (StatsSet)Olympiad._nobles.get(nobleId);
      int points = nobleInfo.getInteger("olympiad_points");
      int compDone = nobleInfo.getInteger("competitions_done");
      nobleInfo.set("olympiad_points", 18);
      if (compDone >= 9)
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
    }
  }

  public static FastList<String> getClassLeaderBoard(int classId)
  {
    FastList names = new FastList();

    Connect con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT `char_name` FROM `olympiad_nobles` WHERE `class_id` = ? AND `olympiad_points` != 0 ORDER BY `olympiad_points` DESC LIMIT 10");
      statement.setInt(1, classId);
      rset = statement.executeQuery();

      while (rset.next()) {
        names.add(rset.getString("char_name"));
      }
      FastList localFastList1 = names;
      return localFastList1;
    }
    catch (Exception e)
    {
      Olympiad._log.warning("Olympiad System: Couldnt get heros from db: ");
      e.printStackTrace();
    }
    finally
    {
      Close.CSR(con, statement, rset);
    }

    return names;
  }

  public static synchronized void sortHerosToBe()
  {
    if (Olympiad._period != 1) {
      return;
    }
    Olympiad._heroesToBe = new FastList();

    Connect con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      for (ClassId id : ClassId.values())
      {
        if (id.getId() == 133)
          continue;
        if (id.level() != 3)
          continue;
        statement = con.prepareStatement("SELECT `char_id`, `char_name` FROM `olympiad_nobles` WHERE `class_id` = ? AND `competitions_done` >= 5 AND `competitions_win` > 0 ORDER BY `olympiad_points` DESC, `competitions_win` DESC, `competitions_done` DESC");
        statement.setInt(1, id.getId());
        rset = statement.executeQuery();

        if (rset.next())
        {
          StatsSet hero = new StatsSet();
          hero.set("class_id", id.getId());
          hero.set("char_id", rset.getInt("char_id"));
          hero.set("char_name", rset.getString("char_name"));

          Olympiad._heroesToBe.add(hero);
        }
        Close.SR(statement, rset);
      }

    }
    catch (Exception e)
    {
      Olympiad._log.warning("Olympiad System: Couldnt heros from db");
    }
    finally
    {
      Close.CSR(con, statement, rset);
    }
  }

  public static synchronized void saveNobleData(int nobleId)
  {
    L2PcInstance player = L2World.getInstance().getPlayer(nobleId);

    Connect con = null;
    PreparedStatement statement = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      StatsSet nobleInfo = (StatsSet)Olympiad._nobles.get(Integer.valueOf(nobleId));

      int classId = nobleInfo.getInteger("class_id");
      String charName = player != null ? player.getName() : nobleInfo.getString("char_name");
      int points = nobleInfo.getInteger("olympiad_points");
      int points_past = nobleInfo.getInteger("olympiad_points_past");
      int points_past_static = nobleInfo.getInteger("olympiad_points_past_static");
      int compDone = nobleInfo.getInteger("competitions_done");
      int compWin = nobleInfo.getInteger("competitions_win");
      int compLoose = nobleInfo.getInteger("competitions_loose");

      statement = con.prepareStatement("REPLACE INTO `olympiad_nobles` (`char_id`, `class_id`, `char_name`, `olympiad_points`, `olympiad_points_past`, `olympiad_points_past_static`, `competitions_done`, `competitions_win`, `competitions_loose`) VALUES (?,?,?,?,?,?,?,?,?)");
      statement.setInt(1, nobleId);
      statement.setInt(2, classId);
      statement.setString(3, charName);
      statement.setInt(4, points);
      statement.setInt(5, points_past);
      statement.setInt(6, points_past_static);
      statement.setInt(7, compDone);
      statement.setInt(8, compWin);
      statement.setInt(9, compLoose);
      statement.execute();
    }
    catch (Exception e)
    {
      Olympiad._log.warning(new StringBuilder().append("Olympiad System: Couldnt save noble info in db for player ").append(player != null ? player.getName() : "null").toString());
      e.printStackTrace();
    }
    finally
    {
      Close.CS(con, statement);
    }
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
    Announcements.getInstance().announceToAll(SystemMessage.id(SystemMessageId.OLYMPIAD_PERIOD_S1_HAS_STARTED).addNumber(Olympiad._currentCycle));

    Calendar currentTime = Calendar.getInstance();
    currentTime.set(5, 1);
    currentTime.add(2, 1);
    currentTime.set(11, 0);
    currentTime.set(12, 0);

    if (Config.ALT_OLYMPIAD_PERIOD == 0)
      Olympiad._olympiadEnd = currentTime.getTimeInMillis();
    else {
      Olympiad._olympiadEnd = getAltEnd();
    }
    Calendar nextChange = Calendar.getInstance();
    Olympiad._nextWeeklyChange = nextChange.getTimeInMillis() + Config.ALT_OLY_WPERIOD;

    Olympiad._isOlympiadEnd = false;
  }

  private static synchronized long getAltEnd()
  {
    Calendar tomorrow = new GregorianCalendar();
    tomorrow.add(5, Config.ALT_OLYMPIAD_PERIOD);
    Calendar result = new GregorianCalendar(tomorrow.get(1), tomorrow.get(2), tomorrow.get(5), 0, 0);

    return result.getTimeInMillis();
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