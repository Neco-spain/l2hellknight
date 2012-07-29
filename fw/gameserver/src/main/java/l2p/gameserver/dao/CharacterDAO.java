package l2p.gameserver.dao;

import l2p.commons.dbutils.DbUtils;
import l2p.gameserver.database.DatabaseFactory;
import l2p.gameserver.model.Player;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CharacterDAO {
    private static final Logger _log = LoggerFactory.getLogger(CharacterDAO.class);

    private static CharacterDAO _instance = new CharacterDAO();

    public static CharacterDAO getInstance() {
        return _instance;
    }

    public void deleteCharByObjId(int objid) {
        if (objid < 0)
            return;
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("DELETE FROM characters WHERE obj_Id=?");
            statement.setInt(1, objid);
            statement.execute();
        } catch (Exception e) {
            _log.error("", e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public boolean insert(Player player) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("INSERT INTO `characters` (account_name, obj_Id, char_name, face, hairStyle, hairColor, sex, karma, pvpkills, pkkills, clanid, createtime, deletetime, title, accesslevel, online, leaveclan, deleteclan, nochannel, pledge_type, pledge_rank, lvl_joined_academy, apprentice) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            statement.setString(1, player.getAccountName());
            statement.setInt(2, player.getObjectId());
            statement.setString(3, player.getName());
            statement.setInt(4, player.getFace());
            statement.setInt(5, player.getHairStyle());
            statement.setInt(6, player.getHairColor());
            statement.setInt(7, player.getSex());
            statement.setInt(8, player.getKarma());
            statement.setInt(9, player.getPvpKills());
            statement.setInt(10, player.getPkKills());
            statement.setInt(11, player.getClanId());
            statement.setLong(12, player.getCreateTime() / 1000);
            statement.setInt(13, player.getDeleteTimer());
            statement.setString(14, player.getTitle());
            statement.setInt(15, player.getAccessLevel());
            statement.setInt(16, player.isOnline() ? 1 : 0);
            statement.setLong(17, player.getLeaveClanTime() / 1000);
            statement.setLong(18, player.getDeleteClanTime() / 1000);
            statement.setLong(19, player.getNoChannel() > 0 ? player.getNoChannel() / 1000 : player.getNoChannel());
            statement.setInt(20, player.getPledgeType());
            statement.setInt(21, player.getPowerGrade());
            statement.setInt(22, player.getLvlJoinedAcademy());
            statement.setInt(23, player.getApprentice());
            statement.executeUpdate();
            DbUtils.close(statement);

            statement = con.prepareStatement("INSERT INTO character_subclasses (char_obj_id, class_id, exp, sp, curHp, curMp, curCp, maxHp, maxMp, maxCp, level, active, isBase, death_penalty, certification, `index`, `race`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            statement.setInt(1, player.getObjectId());
            statement.setInt(2, player.getClassId().getId());
            statement.setInt(3, 0);
            statement.setInt(4, 0);
            statement.setDouble(5, player.getTemplate().baseHpMax + player.getTemplate().lvlHpAdd + player.getTemplate().lvlHpMod);
            statement.setDouble(6, player.getTemplate().baseMpMax + player.getTemplate().lvlMpAdd + player.getTemplate().lvlMpMod);
            statement.setDouble(7, player.getTemplate().baseCpMax + player.getTemplate().lvlCpAdd + player.getTemplate().lvlCpMod);
            statement.setDouble(8, player.getTemplate().baseHpMax + player.getTemplate().lvlHpAdd + player.getTemplate().lvlHpMod);
            statement.setDouble(9, player.getTemplate().baseMpMax + player.getTemplate().lvlMpAdd + player.getTemplate().lvlMpMod);
            statement.setDouble(10, player.getTemplate().baseCpMax + player.getTemplate().lvlCpAdd + player.getTemplate().lvlCpMod);
            statement.setInt(11, 1);
            statement.setInt(12, 1);
            statement.setInt(13, 1);
            statement.setInt(14, 0);
            statement.setInt(15, 0);
            statement.setInt(16, 1);
            statement.setInt(17, player.getRace().ordinal());
            statement.executeUpdate();
        } catch (final Exception e) {
            _log.error("", e);
            return false;
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
        return true;
    }

    public int getObjectIdByName(String name) {
        int result = 0;

        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT obj_Id FROM characters WHERE char_name=?");
            statement.setString(1, name);
            rset = statement.executeQuery();
            if (rset.next())
                result = rset.getInt(1);
        } catch (Exception e) {
            _log.error("CharNameTable.getObjectIdByName(String): " + e, e);
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }

        return result;
    }

    public String getNameByObjectId(int objectId) {
        String result = StringUtils.EMPTY;

        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT char_name FROM characters WHERE obj_Id=?");
            statement.setInt(1, objectId);
            rset = statement.executeQuery();
            if (rset.next())
                result = rset.getString(1);
        } catch (Exception e) {
            _log.error("CharNameTable.getObjectIdByName(int): " + e, e);
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }

        return result;
    }

    public int accountCharNumber(String account) {
        int number = 0;

        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT COUNT(char_name) FROM characters WHERE account_name=?");
            statement.setString(1, account);
            rset = statement.executeQuery();
            if (rset.next())
                number = rset.getInt(1);
        } catch (Exception e) {
            _log.error("", e);
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }

        return number;
    }
}