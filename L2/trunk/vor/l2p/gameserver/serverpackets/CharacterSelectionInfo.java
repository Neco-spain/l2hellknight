package l2p.gameserver.serverpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import l2p.commons.dbutils.DbUtils;
import l2p.gameserver.Config;
import l2p.gameserver.dao.CharacterDAO;
import l2p.gameserver.database.DatabaseFactory;
import l2p.gameserver.model.CharSelectInfoPackage;
import l2p.gameserver.model.base.Experience;
import l2p.gameserver.model.base.Race;
import l2p.gameserver.model.items.Inventory;
import l2p.gameserver.tables.CharTemplateTable;
import l2p.gameserver.templates.PlayerTemplate;
import l2p.gameserver.utils.AutoBan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CharacterSelectionInfo extends L2GameServerPacket
{
  private static final Logger _log = LoggerFactory.getLogger(CharacterSelectionInfo.class);
  private String _loginName;
  private int _sessionId;
  private CharSelectInfoPackage[] _characterPackages;

  public CharacterSelectionInfo(String loginName, int sessionId)
  {
    _sessionId = sessionId;
    _loginName = loginName;
    _characterPackages = loadCharacterSelectInfo(loginName);
  }

  public CharSelectInfoPackage[] getCharInfo()
  {
    return _characterPackages;
  }

  protected final void writeImpl()
  {
    int size = _characterPackages != null ? _characterPackages.length : 0;

    writeC(9);
    writeD(size);
    writeD(7);
    writeC(0);

    long lastAccess = -1L;
    int lastUsed = -1;
    for (int i = 0; i < size; i++) {
      if (lastAccess >= _characterPackages[i].getLastAccess())
        continue;
      lastAccess = _characterPackages[i].getLastAccess();
      lastUsed = i;
    }

    for (int i = 0; i < size; i++)
    {
      CharSelectInfoPackage charInfoPackage = _characterPackages[i];

      writeS(charInfoPackage.getName());
      writeD(charInfoPackage.getCharId());
      writeS(_loginName);
      writeD(_sessionId);
      writeD(charInfoPackage.getClanId());
      writeD(0);

      writeD(charInfoPackage.getSex());
      writeD(charInfoPackage.getRace());
      writeD(charInfoPackage.getBaseClassId());

      writeD(1);

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

      writeD(0);
      writeD(0);
      writeD(0);
      writeD(0);
      writeD(0);
      writeD(0);
      writeD(0);

      for (int PAPERDOLL_ID : Inventory.PAPERDOLL_ORDER) {
        writeD(charInfoPackage.getPaperdollItemId(PAPERDOLL_ID));
      }
      writeD(charInfoPackage.getHairStyle());
      writeD(charInfoPackage.getHairColor());
      writeD(charInfoPackage.getFace());

      writeF(charInfoPackage.getMaxHp());
      writeF(charInfoPackage.getMaxMp());

      writeD(charInfoPackage.getAccessLevel() > -100 ? charInfoPackage.getDeleteTimer() : -1);
      writeD(charInfoPackage.getClassId());
      writeD(i == lastUsed ? 1 : 0);

      writeC(Math.min(charInfoPackage.getPaperdollEnchantEffect(7), 127));
      writeD(charInfoPackage.getPaperdollAugmentationId(7));
      int weaponId = charInfoPackage.getPaperdollItemId(7);
      if (weaponId == 8190)
        writeD(301);
      else if (weaponId == 8689)
        writeD(302);
      else {
        writeD(0);
      }

      writeD(0);
      writeD(0);
      writeD(0);
      writeD(0);
      writeF(0.0D);
      writeF(0.0D);

      writeD(charInfoPackage.getVitalityPoints());
    }
  }

  public static CharSelectInfoPackage[] loadCharacterSelectInfo(String loginName)
  {
    List characterList = new ArrayList();

    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT * FROM characters AS c LEFT JOIN character_subclasses AS cs ON (c.obj_Id=cs.char_obj_id AND cs.active=1) WHERE account_name=? LIMIT 7");
      statement.setString(1, loginName);
      rset = statement.executeQuery();
      while (rset.next())
      {
        CharSelectInfoPackage charInfopackage = restoreChar(rset);
        if (charInfopackage != null)
          characterList.add(charInfopackage);
      }
    }
    catch (Exception e)
    {
      _log.error("could not restore charinfo:", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement, rset);
    }

    return (CharSelectInfoPackage[])characterList.toArray(new CharSelectInfoPackage[characterList.size()]);
  }

  private static int restoreBaseClassId(int objId)
  {
    int classId = 0;

    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT class_id FROM character_subclasses WHERE char_obj_id=? AND isBase=1");
      statement.setInt(1, objId);
      rset = statement.executeQuery();
      while (rset.next())
        classId = rset.getInt("class_id");
    }
    catch (Exception e)
    {
      _log.error("could not restore base class id:", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement, rset);
    }

    return classId;
  }

  private static CharSelectInfoPackage restoreChar(ResultSet chardata)
  {
    CharSelectInfoPackage charInfopackage = null;
    try
    {
      int objectId = chardata.getInt("obj_Id");
      int classid = chardata.getInt("class_id");
      int baseClassId = classid;
      boolean useBaseClass = chardata.getInt("isBase") > 0;
      if (!useBaseClass)
        baseClassId = restoreBaseClassId(objectId);
      boolean female = chardata.getInt("sex") == 1;
      PlayerTemplate templ = CharTemplateTable.getInstance().getTemplate(baseClassId, female);
      if (templ == null)
      {
        _log.error("restoreChar fail | templ == null | objectId: " + objectId + " | classid: " + baseClassId + " | female: " + female);
        return null;
      }
      String name = chardata.getString("char_name");
      charInfopackage = new CharSelectInfoPackage(objectId, name);
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
      charInfopackage.setSex(female ? 1 : 0);

      charInfopackage.setExp(chardata.getLong("exp"));
      charInfopackage.setSp(chardata.getInt("sp"));
      charInfopackage.setClanId(chardata.getInt("clanid"));

      charInfopackage.setKarma(chardata.getInt("karma"));
      charInfopackage.setRace(templ.race.ordinal());
      charInfopackage.setClassId(classid);
      charInfopackage.setBaseClassId(baseClassId);
      long deletetime = chardata.getLong("deletetime");
      int deletedays = 0;
      if (Config.DELETE_DAYS > 0)
        if (deletetime > 0L)
        {
          deletetime = (int)(System.currentTimeMillis() / 1000L - deletetime);
          deletedays = (int)(deletetime / 3600L / 24L);
          if (deletedays >= Config.DELETE_DAYS)
          {
            CharacterDAO.getInstance().deleteCharByObjId(objectId);
            return null;
          }
          deletetime = Config.DELETE_DAYS * 3600 * 24 - deletetime;
        }
        else {
          deletetime = 0L;
        }
      charInfopackage.setDeleteTimer((int)deletetime);
      charInfopackage.setLastAccess(chardata.getLong("lastAccess") * 1000L);
      charInfopackage.setAccessLevel(chardata.getInt("accesslevel"));
      int points = chardata.getInt("vitality") + (int)((System.currentTimeMillis() - charInfopackage.getLastAccess()) / 15.0D);
      if (points > 20000)
        points = 20000;
      else if (points < 0)
        points = 0;
      charInfopackage.setVitalityPoints(points);

      if ((charInfopackage.getAccessLevel() < 0) && (!AutoBan.isBanned(objectId)))
        charInfopackage.setAccessLevel(0);
    }
    catch (Exception e)
    {
      _log.error("", e);
    }

    return charInfopackage;
  }
}