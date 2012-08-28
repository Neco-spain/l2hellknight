package net.sf.l2j.gameserver.network.clientpackets;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.LoginServerThread.SessionKey;
import net.sf.l2j.gameserver.datatables.CharNameTable;
import net.sf.l2j.gameserver.datatables.CharTemplateTable;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.datatables.SkillTreeTable;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2ShortCut;
import net.sf.l2j.gameserver.model.L2SkillLearn;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.CharCreateFail;
import net.sf.l2j.gameserver.network.serverpackets.CharCreateOk;
import net.sf.l2j.gameserver.network.serverpackets.CharSelectInfo;
import net.sf.l2j.gameserver.templates.L2PcTemplate;
import net.sf.l2j.gameserver.util.Util;
import net.sf.l2j.util.Location;
import org.mmocore.network.MMOConnection;

public final class CharacterCreate extends L2GameClientPacket
{
  private String _name;
  private int _race;
  private byte _sex;
  private int _classId;
  private int _int;
  private int _str;
  private int _con;
  private int _men;
  private int _dex;
  private int _wit;
  private byte _hairStyle;
  private byte _hairColor;
  private byte _face;
  private long _exp;
  private static final Pattern cnamePattern = Pattern.compile(Config.CNAME_TEMPLATE);

  protected void readImpl()
  {
    _name = readS();
    _race = readD();
    _sex = (byte)readD();
    _classId = readD();
    _int = readD();
    _str = readD();
    _con = readD();
    _men = readD();
    _dex = readD();
    _wit = readD();
    _hairStyle = (byte)readD();
    _hairColor = (byte)readD();
    _face = (byte)readD();
    _exp = 6299994999L;
  }

  protected void runImpl()
  {
    if ((CharNameTable.getInstance().accountCharNumber(((L2GameClient)getClient()).getAccountName()) >= Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT) && (Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT != 0)) {
      sendPacket(new CharCreateFail(1));
      return;
    }if (CharNameTable.getInstance().doesCharNameExist(_name)) {
      sendPacket(new CharCreateFail(2));
      return;
    }if ((_name.length() < 3) || (_name.length() > 16) || (!Util.isAlphaNumeric(_name)) || (!isValidName(_name))) {
      sendPacket(new CharCreateFail(3));
      return;
    }

    L2PcTemplate template = CharTemplateTable.getInstance().getTemplate(_classId);
    if ((template == null) || (template.classBaseLevel > 1)) {
      sendPacket(new CharCreateFail(0));
      return;
    }

    int objectId = IdFactory.getInstance().getNextId();
    L2PcInstance newChar = L2PcInstance.create(objectId, template, ((L2GameClient)getClient()).getAccountName(), _name, _hairStyle, _hairColor, _face, _sex != 0);

    if (Config.ALT_START_LEVEL > 0) {
      newChar.fullRestore();
    }
    else
    {
      newChar.setCurrentHp(template.baseHpMax);
      newChar.setCurrentCp(template.baseCpMax);
      newChar.setCurrentMp(template.baseMpMax);
    }

    sendPacket(new CharCreateOk());

    initNewChar((L2GameClient)getClient(), newChar);
  }

  private boolean isValidName(String text)
  {
    return cnamePattern.matcher(text).matches();
  }

  private void initNewChar(L2GameClient client, L2PcInstance newChar)
  {
    L2World.getInstance().storeObject(newChar);

    L2PcTemplate template = newChar.getTemplate();

    newChar.addAdena("Init", Config.STARTING_ADENA, null, false);

    Location loc = template.getRandomSpawnPoint();
    newChar.setXYZInvisible(loc.x, loc.y, loc.z);

    L2ShortCut shortcut = new L2ShortCut(0, 0, 3, 2, -1, 1);
    newChar.registerShortCut(shortcut);

    shortcut = new L2ShortCut(3, 0, 3, 5, -1, 1);
    newChar.registerShortCut(shortcut);

    shortcut = new L2ShortCut(10, 0, 3, 0, -1, 1);
    newChar.registerShortCut(shortcut);

    for (Integer itemId : template.getItems()) {
      L2ItemInstance item = newChar.getInventory().addItem("Init", itemId.intValue(), 1, newChar, null);
      if (item.getItemId() == 5588)
      {
        shortcut = new L2ShortCut(11, 0, 1, item.getObjectId(), -1, 1);
        newChar.registerShortCut(shortcut);
      }
      if (item.isEquipable())
      {
        newChar.getInventory().equipItemAndRecord(item);
      }
    }

    L2SkillLearn[] startSkills = SkillTreeTable.getInstance().getAvailableSkills(newChar, newChar.getClassId());
    for (int i = 0; i < startSkills.length; i++) {
      newChar.addSkill(SkillTable.getInstance().getInfo(startSkills[i].getId(), startSkills[i].getLevel()), true);
      if ((startSkills[i].getId() == 1001) || (startSkills[i].getId() == 1177)) {
        shortcut = new L2ShortCut(1, 0, 2, startSkills[i].getId(), 1, 1);
        newChar.registerShortCut(shortcut);
      }
      if (startSkills[i].getId() == 1216) {
        shortcut = new L2ShortCut(10, 0, 2, startSkills[i].getId(), 1, 1);
        newChar.registerShortCut(shortcut);
      }

    }

    L2GameClient.saveCharToDisk(newChar);
    newChar.deleteMe();

    CharSelectInfo cl = new CharSelectInfo(client.getAccountName(), client.getSessionId().playOkID1);
    client.getConnection().sendPacket(cl);
    client.setCharSelection(cl.getCharInfo());
  }
}