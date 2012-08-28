package net.sf.l2j.gameserver.network.serverpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.logging.Logger;
import javolution.util.FastList;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.CharSelectInfoPackage;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;

public class CharSelectInfo extends L2GameServerPacket
{
  private static final String _S__1F_CHARSELECTINFO = "[S] 1F CharSelectInfo";
  private static Logger _log = Logger.getLogger(CharSelectInfo.class.getName());
  private String _loginName;
  private int _sessionId;
  private int _activeId;
  private CharSelectInfoPackage[] _characterPackages;

  public CharSelectInfo(String loginName, int sessionId)
  {
    _sessionId = sessionId;
    _loginName = loginName;
    _characterPackages = loadCharacterSelectInfo();
    _activeId = -1;
  }

  public CharSelectInfo(String loginName, int sessionId, int activeId)
  {
    _sessionId = sessionId;
    _loginName = loginName;
    _characterPackages = loadCharacterSelectInfo();
    _activeId = activeId;
  }

  public CharSelectInfoPackage[] getCharInfo()
  {
    return _characterPackages;
  }

  protected final void writeImpl()
  {
    int size = _characterPackages.length;

    writeC(19);
    writeD(size);

    long lastAccess = 0L;

    if (_activeId == -1) {
      for (int i = 0; i < size; i++) {
        if (lastAccess >= _characterPackages[i].getLastAccess())
          continue;
        lastAccess = _characterPackages[i].getLastAccess();
        _activeId = i;
      }
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

      if (charInfoPackage.getClassId() == charInfoPackage.getBaseClassId())
        writeD(charInfoPackage.getClassId());
      else {
        writeD(charInfoPackage.getBaseClassId());
      }
      writeD(1);

      writeD(0);
      writeD(0);
      writeD(0);

      writeF(charInfoPackage.getCurrentHp());
      writeF(charInfoPackage.getCurrentMp());

      writeD(charInfoPackage.getSp());
      writeQ(charInfoPackage.getExp());
      writeD(charInfoPackage.getLevel());

      writeD(charInfoPackage.getKarma());
      writeD(0);
      writeD(0);
      writeD(0);
      writeD(0);
      writeD(0);
      writeD(0);
      writeD(0);
      writeD(0);
      writeD(0);

      writeD(charInfoPackage.getPaperdollObjectId(17));
      writeD(charInfoPackage.getPaperdollObjectId(2));
      writeD(charInfoPackage.getPaperdollObjectId(1));
      writeD(charInfoPackage.getPaperdollObjectId(3));
      writeD(charInfoPackage.getPaperdollObjectId(5));
      writeD(charInfoPackage.getPaperdollObjectId(4));
      writeD(charInfoPackage.getPaperdollObjectId(6));
      writeD(charInfoPackage.getPaperdollObjectId(7));
      writeD(charInfoPackage.getPaperdollObjectId(8));
      writeD(charInfoPackage.getPaperdollObjectId(9));
      writeD(charInfoPackage.getPaperdollObjectId(10));
      writeD(charInfoPackage.getPaperdollObjectId(11));
      writeD(charInfoPackage.getPaperdollObjectId(12));
      writeD(charInfoPackage.getPaperdollObjectId(13));
      writeD(charInfoPackage.getPaperdollObjectId(14));
      writeD(charInfoPackage.getPaperdollObjectId(16));
      writeD(charInfoPackage.getPaperdollObjectId(15));

      writeD(charInfoPackage.getPaperdollItemId(17));
      writeD(charInfoPackage.getPaperdollItemId(2));
      writeD(charInfoPackage.getPaperdollItemId(1));
      writeD(charInfoPackage.getPaperdollItemId(3));
      writeD(charInfoPackage.getPaperdollItemId(5));
      writeD(charInfoPackage.getPaperdollItemId(4));
      writeD(charInfoPackage.getPaperdollItemId(6));
      writeD(charInfoPackage.getPaperdollItemId(7));
      writeD(charInfoPackage.getPaperdollItemId(8));
      writeD(charInfoPackage.getPaperdollItemId(9));
      writeD(charInfoPackage.getPaperdollItemId(10));
      writeD(charInfoPackage.getPaperdollItemId(11));
      writeD(charInfoPackage.getPaperdollItemId(12));
      writeD(charInfoPackage.getPaperdollItemId(13));
      writeD(charInfoPackage.getPaperdollItemId(14));
      writeD(charInfoPackage.getPaperdollItemId(16));
      writeD(charInfoPackage.getPaperdollItemId(15));

      writeD(charInfoPackage.getHairStyle());
      writeD(charInfoPackage.getHairColor());
      writeD(charInfoPackage.getFace());

      writeF(charInfoPackage.getMaxHp());
      writeF(charInfoPackage.getMaxMp());

      long deleteTime = charInfoPackage.getDeleteTimer();
      int deletedays = 0;
      if (deleteTime > 0L)
        deletedays = (int)((deleteTime - System.currentTimeMillis()) / 1000L);
      writeD(deletedays);

      writeD(charInfoPackage.getClassId());
      if (i == _activeId)
        writeD(1);
      else {
        writeD(0);
      }
      writeC(charInfoPackage.getEnchantEffect() > 127 ? 127 : charInfoPackage.getEnchantEffect());

      writeD(charInfoPackage.getAugmentationId());
    }
  }

