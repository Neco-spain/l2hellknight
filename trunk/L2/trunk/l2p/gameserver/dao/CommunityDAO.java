package l2p.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import l2p.commons.dbutils.DbUtils;
import l2p.gameserver.Config;
import l2p.gameserver.database.DatabaseFactory;
import l2p.gameserver.model.Effect;
import l2p.gameserver.model.EffectList;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommunityDAO
{
  private static final Logger _log = LoggerFactory.getLogger(CommunityDAO.class);
  private static final String SELECT_BUFF_NAMES = "SELECT name FROM `community_skillsave` WHERE charId = ?";
  private static final String SELECT_SKILL_ID = "SELECT skills FROM `community_skillsave` WHERE charId = ? AND name = ?";
  private static final String DELETE_BUFF_SCHEME = "DELETE FROM `community_skillsave` WHERE charId = ? AND name = ?";
  private static final String INSERT_BUFF_SCHEME = "INSERT INTO `community_skillsave` (`charId`, `name`, `skills`) VALUES (?, ?, ?);";
  private static final CommunityDAO instance = new CommunityDAO();

  public static final CommunityDAO getInstance()
  {
    return instance;
  }

  public List<String> select_bnames(Player player)
  {
    List names = new ArrayList();

    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT name FROM `community_skillsave` WHERE charId = ?");
      statement.setInt(1, player.getObjectId());
      rset = statement.executeQuery();
      while (rset.next())
        names.add(rset.getString("name"));
    }
    catch (Exception e)
    {
      _log.error("CommunityDAO.select_bnames(Player):" + e, e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement, rset);
    }

    return names;
  }

  public List<Integer> select_skills_id(Player player, String name)
  {
    List skills_id = new ArrayList();

    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT skills FROM `community_skillsave` WHERE charId = ? AND name = ?");
      statement.setInt(1, player.getObjectId());
      statement.setString(2, name);
      rset = statement.executeQuery();
      if (rset.next())
        for (String id : rset.getString("skills").split(";"))
          skills_id.add(Integer.valueOf(Integer.parseInt(id)));
    }
    catch (Exception e)
    {
      _log.error("CommunityDAO.select_skills_id(Player,String):" + e, e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement, rset);
    }

    return skills_id;
  }

  public void delete_scheme(Player player, String name)
  {
    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("DELETE FROM `community_skillsave` WHERE charId = ? AND name = ?");
      statement.setInt(1, player.getObjectId());
      statement.setString(2, name);
      statement.execute();
    }
    catch (Exception e)
    {
      _log.error("CommunityDAO.delete_scheme(Player, String):" + e, e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }
  }

  public void insert_skills_id(Player player, String name)
  {
    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      String effectList = "";

      for (Effect skill : player.getEffectList().getAllFirstEffects()) {
        if (Config.COMMUNITYBOARD_BUFF_ALLOW.contains(Integer.valueOf(skill.getSkill().getId())))
          effectList = effectList + new StringBuilder().append(skill.getSkill().getId()).append(";").toString();
      }
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("INSERT INTO `community_skillsave` (`charId`, `name`, `skills`) VALUES (?, ?, ?);");
      statement.setInt(1, player.getObjectId());
      statement.setString(2, name);
      statement.setString(3, effectList);
      statement.execute();
    }
    catch (Exception e)
    {
      _log.error("CommunityDAO.insert_skills_id(Player, String):" + e, e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }
  }

  public boolean buff_name_exists(Player player, String name)
  {
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT * FROM `community_skillsave` WHERE name = '" + player.getObjectId() + "' AND charId = '" + name + "'");
      rset = statement.executeQuery();
      if (rset.next()) {
        int i = 1;
        return i;
      }
    }
    catch (Exception e)
    {
      _log.error("CommunityDAO.buff_name_exists(Player, String):" + e, e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement, rset);
    }

    return false;
  }
}