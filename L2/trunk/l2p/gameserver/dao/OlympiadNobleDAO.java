package l2p.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import l2p.commons.dbutils.DbUtils;
import l2p.gameserver.database.DatabaseFactory;
import l2p.gameserver.model.base.ClassId;
import l2p.gameserver.model.entity.olympiad.Olympiad;
import l2p.gameserver.templates.StatsSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OlympiadNobleDAO
{
  private static final Logger _log = LoggerFactory.getLogger(OlympiadNobleDAO.class);
  private static final OlympiadNobleDAO _instance = new OlympiadNobleDAO();
  public static final String SELECT_SQL_QUERY = "SELECT char_id, characters.char_name as char_name, class_id, olympiad_points, olympiad_points_past, olympiad_points_past_static, competitions_done, competitions_loose, competitions_win, game_classes_count, game_noclasses_count, game_team_count FROM olympiad_nobles LEFT JOIN characters ON characters.obj_Id = olympiad_nobles.char_id";
  public static final String REPLACE_SQL_QUERY = "REPLACE INTO `olympiad_nobles` (`char_id`, `class_id`, `olympiad_points`, `olympiad_points_past`, `olympiad_points_past_static`, `competitions_done`, `competitions_win`, `competitions_loose`, game_classes_count, game_noclasses_count, game_team_count) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
  public static final String OLYMPIAD_GET_HEROS = "SELECT `char_id`, characters.char_name AS char_name FROM `olympiad_nobles` LEFT JOIN characters ON char_id=characters.obj_Id WHERE `class_id` = ? AND `competitions_done` >= ? AND `competitions_win` > 0 ORDER BY `olympiad_points` DESC, `competitions_win` DESC, `competitions_done` DESC";
  public static final String OLYMPIAD_GET_HEROS_SOULHOUND = "SELECT `char_id`, characters.char_name AS char_name FROM `olympiad_nobles` LEFT JOIN characters ON char_id=characters.obj_Id WHERE `class_id` IN (?, 133) AND `competitions_done` >= ? AND `competitions_win` > 0 ORDER BY `olympiad_points` DESC, `competitions_win` DESC, `competitions_done` DESC";
  public static final String GET_EACH_CLASS_LEADER = "SELECT characters.char_name AS char_name FROM `olympiad_nobles` LEFT JOIN characters ON char_id=characters.obj_Id WHERE `class_id` = ? AND `olympiad_points_past_static` != 0 ORDER BY `olympiad_points_past_static` DESC LIMIT 10";
  public static final String GET_EACH_CLASS_LEADER_SOULHOUND = "SELECT characters.char_name AS char_name FROM `olympiad_nobles` LEFT JOIN characters ON characters.obj_Id=olympiad_nobles.char_id WHERE `class_id` IN (?, 133) AND `olympiad_points_past_static` != 0 ORDER BY `olympiad_points_past_static` DESC LIMIT 10";
  public static final String GET_ALL_CLASSIFIED_NOBLESS = "SELECT `char_id` FROM `olympiad_nobles` ORDER BY olympiad_points_past_static DESC";
  public static final String OLYMPIAD_CALCULATE_LAST_PERIOD = "UPDATE `olympiad_nobles` SET `olympiad_points_past` = `olympiad_points`, `olympiad_points_past_static` = `olympiad_points` WHERE `competitions_done` >= ?";
  public static final String OLYMPIAD_CLEANUP_NOBLES = "UPDATE `olympiad_nobles` SET `olympiad_points` = ?, `competitions_done` = 0, `competitions_win` = 0, `competitions_loose` = 0, game_classes_count=0, game_noclasses_count=0, game_team_count=0";

  public static OlympiadNobleDAO getInstance()
  {
    return _instance;
  }

  public void select()
  {
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT char_id, characters.char_name as char_name, class_id, olympiad_points, olympiad_points_past, olympiad_points_past_static, competitions_done, competitions_loose, competitions_win, game_classes_count, game_noclasses_count, game_team_count FROM olympiad_nobles LEFT JOIN characters ON characters.obj_Id = olympiad_nobles.char_id");
      rset = statement.executeQuery();
      while (rset.next())
      {
        int classId = rset.getInt("class_id");
        if (classId < 88) {
          for (ClassId id : ClassId.VALUES) {
            if ((id.level() != 3) || (id.getParent(0).getId() != classId))
              continue;
            classId = id.getId();
            break;
          }
        }
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
        statDat.set("game_classes_count", rset.getInt("game_classes_count"));
        statDat.set("game_noclasses_count", rset.getInt("game_noclasses_count"));
        statDat.set("game_team_count", rset.getInt("game_team_count"));

        Olympiad._nobles.put(Integer.valueOf(charId), statDat);
      }
    }
    catch (Exception e)
    {
      _log.error("OlympiadNobleDAO: select():", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement, rset);
    }
  }

  public void replace(int nobleId)
  {
    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      StatsSet nobleInfo = (StatsSet)Olympiad._nobles.get(Integer.valueOf(nobleId));

      statement = con.prepareStatement("REPLACE INTO `olympiad_nobles` (`char_id`, `class_id`, `olympiad_points`, `olympiad_points_past`, `olympiad_points_past_static`, `competitions_done`, `competitions_win`, `competitions_loose`, game_classes_count, game_noclasses_count, game_team_count) VALUES (?,?,?,?,?,?,?,?,?,?,?)");
      statement.setInt(1, nobleId);
      statement.setInt(2, nobleInfo.getInteger("class_id"));
      statement.setInt(3, nobleInfo.getInteger("olympiad_points"));
      statement.setInt(4, nobleInfo.getInteger("olympiad_points_past"));
      statement.setInt(5, nobleInfo.getInteger("olympiad_points_past_static"));
      statement.setInt(6, nobleInfo.getInteger("competitions_done"));
      statement.setInt(7, nobleInfo.getInteger("competitions_win"));
      statement.setInt(8, nobleInfo.getInteger("competitions_loose"));
      statement.setInt(9, nobleInfo.getInteger("game_classes_count"));
      statement.setInt(10, nobleInfo.getInteger("game_noclasses_count"));
      statement.setInt(11, nobleInfo.getInteger("game_team_count"));
      statement.execute();
    }
    catch (Exception e)
    {
      _log.error("OlympiadNobleDAO: replace(int): " + nobleId, e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }
  }
}