package l2m.gameserver.data.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import l2p.commons.dbutils.DbUtils;
import l2m.gameserver.Config;
import l2m.gameserver.database.DatabaseFactory;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.base.ClassId;
import l2m.gameserver.templates.PlayerTemplate;
import l2m.gameserver.utils.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CharacterDAO
{
  private static final Logger _log = LoggerFactory.getLogger(CharacterDAO.class);

  private static CharacterDAO _instance = new CharacterDAO();

  public static CharacterDAO getInstance()
  {
    return _instance;
  }

  public void deleteCharByObjId(int objid)
  {
    if (objid < 0)
      return;
    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("DELETE FROM characters WHERE obj_Id=?");
      statement.setInt(1, objid);
      statement.execute();
    }
    catch (Exception e)
    {
      _log.error("", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }
  }

  public boolean insert(Player player)
  {
    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("INSERT INTO `characters` (account_name, obj_Id, char_name, face, hairStyle, hairColor, sex, x, y, z, karma, pvpkills, pkkills, clanid, createtime, deletetime, title, accesslevel, online, leaveclan, deleteclan, nochannel, pledge_type, pledge_rank, lvl_joined_academy, apprentice) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
      statement.setString(1, player.getAccountName());
      statement.setInt(2, player.getObjectId());
      statement.setString(3, player.getName());
      statement.setInt(4, player.getFace());
      statement.setInt(5, player.getHairStyle());
      statement.setInt(6, player.getHairColor());
      statement.setInt(7, player.getSex());
      statement.setInt(8, player.getTemplate().spawnLoc.x);
      statement.setInt(9, player.getTemplate().spawnLoc.y);
      statement.setInt(10, player.getTemplate().spawnLoc.z);
      statement.setInt(11, player.getKarma());
      statement.setInt(12, player.getPvpKills());
      statement.setInt(13, player.getPkKills());
      statement.setInt(14, player.getClanId());
      statement.setLong(15, player.getCreateTime() / 1000L);
      statement.setInt(16, player.getDeleteTimer());
      statement.setString(17, player.getTitle());
      statement.setInt(18, player.getAccessLevel());
      statement.setInt(19, player.isOnline() ? 1 : 0);
      statement.setLong(20, player.getLeaveClanTime() / 1000L);
      statement.setLong(21, player.getDeleteClanTime() / 1000L);
      statement.setLong(22, player.getNoChannel() > 0L ? player.getNoChannel() / 1000L : player.getNoChannel());
      statement.setInt(23, player.getPledgeType());
      statement.setInt(24, player.getPowerGrade());
      statement.setInt(25, player.getLvlJoinedAcademy());
      statement.setInt(26, player.getApprentice());
      statement.executeUpdate();
      DbUtils.close(statement);

      statement = con.prepareStatement("INSERT INTO character_subclasses (char_obj_id, class_id, exp, sp, curHp, curMp, curCp, maxHp, maxMp, maxCp, level, active, isBase, death_penalty, certification) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
      statement.setInt(1, player.getObjectId());
      statement.setInt(2, player.getTemplate().classId.getId());
      if (Config.STARTING_LEVEL > 0) {
        statement.setLong(3, l2p.gameserver.model.base.Experience.LEVEL[Config.STARTING_LEVEL]);
      }
      else {
        statement.setInt(3, 0);
      }
      statement.setInt(4, 0);
      statement.setDouble(5, player.getTemplate().baseHpMax + player.getTemplate().lvlHpAdd + player.getTemplate().lvlHpMod);
      statement.setDouble(6, player.getTemplate().baseMpMax + player.getTemplate().lvlMpAdd + player.getTemplate().lvlMpMod);
      statement.setDouble(7, player.getTemplate().baseCpMax + player.getTemplate().lvlCpAdd + player.getTemplate().lvlCpMod);
      statement.setDouble(8, player.getTemplate().baseHpMax + player.getTemplate().lvlHpAdd + player.getTemplate().lvlHpMod);
      statement.setDouble(9, player.getTemplate().baseMpMax + player.getTemplate().lvlMpAdd + player.getTemplate().lvlMpMod);
      statement.setDouble(10, player.getTemplate().baseCpMax + player.getTemplate().lvlCpAdd + player.getTemplate().lvlCpMod);
      if (Config.STARTING_LEVEL > 0) {
        statement.setInt(11, Config.STARTING_LEVEL);
      }
      else {
        statement.setInt(11, Config.STARTING_LEVEL);
      }
      statement.setInt(12, 1);
      statement.setInt(13, 1);
      statement.setInt(14, 0);
      statement.setInt(15, 0);
      statement.executeUpdate();
    }
    catch (Exception e)
    {
      _log.error("", e);
      int i = 0;
      return i; } finally { DbUtils.closeQuietly(con, statement);
    }
    return true;
  }

  public int getObjectIdByName(String name)
  {
    int result = 0;

    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT obj_Id FROM characters WHERE char_name=?");
      statement.setString(1, name);
      rset = statement.executeQuery();
      if (rset.next())
        result = rset.getInt(1);
    }
    catch (Exception e)
    {
      _log.error("CharNameTable.getObjectIdByName(String): " + e, e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement, rset);
    }

    return result;
  }

  public String getNameByObjectId(int objectId)
  {
    String result = "";

    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT char_name FROM characters WHERE obj_Id=?");
      statement.setInt(1, objectId);
      rset = statement.executeQuery();
      if (rset.next())
        result = rset.getString(1);
    }
    catch (Exception e)
    {
      _log.error("CharNameTable.getObjectIdByName(int): " + e, e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement, rset);
    }

    return result;
  }

  public int accountCharNumber(String account)
  {
    int number = 0;

    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT COUNT(char_name) FROM characters WHERE account_name=?");
      statement.setString(1, account);
      rset = statement.executeQuery();
      if (rset.next())
        number = rset.getInt(1);
    }
    catch (Exception e)
    {
      _log.error("", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement, rset);
    }

    return number;
  }
}