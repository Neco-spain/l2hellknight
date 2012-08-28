package net.sf.l2j.gameserver.network.clientpackets;

import gnu.trove.TIntIntHashMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.LoginServerThread.SessionKey;
import net.sf.l2j.gameserver.datatables.CharNameTable;
import net.sf.l2j.gameserver.datatables.CharTemplateTable;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.datatables.SkillTreeTable;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.instancemanager.QuestManager;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2ShortCut;
import net.sf.l2j.gameserver.model.L2SkillLearn;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.model.quest.QuestState;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.CharCreateFail;
import net.sf.l2j.gameserver.network.serverpackets.CharCreateOk;
import net.sf.l2j.gameserver.network.serverpackets.CharSelectInfo;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2PcTemplate;
import net.sf.l2j.gameserver.util.Util;
import org.mmocore.network.MMOConnection;

public final class CharacterCreate extends L2GameClientPacket
{
  private static final String _C__0B_CHARACTERCREATE = "[C] 0B CharacterCreate";
  private static Logger _log = Logger.getLogger(CharacterCreate.class.getName());
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
  }

  protected void runImpl()
  {
    if ((CharNameTable.getInstance().accountCharNumber(((L2GameClient)getClient()).getAccountName()) >= Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT) && (Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT != 0))
    {
      if (Config.DEBUG)
        _log.fine("Max number of characters reached. Creation failed.");
      CharCreateFail ccf = new CharCreateFail(1);
      sendPacket(ccf);
      return;
    }
    if (CharNameTable.getInstance().doesCharNameExist(_name))
    {
      if (Config.DEBUG)
        _log.fine("charname: " + _name + " already exists. creation failed.");
      CharCreateFail ccf = new CharCreateFail(2);
      sendPacket(ccf);
      return;
    }
    if ((_name.length() < 3) || (_name.length() > 16) || (!Util.isAlphaNumeric(_name)) || (!isValidName(_name)))
    {
      if (Config.DEBUG)
        _log.fine("charname: " + _name + " is invalid. creation failed.");
      CharCreateFail ccf = new CharCreateFail(3);
      sendPacket(ccf);
      return;
    }

    for (String nick : Config.LIST_NOT_ALLOWED_NICKS)
    {
      if (_name.equalsIgnoreCase(nick))
      {
        CharCreateFail ccf = new CharCreateFail(3);
        sendPacket(ccf);
        return;
      }
    }
    if (Config.DEBUG) {
      _log.fine("charname: " + _name + " classId: " + _classId);
    }
    L2PcTemplate template = CharTemplateTable.getInstance().getTemplate(_classId);
    if ((template == null) || (template.classBaseLevel > 1))
    {
      CharCreateFail ccf = new CharCreateFail(0);
      sendPacket(ccf);
      return;
    }

    int objectId = IdFactory.getInstance().getNextId();
    L2PcInstance newChar = L2PcInstance.create(objectId, template, ((L2GameClient)getClient()).getAccountName(), _name, _hairStyle, _hairColor, _face, _sex != 0);

    newChar.setCurrentHp(newChar.getMaxHp());
    newChar.setCurrentCp(0.0D);
    newChar.setCurrentMp(newChar.getMaxMp());

    CharCreateOk cco = new CharCreateOk();
    sendPacket(cco);

    initNewChar((L2GameClient)getClient(), newChar);
  }
  public static boolean isValidName(String text) {
    boolean result = true;
    String test = text;
    Pattern pattern;
    try {
      pattern = Pattern.compile(Config.CNAME_TEMPLATE);
    }
    catch (PatternSyntaxException e)
    {
      _log.warning("ERROR : Character name pattern of config is wrong!");
      pattern = Pattern.compile(".*");
    }
    Matcher regexp = pattern.matcher(test);
    if (!regexp.matches())
    {
      result = false;
    }
    return result;
  }

  private void initNewChar(L2GameClient client, L2PcInstance newChar)
  {
    if (Config.DEBUG) _log.fine("Character init start");
    L2World.getInstance().storeObject(newChar);

    L2PcTemplate template = newChar.getTemplate();

    newChar.addAdena("Init", Config.STARTING_ADENA, null, false);

    newChar.setXYZInvisible(template.spawnX, template.spawnY, template.spawnZ);

    if (Config.ENABLE_NEWCHAR_TITLE)
    {
      newChar.setTitle(Config.NEW_CHAR_TITLE);
    }
    else
    {
      newChar.setTitle("");
    }

    L2ShortCut shortcut = new L2ShortCut(0, 0, 3, 2, -1, 1);
    newChar.registerShortCut(shortcut);

    shortcut = new L2ShortCut(3, 0, 3, 5, -1, 1);
    newChar.registerShortCut(shortcut);

    shortcut = new L2ShortCut(10, 0, 3, 0, -1, 1);
    newChar.registerShortCut(shortcut);

    ItemTable itemTable = ItemTable.getInstance();
    L2Item[] items = template.getItems();
    for (int i = 0; i < items.length; i++)
    {
      L2ItemInstance item = newChar.getInventory().addItem("Init", items[i].getItemId(), 1, newChar, null);
      if (item.getItemId() == 5588)
      {
        shortcut = new L2ShortCut(11, 0, 1, item.getObjectId(), -1, 1);
        newChar.registerShortCut(shortcut);
      }
      if ((!item.isEquipable()) || (
        (newChar.getActiveWeaponItem() != null) && (item.getItem().getType2() != 0))) continue;
      newChar.getInventory().equipItemAndRecord(item);
    }

    if ((Config.ITEMS_ON_CREATE_CHAR != null) && (!Config.ITEMS_ON_CREATE_CHAR.isEmpty()))
    {
      for (int i : Config.ITEMS_ON_CREATE_CHAR.keys())
      {
        Integer item_id = Integer.valueOf(i);
        Integer item_count = Integer.valueOf(Config.ITEMS_ON_CREATE_CHAR.get(i));
        if ((item_id == null) || (item_count == null) || (Config.ITEMS_ON_CREATE_CHAR == null)) {
          continue;
        }
        newChar.getInventory().addItem("ItemsOnCreate", item_id.intValue(), item_count.intValue(), newChar, null);
      }
    }

    L2SkillLearn[] startSkills = SkillTreeTable.getInstance().getAvailableSkills(newChar, newChar.getClassId());
    for (int i = 0; i < startSkills.length; i++)
    {
      newChar.addSkill(SkillTable.getInstance().getInfo(startSkills[i].getId(), startSkills[i].getLevel()), true);
      if ((startSkills[i].getId() == 1001) || (startSkills[i].getId() == 1177)) {
        shortcut = new L2ShortCut(1, 0, 2, startSkills[i].getId(), 1, 1);
        newChar.registerShortCut(shortcut);
      }
      if (startSkills[i].getId() == 1216) {
        shortcut = new L2ShortCut(10, 0, 2, startSkills[i].getId(), 1, 1);
        newChar.registerShortCut(shortcut);
      }
      if (Config.DEBUG)
        _log.fine("adding starter skill:" + startSkills[i].getId() + " / " + startSkills[i].getLevel());
    }
    L2GameClient.saveCharToDisk(newChar);
    newChar.deleteMe();
    startTutorialQuest(newChar);

    CharSelectInfo cl = new CharSelectInfo(client.getAccountName(), client.getSessionId().playOkID1);
    client.getConnection().sendPacket(cl);
    client.setCharSelection(cl.getCharInfo());
    if (Config.DEBUG) _log.fine("Character init end");
  }

  public void startTutorialQuest(L2PcInstance player)
  {
    QuestState qs = player.getQuestState("255_Tutorial");
    Quest q = null;
    if (qs == null)
      q = QuestManager.getInstance().getQuest("255_Tutorial");
    if (q != null)
      q.newQuestState(player);
  }

  public String getType()
  {
    return "[C] 0B CharacterCreate";
  }
}