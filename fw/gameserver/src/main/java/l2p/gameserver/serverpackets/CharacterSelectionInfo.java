package l2p.gameserver.serverpackets;

import l2p.commons.dbutils.DbUtils;
import l2p.gameserver.Config;
import l2p.gameserver.dao.CharacterDAO;
import l2p.gameserver.database.DatabaseFactory;
import l2p.gameserver.model.CharSelectionInfo;
import l2p.gameserver.model.CharSelectionInfo.CharSelectInfoPackage;
import l2p.gameserver.model.base.Experience;
import l2p.gameserver.model.items.Inventory;
import l2p.gameserver.utils.AutoBan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CharacterSelectionInfo extends L2GameServerPacket {
    // dc cd SdSddddddddddffdQfddddddddddddddddddddddddddddddddddddddddffdddchhdddddffdd
    private static final Logger _log = LoggerFactory.getLogger(CharacterSelectionInfo.class);

    private String _loginName;

    private int _sessionId;

    private CharSelectionInfo charSelectionInfo;

    public CharacterSelectionInfo(String loginName, int sessionId) {
        _sessionId = sessionId;
        _loginName = loginName;
        charSelectionInfo = loadCharacterSelectInfo(loginName);
    }

    public CharSelectionInfo getCharInfo() {
        return charSelectionInfo;
    }

    @Override
    protected final void writeImpl() {
        int size = charSelectionInfo.size();

        writeC(0x09);

        writeD(size);
        writeD(0x07); //Kamael, 0x07 ?
        writeC(0); //разрешает или запрещает создание игроков

        writeC(1);// Unknown (GOD)
        writeD(0);// Unknown (GOD)

        long lastAccess = -1L;
        int lastUsed = -1;
        for (CharSelectInfoPackage info : charSelectionInfo) {
            if (lastAccess < info.getLastAccess()) {
                lastAccess = info.getLastAccess();
                lastUsed++;
            }
        }
        int i = 0;
        for (CharSelectInfoPackage charInfoPackage : charSelectionInfo) {

            writeS(charInfoPackage.getName());
            writeD(charInfoPackage.getCharId()); // ?
            writeS(_loginName);
            writeD(_sessionId);
            writeD(charInfoPackage.getClanId());
            writeD(0x00); // ??

            writeD(charInfoPackage.getSex());
            writeD(charInfoPackage.getRace());
            writeD(charInfoPackage.getBaseClassId());

            writeD(0x01); // active ??

            writeD(charInfoPackage.getX());
            writeD(charInfoPackage.getY());
            writeD(charInfoPackage.getZ());

            writeF(charInfoPackage.getCurrentHp());
            writeF(charInfoPackage.getCurrentMp());

            writeD(charInfoPackage.getSp());
            writeQ(charInfoPackage.getExp());
            int lvl = charInfoPackage.getLevel();
            writeF(Experience.getExpPercent(lvl, charInfoPackage.getExp()));
            writeD(lvl);

            writeD(charInfoPackage.getKarma());
            writeD(charInfoPackage.getPk());
            writeD(charInfoPackage.getPvP());

            writeD(0x00);
            writeD(0x00);
            writeD(0x00);
            writeD(0x00);
            writeD(0x00);
            writeD(0x00);
            writeD(0x00);

            for (int PAPERDOLL_ID : Inventory.PAPERDOLL_ORDER)
                writeD(charInfoPackage.getPaperdollItemId(PAPERDOLL_ID));

            writeD(charInfoPackage.getHairStyle());
            writeD(charInfoPackage.getHairColor());
            writeD(charInfoPackage.getFace());

            writeF(charInfoPackage.getMaxHp()); // hp max
            writeF(charInfoPackage.getMaxMp()); // mp max

            writeD(charInfoPackage.getAccessLevel() > -100 ? charInfoPackage.getDeleteTimer() : -1);
            writeD(charInfoPackage.getClassId());
            writeD(i == lastUsed ? 1 : 0);

            writeC(Math.min(charInfoPackage.getPaperdollEnchantEffect(Inventory.PAPERDOLL_RHAND), 127));
            writeD(charInfoPackage.getPaperdollAugmentationId(Inventory.PAPERDOLL_RHAND));
            int weaponId = charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_RHAND);
            if (weaponId == 8190) // Transform id (на оффе отображаются только КВ трансформации или вообще не отображаются ;)
                writeD(301);
            else if (weaponId == 8689)
                writeD(302);
            else
                writeD(0x00);

            //TODO: Pet info?
            writeD(0x00);
            writeD(0x00);
            writeD(0x00);
            writeD(0x00);
            writeF(0x00);
            writeF(0x00);

            if (getClient().getRevision() == 411) {
                // New Protocol 411
                //writeD(0x00);
                //writeD(0x00);
                //writeD(0x00);
                //writeD(0x00);
                //writeD(0x00);
                //writeD(0x00);
                //writeD(0x00);
                //writeD(0x00);
                //writeD(0x00);
                // End Protocol 411
            }


            writeD(charSelectionInfo.getVitalityPoints());
            writeD(charInfoPackage.getAccessLevel() > -100 ? 0x01 : 0x00);// Активен ли.

            i++;
        }
    }

    public static CharSelectionInfo loadCharacterSelectInfo(String loginName) {
        CharSelectionInfo charSelectionInfo = new CharSelectionInfo();

        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT * FROM characters AS c LEFT JOIN character_subclasses AS cs ON (c.obj_Id=cs.char_obj_id AND cs.active=1) WHERE account_name=? LIMIT 7");
            statement.setString(1, loginName);
            rset = statement.executeQuery();
            while (rset.next()) { // fills the package
                charSelectionInfo.addSelectionInfo(restoreChar(rset));
            }
            DbUtils.closeQuietly(statement, rset);
            statement = con.prepareStatement("SELECT `points` FROM `vitality_points` WHERE `account_name`=?");
            statement.setString(1, loginName);
            rset = statement.executeQuery();
            if (rset.next()) {
                int points = rset.getInt(1);
                charSelectionInfo.setVitalityPoints(points);
            }
        } catch (Exception e) {
            _log.error("could not restore charinfo:", e);
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }

        return charSelectionInfo;
    }

    private static int restoreBaseClassId(int objId) {
        int classId = 0;

        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT class_id FROM character_subclasses WHERE char_obj_id=? AND isBase=1");
            statement.setInt(1, objId);
            rset = statement.executeQuery();
            while (rset.next())
                classId = rset.getInt("class_id");
        } catch (Exception e) {
            _log.error("could not restore base class id:", e);
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }

        return classId;
    }

    private static CharSelectionInfo.CharSelectInfoPackage restoreChar(ResultSet chardata) {
        CharSelectionInfo.CharSelectInfoPackage charInfopackage = null;
        try {
            int objectId = chardata.getInt("obj_Id");
            int classid = chardata.getInt("class_id");
            int baseClassId = classid;
            boolean useBaseClass = chardata.getInt("isBase") > 0;
            if (!useBaseClass)
                baseClassId = restoreBaseClassId(objectId);
            /* boolean female = chardata.getInt("sex") == 1;
            PlayerTemplate templ = CharTemplateTable.getInstance().getTemplate(baseClassId, female);
            if (templ == null) {
                _log.error("restoreChar fail | templ == null | objectId: " + objectId + " | classid: " + baseClassId + " | female: " + female);
                return null;
            }          */
            String name = chardata.getString("char_name");
            charInfopackage = new CharSelectionInfo.CharSelectInfoPackage(objectId, name);
            charInfopackage.setLevel(chardata.getInt("level"));
            charInfopackage.setMaxHp(chardata.getInt("maxHp"));
            charInfopackage.setCurrentHp(chardata.getDouble("curHp"));
            charInfopackage.setMaxMp(chardata.getInt("maxMp"));
            charInfopackage.setCurrentMp(chardata.getDouble("curMp"));

            charInfopackage.setX(chardata.getInt("x"));
            charInfopackage.setY(chardata.getInt("y"));
            charInfopackage.setZ(chardata.getInt("z"));
            charInfopackage.setPk(chardata.getInt("pkkills"));
            charInfopackage.setPvP(chardata.getInt("pvpkills"));

            charInfopackage.setFace(chardata.getInt("face"));
            charInfopackage.setHairStyle(chardata.getInt("hairstyle"));
            charInfopackage.setHairColor(chardata.getInt("haircolor"));
            charInfopackage.setSex(chardata.getInt("sex"));

            charInfopackage.setExp(chardata.getLong("exp"));
            charInfopackage.setSp(chardata.getInt("sp"));
            charInfopackage.setClanId(chardata.getInt("clanid"));

            charInfopackage.setKarma(chardata.getInt("karma"));
            charInfopackage.setRace(chardata.getInt("race"));
            charInfopackage.setClassId(classid);
            charInfopackage.setBaseClassId(baseClassId);
            long deletetime = chardata.getLong("deletetime");
            int deletedays = 0;
            if (Config.DELETE_DAYS > 0)
                if (deletetime > 0) {
                    deletetime = (int) (System.currentTimeMillis() / 1000 - deletetime);
                    deletedays = (int) (deletetime / 3600 / 24);
                    if (deletedays >= Config.DELETE_DAYS) {
                        CharacterDAO.getInstance().deleteCharByObjId(objectId);
                        return null;
                    }
                    deletetime = Config.DELETE_DAYS * 3600 * 24 - deletetime;
                } else
                    deletetime = 0;
            charInfopackage.setDeleteTimer((int) deletetime);
            charInfopackage.setLastAccess(chardata.getLong("lastAccess") * 1000L);
            charInfopackage.setAccessLevel(chardata.getInt("accesslevel"));

            if (charInfopackage.getAccessLevel() < 0 && !AutoBan.isBanned(objectId))
                charInfopackage.setAccessLevel(0);
        } catch (Exception e) {
            _log.error("", e);
        }

        return charInfopackage;
    }
}