  private CharSelectInfoPackage[] loadCharacterSelectInfo()
  {
    List characterList = new FastList();

    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("SELECT account_name, obj_Id, char_name, level, maxHp, curHp, maxMp, curMp, acc, crit, evasion, mAtk, mDef, mSpd, pAtk, pDef, pSpd, runSpd, walkSpd, str, con, dex, _int, men, wit, face, hairStyle, hairColor, sex, heading, x, y, z, movement_multiplier, attack_speed_multiplier, colRad, colHeight, exp, sp, karma, pvpkills, pkkills, clanid, maxload, race, classid, deletetime, cancraft, title, rec_have, rec_left, accesslevel, online, char_slot, lastAccess, base_class FROM characters WHERE account_name=?");
      statement.setString(1, _loginName);
      ResultSet charList = statement.executeQuery();

      while (charList.next())
      {
        CharSelectInfoPackage charInfopackage = restoreChar(charList);
        if (charInfopackage != null) {
          characterList.add(charInfopackage);
        }
      }
      charList.close();
      statement.close();

      CharSelectInfoPackage[] arrayOfCharSelectInfoPackage = (CharSelectInfoPackage[])characterList.toArray(new CharSelectInfoPackage[characterList.size()]);
      return arrayOfCharSelectInfoPackage;
    }
    catch (Exception e)
    {
      _log.warning("Could not restore char info: " + e);
    }
    finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
    return new CharSelectInfoPackage[0];
  }

  private void loadCharacterSubclassInfo(CharSelectInfoPackage charInfopackage, int ObjectId, int activeClassId)
  {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("SELECT exp, sp, level FROM character_subclasses WHERE char_obj_id=? && class_id=? ORDER BY char_obj_id");
      statement.setInt(1, ObjectId);
      statement.setInt(2, activeClassId);
      ResultSet charList = statement.executeQuery();

      if (charList.next())
      {
        charInfopackage.setExp(charList.getLong("exp"));
        charInfopackage.setSp(charList.getInt("sp"));
        charInfopackage.setLevel(charList.getInt("level"));
      }

      charList.close();
      statement.close();
    }
    catch (Exception e)
    {
      _log.warning("Could not restore char subclass info: " + e);
    }
    finally {
      try {
        con.close();
      }
      catch (Exception e) {
      }
    }
  }

  private CharSelectInfoPackage restoreChar(ResultSet chardata) throws Exception {
    int objectId = chardata.getInt("obj_id");

    long deletetime = chardata.getLong("deletetime");
    if (deletetime > 0L)
    {
      if (System.currentTimeMillis() > deletetime)
      {
        L2PcInstance cha = L2PcInstance.load(objectId);
        L2Clan clan = cha.getClan();
        if (clan != null) {
          clan.removeClanMember(cha.getName(), 0L);
        }
        L2GameClient.deleteCharByObjId(objectId);
        return null;
      }
    }

    String name = chardata.getString("char_name");

    CharSelectInfoPackage charInfopackage = new CharSelectInfoPackage(objectId, name);
    charInfopackage.setLevel(chardata.getInt("level"));
    charInfopackage.setMaxHp(chardata.getInt("maxhp"));
    charInfopackage.setCurrentHp(chardata.getDouble("curhp"));
    charInfopackage.setMaxMp(chardata.getInt("maxmp"));
    charInfopackage.setCurrentMp(chardata.getDouble("curmp"));
    charInfopackage.setKarma(chardata.getInt("karma"));

    charInfopackage.setFace(chardata.getInt("face"));
    charInfopackage.setHairStyle(chardata.getInt("hairstyle"));
    charInfopackage.setHairColor(chardata.getInt("haircolor"));
    charInfopackage.setSex(chardata.getInt("sex"));

    charInfopackage.setExp(chardata.getLong("exp"));
    charInfopackage.setSp(chardata.getInt("sp"));
    charInfopackage.setClanId(chardata.getInt("clanid"));

    charInfopackage.setRace(chardata.getInt("race"));

    int baseClassId = chardata.getInt("base_class");
    int activeClassId = chardata.getInt("classid");

    if (baseClassId != activeClassId) {
      loadCharacterSubclassInfo(charInfopackage, objectId, activeClassId);
    }
    charInfopackage.setClassId(activeClassId);

    int weaponObjId = charInfopackage.getPaperdollObjectId(14);
    if (weaponObjId < 1) {
      weaponObjId = charInfopackage.getPaperdollObjectId(7);
    }
    if (weaponObjId > 0)
    {
      Connection con = null;
      try
      {
        con = L2DatabaseFactory.getInstance().getConnection();
        PreparedStatement statement = con.prepareStatement("SELECT attributes FROM augmentations WHERE item_id=?");
        statement.setInt(1, weaponObjId);
        ResultSet result = statement.executeQuery();

        if (result.next())
        {
          charInfopackage.setAugmentationId(result.getInt("attributes"));
        }

        result.close();
        statement.close();
      }
      catch (Exception e)
      {
        _log.warning("Could not restore augmentation info: " + e); } finally {
        try {
          con.close();
        }
        catch (Exception e)
        {
        }

      }

    }

    if ((baseClassId == 0) && (activeClassId > 0))
      charInfopackage.setBaseClassId(activeClassId);
    else {
      charInfopackage.setBaseClassId(baseClassId);
    }
    charInfopackage.setDeleteTimer(deletetime);
    charInfopackage.setLastAccess(chardata.getLong("lastAccess"));

    return charInfopackage;
  }

  public String getType()
  {
    return "[S] 1F CharSelectInfo";
  }
}